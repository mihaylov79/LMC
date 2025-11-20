package lmc.currencyFixing.service;

import jakarta.annotation.PostConstruct;
import lmc.currencyFixing.model.CurrencyFixing;
import lmc.currencyFixing.repository.CurrencyFixingRepository;
import lmc.unit.model.CurrencyType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class CurrencyFixingService {

    private final CurrencyFixingRepository currencyFixingRepository;

    public CurrencyFixingService(CurrencyFixingRepository currencyFixingRepository) {
        this.currencyFixingRepository = currencyFixingRepository;
    }

    /**
     * Зарежда последния (най-актуален) фиксинг за дадена валута.
     *
     * @param currency целева валута
     * @return последен фиксинг
     * @throws IllegalArgumentException ако няма фиксинг за тази валута
     */
    public CurrencyFixing getLatestFixing(CurrencyType currency) {
        if (currency == CurrencyType.EUR) {
            throw new IllegalArgumentException("EUR е базова валута и не изисква фиксинг!");
        }

        return currencyFixingRepository.findFirstByCurrencyOrderByValidFromDescLastUpdatedDesc(currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Няма наличен фиксинг за валута: " + currency));
    }

    /**
     * Зарежда фиксинг валиден на конкретна дата.
     *
     * @param currency целева валута
     * @param date дата за която търсим фиксинг
     * @return фиксинг валиден на тази дата
     * @throws IllegalArgumentException ако няма фиксинг за тази валута и дата
     */
    public CurrencyFixing getFixingForDate(CurrencyType currency, LocalDate date) {
        if (currency == CurrencyType.EUR) {
            throw new IllegalArgumentException("EUR е базова валута и не изисква фиксинг!");
        }

        return currencyFixingRepository.findFirstByCurrencyAndValidFromLessThanEqualOrderByValidFromDesc(
                        currency, date)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Няма наличен фиксинг за валута: " + currency + " на дата: " + date));
    }

    /**
     * Създава или обновява фиксинг за дадена валута.
     *
     * @param currency валута
     * @param rate обменен курс спрямо EUR
     * @return запазения фиксинг
     */
    @Transactional
    public CurrencyFixing saveFixing(CurrencyType currency, BigDecimal rate) {
        if (currency == CurrencyType.EUR) {
            throw new IllegalArgumentException("Не може да се създава фиксинг за базовата валута EUR!");
        }

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Обменният курс трябва да е положително число!");
        }

        CurrencyFixing fixing = CurrencyFixing.builder()
                .currency(currency)
                .rate(rate)
                .validFrom(LocalDate.now())
                .lastUpdated(LocalDate.now())
                .build();

        return currencyFixingRepository.save(fixing);
    }

    /**
     * Инициализира начални фиксинги при стартиране на приложението.
     * Създава фиксинги само ако не съществуват.
     */
    @PostConstruct
    public void initializeDefaultFixings() {
        // Проверяваме дали вече има фиксинги
        if (currencyFixingRepository.count() > 0) {
            return; // Вече има данни, не създаваме нови
        }

        // Създаваме начални фиксинги (примерни курсове)
        createInitialFixing(CurrencyType.USD, new BigDecimal("1.10"));   // 1 EUR = 1.10 USD
        createInitialFixing(CurrencyType.GBP, new BigDecimal("0.86"));   // 1 EUR = 0.86 GBP

        System.out.println("✅ Инициализирани валутни фиксинги (EUR, USD, GBP)");
    }

    private void createInitialFixing(CurrencyType currency, BigDecimal rate) {
        Optional<CurrencyFixing> existing = currencyFixingRepository
                .findFirstByCurrencyOrderByValidFromDescLastUpdatedDesc(currency);

        if (existing.isEmpty()) {
            CurrencyFixing fixing = CurrencyFixing.builder()
                    .currency(currency)
                    .rate(rate)
                    .validFrom(LocalDate.now())
                    .lastUpdated(LocalDate.now())
                    .build();
            currencyFixingRepository.save(fixing);
        }
    }
}

