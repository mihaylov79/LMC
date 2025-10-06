package lmc.unit.service;

import lmc.unit.model.CurrencyType;
import lmc.unit.model.Unit;
import lmc.unit.repository.UnitRepository;
import lmc.web.dto.CreateNewUnitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UnitService {

    private final UnitRepository unitRepository;

    @Autowired
    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    public Unit createNewUnit(CreateNewUnitRequest request){

        Optional<Unit> existingUnit = unitRepository.findByCode(request.getCode());

        if (existingUnit.isPresent()){
            throw new IllegalArgumentException("Елемент с код: %s вече съществува".formatted(request.getCode()));
        }

        Unit newUnit = Unit.builder()
                .imageUrl(request.getImageUrl())
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .size(request.getSize())
                .price(request.getPrice())
                .currency(CurrencyType.EUR)
                .build();

        return unitRepository.save(newUnit);
    }


    public Unit getUnitById(UUID unitId){
        return unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Елементът не е открит"));
    }


}
