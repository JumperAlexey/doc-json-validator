package org.example;

import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Валидация данных")
@Feature("Сравнение наборов строк")
@ExtendWith(AllureJunit5.class)
class ValidatorCLITest {

    @Story("Идентичные наборы строк")
    @Description("Проверка, что два одинаковых набора строк считаются эквивалентными")
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

    @Story("Отсутствующая строка")
    @Description("Проверка, что отсутствие строки в пользовательском наборе приводит к несоответствию")
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