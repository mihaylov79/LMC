package lmc.web.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateNewConfiguredUnitRequest {

    private UUID unitId;

    private List<OptionSelectionDTO> optionSelections;

//    private int quantity;
}
