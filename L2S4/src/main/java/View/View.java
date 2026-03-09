package View;

import Operators.OperatorInt;
import Operators.OperatorStr;
import Operators.OperatorStrFix;

import java.io.IOException;
import java.util.Scanner;

/**
 * Тестирующая программа (консольное приложение)
 * Реализует интерфейс командной строки согласно заданию
 */
public class View {

    private static OperatorInt intOperator;
    private static OperatorStrFix strFixOperator;
    private static OperatorStr strOperator;
    private static String currentFile;
    private static String currentType;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Система управления виртуальной памятью ===");
        System.out.println("Введите 'Help' для списка команд");

        while (true) {
            System.out.print("VM> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toUpperCase();
            String params = parts.length > 1 ? parts[1] : "";

            try {
                switch (command) {
                    case "EXIT":
                        exit();
                        return;
                    case "HELP":
                        help(params);
                        break;
                    case "CREATE":
                        create(params);
                        break;
                    case "OPEN":
                        open(params);
                        break;
                    case "INPUT":
                        input(params);
                        break;
                    case "PRINT":
                        print(params);
                        break;
                    case "STATUS":
                        printStatus();
                        break;
                    case "STATS":
                        printStats();
                        break;
                    default:
                        System.out.println("Неизвестная команда. Введите 'Help' для списка команд.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void help(String filename) {
        String helpText =
                "=== Список команд ===\n" +
                        "Create имя_файла(int|char(длина)|varchar(макс.длина)) - создает файл виртуального массива\n" +
                        "Open имя_файла - открывает существующий файл\n" +
                        "Input(индекс, значение) - записывает значение в элемент массива\n" +
                        "Print(индекс) - выводит значение элемента массива\n" +
                        "Status - выводит состояние буфера\n" +
                        "Stats - выводит статистику работы\n" +
                        "Help [имя_файла] - выводит справку\n" +
                        "Exit - завершает программу\n";

        if (filename.isEmpty()) {
            System.out.println(helpText);
        } else {
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Paths.get(filename),
                        helpText,
                        java.nio.charset.StandardCharsets.UTF_8
                );
                System.out.println("Справка сохранена в файл: " + filename);
            } catch (IOException e) {
                System.out.println("Ошибка сохранения справки: " + e.getMessage());
            }
        }
    }

    private static void create(String params) {
        // Формат: имя_файла(int|char(длина)|varchar(макс.длина))
        int openBracket = params.indexOf('(');
        int closeBracket = params.indexOf(')');

        if (openBracket == -1 || closeBracket == -1) {
            System.out.println("Неверный формат команды. Используйте: Create имя(int|char(длина)|varchar(макс.длина))");
            return;
        }

        String filename = params.substring(0, openBracket).trim();
        String typeInfo = params.substring(openBracket + 1, closeBracket);

        // Определяем тип и размерность (по умолчанию 10000 элементов)
        long size = 10000;

        if (typeInfo.startsWith("int")) {
            currentType = "I";
            System.out.println("Создание массива целых чисел...");
            intOperator = new OperatorInt(filename, size, "I");

        } else if (typeInfo.startsWith("char")) {
            currentType = "C";
            // Извлекаем длину строки
            int lenStart = typeInfo.indexOf('(');
            int lenEnd = typeInfo.lastIndexOf(')');
            if (lenStart != -1 && lenEnd != -1) {
                int strLength = Integer.parseInt(typeInfo.substring(lenStart + 1, lenEnd).trim());
                System.out.println("Создание массива строк фиксированной длины (" + strLength + ")...");
                strFixOperator = new OperatorStrFix(filename, size, "C", strLength);
            } else {
                System.out.println("Не указана длина строки для char");
                return;
            }

        } else if (typeInfo.startsWith("varchar")) {
            currentType = "V";
            // Извлекаем максимальную длину
            int lenStart = typeInfo.indexOf('(');
            int lenEnd = typeInfo.lastIndexOf(')');
            if (lenStart != -1 && lenEnd != -1) {
                int maxLength = Integer.parseInt(typeInfo.substring(lenStart + 1, lenEnd).trim());
                System.out.println("Создание массива строк переменной длины (макс. " + maxLength + ")...");
                strOperator = new OperatorStr(filename, size, "V", maxLength);
            } else {
                System.out.println("Не указана максимальная длина строки для varchar");
                return;
            }
        } else {
            System.out.println("Неизвестный тип данных. Используйте int, char(длина) или varchar(макс.длина)");
            return;
        }

        currentFile = filename;
        System.out.println("Файл успешно создан: " + filename);
    }

    private static void open(String filename) {
        // В реальной реализации нужно определить тип из заголовка файла
        // Для демо просто сообщаем, что файл открыт
        System.out.println("Открытие файла: " + filename);

        // Здесь должна быть логика определения типа файла
        // Пока просто сообщаем, что нужно создать новый массив
        System.out.println("Для работы с файлом используйте команду Create");
    }

    private static void input(String params) {
        if (intOperator == null && strFixOperator == null && strOperator == null) {
            System.out.println("Сначала создайте или откройте файл командой Create");
            return;
        }

        // Формат: (индекс, значение)
        params = params.trim();
        if (!params.startsWith("(") || !params.endsWith(")")) {
            System.out.println("Неверный формат. Используйте: Input(индекс, значение)");
            return;
        }

        String content = params.substring(1, params.length() - 1);
        String[] parts = content.split(",", 2);

        if (parts.length != 2) {
            System.out.println("Неверный формат. Используйте: Input(индекс, значение)");
            return;
        }

        try {
            long index = Long.parseLong(parts[0].trim());
            String value = parts[1].trim();

            // Убираем кавычки, если есть
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            if (currentType.equals("I")) {
                int intValue = Integer.parseInt(value);
                intOperator.input((int) index, intValue);
                System.out.println("Значение " + intValue + " записано в индекс " + index);
            } else if (currentType.equals("C")) {
                strFixOperator.input(index, value);
                System.out.println("Строка \"" + value + "\" записана в индекс " + index);
            } else if (currentType.equals("V")) {
                strOperator.input(index, value);
                System.out.println("Строка \"" + value + "\" записана в индекс " + index);
            }

        } catch (NumberFormatException e) {
            System.out.println("Неверный формат числа: " + e.getMessage());
        }
    }

    private static void print(String params) {
        if (intOperator == null && strFixOperator == null && strOperator == null) {
            System.out.println("Сначала создайте или откройте файл командой Create");
            return;
        }

        // Формат: (индекс)
        params = params.trim();
        if (!params.startsWith("(") || !params.endsWith(")")) {
            System.out.println("Неверный формат. Используйте: Print(индекс)");
            return;
        }

        String content = params.substring(1, params.length() - 1).trim();

        try {
            long index = Long.parseLong(content);

            if (currentType.equals("I")) {
                int value = intOperator.getValueByIndex((int) index);
                System.out.println("[" + index + "] = " + value);
            } else if (currentType.equals("C")) {
                String value = strFixOperator.getValueByIndex(index);
                System.out.println("[" + index + "] = \"" + value + "\"");
            } else if (currentType.equals("V")) {
                String value = strOperator.getValueByIndex(index);
                System.out.println("[" + index + "] = \"" + value + "\"");
            }

        } catch (NumberFormatException e) {
            System.out.println("Неверный формат индекса: " + e.getMessage());
        }
    }

    private static void printStatus() {
        if (intOperator != null) {
            System.out.println(intOperator.getBuffer().getBufferStatus());
        } else if (strFixOperator != null) {
            System.out.println(strFixOperator.getBuffer().getBufferStatus());
        } else if (strOperator != null) {
            System.out.println(strOperator.getBuffer().getBufferStatus());
        } else {
            System.out.println("Нет открытых файлов");
        }
    }

    private static void printStats() {
        if (intOperator != null) {
            System.out.println(intOperator.getBuffer().getBufferStats());
            System.out.println(intOperator.getFile().getFileStats());
        } else if (strFixOperator != null) {
            System.out.println(strFixOperator.getBuffer().getBufferStats());
            System.out.println(strFixOperator.getFile().getFileStats());
        } else if (strOperator != null) {
            System.out.println(strOperator.getBuffer().getBufferStats());
            System.out.println(strOperator.getFile().getFileStats());
        } else {
            System.out.println("Нет открытых файлов");
        }
    }

    private static void exit() {
        System.out.println("Завершение работы...");

        try {
            if (intOperator != null) {
                intOperator.getBuffer().flushAll();
                intOperator.getFile().close();
            }
            if (strFixOperator != null) {
                strFixOperator.getBuffer().flushAll();
                strFixOperator.getFile().close();
            }
            if (strOperator != null) {
                strOperator.getBuffer().flushAll();
                strOperator.getFile().close();
                strOperator.close();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при закрытии файлов: " + e.getMessage());
        }

        System.out.println("Программа завершена.");
    }
}