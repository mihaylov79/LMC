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
import lmc.configuration.util.OptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


        // collect all option ids to batch-load Options once
        Set<UUID> allOptionIds = request.getUnits().stream()
                .flatMap(dto -> (dto.getOptionSelections() == null) ? Stream.empty() : dto.getOptionSelections().stream())
                .map(OptionSelectionDTO::getOptionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Map<UUID, Option> optionMap = allOptionIds.isEmpty()
                ? Collections.emptyMap()
                : optionService.getOptionsByIds(new ArrayList<>(allOptionIds)).stream()
                .collect(Collectors.toMap(Option::getId, o -> o));


        // map DTO units -> ConfigurationUnit (include per-config option quantities)
        request.getUnits().forEach(dto -> {
            ConfigurableUnit cu = configurableUnitService.findUnitById(dto.getConfigurableUnitId());
            ConfigurationUnit configurationUnit = createConfigurationUnit(cu, dto.getQuantity(), dto.getOptionSelections(), optionMap);
            configuration.addIncludedUnit(configurationUnit);
        });

        // calculate and set price on the same configuration instance
        BigDecimal totalPrice = calculationService.calculateConfigurationTotalPrice(configuration);
        configuration.setTotalPrice(totalPrice);
        configuration.setPriceUpdateDate(LocalDate.now());

        // save single configuration (cascade will persist units)
        return configurationRepository.save(configuration);
    }

    /**
     * Find a matching ConfigurationUnit inside a configuration following the priority:
     * 1) exact options match when selections provided
     * 2) template options match (for ConfiguredUnit)
     * 3) fallback to configurationUnitService lookup
     */
    private Optional<ConfigurationUnit> findMatchingUnit(Configuration configuration, UUID configurableUnitId, List<OptionSelectionDTO> optionSelections) {
        // fast path: if selections provided, compute signature and try repository lookup
        if (optionSelections != null && !optionSelections.isEmpty()) {
            Map<UUID,Integer> req = OptionUtils.toMapFromSelections(optionSelections);
            String sig = OptionUtils.signatureFromMap(req);
            Optional<ConfigurationUnit> bySig = configurationUnitService.findConfigurationUnitByConfigurationIdAndConfigurableUnitIdAndSignature(configuration.getId(), configurableUnitId, sig);
            if (bySig.isPresent()) return bySig;
            // fallback to in-memory matching if not found
            return configuration.getIncludedUnits().stream()
                    .filter(u -> u.getConfigurableUnit() != null && configurableUnitId.equals(u.getConfigurableUnit().getId()))
                    .filter(u -> OptionUtils.toMapFromConfigurationUnitOptions(u.getOptions()).equals(req))
                    .findFirst();
        }

        try {
            ConfigurableUnit cu = configurableUnitService.findUnitById(configurableUnitId);
            if (cu instanceof ConfiguredUnit) {
                var templateOptions = ((ConfiguredUnit) cu).getOptions();
                Map<UUID, Integer> templateMap = OptionUtils.toMapFromConfiguredUnitOptions(templateOptions);
                if (!templateMap.isEmpty()) {
                    // try signature lookup for template options
                    String templateSig = OptionUtils.signatureFromMap(templateMap);
                    Optional<ConfigurationUnit> byTplSig = configurationUnitService.findConfigurationUnitByConfigurationIdAndConfigurableUnitIdAndSignature(configuration.getId(), configurableUnitId, templateSig);
                    if (byTplSig.isPresent()) return byTplSig;

                    Optional<ConfigurationUnit> m = configuration.getIncludedUnits().stream()
                            .filter(u -> u.getConfigurableUnit() != null && configurableUnitId.equals(u.getConfigurableUnit().getId()))
                            .filter(u -> OptionUtils.toMapFromConfigurationUnitOptions(u.getOptions()).equals(templateMap))
                            .findFirst();
                    if (m.isPresent()) return m;
                }
            }
        } catch (IllegalArgumentException ignored) {
            // fallback below
        }

        return configurationUnitService.findConfigurationUnitByConfigurationIdAndConfigurableUnitId(configuration.getId(), configurableUnitId);
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
        int page = 0;
        final int pageSize = 200;
        int updated = 0;

        while (true) {
            org.springframework.data.domain.Page<Configuration> configs = configurationRepository.findAll(org.springframework.data.domain.PageRequest.of(page, pageSize));
            if (!configs.hasContent()) break;

            List<Configuration> updatedBatch = configs.getContent().stream()
                    .map(configuration -> {
                        BigDecimal newPrice = calculationService.calculateConfigurationTotalPrice(configuration);
                        return configuration.toBuilder()
                                .totalPrice(newPrice)
                                .priceUpdateDate(LocalDate.now())
                                .build();
                    })
                    .toList();

            configurationRepository.saveAll(updatedBatch);
            updated += updatedBatch.size();
            if (configs.getTotalPages() <= page + 1) break;
            page++;
        }

        return updated;
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

        // try to find a matching existing ConfigurationUnit using consolidated logic
        Optional<ConfigurationUnit> existingOpt = findMatchingUnit(configuration, configurableUnitId, optionSelections);
        if (existingOpt.isPresent()) {
            ConfigurationUnit existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + quantity);
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
        Optional<ConfigurationUnit> match = findMatchingUnit(configuration, configurableUnitId, optionSelections);

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


    // new overload: accepts preloaded optionMap for performance
    private ConfigurationUnit createConfigurationUnit(ConfigurableUnit cu, int quantity, List<OptionSelectionDTO> optionSelections, Map<UUID, Option> optionMap) {
        ConfigurationUnit newUnit = ConfigurationUnit.builder()
                .configurableUnit(cu)
                .quantity(quantity)
                .build();

        if (optionSelections != null && !optionSelections.isEmpty()) {
            addOptionsFromSelections(newUnit, optionSelections, optionMap);
        } else {
            addTemplateOptionsIfNeeded(newUnit, cu);
        }

        // compute and set canonical signature for faster lookups
        String sig = OptionUtils.signatureFromConfigUnitOptions(newUnit.getOptions());
        newUnit.setOptionsSignature(sig);

        return newUnit;
    }

    // keep existing signature but delegate to new overload with empty map
    private ConfigurationUnit createConfigurationUnit(ConfigurableUnit cu, int quantity, List<OptionSelectionDTO> optionSelections) {
        return createConfigurationUnit(cu, quantity, optionSelections, Collections.emptyMap());
    }

    private void addOptionsFromSelections(ConfigurationUnit unit, List<OptionSelectionDTO> selections, Map<UUID, Option> optionMap) {
        selections.stream()
                .filter(s -> s != null && s.getOptionId() != null)
                .forEach(s -> {
                    Option opt = optionMap.get(s.getOptionId());
                    if (opt == null) {
                        // fallback to service single-load if not available in map
                        opt = optionService.getOptionsByIds(List.of(s.getOptionId())).stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Option not found: " + s.getOptionId()));
                    }
                    ConfigurationUnitOption cuo = ConfigurationUnitOption.builder()
                            .option(opt)
                            .quantity(Math.max(1, s.getQuantity()))
                            .build();
                    unit.addOption(cuo);
                });
    }

    // keep original method but delegate to new method with empty map
    private void addOptionsFromSelections(ConfigurationUnit unit, List<OptionSelectionDTO> selections) {
        addOptionsFromSelections(unit, selections, Collections.emptyMap());
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

}
