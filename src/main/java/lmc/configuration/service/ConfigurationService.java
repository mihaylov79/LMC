package lmc.configuration.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configurableUnit.service.PriceCalculationService;
import lmc.configuration.model.Configuration;
import lmc.configuration.repository.ConfigurationRepository;
import lmc.web.dto.CreateNewConfigurationRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final ConfigurableUnitService configurableUnitService;
    private final PriceCalculationService calculationService;

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

}
