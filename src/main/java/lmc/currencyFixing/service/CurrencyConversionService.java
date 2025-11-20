package lmc.currencyFixing.service;

import lmc.currencyFixing.model.CurrencyFixing;
import lmc.offer.mapper.OfferMapper;
import lmc.offer.model.Offer;
import lmc.unit.model.CurrencyType;
import lmc.web.dto.ConfigurationSnapshotDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Сървис за валутна конвертация на цени и snapshots.
 * Отговаря за преобразуване на суми и обекти между различни валути.
 */
@Service
public class CurrencyConversionService {

    private static final CurrencyType BASE_CURRENCY = CurrencyType.EUR;
    private static final int PRICE_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final CurrencyFixingService currencyFixingService;
    private final OfferMapper offerMapper;

    public CurrencyConversionService(CurrencyFixingService currencyFixingService, OfferMapper offerMapper) {
        this.currencyFixingService = currencyFixingService;
        this.offerMapper = offerMapper;
    }

    /**
     * Конвертира сума от базовата валута (EUR) към целева валута.
     *
     * @param amountInEur сума в евро
     * @param targetCurrency целева валута
     * @return конвертирана сума
     */
    public BigDecimal convertFromBaseCurrency(BigDecimal amountInEur, CurrencyType targetCurrency) {
        if (amountInEur == null) {
            return BigDecimal.ZERO;
        }

        if (targetCurrency == null || targetCurrency == BASE_CURRENCY) {
            return amountInEur;
        }

        CurrencyFixing fixing = currencyFixingService.getLatestFixing(targetCurrency);
        return amountInEur.multiply(fixing.getRate())
                .setScale(PRICE_SCALE, ROUNDING_MODE);
    }

    /**
     * Конвертира сума с конкретен обменен курс (вече известен exchange rate).
     *
     * @param amount сума в базова валута
     * @param exchangeRate обменен курс
     * @return конвертирана сума
     */
    public BigDecimal convertWithExchangeRate(BigDecimal amount, BigDecimal exchangeRate) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        if (exchangeRate == null) {
            return amount;
        }

        return amount.multiply(exchangeRate)
                .setScale(PRICE_SCALE, ROUNDING_MODE);
    }

    /**
     * Извлича snapshot на конфигурацията от офертата и конвертира цените според display currency.
     * Ако офертата е в EUR или няма exchangeRate, връща snapshot директно.
     *
     * @param offer офертата
     * @return snapshot с конвертирани цени според валутата на офертата
     */
    public ConfigurationSnapshotDTO getOfferSnapshotInDisplayCurrency(Offer offer) {
        ConfigurationSnapshotDTO snapshot = offerMapper.parseConfigurationSnapshot(offer.getConfigurationSnapshot());

        if (snapshot == null) {
            return null;
        }

        // Ако е EUR или няма exchangeRate, връщаме директно
        if (offer.getCurrency() == null ||
                offer.getCurrency() == BASE_CURRENCY ||
                offer.getExchangeRate() == null) {
            return snapshot;
        }

        // Конвертираме всички цени в snapshot-а
        return convertSnapshot(snapshot, offer.getExchangeRate());
    }

    /**
     * Конвертира всички цени в snapshot-а с даден обменен курс.
     *
     * @param snapshot оригинален snapshot в EUR
     * @param exchangeRate обменен курс
     * @return snapshot с конвертирани цени
     */
    public ConfigurationSnapshotDTO convertSnapshot(ConfigurationSnapshotDTO snapshot, BigDecimal exchangeRate) {
        if (snapshot == null || exchangeRate == null) {
            return snapshot;
        }

        // Конвертираме totalPrice на конфигурацията
        BigDecimal convertedTotalPrice = convertWithExchangeRate(snapshot.getTotalPrice(), exchangeRate);

        // Конвертираме цените на всички units и options
        List<ConfigurationSnapshotDTO.ConfigurationUnitSnapshotDTO> convertedUnits =
                snapshot.getIncludedUnits().stream()
                        .map(unit -> convertUnitSnapshot(unit, exchangeRate))
                        .toList();

        return snapshot.toBuilder()
                .totalPrice(convertedTotalPrice)
                .includedUnits(convertedUnits)
                .build();
    }

    /**
     * Конвертира цените в unit snapshot.
     */
    private ConfigurationSnapshotDTO.ConfigurationUnitSnapshotDTO convertUnitSnapshot(
            ConfigurationSnapshotDTO.ConfigurationUnitSnapshotDTO unit, BigDecimal exchangeRate) {

        BigDecimal convertedUnitPrice = convertWithExchangeRate(unit.getUnitPrice(), exchangeRate);

        List<ConfigurationSnapshotDTO.OptionSnapshotDTO> convertedOptions =
                unit.getOptions().stream()
                        .map(opt -> convertOptionSnapshot(opt, exchangeRate))
                        .toList();

        return unit.toBuilder()
                .unitPrice(convertedUnitPrice)
                .options(convertedOptions)
                .build();
    }

    /**
     * Конвертира цената в option snapshot.
     */
    private ConfigurationSnapshotDTO.OptionSnapshotDTO convertOptionSnapshot(
            ConfigurationSnapshotDTO.OptionSnapshotDTO option, BigDecimal exchangeRate) {

        BigDecimal convertedOptionPrice = convertWithExchangeRate(option.getOptionPrice(), exchangeRate);

        return option.toBuilder()
                .optionPrice(convertedOptionPrice)
                .build();
    }

    /**
     * Връща крайната цена на офертата в display currency.
     *
     * @param offer офертата
     * @return крайна цена в display currency
     */
    public BigDecimal getOfferDisplayPrice(Offer offer) {
        if (offer == null || offer.getFinalPrice() == null) {
            return BigDecimal.ZERO;
        }

        return convertWithExchangeRate(offer.getFinalPrice(), offer.getExchangeRate());
    }

    /**
     * Връща базовата валута на системата.
     */
    public CurrencyType getBaseCurrency() {
        return BASE_CURRENCY;
    }
}

