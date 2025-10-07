package lmc.web.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterRequest {

    @Length(min = 3, max = 15 , message = "Името трябва да съдържа само букви и да бъде между 3 и 15 символа")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$", message = "Името трябва да съдържа само букви (кирилица и латиница)")
    private String firstName;

    @Length(min = 3, max = 15 , message = "Името трябва да съдържа само букви и да бъде между 3 и 15 символа")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$", message = "Името трябва да съдържа само букви (кирилица и латиница)")
    private String lastName;

    @NotBlank(message = "Това поле не може да бъде празно!")
    @Email(message = "Моля въведете валидем мейл!")
    private String email;

    @NotBlank
    @Length(min = 6, max = 20, message = "Паролата трябва да бъде минимум 6 и максимум 20 символа")
    private String password;

}
