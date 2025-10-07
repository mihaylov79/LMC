package lmc.web.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public class CreateCompanyRequest {

    @NotBlank(message = "Това поле не може да бъде празно")
    private String companyName;

    @NotBlank(message = "Това поле не може да бъде празно")
    private String companyEIK;


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
}
