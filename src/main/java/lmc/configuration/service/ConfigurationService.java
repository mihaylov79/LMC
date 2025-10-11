package lmc.configuration.service;


import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configuration.model.Configuration;
import lmc.configuration.repository.ConfigurationRepository;
import lmc.web.dto.CreateNewConfigurationRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final ConfigurableUnitService configurableUnitService;

    public ConfigurationService(ConfigurationRepository configurationRepository, ConfigurableUnitService configurableUnitService) {
        this.configurationRepository = configurationRepository;
        this.configurableUnitService = configurableUnitService;
    }


    public Configuration createNewConfiguration(CreateNewConfigurationRequest request){
        List<ConfigurableUnit>units = request.getUnits().stream()
                .map(configurableUnitService::createUnit).toList();


        Configuration configuration = Configuration.builder()
                .imageUrl(request.getImgUrl())
                .code(request.getCode())
                .line(request.getLine())
                .type(request.getType())
                .description(request.getDescription())
                .model(request.getModel())
                .build();

        return configurationRepository.save(configuration);
    }

}
