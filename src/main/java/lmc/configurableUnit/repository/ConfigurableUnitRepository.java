package lmc.configurableUnit.repository;

import lmc.configurableUnit.model.ConfigurableUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConfigurableUnitRepository extends JpaRepository<ConfigurableUnit, UUID> {
}
