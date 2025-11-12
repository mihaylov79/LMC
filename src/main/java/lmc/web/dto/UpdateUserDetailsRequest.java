package lmc.web.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDetailsRequest {

    @Length(min = 3, max = 15 , message = "Името трябва да съдържа само букви и да бъде между 3 и 15 символа")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$", message = "Името трябва да съдържа само букви (кирилица и латиница)")
    private String firstName;

    @Length(min = 3, max = 15 , message = "Името трябва да съдържа само букви и да бъде между 3 и 15 символа")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$", message = "Името трябва да съдържа само букви (кирилица и латиница)")
    private String lastName;
}
