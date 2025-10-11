package lmc.web.dto;

import lmc.configuration.model.MachineLine;
import lmc.configuration.model.MachineType;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateNewConfigurationRequest {

    @URL(message = "Моля въведете валиден URL")
    private String imgUrl;

    private String code;

    private MachineLine line;

    private MachineType type;

    private String description;

    private String model;

    private List<CreateNewConfiguredUnitRequest> units = new ArrayList<>();
}
