package GUI.Task1;


import Base.Client;
import Base.Server;
import Task1.ClientReqest;
import Task1.FileServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InputAdressFrame extends JFrame implements PropertyChangeListener {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private JFrame frame;
    private Server server;
    private Client client;
    private File[] roots;
    private JComboBox<String> comboBox;
    public void setFile(File[] files){
        var oldName = roots;
        roots =files;
        pcs.firePropertyChange("File", oldName, roots);
    }
    private int port;
    // Поля, к которым нужен доступ из разных методов
    private JButton ConnectDisconnect;
    private JTextField IPField;
    public boolean readObjectClient = false;
    public boolean readStringClient = false;
    public boolean readStringServer = false;

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
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
    public void initElements() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBackground(Color.decode("#ECE9D8"));
        panel.setBorder(new EmptyBorder(5, 5, 0, 5));

        panel.add(createLeftWrap());
        panel.add(createCenterWrap("Клиентская сторона"));
        panel.add(createCenterWrap("Серверная сторона"));

        this.addPropertyChangeListener(this);//Для comboBox с путями;


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
        IPField.setText("localhost");

        ConnectDisconnect = CreateButton("Подключиться", new Dimension(100, 25));
        ConnectDisconnect.addActionListener(e -> {
            if (ConnectDisconnect.getText().equals("Подключиться")) {
                try {
                    if(!connectServer(IPField.getText())){ return;}
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка подключения: "+ ex);
                }
                ConnectDisconnect.setText("  Отключиться  ");
            } else {
                try {
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
                    server = new Server(port);
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
    private Runnable createTask(int port) {
        return () -> {
            try {
                 FileServer reciver = new FileServer(port);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
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

    private boolean connectServer(String address) throws IOException, ClassNotFoundException {
        if(server==null) {JOptionPane.showMessageDialog(this, "Ошибка подключения к клиенту: Сервер недоступен"); return false;}
        Thread servLauncher = new Thread(() -> {
            try {
                server.Accept();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка подключения к клиенту: "+ e);
            }
        });
        servLauncher.start();
        if(!Objects.equals(address, "")){
            client = new Client(new InetSocketAddress(address, port).getAddress(), port);
            return true;
        }
        return false;
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

    private void clientObjectThread(ClientReqest client){
        Thread objIn = new Thread(() -> {
           while(true){
               try {
                   Object obj = client.Read(true);
                   if (obj == null) { break; }
                   this.roots = (File[]) obj;
               } catch (Exception e) {
                   JOptionPane.showMessageDialog(this, "Ошибка получения File[]: "+ e);
               }
           }
        });
        objIn.start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String[] paths = new String[roots.length];
        int i = 0;
        for(File file:  roots){
            paths[i] = file.getAbsolutePath();
            i++;
        }
        comboBox = new JComboBox<>(paths);
    }
}