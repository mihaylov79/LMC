package lmc.configuration.service;


import jakarta.transaction.Transactional;
import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configurableUnit.service.PriceCalculationService;
import lmc.configuration.model.Configuration;
import lmc.configuration.repository.ConfigurationRepository;
import lmc.web.dto.CreateNewConfigurationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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


    public Configuration createNewConfiguration(CreateNewConfigurationRequest request){
        List<ConfigurableUnit>units = request.getUnits().stream()
                .map(configurableUnitService::createUnit).toList();


        Configuration configuration = Configuration.builder()
                .imageUrl(request.getImgUrl())
                .code(request.getCode())
                .line(request.getLine())
                .type(request.getType())
                .description(request.getDescription())
                .model(request.getModel())
                .includedUnits(units)
                .active(true)
                .build();

        BigDecimal totalPrice = calculationService.calculateConfigurationTotalPrice(configuration);

        Configuration configurationWithPrice = configuration.toBuilder()
                .totalPrice(totalPrice)
                .priceUpdateDate(LocalDate.now())
                .build();

        return configurationRepository.save(configurationWithPrice);
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

    public Configuration findConfigurationById(UUID id){
        return configurationRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Конфигурация с идентификация: [ %s ] не беще открита"
                .formatted(id)));
    }

    public List<Configuration> getAllConfigurations(){
        return configurationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

}
