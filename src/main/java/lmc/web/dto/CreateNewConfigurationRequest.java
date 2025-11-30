package lmc.web.dto;

import lmc.configuration.model.MachineLine;
import lmc.configuration.model.MachineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewConfigurationRequest {

    @URL(message = "Моля въведете валиден URL")
    private String imgUrl;

    private String code;

    private MachineLine line;

    private MachineType type;

    private String description;

    private String model;

    // version for optimistic locking binding
    private Long version;

    private List<ConfigurationUnitRequest> units = new ArrayList<>();
}
