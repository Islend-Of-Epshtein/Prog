package FileWorker;

import java.io.*;

public class FileWorker implements IFileWorker {
    private RandomAccessFile file;
    private FileHeader header;
    private String filename;
    private boolean isOpen = false;

    private long readPageCount = 0;
    private long writePageCount = 0;
    private long readBitmapCount = 0;
    private long writeBitmapCount = 0;

    @Override
    public void initialize(String filename, long size, String dataType, int stringLength) throws IOException {
        this.filename = filename;
        this.header = new FileHeader();

        header.setSignature(FileHeader.SIGNATURE);
        header.setArraySize(size);
        header.setDataType((byte)dataType.charAt(0));
        header.setStringLength(stringLength);
        // pageSize уже 512 по умолчанию

        int elementSize = getElementSize(dataType, stringLength);
        header.setElementsPerPage(header.getPageSize() / elementSize);
        header.setTotalPages((int)((size + header.getElementsPerPage() - 1) / header.getElementsPerPage()));
        header.setFirstFreePage(0);

        file = new RandomAccessFile(filename, "rw");
        writeHeader();
        allocateFileSpace();
        isOpen = true;
    }

    @Override
    public void open(String filename) throws IOException {
        this.filename = filename;
        file = new RandomAccessFile(filename, "rw");
        readHeader();
        validateSignature();
        isOpen = true;
    }

    private void writeHeader() throws IOException {
        file.seek(0);
        file.write(header.getSignature());
        file.writeLong(header.getArraySize());
        file.writeByte(header.getDataType());
        file.writeInt(header.getStringLength());

    }

    private void readHeader() throws IOException {
        file.seek(0);
        header = new FileHeader();
        byte[] sig = new byte[2];
        file.read(sig);
        header.setSignature(sig);
        header.setArraySize(file.readLong());
        header.setDataType(file.readByte());
        header.setStringLength(file.readInt());
    }

    private void allocateFileSpace() throws IOException {
        int bitmapSize = getBitmapSize();

        for (int i = 0; i < header.getTotalPages(); i++) {
            byte[] bitmap = new byte[bitmapSize];
            file.write(bitmap);

            byte[] pageData = new byte[header.getPageSize()];
            file.write(pageData);
        }
    }

    @Override
    public long getPageOffset(int pageNumber) {
        return FileHeader.HEADER_SIZE +
                (long)pageNumber * (header.getPageSize() + getBitmapSize()) +
                getBitmapSize();
    }

    @Override
    public long getBitmapOffset(int pageNumber) {
        return FileHeader.HEADER_SIZE +
                (long)pageNumber * (header.getPageSize() + getBitmapSize());
    }

    @Override
    public int getBitmapSize() {
        return (header.getElementsPerPage() + 7) / 8;
    }

    @Override
    public byte[] readPage(int pageNumber) throws IOException {
        validatePageNumber(pageNumber);
        file.seek(getPageOffset(pageNumber));
        byte[] data = new byte[header.getPageSize()];
        file.read(data);
        readPageCount++;
        return data;
    }

    @Override
    public void writePage(int pageNumber, byte[] data) throws IOException {
        validatePageNumber(pageNumber);
        if (data.length != header.getPageSize()) {
            throw new IOException("Invalid page data size");
        }
        file.seek(getPageOffset(pageNumber));
        file.write(data);
        writePageCount++;
    }

    @Override
    public byte[] readBitmap(int pageNumber) throws IOException {
        validatePageNumber(pageNumber);
        file.seek(getBitmapOffset(pageNumber));
        byte[] bitmap = new byte[getBitmapSize()];
        file.read(bitmap);
        readBitmapCount++;
        return bitmap;
    }

    @Override
    public void writeBitmap(int pageNumber, byte[] bitmap) throws IOException {
        validatePageNumber(pageNumber);
        if (bitmap.length != getBitmapSize()) {
            throw new IOException("Invalid bitmap size");
        }
        file.seek(getBitmapOffset(pageNumber));
        file.write(bitmap);
        writeBitmapCount++;
    }

    @Override
    public FileHeader getHeader() {
        return header;
    }

    @Override
    public void close() throws IOException {
        if (file != null) {
            file.close();
            isOpen = false;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getFileStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== File Statistics ===\n");
        sb.append("Filename: ").append(filename).append("\n");
        sb.append("Pages read: ").append(readPageCount).append("\n");
        sb.append("Pages written: ").append(writePageCount).append("\n");
        sb.append("Bitmaps read: ").append(readBitmapCount).append("\n");
        sb.append("Bitmaps written: ").append(writeBitmapCount).append("\n");
        sb.append("Total operations: ").append(readPageCount + writePageCount + readBitmapCount + writeBitmapCount).append("\n");
        sb.append("======================\n");
        return sb.toString();
    }

    private void validateSignature() throws IOException {
        byte[] sig = header.getSignature();
        if (sig[0] != 'V' || sig[1] != 'M') {
            throw new IOException("Invalid file signature");
        }
    }

    private void validatePageNumber(int pageNumber) throws IOException {
        if (pageNumber < 0 || pageNumber >= header.getTotalPages()) {
            throw new IOException("Invalid page number: " + pageNumber);
        }
    }

    private int getElementSize(String dataType, int stringLength) {
        switch (dataType) {
            case "I": return 4;
            case "C": return stringLength;
            case "V": return 4;
            default: throw new IllegalArgumentException("Unknown data type: " + dataType);
        }
    }
}