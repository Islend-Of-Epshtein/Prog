// Task3/Dispatcher.java - обновлен конструктор для принятия нового количества
package Task3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;

public class Dispatcher {
    private JFrame mainFrame;
    private JPanel buttonsPanel;
    private JButton[] unitButtons;
    private JLabel statusLabel;
    private int numberOfUnits;

    private JFrame connectionFrame;
    private JTextField hostField;
    private JTextField portField;
    private JSpinner unitsSpinner;
    private JButton connectButton;
    private JLabel connectionStatusLabel;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private volatile boolean connected;
    private Thread receiverThread;

    private int presetUnits;

    public Dispatcher() {
        this(5);
    }

    public Dispatcher(int presetUnits) {
        this.presetUnits = presetUnits;
        System.out.println("Диспетчер запущен с предустановленным количеством установок: " + presetUnits);
        showConnectionDialog();
    }

    private void showConnectionDialog() {
        connectionFrame = new JFrame("Подключение к контроллеру");
        connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectionFrame.setSize(450, 500);
        connectionFrame.setLayout(new BorderLayout());
        connectionFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("ПОДКЛЮЧЕНИЕ К КОНТРОЛЛЕРУ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Хост:"), gbc);
        hostField = new JTextField("localhost", 15);
        gbc.gridx = 1;
        mainPanel.add(hostField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Порт:"), gbc);
        portField = new JTextField("12345", 15);
        gbc.gridx = 1;
        mainPanel.add(portField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Количество установок:"), gbc);
        // Используем presetUnits для установки значения в спиннер
        unitsSpinner = new JSpinner(new SpinnerNumberModel(presetUnits, 1, 50, 1));
        unitsSpinner.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1;
        mainPanel.add(unitsSpinner, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("Количество установок должно совпадать с настройками контроллера");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(infoLabel, gbc);

        gbc.gridy++;
        connectionStatusLabel = new JLabel("Введите параметры и нажмите 'Подключиться'");
        connectionStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        connectionStatusLabel.setForeground(Color.GRAY);
        mainPanel.add(connectionStatusLabel, gbc);

        gbc.gridy++;
        connectButton = new JButton("ПОДКЛЮЧИТЬСЯ");
        connectButton.setFont(new Font("Arial", Font.BOLD, 14));
        connectButton.setBackground(new Color(0, 120, 215));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(e -> attemptConnection());
        mainPanel.add(connectButton, gbc);

        connectionFrame.add(mainPanel, BorderLayout.CENTER);
        connectionFrame.setVisible(true);
    }

    private void attemptConnection() {
        String host = hostField.getText().trim();
        int port;
        int units;

        try {
            port = Integer.parseInt(portField.getText().trim());
            units = (Integer) unitsSpinner.getValue();
        } catch (NumberFormatException e) {
            connectionStatusLabel.setText("Ошибка: неверный номер порта!");
            connectionStatusLabel.setForeground(Color.RED);
            return;
        }

        if (units < 1 || units > 100) {
            connectionStatusLabel.setText("Ошибка: количество установок должно быть от 1 до 100");
            connectionStatusLabel.setForeground(Color.RED);
            return;
        }

        connectionStatusLabel.setText("Подключение к " + host + ":" + port + "...");
        connectionStatusLabel.setForeground(Color.ORANGE);
        connectButton.setEnabled(false);
        connectButton.setText("ПОДКЛЮЧЕНИЕ...");

        new Thread(() -> {
            boolean success = connectToController(host, port, units);
            if (!success) {
                SwingUtilities.invokeLater(() -> {
                    connectButton.setEnabled(true);
                    connectButton.setText("ПОДКЛЮЧИТЬСЯ");
                });
            }
        }).start();
    }

    private boolean connectToController(String host, int port, int expectedUnits) {
        try {
            closeConnection();

            System.out.println("Подключение к контроллеру " + host + ":" + port);
            System.out.println("Ожидаемое количество установок: " + expectedUnits);

            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            Object responseObj = in.readObject();
            int controllerUnits = (Integer) responseObj;
            System.out.println("Контроллер имеет установок: " + controllerUnits);

            if (controllerUnits != expectedUnits) {
                final String errorMsg = "Контроллер имеет " + controllerUnits +
                        " установок, а выбрано " + expectedUnits;
                SwingUtilities.invokeLater(() -> {
                    connectionStatusLabel.setText("Ошибка: " + errorMsg);
                    connectionStatusLabel.setForeground(Color.RED);
                    connectButton.setEnabled(true);
                    connectButton.setText("ПОДКЛЮЧИТЬСЯ");
                });
                closeConnection();
                return false;
            }

            this.numberOfUnits = controllerUnits;
            this.connected = true;

            SwingUtilities.invokeLater(() -> {
                connectionStatusLabel.setText("УСПЕШНО ПОДКЛЮЧЕНО!");
                connectionStatusLabel.setForeground(Color.GREEN);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                connectionFrame.dispose();
                createMainWindow();
            });

            startReceiving();

            return true;

        } catch (ConnectException e) {
            SwingUtilities.invokeLater(() -> {
                connectionStatusLabel.setText("Ошибка: контроллер не запущен на порту " + port);
                connectionStatusLabel.setForeground(Color.RED);
                connectButton.setEnabled(true);
                connectButton.setText("ПОДКЛЮЧИТЬСЯ");
            });
            return false;
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                connectionStatusLabel.setText("Ошибка: " + e.getMessage());
                connectionStatusLabel.setForeground(Color.RED);
                connectButton.setEnabled(true);
                connectButton.setText("ПОДКЛЮЧИТЬСЯ");
            });
            e.printStackTrace();
            return false;
        }
    }

    private void startReceiving() {
        receiverThread = new Thread(() -> {
            try {
                while (connected && socket != null && !socket.isClosed()) {
                    receiveStates();
                }
            } catch (EOFException e) {
                if (connected) {
                    handleDisconnect("Контроллер закрыл соединение");
                }
            } catch (SocketException e) {
                if (connected) {
                    handleDisconnect("Соединение разорвано");
                }
            } catch (Exception e) {
                if (connected) {
                    System.err.println("Ошибка: " + e.getMessage());
                }
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private void receiveStates() throws Exception {
        int[] states = new int[numberOfUnits];

        for (int i = 0; i < numberOfUnits; i++) {
            Object obj = in.readObject();
            states[i] = (Integer) obj;
        }

        SwingUtilities.invokeLater(() -> updateButtons(states));
    }

    private void handleDisconnect(String message) {
        connected = false;
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null && mainFrame.isVisible()) {
                statusLabel.setText("СВЯЗЬ ПОТЕРЯНА! " + message);
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(mainFrame,
                        "Соединение с контроллером потеряно!\n" + message,
                        "Потеря связи", JOptionPane.WARNING_MESSAGE);
                mainFrame.dispose();
            }
            showConnectionDialog();
        });
    }

    private void createMainWindow() {
        mainFrame = new JFrame("Пульт диспетчера - " + numberOfUnits + " установок");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int windowSize = Math.min(800, 300 + (int)Math.sqrt(numberOfUnits) * 80);
        mainFrame.setSize(windowSize, windowSize);
        mainFrame.setLayout(new BorderLayout());

        buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainFrame.add(buttonsPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Подключено к контроллеру. Прием данных...", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        JButton reconnectButton = new JButton("Переподключиться");
        reconnectButton.setFont(new Font("Arial", Font.PLAIN, 11));
        reconnectButton.addActionListener(e -> {
            closeConnection();
            if (mainFrame != null) {
                mainFrame.dispose();
            }
            // При переподключении показываем окно с текущим presetUnits
            showConnectionDialog();
        });
        statusPanel.add(reconnectButton, BorderLayout.EAST);

        mainFrame.add(statusPanel, BorderLayout.SOUTH);
        mainFrame.setLocationRelativeTo(null);

        createButtons();
        mainFrame.setVisible(true);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
                System.exit(0);
            }
        });
    }

    private void createButtons() {
        int cols = (int) Math.ceil(Math.sqrt(numberOfUnits));
        int rows = (int) Math.ceil((double) numberOfUnits / cols);
        buttonsPanel.setLayout(new GridLayout(rows, cols, 10, 10));

        unitButtons = new JButton[numberOfUnits];
        for (int i = 0; i < numberOfUnits; i++) {
            JButton btn = new JButton();
            btn.setText("<html><center>Установка " + (i + 1) + "<br>ОЖИДАНИЕ</center></html>");
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setEnabled(false);
            unitButtons[i] = btn;
            buttonsPanel.add(btn);
        }

        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void updateButtons(int[] states) {
        if (unitButtons == null) return;

        for (int i = 0; i < numberOfUnits && i < unitButtons.length; i++) {
            JButton btn = unitButtons[i];
            switch (states[i]) {
                case 0:
                    btn.setBackground(Color.GREEN);
                    btn.setText("<html><center>Установка " + (i + 1) +
                            "<br><font color='black'><b>РАБОТАЕТ</b></font></center></html>");
                    break;
                case 1:
                    btn.setBackground(Color.RED);
                    btn.setText("<html><center>Установка " + (i + 1) +
                            "<br><font color='white'><b>АВАРИЯ!</b></font></center></html>");
                    break;
                case 2:
                    btn.setBackground(Color.GRAY);
                    btn.setText("<html><center>Установка " + (i + 1) +
                            "<br><font color='black'><b>РЕМОНТ</b></font></center></html>");
                    break;
            }
        }
    }

    private void closeConnection() {
        connected = false;
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        in = null;
        out = null;
        socket = null;
        System.out.println("Соединение закрыто");
    }

    public static void main(String[] args) {
        int presetUnits = 5;

        if (args.length >= 1) {
            try {
                presetUnits = Integer.parseInt(args[0]);
                System.out.println("Получено количество установок из командной строки: " + presetUnits);
            } catch (NumberFormatException e) {
                System.err.println("Неверное количество установок, используется значение по умолчанию: 5");
            }
        }

        final int finalPresetUnits = presetUnits;
        SwingUtilities.invokeLater(() -> new Dispatcher(finalPresetUnits));
    }
}