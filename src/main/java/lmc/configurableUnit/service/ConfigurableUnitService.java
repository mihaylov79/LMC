package lmc.configurableUnit.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.repository.ConfigurableUnitRepository;
import lmc.unit.model.Unit;
import lmc.unit.service.UnitService;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ConfigurableUnitService {

    private final SimpleUnitService simpleUnitService;
    private final ConfiguredUnitService configuredUnitService;
    private final ConfigurableUnitRepository configurableUnitRepository;
    private final UnitService unitService;

    @Autowired
    public ConfigurableUnitService(SimpleUnitService simpleUnitService, ConfiguredUnitService configuredUnitService, ConfigurableUnitRepository configurableUnitRepository, UnitService unitService) {
        this.simpleUnitService = simpleUnitService;
        this.configuredUnitService = configuredUnitService;
        this.configurableUnitRepository = configurableUnitRepository;
        this.unitService = unitService;
    }


    public ConfigurableUnit createUnit(CreateNewConfiguredUnitRequest request) {
        Unit unit = unitService.getUnitByCode(request.getCode());

        if (request.getOptionCodes() == null || request.getOptionCodes().isEmpty()) {
            // Няма опции → създава SimpleUnit
            return simpleUnitService.createSimpleUnit(request);
        } else {
            // Има опции → създава ConfiguredUnit
            return configuredUnitService.createConfiguredUnit(request);
        }
    }


    public List<ConfigurableUnit> getAllUnits() {
        return configurableUnitRepository.findAll().stream().toList();
    }
}
