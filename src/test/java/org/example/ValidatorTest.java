package org.example;

import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@Epic("Валидация входных данных")
@ExtendWith(AllureJunit5.class)
class ValidatorTest {

    @TempDir
    Path tempDir;

    // === Тесты для CSV ===

    @Feature("Сравнение CSV-файлов")
    @Story("Идентичные файлы")
    @Description("Проверка, что идентичные CSV-файлы распознаются как валидные")
    @Test
    void compareCsvFiles_identical() throws Exception {
        Path ideal = TestUtils.createTempCsv("id,name", "1,Alice", "2,Bob");
        Path user = TestUtils.createTempCsv("id,name", "1,Alice", "2,Bob");

        boolean result = ValidatorCLI.compareCsvFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isTrue();
    }

    @Feature("Сравнение CSV-файлов")
    @Story("Расхождения в данных")
    @Description("Проверка, что наличие лишних или отсутствующих строк в CSV приводит к невалидности")
    @Test
    void compareCsvFiles_missingAndExtra() throws Exception {
        Path ideal = TestUtils.createTempCsv("id,name", "1,Alice", "2,Bob");
        Path user = TestUtils.createTempCsv("id,name", "1,Alice", "3,Charlie");

        boolean result = ValidatorCLI.compareCsvFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isFalse();
    }

    // === Тесты для .docx ===

    @Feature("Сравнение .docx-документов")
    @Story("Идентичные документы")
    @Description("Проверка, что идентичные .docx-файлы распознаются как валидные")
    @Test
    void compareDocxFiles_identical() throws Exception {
        Path ideal = TestUtils.createTempDocx("Line 1", "Line 2");
        Path user = TestUtils.createTempDocx("Line 1", "Line 2");

        boolean result = ValidatorCLI.compareDocxFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isTrue();
    }

    @Feature("Сравнение .docx-документов")
    @Story("Различия в содержимом")
    @Description("Проверка, что различия в строках документов приводят к невалидности")
    @Test
    void compareDocxFiles_different() throws Exception {
        Path ideal = TestUtils.createTempDocx("Line 1", "Line 2");
        Path user = TestUtils.createTempDocx("Line 1", "Line 3");

        boolean result = ValidatorCLI.compareDocxFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isFalse();
    }

    // === Тесты для Postman JSON ===

    @Feature("Анализ Postman Collection")
    @Story("Валидный JSON с переменными и тестами")
    @Description("Проверка, что коллекция с корректным JSON, переменными и тестами считается валидной")
    @Test
    void analyzePostmanJson_validWithTestsAndVars() throws Exception {
        String json = """
        {
          "info": { "name": "Test" },
          "variable": [{"key": "url", "value": "https://api.com"}],
          "item": [
            {
              "name": "Request",
              "request": { "method": "GET", "url": "https://api.com" },
              "event": [
                {
                  "listen": "test",
                  "script": {
                    "exec": ["pm.test('Status', function() { pm.response.to.have.status(200); });"]
                  }
                }
              ]
            }
          ]
        }
        """;
        Path jsonFile = tempDir.resolve("collection.json");
        java.nio.file.Files.write(jsonFile, Collections.singletonList(json));

        boolean result = ValidatorCLI.analyzePostmanJsonForTest(jsonFile.toString());
        assertThat(result).isTrue();
    }

    @Feature("Анализ Postman Collection")
    @Story("Невалидный JSON")
    @Description("Проверка, что синтаксически неверный JSON распознаётся как невалидный")
    @Test
    void analyzePostmanJson_invalidJson() throws Exception {
        Path jsonFile = tempDir.resolve("invalid.json");
        java.nio.file.Files.write(jsonFile, Collections.singletonList("{ invalid json }"));

        boolean result = ValidatorCLI.analyzePostmanJsonForTest(jsonFile.toString());
        assertThat(result).isFalse();
    }

    @Feature("Анализ Postman Collection")
    @Story("Валидный JSON без доп. элементов")
    @Description("Проверка, что валидный JSON без переменных и тестов всё равно считается валидным")
    @Test
    void analyzePostmanJson_noTestsNoVars() throws Exception {
        String json = """
        {
          "info": { "name": "Test" },
          "item": []
        }
        """;
        Path jsonFile = tempDir.resolve("collection.json");
        java.nio.file.Files.write(jsonFile, Collections.singletonList(json));

        boolean result = ValidatorCLI.analyzePostmanJsonForTest(jsonFile.toString());
        assertThat(result).isTrue();
    }
}