package lmc.configurationUnit.repository;

import lmc.configurationUnit.model.ConfigurationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigurationUnitRepository extends JpaRepository<ConfigurationUnit, UUID> {
    Optional<ConfigurationUnit> findByConfigurationIdAndConfigurableUnitId(UUID configurationId, UUID configurableUnitId);

    Optional<ConfigurationUnit> findByConfigurationIdAndConfigurableUnitIdAndOptionsSignature(UUID configurationId, UUID configurableUnitId, String optionsSignature);
}
