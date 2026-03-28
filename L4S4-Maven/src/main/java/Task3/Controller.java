// Task3/Controller.java
package Task3;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Controller {
    private int numberOfUnits;
    private int[] unitStates;
    private Random random;
    private volatile boolean running;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Controller(int port, int numberOfUnits) throws IOException {
        this.numberOfUnits = numberOfUnits;
        this.unitStates = new int[numberOfUnits];
        this.random = new Random();
        this.running = true;

        for (int i = 0; i < numberOfUnits; i++) {
            unitStates[i] = 0;
        }

        System.out.println("=== КОНТРОЛЛЕР ЗАПУЩЕН ===");
        System.out.println("Порт: " + port);
        System.out.println("Количество установок: " + numberOfUnits);

        serverSocket = new ServerSocket(port);
    }

    public void start() {
        while (running) {
            try {
                // Ожидаем подключения диспетчера
                System.out.println("Ожидание подключения диспетчера...");
                clientSocket = serverSocket.accept();
                System.out.println("Диспетчер подключен!");

                // Инициализируем потоки для нового подключения
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                // Отправляем количество установок
                out.writeObject(numberOfUnits);
                out.flush();
                System.out.println("Отправлено количество установок: " + numberOfUnits);

                // Работаем с подключенным диспетчером
                handleClient();

            } catch (SocketException e) {
                if (running) {
                    System.out.println("Соединение с диспетчером разорвано");
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Ошибка ввода-вывода: " + e.getMessage());
                }
            } finally {
                // Закрываем соединение с текущим диспетчером
                closeClientConnection();
            }
        }

        closeServer();
    }

    private void handleClient() {
        try {
            // Основной цикл отправки состояний
            while (running && clientSocket != null && !clientSocket.isClosed()) {
                Thread.sleep(2000);
                updateStates();
                sendStates();
            }
        } catch (InterruptedException e) {
            System.err.println("Поток контроллера прерван");
        } catch (IOException e) {
            if (running) {
                System.out.println("Ошибка при отправке данных: " + e.getMessage());
            }
        }
    }

    private void updateStates() {
        System.out.println("\n--- Обновление состояний ---");
        for (int i = 0; i < numberOfUnits; i++) {
            switch (unitStates[i]) {
                case 0:
                    if (random.nextDouble() < 0.2) {
                        unitStates[i] = 1;
                        System.out.println("Установка " + (i + 1) + " -> АВАРИЯ!");
                    }
                    break;
                case 1:
                    unitStates[i] = 2;
                    System.out.println("Установка " + (i + 1) + " -> РЕМОНТ");
                    break;
                case 2:
                    if (random.nextDouble() < 0.5) {
                        unitStates[i] = 0;
                        System.out.println("Установка " + (i + 1) + " -> РАБОТАЕТ");
                    }
                    break;
            }
        }

        System.out.println("Текущие состояния:");
        String[] stateNames = {"РАБОТАЕТ", "АВАРИЯ", "РЕМОНТ"};
        for (int i = 0; i < numberOfUnits; i++) {
            System.out.println("  Установка " + (i + 1) + ": " + stateNames[unitStates[i]]);
        }
    }

    private void sendStates() throws IOException {
        for (int state : unitStates) {
            out.writeObject(state);
            out.flush();
        }
        System.out.println("Состояния отправлены диспетчеру");
    }

    private void closeClientConnection() {
        try {
            if (out != null) {
                out.close();
                out = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                clientSocket = null;
            }
            System.out.println("Соединение с диспетчером закрыто");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        closeClientConnection();
        closeServer();
    }

    public static void main(String[] args) {
        int numberOfUnits = 5;
        int port = 12345;

        if (args.length >= 1) {
            try {
                numberOfUnits = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверное количество установок, используется значение по умолчанию: 5");
            }
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется значение по умолчанию: 12345");
            }
        }

        System.out.println("Запуск контроллера с " + numberOfUnits + " установками на порту " + port);

        try {
            Controller controller = new Controller(port, numberOfUnits);
            controller.start();
        } catch (IOException e) {
            System.err.println("Не удалось запустить контроллер: " + e.getMessage());
            e.printStackTrace();
        }
    }
}