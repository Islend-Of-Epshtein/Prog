package Task2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsoleProcessDataTransmitter implements ProcessDataTransmitter {
    private final Socket socket;
    private final PrintWriter writer;

    public ConsoleProcessDataTransmitter(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void send(ProcessData data) throws IOException {
        writer.println(data.serialize());
        if (writer.checkError()) {
            throw new IOException("Ошибка отправки данных диспетчеру");
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}