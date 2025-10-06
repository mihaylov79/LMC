package lmc.web.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
public class CreateNewUnitRequest {

    @URL
    private String imageUrl;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @Length(max = 256)
    private String description;

    @Positive
    private BigDecimal price;




}
