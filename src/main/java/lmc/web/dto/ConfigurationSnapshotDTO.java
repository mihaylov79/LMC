package lmc.web.dto;

import lmc.configuration.model.MachineLine;
import lmc.configuration.model.MachineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot на конфигурация към момента на създаване на оферта.
 * Запазва всички данни за да не се губи информация при промени в оригиналната конфигурация.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationSnapshotDTO {

    private String code;
    private String imageUrl;
    private MachineLine line;
    private MachineType type;
    private String description;
    private String model;
    private BigDecimal totalPrice;
    private LocalDate priceUpdateDate;

    @Builder.Default
    private List<ConfigurationUnitSnapshotDTO> includedUnits = new ArrayList<>();

    /**
     * Snapshot на конфигурационна единица (ConfigurationUnit)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigurationUnitSnapshotDTO {
        private String configurableUnitCode;
        private String unitName;
        private String unitDescription;
        private BigDecimal unitPrice;
        private int quantity;

        @Builder.Default
        private List<OptionSnapshotDTO> options = new ArrayList<>();
    }

    /**
     * Snapshot на опция (ConfigurationUnitOption)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionSnapshotDTO {
        private String optionCode;
        private String optionName;
        private String optionDescription;
        private BigDecimal optionPrice;
        private int quantity;
    }
}
