package lmc.web.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateCompanyRequest {

    @NotBlank(message = "Това поле не може да бъде празно")
    private String companyName;

    @NotBlank(message = "Това поле не може да бъде празно")
    private String companyEIK;


    @Size(min=12, max = 12, message = "Това поле не може да бъде по-дълго от 12 символа")
    private String VAT;


    private String country;

    @Length(max = 15 , message = "Това поле  не може да надвишава 15 символа")
    private String town;

    @Length(max = 20 , message = "Това поле  не може да надвишава 20 символа")
    private String address;

    @Length(max = 30, message = "Това поле не може да надвишава 30 символа")
    @Pattern(regexp = "^[А-Яа-яA-Za-z\\s'-]+$", message = "Името може да съдържа само букви, интервали, тире и апостроф")
    private String manager;

    @Length(max = 30, message = "Това поле не може да надвишава 30 символа")
    @Pattern(regexp = "^[А-Яа-яA-Za-z\\s'-]+$", message = "Името може да съдържа само букви, интервали, тире и апостроф")
    private String contactPerson;

    @Pattern(regexp = "^(\\+359|0)?[0-9]{9}$", message = "Невалиден телефонен номер")
    private String phone;

    @Email
    private String email;
}
