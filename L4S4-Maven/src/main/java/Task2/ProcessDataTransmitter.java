package Task2;

import java.io.IOException;

public interface ProcessDataTransmitter {
    void send(ProcessData data) throws IOException;
    void close() throws IOException;
}