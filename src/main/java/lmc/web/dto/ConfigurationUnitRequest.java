package lmc.web.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
public class ConfigurationUnitRequest {

    private UUID configurableUnitId;

    private int quantity;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String displayLabel;
}
