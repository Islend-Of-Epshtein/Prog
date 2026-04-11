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

class CustomComboBoxUI extends BasicComboBoxUI {

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton();
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(25, 25));

        // Рисуем круглую стрелку
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Круглый фон
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

                // Стрелка вниз
                g2.setColor(Color.WHITE);
                int[] xPoints = {button.getWidth()/2 - 4, button.getWidth()/2 + 4, button.getWidth()/2};
                int[] yPoints = {button.getHeight()/2 - 1, button.getHeight()/2 - 1, button.getHeight()/2 + 3};
                g2.fillPolygon(xPoints, yPoints, 3);

                g2.dispose();
            }
        });

        return button;
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        // Убираем фон у выбранного элемента (делаем прозрачным)
        if (!hasFocus) {
            g.setColor(comboBox.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            super.paintCurrentValueBackground(g, bounds, hasFocus);
        }
    }
}

// Рендерер для убирания рамки у выбранного элемента
class CustomComboBoxRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (c instanceof JLabel) {
            JLabel label = (JLabel) c;

            // Убираем рамку фокуса
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

            if (isSelected) {
                label.setBackground(new Color(200, 200, 255));
                label.setForeground(Color.BLACK);
                // Убираем любую рамку у выбранного элемента
                label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 10));
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.DARK_GRAY);
            }
        }

        return c;
    }
}
// Главное окно приложения
public class InputAddressFrame extends JFrame implements PropertyChangeListener {

    // Константы цветов
    private static final Color BACKGROUND_COLOR = Color.decode("#aceba2");  // основной бежевый фон #ECE9D8
    private static final Color BUTTON_COLOR = Color.decode("#eeeeee");
    private static final Color BUTTON_TEXT_COLOR = Color.decode("#000000");      // цвет кнопок
    private static final Color FRAME_BACKGROUND = Color.decode("#aceba2");  // фон главного окна
    private static final Color BACKGROUND_FILETABLE_COLOR = Color.decode("#ffffff");
    private static final Color BACKGROUND_SERVER_COLOR = Color.decode("#ffffff");

    private final JFrame frame;
    private FileServer server;
    private ClientRequest client;
    private final JComboBox<String> comboBox = new JComboBox<>();
    private int port;
    private JButton ConnectDisconnect;
    private JTextField IPField;
    private String selectedDir = "";  // текущая выбранная директория
    private DefaultTableModel fileTableModel, clientTableModel, serverTableModel;  // модели таблиц
    private JTable fileTable;
    private final int logFrameLengthTextInRow = 145;
    private final int logFrameLengthTimeInRow =115;

    public InputAddressFrame() {
        frame = new JFrame("GxpExplorer");
        frame.setSize(900, 500);
        frame.setIconImage(new ImageIcon("FileManagerLogo.png").getImage());
        frame.setLocationRelativeTo(null);
        frame.setBackground(FRAME_BACKGROUND);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            port = getPort();  // выбор порта через диалог
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(this, "Ошибка порта: порт не выбран" );
            frame.dispose();
        }

        initElements();
        ConnectDisconnect.requestFocus();
    }

    // Инициализация всех элементов интерфейса
    public void initElements() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(5, 5, 0, 5));

        panel.add(createLeftWrap());                           // левая панель с файлами
        panel.add(createCenterWrap("Клиентская сторона", true));   // центр - клиент
        panel.add(createCenterWrap("Серверная сторона", false));   // центр - сервер

        frame.add(panel);
        frame.setVisible(true);
    }

    // Левая панель (ComboBox + таблица + кнопки)
    private JPanel createLeftWrap() {
        JPanel leftWrap = new JPanel();
        leftWrap.setBackground(BACKGROUND_COLOR);

        JPanel leftPan = new JPanel();
        leftPan.setLayout(new BoxLayout(leftPan, BoxLayout.Y_AXIS));
        leftPan.setBackground(BACKGROUND_COLOR);

        leftPan.add(createFilePanel());    // панель с файловой таблицей
        leftPan.add(createTransferPanel()); // панель с кнопками передачи

        leftWrap.add(leftPan);
        return leftWrap;
    }

    // Панель с ComboBox и таблицей директорий
    private JPanel createFilePanel() {
        JPanel filepan = new JPanel();
        filepan.setBackground(BACKGROUND_COLOR);
        filepan.setLayout(new BoxLayout(filepan, BoxLayout.Y_AXIS));

        // ComboBox для выбора корневых директорий
        JPanel boxpan = new JPanel(new GridLayout(1, 1));
        boxpan.setBackground(BACKGROUND_COLOR);
        comboBox.setPreferredSize(new Dimension(290, 25));
        comboBox.setBorder(new LineBorder(Color.BLACK, 2, true));
        // Устанавливаем кастомный UI с круглой кнопкой
        comboBox.setUI(new CustomComboBoxUI());

        // Убираем рамку фокуса у ComboBox
        comboBox.setFocusable(false);

        // Устанавливаем рендерер для убирания рамки у выбранного элемента
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
                fileTableModel.addRow(new Object[]{".."});  // кнопка "наверх"
                serverWrite(selectedDir);
                fileTable.requestFocusInWindow();
            }
        });

        boxpan.setBorder(new EmptyBorder(0, 5, 10, 5));
        boxpan.add(comboBox);
        filepan.add(boxpan);

        // Таблица для отображения содержимого директории
        JPanel tablePan = new JPanel(new GridLayout(1, 1));
        tablePan.setBorder(new EmptyBorder(0, 5, 0, 5));
        tablePan.setBackground(BACKGROUND_COLOR);

        // Модель таблицы (один столбец, без заголовка)
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
        fileTable.setTableHeader(null);                    // скрываем заголовок
        fileTable.setRowSelectionAllowed(true);            // разрешаем выделение строк
        fileTable.setShowGrid(false);                      // убираем сетку
        fileTable.setRowHeight(20);                        // высота строки
        fileTable.setBackground(BACKGROUND_FILETABLE_COLOR);

        // Левое выравнивание
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        fileTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        // Двойной клик по строке - запрос содержимого директории
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && client!=null) {
                    serverRequest();  // отправляем запрос на сервер
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

        filepan.add(createIpPanel());     // панель с IP и кнопкой подключения
        filepan.add(createBottomPanel()); // нижняя панель с кнопками

        return filepan;
    }

    // Панель для ввода IP адреса и кнопки подключения
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
        IPField.setText("localhost");
        IPField.setBorder(new LineBorder(Color.BLACK, 2, true));
        IPField.setPreferredSize(new Dimension(100,25));


        ConnectDisconnect = CreateButton("Подключиться", new Dimension(100, 25));
        ConnectDisconnect.addActionListener(_ -> {
            if (ConnectDisconnect.getText().equals("Подключиться")) {
                try {
                    if(connectServer(IPField.getText())){ return;}
                    // логируем успешное подключение
                    serverTableModel.addRow(new Object[]{"Клиент соеденился", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))});
                    ConnectDisconnect.setText("  Отключиться  ");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка подключения: "+ ex);
                }
            } else {
                // отключение клиента
                try {
                    client.removePropertyChangeListener(this);
                    client.Off();
                    client = null;
                    serverTableModel.addRow(new Object[]{"Клиент отключился", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))});
                    ConnectDisconnect.setText("Подключиться");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка отключния: "+ ex);
                }
            }
        });

        JPanel IpPan = new JPanel();
        IpPan.setBackground(BACKGROUND_COLOR);
        IpPan.add(address);
        IpPan.add(IPField);
        IpPan.add(ConnectDisconnect);

        return IpPan;
    }

    // Нижняя панель (включение/выключение сервера и выход)
    private JPanel createBottomPanel() {
        JPanel bottomPan = new JPanel(new GridLayout(1, 2));
        bottomPan.setBackground(BACKGROUND_COLOR);

        JButton TurnOnOff = CreateButton("  Включить Сервер  ", new Dimension(130, 25));
        TurnOnOff.addActionListener(_ -> {
            if (TurnOnOff.getText().equals("  Включить Сервер  ")) {
                try {
                    server = new FileServer(port);
                    serverTableModel.addRow(new Object[]{"Сервер включен", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))});
                    TurnOnOff.setText("Выключить Сервер");
                }
                catch (Exception ex){
                    JOptionPane.showMessageDialog(this, "Ошибка включения: "+ ex);
                }
            } else {
                // выключение сервера
                try {
                    server.removePropertyChangeListener(this);
                    server.Off();
                    server = null;
                    serverTableModel.setRowCount(0);
                    serverTableModel.addRow(new Object[]{"Сервер выключен", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))});
                    TurnOnOff.setText("  Включить Сервер  ");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка выключения: "+ ex);
                }
            }
        });

        JButton exitBtn = CreateButton("Выход", new Dimension(80, 25));
        exitBtn.addActionListener(_ -> {
            try {
                if(client!=null){ client.Off(); }
                if(server!=null){ server.Off(); }
                frame.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка выключения: "+ ex);
            }
        });

        JPanel left = new JPanel();
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        left.add(TurnOnOff);
        left.setBackground(BACKGROUND_COLOR);
        right.add(exitBtn);
        right.setBackground(BACKGROUND_COLOR);

        bottomPan.add(left);
        bottomPan.add(right);

        return bottomPan;
    }

    // Панель с кнопками передачи данных (заглушки)
    private JPanel createTransferPanel() {
        JPanel transferPan = new JPanel();
        transferPan.setBackground(BACKGROUND_COLOR);

        JButton clientTransfer = CreateButton("Передать клиенту", new Dimension(140, 25));
        JButton serverTransfer = CreateButton("Передать серверу", new Dimension(140, 25));

        serverTransfer.setFocusable(false);
        clientTransfer.setFocusable(false);

        clientTransfer.addActionListener(_ -> {
            clientTableModel.setRowCount(0);
            clientTableModel.addRow(new Object[]{"Клиент отчищен", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss"))});
        });
        serverTransfer.addActionListener(_ -> {
            if(client!=null) {serverRequest(); }
        });

        transferPan.add(clientTransfer);
        transferPan.add(serverTransfer);

        return transferPan;
    }

    // Центральные панели (клиентская и серверная) с таблицами логов
    private JPanel createCenterWrap(String title, boolean log) {
        JPanel centerWrap = new JPanel();
        centerWrap.setBackground(BACKGROUND_COLOR);

        JPanel centerPan = new JPanel();
        centerPan.setBorder(new LineBorder(Color.BLACK, 2, true));
        centerPan.setBackground(BACKGROUND_SERVER_COLOR);
        centerPan.setLayout(new BoxLayout(centerPan, BoxLayout.Y_AXIS));
        centerPan.setAlignmentX(Panel.LEFT_ALIGNMENT);

        JLabel CenterPanLabel = new JLabel(title);
        CenterPanLabel.setBackground(BACKGROUND_SERVER_COLOR);
        CenterPanLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Модель с двумя столбцами: текст сообщения и время
        DefaultTableModel clientAreaModel = new DefaultTableModel(0, 2) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public String getColumnName(int column) {
                return "";
            }
        };

        JTable localTable = new JTable(clientAreaModel);
        localTable.setTableHeader(null);      // скрываем заголовок
        localTable.setRowSelectionAllowed(true);
        localTable.setShowGrid(false);
        localTable.setRowHeight(20);
        localTable.setBackground(BACKGROUND_SERVER_COLOR);

        // левое выравнивание для текста
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        localTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        // правое выравнивание для времени
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        localTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        // настройка ширины
        localTable.getColumnModel().getColumn(0).setPreferredWidth(logFrameLengthTextInRow);
        localTable.getColumnModel().getColumn(1).setPreferredWidth(logFrameLengthTimeInRow);

        JScrollPane scrollPane = new JScrollPane(localTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(logFrameLengthTextInRow + logFrameLengthTimeInRow, 420));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        scrollPane.getViewport().setBackground(BACKGROUND_SERVER_COLOR);
        //scrollPane.getViewport().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        if (log) {
            this.clientTableModel = clientAreaModel;  // для клиентской стороны
        } else {
            this.serverTableModel = clientAreaModel;  // для серверной стороны
        }

        centerPan.add(CenterPanLabel);
        centerPan.add(scrollPane);
        centerWrap.add(centerPan);

        return centerWrap;
    }

    // Разбивает длинную строку на части фиксированной длины (для переноса в таблице)
    private static List<String> splitByLength(String text, int chunkLength) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkLength) {
            int end = Math.min(i + chunkLength, text.length());
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

    public static void Run() {
        new InputAddressFrame();
    }

    // Подключение к серверу
    private boolean connectServer(String address) throws IOException
    {
        if (server == null) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения: Сервер недоступен");
            return true;
        }
        if (Objects.equals(address, "")) {
            return true;
        }

        // создаём клиента
        client = new ClientRequest(new InetSocketAddress(address, port).getAddress(), port);
        clientTableModel.setRowCount(0);
        comboBox.removeAllItems();
        client.clientStringThread();  // запускаем поток чтения сообщений

        // запускаем сервер в отдельном потоке
        Thread servLauncher = new Thread(() -> {
            try {
                server.Accept();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Ошибка подключения: " + e)
                );
            }
        });
        servLauncher.start();
        server.serverStringThread();  // поток чтения на сервере

        // подписываемся на события
        client.addPropertyChangeListener("InClientMessage", this);
        server.addPropertyChangeListener("InServerMessage", this);
        client.addPropertyChangeListener("OutClientMessage", this);
        server.addPropertyChangeListener("OutServerMessage", this);
        return false;
    }

    // Выбор свободного порта через диалог
    private int getPort() throws IOException {
        List<ServerSocket> sockets = new ArrayList<>();
        Integer[] freeport = new Integer[6];
        for(int i=0;i<5;i++){
            ServerSocket socket = new ServerSocket(0);
            sockets.add(socket);
            freeport[i] = socket.getLocalPort();
        }
        for(ServerSocket socket : sockets){
            socket.close();
        }
        freeport[5]=0;
        return (Integer) JOptionPane.showInputDialog(
                frame,
                "Выберите порт:",
                "Порт соединения",
                JOptionPane.QUESTION_MESSAGE,
                null,
                freeport,
                freeport[0]
        );
    }

    // Отправка запроса на сервер для получения содержимого директории
    private void serverRequest(){
        if (comboBox.getSelectedIndex() == -1) {return;}

        String selectedFile = "", dir = selectedDir.substring(0,3);
        //Если пустой заброс - пропускаем
        if(fileTable.getSelectedRow()!=-1) {
            selectedFile = (String)fileTableModel.getValueAt(fileTable.getSelectedRow(), 0);
            if (!selectedFile.equals("..")&& !selectedFile.equals(".")){
                selectedFile= selectedFile.substring(3);
            }
        }
        //Обрабатываем "." и ".."
        switch (selectedFile){
            case ("."):{
                for (int i = comboBox.getItemCount() - 1; i >= 0; i--) {
                    String item = comboBox.getItemAt(i);
                    if (item.equals(dir)) {
                        comboBox.setSelectedItem(item);
                    }
                }
                return;
            }
            case (".."):{
                if(selectedDir.length()>3){
                    selectedDir = selectedDir.substring(0, selectedDir.lastIndexOf("\\"));
                    selectedDir = selectedDir.substring(0, selectedDir.lastIndexOf("\\")+1);
                    if(selectedDir.indexOf('\\', 3) > 1) { comboBox.setSelectedItem(comboBox.getItemAt(comboBox.getItemCount()-2)); }
                    else {
                        for (int i = comboBox.getItemCount() - 1; i >= 0; i--) {
                            String item = comboBox.getItemAt(i);
                            if (item.equals(dir)) {
                                comboBox.setSelectedItem(comboBox.getItemAt(i));
                            }
                        }
                    }
                }
                return;
            }
        }
        //Открытие .exe
        if(selectedFile.endsWith(".exe")||selectedFile.endsWith(".exe\\")){
            serverWrite(selectedDir+selectedFile);
            return;
        }
        //По всем остальным файлам переходим
        if(!selectedFile.isEmpty()){
            selectedDir = selectedDir + selectedFile + '\\';
            comboBox.addItem(selectedDir);
            comboBox.setSelectedItem(comboBox.getItemAt(comboBox.getItemCount()-1));
        }
    }
    private void serverWrite(String absolutePath){
        try {
            client.Write(absolutePath, true);  // отправляем путь на сервер
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка передачи от клиента: " + ex);
        }
    }

    // Вспомогательный метод для создания кнопок
    public static JButton CreateButton(String text, Dimension size) {
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

        // Сохраняем оригинальные цвета

        // Эффект при наведении
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
                button.setBackground(getRandomLightColor());  // светлый фон
                button.setForeground(Color.BLACK);            // чёрный текст (контрастный)
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

    // Светлые цвета для фона
    public static Color getRandomLightColor() {
        Random random = new Random();
        int r = random.nextInt(200) + 55;  // 55-255 (светлые)
        int g = random.nextInt(200) + 55;
        int b = random.nextInt(200) + 55;
        return new Color(r, g, b);
    }

    // Обработка событий изменения свойств (PropertyChangeListener)
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateLog(evt);  // обновляем логи клиента/сервера

        // обрабатываем только входящие сообщения клиента с новыми данными
        if(!(evt.getPropertyName().equals("InClientMessage")) || evt.getOldValue()!=null){ return; }
        if(client == null) {return;}

        Cortege massage = (Cortege)evt.getNewValue();

        // корневые элементы добавляем в ComboBox
        if(massage.isRootElement())
        {
            comboBox.addItem(massage.getData());
            return;
        }
        // пропускаем null, пустые строки и ".."
        if(massage.getData() == null || massage.getData().trim().isEmpty() || massage.getData().equals("..")) {
            return;
        }
        String displayName = getString(massage);
        fileTableModel.addRow(new Object[]{displayName});
    }
    //Метод для иконок возле папок (опионально и для красоты
    private String getString(Cortege massage) {
        File file = new File(selectedDir + '\\' + massage.getData());
        String displayName;
        if (file.isDirectory()) {
            displayName = "📁 " + massage.getData();
        } else if (file.getName().endsWith(".txt")) {
            displayName = "📄 " + massage.getData();
        } else if (file.getName().endsWith(".exe")) {
            displayName = "⚙️ " + massage.getData();
        } else if(file.exists()){
            displayName = "📎 " + massage.getData();
        }
        else {
            displayName =massage.getData();
        }
        return displayName;
    }

    // Обновление логов в центральных таблицах
    private void updateLog(PropertyChangeEvent evt){
        Cortege data = (Cortege)evt.getNewValue();
        int charLenghtInPx = 7;
        switch (evt.getPropertyName()){
            case ("InClientMessage"):
            {
                if (evt.getOldValue()!=null) {
                    clientTableModel.addRow(new Object[]{"Клиент получил ответ:", ""});
                    int i = 1;
                    // разбиваем длинное сообщение на строки
                    for(String str: splitByLength(data.getData(), logFrameLengthTextInRow / charLenghtInPx)){
                        if (i == 1) {
                            clientTableModel.addRow(new Object[]{str, data.getTime()});
                            i = 0;
                        } else {
                            clientTableModel.addRow(new Object[]{str, ""});
                        }
                    }
                }
                break;
            }
            case ("InServerMessage"):
            {
                if (evt.getOldValue()!=null) {
                    serverTableModel.addRow(new Object[]{"Сервер получил:", ""});
                    int i = 1;
                    for(String str: splitByLength(data.getData(), logFrameLengthTextInRow/ charLenghtInPx)){
                        if (i == 1) {
                            serverTableModel.addRow(new Object[]{str, data.getTime()});
                            i = 0;
                        } else {
                            serverTableModel.addRow(new Object[]{str, ""});
                        }
                    }
                }
                break;
            }
            default:
        }
    }
}