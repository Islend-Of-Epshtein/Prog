package Task2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {
    private final int port;
    private final ProcessDataGenerator generator;
    private volatile boolean running;

    public Controller(int port) {
        this.port = port;
        this.generator = new ProcessDataGenerator();
        this.running = true;
    }
    public void start() {
        System.out.println("=== КОНТРОЛЛЕР ТЕХНОЛОГИЧЕСКОГО ПРОЦЕССА ===");
        System.out.println("Ожидание подключения диспетчера на порту: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket socket = serverSocket.accept();
             ProcessDataTransmitter transmitter = new ConsoleProcessDataTransmitter(socket)) {

            System.out.println("Диспетчер подключен: "
                    + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

            while (running) {
                ProcessData data = generator.next();

                System.out.println("[Контроллер] Сгенерировано: " + data);
                transmitter.send(data);

                Thread.sleep(1000);
            }

        } catch (IOException e) {
            System.err.println("Ошибка контроллера: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Поток контроллера был прерван");
        }
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        int port = 12345;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, будет использован 12345");
            }
        }

        new Controller(port).start();
    }
}