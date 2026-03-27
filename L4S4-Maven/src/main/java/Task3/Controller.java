// Task3/Controller.java
package Task3;

import Base.Server;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Controller extends Server {
    private static final int NUMBER_OF_UNITS = 5;
    private int[] unitStates;
    private Random random;
    private volatile boolean running;
    private boolean clientConnected = false;

    public Controller(int port) throws IOException {
        super(port);
        this.unitStates = new int[NUMBER_OF_UNITS];
        this.random = new Random();
        this.running = true;

        // Инициализация: все установки работают
        for (int i = 0; i < NUMBER_OF_UNITS; i++) {
            unitStates[i] = 0;
        }

        System.out.println("=== КОНТРОЛЛЕР ЗАПУЩЕН ===");
        System.out.println("Порт: " + port);
        System.out.println("Количество установок: " + NUMBER_OF_UNITS);

        // Помечаем, что клиент подключился (конструктор Server уже принял соединение)
        clientConnected = true;
        System.out.println("Диспетчер подключен!");
    }

    public void start() {
        try {
            // Получаем PrintWriter для отправки данных (используем GetSocket() из Server)
            PrintWriter out = new PrintWriter(GetSocket().getOutputStream(), true);

            // 1. Отправляем диспетчеру количество установок
            out.println(NUMBER_OF_UNITS);
            System.out.println("Отправлено количество установок: " + NUMBER_OF_UNITS);

            // 2. Основной цикл
            while (running) {
                Thread.sleep(2000);
                updateStates();
                sendStates(out);
            }

        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Поток контроллера прерван");
        } finally {
            closeConnection();
        }
    }

    private void updateStates() {
        System.out.println("\n--- Обновление состояний ---");
        for (int i = 0; i < NUMBER_OF_UNITS; i++) {
            switch (unitStates[i]) {
                case 0: // Работает
                    if (random.nextDouble() < 0.2) {
                        unitStates[i] = 1;
                        System.out.println("Установка " + (i + 1) + " -> АВАРИЯ!");
                    }
                    break;
                case 1: // Авария
                    unitStates[i] = 2;
                    System.out.println("Установка " + (i + 1) + " -> РЕМОНТ");
                    break;
                case 2: // Ремонт
                    if (random.nextDouble() < 0.5) {
                        unitStates[i] = 0;
                        System.out.println("Установка " + (i + 1) + " -> РАБОТАЕТ");
                    }
                    break;
            }
        }

        // Выводим текущие состояния
        System.out.println("Текущие состояния:");
        for (int i = 0; i < NUMBER_OF_UNITS; i++) {
            String state = "";
            switch (unitStates[i]) {
                case 0: state = "РАБОТАЕТ"; break;
                case 1: state = "АВАРИЯ"; break;
                case 2: state = "РЕМОНТ"; break;
            }
            System.out.println("  Установка " + (i + 1) + ": " + state);
        }
    }

    private void sendStates(PrintWriter out) throws IOException {
        for (int state : unitStates) {
            out.println(state);
        }
        out.flush();
        System.out.println("Состояния отправлены диспетчеру");
    }

    private void closeConnection() {
        try {
            if (GetSocket() != null && !GetSocket().isClosed()) {
                GetSocket().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        try {
            Controller controller = new Controller(12345);
            controller.start();
        } catch (IOException e) {
            System.err.println("Не удалось запустить контроллер: " + e.getMessage());
            e.printStackTrace();
        }
    }
}