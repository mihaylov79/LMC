package lmc.offer.service;

import lmc.offer.model.Offer;
import lmc.offer.model.OfferStatus;
import lmc.offer.repository.OfferRepository;
import lmc.user.model.User;
import lmc.web.dto.NewOfferRequest;
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

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public Offer getOfferById(UUID offerId){
         return offerRepository.findById(offerId)
                 .orElseThrow(() -> new IllegalArgumentException("Оферта с идентификация: %s не съществува!"
                         .formatted(offerId)));
    }

    public String generateOfferNumber(){

        int year = LocalDate.now().getYear();
        long size = countOffersByYear() + 1;

        String offerNumber = year + "-" + String.format("%05d", size);

        while (existsByOfferNumber(offerNumber)){
            size++;
            offerNumber = year + "-" + String.format("%05d", size);
        }

        return offerNumber;

    }

    public List<Offer> getAllOffers(){
        return offerRepository.findAll();
    }

    public long countOffersByYear(){
        int  year = LocalDate.now().getYear();
       return offerRepository.countByCreated_Year(year);
    }

    public boolean existsByOfferNumber(String offerNumber){
      return offerRepository.existsByOfferNumber(offerNumber);
    }

    @Transactional
    public Offer createNewOffer(NewOfferRequest request, User currentUser){


        Offer newOffer = Offer.builder()
                .offerNumber(generateOfferNumber())
                .configuration(request.getConfiguration())
                .company(request.getCompany())
                .installationFee(request.getInstallationFee())
                .deliveryFee(request.getDeliveryFee())
                .transportCosts(request.getTransportCosts())
                .currency(request.getCurrency())
                .discount(request.getDiscount())
                .created(LocalDate.now())
                .createdBy(currentUser)
                .status(OfferStatus.PENDING)
                .expires(LocalDate.now().plusMonths(1))
                .finalPrice(calculateOfferFinalPrice(request).setScale(2, RoundingMode.HALF_UP))
                .build();

        return offerRepository.save(newOffer);
    }

    private BigDecimal calculateOfferFinalPrice(NewOfferRequest request){

        BigDecimal configurationPrice = request.getConfiguration().getTotalPrice();

        BigDecimal discountPercent = request.getDiscount() == null ? BigDecimal.ZERO : request.getDiscount();
        BigDecimal discountRate = discountPercent.movePointLeft(2);
        BigDecimal discountedPrice = configurationPrice.subtract(configurationPrice.multiply(discountRate));
        BigDecimal deliveryFee = request.getDeliveryFee() == null ? BigDecimal.ZERO : request.getDeliveryFee();
        BigDecimal installationFee = request.getInstallationFee() == null ? BigDecimal.ZERO : request.getInstallationFee();
        BigDecimal transportCosts = request.getTransportCosts() == null ? BigDecimal.ZERO : request.getTransportCosts();

        return discountedPrice.add(deliveryFee).add(installationFee).add(transportCosts);
    }
}
