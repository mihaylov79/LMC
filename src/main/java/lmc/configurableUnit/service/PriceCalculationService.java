package lmc.configurableUnit.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.model.ConfiguredUnit;
import lmc.configurableUnit.model.SimpleUnit;
import lmc.configuration.model.Configuration;
import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.configurationUnitOption.model.ConfigurationUnitOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class PriceCalculationService {

    private final ConfigurableUnitService configurableUnitService;

    @Autowired
    public PriceCalculationService(ConfigurableUnitService configurableUnitService) {
        this.configurableUnitService = configurableUnitService;
    }

    @Transactional
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

    // ConfiguredUnitOption has no quantity field -> treat each option as quantity = 1
    private BigDecimal calculateConfiguredUnitPrice(ConfiguredUnit unit){
        BigDecimal basePrice = unit.getUnit().getPrice();

        BigDecimal optionsPrice = unit.getOptions().stream()
                .filter(Objects::nonNull)
                .map(cuo -> cuo.getOption() != null && cuo.getOption().getPrice() != null
                        ? cuo.getOption().getPrice()
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(optionsPrice);
    }

    public BigDecimal calculateConfigurationUnitPrice(ConfigurationUnit configurationUnit) {
        BigDecimal basePrice = configurationUnit.getConfigurableUnit().getUnit().getPrice();
        BigDecimal optionsPrice = configurationUnit.getOptions().stream()
                .map((ConfigurationUnitOption cuo) ->
                        cuo.getOption().getPrice().multiply(BigDecimal.valueOf(Math.max(1, cuo.getQuantity())))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return basePrice.add(optionsPrice);
    }

    public BigDecimal calculateConfigurationTotalPrice(Configuration configuration){
        return configuration.getIncludedUnits().stream()
                .map(cu -> calculateConfigurationUnitPrice(cu)
                        .multiply(new BigDecimal(cu.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Изчислява реални цени за всички ConfigurableUnits (включва template опции).
     * Използва се за визуализация на Configuration в UI (например edit-configuration форми).
     *
     * @return Map с ID на ConfigurableUnit като ключ и изчислената цена като стойност
     */
    public Map<UUID, BigDecimal> calculateConfigurationConfigurableUnitsPrices() {
        List<ConfigurableUnit> allUnits = configurableUnitService.getAllUnits();
        return allUnits.stream()
                .collect(Collectors.toMap(
                        ConfigurableUnit::getId,
                        this::calculateConfigurableUnitPrice
                ));
    }


}
