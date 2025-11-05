package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.Set;
import java.util.LinkedHashSet;
import static org.assertj.core.api.Assertions.*;

class ValidatorTest {

    @TempDir
    Path tempDir;

    // === Тесты для CSV ===
    @Test
    void compareCsvFiles_identical() throws Exception {
        Path ideal = TestUtils.createTempCsv("id,name", "1,Alice", "2,Bob");
        Path user = TestUtils.createTempCsv("id,name", "1,Alice", "2,Bob");

        boolean result = ValidatorCLI.compareCsvFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isTrue();
    }

    @Test
    void compareCsvFiles_missingAndExtra() throws Exception {
        Path ideal = TestUtils.createTempCsv("id,name", "1,Alice", "2,Bob");
        Path user = TestUtils.createTempCsv("id,name", "1,Alice", "3,Charlie");

        boolean result = ValidatorCLI.compareCsvFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isFalse();
    }

    // === Тесты для .docx ===
    @Test
    void compareDocxFiles_identical() throws Exception {
        Path ideal = TestUtils.createTempDocx("Line 1", "Line 2");
        Path user = TestUtils.createTempDocx("Line 1", "Line 2");

        boolean result = ValidatorCLI.compareDocxFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isTrue();
    }

    @Test
    void compareDocxFiles_different() throws Exception {
        Path ideal = TestUtils.createTempDocx("Line 1", "Line 2");
        Path user = TestUtils.createTempDocx("Line 1", "Line 3");

        boolean result = ValidatorCLI.compareDocxFilesForTest(ideal.toString(), user.toString());
        assertThat(result).isFalse();
    }

    // === Тесты для Postman JSON ===
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
        java.nio.file.Files.write(jsonFile, java.util.Collections.singletonList(json));

        boolean result = ValidatorCLI.analyzePostmanJsonForTest(jsonFile.toString());
        assertThat(result).isTrue();
    }

    @Test
    void analyzePostmanJson_invalidJson() throws Exception {
        Path jsonFile = tempDir.resolve("invalid.json");
        java.nio.file.Files.write(jsonFile, java.util.Collections.singletonList("{ invalid json }"));

        boolean result = ValidatorCLI.analyzePostmanJsonForTest(jsonFile.toString());
        assertThat(result).isFalse();
    }

    @Test
    void analyzePostmanJson_noTestsNoVars() throws Exception {
        String json = """
        {
          "info": { "name": "Test" },
          "item": []
        }
        """;
        Path jsonFile = tempDir.resolve("collection.json");
        java.nio.file.Files.write(jsonFile, java.util.Collections.singletonList(json));

        boolean result = ValidatorCLI.analyzePostmanJsonForTest(jsonFile.toString());
        assertThat(result).isTrue(); // JSON валиден, даже если нет тестов/переменных
    }

    // === Вспомогательные методы для тестов (в основном классе) ===
    // Они будут вызывать приватную логику, но через публичные обёртки
}