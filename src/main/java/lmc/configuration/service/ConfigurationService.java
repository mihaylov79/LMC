package lmc.configuration.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.model.ConfiguredUnit;
import lmc.configurableUnit.model.ConfiguredUnitOption;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configurableUnit.service.PriceCalculationService;
import lmc.configuration.model.Configuration;
import lmc.configuration.repository.ConfigurationRepository;
import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.configurationUnit.service.ConfigurationUnitService;
import lmc.configurationUnitOption.model.ConfigurationUnitOption;
import lmc.option.model.Option;
import lmc.option.service.OptionService;
import lmc.web.dto.CreateNewConfigurationRequest;
import lmc.web.dto.OptionSelectionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final ConfigurableUnitService configurableUnitService;
    private final PriceCalculationService calculationService;
    private final ConfigurationUnitService configurationUnitService;
    private final OptionService optionService;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository, ConfigurableUnitService configurableUnitService, PriceCalculationService calculationService, ConfigurationUnitService configurationUnitService, OptionService optionService) {
        this.configurationRepository = configurationRepository;
        this.configurableUnitService = configurableUnitService;
        this.calculationService = calculationService;
        this.configurationUnitService = configurationUnitService;
        this.optionService = optionService;
    }

    @Transactional
    public Configuration createNewConfiguration(CreateNewConfigurationRequest request){

        // build a single mutable Configuration instance
        final Configuration configuration = Configuration.builder()
                .imageUrl(request.getImgUrl())
                .code(request.getCode())
                .line(request.getLine())
                .type(request.getType())
                .description(request.getDescription())
                .model(request.getModel())
                .active(true)
                .build();


        // map DTO units -> ConfigurationUnit (include per-config option quantities)
        request.getUnits().forEach(dto -> {
            ConfigurableUnit cu = configurableUnitService.findUnitById(dto.getConfigurableUnitId());
            ConfigurationUnit configurationUnit = ConfigurationUnit.builder()
                    .configurableUnit(cu)
                    .quantity(dto.getQuantity())
                    .build();

        if (dto.getOptionSelections() != null) {
            dto.getOptionSelections().stream()
                    .filter(s -> s != null && s.getOptionId() != null)
                    .forEach(s -> {
                        // load option (by id) - OptionService returns list for ids
                        Option opt = optionService.getOptionsByIds(List.of(s.getOptionId())).stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Option not found: " + s.getOptionId()));
                        ConfigurationUnitOption cuo = ConfigurationUnitOption.builder()
                                .option(opt)
                                .quantity(Math.max(1, s.getQuantity()))
                                .build();
                        configurationUnit.addOption(cuo);
                    });
        }

        configuration.addIncludedUnit(configurationUnit);
        });

        // calculate and set price on the same configuration instance
        BigDecimal totalPrice = calculationService.calculateConfigurationTotalPrice(configuration);
        configuration.setTotalPrice(totalPrice);
        configuration.setPriceUpdateDate(LocalDate.now());

        // save single configuration (cascade will persist units)
        return configurationRepository.save(configuration);
    }



    @Transactional
    public Configuration updateConfigurationPrice(UUID configurationId){
        Configuration configuration = findConfigurationById(configurationId);

        BigDecimal newPrice = calculationService.calculateConfigurationTotalPrice(configuration);

        Configuration updatedConfiguration = configuration.toBuilder()
                .totalPrice(newPrice)
                .priceUpdateDate(LocalDate.now())
                .build();
        return configurationRepository.save(updatedConfiguration);
    }
    @Transactional
    public int updateAllConfigurationsPrices(){
        List<Configuration>configurations = getAllConfigurations();


        List<Configuration> updatedConfigurations = configurations.stream()
                .map(configuration -> {
                    BigDecimal newPrice = calculationService.calculateConfigurationTotalPrice(configuration);
                    return configuration.toBuilder()
                            .totalPrice(newPrice)
                            .priceUpdateDate(LocalDate.now())
                            .build();
                })
                .toList();
        configurationRepository.saveAll(updatedConfigurations);
        return updatedConfigurations.size();
    }



    @Transactional(readOnly = true)
    public List<Configuration> getAllConfigurations(){
        return configurationRepository.findAllWithUnits();
    }

    @Transactional(readOnly = true)
    public Configuration findConfigurationById(UUID id){
        return configurationRepository.findByIdWithUnits(id)
                .orElseThrow(() -> new IllegalArgumentException("Конфигурация с идентификация: [ %s ] не беше открита".formatted(id)));
    }

    @Transactional
    public Configuration addConfigurableUnit(UUID configurationId, UUID configurableUnitId, int quantity, List<OptionSelectionDTO> optionSelections) {
        if (quantity <= 0) throw new IllegalArgumentException("Количеството трябва да бъде положителна стойност!");
        Configuration configuration = findConfigurationById(configurationId);

        Optional<ConfigurationUnit> existingOpt = configurationUnitService
                .findConfigurationUnitByConfigurationIdAndConfigurableUnitId(configurationId, configurableUnitId);

        if (existingOpt.isPresent()) {
            ConfigurationUnit existing = existingOpt.get();

            if (optionSelections == null || optionSelections.isEmpty()) {
                existing.setQuantity(existing.getQuantity() + quantity);
            } else {
                // build requested map id->qty
                Map<UUID,Integer> req = optionSelections.stream()
                        .filter(s -> s != null && s.getOptionId() != null)
                        .collect(Collectors.toMap(OptionSelectionDTO::getOptionId, s -> Math.max(1, s.getQuantity()), Integer::sum));

                /* Java: fix null-safety when building existingMap */
                Map<UUID,Integer> existingMap = (existing.getOptions() == null ? List.<ConfigurationUnitOption>of() : existing.getOptions())
                        .stream()
                        .filter(o -> o.getOption() != null && o.getOption().getId() != null)
                        .collect(Collectors.toMap(o -> o.getOption().getId(), ConfigurationUnitOption::getQuantity, Integer::sum));

                if (existingMap.equals(req)) {
                    existing.setQuantity(existing.getQuantity() + quantity);
                } else {
                    // different option composition => create new configuration unit instance
                    ConfigurableUnit cu = configurableUnitService.findUnitById(configurableUnitId);
                    ConfigurationUnit newUnit = createConfigurationUnit(cu, quantity, optionSelections);
                    configuration.addIncludedUnit(newUnit);
                }
            }
        } else {
            ConfigurableUnit cu = configurableUnitService.findUnitById(configurableUnitId);
            ConfigurationUnit newUnit = createConfigurationUnit(cu, quantity, optionSelections);
            configuration.addIncludedUnit(newUnit);
        }

        BigDecimal totalPrice = calculationService.calculateConfigurationTotalPrice(configuration);
        configuration.setTotalPrice(totalPrice);
        configuration.setPriceUpdateDate(LocalDate.now());
        return configurationRepository.save(configuration);
    }


    @Transactional
    public Configuration removeConfigurableUnit(UUID configurationId, UUID configurableUnitId, int quantity, List<OptionSelectionDTO> optionSelections) {
        Configuration configuration = findConfigurationById(configurationId);

        // find matching ConfigurationUnit: prefer exact options match when optionSelections provided
        Optional<ConfigurationUnit> match = Optional.empty();

        if (optionSelections != null && !optionSelections.isEmpty()) {
            Map<UUID,Integer> req = buildMapFromSelections(optionSelections);

            match = configuration.getIncludedUnits().stream()
                    .filter(u -> u.getConfigurableUnit() != null && configurableUnitId.equals(u.getConfigurableUnit().getId()))
                    .filter(u -> buildMapFromConfigUnitOptions(u.getOptions()).equals(req))
                    .findFirst();
        } else {
            // No selections provided: prefer a unit that matches the ConfiguredUnit template options (quantity = 1)
            try {
                ConfigurableUnit cu = configurableUnitService.findUnitById(configurableUnitId);
                if (cu instanceof ConfiguredUnit) {
                    var templateOptions = ((ConfiguredUnit) cu).getOptions();
                    Map<UUID, Integer> templateMap = buildMapFromConfiguredUnitOptions(templateOptions);

                    if (!templateMap.isEmpty()) {
                        match = configuration.getIncludedUnits().stream()
                                .filter(u -> u.getConfigurableUnit() != null && configurableUnitId.equals(u.getConfigurableUnit().getId()))
                                .filter(u -> buildMapFromConfigUnitOptions(u.getOptions()).equals(templateMap))
                                .findFirst();
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // fallback to generic lookup below
            }
        }

        // fallback: if no exact/template match found, try the generic lookup
        if (match.isEmpty()) {
            match = configurationUnitService
                    .findConfigurationUnitByConfigurationIdAndConfigurableUnitId(configurationId, configurableUnitId);
        }

        if (match.isPresent()) {
            ConfigurationUnit existing = match.get();
            if (existing.getQuantity() <= quantity) {
                configuration.removeIncludedUnit(existing);
            } else {
                existing.setQuantity(existing.getQuantity() - quantity);
            }
        } else {
            throw new IllegalArgumentException("No matching configuration unit found to remove");
        }

        BigDecimal totalPrice = calculationService.calculateConfigurationTotalPrice(configuration);
        configuration.setTotalPrice(totalPrice);
        configuration.setPriceUpdateDate(LocalDate.now());
        return configurationRepository.save(configuration);
    }

    @Transactional
    public Configuration updateConfiguration(UUID configurationId, CreateNewConfigurationRequest request){
        Configuration configuration = findConfigurationById(configurationId);

        configuration = configuration.toBuilder()
                .imageUrl(request.getImgUrl())
                .code(request.getCode())
                .line(request.getLine())
                .type(request.getType())
                .model(request.getModel())
                .description(request.getDescription())
                .build();

        // NOTE: if units changed mapping needed; for now just recompute price using existing includedUnits
        BigDecimal newPrice = calculationService.calculateConfigurationTotalPrice(configuration);
        configuration = configuration.toBuilder()
                .totalPrice(newPrice)
                .priceUpdateDate(LocalDate.now())
                .build();
        return configurationRepository.save(configuration);

    }

    @Transactional
    public void disableConfiguration(UUID configurationId){
        Configuration configuration = findConfigurationById(configurationId);

        configuration = configuration.toBuilder()
                .active(false)
                .build();

        configurationRepository.save(configuration);
    }


    private ConfigurationUnit createConfigurationUnit(ConfigurableUnit cu, int quantity, List<OptionSelectionDTO> optionSelections) {
        ConfigurationUnit newUnit = ConfigurationUnit.builder()
                .configurableUnit(cu)
                .quantity(quantity)
                .build();

        if (optionSelections != null && !optionSelections.isEmpty()) {
            addOptionsFromSelections(newUnit, optionSelections);
        } else {
            addTemplateOptionsIfNeeded(newUnit, cu);
        }

        return newUnit;
    }

    private void addOptionsFromSelections(ConfigurationUnit unit, List<OptionSelectionDTO> selections) {
        selections.stream()
                .filter(s -> s != null && s.getOptionId() != null)
                .forEach(s -> {
                    Option opt = optionService.getOptionsByIds(List.of(s.getOptionId())).stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + s.getOptionId()));
                    ConfigurationUnitOption cuo = ConfigurationUnitOption.builder()
                            .option(opt)
                            .quantity(Math.max(1, s.getQuantity()))
                            .build();
                    unit.addOption(cuo);
                });
    }

    private void addTemplateOptionsIfNeeded(ConfigurationUnit unit, ConfigurableUnit cu) {
        if (!(cu instanceof ConfiguredUnit)) return;
        List<ConfiguredUnitOption> templateOptions = ((ConfiguredUnit) cu).getOptions();
        if (templateOptions == null || templateOptions.isEmpty()) return;

        templateOptions.stream()
                .filter(Objects::nonNull)
                .map(ConfiguredUnitOption::getOption)
                .filter(Objects::nonNull)
                .map(opt -> ConfigurationUnitOption.builder()
                        .option(opt)
                        .quantity(1)
                        .build())
                .forEach(unit::addOption);
    }

    private Map<UUID, Integer> buildMapFromSelections(List<OptionSelectionDTO> selections) {
        if (selections == null || selections.isEmpty()) return Map.of();
        return selections.stream()
                .filter(s -> s != null && s.getOptionId() != null)
                .collect(Collectors.toMap(
                        OptionSelectionDTO::getOptionId,
                        s -> Math.max(1, s.getQuantity()),
                        Integer::sum
                ));
    }

    private Map<UUID, Integer> buildMapFromConfigUnitOptions(List<ConfigurationUnitOption> options) {
        if (options == null || options.isEmpty()) return Map.of();
        return options.stream()
                .filter(o -> o != null && o.getOption() != null && o.getOption().getId() != null)
                .collect(Collectors.toMap(
                        o -> o.getOption().getId(),
                        ConfigurationUnitOption::getQuantity,
                        Integer::sum
                ));
    }

    private Map<UUID, Integer> buildMapFromConfiguredUnitOptions(List<ConfiguredUnitOption> templateOptions) {
        if (templateOptions == null || templateOptions.isEmpty()) return Map.of();
        return templateOptions.stream()
                .filter(Objects::nonNull)
                .map(ConfiguredUnitOption::getOption)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Option::getId,
                        o -> 1,
                        Integer::sum
                ));
    }

}
