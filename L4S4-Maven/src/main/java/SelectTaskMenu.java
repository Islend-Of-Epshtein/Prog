

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/// Меню выбора задания (Task1, Task2, Task3)
public class SelectTaskMenu extends JFrame {
    private static JFrame frame = new JFrame("Select Task");;
    static void main(String[] args){
        InitFrame();
    }
    public static void InitFrame(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,80);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.GRAY);
        JPanel panel = new JPanel(new GridLayout(1, 3, 1, 0)); // 1 строка, 3 колонки
        JButton button1 = CreateButton("Task №1");
        JButton button2 = CreateButton("Task №2");
        JButton button3 = CreateButton("Task №3");

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                GUI.Task1.InputAdressFrame.Run();
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                GUI.Task2.ConnectionFrame.Run();
            }
        });
        button3.addActionListener(e -> {
            frame.dispose();
            GUI.Task3.Launcher.run(null);
        });

        panel.add(button1 );
        panel.add(button2);
        panel.add(button3);

        frame.add(panel);

        frame.setVisible(true);
    }
    public static JButton CreateButton(String text){
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 130, 180));
        button.setFont(new Font("Calibri", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);

        Border paddingBorder = BorderFactory.createEmptyBorder(15, 20, 10, 20);
        button.setBorder(paddingBorder);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 149, 237));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        return button;
    }
}