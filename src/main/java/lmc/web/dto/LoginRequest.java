package lmc.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginRequest {

    @NotBlank(message = "Tова поле не може да бъде празно")
    @Email(message = "Моля въведете валиден мейл!")
    private String email;

    @NotBlank(message = "Tова поле не може да бъде празно")
    @Length(min = 6, max = 20, message = "Паролата трябва да бъде минимум 6 и максимум 20 символа")
    private String password;
}
