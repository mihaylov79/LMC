package lmc.configuration.service;

import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.configurationUnitOption.model.ConfigurationUnitOption;
import lmc.option.model.Option;
import lmc.web.dto.OptionSelectionDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationServiceAggregationTest {

    @Test
    void addOptionsFromSelections_aggregatesDuplicates() throws Exception {
        UUID optId = UUID.randomUUID();

        OptionSelectionDTO s1 = new OptionSelectionDTO();
        s1.setOptionId(optId); s1.setQuantity(1);
        OptionSelectionDTO s2 = new OptionSelectionDTO();
        s2.setOptionId(optId); s2.setQuantity(2);

        // prepare option map with Option object so fallback is not used
        Option opt = Option.builder().id(optId).build();
        Map<UUID, Option> optionMap = new HashMap<>();
        optionMap.put(optId, opt);

        ConfigurationUnit unit = ConfigurationUnit.builder().build();

        // create configurationService with null dependencies (we won't use them)
        ConfigurationService svc = new ConfigurationService(null, null, null, null, null);

        // invoke private method addOptionsFromSelections(ConfigurationUnit, List, Map)
        Method m = ConfigurationService.class.getDeclaredMethod("addOptionsFromSelections", ConfigurationUnit.class, List.class, Map.class);
        m.setAccessible(true);
        m.invoke(svc, unit, List.of(s1, s2), optionMap);

        // verify aggregation: one ConfigurationUnitOption with quantity = 3
        assertNotNull(unit.getOptions());
        assertEquals(1, unit.getOptions().size());
        ConfigurationUnitOption cuo = unit.getOptions().get(0);
        assertEquals(optId, cuo.getOption().getId());
        assertEquals(3, cuo.getQuantity());
    }
}

