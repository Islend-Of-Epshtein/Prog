// Task3/gui/Launcher.java
package GUI.Task3;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Launcher {
    private Process controllerProcess;
    private Process dispatcherProcess;
    private JSpinner unitsSpinner;
    private JLabel controllerStatus;
    private JLabel dispatcherStatus;
    private JButton startControllerButton;
    private JButton stopControllerButton;
    private JButton startDispatcherButton;

    public Launcher() {
        showLauncherWindow();
    }

    private void showLauncherWindow() {
        JFrame frame = new JFrame("Task 3: Система мониторинга установок");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 500);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel titleLabel = new JLabel("СИСТЕМА МОНИТОРИНГА УСТАНОВОК");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Количество установок
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel unitsLabel = new JLabel("Количество установок:");
        unitsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(unitsLabel, gbc);

        unitsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        unitsSpinner.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(unitsSpinner, gbc);

        // Информация
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><center>Сначала запустите контроллер, затем диспетчер.<br>При остановке контроллера диспетчер автоматически закроется.</center></html>");
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
        mainPanel.add(startControllerButton, gbc);

        // Кнопка остановки контроллера
        gbc.gridx = 1;
        stopControllerButton = new JButton("ОСТАНОВИТЬ КОНТРОЛЛЕР");
        stopControllerButton.setFont(new Font("Arial", Font.BOLD, 12));
        stopControllerButton.setBackground(new Color(200, 0, 0));
        stopControllerButton.setForeground(Color.WHITE);
        stopControllerButton.setFocusPainted(false);
        stopControllerButton.setEnabled(false);
        stopControllerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

        // Обработчики кнопок
        startControllerButton.addActionListener(e -> startController());
        stopControllerButton.addActionListener(e -> stopController());

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopController();
                if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
                    dispatcherProcess.destroy();
                }
                System.exit(0);
            }
        });
    }

    private void startController() {
        int numberOfUnits = (Integer) unitsSpinner.getValue();

        unitsSpinner.setEnabled(false);
        startControllerButton.setEnabled(false);
        stopControllerButton.setEnabled(true);

        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = "Task3.Controller";

            ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
                    String.valueOf(numberOfUnits), "12345");
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            controllerProcess = builder.start();

            controllerStatus.setText("● Запущен (" + numberOfUnits + " уст.)");
            controllerStatus.setForeground(new Color(0, 150, 0));
            System.out.println("✓ Контроллер запущен с " + numberOfUnits + " установками на порту 12345");

            // Мониторинг завершения контроллера
            new Thread(() -> {
                try {
                    controllerProcess.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("⚠ Контроллер завершил работу");

                        // Если диспетчер запущен, закрываем его
                        if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
                            System.out.println("Закрытие диспетчера...");
                            dispatcherProcess.destroy();
                            dispatcherStatus.setText("● Закрыт (контроллер остановлен)");
                            dispatcherStatus.setForeground(Color.ORANGE);
                            dispatcherProcess = null;
                        }

                        controllerStatus.setText("● Завершен");
                        controllerStatus.setForeground(Color.ORANGE);
                        unitsSpinner.setEnabled(true);
                        startControllerButton.setEnabled(true);
                        stopControllerButton.setEnabled(false);

                        // Показываем сообщение пользователю
                        JOptionPane.showMessageDialog(null,
                                "Контроллер завершил работу.\nДиспетчер автоматически закрыт.",
                                "Контроллер остановлен", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            controllerStatus.setText("● Ошибка: " + ex.getMessage());
            controllerStatus.setForeground(Color.RED);
            ex.printStackTrace();
            unitsSpinner.setEnabled(true);
            startControllerButton.setEnabled(true);
            stopControllerButton.setEnabled(false);
        }
    }

    private void startDispatcher() {
        int numberOfUnits = (Integer) unitsSpinner.getValue();

        // Проверяем, запущен ли контроллер
        if (controllerProcess == null || !controllerProcess.isAlive()) {
            JOptionPane.showMessageDialog(null,
                    "Сначала запустите контроллер!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Проверяем, не запущен ли уже диспетчер
        if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
            JOptionPane.showMessageDialog(null,
                    "Диспетчер уже запущен!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = "Task3.Dispatcher";

            ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
                    String.valueOf(numberOfUnits));
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            dispatcherProcess = builder.start();

            dispatcherStatus.setText("● Запущен (" + numberOfUnits + " уст.)");
            dispatcherStatus.setForeground(new Color(0, 150, 0));
            System.out.println("✓ Диспетчер запущен с " + numberOfUnits + " установками");

            // Мониторинг завершения диспетчера
            new Thread(() -> {
                try {
                    dispatcherProcess.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        // Проверяем, не был ли диспетчер закрыт из-за остановки контроллера
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

        // Если диспетчер запущен, закрываем его
        if (dispatcherProcess != null && dispatcherProcess.isAlive()) {
            dispatcherProcess.destroy();
            dispatcherStatus.setText("● Закрыт (контроллер остановлен)");
            dispatcherStatus.setForeground(Color.ORANGE);
            System.out.println("✗ Диспетчер закрыт");
            dispatcherProcess = null;
        }

        unitsSpinner.setEnabled(true);
        startControllerButton.setEnabled(true);
        stopControllerButton.setEnabled(false);
        controllerProcess = null;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new Launcher());
    }
}