package lmc.configredUnit.service;


import lmc.configredUnit.model.ConfiguredUnit;
import lmc.configredUnit.repository.ConfiguredUnitRepository;
import lmc.option.model.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ConfiguredUnitService {

    private final ConfiguredUnitRepository repository;

    @Autowired
    public ConfiguredUnitService(ConfiguredUnitRepository repository) {
        this.repository = repository;
    }

    public ConfiguredUnit createConfiguredUnit(UUID unitId, List<Option> options, int quantity){


        return null;
    }


    public BigDecimal unitTotalPrice(ConfiguredUnit unit){
        BigDecimal unitPrice = unit.getUnit().getPrice()
                .multiply(new BigDecimal(unit.getQuantity()));

        BigDecimal optionsPrice = unit.getOptions().stream()
                .map(Option::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(new BigDecimal(unit.getQuantity()));

        return unitPrice.add(optionsPrice);
    }
}
