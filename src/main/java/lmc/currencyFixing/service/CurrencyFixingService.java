package lmc.currencyFixing.service;

import jakarta.annotation.PostConstruct;
import lmc.currencyFixing.model.CurrencyFixing;
import lmc.currencyFixing.repository.CurrencyFixingRepository;
import lmc.exceptions.CurrencyFixingCanNotBeCreated;
import lmc.unit.model.CurrencyType;
import lmc.web.dto.CurrencyFixingRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrencyFixingService {

    private final CurrencyFixingRepository currencyFixingRepository;

    public CurrencyFixingService(CurrencyFixingRepository currencyFixingRepository) {
        this.currencyFixingRepository = currencyFixingRepository;
    }

    /**
     * Зарежда фиксинг за дадена валута.
     *
     * @param currency целева валута
     * @return фиксинг за валутата
     * @throws IllegalArgumentException ако няма фиксинг за тази валута
     */
    public CurrencyFixing getLatestFixing(CurrencyType currency) {
        if (currency == CurrencyType.EUR) {
            throw new IllegalArgumentException("EUR е базова валута и не изисква фиксинг!");
        }

        return currencyFixingRepository.findByCurrency(currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Няма наличен фиксинг за валута: " + currency));
    }

    /**
     * Зарежда всички налични валутни фиксинги.
     * Полезно за админ интерфейс за управление на курсове.
     *
     * @return списък с всички фиксинги
     */
    public List<CurrencyFixing> getAllFixings() {
        return currencyFixingRepository.findAll();
    }

    /**
     * Създава нов или актуализира съществуващ фиксинг за дадена валута.
     * При update се променя само rate и lastUpdated, validFrom остава оригинален.
     *
     * @param request заявка с валута и обменен курс спрямо EUR
     * @return запазения/актуализирания фиксинг
     * @throws IllegalArgumentException ако се опита да създаде фиксинг за EUR
     */
    @Transactional
    public CurrencyFixing saveOrUpdateFixing(CurrencyFixingRequest request) {
        // Проверка за базова валута
        if (request.getCurrency() == CurrencyType.EUR) {
            throw new CurrencyFixingCanNotBeCreated("Не може да се създава фиксинг за базовата валута EUR!");
        }

        // Търсим съществуващ фиксинг за тази валута
        Optional<CurrencyFixing> existing = currencyFixingRepository.findByCurrency(request.getCurrency());

        CurrencyFixing fixing;

        if (existing.isPresent()) {
            // UPDATE: Обновяваме съществуващия
            fixing = existing.get().toBuilder()
                    .rate(request.getRate().setScale(6, java.math.RoundingMode.HALF_UP))
                    .lastUpdated(LocalDate.now())
                    // validFrom остава оригиналната дата (не променяме)
                    .build();
        } else {
            // CREATE: Създаваме нов
            fixing = CurrencyFixing.builder()
                    .currency(request.getCurrency())
                    .rate(request.getRate().setScale(6, java.math.RoundingMode.HALF_UP))
                    .validFrom(LocalDate.now())
                    .lastUpdated(LocalDate.now())
                    .build();
        }

        return currencyFixingRepository.save(fixing);
    }

    /**
     * Инициализира начални фиксинги при стартиране на приложението.
     * Създава фиксинги само ако не съществуват за съответната валута.
     */
    @PostConstruct
    public void initializeDefaultFixings() {
        // Всяка валута се проверява и създава независимо
        createInitialFixing(CurrencyType.USD, new BigDecimal("1.10"));   // 1 EUR = 1.10 USD
        createInitialFixing(CurrencyType.GBP, new BigDecimal("0.86"));   // 1 EUR = 0.86 GBP

        System.out.println("✅ Проверени валутни фиксинги (USD, GBP)");
    }

    /**
     * Създава начален фиксинг за валута ако не съществува.
     * Идемпотентен метод - безопасен за многократно извикване.
     */
    private void createInitialFixing(CurrencyType currency, BigDecimal rate) {
        Optional<CurrencyFixing> existing = currencyFixingRepository.findByCurrency(currency);

        if (existing.isEmpty()) {
            CurrencyFixing fixing = CurrencyFixing.builder()
                    .currency(currency)
                    .rate(rate)
                    .validFrom(LocalDate.now())
                    .lastUpdated(LocalDate.now())
                    .build();
            currencyFixingRepository.save(fixing);
            System.out.println("✅ Създаден начален фиксинг за " + currency + ": " + rate);
        } else {
            System.out.println("ℹ️ Фиксинг за " + currency + " вече съществува, пропускам.");
        }
    }

    public CurrencyFixing getFixingById(UUID id) {

        return currencyFixingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Фиксинг с идентификация: %s не е намерен!".formatted(id)));
    }
}

