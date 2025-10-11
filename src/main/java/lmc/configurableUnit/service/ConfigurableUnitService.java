package lmc.configurableUnit.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ConfigurableUnitService {

    private final SimpleUnitService simpleUnitService;
    private final ConfiguredUnitService configuredUnitService;

    @Autowired
    public ConfigurableUnitService(SimpleUnitService simpleUnitService, ConfiguredUnitService configuredUnitService) {
        this.simpleUnitService = simpleUnitService;
        this.configuredUnitService = configuredUnitService;
    }


    public ConfigurableUnit createUnit(CreateNewConfiguredUnitRequest request) {
        if (request.getOptionIds() == null || request.getOptionIds().isEmpty()) {
            // Няма опции → създава SimpleUnit
            return simpleUnitService.createSimpleUnit(request);
        } else {
            // Има опции → създава ConfiguredUnit
            return configuredUnitService.createConfiguredUnit(request);
        }
    }


}
