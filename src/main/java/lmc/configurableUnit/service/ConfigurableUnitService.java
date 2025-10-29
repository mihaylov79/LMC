package lmc.configurableUnit.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.repository.ConfigurableUnitRepository;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (request.getOptionSelections() == null || request.getOptionSelections().isEmpty()) {
            return simpleUnitService.createSimpleUnit(request);
        } else {
            return configuredUnitService.createConfiguredUnit(request);
        }
    }



    @Transactional(readOnly = true)
    public List<ConfigurableUnit> getAllUnits() {
        List<ConfigurableUnit> all = configurableUnitRepository.findAll();

        // ensure subclass collections / relations are initialized while the session is open
        all.forEach(u -> {
            try {
                // touch base relation
                if (u.getUnit() != null) u.getUnit().getCode();
                // if this is a configured unit, initialize its options collection
                if (u instanceof lmc.configurableUnit.model.ConfiguredUnit) {
                    var cu = (lmc.configurableUnit.model.ConfiguredUnit) u;
                    if (cu.getOptions() != null) cu.getOptions().size();
                    // optionally touch nested option properties to be safe
                    if (cu.getOptions() != null) {
                        cu.getOptions().forEach(co -> {
                            if (co != null && co.getOption() != null) {
                                try { co.getOption().getCode(); } catch (Exception ignored) {}
                            }
                        });
                    }
                }
            } catch (Exception ignored) {
                // defensive: ignore initialization errors here, let template fail later if necessary
            }
        });

        return all;
    }


    public ConfigurableUnit findUnitById(UUID unitId) {
        return configurableUnitRepository.findById(unitId).orElseThrow(() -> new IllegalArgumentException("Елемент с идентификация : [ %s } не беше открит!".formatted(unitId)));
    }
}
