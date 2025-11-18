package lmc.offer.service;

import lmc.company.model.Company;
import lmc.company.service.CompanyService;
import lmc.configuration.model.Configuration;
import lmc.configuration.service.ConfigurationService;
import lmc.offer.mapper.OfferMapper;
import lmc.offer.model.Offer;
import lmc.offer.model.OfferStatus;
import lmc.offer.repository.OfferRepository;
import lmc.user.model.User;
import lmc.web.dto.ConfigurationSnapshotDTO;
import lmc.web.dto.NewOfferRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferMapper offerMapper;
    private final CompanyService companyService;
    private final ConfigurationService configurationService;

    public OfferService(OfferRepository offerRepository, OfferMapper offerMapper,
                        CompanyService companyService, ConfigurationService configurationService) {
        this.offerRepository = offerRepository;
        this.offerMapper = offerMapper;
        this.companyService = companyService;
        this.configurationService = configurationService;
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
                .transportCosts(nvl(request.getTransportCosts()))
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
                .add(nvl(request.getTransportCosts()));
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
}
