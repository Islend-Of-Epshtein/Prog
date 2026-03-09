package BufferWorker;

import java.io.IOException;

public interface IBufferWorker {
    /** Загружает страницу в буфер, возвращает индекс в буфере */
    int loadPage(int pageNumber) throws IOException;

    /** Выгружает страницу из буфера на диск */
    void unloadPage(int bufferIndex) throws IOException;

    /** Возвращает данные страницы по индексу в буфере */
    byte[] getPageData(int bufferIndex);


    /** Записывает массив данных в страницу по указанной позиции */
    void writeToPage(int bufferIndex, int position, byte[] data);

    /** Записывает один байт в страницу по указанной позиции */
    void writeToPage(int bufferIndex, int position, byte value);

    /** Записывает байт в битовую карту страницы */
    void writeToBitmap(int bufferIndex, int bytePosition, byte value);

    /** Устанавливает или сбрасывает конкретный бит в битовой карте */
    void setBitInBitmap(int bufferIndex, int bitPosition, boolean value);

    /** Изменяет часть данных страницы */
    void modifyPageData(int bufferIndex, int offset, byte[] data, int dataOffset, int length);

    /** Очищает страницу (заполняет нулями) */
    void clearPage(int bufferIndex);
    /** Возвращает битовую карту страницы по индексу в буфере */
    byte[] getPageBitmap(int bufferIndex);

    /** Помечает страницу как измененную */
    void markPageDirty(int bufferIndex);

    /** Возвращает номер страницы в файле по индексу в буфере */
    int getPageNumber(int bufferIndex);

    int getFreeSlotIndex();

    /** Записывает на диск все измененные страницы */
    void flushAll() throws IOException;

    /** Явно выгружает страницу из буфера (для оператора) */
    void releasePage(int bufferIndex) throws IOException;

    /** Проверяет, загружена ли страница в буфер */
    boolean isPageLoaded(int pageNumber);

    /** Возвращает количество свободных слотов в буфере */
    int getFreeSlotCount();

    /** Проверяет, есть ли свободный слот */
    boolean hasFreeSlot();

    /** Возвращает размер буфера */
    int getBufferSize();

    /** Принудительно записывает страницу на диск */
    void forceFlushPage(int bufferIndex) throws IOException;

    /** Возвращает количество измененных страниц */
    int getDirtyPagesCount();

    /** Ищет индекс страницы в буфере по её номеру */
    int findPageInBuffer(int pageNumber);

    /** Возвращает строку с состоянием буфера (для вывода в View) */
    String getBufferStatus();

    /** Возвращает статистику работы буфера */
    String getBufferStats();
}