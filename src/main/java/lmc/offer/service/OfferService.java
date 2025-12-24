package lmc.offer.service;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lmc.company.model.Company;
import lmc.company.service.CompanyService;
import lmc.configuration.model.Configuration;
import lmc.configuration.service.ConfigurationService;
import lmc.currencyFixing.model.CurrencyFixing;
import lmc.currencyFixing.service.CurrencyConversionService;
import lmc.currencyFixing.service.CurrencyFixingService;
import lmc.offer.mapper.OfferMapper;
import lmc.offer.model.Offer;
import lmc.offer.model.OfferStatus;
import lmc.offer.repository.OfferRepository;
import lmc.unit.model.CurrencyType;
import lmc.user.model.User;
import lmc.web.dto.ConfigurationSnapshotDTO;
import lmc.web.dto.NewOfferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class OfferService {

    // Базова валута за всички цени в системата
    private static final CurrencyType DEFAULT_CURRENCY = CurrencyType.EUR;

    private final OfferRepository offerRepository;
    private final OfferMapper offerMapper;
    private final CompanyService companyService;
    private final ConfigurationService configurationService;
    private final CurrencyFixingService currencyFixingService;
    private final CurrencyConversionService currencyConversionService;
    private final SpringTemplateEngine templateEngine;

    public OfferService(OfferRepository offerRepository, OfferMapper offerMapper,
                        CompanyService companyService, ConfigurationService configurationService,
                        CurrencyFixingService currencyFixingService,
                        CurrencyConversionService currencyConversionService, SpringTemplateEngine templateEngine) {
        this.offerRepository = offerRepository;
        this.offerMapper = offerMapper;
        this.companyService = companyService;
        this.configurationService = configurationService;
        this.currencyFixingService = currencyFixingService;
        this.currencyConversionService = currencyConversionService;
        this.templateEngine = templateEngine;
    }


    public List<Offer> getAllOffersWithoutDeleted() {
        return offerRepository.findByDeletedFalse();
    }


    public List<Offer> getAllOffersIncludingDeleted() {
        return offerRepository.findAllByOrderByDeletedAscCompanyCompanyNameAsc();
    }

    public Offer getOfferById(UUID offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Оферта с идентификация: %s не съществува!".formatted(offerId)));
    }

    public long countOffersByYear() {
        int year = LocalDate.now().getYear();
        Long count = offerRepository.countByCreated_Year(year);
        return count == null ? 0L : count;
    }

    public boolean existsByOfferNumber(String offerNumber) {
        return offerRepository.existsByOfferNumber(offerNumber);
    }

    public String generateOfferNumber() {
        int year = LocalDate.now().getYear();
        long seq = countOffersByYear() + 1;
        String candidate = year + "-" + String.format("%05d", seq);
        while (existsByOfferNumber(candidate)) {
            seq++;
            candidate = year + "-" + String.format("%05d", seq);
        }
        return candidate;
    }

    @Transactional
    public Offer createNewOffer(NewOfferRequest request, User currentUser) {

        // Зареждаме ентитетите от UUID-тата в request
        Company company = companyService.getCompanyById(request.getCompanyId());
        Configuration configuration = configurationService.findConfigurationById(request.getConfigurationId());

        BigDecimal configurationPrice = configuration.getTotalPrice();
        if (configurationPrice == null) configurationPrice = BigDecimal.ZERO;

        // Snapshot на конфигурацията към момента на създаване
        String configurationSnapshot = offerMapper.createConfigurationSnapshot(configuration);

        Offer offer = Offer.builder()
                .offerNumber(generateOfferNumber())
                .company(company)
                .configuration(configuration)
                .configurationPrice(configurationPrice)
                .configurationSnapshot(configurationSnapshot)
                .installationFee(nvl(request.getInstallationFee()))
                .deliveryFee(nvl(request.getDeliveryFee()))
                .installationMaterials(nvl(request.getInstallationMaterials()))
                .currency(request.getCurrency())
                .discount(nvl(request.getDiscount()))
                .finalPrice(calculateFinalPrice(configurationPrice, request).setScale(2, RoundingMode.HALF_UP))
                .created(LocalDate.now())
                .expires(LocalDate.now().plusMonths(1))
                .createdBy(currentUser)
                .status(OfferStatus.PENDING)
                .deleted(false)
                .build();

        // Retry-on-conflict за уникален offer_number
        int attempts = 0;
        while (true) {
            try {
                return offerRepository.save(offer);
            } catch (DataIntegrityViolationException ex) {
                if (++attempts > 3) throw ex;
                offer = offer.toBuilder()
                        .offerNumber(generateOfferNumber())
                        .build();
            }
        }
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private BigDecimal calculateFinalPrice(BigDecimal configurationPrice, NewOfferRequest request) {
        BigDecimal discountPercent = nvl(request.getDiscount());
        BigDecimal discountRate = discountPercent.movePointLeft(2); // безопасно вместо divide(100)
        BigDecimal discounted = configurationPrice.subtract(configurationPrice.multiply(discountRate));
        return discounted
                .add(nvl(request.getDeliveryFee()))
                .add(nvl(request.getInstallationFee()))
                .add(nvl(request.getInstallationMaterials()));
    }

    @Transactional(readOnly = true)
    public Offer getOfferWithConfiguration(UUID offerId) {
        return offerRepository.findByIdWithConfiguration(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Оферта с идентификация: %s не съществува!".formatted(offerId)));
    }

    /**
     * Анулира оферта (променя статус на CANCELED).
     * Офертата остава видима в списъка, но е маркирана като анулирана.
     *
     * @param offerId ID на офертата
     * @param reason причина за анулиране (опционално)
     * @return анулираната оферта
     */
    @Transactional
    public Offer cancelOffer(UUID offerId, String reason) {
        Offer offer = getOfferById(offerId);
        if (offer.getStatus() == OfferStatus.ACCEPTED) {
            throw new IllegalStateException("Не можете да анулирате приета оферта!");
        }
        Offer cancelled = offer.toBuilder()
                .status(OfferStatus.CANCELED)
                .build();
        return offerRepository.save(cancelled);
    }

    /**
     * Изтрива оферта (soft delete - маркира като deleted=true).
     * Офертата се крие от списъка, но остава в базата данни.
     *
     * @param offerId ID на офертата
     * @return изтритата оферта
     */
    @Transactional
    public Offer deleteOffer(UUID offerId) {
        Offer offer = getOfferById(offerId);
        Offer deleted = offer.toBuilder()
                .deleted(true)
                .build();
        return offerRepository.save(deleted);
    }

    /**
     * Извлича snapshot данните на конфигурацията от JSON.
     * Използва OfferMapper за десериализация.
     *
     * @param offer офертата
     * @return snapshot на конфигурацията или null ако няма snapshot
     */
    public ConfigurationSnapshotDTO getConfigurationSnapshot(Offer offer) {
        return offerMapper.parseConfigurationSnapshot(offer.getConfigurationSnapshot());
    }

    /**
     * Извлича snapshot на конфигурацията и конвертира цените в displayCurrency на офертата.
     * Делегира към CurrencyConversionService.
     *
     * @param offer офертата
     * @return snapshot с конвертирани цени според валутата на офертата
     */
    public ConfigurationSnapshotDTO getConfigurationSnapshotInDisplayCurrency(Offer offer) {
        return currencyConversionService.getOfferSnapshotInDisplayCurrency(offer);
    }

    /**
     * Връща всички активни оферти със съответните snapshots на конфигурациите.
     * Цените в snapshot-ите са конвертирани според валутата на всяка оферта.
     * Използва се за визуализация в home страницата.
     *
     * @return Map с UUID на офертата като ключ и ConfigurationSnapshotDTO като стойност
     */
    public Map<UUID, ConfigurationSnapshotDTO> getAllOffersWithSnapshots() {
        List<Offer> offers = getAllOffersWithoutDeleted();
        return offers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Offer::getId,
                        this::getConfigurationSnapshotInDisplayCurrency
                ));
    }

    /**
     * Изчислява крайна цена за визуализация, като използва конфигурационния snapshot (ако е наличен)
     * за базова цена, прилага отстъпка и такси и конвертира според exchangeRate на офертата.
     *
     * @param offer офертата
     * @return цена за показване в display валутата
     */
    public BigDecimal getOfferDisplayFinalPriceUsingSnapshot(Offer offer) {
        if (offer == null) return BigDecimal.ZERO;

        // Вземаме snapshot вече конвертиран в display валута (ако валутата е EUR или rate==null, ще върне оригиналния EUR snapshot)
        ConfigurationSnapshotDTO snapshot = currencyConversionService.getOfferSnapshotInDisplayCurrency(offer);

        BigDecimal baseDisplayPrice = BigDecimal.ZERO;
        if (snapshot != null && snapshot.getTotalPrice() != null) {
            baseDisplayPrice = snapshot.getTotalPrice(); // вече в display валута
        } else if (offer.getConfigurationPrice() != null) {
            // Ако няма snapshot, конвертираме конфигурационната цена в display валута
            baseDisplayPrice = currencyConversionService.convertWithExchangeRate(offer.getConfigurationPrice(), offer.getExchangeRate());
        }

        // Конвертираме таксите в display валута
        BigDecimal delivery = currencyConversionService.convertWithExchangeRate(nvl(offer.getDeliveryFee()), offer.getExchangeRate());
        BigDecimal installation = currencyConversionService.convertWithExchangeRate(nvl(offer.getInstallationFee()), offer.getExchangeRate());
        BigDecimal materials = currencyConversionService.convertWithExchangeRate(nvl(offer.getInstallationMaterials()), offer.getExchangeRate());

        // Прилагаме отстъпката върху базовата display цена
        BigDecimal discountPercent = nvl(offer.getDiscount());
        BigDecimal discountRate = discountPercent.movePointLeft(2);
        BigDecimal discounted = baseDisplayPrice.subtract(baseDisplayPrice.multiply(discountRate));

        BigDecimal total = discounted.add(delivery).add(installation).add(materials);

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // ===== Валутна конвертация =====

    /**
     * Задава валута за визуализация на оферта.
     * Записва exchangeRate към момента на смяна на валутата.
     * Цените в базата остават в EUR.
     *
     * @param offerId ID на офертата
     * @param targetCurrency целева валута за визуализация
     * @return актуализираната оферта
     */
    @Transactional
    public Offer setOfferCurrency(UUID offerId, CurrencyType targetCurrency) {
        Offer offer = getOfferById(offerId);

        // Ако искаме да върнем към EUR, махаме exchangeRate
        if (targetCurrency == null || targetCurrency == DEFAULT_CURRENCY) {
            return offerRepository.save(offer.toBuilder()
                    .currency(DEFAULT_CURRENCY)
                    .exchangeRate(null)
                    .exchangeRateDate(null)
                    .build());
        }

        // Зареждаме текущ фиксинг за целевата валута
        CurrencyFixing fixing = currencyFixingService.getLatestFixing(targetCurrency);

        return offerRepository.save(offer.toBuilder()
                .currency(targetCurrency)
                .exchangeRate(fixing.getRate())
                .exchangeRateDate(LocalDate.now())
                .build());
    }

    public byte[] generateOfferPdf(UUID offerId){

        try {
            Offer offer = getOfferWithConfiguration(offerId);
            ConfigurationSnapshotDTO snapshot = getConfigurationSnapshot(offer);
            BigDecimal finalPrice = getOfferDisplayFinalPriceUsingSnapshot(offer);

            String html = renderOfferHtmlForPdf(offer, snapshot, finalPrice);
            return convertHtmlToPdf(html);
        } catch (Exception e) {
            log.error("Грешка при генериране на PDF за оферта {}: {}", offerId, e.getMessage(),e);
            throw new RuntimeException("Не може да се генерира PDF за офертата", e);
        }
    }

    public String renderOfferHtmlForPdf(Offer offer, ConfigurationSnapshotDTO snapshot,
                                        BigDecimal finalPrice){

        Context context = new Context();
        context.setVariable("offer", offer);
        context.setVariable("snapshot", snapshot);
        context.setVariable("finalPrice", finalPrice);
        context.setLocale(Locale.forLanguageTag("bg-BG"));

        return templateEngine.process("offer-pdf", context);
    }

    private byte[] convertHtmlToPdf(String html) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();
        // Explicitly register a Cyrillic-capable font from classpath
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"),
                "DejaVu Sans", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans-Bold.ttf"),
                "DejaVu Sans", 700, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.withHtmlContent(html, "");
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }
}
