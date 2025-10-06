package lmc.web.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
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


    @Length(max = 15, message = "Размерът не може да надвишава 15 символа")
    @Pattern(regexp = "^[0-9x×X\\s.-]+$", message = "Невалиден формат на размер")
    private String size;

    @Positive
    private BigDecimal price;




}
