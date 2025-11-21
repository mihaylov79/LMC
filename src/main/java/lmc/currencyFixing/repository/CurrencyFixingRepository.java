package lmc.currencyFixing.repository;

import lmc.currencyFixing.model.CurrencyFixing;
import lmc.unit.model.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyFixingRepository extends JpaRepository<CurrencyFixing, UUID> {

    /**
     * Намира последния (най-актуален) фиксинг за дадена валута.
     * Сортира по validFrom и lastUpdated в низходящ ред и връща първия резултат.
     */
    Optional<CurrencyFixing> findFirstByCurrencyOrderByValidFromDescLastUpdatedDesc(CurrencyType currency);

    /**
     * Намира фиксинг за дадена валута, валиден на или преди конкретна дата.
     * Връща най-близкия фиксинг преди или на дадената дата.
     */
    Optional<CurrencyFixing> findFirstByCurrencyAndValidFromLessThanEqualOrderByValidFromDesc(
            CurrencyType currency,
            LocalDate date);
}

