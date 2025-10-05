package lmc.unit.service;

import lmc.unit.model.Unit;
import lmc.unit.repository.UnitRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UnitService {

    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }


    public Unit getUnitById(UUID unitId){
        return unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Елементът не е открит"));
    }


}
