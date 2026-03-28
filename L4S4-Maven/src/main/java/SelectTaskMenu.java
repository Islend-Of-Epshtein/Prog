

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import GUI.Task1.InputAdressFrame.*;
import GUI.Task2.ConnectionFrame.*;
import GUI.Task3.ConnectionFrame.*;

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
            GUI.Task3.ConnectionFrame.Run();
        });

        panel.add(button1 );
        panel.add(button2);
        panel.add(button3);

        frame.add(panel);

        frame.setVisible(true);
    }
    public static JButton CreateButton(String text){
        JButton button = new JButton(text);
        button.setBackground(Color.decode("#d1d1d1"));
        button.setFont(new Font("Calibri", Font.BOLD, 16));
        button.setForeground(Color.decode("#081421"));

        Border bevelBorder = new BevelBorder(0, Color.BLACK, Color.BLACK);
        Border paddingBorder = BorderFactory.createEmptyBorder(15, 20, 10, 20);
        button.setBorder(BorderFactory.createCompoundBorder(bevelBorder, paddingBorder));
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setPreferredSize(new Dimension(button.getWidth()*3/2, button.getHeight()*3/2));
                button.setForeground(Color.decode("#052e57"));
                button.setBackground(Color.white);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setPreferredSize(new Dimension(button.getWidth(), button.getHeight()));
                button.setBackground(Color.decode("#d1d1d1"));
                button.setForeground(Color.decode("#081421"));
            }
        });
        return button;
    }
}