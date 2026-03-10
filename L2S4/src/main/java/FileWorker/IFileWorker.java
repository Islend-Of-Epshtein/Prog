package FileWorker;

import java.io.IOException;

public interface IFileWorker {
    /** Создает новый файл с заданными параметрами */
    void initialize(String filename, long size, String dataType, int stringLength) throws IOException;

    /** Открывает существующий файл */
    void open(String filename) throws IOException;

    int getElementSize(String dataType, int stringLength);
    /** Читает страницу данных из файла */
    byte[] readPage(int pageNumber) throws IOException;

    /** Записывает страницу данных в файл */
    void writePage(int pageNumber, byte[] data) throws IOException;

    /** Читает битовую карту страницы из файла */
    byte[] readBitmap(int pageNumber) throws IOException;

    /** Записывает битовую карту страницы в файл */
    void writeBitmap(int pageNumber, byte[] bitmap) throws IOException;

    /** Возвращает заголовок файла */
    FileHeader getHeader();

    /** Возвращает смещение страницы в файле */
    long getPageOffset(int pageNumber);

    /** Возвращает смещение битовой карты в файле */
    long getBitmapOffset(int pageNumber);

    /** Возвращает размер битовой карты в байтах */
    int getBitmapSize();

    /** Закрывает файл */
    void close() throws IOException;

    /** Проверяет, открыт ли файл */
    boolean isOpen();

    /** Возвращает имя файла */
    String getFilename();

    /** Возвращает статистику операций с файлом */
    String getFileStats();
}