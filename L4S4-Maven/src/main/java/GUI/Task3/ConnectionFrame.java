package GUI.Task3;
import Task3.*;

import javax.swing.*;

/// Запуск старой версии Task3 , надо сделать так чтобы запустилось окно ConnectionFrame которое
/// по нажатию кнопки производит попытку подключения к серверу а также в этом окне задается количество станков
/// Окно подключения реализовать здесь.
public class ConnectionFrame {
    public static void Run() {
        Thread serverThread = new Thread(() -> {
            // Task3.Controller.run(null); // ЗАПУСК ОКНА КОНТРЛЛЕРА
        });
        serverThread.start();
        try {
            Thread.sleep(1000); // 1 секунда
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Запускаем клиент
        // Task3.Dispatcher.run(null); // ЗАПУСК ОКНА ДИСПЕТЧЕРА
    }
}
