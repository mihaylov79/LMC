package lmc.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lmc.option.model.SupportedBy;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
public class CreateNewOptionRequest {

    @NotBlank(message = "Това поле не може да бъде празно")
    private String code;

    private String name;

    @Length(max = 256, message = "Описанието не може да надвишава 256 символа")
    private String description;

    private SupportedBy compatibleWith;

    @Positive(message = "Въведената стойност трябва да бъде положително число")
    @Digits(integer = 6, fraction = 2, message = "Цената не може да надвишава 6 цифрена сума")
    private BigDecimal price;
}
