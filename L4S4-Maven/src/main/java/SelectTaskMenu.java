import GUI.Task1.InputAddressFrame;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Меню выбора задания (Task1, Task2, Task3).
 */
public class SelectTaskMenu extends JFrame {

    private static final JFrame FRAME = new JFrame("Select Task");

    public static void main(String[] args) {
        initFrame();
    }

    public static void initFrame() {
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FRAME.setSize(400, 80);
        FRAME.setLocationRelativeTo(null);
        FRAME.setBackground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(1, 3, 1, 0));
        JButton button1 = createButton("Task №1");
        JButton button2 = createButton("Task №2");
        JButton button3 = createButton("Task №3");

        button1.addActionListener(e -> {
            FRAME.dispose();
            InputAddressFrame.run();
        });
        button2.addActionListener(e -> {
            FRAME.dispose();
            GUI.Task2.ConnectionFrame.run();
        });
        button3.addActionListener(e -> {
            FRAME.dispose();
            GUI.Task3.Launcher.run(null);
        });

        panel.add(button1);
        panel.add(button2);
        panel.add(button3);

        FRAME.add(panel);
        FRAME.setVisible(true);
    }

    public static JButton createButton(String text) {
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