package GUI.Task2;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ConnectionFrame {
    private Process controllerProcess;
    private Process dispatcherProcess;
    private JTextField portField;
    private JLabel controllerStatus;
    private JLabel dispatcherStatus;
    private JButton startControllerButton;
    private JButton stopControllerButton;
    private JButton startDispatcherButton;
    private JFrame frame;

    public ConnectionFrame() {
        showLauncherWindow();
    }

    private void showLauncherWindow() {
        frame = new JFrame("Task 2: Контроллер технологического процесса");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 450);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel titleLabel = new JLabel("КОНТРОЛЛЕР ТЕХНОЛОГИЧЕСКОГО ПРОЦЕССА");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Порт
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel portLabel = new JLabel("Порт:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(portLabel, gbc);

        portField = new JTextField("12345", 10);
        portField.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(portField, gbc);

        // Информация
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><center>Сначала запустите контроллер, затем диспетчер.<br>Данные температуры и давления будут отображаться в консоли.</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(infoLabel, gbc);

        // Разделитель
        gbc.gridy++;
        JSeparator separator = new JSeparator();
        mainPanel.add(separator, gbc);

        // Статус контроллера
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel controllerLabel = new JLabel("Контроллер:");
        controllerLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(controllerLabel, gbc);

        controllerStatus = new JLabel("● Не запущен");
        controllerStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        controllerStatus.setForeground(Color.RED);
        gbc.gridx = 1;
        mainPanel.add(controllerStatus, gbc);

        // Статус диспетчера
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel dispatcherLabel = new JLabel("Диспетчер:");
        dispatcherLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(dispatcherLabel, gbc);

        dispatcherStatus = new JLabel("● Не запущен");
        dispatcherStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        dispatcherStatus.setForeground(Color.RED);
        gbc.gridx = 1;
        mainPanel.add(dispatcherStatus, gbc);

        // Разделитель
        gbc.gridy++;
        JSeparator separator2 = new JSeparator();
        mainPanel.add(separator2, gbc);

        // Кнопка запуска контроллера
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        startControllerButton = new JButton("ЗАПУСТИТЬ КОНТРОЛЛЕР");
        startControllerButton.setFont(new Font("Arial", Font.BOLD, 12));
        startControllerButton.setBackground(new Color(0, 120, 215));
        startControllerButton.setForeground(Color.WHITE);
        startControllerButton.setFocusPainted(false);
        startControllerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startControllerButton.addActionListener(e -> startController());
        mainPanel.add(startControllerButton, gbc);

        // Кнопка остановки контроллера
        gbc.gridx = 1;
        stopControllerButton = new JButton("ОСТАНОВИТЬ");
        stopControllerButton.setFont(new Font("Arial", Font.BOLD, 12));
        stopControllerButton.setBackground(new Color(200, 0, 0));
        stopControllerButton.setForeground(Color.WHITE);
        stopControllerButton.setFocusPainted(false);
        stopControllerButton.setEnabled(false);
        stopControllerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stopControllerButton.addActionListener(e -> stopController());
        mainPanel.add(stopControllerButton, gbc);

        // Кнопка запуска диспетчера
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        startDispatcherButton = new JButton("ЗАПУСТИТЬ ДИСПЕТЧЕРА");
        startDispatcherButton.setFont(new Font("Arial", Font.BOLD, 12));
        startDispatcherButton.setBackground(new Color(0, 150, 100));
        startDispatcherButton.setForeground(Color.WHITE);
        startDispatcherButton.setFocusPainted(false);
        startDispatcherButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startDispatcherButton.addActionListener(e -> startDispatcher());
        mainPanel.add(startDispatcherButton, gbc);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopController();
                if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
                    dispatcherProcess.destroy();
                }
            }
        });
    }

    private int getPort() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1024 || port > 65535) {
                JOptionPane.showMessageDialog(frame,
                        "Порт должен быть в диапазоне 1024-65535",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return -1;
            }
            return port;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame,
                    "Неверный формат порта",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
    }

    private void startController() {
        int port = getPort();
        if (port < 0) return;

        portField.setEnabled(false);
        startControllerButton.setEnabled(false);
        stopControllerButton.setEnabled(true);

        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = "Task2.Controller";

            ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
                    String.valueOf(port));
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            controllerProcess = builder.start();

            controllerStatus.setText("● Запущен (порт " + port + ")");
            controllerStatus.setForeground(new Color(0, 150, 0));
            System.out.println("✓ Контроллер запущен на порту " + port);

            new Thread(() -> {
                try {
                    controllerProcess.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("⚠ Контроллер завершил работу");

                        if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
                            System.out.println("Закрытие диспетчера...");
                            dispatcherProcess.destroy();
                            dispatcherStatus.setText("● Закрыт (контроллер остановлен)");
                            dispatcherStatus.setForeground(Color.ORANGE);
                            dispatcherProcess = null;
                        }

                        controllerStatus.setText("● Завершен");
                        controllerStatus.setForeground(Color.ORANGE);
                        portField.setEnabled(true);
                        startControllerButton.setEnabled(true);
                        stopControllerButton.setEnabled(false);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            controllerStatus.setText("● Ошибка: " + ex.getMessage());
            controllerStatus.setForeground(Color.RED);
            ex.printStackTrace();
            portField.setEnabled(true);
            startControllerButton.setEnabled(true);
            stopControllerButton.setEnabled(false);
        }
    }

    private void startDispatcher() {
        int port = getPort();
        if (port < 0) return;

        if (controllerProcess == null || !controllerProcess.isAlive()) {
            JOptionPane.showMessageDialog(frame,
                    "Сначала запустите контроллер!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
            JOptionPane.showMessageDialog(frame,
                    "Диспетчер уже запущен!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = "Task2.Dispatcher";

            ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
                    "localhost", String.valueOf(port));
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            dispatcherProcess = builder.start();

            dispatcherStatus.setText("● Запущен");
            dispatcherStatus.setForeground(new Color(0, 150, 0));
            System.out.println("✓ Диспетчер запущен");

            new Thread(() -> {
                try {
                    dispatcherProcess.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        if (controllerProcess != null && controllerProcess.isAlive()) {
                            dispatcherStatus.setText("● Завершен");
                            dispatcherStatus.setForeground(Color.ORANGE);
                            System.out.println("⚠ Диспетчер завершил работу");
                        } else {
                            dispatcherStatus.setText("● Закрыт");
                            dispatcherStatus.setForeground(Color.ORANGE);
                        }
                        dispatcherProcess = null;
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            dispatcherStatus.setText("● Ошибка: " + ex.getMessage());
            dispatcherStatus.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    private void stopController() {
        if (controllerProcess != null && controllerProcess.isAlive()) {
            controllerProcess.destroy();
            try {
                controllerProcess.waitFor(3000, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controllerStatus.setText("● Остановлен");
            controllerStatus.setForeground(Color.RED);
            System.out.println("✗ Контроллер остановлен");
        }

        if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
            dispatcherProcess.destroy();
            dispatcherStatus.setText("● Закрыт (контроллер остановлен)");
            dispatcherStatus.setForeground(Color.ORANGE);
            System.out.println("✗ Диспетчер закрыт");
            dispatcherProcess = null;
        }

        portField.setEnabled(true);
        startControllerButton.setEnabled(true);
        stopControllerButton.setEnabled(false);
        controllerProcess = null;
    }

    public static void Run() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new ConnectionFrame());
    }
}
