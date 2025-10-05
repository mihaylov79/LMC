package lmc.configredUnit.repository;

import lmc.configredUnit.model.ConfiguredUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConfiguredUnitRepository extends JpaRepository<ConfiguredUnit, UUID> {
}
