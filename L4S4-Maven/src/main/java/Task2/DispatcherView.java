package Task2;

public interface DispatcherView {
    void showConnectionInfo(String host, int port);
    void showData(ProcessData data);
    void showError(String message);
}