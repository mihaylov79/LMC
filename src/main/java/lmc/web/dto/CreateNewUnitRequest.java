package lmc.web.dto;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewUnitRequest {

    @URL
    private String imageUrl;

    @NotBlank(message = "Това поле не може да бъде празно")
    private String code;

    @NotBlank(message = "Това поле не може да бъде празно")
    private String name;

    @Length(max = 256, message = "Описанието не може да съдържа повече от 256 символа")
    private String description;


    @Length(max = 15, message = "Размерът не може да надвишава 15 символа")
    @Pattern(regexp = "^[0-9x×X\\s.\\-]*$", message = "Невалиден формат на размер")
    private String size;

    @Positive(message = "Въведената цена трябва да бъде положително число")
    @Digits(integer = 6, fraction = 2, message = "Цената не може да надвишава 6 цифрена стойност")
    private BigDecimal price;





}
