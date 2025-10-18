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
        String code = unit.getCode();

        return repository.findByCodeAndActiveTrue(code).orElseGet(() -> {
            SimpleUnit simpleUnit = SimpleUnit.builder()
                    .code(code)
                    .unit(unit)
                    .active(true)
                    .build();

            return repository.save(simpleUnit);
        });

    }

//    public SimpleUnit findSimpleUnitByCode(String code) {
//         return repository.findByCodeAndActiveTrue(code, true)
//                 .orElseThrow(() -> new IllegalArgumentException("Елемент с идентификация: %s не беше открит!"
//                         .formatted(code)));
//    }
}
