package lmc.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ConfigurationUnitRequest {

    private UUID configurableUnitId;

    private int quantity;
}
