package lmc.configuration.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configurableUnit.service.PriceCalculationService;
import lmc.configuration.model.Configuration;
import lmc.configuration.repository.ConfigurationRepository;
import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.web.dto.CreateNewConfigurationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final ConfigurableUnitService configurableUnitService;
    private final PriceCalculationService calculationService;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository, ConfigurableUnitService configurableUnitService, PriceCalculationService calculationService) {
        this.configurationRepository = configurationRepository;
        this.configurableUnitService = configurableUnitService;
        this.calculationService = calculationService;
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

        // create ConfigurationUnit objects and attach them to the same configuration
        request.getUnits().forEach(dto -> {
            ConfigurableUnit cu = configurableUnitService.findUnitById(dto.getConfigurableUnitId());
            ConfigurationUnit configurationUnit = ConfigurationUnit.builder()
                    .configurableUnit(cu)
                    .quantity(dto.getQuantity())
                    .build();

            // helper sets the back-reference: configurationUnit.setConfiguration(configuration)
            configuration.addIncludedUnit(configurationUnit);
        });

        // calculate and set price on the same configuration instance
        BigDecimal totalPrice = calculationService.calculateConfigurationTotalPrice(configuration);
        configuration.setTotalPrice(totalPrice);
        configuration.setPriceUpdateDate(LocalDate.now());

        // save single configuration (cascade will persist units)
        return configurationRepository.save(configuration);
    }

//    @Transactional
//    public Configuration createNewConfiguration(CreateNewConfigurationRequest request){
//
//        final Configuration baseconfiguration = Configuration.builder()
//                .imageUrl(request.getImgUrl())
//                .code(request.getCode())
//                .line(request.getLine())
//                .type(request.getType())
//                .description(request.getDescription())
//                .model(request.getModel())
//                .active(true)
//                .build();
//
//        List<ConfigurationUnit>units = request.getUnits().stream()
//                .map(dto -> {
//                    ConfigurableUnit unit = configurableUnitService.findUnitById(dto.getConfigurableUnitId());
//                    return ConfigurationUnit.builder()
//                            .configuration(baseconfiguration)
//                            .configurableUnit(unit)
//                            .quantity(dto.getQuantity())
//                            .build();
//                }).toList();
//
//        Configuration configurationWithUnits = baseconfiguration.toBuilder()
//                .includedUnits(units)
//                .build();
//
//        BigDecimal totalPrice = calculationService.calculateConfigurationTotalPrice(configurationWithUnits);
//
//        Configuration configurationWithPrice = configurationWithUnits.toBuilder()
//                .totalPrice(totalPrice)
//                .priceUpdateDate(LocalDate.now())
//                .build();
//
//        return configurationRepository.save(configurationWithPrice);
//    }


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

//    public Configuration findConfigurationById(UUID id){
//        return configurationRepository.findById(id)
//                .orElseThrow(()-> new IllegalArgumentException("Конфигурация с идентификация: [ %s ] не беще открита"
//                .formatted(id)));
//    }
//
//    public List<Configuration> getAllConfigurations(){
//        return  configurationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
//
//    }

    @Transactional(readOnly = true)
    public List<Configuration> getAllConfigurations(){
        return configurationRepository.findAllWithUnits();
    }

    @Transactional(readOnly = true)
    public Configuration findConfigurationById(UUID id){
        return configurationRepository.findByIdWithUnits(id)
                .orElseThrow(() -> new IllegalArgumentException("Конфигурация с идентификация: [ %s ] не беше открита".formatted(id)));
    }

}
