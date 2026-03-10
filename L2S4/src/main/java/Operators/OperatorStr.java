package Operators;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class OperatorStr extends Operator {
    private final int maxLength;
    private final int elementsPerPage;
    private RandomAccessFile stringFile;
    private boolean stringFileOpen = false;

    // Конструктор для создания нового файла
    public OperatorStr(String filename, long size, String arrayType, int maxLength) {
        super(filename, size, arrayType, maxLength); // maxLength передаётся в родительский конструктор
        this.maxLength = maxLength;
        this.elementsPerPage = 512 / 4; // 128 указателей на страницу (int по 4 байта)

        try {
            // Создаём отдельный файл для строк
            String stringFilename = filename + ".str";
            stringFile = new RandomAccessFile(stringFilename, "rw");

            // Рассчитываем общий размер файла строк
            long totalStringSize = size * maxLength;

            // Выделяем место под все строки (заполняем нулями)
            byte[] emptyBlock = new byte[8192]; // Пишем блоками для скорости
            long written = 0;
            while (written < totalStringSize) {
                int toWrite = (int) Math.min(emptyBlock.length, totalStringSize - written);
                stringFile.write(emptyBlock, 0, toWrite);
                written += toWrite;
            }

            stringFileOpen = true;
            System.out.println("Создан файл строк: " + stringFilename +
                    " размером " + totalStringSize + " байт");

        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания файла строк: " + e.getMessage());
        }
    }

    // Конструктор для открытия существующего файла
    public OperatorStr(String filename) {
        super(filename);

        // Получаем maxLength из заголовка основного файла
        this.maxLength = getFile().getHeader().getStringLength();
        this.elementsPerPage = 512 / 4;

        try {
            // Открываем существующий файл строк
            String stringFilename = filename + ".str";
            stringFile = new RandomAccessFile(stringFilename, "rw");
            stringFileOpen = true;
            System.out.println("Открыт файл строк: " + stringFilename);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка открытия файла строк: " + e.getMessage());
        }
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

            // 2. Вычисляем позицию в файле строк
            long position = index * maxLength;

            // 3. Конвертируем строку в байты (фиксированная длина)
            byte[] stringBytes = new byte[maxLength];
            byte[] valueBytes = value.getBytes("UTF-8");

            // Копируем байты строки
            int copyLength = Math.min(valueBytes.length, maxLength);
            System.arraycopy(valueBytes, 0, stringBytes, 0, copyLength);

            // Остальные байты остаются нулями

            // 4. Записываем в файл строк
            stringFile.seek(position);
            stringFile.write(stringBytes);

            // 5. Записываем указатель в основной файл
            int pageIndex = (int)(index / elementsPerPage);
            int posInPage = (int)(index % elementsPerPage);

            int bufferIndex = getBuffer().loadPage(pageIndex);

            // Указатель - это смещение в файле строк
            byte[] pointer = ByteBuffer.allocate(4).putInt((int)position).array();

            // Записываем указатель (int) в нужное место страницы
            getBuffer().writeToPage(bufferIndex, posInPage * 4, pointer);

            // Помечаем страницу как изменённую
            getBuffer().markPageDirty(bufferIndex);

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

            // 3. Читаем строку из файла строк
            stringFile.seek(position);
            byte[] stringBytes = new byte[maxLength];
            stringFile.read(stringBytes);

            // 4. Находим конец строки (первый нулевой байт)
            int realLength = 0;
            while (realLength < maxLength && stringBytes[realLength] != 0) {
                realLength++;
            }

            // 5. Конвертируем в строку
            if (realLength == 0) {
                return "";
            }
            return new String(stringBytes, 0, realLength, "UTF-8");

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения строки: " + e.getMessage());
        }
    }

    /**
     * Закрытие файла строк
     */
    public void close() {
        try {
            if (stringFile != null) {
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