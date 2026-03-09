package Operators;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Класс для работы с массивом строк переменной длины (тип 'V')
 * Используется два файла: основной файл страниц и файл для хранения строк
 */
public class OperatorStr extends Operator {
    private final int pageSize = 512;
    private final int elementsPerPage = 128; // На странице 128 элементов (адресов)
    private final int maxStringLength;
    private final long arraySize;
    private final String stringFileName;

    // Файл для хранения строк
    private FileWorker.IFileWorker stringFile;

    public OperatorStr(String filename, long size, String arrayType, int maxStringLength) {
        super(filename, size, arrayType);
        this.maxStringLength = maxStringLength;
        this.arraySize = size;
        this.stringFileName = filename + ".str";

        // Создаем или открываем файл для хранения строк
        initStringFile();

        // Инициализируем основной массив (все адреса = -1)
        initializeArray();

        // Загружаем первые 5 страниц в буфер
        try {
            int totalPages = getFile().getHeader().getTotalPages();
            for (int i = 0; i < Math.min(5, totalPages); i++) {
                getBuffer().loadPage(i);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка заполнения буфера: " + e.getMessage());
        }
    }

    private void initStringFile() {
        stringFile = new FileWorker.FileWorker();
        try {
            // Пытаемся открыть существующий файл
            stringFile.open(stringFileName);
        } catch (IOException e) {
            // Если файла нет, создаем новый
            try {
                // Для файла строк используем упрощенную структуру:
                // Заголовок 256 байт, далее записи переменной длины
                // Каждой записи предшествуют 4 байта длины
                stringFile.initialize(stringFileName, 0, "C", 0);
            } catch (IOException ex) {
                throw new RuntimeException("Ошибка создания файла строк: " + ex.getMessage());
            }
        }
    }

    private void initializeArray() {
        int totalPages = getFile().getHeader().getTotalPages();
        byte[] emptyPage = new byte[pageSize];
        Arrays.fill(emptyPage, (byte) 0xFF); // Заполняем -1 (все адреса пусты)

        try {
            for (int page = 0; page < totalPages; page++) {
                getFile().writePage(page, emptyPage);

                // Инициализируем битовую карту
                byte[] bitmap = new byte[getFile().getBitmapSize()];
                Arrays.fill(bitmap, (byte) 0);
                getFile().writeBitmap(page, bitmap);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка инициализации массива: " + e.getMessage());
        }
    }

    /**
     * Определяет номер страницы по индексу элемента
     */
    public int getPageByIndex(long index) {
        return (int) (index / elementsPerPage);
    }

    /**
     * Определяет смещение элемента на странице (в байтах)
     */
    public int getOffsetInPage(long index) {
        return (int) ((index % elementsPerPage) * 4); // 4 байта на адрес
    }

    /**
     * Чтение значения строки по индексу
     */
    public String getValueByIndex(long index) {
        if (index < 0 || index >= arraySize) {
            throw new IndexOutOfBoundsException("Индекс вне границ массива: " + index);
        }

        int pageNum = getPageByIndex(index);
        int offset = getOffsetInPage(index);

        try {
            // Загружаем страницу в буфер
            int bufferIndex = getBuffer().loadPage(pageNum);

            // Проверяем битовую карту
            byte[] bitmap = getBuffer().getPageBitmap(bufferIndex);
            int bitPosition = (int) (index % elementsPerPage);
            int bytePos = bitPosition / 8;
            int bitInByte = bitPosition % 8;

            boolean isWritten = ((bitmap[bytePos] >> bitInByte) & 1) == 1;

            if (!isWritten) {
                return ""; // Пустая строка
            }

            // Читаем адрес строки из страницы
            byte[] pageData = getBuffer().getPageData(bufferIndex);
            byte[] addressBytes = new byte[4];
            System.arraycopy(pageData, offset, addressBytes, 0, 4);
            long stringAddress = ByteBuffer.wrap(addressBytes).getInt() & 0xFFFFFFFFL;

            if (stringAddress == 0xFFFFFFFFL) {
                return "";
            }

            // Читаем строку из файла строк
            return readStringFromFile(stringAddress);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения: " + e.getMessage());
        }
    }

    /**
     * Запись значения строки по индексу
     */
    public void input(long index, String value) {
        if (index < 0 || index >= arraySize) {
            throw new IndexOutOfBoundsException("Индекс вне границ массива: " + index);
        }

        if (value.length() > maxStringLength) {
            throw new IllegalArgumentException("Строка слишком длинная. Максимальная длина: " + maxStringLength);
        }

        int pageNum = getPageByIndex(index);
        int offset = getOffsetInPage(index);

        try {
            // Загружаем страницу в буфер
            int bufferIndex = getBuffer().loadPage(pageNum);

            // Сохраняем строку в файл строк и получаем адрес
            long stringAddress;

            if (value.isEmpty()) {
                stringAddress = 0xFFFFFFFFL;
            } else {
                stringAddress = writeStringToFile(value);
            }

            // Записываем адрес в страницу
            byte[] addressBytes = ByteBuffer.allocate(4).putInt((int) stringAddress).array();
            getBuffer().modifyPageData(bufferIndex, offset, addressBytes, 0, 4);

            // Устанавливаем бит в битовой карте
            int bitPosition = (int) (index % elementsPerPage);
            getBuffer().setBitInBitmap(bufferIndex, bitPosition, true);

            getBuffer().markPageDirty(bufferIndex);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи: " + e.getMessage());
        }
    }

    /**
     * Запись строки в файл строк
     * Возвращает адрес (смещение) записи
     */
    private long writeStringToFile(String value) throws IOException {
        byte[] strBytes = value.getBytes(StandardCharsets.UTF_8);

        // Длина строки (4 байта) + сами данные
        ByteBuffer buffer = ByteBuffer.allocate(4 + strBytes.length);
        buffer.putInt(strBytes.length);
        buffer.put(strBytes);

        // Определяем место для записи (в конец файла)
        long fileSize = stringFile.getHeader() != null ?
                stringFile.getHeader().getArraySize() : 0;
        long position = FileWorker.FileHeader.HEADER_SIZE + fileSize;

        // Записываем
        java.io.RandomAccessFile raf = new java.io.RandomAccessFile(stringFileName, "rw");
        raf.seek(position);
        raf.write(buffer.array());
        raf.close();

        return position - FileWorker.FileHeader.HEADER_SIZE;
    }

    /**
     * Чтение строки из файла строк по адресу
     */
    private String readStringFromFile(long address) throws IOException {
        java.io.RandomAccessFile raf = new java.io.RandomAccessFile(stringFileName, "r");
        raf.seek(FileWorker.FileHeader.HEADER_SIZE + address);

        // Читаем длину строки
        byte[] lenBytes = new byte[4];
        raf.read(lenBytes);
        int length = ByteBuffer.wrap(lenBytes).getInt();

        if (length <= 0 || length > maxStringLength) {
            raf.close();
            return "";
        }

        // Читаем строку
        byte[] strBytes = new byte[length];
        raf.read(strBytes);
        raf.close();

        return new String(strBytes, StandardCharsets.UTF_8);
    }

    /**
     * Удаление значения
     */
    public void delete(long index) {
        if (index < 0 || index >= arraySize) {
            throw new IndexOutOfBoundsException("Индекс вне границ массива: " + index);
        }

        int pageNum = getPageByIndex(index);

        try {
            int bufferIndex = getBuffer().loadPage(pageNum);

            // Сбрасываем бит
            int bitPosition = (int) (index % elementsPerPage);
            getBuffer().setBitInBitmap(bufferIndex, bitPosition, false);

            // Очищаем адрес
            int offset = getOffsetInPage(index);
            byte[] emptyAddress = ByteBuffer.allocate(4).putInt(-1).array();
            getBuffer().modifyPageData(bufferIndex, offset, emptyAddress, 0, 4);

            getBuffer().markPageDirty(bufferIndex);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка удаления: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        getBuffer().flushAll();
        getFile().close();
        if (stringFile != null) {
            stringFile.close();
        }
    }
}