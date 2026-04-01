package GUI.Task1;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;

public class InputAdressFrame extends JFrame {
    private JFrame frame;
    public InputAdressFrame(){
        frame = new JFrame("Input address");
        frame.setSize(600,600);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.decode("#081421"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initElements();
        //frame.setResizable(false);
    }
    public void initElements(){
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Адрес сервера:");

        JTextField IPField = new JTextField(9);
        IPField.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                if (str == null) return;
                String currentText = getText(0, getLength());
                int newLength = currentText.length() + str.length();
                if (newLength <= 15) {
                    super.insertString(offs, str, a);
                }
            }
        });
        IPField.addActionListener(e -> {
            // Блокируем Enter, переноса строки не будет
            IPField.transferFocus();
        });

        JButton TurnOnOff = CreateButton("  Включить Сервер  ");
        TurnOnOff.addActionListener(e->{
            if (TurnOnOff.getText().equals   ("  Включить Сервер  ")) {
                TurnOnOff.setText("Выключить Сервер");
            }
            else{
                TurnOnOff.setText("  Включить Сервер  ");
            }
        });

        JButton ConnectDisconnect = CreateButton("Подключиться");
        ConnectDisconnect.addActionListener(e->{
            if (ConnectDisconnect.getText().equals("Подключиться")) {
                // подключаемся
                ConnectDisconnect.setText("  Отключиться  ");
            }
            else{
                // отключаемся
                ConnectDisconnect.setText("Подключиться");
            }
        });
        JPanel leftPan = new JPanel();
        leftPan.setLayout(new BoxLayout(leftPan, BoxLayout.Y_AXIS));
        leftPan.setPreferredSize(new Dimension(330, 0));

        JPanel leftTopPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftTopPan.setMaximumSize(new Dimension(Integer.MAX_VALUE, leftTopPan.getPreferredSize().height));
        leftTopPan.add(label);
        leftTopPan.add(IPField);
        leftTopPan.add(TurnOnOff);
        leftTopPan.add(ConnectDisconnect);

        JPanel filepan = new JPanel();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setControlButtonsAreShown(false);

        filepan.add(fileChooser);
        filepan.setPreferredSize(new Dimension(250, 200));

        JPanel bottomPan = new JPanel();
        JButton exitBtn = CreateButton("Выход");
        exitBtn.setSize(new Dimension(100, 40));
        exitBtn.addActionListener(e ->
        {
            frame.dispose();
        });
        bottomPan.add(exitBtn);

        JPanel rightPan = new JPanel(new BorderLayout());
        rightPan.setPreferredSize(new Dimension(250, 600));

        leftPan.add(leftTopPan);
        leftPan.add(filepan);
        leftPan.add(bottomPan);

        panel.add(leftPan, BorderLayout.WEST);
        rightPan.add(new Button("XYZ"), BorderLayout.CENTER);
        panel.add(rightPan, BorderLayout.EAST);
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(){
        //для SelectMenuFrame - не удалять
        new InputAdressFrame();
    }
    public static void Run(){
        //для SelectMenuFrame - не удалять
        new InputAdressFrame();
    }
    public static JButton CreateButton(String text){
        JButton button = new JButton(text);
        button.setBackground(Color.decode("#eeeeee"));
        //button.setFont(new Font("Arial", Font.PLAIN, 14));
        //button.setForeground(Color.decode("#081421"));
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }
}
