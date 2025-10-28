package lmc.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OptionSelectionDTO {

    private UUID optionId;

    private int quantity = 1;
}
