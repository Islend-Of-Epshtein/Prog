import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/// Меню выбора задания (Task1, Task2, Task3)
public class SelectTaskMenu extends JFrame {
    static void main(String[] args){

        InitFrame();
    }
    public static void InitFrame(){

        JFrame frame = new JFrame("Select Task");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,100);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(1, 3, 0, 0)); // 1 строка, 3 колонки, горизонтальный отступ 10px
        JButton button1 = new JButton("Task1");
        JButton button2 = new JButton("Task2");
        JButton button3 = new JButton("Task3");

        panel.add(button1 );
        panel.add(button2);
        panel.add(button3);
        frame.add(panel);
        frame.setVisible(true);
    }
    public JButton CreateButton(String text){
        JButton button = new JButton(text);
        button.setBackground(Color.gray);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBorder(new BevelBorder(0, Color.RED, Color.black));
        return button;
    }
}