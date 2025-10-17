package lmc.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateNewConfiguredUnitRequest {

//    private UUID unitId;
    private String code;

//    private List<UUID> optionIds;

    private List<String> optionCodes;

    private int quantity;
}
