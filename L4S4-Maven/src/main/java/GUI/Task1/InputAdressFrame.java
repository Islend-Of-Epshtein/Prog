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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class InputAdressFrame extends JFrame implements PropertyChangeListener {
    private final JFrame frame;
    private FileServer server;
    private ClientRequest client;
    private File[] roots;
    private JComboBox<String> comboBox = new JComboBox<>();
    private int port;
    private JTable textTable = new JTable();
    // Поля, к которым нужен доступ из разных методов
    private JButton ConnectDisconnect;
    private JTextField IPField;
    private String selectedFile="",selectedDir ="";
    private DefaultTableModel fileTableModel;  // модель таблицы для директорий
    private JTable fileTable;

    public InputAdressFrame() {
        frame = new JFrame("Input address");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.decode("#081421"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            port = getPort();
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(this, "Ошибка порта: " + e);
        }
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
        comboBox.addActionListener(e -> {
            if (comboBox.getSelectedItem() != null) {
                selectedDir = (String) comboBox.getSelectedItem();
                fileTableModel.setRowCount(0);
                fileTableModel.addRow(new Object[]{".."});
                fileTable.requestFocusInWindow();
            }
        });
        boxpan.setBorder(new EmptyBorder(0, 5, 10, 5));
        boxpan.add(comboBox);
        filepan.add(boxpan);

        // Таблица для отображения директорий
        JPanel tablePan = new JPanel(new GridLayout(1, 1));
        tablePan.setBorder(new EmptyBorder(0, 5, 0, 5));

        // Создаём модель таблицы с одним столбцом
        fileTableModel = new DefaultTableModel(0, 1) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // запрещаем редактирование
            }

            @Override
            public String getColumnName(int column) {
                return "";  // убираем название столбца
            }
        };

        fileTable = new JTable(fileTableModel);

        // Убираем заголовок таблицы
        fileTable.setTableHeader(null);

        // Левое выравнивание содержимого
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        fileTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        // Настройка выделения строк
        fileTable.setRowSelectionAllowed(true);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Убираем сетку
        fileTable.setShowGrid(false);
        fileTable.setIntercellSpacing(new Dimension(0, 0));
        fileTable.setRowHeight(20);

        // Обработка двойного клика
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    serverRequest();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        fileTableModel.addRow(new Object[]{".."});
        scrollPane.setPreferredSize(new Dimension(0, 300));
        tablePan.add(scrollPane);
        filepan.add(tablePan);

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
        IPField.setText("localhost");

        ConnectDisconnect = CreateButton("Подключиться", new Dimension(100, 25));
        ConnectDisconnect.addActionListener(e -> {
            if (ConnectDisconnect.getText().equals("Подключиться")) {
                try {
                    if(!connectServer(IPField.getText())){ return;}
                    client.clientStringThread();
                    ConnectDisconnect.setText("  Отключиться  ");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка подключения: "+ ex);
                }
            } else {
                try {
                    client.removePropertyChangeListener(this);
                    client.Off();
                    client = null;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка отключния: "+ ex);
                }
                ConnectDisconnect.setText("Подключиться");
            }
        });

        IPField.addActionListener(e -> {
            if (ConnectDisconnect.getText().equals("Подключиться")) {
                try {
                    if(!connectServer(IPField.getText())){ return;}
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка подключения: "+ ex);
                }
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
                try {
                    server = new FileServer(port);
                    TurnOnOff.setText("Выключить Сервер");
                }
                catch (Exception ex){
                     JOptionPane.showMessageDialog(this, "Ошибка включения: "+ ex);
                }
            } else {
                try {
                    server.Off();
                    server = null;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка выключения: "+ ex);
                }
                TurnOnOff.setText("  Включить Сервер  ");
            }
        });

        JButton exitBtn = CreateButton("Выход", new Dimension(80, 25));
        exitBtn.addActionListener(e -> {
            try {
                client.Off();
                server.Off();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            frame.dispose();
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
        });
        serverTransfer.addActionListener(e -> {
            serverRequest();
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

    public static void Run() {
        new InputAdressFrame();
    }

    private boolean connectServer(String address) throws IOException, ClassNotFoundException
    {
        if (server == null) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения: Сервер недоступен");
            return false;
        }

        if (Objects.equals(address, "")) {
            return false;
        }

        // 1. Создаём клиента (буферы инициализируются в конструкторе)
        client = new ClientRequest(new InetSocketAddress(address, port).getAddress(), port);

        // 2. Запускаем поток прослушивания
        client.clientStringThread();
        server.serverStringThread();
        // 3. Запускаем сервер в отдельном потоке
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

        client.addPropertyChangeListener("message", this);
        return true;
    }

    private int getPort() throws IOException {
        List<ServerSocket> sockets = new ArrayList<ServerSocket>();
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
                frame,                          // родительский компонент
                "Выберите порт:",                // сообщение
                "Порт соединения",               // заголовок
                JOptionPane.QUESTION_MESSAGE,   // тип сообщения
                null,                           // иконка
                freeport,                        // массив вариантов
                freeport[0]                      // значение по умолчанию
        );
    }
    private void serverRequest(){
        if (comboBox.getSelectedIndex() == -1) {return;}
        // передача серверу
        // редактируем путь и получаем от сервера следующую иерархию


        if(fileTable.getSelectedRow()!=-1) {selectedFile = (String)fileTableModel.getValueAt(fileTable.getSelectedRow(), 0);}
        else {
                selectedFile = "";
        }

        if(selectedFile.equals("..") && selectedDir.length()!=3){
            selectedFile = "";
            selectedDir = selectedDir.substring(0, selectedDir.lastIndexOf("\\"));
            selectedDir = selectedDir.substring(0, selectedDir.lastIndexOf("\\"));
        }

        String absolutePath = selectedDir + selectedFile;

        selectedDir = selectedDir + selectedFile + '\\';

        fileTableModel.setRowCount(0);
        fileTableModel.addRow(new Object[]{".."});

        try {
            client.Write(absolutePath, true);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(client == null) {return;}
        Cortege massage = (Cortege)evt.getNewValue();
        if(massage.isRootElement())
        {
            comboBox.addItem(massage.getData());
            return;
        }
        fileTableModel.addRow(new Object[]{massage.getData()});
    }
}