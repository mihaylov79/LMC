package lmc.offer.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lmc.configuration.model.Configuration;
import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.configurationUnitOption.model.ConfigurationUnitOption;
import lmc.web.dto.ConfigurationSnapshotDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper за конвертиране на конфигурации към snapshot DTO формат.
 * Използва се при създаване на оферти за да запази пълен snapshot на конфигурацията.
 */
@Component
public class OfferMapper {

    private final ObjectMapper objectMapper;

    public OfferMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Създава JSON snapshot на конфигурацията към момента на създаване на офертата.
     * Запазва всички данни: код, описание, модел, единици, опции и техните цени.
     *
     * @param configuration конфигурацията за snapshot
     * @return JSON string със snapshot данни
     */
    public String createConfigurationSnapshot(Configuration configuration) {
        try {
            ConfigurationSnapshotDTO snapshot = mapToSnapshotDTO(configuration);
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Грешка при сериализация на snapshot", e);
        }
    }

    /**
     * Мапва Configuration entity към ConfigurationSnapshotDTO.
     *
     * @param configuration конфигурацията
     * @return snapshot DTO
     */
    public ConfigurationSnapshotDTO mapToSnapshotDTO(Configuration configuration) {
        return ConfigurationSnapshotDTO.builder()
                .code(configuration.getCode())
                .imageUrl(configuration.getImageUrl())
                .line(configuration.getLine())
                .type(configuration.getType())
                .description(configuration.getDescription())
                .model(configuration.getModel())
                .totalPrice(configuration.getTotalPrice())
                .priceUpdateDate(configuration.getPriceUpdateDate())
                .includedUnits(configuration.getIncludedUnits().stream()
                        .map(this::mapConfigurationUnitToSnapshot)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Извлича snapshot данните на конфигурацията от JSON.
     * Използвай това вместо offer.getConfiguration() за да видиш ОРИГИНАЛНАТА конфигурация
     * каквато е била към момента на създаване на офертата.
     *
     * @param snapshotJson JSON string със snapshot данни
     * @return snapshot на конфигурацията или null ако няма snapshot
     */
    public ConfigurationSnapshotDTO parseConfigurationSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(snapshotJson, ConfigurationSnapshotDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Грешка при десериализация на snapshot", e);
        }
    }

    private ConfigurationSnapshotDTO.ConfigurationUnitSnapshotDTO mapConfigurationUnitToSnapshot(ConfigurationUnit cu) {
        return ConfigurationSnapshotDTO.ConfigurationUnitSnapshotDTO.builder()
                .configurableUnitCode(cu.getConfigurableUnit().getCode())
                .unitName(cu.getConfigurableUnit().getUnit().getName())
                .unitDescription(cu.getConfigurableUnit().getUnit().getDescription())
                .unitPrice(cu.getConfigurableUnit().getUnit().getPrice())
                .quantity(cu.getQuantity())
                .options(cu.getOptions().stream()
                        .map(this::mapOptionToSnapshot)
                        .collect(Collectors.toList()))
                .build();
    }

    private ConfigurationSnapshotDTO.OptionSnapshotDTO mapOptionToSnapshot(ConfigurationUnitOption opt) {
        return ConfigurationSnapshotDTO.OptionSnapshotDTO.builder()
                .optionCode(opt.getOption().getCode())
                .optionName(opt.getOption().getName())
                .optionDescription(opt.getOption().getDescription())
                .optionPrice(opt.getOption().getPrice())
                .quantity(opt.getQuantity())
                .build();
    }
}

