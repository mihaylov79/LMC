package lmc.configurableUnit.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.repository.ConfigurableUnitRepository;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ConfigurableUnitService {

    private final SimpleUnitService simpleUnitService;
    private final ConfiguredUnitService configuredUnitService;
    private final ConfigurableUnitRepository configurableUnitRepository;

    @Autowired
    public ConfigurableUnitService(SimpleUnitService simpleUnitService, ConfiguredUnitService configuredUnitService, ConfigurableUnitRepository configurableUnitRepository) {
        this.simpleUnitService = simpleUnitService;
        this.configuredUnitService = configuredUnitService;
        this.configurableUnitRepository = configurableUnitRepository;
    }


    public ConfigurableUnit createUnit(CreateNewConfiguredUnitRequest request) {
        if (request.getOptionIds() == null || request.getOptionIds().isEmpty()) {
            return simpleUnitService.createSimpleUnit(request);
        } else {
            return configuredUnitService.createConfiguredUnit(request);
        }
    }



    public List<ConfigurableUnit> getAllUnits() {
        return configurableUnitRepository.findAll().stream().toList();
    }


    public ConfigurableUnit findUnitById(UUID unitId) {
        return configurableUnitRepository.findById(unitId).orElseThrow(() -> new IllegalArgumentException("Елемент с идентификация : [ %s } не беше открит!".formatted(unitId)));
    }
}
