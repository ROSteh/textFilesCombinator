package ru.rosteh.textFilesCombinator.shellComponents;


import org.jline.utils.AttributedStringBuilder;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Контроллер с коммандами CLI
 * <p>
 * Created by Молдавский Максим on 12.11.2020
 */
@ShellComponent
public class filesCombinatorCLI {

    /**
     * Путь к файлу вывода
     */
    private static Path output;

    /**
     * Кодировка файла вывода
     */
    private static Charset charset;

    /**
     * Объединение файлов в 1 файл
     *
     * @param path    Директория где находятся объединяемые файлы
     * @param outfile файл результата объединения
     * @param ext     Расширение объединяемых файлов (по умолчанию txt)
     * @param chr     Кодировка (по умолчанию UTF_8)
     * @return Результат
     */
    @ShellMethod(key = "merge", value = "Склеивает файлы в единый файл")
    public String mergeFiles(
            @ShellOption(value = {"path", "-p"}, help = "Директория где находятся файлы для склеивания") String path,
            @ShellOption(value = {"file", "-out"}, help = "Укажите файл") String outfile,
            @ShellOption(value = {"extension", "-ext"}, defaultValue = "txt", help = "Расширение файлов.") String ext,
            @ShellOption(value = {"charset", "-chr"}, defaultValue = "UTF8", help = "Кодировка файла вывода.") String chr) {

        File file = new File(path);
        if (!file.isDirectory()) {
            return "Не существующая директория: " + file.getAbsolutePath();
        }

        File outFile = new File(outfile);
        try {
            if (!outFile.createNewFile()) {
                return "Файл уже существует: " + outFile.getAbsolutePath();
            };
        } catch (Exception e) {
            return "Не возможно создать файл: " + outFile.getAbsolutePath();
        }

        output = outFile.toPath();
        try {
            charset = Charset.forName(chr);
        } catch (Exception e) {
            return "Не верная кодировка: " + chr;
        }
        AttributedStringBuilder result = (new AttributedStringBuilder());
        result.append("Склеиваем файлы:");

        listAllFiles(path, ext, (res) -> {
            result.append(writeContent(res)).append(System.lineSeparator());
        });

        result.append("Склеивание файлов завершено.");
        return result.toString();
    }

    /**
     * Читает все файлы по порядку, сортируя по имени файла
     *
     * @param path Директория где находятся файлы
     * @param ext  Расширение файлов для чтения
     * @return Результат
     */
    @ShellMethod(key = "read", value = "Показывает содержимое файлов.")
    public String readFiles(
            @ShellOption(value = {"path", "p"}, help = "Директория где находятся файлы.") String path,
            @ShellOption(value = {"ext", "extension"}, defaultValue = "txt", help = "Расширение файлов.") String ext) {

        File file = new File(path);
        if (!file.isDirectory()) {
            return "Не существующая директория: " + file.getAbsolutePath();
        }

        AttributedStringBuilder result = (new AttributedStringBuilder());
        result.append("Читаем содержимое файлов:").append(System.lineSeparator());

        listAllFiles(path, ext, (res) -> {
            result.append(readContent(res)).append(System.lineSeparator());
        });

        result.append("Конец чтения содержимого файлов.");
        return result.toString();
    }

    /**
     * Показывает список файлов
     *
     * @param path Директория где находятся файлы
     * @param ext  Расширение файлов
     * @return Список файлов
     */
    @ShellMethod(key = {"list", "ls"}, value = "Показывает список файлов.")
    public String listFiles(
            @ShellOption(value = {"path", "p"}, help = "Директория где находятся файлы.") String path,
            @ShellOption(value = {"ext", "extension"}, defaultValue = "txt", help = "Расширение файлов.") String ext) {

        File file = new File(path);
        if (!file.isDirectory()) {
            return "Не существующая директория: " + file.getAbsolutePath();
        }

        AttributedStringBuilder result = (new AttributedStringBuilder());
        result.append("Читаем список файлов:").append(System.lineSeparator());

        listAllFiles(path, ext, (respath) -> {
            result.append(respath.toString()).append(System.lineSeparator());
        });
        result.append("Конец списка.");
        return result.toString();
    }

    /**
     * Получение пути до каждого файла в директории и субдиректориях и выполнение block
     *
     * @param path      Директория где находятся файлы
     * @param block     выполняемая функция
     * @param extension Расширение файлов
     */
    private static void listAllFiles(String path, String extension, Consumer<Path> block) {
        try {
            Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .filter(s -> s.toString().endsWith(extension))
                    .filter(s -> !s.equals(output))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(filePath -> {
                        try {
                            block.accept(filePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Дописывает содержимое файла из параметров в конец {@link #output}
     *
     * @param filePath путь к файлу
     * @return Строка результата выполнения записи
     */
    private static String writeContent(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath, charset);
            Files.write(output, lines, charset, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            return String.format("Добавлено %d строк из файла %s", lines.size(), filePath.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Читает содержимое файла
     *
     * @param filePath путь к файлу
     * @return содержимое файла
     */
    private String readContent(Path filePath) {
        try {
            return Files.readAllLines(filePath).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
