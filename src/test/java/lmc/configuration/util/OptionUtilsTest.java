package lmc.configuration.util;

import lmc.configurationUnitOption.model.ConfigurationUnitOption;
import lmc.web.dto.OptionSelectionDTO;
import lmc.option.model.Option;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OptionUtilsTest {

    @Test
    void toMapFromSelections_handlesDuplicatesAndNulls() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        OptionSelectionDTO s1 = new OptionSelectionDTO(id1, 2);
        OptionSelectionDTO s2 = new OptionSelectionDTO(id1, 3);
        OptionSelectionDTO s3 = new OptionSelectionDTO(id2, 1);
        OptionSelectionDTO sNull = null;

        Map<UUID, Integer> map = OptionUtils.toMapFromSelections(List.of(s1, s2, s3, sNull));
        assertEquals(2, map.size());
        assertEquals(5, map.get(id1));
        assertEquals(1, map.get(id2));
    }

    @Test
    void signatureFromMap_and_fromConfigUnitOptions_consistent() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        var map = Map.of(id1, 1, id2, 2);
        String sig = OptionUtils.signatureFromMap(map);
        // signature should contain both ids and their quantities
        assertTrue(sig.contains(id1.toString()));
        assertTrue(sig.contains(id2.toString()));

        // build ConfigurationUnitOption list and test signatureFromConfigUnitOptions
        ConfigurationUnitOption o1 = ConfigurationUnitOption.builder().option(Option.builder().id(id1).build()).quantity(1).build();
        ConfigurationUnitOption o2 = ConfigurationUnitOption.builder().option(Option.builder().id(id2).build()).quantity(2).build();

        String sig2 = OptionUtils.signatureFromConfigUnitOptions(List.of(o1, o2));
        assertEquals(sig, sig2);
    }
}

