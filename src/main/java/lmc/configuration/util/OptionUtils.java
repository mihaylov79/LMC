package lmc.configuration.util;

import lmc.configurableUnit.model.ConfiguredUnitOption;
import lmc.configurationUnitOption.model.ConfigurationUnitOption;
import lmc.web.dto.OptionSelectionDTO;
import lmc.option.model.Option;

import java.util.*;
import java.util.stream.Collectors;

public final class OptionUtils {

    private OptionUtils() {}

    public static Map<UUID, Integer> toMapFromSelections(List<OptionSelectionDTO> selections) {
        if (selections == null || selections.isEmpty()) return Map.of();
        return selections.stream()
                .filter(s -> s != null && s.getOptionId() != null)
                .collect(Collectors.toMap(
                        OptionSelectionDTO::getOptionId,
                        s -> Math.max(1, s.getQuantity()),
                        Integer::sum
                ));
    }

    public static Map<UUID, Integer> toMapFromConfigurationUnitOptions(List<ConfigurationUnitOption> options) {
        if (options == null || options.isEmpty()) return Map.of();
        return options.stream()
                .filter(o -> o != null && o.getOption() != null && o.getOption().getId() != null)
                .collect(Collectors.toMap(
                        o -> o.getOption().getId(),
                        ConfigurationUnitOption::getQuantity,
                        Integer::sum
                ));
    }

    public static Map<UUID, Integer> toMapFromConfiguredUnitOptions(List<ConfiguredUnitOption> templateOptions) {
        if (templateOptions == null || templateOptions.isEmpty()) return Map.of();
        return templateOptions.stream()
                .filter(Objects::nonNull)
                .map(ConfiguredUnitOption::getOption)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Option::getId,
                        o -> 1,
                        Integer::sum
                ));
    }

    public static Set<UUID> collectOptionIdsFromSelectionsAcrossUnits(List<?> unitsWithSelections) {
        // generic helper if needed in future - not used right now
        return Collections.emptySet();
    }

    public static String signatureFromMap(Map<UUID, Integer> map) {
        if (map == null || map.isEmpty()) return "";
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey().toString() + ":" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    public static String signatureFromConfigUnitOptions(List<ConfigurationUnitOption> options) {
        if (options == null || options.isEmpty()) return "";
        Map<UUID,Integer> map = toMapFromConfigurationUnitOptions(options);
        return signatureFromMap(map);
    }
}
