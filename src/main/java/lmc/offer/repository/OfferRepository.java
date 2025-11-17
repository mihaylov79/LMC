package lmc.offer.repository;

import lmc.offer.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {

    // Use FUNCTION('YEAR', ...) for better portability across JPA providers/DBs
    @Query("SELECT COUNT(o) FROM Offer o WHERE FUNCTION('YEAR', o.created) = :createdYear")
    Long countByCreated_Year(@Param("createdYear") int createdYear);

    boolean existsByOfferNumber(String offerNumber);

    Optional<Offer> findByOfferNumber(String offerNumber);

    @Query("SELECT o FROM Offer o " +
           "LEFT JOIN FETCH o.configuration c " +
           "LEFT JOIN FETCH c.includedUnits cu " +
           "LEFT JOIN FETCH cu.configurableUnit " +
           "WHERE o.id = :offerId")
    Optional<Offer> findByIdWithConfiguration(@Param("offerId") UUID offerId);
}
