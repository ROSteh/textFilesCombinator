package ru.rosteh.textFilesCombinator.shellComponents;

import org.jline.utils.AttributedStringBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.ScriptShellApplicationRunner;
import org.springframework.shell.result.DefaultResultHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Молдавский Максим on 12.11.2020
 */
@SpringBootTest(properties = {
        InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=false",
        ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT + ".enabled=false"
})
class filesCombinatorCLITest {
    @TempDir
    Path tempDir;
    @Autowired
    private Shell shell;
    @Autowired
    private DefaultResultHandler resultHandler;

    /**
     * Тест склеивания файлов
     */
    @Test
    void mergeFiles() throws IOException {
        Path file = tempDir.resolve("1.txt");
        Files.write(file, Arrays.asList("1", "2", "3"));

        Path file2 = tempDir.resolve("2.txt");
        Files.write(file2, Arrays.asList("4", "5", "6"));

        Path subPath = tempDir.resolve("test");
        Path file3 = subPath.resolve("3.txt");
        Files.createDirectories(file3.getParent());
        Files.write(file3, Arrays.asList("7", "8", "9"));

        Path output = tempDir.resolve("res.txt");
        Object result = shell.evaluate(() -> "merge -p " + tempDir.toString() + " -out " + output.toString());

        assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"), Files.readAllLines(output));
        resultHandler.handleResult(result);
    }

    /**
     * Тест склеивания файлов
     */
    @Test
    void mergeFilesResInDir() throws IOException {
        Path file = tempDir.resolve("1.txt");
        Files.write(file, Arrays.asList("1", "2", "3"));

        Path output = tempDir.resolve("test2"+File.separator+"res.txt");
        Object result = shell.evaluate(() -> "merge -p " + tempDir.toString() + " -out " + output.toString());

        assertEquals("Не возможно создать файл: "+output, result);
        resultHandler.handleResult(result);
    }

    /**
     * Тест чтения файлов
     */
    @Test
    void readFiles() throws IOException {
        Path file = tempDir.resolve("1.txt");
        Files.write(file, Arrays.asList("1", "2", "3"));

        Path file2 = tempDir.resolve("2.txt");
        Files.write(file2, Arrays.asList("3", "4", "5"));

        Path subPath = tempDir.resolve("test");
        Path file3 = subPath.resolve("3.txt");
        Files.createDirectories(file3.getParent());
        Files.write(file3, Arrays.asList("6", "7", "8"));

        Object result = shell.evaluate(() -> "read " + tempDir.toString());

        AttributedStringBuilder linesFromFiles = new AttributedStringBuilder()
                .append("Читаем содержимое файлов:").append(System.lineSeparator())
                .append(Arrays.asList("1", "2", "3").toString()).append(System.lineSeparator())
                .append(Arrays.asList("3", "4", "5").toString()).append(System.lineSeparator())
                .append(Arrays.asList("6", "7", "8").toString()).append(System.lineSeparator())
                .append("Конец чтения содержимого файлов.");
        assertEquals(linesFromFiles.toString(), result);
        resultHandler.handleResult(result);
    }

    /**
     * Тест получения списка файлов
     */
    @Test
    void listFiles() throws IOException {
        Path file = tempDir.resolve("1.txt");
        Files.write(file, Arrays.asList("1", "2", "3"));

        Path file2 = tempDir.resolve("2.txt");
        Files.write(file2, Arrays.asList("3", "4", "5"));

        Path subPath = tempDir.resolve("test");
        Path file3 = subPath.resolve("3.txt");
        Files.createDirectories(file3.getParent());
        Files.write(file3, Arrays.asList("6", "7", "8"));

        Object result = shell.evaluate(() -> "ls " + tempDir.toString());

        AttributedStringBuilder listFiles = new AttributedStringBuilder()
                .append("Читаем список файлов:").append(System.lineSeparator())
                .append(file.toString()).append(System.lineSeparator())
                .append(file2.toString()).append(System.lineSeparator())
                .append(file3.toString()).append(System.lineSeparator())
                .append("Конец списка.");
        assertEquals(listFiles.toString(), result);
        resultHandler.handleResult(result);
    }

    /**
     * Проверка исключения "Не существующая директория."
     */
    @Test
    void noDir() {
        String badDir = tempDir.toString()+ File.separator+ "badDir";
        Object result = shell.evaluate(() -> "ls " + badDir);

        assertEquals("Не существующая директория: "+badDir, result);

        resultHandler.handleResult(result);
    }

    /**
     * Проверка файл существует
     */
    @Test
    void alreadyExistOut() throws IOException {
        Path file = tempDir.resolve("1.txt");
        Files.write(file, Arrays.asList("1", "2", "3"));

        Path output = tempDir.resolve("res.txt");
        Files.write(output, Collections.singletonList("0"));

        Object result = shell.evaluate(() -> "merge -p " + tempDir.toString() + " -out " + output.toString());

       assertEquals("Файл уже существует: "+output, result);

        resultHandler.handleResult(result);
    }

    /**
     * Проверка файл существует
     */
    @Test
    void badCharset() throws IOException {
        String badCharset = "bla";

        Path file = tempDir.resolve("1.txt");
        Files.write(file, Arrays.asList("1", "2", "3"));

        Path output = tempDir.resolve("res.txt");

        Object result = shell.evaluate(() -> "merge -p " + tempDir.toString() + " -out " + output.toString() +" -chr "+ badCharset);

        assertEquals("Не верная кодировка: " + badCharset, result);

        resultHandler.handleResult(result);
    }
}