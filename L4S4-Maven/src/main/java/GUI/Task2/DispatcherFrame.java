package GUI.Task2;

import Task2.DispatcherView;
import Task2.ProcessData;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class DispatcherFrame extends JFrame implements DispatcherView {
    private final JLabel connectionLabel;
    private final JLabel temperatureLabel;
    private final JLabel pressureLabel;
    private final JLabel statusLabel;

    public DispatcherFrame() {
        setTitle("Task2 - Пульт диспетчера");
        setSize(420, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        connectionLabel = new JLabel("Нет подключения", SwingConstants.CENTER);
        temperatureLabel = new JLabel("Температура: -- °C", SwingConstants.CENTER);
        pressureLabel = new JLabel("Давление: -- атм", SwingConstants.CENTER);
        statusLabel = new JLabel("Ожидание данных...", SwingConstants.CENTER);

        add(connectionLabel);
        add(temperatureLabel);
        add(pressureLabel);
        add(statusLabel);
    }

    @Override
    public void showConnectionInfo(String host, int port) {
        SwingUtilities.invokeLater(() ->
                connectionLabel.setText("Подключено к " + host + ":" + port));
    }

    @Override
    public void showData(ProcessData data) {
        SwingUtilities.invokeLater(() -> {
            temperatureLabel.setText(
                    "Температура: " + String.format(Locale.US, "%.2f", data.getTemperature()) + " °C");
            pressureLabel.setText(
                    "Давление: " + String.format(Locale.US, "%.2f", data.getPressure()) + " атм");
            statusLabel.setText("Данные получены");
        });
    }

    @Override
    public void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Ошибка",
                        JOptionPane.ERROR_MESSAGE));
    }
}