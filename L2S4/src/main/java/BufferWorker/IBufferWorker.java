package BufferWorker;

import java.io.IOException;

public interface IBufferWorker {
    /** Загружает страницу в буфер, возвращает индекс в буфере */
    int loadPage(int pageNumber) throws IOException;

    /** Выгружает страницу из буфера на диск */
    void unloadPage(int bufferIndex) throws IOException;

    /** Возвращает данные страницы по индексу в буфере */
    byte[] getPageData(int bufferIndex);

    /** Возвращает битовую карту страницы по индексу в буфере */
    byte[] getPageBitmap(int bufferIndex);

    /** Помечает страницу как измененную */
    void markPageDirty(int bufferIndex);

    /** Возвращает номер страницы в файле по индексу в буфере */
    int getPageNumber(int bufferIndex);

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