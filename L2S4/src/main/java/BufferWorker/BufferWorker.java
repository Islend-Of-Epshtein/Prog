package BufferWorker;

import FileWorker.IFileWorker;
import java.io.IOException;
import java.util.Arrays;

public class BufferWorker implements IBufferWorker {
    private final IFileWorker fileWorker;
    private final PageBufferEntry[] buffers;
    private final int bufferSize;
    private int nextVictimIndex = 0;
    private int pageHits = 0;
    private int pageMisses = 0;
    private int pagesLoaded = 0;
    private int pagesUnloaded = 0;
    private int pagesFlushed = 0;

    private static class PageBufferEntry {
        int pageNumber = -1;
        byte[] data;
        byte[] bitmap;
        boolean dirty = false;
        long lastAccessTime;
        long loadTime;

        PageBufferEntry(int pageSize, int bitmapSize) {
            data = new byte[pageSize];
            bitmap = new byte[bitmapSize];
            lastAccessTime = System.nanoTime();
            loadTime = System.nanoTime();
        }

        void reset() {
            pageNumber = -1;
            dirty = false;
            Arrays.fill(data, (byte)0);
            Arrays.fill(bitmap, (byte)0);
        }
    }

    /** Конструктор с размером буфера (минимум 3, рекомендуется 5) */
    public BufferWorker(IFileWorker fileWorker, int bufferSize) {
        this.fileWorker = fileWorker;
        this.bufferSize = Math.max(bufferSize, 3);
        this.buffers = new PageBufferEntry[this.bufferSize];

        int pageSize = fileWorker.getHeader().getPageSize();
        int bitmapSize = fileWorker.getBitmapSize();
        for (int i = 0; i < this.bufferSize; i++) {
            buffers[i] = new PageBufferEntry(pageSize, bitmapSize);
        }
    }

    /** Конструктор по умолчанию с 5 страницами */
    public BufferWorker(IFileWorker fileWorker) {
        this(fileWorker, 5);
    }
    // Добавьте эти методы в класс BufferWorker

    @Override
    public void writeToPage(int bufferIndex, int position, byte[] data) {
        validateBufferIndex(bufferIndex);
        PageBufferEntry entry = buffers[bufferIndex];

        // Копируем данные в страницу
        System.arraycopy(data, 0, entry.data, position, data.length);

        // Помечаем страницу как измененную
        entry.dirty = true;
        entry.lastAccessTime = System.nanoTime();
    }
    @Override
    public void writeToPage(int bufferIndex, int position, byte value) {
        validateBufferIndex(bufferIndex);
        PageBufferEntry entry = buffers[bufferIndex];

        // Записываем один байт
        entry.data[position] = value;

        // Помечаем страницу как измененную
        entry.dirty = true;
        entry.lastAccessTime = System.nanoTime();
    }

    @Override
    public void writeToBitmap(int bufferIndex, int bytePosition, byte value) {
        validateBufferIndex(bufferIndex);
        PageBufferEntry entry = buffers[bufferIndex];

        // Записываем байт в битовую карту
        entry.bitmap[bytePosition] = value;

        // Помечаем страницу как измененную
        entry.dirty = true;
        entry.lastAccessTime = System.nanoTime();
    }

    @Override
    public void setBitInBitmap(int bufferIndex, int bitPosition, boolean value) {
        validateBufferIndex(bufferIndex);
        PageBufferEntry entry = buffers[bufferIndex];

        int bytePos = bitPosition / 8;
        int bitInByte = bitPosition % 8;

        if (value) {
            // Устанавливаем бит в 1
            entry.bitmap[bytePos] |= (1 << bitInByte);
        } else {
            // Устанавливаем бит в 0
            entry.bitmap[bytePos] &= ~(1 << bitInByte);
        }

        // Помечаем страницу как измененную
        entry.dirty = true;
        entry.lastAccessTime = System.nanoTime();
    }

    @Override
    public void modifyPageData(int bufferIndex, int offset, byte[] data, int dataOffset, int length) {
        validateBufferIndex(bufferIndex);
        PageBufferEntry entry = buffers[bufferIndex];

        // Копируем часть данных
        System.arraycopy(data, dataOffset, entry.data, offset, length);

        // Помечаем страницу как измененную
        entry.dirty = true;
        entry.lastAccessTime = System.nanoTime();
    }

    @Override
    public void clearPage(int bufferIndex) {
        validateBufferIndex(bufferIndex);
        PageBufferEntry entry = buffers[bufferIndex];

        // Очищаем данные страницы
        Arrays.fill(entry.data, (byte)0);
        Arrays.fill(entry.bitmap, (byte)0);

        // Помечаем страницу как измененную
        entry.dirty = true;
        entry.lastAccessTime = System.nanoTime();
    }
    @Override
    public int loadPage(int pageNumber) throws IOException {
        int existingIndex = findPageInBuffer(pageNumber);
        if (existingIndex != -1) {
            buffers[existingIndex].lastAccessTime = System.nanoTime();
            pageHits++;
            return existingIndex;
        }
        pageMisses++;

        int freeSlot = findFreeSlot();
        if (freeSlot != -1) {
            pagesLoaded++;
            return loadPageToSlot(pageNumber, freeSlot);
        }

        int victimIndex = selectVictimPage();
        unloadPage(victimIndex);
        pagesLoaded++;
        return loadPageToSlot(pageNumber, victimIndex);
    }

    @Override
    public int findPageInBuffer(int pageNumber) {
        for (int i = 0; i < bufferSize; i++) {
            if (buffers[i].pageNumber == pageNumber) {
                return i;
            }
        }
        return -1;
    }

    private int findFreeSlot() {
        for (int i = 0; i < bufferSize; i++) {
            if (buffers[i].pageNumber == -1) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean hasFreeSlot() {
        return findFreeSlot() != -1;
    }

    @Override
    public int getFreeSlotCount() {
        int count = 0;
        for (int i = 0; i < bufferSize; i++) {
            if (buffers[i].pageNumber == -1) {
                count++;
            }
        }
        return count;
    }

    private int selectVictimPage() {
        int victim = nextVictimIndex;
        nextVictimIndex = (nextVictimIndex + 1) % bufferSize;
        return victim;
    }

    private int loadPageToSlot(int pageNumber, int slotIndex) throws IOException {
        PageBufferEntry entry = buffers[slotIndex];

        byte[] pageData = fileWorker.readPage(pageNumber);
        byte[] pageBitmap = fileWorker.readBitmap(pageNumber);

        System.arraycopy(pageData, 0, entry.data, 0, entry.data.length);
        System.arraycopy(pageBitmap, 0, entry.bitmap, 0, entry.bitmap.length);

        entry.pageNumber = pageNumber;
        entry.dirty = false;
        entry.lastAccessTime = System.nanoTime();
        entry.loadTime = System.nanoTime();

        return slotIndex;
    }

    @Override
    public void unloadPage(int bufferIndex) throws IOException {
        PageBufferEntry entry = buffers[bufferIndex];
        if (entry.pageNumber == -1) return;

        if (entry.dirty) {
            fileWorker.writePage(entry.pageNumber, entry.data);
            fileWorker.writeBitmap(entry.pageNumber, entry.bitmap);
            pagesFlushed++;
        }

        entry.reset();
        pagesUnloaded++;
    }

    @Override
    public void releasePage(int bufferIndex) throws IOException {
        validateBufferIndex(bufferIndex);
        unloadPage(bufferIndex);
    }

    @Override
    public boolean isPageLoaded(int pageNumber) {
        return findPageInBuffer(pageNumber) != -1;
    }

    @Override
    public byte[] getPageData(int bufferIndex) {
        validateBufferIndex(bufferIndex);
        return buffers[bufferIndex].data;
    }

    @Override
    public byte[] getPageBitmap(int bufferIndex) {
        validateBufferIndex(bufferIndex);
        return buffers[bufferIndex].bitmap;
    }

    @Override
    public void markPageDirty(int bufferIndex) {
        validateBufferIndex(bufferIndex);
        buffers[bufferIndex].dirty = true;
        buffers[bufferIndex].lastAccessTime = System.nanoTime();
    }

    @Override
    public int getPageNumber(int bufferIndex) {
        validateBufferIndex(bufferIndex);
        return buffers[bufferIndex].pageNumber;
    }
    @Override
    public int getFreeSlotIndex() {
        if(getFreeSlotCount()==0) throw new RuntimeException("Свободного места нет");
        int i = 0;
        for(; i<getBufferSize();i++){
            if(buffers[i].pageNumber==-1) {  break; }
        }
        nextVictimIndex = i;
        return nextVictimIndex;
    }

    @Override
    public void forceFlushPage(int bufferIndex) throws IOException {
        validateBufferIndex(bufferIndex);
        PageBufferEntry entry = buffers[bufferIndex];
        if (entry.dirty) {
            fileWorker.writePage(entry.pageNumber, entry.data);
            fileWorker.writeBitmap(entry.pageNumber, entry.bitmap);
            entry.dirty = false;
            pagesFlushed++;
        }
    }

    @Override
    public void flushAll() throws IOException {
        for (int i = 0; i < bufferSize; i++) {
            if (buffers[i].pageNumber != -1 && buffers[i].dirty) {
                fileWorker.writePage(buffers[i].pageNumber, buffers[i].data);
                fileWorker.writeBitmap(buffers[i].pageNumber, buffers[i].bitmap);
                buffers[i].dirty = false;
                pagesFlushed++;
            }
        }
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public int getDirtyPagesCount() {
        int count = 0;
        for (int i = 0; i < bufferSize; i++) {
            if (buffers[i].pageNumber != -1 && buffers[i].dirty) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String getBufferStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Buffer Status ===\n");
        sb.append("Buffer size: ").append(bufferSize).append("\n");
        sb.append("Free slots: ").append(getFreeSlotCount()).append("\n");
        sb.append("Dirty pages: ").append(getDirtyPagesCount()).append("\n");
        sb.append("Pages in memory:\n");
        for (int i = 0; i < bufferSize; i++) {
            if (buffers[i].pageNumber != -1) {
                sb.append("  Slot ").append(i).append(": Page ").append(buffers[i].pageNumber)
                        .append(" (dirty=").append(buffers[i].dirty).append(")\n");
            } else {
                sb.append("  Slot ").append(i).append(": empty\n");
            }
        }
        sb.append("===================\n");
        return sb.toString();
    }

    @Override
    public String getBufferStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Buffer Statistics ===\n");
        sb.append("Page hits: ").append(pageHits).append("\n");
        sb.append("Page misses: ").append(pageMisses).append("\n");
        sb.append("Pages loaded: ").append(pagesLoaded).append("\n");
        sb.append("Pages unloaded: ").append(pagesUnloaded).append("\n");
        sb.append("Pages flushed: ").append(pagesFlushed).append("\n");
        double hitRatio = (pageHits + pageMisses) > 0 ?
                (double)pageHits / (pageHits + pageMisses) * 100 : 0;
        sb.append(String.format("Hit ratio: %.2f%%\n", hitRatio));
        sb.append("========================\n");
        return sb.toString();
    }

    private void validateBufferIndex(int index) {
        if (index < 0 || index >= bufferSize) {
            throw new IllegalArgumentException("Invalid buffer index: " + index +
                    ". Buffer size: " + bufferSize);
        }
        if (buffers[index].pageNumber == -1) {
            throw new IllegalStateException("Buffer slot " + index + " is empty");
        }
    }
}