package Operators;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OperatorStr extends Operator {
    private final int maxLength;
    private final int elementsPerPage;
    private RandomAccessFile stringFile;
    private boolean stringFileOpen = false;

    // Сигнатура для файла строк (как в методичке)
    private static final byte[] STRING_FILE_SIGNATURE = {'V', 'M'};
    private static final int STRING_FILE_HEADER_SIZE = 2; // только сигнатура

    // Конструктор для создания нового файла
    public OperatorStr(String filename, long size, String arrayType, int maxLength) {
        super(filename, size, arrayType, maxLength);
        this.maxLength = maxLength;
        this.elementsPerPage = 512 / 4; // 128 указателей на страницу

        try {
            // Создаём отдельный файл для строк
            String stringFilename = filename + ".str";
            stringFile = new RandomAccessFile(stringFilename, "rw");

            // 1. Записываем сигнатуру
            stringFile.write(STRING_FILE_SIGNATURE);

            // Файл пока пустой, данные будут добавляться по мере записи
            stringFileOpen = true;
            System.out.println("Создан файл строк: " + stringFilename);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания файла строк: " + e.getMessage());
        }
    }

    // Конструктор для открытия существующего файла
    public OperatorStr(String filename) {
        super(filename);

        this.maxLength = getFile().getHeader().getStringLength();
        this.elementsPerPage = 512 / 4;

        try {
            String stringFilename = filename + ".str";
            stringFile = new RandomAccessFile(stringFilename, "rw");

            // Проверяем сигнатуру
            byte[] signature = new byte[2];
            stringFile.read(signature);

            if (signature[0] != STRING_FILE_SIGNATURE[0] ||
                    signature[1] != STRING_FILE_SIGNATURE[1]) {
                throw new RuntimeException("Неверная сигнатура файла строк");
            }

            stringFileOpen = true;
            System.out.println("Открыт файл строк: " + stringFilename);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка открытия файла строк: " + e.getMessage());
        }
    }

    /**
     * Получить позицию для записи строки в файл строк
     * Строки хранятся последовательно: [длина][данные строки]
     */
    private long getStringPositionInFile(long stringIndex) throws IOException {
        // Проходим по всем строкам до нужного индекса
        stringFile.seek(STRING_FILE_HEADER_SIZE); // Пропускаем сигнатуру
        long position = STRING_FILE_HEADER_SIZE;

        for (long i = 0; i < stringIndex; i++) {
            // Читаем длину строки (4 байта)
            if (stringFile.getFilePointer() >= stringFile.length()) {
                return -1; // Строка ещё не записана
            }

            byte[] lengthBytes = new byte[4];
            stringFile.read(lengthBytes);
            int length = ByteBuffer.wrap(lengthBytes).getInt();

            // Пропускаем данные строки
            stringFile.skipBytes(length);
            position += 4 + length; // длина + данные
        }

        return position;
    }

    /**
     * Запись строки в файл
     */
    public void input(long index, String value) {
        if (!stringFileOpen) {
            throw new RuntimeException("Файл строк не открыт");
        }

        try {
            // 1. Обрезаем строку, если она длиннее maxLength
            if (value.length() > maxLength) {
                value = value.substring(0, maxLength);
            }

            // 2. Конвертируем строку в байты
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

            // 3. Определяем позицию для записи в файл строк
            long stringPosition = getStringPositionInFile(index);

            // Если строка уже была записана, перезаписываем её
            if (stringPosition != -1) {
                stringFile.seek(stringPosition);
            } else {
                // Иначе добавляем в конец файла
                stringFile.seek(stringFile.length());
            }

            // 4. Записываем длину строки (4 байта)
            byte[] lengthBytes = ByteBuffer.allocate(4).putInt(valueBytes.length).array();
            stringFile.write(lengthBytes);

            // 5. Записываем данные строки
            stringFile.write(valueBytes);

            // 6. Получаем позицию начала этой записи (для указателя)
            long recordStartPosition = stringFile.getFilePointer() - 4 - valueBytes.length;

            // 7. Записываем указатель в основной файл
            int pageIndex = (int)(index / elementsPerPage);
            int posInPage = (int)(index % elementsPerPage);

            int bufferIndex = getBuffer().loadPage(pageIndex);

            // Указатель - это смещение в файле строк (относительно начала файла)
            byte[] pointer = ByteBuffer.allocate(4).putInt((int)recordStartPosition).array();

            // Записываем указатель в нужное место страницы
            getBuffer().writeToPage(bufferIndex, posInPage * 4, pointer);
            getBuffer().markPageDirty(bufferIndex);

            System.out.println("Строка записана: индекс=" + index +
                    ", позиция в файле строк=" + recordStartPosition +
                    ", длина=" + valueBytes.length);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи строки: " + e.getMessage());
        }
    }

    /**
     * Чтение строки из файла
     */
    public String getValueByIndex(long index) {
        if (!stringFileOpen) {
            throw new RuntimeException("Файл строк не открыт");
        }

        try {
            // 1. Читаем указатель из основного файла
            int pageIndex = (int)(index / elementsPerPage);
            int posInPage = (int)(index % elementsPerPage);

            int bufferIndex = getBuffer().loadPage(pageIndex);
            byte[] pageData = getBuffer().getPageData(bufferIndex);

            // Извлекаем указатель (4 байта)
            byte[] pointerBytes = new byte[4];
            System.arraycopy(pageData, posInPage * 4, pointerBytes, 0, 4);
            int position = ByteBuffer.wrap(pointerBytes).getInt();

            // 2. Если указатель = 0, значит строка не записана
            if (position == 0) {
                return "";
            }

            // 3. Читаем из файла строк по указателю
            stringFile.seek(position);

            // Читаем длину строки (4 байта)
            byte[] lengthBytes = new byte[4];
            stringFile.read(lengthBytes);
            int strLength = ByteBuffer.wrap(lengthBytes).getInt();

            // Проверяем на валидность длины
            if (strLength <= 0 || strLength > maxLength * 4) { // *4 для UTF-8
                System.err.println("Предупреждение: некорректная длина строки: " + strLength);
                return "[ошибка данных]";
            }

            // Читаем данные строки
            byte[] stringBytes = new byte[strLength];
            stringFile.read(stringBytes);

            // Конвертируем в строку
            return new String(stringBytes, 0, strLength, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения строки: " + e.getMessage());
        }
    }

    /**
     * Получить информацию о файле строк
     */
    public String getStringFileInfo() throws IOException {
        if (stringFile == null) return "Файл строк не открыт";

        StringBuilder sb = new StringBuilder();
        sb.append("=== Информация о файле строк ===\n");
        sb.append("Размер файла: ").append(stringFile.length()).append(" байт\n");
        sb.append("Сигнатура: присутствует\n");

        // Подсчитываем количество записанных строк
        long pos = STRING_FILE_HEADER_SIZE;
        long count = 0;
        stringFile.seek(pos);

        while (pos < stringFile.length()) {
            byte[] lenBytes = new byte[4];
            if (stringFile.read(lenBytes) != 4) break;
            int len = ByteBuffer.wrap(lenBytes).getInt();
            pos = stringFile.getFilePointer() + len;
            stringFile.seek(pos);
            count++;
        }

        sb.append("Записано строк: ").append(count).append("\n");
        sb.append("==============================\n");
        return sb.toString();
    }

    /**
     * Закрытие файла строк
     */
    public void close() {
        try {
            if (stringFile != null) {
                // Перед закрытием сбрасываем все буферы
                getBuffer().flushAll();

                stringFile.close();
                stringFileOpen = false;
                System.out.println("Файл строк закрыт");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии файла строк: " + e.getMessage());
        }
    }

    /**
     * Получить размер файла строк
     */
    public long getStringFileSize() throws IOException {
        if (stringFile != null) {
            return stringFile.length();
        }
        return 0;
    }
}