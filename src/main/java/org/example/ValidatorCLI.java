package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ValidatorCLI {

    public static void main(String[] args) {
        String mode = (args.length > 0) ? args[0] : "all";

        switch (mode) {
            case "docs-servis":
                System.out.println("=== Проверка: docs_servis_check ===");
                System.exit(compareDocxFiles("docs_servis_check/ideal_doc.docx", "docs_servis_check/user_doc.docx") ? 0 : 1);

            case "check-list":
                System.out.println("=== Проверка: check_list_check ===");
                System.exit(compareDocxFiles("check_list_check/ideal_check.docx", "check_list_check/user_check.docx") ? 0 : 1);

            case "csv":
                System.out.println("=== Проверка: doc_load_check ===");
                System.exit(compareCsvFiles("doc_load_check/ideal_file.csv", "doc_load_check/user_file.csv") ? 0 : 1);

            case "json":
                System.out.println("=== Проверка: ckeck_json_check/postman_collection.json ===");
                System.exit(analyzePostmanJson("ckeck_json_check/postman_collection.json") ? 0 : 1);

            case "all":
                // Для локальной отладки — не используется в CI
                boolean allValid = true;
                System.out.println("=== Проверка: docs_servis_check ===");
                allValid &= compareDocxFiles("docs_servis_check/ideal_doc.docx", "docs_servis_check/user_doc.docx");
                System.out.println("\n=== Проверка: check_list_check ===");
                allValid &= compareDocxFiles("check_list_check/ideal_check.docx", "check_list_check/user_check.docx");
                System.out.println("\n=== Проверка: doc_load_check ===");
                allValid &= compareCsvFiles("doc_load_check/ideal_file.csv", "doc_load_check/user_file.csv");
                System.out.println("\n=== Проверка: ckeck_json_check/postman_collection.json ===");
                allValid &= analyzePostmanJson("ckeck_json_check/postman_collection.json");
                System.out.println("\n=== ИТОГ ===");
                if (allValid) {
                    System.out.println("✅ Все проверки пройдены");
                } else {
                    System.out.println("❌ Найдены расхождения или ошибки");
                }
                System.exit(allValid ? 0 : 1);

            default:
                System.err.println("Usage: java ValidatorCLI [docs-servis|check-list|csv|json|all]");
                System.exit(1);
        }
    }

    // === Сравнение .docx файлов ===
    private static boolean compareDocxFiles(String idealPath, String userPath) {
        try {
            String idealText = extractTextFromDocx(idealPath);
            String userText = extractTextFromDocx(userPath);

            Set<String> idealLines = cleanLines(idealText);
            Set<String> userLines = cleanLines(userText);

            return compareLineSets(idealLines, userLines, "пользовательском файле");
        } catch (Exception e) {
            System.err.println("Ошибка при сравнении .docx: " + e.getMessage());
            return false;
        }
    }

    private static String extractTextFromDocx(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path);
             XWPFDocument doc = new XWPFDocument(fis)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            return extractor.getText();
        }
    }

    // === Сравнение CSV файлов ===
    private static boolean compareCsvFiles(String idealPath, String userPath) {
        try {
            Set<String> idealLines = readCsvLines(idealPath);
            Set<String> userLines = readCsvLines(userPath);
            return compareLineSets(idealLines, userLines, "пользовательском файле");
        } catch (IOException e) {
            System.err.println("Ошибка при чтении CSV: " + e.getMessage());
            return false;
        }
    }

    private static Set<String> readCsvLines(String path) throws IOException {
        Set<String> lines = new LinkedHashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    // === Общая логика сравнения ===
    private static boolean compareLineSets(Set<String> ideal, Set<String> user, String target) {
        Set<String> missing = new LinkedHashSet<>(ideal);
        missing.removeAll(user);

        Set<String> extra = new LinkedHashSet<>(user);
        extra.removeAll(ideal);

        if (missing.isEmpty() && extra.isEmpty()) {
            System.out.println("✅ Ошибок не обнаружено");
            return true;
        } else {
            if (!missing.isEmpty()) {
                System.out.println("❌ Отсутствует в " + target + ":");
                for (String line : missing) {
                    System.out.println("  - \"" + line + "\"");
                }
            }
            if (!extra.isEmpty()) {
                System.out.println("❌ Лишнее в " + target + ":");
                for (String line : extra) {
                    System.out.println("  - \"" + line + "\"");
                }
            }
            return false;
        }
    }

    private static Set<String> cleanLines(String text) {
        Set<String> lines = new LinkedHashSet<>();
        String[] splitLines = text.split("\\r?\\n");
        for (String line : splitLines) {
            line = line.trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    // === Анализ Postman Collection ===
    private static boolean analyzePostmanJson(String jsonPath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(new File(jsonPath));
            System.out.println("Валиден ли файл? Да");

            boolean hasVariables = root.has("variable") &&
                    root.get("variable").isArray() &&
                    root.get("variable").size() > 0;
            System.out.println("Есть ли в коллекции переменные? " + (hasVariables ? "Да" : "Нет"));

            boolean hasTests = root.has("item") && root.get("item").isArray() &&
                    checkItemsForTests(root.get("item"));
            System.out.println("Есть ли в коллекции тесты? " + (hasTests ? "Да" : "Нет"));

            return true;
        } catch (IOException e) {
            System.out.println("Валиден ли файл? Нет");
            System.out.println("Есть ли в коллекции переменные? Нет");
            System.out.println("Есть ли в коллекции тесты? Нет");
            return false;
        }
    }

    private static boolean checkItemsForTests(JsonNode items) {
        for (JsonNode item : items) {
            if (item.has("request") && item.has("event") && item.get("event").isArray()) {
                for (JsonNode event : item.get("event")) {
                    if ("test".equals(event.path("listen").asText()) &&
                            event.has("script") &&
                            event.get("script").has("exec")) {
                        return true;
                    }
                }
            }
            if (item.has("item")) {
                if (checkItemsForTests(item.get("item"))) {
                    return true;
                }
            }
        }
        return false;
    }
}