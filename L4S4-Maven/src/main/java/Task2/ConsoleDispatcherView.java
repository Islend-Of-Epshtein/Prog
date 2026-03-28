package Task2;

public class ConsoleDispatcherView implements DispatcherView {
    @Override
    public void showConnectionInfo(String host, int port) {
        System.out.println("Пульт диспетчера подключен к " + host + ":" + port);
    }

    @Override
    public void showData(ProcessData data) {
        System.out.println("[Диспетчер] " + data);
    }

    @Override
    public void showError(String message) {
        System.err.println("[Ошибка диспетчера] " + message);
    }
}