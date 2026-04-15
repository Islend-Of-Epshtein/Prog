package GUI.Task1;

import Task1.ClientRequest;
import Task1.Cortege;
import Task1.FileServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Главное окно приложения.
 */
public class InputAddressFrame extends JFrame implements PropertyChangeListener {

    private static final Color BACKGROUND_COLOR = Color.decode("#aceba2");
    private static final Color BUTTON_COLOR = Color.decode("#eeeeee");
    private static final Color BUTTON_TEXT_COLOR = Color.decode("#000000");
    private static final Color FRAME_BACKGROUND = Color.decode("#aceba2");
    private static final Color BACKGROUND_FILETABLE_COLOR = Color.decode("#ffffff");
    private static final Color BACKGROUND_SERVER_COLOR = Color.decode("#ffffff");

    private static final int LOG_FRAME_LENGTH_TEXT_IN_ROW = 145;
    private static final int LOG_FRAME_LENGTH_TIME_IN_ROW = 115;
    private static final int CHAR_LENGTH_IN_PX = 7;

    private final JFrame frame;
    private FileServer server;
    private ClientRequest client;
    private final JComboBox<String> comboBox = new JComboBox<>();
    private int port;
    private JButton connectDisconnectButton;
    private JTextField ipField;
    private String selectedDir = "";
    private DefaultTableModel fileTableModel;
    private DefaultTableModel clientTableModel;
    private DefaultTableModel serverTableModel;
    private JTable fileTable;

    /**
     * Конструктор главного окна.
     */
    public InputAddressFrame() {
        frame = new JFrame("GxpExplorer");
        frame.setSize(900, 500);
        frame.setIconImage(new ImageIcon("FileManagerLogo.png").getImage());
        frame.setLocationRelativeTo(null);
        frame.setBackground(FRAME_BACKGROUND);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            port = getPort();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка порта: порт не выбран");
            frame.dispose();
        }

        initElements();
        connectDisconnectButton.requestFocus();
    }

    /**
     * Инициализация всех элементов интерфейса.
     */
    public void initElements() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(5, 5, 0, 5));

        panel.add(createLeftWrap());
        panel.add(createCenterWrap("Клиентская сторона", true));
        panel.add(createCenterWrap("Серверная сторона", false));

        frame.add(panel);
        frame.setVisible(true);
    }

    /**
     * Создание левой панели с файловой системой.
     *
     * @return панель
     */
    private JPanel createLeftWrap() {
        JPanel leftWrap = new JPanel();
        leftWrap.setBackground(BACKGROUND_COLOR);

        JPanel leftPan = new JPanel();
        leftPan.setLayout(new BoxLayout(leftPan, BoxLayout.Y_AXIS));
        leftPan.setBackground(BACKGROUND_COLOR);

        leftPan.add(createFilePanel());
        leftPan.add(createTransferPanel());

        leftWrap.add(leftPan);
        return leftWrap;
    }

    /**
     * Создание панели с ComboBox и таблицей директорий.
     *
     * @return панель
     */
    private JPanel createFilePanel() {
        JPanel filepan = new JPanel();
        filepan.setBackground(BACKGROUND_COLOR);
        filepan.setLayout(new BoxLayout(filepan, BoxLayout.Y_AXIS));

        JPanel boxpan = new JPanel(new GridLayout(1, 1));
        boxpan.setBackground(BACKGROUND_COLOR);
        comboBox.setPreferredSize(new Dimension(290, 25));
        comboBox.setBorder(new LineBorder(Color.BLACK, 2, true));
        comboBox.setUI(new CustomComboBoxUI());
        comboBox.setFocusable(false);
        comboBox.setRenderer(new CustomComboBoxRenderer());
        comboBox.addActionListener(_ -> {
            if (comboBox.getSelectedItem() != null) {
                selectedDir = (String) comboBox.getSelectedItem();
                fileTableModel.setRowCount(0);
                for (int i = comboBox.getItemCount() - 1; i >= 0; i--) {
                    String item = comboBox.getItemAt(i);
                    if (selectedDir.length() < item.length()) {
                        comboBox.removeItemAt(i);
                    }
                }
                fileTableModel.addRow(new Object[]{"."});
                fileTableModel.addRow(new Object[]{".."});
                serverWrite(selectedDir);
                fileTable.requestFocusInWindow();
            }
        });

        boxpan.setBorder(new EmptyBorder(0, 5, 10, 5));
        boxpan.add(comboBox);
        filepan.add(boxpan);

        JPanel tablePan = new JPanel(new GridLayout(1, 1));
        tablePan.setBorder(new EmptyBorder(0, 5, 0, 5));
        tablePan.setBackground(BACKGROUND_COLOR);

        fileTableModel = new DefaultTableModel(0, 1) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public String getColumnName(int column) {
                return "";
            }
        };

        fileTable = new JTable(fileTableModel);
        fileTable.setTableHeader(null);
        fileTable.setRowSelectionAllowed(true);
        fileTable.setShowGrid(false);
        fileTable.setRowHeight(20);
        fileTable.setBackground(BACKGROUND_FILETABLE_COLOR);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        fileTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && client != null) {
                    serverRequest();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BACKGROUND_FILETABLE_COLOR);
        scrollPane.setPreferredSize(new Dimension(290, 300));
        scrollPane.setBorder(new LineBorder(Color.BLACK, 2, true));
        tablePan.add(scrollPane);
        filepan.add(tablePan);

        filepan.add(createIpPanel());
        filepan.add(createBottomPanel());

        return filepan;
    }

    /**
     * Создание панели для ввода IP адреса и кнопки подключения.
     *
     * @return панель
     */
    private JPanel createIpPanel() {
        JLabel address = new JLabel("IP-aдpec:");
        ipField = new JTextField(9);
        ipField.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) {
                    return;
                }
                String currentText = getText(0, getLength());
                int newLength = currentText.length() + str.length();
                if (newLength <= 15) {
                    super.insertString(offs, str, a);
                }
            }
        });
        ipField.setText("localhost");
        ipField.setBorder(new LineBorder(Color.BLACK, 2, true));
        ipField.setPreferredSize(new Dimension(100, 25));

        connectDisconnectButton = createButton("Подключиться", new Dimension(100, 25));
        connectDisconnectButton.addActionListener(_ -> {
            if (connectDisconnectButton.getText().equals("Подключиться")) {
                try {
                    if (connectServer(ipField.getText())) {
                        return;
                    }
                    serverTableModel.addRow(new Object[]{
                            "Клиент соединился",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))
                    });
                    connectDisconnectButton.setText("  Отключиться  ");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка подключения: " + ex);
                }
            } else {
                try {
                    client.removePropertyChangeListener(this);
                    client.Off();
                    client = null;
                    serverTableModel.addRow(new Object[]{
                            "Клиент отключился",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))
                    });
                    connectDisconnectButton.setText("Подключиться");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка отключения: " + ex);
                }
            }
        });

        JPanel ipPanel = new JPanel();
        ipPanel.setBackground(BACKGROUND_COLOR);
        ipPanel.add(address);
        ipPanel.add(ipField);
        ipPanel.add(connectDisconnectButton);

        return ipPanel;
    }

    /**
     * Создание нижней панели с кнопками управления сервером и выхода.
     *
     * @return панель
     */
    private JPanel createBottomPanel() {
        JPanel bottomPan = new JPanel(new GridLayout(1, 2));
        bottomPan.setBackground(BACKGROUND_COLOR);

        JButton turnOnOffButton = createButton("  Включить Сервер  ", new Dimension(130, 25));
        turnOnOffButton.addActionListener(_ -> {
            if (turnOnOffButton.getText().equals("  Включить Сервер  ")) {
                try {
                    server = new FileServer(port);
                    serverTableModel.addRow(new Object[]{
                            "Сервер включен",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))
                    });
                    turnOnOffButton.setText("Выключить Сервер");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка включения: " + ex);
                }
            } else {
                try {
                    server.removePropertyChangeListener(this);
                    server.Off();
                    server = null;
                    serverTableModel.setRowCount(0);
                    serverTableModel.addRow(new Object[]{
                            "Сервер выключен",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))
                    });
                    turnOnOffButton.setText("  Включить Сервер  ");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка выключения: " + ex);
                }
            }
        });

        JButton exitButton = createButton("Выход", new Dimension(80, 25));
        exitButton.addActionListener(_ -> {
            try {
                if (client != null) {
                    client.Off();
                }
                if (server != null) {
                    server.Off();
                }
                frame.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка выключения: " + ex);
            }
        });

        JPanel left = new JPanel();
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        left.add(turnOnOffButton);
        left.setBackground(BACKGROUND_COLOR);
        right.add(exitButton);
        right.setBackground(BACKGROUND_COLOR);

        bottomPan.add(left);
        bottomPan.add(right);

        return bottomPan;
    }

    /**
     * Создание панели с кнопками передачи данных.
     *
     * @return панель
     */
    private JPanel createTransferPanel() {
        JPanel transferPan = new JPanel();
        transferPan.setBackground(BACKGROUND_COLOR);

        JButton clientTransferButton = createButton("Передать клиенту", new Dimension(140, 25));
        JButton serverTransferButton = createButton("Передать серверу", new Dimension(140, 25));

        serverTransferButton.setFocusable(false);
        clientTransferButton.setFocusable(false);

        clientTransferButton.addActionListener(_ -> {
            clientTableModel.setRowCount(0);
            clientTableModel.addRow(new Object[]{
                    "Клиент очищен",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))
            });
        });

        serverTransferButton.addActionListener(_ -> {
            if (client != null) {
                serverRequest();
            }
        });

        transferPan.add(clientTransferButton);
        transferPan.add(serverTransferButton);

        return transferPan;
    }

    /**
     * Создание центральной панели с логами.
     *
     * @param title заголовок панели
     * @param isClient true для клиентской панели, false для серверной
     * @return панель
     */
    private JPanel createCenterWrap(String title, boolean isClient) {
        JPanel centerWrap = new JPanel();
        centerWrap.setBackground(BACKGROUND_COLOR);

        JPanel centerPan = new JPanel();
        centerPan.setBorder(new LineBorder(Color.BLACK, 2, true));
        centerPan.setBackground(BACKGROUND_SERVER_COLOR);
        centerPan.setLayout(new BoxLayout(centerPan, BoxLayout.Y_AXIS));
        centerPan.setAlignmentX(Panel.LEFT_ALIGNMENT);

        JLabel centerPanelLabel = new JLabel(title);
        centerPanelLabel.setBackground(BACKGROUND_SERVER_COLOR);
        centerPanelLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        DefaultTableModel tableModel = new DefaultTableModel(0, 2) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public String getColumnName(int column) {
                return "";
            }
        };

        JTable localTable = new JTable(tableModel);
        localTable.setTableHeader(null);
        localTable.setRowSelectionAllowed(true);
        localTable.setShowGrid(false);
        localTable.setRowHeight(20);
        localTable.setBackground(BACKGROUND_SERVER_COLOR);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        localTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        localTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        localTable.getColumnModel().getColumn(0).setPreferredWidth(LOG_FRAME_LENGTH_TEXT_IN_ROW);
        localTable.getColumnModel().getColumn(1).setPreferredWidth(LOG_FRAME_LENGTH_TIME_IN_ROW);

        JScrollPane scrollPane = new JScrollPane(localTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(
                LOG_FRAME_LENGTH_TEXT_IN_ROW + LOG_FRAME_LENGTH_TIME_IN_ROW, 420));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.getViewport().setBackground(BACKGROUND_SERVER_COLOR);

        if (isClient) {
            clientTableModel = tableModel;
        } else {
            serverTableModel = tableModel;
        }

        centerPan.add(centerPanelLabel);
        centerPan.add(scrollPane);
        centerWrap.add(centerPan);

        return centerWrap;
    }

    /**
     * Разбивает длинную строку на части фиксированной длины.
     *
     * @param text        исходный текст
     * @param chunkLength длина фрагмента
     * @return список фрагментов
     */
    private static List<String> splitByLength(String text, int chunkLength) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkLength) {
            int end = Math.min(i + chunkLength, text.length());
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    /**
     * Запуск главного окна.
     */
    public static void run() {
        new InputAddressFrame();
    }

    /**
     * Подключение к серверу.
     *
     * @param address IP-адрес сервера
     * @return true если произошла ошибка, false если подключение успешно
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private boolean connectServer(String address) throws IOException {
        if (server == null) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения: Сервер недоступен");
            return true;
        }
        if (Objects.equals(address, "")) {
            return true;
        }

        client = new ClientRequest(new InetSocketAddress(address, port).getAddress(), port);
        clientTableModel.setRowCount(0);
        comboBox.removeAllItems();
        client.clientStringThread();

        Thread serverLauncher = new Thread(() -> {
            try {
                server.Accept();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Ошибка подключения: " + e)
                );
            }
        });
        serverLauncher.start();
        server.serverStringThread();

        client.addPropertyChangeListener("InClientMessage", this);
        server.addPropertyChangeListener("InServerMessage", this);
        client.addPropertyChangeListener("OutClientMessage", this);
        server.addPropertyChangeListener("OutServerMessage", this);

        return false;
    }

    /**
     * Выбор свободного порта через диалог.
     *
     * @return выбранный порт
     * @throws IOException если не удалось получить порт
     */
    private int getPort() throws IOException {
        List<ServerSocket> sockets = new ArrayList<>();
        Integer[] freePorts = new Integer[6];

        for (int i = 0; i < 5; i++) {
            ServerSocket socket = new ServerSocket(0);
            sockets.add(socket);
            freePorts[i] = socket.getLocalPort();
        }

        for (ServerSocket socket : sockets) {
            socket.close();
        }

        freePorts[5] = 0;

        return (Integer) JOptionPane.showInputDialog(
                frame,
                "Выберите порт:",
                "Порт соединения",
                JOptionPane.QUESTION_MESSAGE,
                null,
                freePorts,
                freePorts[0]
        );
    }

    /**
     * Отправка запроса на сервер для получения содержимого директории.
     */
    private void serverRequest() {
        if (comboBox.getSelectedIndex() == -1) {
            return;
        }

        String selectedFile = "";
        String dir = selectedDir.substring(0, 3);

        if (fileTable.getSelectedRow() != -1) {
            selectedFile = (String) fileTableModel.getValueAt(fileTable.getSelectedRow(), 0);
            if (!selectedFile.equals("..") && !selectedFile.equals(".")) {
                selectedFile = selectedFile.substring(3);
            }
        }

        switch (selectedFile) {
            case ".":
                for (int i = comboBox.getItemCount() - 1; i >= 0; i--) {
                    String item = comboBox.getItemAt(i);
                    if (item.equals(dir)) {
                        comboBox.setSelectedItem(item);
                    }
                }
                return;
            case "..":
                if (selectedDir.length() > 3) {
                    selectedDir = selectedDir.substring(0, selectedDir.lastIndexOf("\\"));
                    selectedDir = selectedDir.substring(0, selectedDir.lastIndexOf("\\") + 1);
                    if (selectedDir.indexOf('\\', 3) > 1) {
                        comboBox.setSelectedItem(comboBox.getItemAt(comboBox.getItemCount() - 2));
                    } else {
                        for (int i = comboBox.getItemCount() - 1; i >= 0; i--) {
                            String item = comboBox.getItemAt(i);
                            if (item.equals(dir)) {
                                comboBox.setSelectedItem(comboBox.getItemAt(i));
                            }
                        }
                    }
                }
                return;
            default:
                break;
        }

        if (selectedFile.endsWith(".exe") || selectedFile.endsWith(".exe\\")) {
            serverWrite(selectedDir + selectedFile);
            return;
        }

        if (!selectedFile.isEmpty()) {
            selectedDir = selectedDir + selectedFile + '\\';
            comboBox.addItem(selectedDir);
            comboBox.setSelectedItem(comboBox.getItemAt(comboBox.getItemCount() - 1));
        }
    }

    /**
     * Отправка абсолютного пути на сервер.
     *
     * @param absolutePath путь для отправки
     */
    private void serverWrite(String absolutePath) {
        try {
            if ( this.client == null ) { return; }
            client.Write(absolutePath, true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка передачи от клиента: " + ex);
        }
    }

    /**
     * Создание стилизованной кнопки.
     *
     * @param text текст кнопки
     * @param size размер кнопки
     * @return созданная кнопка
     */
    public static JButton createButton(String text, Dimension size) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setFocusPainted(false);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setBorder(new LineBorder(Color.BLACK, 2, true));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
                button.setBackground(getRandomLightColor());
                button.setForeground(Color.BLACK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(new LineBorder(Color.BLACK, 2, true));
                button.setBackground(BUTTON_COLOR);
                button.setForeground(BUTTON_TEXT_COLOR);
            }
        });

        return button;
    }

    /**
     * Генерация случайного светлого цвета.
     *
     * @return случайный светлый цвет
     */
    public static Color getRandomLightColor() {
        Random random = new Random();
        int r = random.nextInt(200) + 55;
        int g = random.nextInt(200) + 55;
        int b = random.nextInt(200) + 55;
        return new Color(r, g, b);
    }

    /**
     * Обработка событий изменения свойств.
     *
     * @param evt событие изменения свойства
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateLog(evt);

        if (!(evt.getPropertyName().equals("InClientMessage")) || evt.getOldValue() != null) {
            return;
        }
        if (client == null) {
            return;
        }

        Cortege message = (Cortege) evt.getNewValue();

        if (message.isRootElement()) {
            comboBox.addItem(message.getData());
            return;
        }

        if (message.getData() == null || message.getData().trim().isEmpty() || message.getData().equals("..")) {
            return;
        }

        String displayName = getDisplayName(message);
        fileTableModel.addRow(new Object[]{displayName});
    }

    /**
     * Получение отображаемого имени с иконкой.
     *
     * @param message объект Cortege с данными
     * @return отображаемое имя
     */
    private String getDisplayName(Cortege message) {
        File file = new File(selectedDir + '\\' + message.getData());
        String displayName;

        if (file.isDirectory()) {
            displayName = "📁 " + message.getData();
        } else if (file.getName().endsWith(".txt")) {
            displayName = "📄 " + message.getData();
        } else if (file.getName().endsWith(".exe")) {
            displayName = "⚙️ " + message.getData();
        } else if (file.exists()) {
            displayName = "📎 " + message.getData();
        } else {
            displayName = message.getData();
        }

        return displayName;
    }

    /**
     * Обновление логов в центральных таблицах.
     *
     * @param evt событие изменения свойства
     */
    private void updateLog(PropertyChangeEvent evt) {
        Cortege data = (Cortege) evt.getNewValue();

        switch (evt.getPropertyName()) {
            case "InClientMessage":
                if (evt.getOldValue() != null) {
                    clientTableModel.addRow(new Object[]{"Клиент получил ответ:", ""});
                    int i = 1;
                    for (String str : splitByLength(data.getData(),
                            LOG_FRAME_LENGTH_TEXT_IN_ROW / CHAR_LENGTH_IN_PX)) {
                        if (i == 1) {
                            clientTableModel.addRow(new Object[]{str, data.getTime()});
                            i = 0;
                        } else {
                            clientTableModel.addRow(new Object[]{str, ""});
                        }
                    }
                }
                break;
            case "InServerMessage":
                if (evt.getOldValue() != null) {
                    serverTableModel.addRow(new Object[]{"Сервер получил:", ""});
                    int i = 1;
                    for (String str : splitByLength(data.getData(),
                            LOG_FRAME_LENGTH_TEXT_IN_ROW / CHAR_LENGTH_IN_PX)) {
                        if (i == 1) {
                            serverTableModel.addRow(new Object[]{str, data.getTime()});
                            i = 0;
                        } else {
                            serverTableModel.addRow(new Object[]{str, ""});
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}

/**
 * Пользовательский UI для ComboBox с круглой кнопкой.
 */
class CustomComboBoxUI extends BasicComboBoxUI {

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton();
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(25, 25));

        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (button.getModel().isPressed()) {
                    g2.setColor(Color.DARK_GRAY);
                } else if (button.getModel().isRollover()) {
                    g2.setColor(new Color(200, 200, 200));
                } else {
                    g2.setColor(Color.BLACK);
                }

                int size = Math.min(button.getWidth(), button.getHeight()) - 4;
                int x = (button.getWidth() - size) / 2;
                int y = (button.getHeight() - size) / 2;
                g2.fillOval(x, y, size, size);

                g2.setColor(Color.WHITE);
                int[] xPoints = {button.getWidth() / 2 - 4, button.getWidth() / 2 + 4, button.getWidth() / 2};
                int[] yPoints = {button.getHeight() / 2 - 1, button.getHeight() / 2 - 1, button.getHeight() / 2 + 3};
                g2.fillPolygon(xPoints, yPoints, 3);

                g2.dispose();
            }
        });

        return button;
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        if (!hasFocus) {
            g.setColor(comboBox.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            super.paintCurrentValueBackground(g, bounds, true);
        }
    }
}

/**
 * Рендерер для убирания рамки у выбранного элемента в ComboBox.
 */
class CustomComboBoxRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (c instanceof JLabel label) {
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

            if (isSelected) {
                label.setBackground(new Color(200, 200, 255));
                label.setForeground(Color.BLACK);
                label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 10));
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.DARK_GRAY);
            }
        }

        return c;
    }
}