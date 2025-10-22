package lmc.web.dto.mapper;

import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.service.PriceCalculationService;
import lmc.configuration.model.Configuration;
import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.web.dto.ConfigurationDetailsDTO;
import lmc.web.dto.ConfigurationIncludedUnitsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class ConfigurationMapper {

    private final PriceCalculationService calculationService;

    @Autowired
    public ConfigurationMapper(PriceCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    public ConfigurationDetailsDTO toDto(Configuration configuration){
        List<ConfigurationIncludedUnitsDTO> includedUnits = configuration.getIncludedUnits()
                .stream().map(this::mapIncludedUnit).toList();

        return ConfigurationDetailsDTO.builder()
                .id(configuration.getId())
                .imageUrl(configuration.getImageUrl())
                .code(configuration.getCode())
                .description(configuration.getDescription())
                .model(configuration.getModel())
                .line(configuration.getLine())
                .type(configuration.getType())
                .active(configuration.isActive())
                .totalPrice(calculationService.calculateConfigurationTotalPrice(configuration))
                .priceUpdateDate(LocalDate.now())
                .includedUnits(includedUnits)
                .build();

    }

    private ConfigurationIncludedUnitsDTO mapIncludedUnit(ConfigurationUnit unit) {
        ConfigurableUnit confUnit = unit.getConfigurableUnit();
        BigDecimal unitPrice = calculationService.calculateConfigurableUnitPrice(confUnit);
        BigDecimal totalUnitPrice = unitPrice.multiply(BigDecimal.valueOf(unit.getQuantity()));

        return ConfigurationIncludedUnitsDTO.builder()
                .configurableUnitId(confUnit.getId())
                .configurableUnitCode(confUnit.getCode())
                .baseUnitCode(confUnit.getUnit() != null ? confUnit.getUnit().getCode() : null)
                .baseUnitName(confUnit.getUnit() != null ? confUnit.getUnit().getName() : null)
                .unitPrice(unitPrice)
                .quantity(unit.getQuantity())
                .totalPrice(totalUnitPrice)
                .build();
    }



}
