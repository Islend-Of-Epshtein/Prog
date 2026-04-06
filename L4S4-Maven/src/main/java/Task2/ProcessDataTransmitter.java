package Task2;

import java.io.IOException;

public interface ProcessDataTransmitter extends AutoCloseable {
    void send(ProcessData data) throws IOException;
    void close() throws IOException;
}