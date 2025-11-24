package lmc.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lmc.unit.model.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyFixingRequest {

    @NotNull(message = "Това поле не може да бъде празно")
    private CurrencyType currency;

    @NotNull(message = "Това поле не може да бъде празно")
    @Positive(message = "Въведеният фиксинг трябва да бъде положително число")
    @Digits(integer = 4, fraction = 6, message = "Фиксингът трябва да има до 4 цифри преди запетаята и до 6 след нея")
    private BigDecimal rate;
}
