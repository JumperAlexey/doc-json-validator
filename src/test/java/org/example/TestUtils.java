package org.example;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {

    // Создаём временный .docx файл
    public static Path createTempDocx(String... lines) throws IOException {
        Path tempFile = Files.createTempFile("test", ".docx");
        try (XWPFDocument document = new XWPFDocument()) {
            for (String line : lines) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.createRun().setText(line);
            }
            try (FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
                document.write(out);
            }
        }
        return tempFile;
    }

    // Создаём временный CSV-файл
    public static Path createTempCsv(String... lines) throws IOException {
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.write(tempFile, java.util.Arrays.asList(lines));
        return tempFile;
    }
}