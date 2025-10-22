package lmc.web.dto;

import lmc.configuration.model.MachineLine;
import lmc.configuration.model.MachineType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ConfigurationDetailsDTO {

    private UUID id;
    private String imageUrl;
    private String code;
    private String description;
    private String model;
    private MachineLine line;
    private MachineType type;
    private boolean active;
    private BigDecimal totalPrice;
    private LocalDate priceUpdateDate;
    private List<ConfigurationIncludedUnitsDTO>includedUnits;
}
