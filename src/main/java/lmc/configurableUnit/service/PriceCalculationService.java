package lmc.configurableUnit.service;

import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.model.ConfiguredUnit;
import lmc.configurableUnit.model.SimpleUnit;
import lmc.configuration.model.Configuration;
import lmc.option.model.Option;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
        return unit.getUnit().getPrice().multiply(new BigDecimal(unit.getQuantity()));
    }

    private BigDecimal calculateConfiguredUnitPrice(ConfiguredUnit unit){
        BigDecimal basePrice = unit.getUnit().getPrice()
                    .multiply(new BigDecimal(unit.getQuantity()));

        BigDecimal optionsPrice = unit.getOptions().stream()
                .map(Option::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(new BigDecimal(unit.getQuantity()));

        return basePrice.add(optionsPrice);

    }

    public BigDecimal calculateConfigurationTotalPrice(Configuration configuration){
        return configuration.getIncludedUnits().stream()
                .map(this::calculateConfigurableUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateConfigurationTotalPrice(List<ConfigurableUnit>units){
        return units.stream().map(this::calculateConfigurableUnitPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
