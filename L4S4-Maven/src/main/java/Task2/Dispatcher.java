package Task2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Dispatcher {
    private final String host;
    private final int port;
    private final DispatcherView view;

    public Dispatcher(String host, int port, DispatcherView view) {
        this.host = host;
        this.port = port;
        this.view = view;
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            view.showConnectionInfo(host, port);

            String line;
            while ((line = reader.readLine()) != null) {
                ProcessData data = ProcessData.deserialize(line);
                view.showData(data);
            }

        } catch (IOException e) {
            view.showError("Ошибка соединения: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            view.showError("Ошибка разбора данных: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        if (args.length > 0) {
            host = args[0];
        }

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, будет использован 12345");
            }
        }

        DispatcherView view = new ConsoleDispatcherView();
        new Dispatcher(host, port, view).start();
    }
}