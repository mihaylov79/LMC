package lmc.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lmc.company.model.Company;
import lmc.configuration.model.Configuration;
import lmc.unit.model.CurrencyType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewOfferRequest {

    @NotNull(message = "Клиентът е задължителен!")
    private Company company;

    @NotNull(message = "Конфигурацията е задължителна!")
    private Configuration configuration;

    @Digits(integer = 7, fraction = 2, message = "Невалиден формат за цена!")
    private BigDecimal installationFee;

    @Digits(integer = 7, fraction = 2, message = "Невалиден формат за цена!")
    private BigDecimal deliveryFee;

    @Digits(integer = 7, fraction = 2, message = "Невалиден формат за цена!")
    private BigDecimal transportCosts;

    @NotNull(message = "Валутата е задължителна!")
    private CurrencyType currency;

    @Digits(integer = 2, fraction = 2, message = "Невалиден формат за отстъпка!")
    private BigDecimal discount;
}
