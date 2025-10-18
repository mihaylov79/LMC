package lmc.configurableUnit.service;

import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.model.ConfiguredUnit;
import lmc.configurableUnit.model.SimpleUnit;
import lmc.configuration.model.Configuration;
import lmc.option.model.Option;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PriceCalculationService {

    public BigDecimal calculateConfigurableUnitPrice(ConfigurableUnit unit){

        if (unit instanceof SimpleUnit){
            return calculateSimpleUnitPrice((SimpleUnit) unit);
        } else if (unit instanceof ConfiguredUnit) {
            return calculateConfiguredUnitPrice((ConfiguredUnit) unit);
        }
        throw new IllegalArgumentException("Неизвестен тип Конфигурационен елемент");
    }

    private BigDecimal calculateSimpleUnitPrice(SimpleUnit unit){
        return unit.getUnit().getPrice();
    }

    private BigDecimal calculateConfiguredUnitPrice(ConfiguredUnit unit){
        BigDecimal basePrice = unit.getUnit().getPrice();

        BigDecimal optionsPrice = unit.getOptions().stream()
                .map(Option::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(optionsPrice);

    }

    public BigDecimal calculateConfigurationTotalPrice(Configuration configuration){
        return configuration.getIncludedUnits().stream()
                .map(cu -> calculateConfigurableUnitPrice(cu.getConfigurableUnit())
                        .multiply(new BigDecimal(cu.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
