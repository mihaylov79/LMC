package lmc.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ConfigurationIncludedUnitsDTO {

    private UUID configurableUnitId;
    private String configurableUnitCode;
    private String baseUnitCode;
    private String baseUnitName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;
}
