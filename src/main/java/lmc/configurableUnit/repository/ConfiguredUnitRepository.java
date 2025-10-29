package lmc.configurableUnit.repository;

import lmc.configurableUnit.model.ConfiguredUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguredUnitRepository extends JpaRepository<ConfiguredUnit, UUID> {
    Optional<ConfiguredUnit> findByCodeAndActiveIsTrue(String code);

    @Query("select distinct cu from ConfiguredUnit cu " +
            "left join fetch cu.unit u " +
            "left join fetch cu.options co " +
            "left join fetch co.option opt")
    List<ConfiguredUnit> findAllWithOptions();
}
