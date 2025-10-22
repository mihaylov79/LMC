package lmc.configurationUnit.service;

import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.configurationUnit.repository.ConfigurationUnitRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ConfigurationUnitService {

    private final ConfigurationUnitRepository repository;

    public ConfigurationUnitService(ConfigurationUnitRepository repository) {
        this.repository = repository;
    }


    public Optional<ConfigurationUnit> findConfigurationUnitByConfigurationIdAndConfigurableUnitId(UUID configurationId, UUID configurableUnitId){
         return repository.findByConfigurationIdAndConfigurableUnitId(configurationId,configurableUnitId);
    }
}
