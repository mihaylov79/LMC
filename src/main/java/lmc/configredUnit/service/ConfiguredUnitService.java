package lmc.configredUnit.service;


import lmc.configredUnit.model.ConfiguredUnit;
import lmc.option.model.Option;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ConfiguredUnitService {


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
