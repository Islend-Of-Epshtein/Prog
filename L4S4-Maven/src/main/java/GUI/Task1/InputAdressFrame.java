package GUI.Task1;

import Task1.ClientRequest;
import Task1.Cortege;
import Task1.FileServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

// Главное окно приложения
public class InputAdressFrame extends JFrame implements PropertyChangeListener {
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
    private final int logFrameLengthTextInRow = 145, logFrameLengthTimeInRow =115, charLenghtInPx = 7;

    public InputAdressFrame() {
        frame = new JFrame("GxpExplorer");
        frame.setSize(900, 500);
        frame.setIconImage(new ImageIcon("FileManagerLogo.png").getImage());
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.decode("#081421"));
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
        panel.setBackground(Color.decode("#ECE9D8"));
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
        leftWrap.setBackground(Color.decode("#ECE9D8"));

        JPanel leftPan = new JPanel();
        leftPan.setLayout(new BoxLayout(leftPan, BoxLayout.Y_AXIS));

        leftPan.add(createFilePanel());    // панель с файловой таблицей
        leftPan.add(createTransferPanel()); // панель с кнопками передачи

        leftWrap.add(leftPan);
        return leftWrap;
    }

    // Панель с ComboBox и таблицей директорий
    private JPanel createFilePanel() {
        JPanel filepan = new JPanel();
        filepan.setBackground(Color.decode("#ECE9D8"));
        filepan.setLayout(new BoxLayout(filepan, BoxLayout.Y_AXIS));

        // ComboBox для выбора корневых директорий
        JPanel boxpan = new JPanel(new GridLayout(1, 1));
        boxpan.setBackground(Color.decode("#ECE9D8"));
        comboBox.setPreferredSize(new Dimension(290, 25));
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
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        scrollPane.setPreferredSize(new Dimension(290, 300));
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
        IPField.setText("localhost");

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
        IpPan.setBackground(Color.decode("#ECE9D8"));
        IpPan.add(address);
        IpPan.add(IPField);
        IpPan.add(ConnectDisconnect);

        return IpPan;
    }

    // Нижняя панель (включение/выключение сервера и выход)
    private JPanel createBottomPanel() {
        JPanel bottomPan = new JPanel(new GridLayout(1, 2));
        bottomPan.setBackground(Color.decode("#ECE9D8"));

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
        left.setBackground(Color.decode("#ECE9D8"));
        right.add(exitBtn);
        right.setBackground(Color.decode("#ECE9D8"));

        bottomPan.add(left);
        bottomPan.add(right);

        return bottomPan;
    }

    // Панель с кнопками передачи данных (заглушки)
    private JPanel createTransferPanel() {
        JPanel transferPan = new JPanel();
        transferPan.setBackground(Color.decode("#ECE9D8"));

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
        centerWrap.setBackground(Color.decode("#ECE9D8"));

        JPanel centerPan = new JPanel();
        centerPan.setBackground(Color.decode("#ECE9D8"));
        centerPan.setLayout(new BoxLayout(centerPan, BoxLayout.Y_AXIS));
        centerPan.setAlignmentX(Panel.LEFT_ALIGNMENT);

        JLabel CenterPanLabel = new JLabel(title);
        CenterPanLabel.setBackground(Color.decode("#ECE9D8"));
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
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(logFrameLengthTextInRow + logFrameLengthTimeInRow, 420));
        scrollPane.setBorder(BorderFactory.createBevelBorder(1));

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
        new InputAdressFrame();
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

        if(fileTable.getSelectedRow()!=-1) {
            selectedFile = (String)fileTableModel.getValueAt(fileTable.getSelectedRow(), 0);
            if (!selectedFile.equals("..")&& !selectedFile.equals(".")){
                selectedFile= selectedFile.substring(3);
            }
        }

        // обработка перехода на уровень корня ("..")
        if(selectedFile.equals(".")){
            selectedFile = "";
            for (int i = comboBox.getItemCount() - 1; i >= 0; i--) {
                String item = comboBox.getItemAt(i);
                if (item.equals(dir)) {
                    comboBox.setSelectedItem(item);
                }
            }
        }



        // обработка перехода на уровень выше ("..")
        if(selectedFile.equals("..") && selectedDir.length()>3){
            selectedFile = "";
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
        if (selectedFile.equals("..")) {return;}

        if(selectedFile.endsWith(".exe")||selectedFile.endsWith(".exe\\")){
            serverWrite(selectedDir+selectedFile);
            return;
        }
        if(!selectedFile.isEmpty()){
            selectedDir = selectedDir + selectedFile + '\\';
            comboBox.addItem(selectedDir);
            comboBox.setSelectedItem(comboBox.getItemAt(comboBox.getItemCount()-1));
        }
        fileTableModel.setRowCount(0);
        fileTableModel.addRow(new Object[]{"."});
        fileTableModel.addRow(new Object[]{".."});  // добавляем "наверх"
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
        switch (evt.getPropertyName()){
            case ("InClientMessage"):
            {
                if (evt.getOldValue()!=null) {
                    clientTableModel.addRow(new Object[]{"Клиент получил ответ:", ""});
                    int i = 1;
                    // разбиваем длинное сообщение на строки
                    for(String str: splitByLength(data.getData(), logFrameLengthTextInRow /charLenghtInPx)){
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
                    for(String str: splitByLength(data.getData(), logFrameLengthTextInRow/charLenghtInPx)){
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