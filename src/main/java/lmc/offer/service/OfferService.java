package lmc.offer.service;

import lmc.offer.model.Offer;
import lmc.offer.repository.OfferRepository;
import org.springframework.stereotype.Service;

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
}
