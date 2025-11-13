# ConfigurationService — пълно обяснение

Дата: 2025-11-13

Този документ съдържа подробно и систематично обяснение на логиката, която беше приложена върху `ConfigurationService` и свързаните компоненти (OptionUtils, ConfigurationUnit, signature lookup). Включено е и обяснение на термина "canonical signature" и примери.

Съдържание
- Цел и високo ниво
- Вход/изход: откъде идват данните и къде отиват
- Пълен walkthrough по методите (ред по ред поведение)
- Формат и значение на canonical signature
- Защо използваме Set при събиране на IDs
- Какво прави `flatMap` (с пример в контекста)
- Какво означава "агрегира" (с примери)
- Edge-cases, race-conditions и препоръки
- Бързи команди за локална проверка и файлове за преглед

---

Цел и високo ниво
------------------
`ConfigurationService` управлява създаването и промяната на потребителските "конфигурации". Това означава:
- създаване на `Configuration` (с линии/units и опции),
- добавяне/премахване на `ConfigurableUnit` в/от конфигурация,
- гарантиране, че за една и съща конфигурация не се появяват излишни дубли на опции,
- оптимизации: batch-зареждане на опции и бърз lookup чрез canonical signature.

Основната идея: дефинициите на единици/опции (в `ConfigurableUnit` / `ConfiguredUnitOption`) са "типове" — количествата се задават само когато тези типове влязат в конкретна `Configuration` (резултатът е `ConfigurationUnit` с `ConfigurationUnitOption` и quantity).

Вход/изход: откъде идват данните и къде отиват
-----------------------------------------------
- Вход: DTO-та от контролера (напр. `CreateNewConfigurationRequest`, `OptionSelectionDTO`). Те идват от UI/REST (формуляри или requests).
- Вътрешно: `ConfigurationService` използва `ConfigurableUnitService` (за дефиниции на unit), `OptionService` (за зареждане на Option entities) и `PriceCalculationService` (за изчисляване на цена).
- Изход: JPA entities записани чрез `configurationRepository` и `configurationUnitRepository` (таблици: `configurations`, `configuration_units`, `configuration_unit_options`).

Walkthrough по методите (важните публични/вътрешни)
---------------------------------------------------
(Тук обяснявам логиката в кодов ред — намери съответните методи в `src/main/java/lmc/configuration/service/ConfigurationService.java`.)

1) `createNewConfiguration(CreateNewConfigurationRequest request)`
- Вход: request с основни полета (image, code, description...) и списък units.
- Стъпки:
  1. Събира всички optionId-та от всички units (stream + flatMap) и създава Set<UUID> `allOptionIds`.
  2. Ако `allOptionIds` не е празен → прави 1 batch call: `optionService.getOptionsByIds(List)` и превръща резултата в Map<UUID, Option> `optionMap`.
     - Защо: една обща заявка вместо N по-малки е по-ефективна.
  3. За всеки `dto` unit: зарежда `ConfigurableUnit cu = configurableUnitService.findUnitById(dto.getConfigurableUnitId())`.
  4. Вика `createConfigurationUnit(cu, dto.getQuantity(), dto.getOptionSelections(), optionMap)` за да конструира `ConfigurationUnit` с `ConfigurationUnitOption` вътре.
  5. Добавя unit към `configuration`, изчислява обща цена и записва `configurationRepository.save(configuration)`.

2) `createConfigurationUnit(ConfigurableUnit cu, int quantity, List<OptionSelectionDTO> optionSelections, Map<UUID, Option> optionMap)`
- Създава `ConfigurationUnit` entity (в паметта) с `configurableUnit` и `quantity`.
- Ако има `optionSelections`:
  - вика `addOptionsFromSelections(newUnit, optionSelections, optionMap)` — този метод агрегира дублираните избори и добавя един `ConfigurationUnitOption` на Option (с общото quantity).
- Ако няма `optionSelections`:
  - вика `addTemplateOptionsIfNeeded(newUnit, cu)` — копира template options (типовете) от `ConfiguredUnit` в unit-а с quantity = 1.
- След добавянето на options: изчислява canonical signature чрез `OptionUtils.signatureFromConfigUnitOptions(newUnit.getOptions())` и сетва `newUnit.setOptionsSignature(sig)`.
- Връща `ConfigurationUnit` (готов за добавяне в `Configuration`).

3) `addOptionsFromSelections(ConfigurationUnit unit, List<OptionSelectionDTO> selections, Map<UUID, Option> optionMap)`
- Основна цел: да превърне селекциите на потребителя в реални `ConfigurationUnitOption` записи *без дубли*.
- Как работи:
  1. Ако `selections` е празно: return.
  2. `aggregated = OptionUtils.toMapFromSelections(selections)` — това прави Map<optionId, totalQuantity> (сумира duplicate в DTO).
  3. За всеки (optionId, qty) в aggregated: взима `Option` от `optionMap` (ако е подаден), иначе fallback към `optionService.getOptionsByIds(List.of(optionId))` и създава `ConfigurationUnitOption(option, quantity=qty)` и `unit.addOption(cuo)`.
- Крайна цел: в `unit.getOptions()` ще има един ред за всяка optionId с правилната сума.

4) `addTemplateOptionsIfNeeded(ConfigurationUnit unit, ConfigurableUnit cu)`
- Ако `cu` е `ConfiguredUnit`, вземи `templateOptions` (list of ConfiguredUnitOption), и за всяка опция добави `ConfigurationUnitOption(option, quantity=1)`.
- Защо quantity=1: template-опцията е тип (не носи количество) — количеството се определя при добавяне в конфигурация ако потребителят посочи друго.

5) `findMatchingUnit(Configuration configuration, UUID configurableUnitId, List<OptionSelectionDTO> optionSelections)`
- Цел: да намерим дали вече съществува `ConfigurationUnit` в дадената configuration, който съвпада по composition с искания.
- Стъпки/приоритет:
  1. Ако са подадени `optionSelections`: превърни ги в map `req`, изчисли `sig = OptionUtils.signatureFromMap(req)`.
     - Опитай fast-path lookup: `configurationUnitService.findConfigurationUnitByConfigurationIdAndConfigurableUnitIdAndSignature(configuration.getId(), configurableUnitId, sig)` — това е DB lookup, което е бързо когато signature е записан.
     - Ако няма repo match → fallback към in-memory сравнение: за всяка included unit сравни `OptionUtils.toMapFromConfigurationUnitOptions(u.getOptions()).equals(req)`.
  2. Ако няма `optionSelections`: ако `ConfigurableUnit` е `ConfiguredUnit` и има template options, опитай template match (signature от template map) с fast-path + fallback.
  3. Ако нищо не помогне → вторичен fallback: просто `findConfigurationUnitByConfigurationIdAndConfigurableUnitId(configurationId, configurableUnitId)` (match по unit id без опции).

6) `addConfigurableUnit` и `removeConfigurableUnit`
- `addConfigurableUnit`:
  - load configuration, findMatchingUnit(...) → ако match: existing.setQuantity(existing.getQuantity() + quantity) — т.е. увеличаваме броя units; ако няма match: създаваме нов unit.
  - Save configuration (cascade) и пресмятаме price.
- `removeConfigurableUnit`:
  - findMatchingUnit(...), ако match: ако existing.quantity <= quantity → премахваме unit, иначе намаляваме quantity.

Canonical signature — какво е и защо
---------------------------------
- Definition (на общ език): canonical signature е текстово (string) представяне, което описва точно композицията на опции в един `ConfigurationUnit`: за всяка опция съдържа нейния ID и количеството. Форматът е детерминистичен и canonical — т.е. две еквивалентни композиции ще дадат точно един и същ string.
- Формат в нашата имплементация: "{optionId}:{quantity};{optionId2}:{quantity2};..." където опциятe са сортирани по optionId (за детерминизъм) и разделени със `;`.
- Пример: "11111111-aaaa-...:3;22222222-bbbb-...:1" означава optionId1 с qty 3 и optionId2 с qty 1.
- Защо е полезен:
  - Работи като бърз и сравнително малък ключ за exact-match в DB.
  - Позволява да правиш fast-path lookup по `(configuration_id, configurable_unit_id, options_signature)` вместо да сравняваш обекти в паметта.
  - Лесно се генерира от map<optionId, qty>.

Set vs List — защо на места използвахме `Set`
--------------------------------------------
- Когато събираме всички option IDs (преди batch load), използваме `Set<UUID>` за уникалност. Причината е да не изпратим дублирани IDs към `optionService.getOptionsByIds(...)` — няма нужда да искаме един и същ Option многократно.
- `List` е редно да се използва когато е важно реда или когато допускаме дубли; `Set` е за уникални стойности. В нашия случай за batch load имаме нужда от уникални IDs.

Какво е `flatMap` (с прост пример)
---------------------------------
- `flatMap` в Java Streams приема всеки елемент от входния stream и го трансформира в *Stream* от други елементи; след това "изравя" всички тези вътрешни stream-ове в един общ stream.
- Пример в контекста: имаме list of units; всеки unit има List<OptionSelectionDTO>. Ако искаме един stream от всички OptionSelectionDTO-та от всички units:
  ```java
  request.getUnits().stream()
         .flatMap(dto -> dto.getOptionSelections() == null ? Stream.empty() : dto.getOptionSelections().stream())
         .map(OptionSelectionDTO::getOptionId)
         ...
  ```
  тук `flatMap` отваря всеки списък optionSelections и слепва (flatten) всички им елементи в един stream, който после можем да map-ваме или collect-ваме.

Какво значи "агрегира" (в контекста на кода)
----------------------------------------------
- "Агрегира" означава "групира и комбинира" — в нашия контекст: при подаване на няколко selection-а за една и съща optionId ние не създаваме отделни редове, а "агрегираме" тези selection-и в един запис с общото количество.
- Пример: ако потребителят е избрал Option C два пъти в един unit: първо qty=1 и после qty=2 в DTO-то, агрегацията прави map: C -> 3 и създава един `ConfigurationUnitOption` с quantity=3.
- Технически: `OptionUtils.toMapFromSelections(selections)` прави тази агрегация — тя `collect(Collectors.toMap(optionId -> quantity, mergeFunction = Integer::sum))`.

Edge-cases, race-conditions и препоръки
--------------------------------------
- Конкурентни добавяния: двa едновременни request-а могат да доведат до две идентични `ConfigurationUnit` rows. Препоръчително е да добавиш DB unique constraint на `(configuration_id, configurable_unit_id, options_signature)` и да имплементираш retry/merge logic при DuplicateKey.
- Missing Option: кодът хвърля IllegalArgumentException когато дадено optionId не е намерено — можеш да валидираш входа или да върнеш user-friendly error.

Бързи команди за локална проверка
---------------------------------
```bash
# стартирай единичния тест за агрегация
chmod +x mvnw
./mvnw -Dtest=lmc.configuration.service.ConfigurationServiceAggregationTest test

# компилация без тестове
./mvnw -DskipTests package

# стартирай приложението
./mvnw spring-boot:run
```

Ключови файлове за преглед
- `src/main/java/lmc/configuration/service/ConfigurationService.java`
- `src/main/java/lmc/configuration/util/OptionUtils.java`
- `src/main/java/lmc/configurationUnit/model/ConfigurationUnit.java` (optionsSignature field)
- `src/main/java/lmc/configurationUnit/repository/ConfigurationUnitRepository.java`
- `src/test/java/lmc/configuration/service/ConfigurationServiceAggregationTest.java`

---

Ако искаш, след като прегледаш този md файл, мога да:
- добавя кратки JavaDoc коментари в `ConfigurationService` над публичните методи (за да е по-лесно четим), или
- разгледаме ред по ред конкретен метод (кажи кой ред/метод) и ще го обясня детайлно.

Добре дошъл да питаш каквото и да е — готов съм да преминем ред по ред в момента, който си готов.
