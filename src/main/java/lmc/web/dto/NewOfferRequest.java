package lmc.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lmc.company.model.Company;
import lmc.configuration.model.Configuration;
import lmc.unit.model.CurrencyType;

import java.math.BigDecimal;

public class NewOfferRequest {

    @NotBlank(message = "Това поле не може да бъде празно!")
    private Company company;

    private Configuration configuration;

    @Digits(integer = 7, fraction = 2, message = "Невалиден формат за цена!")
    private BigDecimal installationFee;

    @Digits(integer = 7, fraction = 2, message = "Невалиден формат за цена!")
    private BigDecimal deliveryFee;

    @Digits(integer = 7, fraction = 2,message = "Невалиден формат за цена!")
    private BigDecimal transportCosts;

    @NotBlank(message = "Това поле не може да бъде празно!")
    private CurrencyType currency;
    @Digits(integer = 2, fraction = 2, message = "Невалиден формат за отстъпка!")
    private BigDecimal discount;

    //TODO finish the DTO and Validation - find Do U need second price Field in the Offer Class?
}
