package org.example;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A utility for filtering file contents.
 *
 * @author Malko Anton
 */
public class FileContentFiltering {

    // flags
    private static boolean f = false;
    private static boolean a = false;
    private static boolean out = false;

    // The path where the files will be created
    private static String filePath;

    // The name of the file that will be created only works with the -out flag
    private static String outFileName;
    // Command Line Arguments
    private static String str;

    // Names of files that will be created
    private static final String[] FILE_NAMES = { "integers.txt", "strings.txt", "floats.txt" };

    /**
     * Point of entry
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        isEmptyString(String.join(" ", args));
        init(args);
        isValidCommandLine();
        if (!a) deleteText();
        readFiles();
        if (f || str.contains("-s ")) initStatistics();
    }

    /**
     * Initializing Variables
     *
     * @param args Command line arguments
     */
    private static void init(String[] args) {
        str = String.join(" ", args);

        out = str.contains("-out ");
        f = str.contains("-f ");
        a = str.contains("-a ");

        if (out) {
            outFileName = str.substring(str.indexOf("-out ")).split(" ")[1];
            str = "";

            List<String> list = new ArrayList<>(Arrays.asList(args));
            list.remove(outFileName);
            list.remove("-out");

            str = String.join(" ", list.toArray(new String[0]));
        }

        if (str.contains("-p ")) {
            String prefix = str.substring(str.indexOf("-p")).split(" ")[1];

            if (!out) {
                for (int i = 0; i < FILE_NAMES.length; i++) {
                    FILE_NAMES[i] = prefix + FILE_NAMES[i];
                }
            } else {
                outFileName = prefix + outFileName;
            }
        }

        if (!str.contains("-o ")){
            filePath = System.getProperty("user.dir") + "\\";
        } else {
            filePath = str.substring(str.indexOf("-o ")).split(" ")[1] + "\\";
        }
    }

    /**
     * This method reads data from input files and
     * passes the text and file name to writeFile().
     */
    private static void readFiles() {
        String[] array = str.split(" ");

        for (String i : array) {
            if (i.contains(".txt")) {
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(i))) {
                    while (bufferedReader.ready()) {
                        String tmp = bufferedReader.readLine();

                        if (NumberUtils.isCreatable(tmp) && isFloat(tmp) && !out) {
                            writeInFiles(tmp, FILE_NAMES[2]);
                        } else if (NumberUtils.isCreatable(tmp) && !isFloat(tmp) && !out) {
                            writeInFiles(tmp, FILE_NAMES[0]);
                        } else if (!out) {
                            writeInFiles(tmp, FILE_NAMES[1]);
                        } else {
                            writeInFiles(tmp, outFileName);
                        }
                    }
                } catch (IOException e) {
                    printErrorMessage("Ошибка при чтении файла: ", i);
                }
            }
        }
    }

    /**
     * This method writes text to files
     *
     * @param text Text to be written down
     * @param fileName The name of the file into which the text should be written
     */
    private static void writeInFiles(String text, String fileName) {
        File file = new File(filePath + fileName);

        try(FileWriter writer = new FileWriter(file, file.exists())) {
            writer.write(text + "\n");
        } catch (IOException e) {
            printErrorMessage("Ошибка при записи в файл ", fileName);

        }
    }

    /**
     * Starting statistics calculation
     */
    private static void initStatistics() {
        String[] files = out ? new String[]{outFileName} : FILE_NAMES;

        for (String i : files) {
            HashMap<String, BigDecimal> stats = new HashMap<>()
            {{
                put("Max", BigDecimal.valueOf(Integer.MIN_VALUE));
                put("Min", BigDecimal.valueOf(Integer.MAX_VALUE));
                put("MaxLen", BigDecimal.valueOf(Integer.MIN_VALUE));
                put("MinLen", BigDecimal.valueOf(Integer.MAX_VALUE));
                put("Sum", BigDecimal.valueOf(0));
                put("Count", BigDecimal.valueOf(0));
            }};

            if (new File(filePath + i).exists()){
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(i))) {
                    while (bufferedReader.ready()) {
                        stats.replace("Count", stats.get("Count").add(BigDecimal.valueOf(1)));

                        if (f) {
                            countingStatistics(bufferedReader.readLine(), stats);
                        } else bufferedReader.readLine();
                    }

                    if (stats.get("Count").compareTo(BigDecimal.valueOf(0)) != 0) {
                        printStatistics(i, stats);
                    }
                } catch (IOException e) {
                    printErrorMessage("Ошибка при чтении файла: ", i);
                }
            }
        }
    }

    /**
     * This method calculates statistics
     *
     * @param tmp Line read from file.
     * @param stats Counted Statistics
     */
    private static void countingStatistics(String tmp, HashMap<String, BigDecimal> stats) {
        if (NumberUtils.isCreatable(tmp)) {
            BigDecimal bigDecimal = new BigDecimal(tmp);
            BigDecimal sum = bigDecimal.add(stats.get("Sum"));

            stats.replace("Max", bigDecimal.compareTo((stats.get("Max"))) > 0  ? bigDecimal : stats.get("Max"));
            stats.replace("Min", bigDecimal.compareTo((stats.get("Min"))) < 0 ? bigDecimal : stats.get("Min"));
            stats.replace("Sum", sum.scale() > 0 ? sum.setScale(3, RoundingMode.HALF_UP) : sum);
        } else {
            BigDecimal length = new BigDecimal(tmp.length());

            stats.replace("MaxLen", length.compareTo((stats.get("MaxLen"))) > 0  ? length : stats.get("MaxLen"));
            stats.replace("MinLen", length.compareTo((stats.get("MinLen"))) < 0 ? length : stats.get("MinLen"));
        }
    }

    /**
     * This method displays the calculated statistics
     *
     * @param fileName Name of the file for which statistics were calculated
     * @param stats Counted Statistics
     */
    private  static  void  printStatistics(String fileName, HashMap<String, BigDecimal> stats) {
        System.out.printf("Количество элементов для файла %s: %s\n", fileName, stats.get("Count"));

        if (stats.get("Sum").compareTo(BigDecimal.valueOf(0)) != 0 && f && !out) {
            System.out.printf("Максимальное значение - %s\n", stats.get("Max"));
            System.out.printf("Минимальное значение - %s\n", stats.get("Min"));
            System.out.printf("Сумма - %s\n", stats.get("Sum"));
            System.out.printf("Среднее значение - %s\n", stats.get("Sum").divide(stats.get("Count"), RoundingMode.HALF_UP));
        } else if (f && !out) {
            System.out.printf("Размер короткой строки - %s\n", stats.get("MinLen"));
            System.out.printf("Размер длинной строки  - %s\n", stats.get("MaxLen"));
        } else if (out) {
            System.out.printf("Максимальное значение - %s\n", stats.get("Max"));
            System.out.printf("Минимальное значение - %s\n", stats.get("Min"));
            System.out.printf("Сумма - %s\n", stats.get("Sum"));
            System.out.printf("Среднее значение - %s\n", stats.get("Sum").divide(stats.get("Count"), RoundingMode.HALF_UP));
            System.out.printf("Размер короткой строки - %s\n", stats.get("MinLen"));
            System.out.printf("Размер длинной строки  - %s\n", stats.get("MaxLen"));
        }
    }

    /**
     * This method cleans the file
     */
    private static void deleteText() {
        String[] files = out ? new String[]{outFileName} : FILE_NAMES;

        for (String i: files){
            if (new File(filePath + i).exists()) {
                Path path = Paths.get(i);

                try {
                    Files.writeString(path, "");
                } catch (IOException e) {
                    printErrorMessage("Ошибка при очистки файла: ", i);
                }
            }
        }
    }

    /**
     * Checks the contents of the command line.
     * First, the presence of flags and their compliance are checked.
     * It then checks to see if the file exists.
     */
    private static void isValidCommandLine() {
        List<String> validFlags = Arrays.asList("-a", "-f", "-s", "-o", "-p", "-out");

        String[] tokens = str.split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (token.startsWith("-")) {
                if (!validFlags.contains(token)) {
                    printErrorMessage("Введен некорректный флаг: ", token);
                } else if (token.equals("-o") || token.equals("-p")  && i + 1 < tokens.length) {
                    i = i + 1;
                    String path = tokens[i];

                    if (token.equals("-o") && !Files.exists(FileSystems.getDefault().getPath(path))) {
                        printErrorMessage("Введен несуществующий путь: ", token);
                    }
                }
            } else if (token.contains(".txt") && !new File(token).exists()) {
                printErrorMessage("Введен несуществующий файл: ", token);
            } else if (!token.contains(".txt")) {
                printErrorMessage("Введена некорректная команда или введен некорректный формат файла: ", token);
            }
        }

    }

    /**
     * This method prints an error message and terminates the program.
     *
     * @param token The element that caused the error
     * @param message Message to be displayed
     */
    private static void printErrorMessage(String message, String token) {
        System.err.println(message + token);

        Runtime.getRuntime().exit(0);
    }

    /**
     * This method checks if the command line is empty
     */
    private static void isEmptyString(String args) {
        if (args == null || args.trim().isEmpty()) {
            printErrorMessage("Введена пустая строка", " ");
        }
    }

    /**
     * This method checks that the string contains a floating point number
     */
    private static boolean isFloat (String s) {
        return s.matches("[-+]?\\d+\\.\\d+([eE][-+]?\\d+)?") ||
                s.matches("^[\\+\\-]{0,1}[0-9]+[\\.\\,][0-9]+$");
    }
}
