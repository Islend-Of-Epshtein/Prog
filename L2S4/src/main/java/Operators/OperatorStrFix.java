package Operators;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Класс для работы с массивом строк фиксированной длины (тип 'C')
 * Строки имеют фиксированную длину, заданную при создании
 */
public class OperatorStrFix extends Operator {
    private final int pageSize = 512;
    private final int stringLength;
    private final int elementsPerPage;
    private final long arraySize;

    public OperatorStrFix(String filename){
        super(filename);
        this.stringLength = getFile().getHeader().getStringLength();
        this.arraySize = getFile().getHeader().getArraySize();
        this.elementsPerPage = pageSize/stringLength;
    }

    public OperatorStrFix(String filename, long size, String arrayType, int stringLength) {
        super(filename, size, arrayType, stringLength);
        this.stringLength = stringLength;
        this.arraySize = size;
        this.elementsPerPage = pageSize / stringLength;

        // Заполняем начальный массив пустыми строками
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

    private void initializeArray() {
        int totalPages = getFile().getHeader().getTotalPages();
        byte[] emptyPage = new byte[pageSize];
        Arrays.fill(emptyPage, (byte) 0);

        try {
            for (int page = 0; page < totalPages; page++) {
                getFile().writePage(page, emptyPage);

                // Инициализируем битовую карту (все биты = 0 - ничего не записано)
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
     * Определяет смещение элемента на странице
     */
    public int getOffsetInPage(long index) {
        return (int) ((index % elementsPerPage) * stringLength);
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

            // Проверяем битовую карту - записано ли значение
            byte[] bitmap = getBuffer().getPageBitmap(bufferIndex);
            int bitPosition = (int) (index % elementsPerPage);
            int bytePos = bitPosition / 8;
            int bitInByte = bitPosition % 8;

            boolean isWritten = ((bitmap[bytePos] >> bitInByte) & 1) == 1;

            if (!isWritten) {
                return ""; // Пустая строка
            }

            // Читаем данные со страницы
            byte[] pageData = getBuffer().getPageData(bufferIndex);
            byte[] strBytes = new byte[stringLength];
            System.arraycopy(pageData, offset, strBytes, 0, stringLength);

            // Убираем завершающие нули
            int realLength = 0;
            while (realLength < stringLength && strBytes[realLength] != 0) {
                realLength++;
            }

            return new String(strBytes, 0, realLength, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: " + e.getMessage());
        }
    }

    /**
     * Запись значения строки по индексу
     */
    public void input(long index, String value) {
        if (index < 0 || index >= arraySize) {
            throw new IndexOutOfBoundsException("Индекс вне границ массива: " + index);
        }

        if (value.length() > stringLength) {
            throw new IllegalArgumentException("Строка слишком длинная. Максимальная длина: " + stringLength);
        }

        int pageNum = getPageByIndex(index);
        int offset = getOffsetInPage(index);

        try {
            // Загружаем страницу в буфер
            int bufferIndex = getBuffer().loadPage(pageNum);

            // Преобразуем строку в байты фиксированной длины
            byte[] strBytes = new byte[stringLength];
            Arrays.fill(strBytes, (byte) 0);
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(valueBytes, 0, strBytes, 0, valueBytes.length);

            // Записываем данные на страницу
            getBuffer().modifyPageData(bufferIndex, offset, strBytes, 0, stringLength);

            // Устанавливаем бит в битовой карте
            int bitPosition = (int) (index % elementsPerPage);
            getBuffer().setBitInBitmap(bufferIndex, bitPosition, true);

            // Помечаем страницу как измененную
            getBuffer().markPageDirty(bufferIndex);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи в файл: " + e.getMessage());
        }
    }

    /**
     * Удаление значения (сброс бита)
     */
    public void delete(long index) {
        if (index < 0 || index >= arraySize) {
            throw new IndexOutOfBoundsException("Индекс вне границ массива: " + index);
        }

        int pageNum = getPageByIndex(index);

        try {
            int bufferIndex = getBuffer().loadPage(pageNum);

            // Сбрасываем бит в битовой карте
            int bitPosition = (int) (index % elementsPerPage);
            getBuffer().setBitInBitmap(bufferIndex, bitPosition, false);

            // Очищаем данные (опционально)
            int offset = getOffsetInPage(index);
            byte[] empty = new byte[stringLength];
            getBuffer().modifyPageData(bufferIndex, offset, empty, 0, stringLength);

            getBuffer().markPageDirty(bufferIndex);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка удаления: " + e.getMessage());
        }
    }
}