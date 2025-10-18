package lmc.configurationUnit;

import lmc.configurationUnit.model.ConfigurationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConfigurationUnitRepository extends JpaRepository<ConfigurationUnit, UUID> {
}
