package lmc.configurableUnit.service;


import lmc.configurableUnit.model.SimpleUnit;
import lmc.configurableUnit.repository.SimpleUnitRepository;
import lmc.unit.model.Unit;
import lmc.unit.service.UnitService;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SimpleUnitService {

    private final SimpleUnitRepository repository;
    private final UnitService unitService;


    @Autowired
    public SimpleUnitService(SimpleUnitRepository repository, UnitService unitService) {
        this.repository = repository;
        this.unitService = unitService;
    }


    public SimpleUnit createSimpleUnit(CreateNewConfiguredUnitRequest request) {
        Unit unit = unitService.getUnitById(request.getUnitId());

        SimpleUnit simpleUnit = SimpleUnit.builder()
                .unit(unit)
                .quantity(request.getQuantity())
                .active(true)
                .build();

        return repository.save(simpleUnit);
    }
}
