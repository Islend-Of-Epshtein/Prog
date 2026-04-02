package GUI.Task1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InputAdressFrame extends JFrame {
    private JFrame frame;
    // Поля, к которым нужен доступ из разных методов
    private JButton ConnectDisconnect;
    private JTextField IPField;

    public InputAdressFrame() {
        frame = new JFrame("Input address");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.decode("#081421"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initElements();
    }

    public void initElements() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBackground(Color.decode("#ECE9D8"));
        panel.setBorder(new EmptyBorder(5, 5, 0, 5));

        panel.add(createLeftWrap());
        panel.add(createCenterWrap("Клиентская сторона"));
        panel.add(createCenterWrap("Серверная сторона"));

        frame.add(panel);
        frame.setVisible(true);
    }

    /**
     * Создание левой обертки
     */
    private JPanel createLeftWrap() {
        JPanel leftWrap = new JPanel();
        leftWrap.setBackground(Color.decode("#ECE9D8"));

        JPanel leftPan = new JPanel();
        leftPan.setLayout(new BoxLayout(leftPan, BoxLayout.Y_AXIS));

        leftPan.add(createFilePanel());
        leftPan.add(createTransferPanel());

        leftWrap.add(leftPan);
        return leftWrap;
    }

    /**
     * Создание панели с файлами (ComboBox + TextArea + IP + Bottom)
     */
    private JPanel createFilePanel() {
        JPanel filepan = new JPanel();
        filepan.setBackground(Color.decode("#ECE9D8"));
        filepan.setLayout(new BoxLayout(filepan, BoxLayout.Y_AXIS));

        // ComboBox панель
        JPanel boxpan = new JPanel(new GridLayout(1, 1));
        boxpan.setBackground(Color.decode("#ECE9D8"));
        boxpan.setBorder(new EmptyBorder(0, 5, 10, 5));
        JComboBox<String> comboBox = new JComboBox<>();
        boxpan.add(comboBox);
        filepan.add(boxpan);

        // TextArea панель
        JPanel textpan = new JPanel(new GridLayout(1, 1));
        textpan.setBorder(new EmptyBorder(0, 5, 0, 5));
        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        textpan.add(scrollPane);
        filepan.add(textpan);

        // IP панель
        filepan.add(createIpPanel());

        // Bottom панель
        filepan.add(createBottomPanel());

        return filepan;
    }

    /**
     * Создание панели с IP адресом и кнопкой подключения
     */
    private JPanel createIpPanel() {
        JLabel address = new JLabel("IP-aдрес:");

        IPField = new JTextField(9);
        IPField.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null)
                    return;
                String currentText = getText(0, getLength());
                int newLength = currentText.length() + str.length();
                if (newLength <= 15) {
                    super.insertString(offs, str, a);
                }
            }
        });

        ConnectDisconnect = CreateButton("Подключиться", new Dimension(100, 25));
        ConnectDisconnect.addActionListener(e -> {
            if (ConnectDisconnect.getText().equals("Подключиться")) {
                connectServer();
                ConnectDisconnect.setText("  Отключиться  ");
            } else {
                ConnectDisconnect.setText("Подключиться");
            }
        });

        IPField.addActionListener(e -> {
            if (ConnectDisconnect.getText().equals("Подключиться")) {
                connectServer();
                ConnectDisconnect.setText("  Отключиться  ");
            }
            IPField.transferFocus();
        });

        JPanel IpPan = new JPanel();
        IpPan.setBackground(Color.decode("#ECE9D8"));
        IpPan.add(address);
        IpPan.add(IPField);
        IpPan.add(ConnectDisconnect);

        return IpPan;
    }

    /**
     * Создание нижней панели с кнопками
     */
    private JPanel createBottomPanel() {
        JPanel bottomPan = new JPanel(new GridLayout(1, 2));
        bottomPan.setBackground(Color.decode("#ECE9D8"));

        JButton TurnOnOff = CreateButton("  Включить Сервер  ", new Dimension(130, 25));
        TurnOnOff.addActionListener(e -> {
            if (TurnOnOff.getText().equals("  Включить Сервер  ")) {
                TurnOnOff.setText("Выключить Сервер");
            } else {
                TurnOnOff.setText("  Включить Сервер  ");
            }
        });

        JButton exitBtn = CreateButton("Выход", new Dimension(80, 25));
        exitBtn.addActionListener(e -> frame.dispose());

        JPanel left = new JPanel();
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        left.add(TurnOnOff);
        left.setBackground(Color.decode("#ECE9D8"));
        right.add(exitBtn);
        right.setBackground(Color.decode("#ECE9D8"));

        bottomPan.add(left);
        bottomPan.add(right);

        return bottomPan;
    }

    /**
     * Создание панели передачи данных
     */
    private JPanel createTransferPanel() {
        JPanel transferPan = new JPanel();
        transferPan.setBackground(Color.decode("#ECE9D8"));

        JButton clientTransfer = CreateButton("Передать клиенту", new Dimension(140, 25));
        JButton serverTransfer = CreateButton("Передать серверу", new Dimension(140, 25));

        serverTransfer.setFocusable(false);
        clientTransfer.setFocusable(false);

        clientTransfer.addActionListener(e -> {
            // передача клиенту
        });
        serverTransfer.addActionListener(e -> {
            // передача серверу
        });

        transferPan.add(clientTransfer);
        transferPan.add(serverTransfer);

        return transferPan;
    }

    /**
     * Создание центральной обертки (для клиентской и серверной стороны)
     */
    private JPanel createCenterWrap(String title) {
        JPanel centerWrap = new JPanel();
        centerWrap.setBackground(Color.decode("#ECE9D8"));

        JPanel centerPan = new JPanel();
        centerPan.setBackground(Color.decode("#ECE9D8"));
        centerPan.setLayout(new BoxLayout(centerPan, BoxLayout.Y_AXIS));
        centerPan.setAlignmentX(Panel.LEFT_ALIGNMENT);

        JLabel CenterPanLabel = new JLabel(title);
        CenterPanLabel.setBackground(Color.decode("#ECE9D8"));
        CenterPanLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JTextArea clientArea = new JTextArea();
        clientArea.setBorder(BorderFactory.createBevelBorder(1));

        JScrollPane clientScroll = new JScrollPane(clientArea);

        clientScroll.setPreferredSize(new Dimension(260, 420));

        centerPan.add(CenterPanLabel);
        centerPan.add(clientScroll);
        centerWrap.add(centerPan);

        return centerWrap;
    }

    public static void main() {
        new InputAdressFrame();
    }

    public static void Run() {
        new InputAdressFrame();
    }

    private void connectServer() {
        // Логика подключения к серверу
    }

    public static JButton CreateButton(String text, Dimension size) {
        JButton button = new JButton(text);
        button.setBackground(Color.decode("#eeeeee"));
        button.setPreferredSize(size);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }
}