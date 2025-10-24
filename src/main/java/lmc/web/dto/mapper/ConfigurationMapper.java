package lmc.web.dto.mapper;

import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.service.PriceCalculationService;
import lmc.configuration.model.Configuration;
import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.unit.model.CurrencyType;
import lmc.web.dto.ConfigurationDetailsDTO;
import lmc.web.dto.ConfigurationIncludedUnitsDTO;
import lmc.web.dto.ConfigurationUnitRequest;
import lmc.web.dto.CreateNewConfigurationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
                .totalPrice(configuration.getTotalPrice())
                .priceUpdateDate(configuration.getPriceUpdateDate())
                .currency(CurrencyType.EUR)
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

    public CreateNewConfigurationRequest toEditRequest(Configuration configuration){

        List<ConfigurationUnitRequest> units = configuration.getIncludedUnits().stream().map(u -> {
            ConfigurationUnitRequest cu = new ConfigurationUnitRequest();
            cu.setConfigurableUnitId(u.getConfigurableUnit().getId());
            cu.setQuantity(u.getQuantity());

            ConfigurableUnit confUnit = u.getConfigurableUnit();
            String label = confUnit.getCode() + (confUnit.getUnit() != null ? " - " + confUnit.getUnit().getName() : "");
            cu.setDisplayLabel(label);
            return cu;
        }).toList();


        return CreateNewConfigurationRequest.builder()
                .imgUrl(configuration.getImageUrl())
                .code(configuration.getCode())
                .line(configuration.getLine())
                .type(configuration.getType())
                .model(configuration.getModel())
                .description(configuration.getDescription())
                .units(units)
                .build();


    }



}
