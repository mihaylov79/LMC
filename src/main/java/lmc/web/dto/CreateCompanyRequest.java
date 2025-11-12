package lmc.web.dto;


import jakarta.validation.constraints.*;
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
    @Size(min = 9, max = 13, message = "Това поле трявбва да съдържа между 9 и 13 символа")
    @Pattern(regexp = "\\d+", message = "ЕИК трябва да съдържа само цифри")
    private String companyEIK;


    @Size(min= 11, max = 15, message = "Това поле трявбва да съдържа между 11 и 15 символа")
    private String VAT;

    @NotBlank(message = "Това поле не може да бъде празно")
    @Length(max = 20 , message = "Това поле  не може да надвишава 20 символа")
    private String country;

    @NotBlank(message = "Това поле не може да бъде празни")
    @Length(max = 20 , message = "Това поле  не може да надвишава 20 символа")
    private String town;

    @Length(max = 60 , message = "Това поле  не може да надвишава 60 символа")
    private String address;

    @Length(max = 50, message = "Това поле не може да надвишава 50 символа")
    @Pattern(regexp = "^[А-Яа-яA-Za-z\\s'-]+$", message = "Името може да съдържа само букви, интервали, тире и апостроф")
    private String manager;

    @Length(max = 30, message = "Това поле не може да надвишава 30 символа")
    @Pattern(regexp = "^(?:$|[А-Яа-яA-Za-z\\s'-]+)$", message = "Името може да съдържа само букви, интервали, тире и апостроф")
    private String contactPerson;

    @Pattern(regexp = "^(?:\\+359|0)?[0-9]{8,9}$", message = "Невалиден телефонен номер")
    private String phone;

    @Email(message = "Въведете валиден имейл адрес")
    private String email;
}
