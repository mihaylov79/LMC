package lmc.currencyFixing.repository;

import lmc.currencyFixing.model.CurrencyFixing;
import lmc.unit.model.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyFixingRepository extends JpaRepository<CurrencyFixing, UUID> {

    /**
     * Намира фиксинг за дадена валута.
     * При SaveOrUpdate подход винаги има точно 1 запис per валута.
     */

    Optional<CurrencyFixing> findByCurrency(CurrencyType currency);
}

