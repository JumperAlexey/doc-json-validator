package org.example;

import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.LinkedHashSet;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorCLITest {

    @Test
    void testCompareLineSets_Identical() {
        Set<String> ideal = new LinkedHashSet<>();
        ideal.add("line1");
        ideal.add("line2");

        Set<String> user = new LinkedHashSet<>();
        user.add("line1");
        user.add("line2");

        assertTrue(ValidatorCLI.compareLineSetsForTest(ideal, user, "test"));
    }

    @Test
    void testCompareLineSets_MissingLine() {
        Set<String> ideal = new LinkedHashSet<>();
        ideal.add("line1");
        ideal.add("line2");

        Set<String> user = new LinkedHashSet<>();
        user.add("line1");

        assertFalse(ValidatorCLI.compareLineSetsForTest(ideal, user, "test"));
    }
}