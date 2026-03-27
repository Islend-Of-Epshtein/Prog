// Task3/Dispatcher.java
package Task3;

import Base.Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Dispatcher extends Client {
    private JFrame frame;
    private JPanel buttonsPanel;
    private JButton[] unitButtons;
    private JLabel statusLabel;
    private int numberOfUnits;
    private volatile boolean running;
    private Thread receiverThread;

    public Dispatcher(String host, int port) throws IOException {
        super(java.net.InetAddress.getByName(host), port);
        this.running = true;
        System.out.println("Диспетчер подключен к " + host + ":" + port);
        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("Пульт диспетчера");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(buttonsPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Подключение к контроллеру...", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
                if (receiverThread != null) {
                    receiverThread.interrupt();
                }
                try {
                    if (GetSocket() != null && !GetSocket().isClosed()) {
                        GetSocket().close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        startReceiver();
    }

    private void startReceiver() {
        receiverThread = new Thread(() -> {
            try {
                // 1. Получаем количество установок
                String response = Read();
                numberOfUnits = Integer.parseInt(response.trim());
                System.out.println("Получено количество установок: " + numberOfUnits);

                // Создаем кнопки в потоке Swing
                SwingUtilities.invokeLater(() -> createButtons());

                // 2. Основной цикл приема состояний
                while (running) {
                    receiveStates();
                }

            } catch (IOException e) {
                if (running) {
                    System.err.println("Ошибка: потеряно соединение с контроллером");
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame,
                                    "Потеряно соединение с контроллером!",
                                    "Ошибка", JOptionPane.ERROR_MESSAGE)
                    );
                }
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: неверный формат данных");
            }
        });
        receiverThread.start();
    }

    private void createButtons() {
        int cols = (int) Math.ceil(Math.sqrt(numberOfUnits));
        int rows = (int) Math.ceil((double) numberOfUnits / cols);
        buttonsPanel.setLayout(new GridLayout(rows, cols, 15, 15));

        unitButtons = new JButton[numberOfUnits];
        for (int i = 0; i < numberOfUnits; i++) {
            JButton btn = new JButton();
            btn.setText("<html><center>Установка " + (i + 1) + "<br>ОЖИДАНИЕ</center></html>");
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setEnabled(false);
            unitButtons[i] = btn;
            buttonsPanel.add(btn);
        }

        frame.revalidate();
        frame.repaint();
        System.out.println("Создано кнопок: " + numberOfUnits);
        statusLabel.setText("Подключено. Прием данных...");
    }

    private void receiveStates() throws IOException {
        int[] states = new int[numberOfUnits];

        // Читаем состояния всех установок
        for (int i = 0; i < numberOfUnits; i++) {
            String stateStr = Read();
            if (stateStr == null) {
                throw new IOException("Соединение разорвано");
            }
            states[i] = Integer.parseInt(stateStr.trim());
        }

        // Обновляем кнопки в потоке Swing
        SwingUtilities.invokeLater(() -> updateButtons(states));
    }

    private void updateButtons(int[] states) {
        if (unitButtons == null) return;

        for (int i = 0; i < numberOfUnits && i < unitButtons.length; i++) {
            JButton btn = unitButtons[i];
            switch (states[i]) {
                case 0: // Работает
                    btn.setBackground(Color.GREEN);
                    btn.setText("<html><center>Установка " + (i + 1) +
                            "<br><font color='black'><b>РАБОТАЕТ</b></font></center></html>");
                    break;
                case 1: // Авария
                    btn.setBackground(Color.RED);
                    btn.setText("<html><center>Установка " + (i + 1) +
                            "<br><font color='white'><b>АВАРИЯ!</b></font></center></html>");
                    break;
                case 2: // Ремонт
                    btn.setBackground(Color.GRAY);
                    btn.setText("<html><center>Установка " + (i + 1) +
                            "<br><font color='black'><b>РЕМОНТ</b></font></center></html>");
                    break;
            }
        }
    }

    public void stop() {
        running = false;
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Dispatcher("localhost", 12345);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Не удалось подключиться к контроллеру!\n" + e.getMessage(),
                        "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}