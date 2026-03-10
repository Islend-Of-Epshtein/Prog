package FileWorker;

public class FileHeader {
    public static final int HEADER_SIZE = 15;
    public static final byte[] SIGNATURE = {'V', 'M'};

    private byte[] signature = new byte[2];
    private long arraySize;
    private byte dataType; // 'I', 'C', 'V'
    private int stringLength;
    private int pageSize = 512;
    private int elementsPerPage;
    private int totalPages;

    public byte[] getSignature() { return signature; }
    public void setSignature(byte[] sig) { System.arraycopy(sig, 0, signature, 0, 2); }

    public long getArraySize() { return arraySize; }
    public void setArraySize(long size) { this.arraySize = size; }

    public byte getDataType() { return dataType; }
    public void setDataType(byte type) { this.dataType = type; }

    public int getStringLength() { return stringLength; }
    public void setStringLength(int len) { this.stringLength = len; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int size) { this.pageSize = size; }

    public int getElementsPerPage() { return elementsPerPage; }
    public void setElementsPerPage(int epp) { this.elementsPerPage = epp; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int pages) { this.totalPages = pages; }

}