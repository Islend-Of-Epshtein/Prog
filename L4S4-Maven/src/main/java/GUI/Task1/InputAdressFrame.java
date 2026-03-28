package GUI.Task1;

import javax.swing.*;
import java.awt.*;

public class InputAdressFrame {
    public InputAdressFrame(){
        Frame frame = new Frame("Input address");
        frame.setSize(400,100);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.GRAY);

        JPanel panel = new JPanel();
        Label label = new Label("Адрес сервера:");
        TextField field = new TextField("localhost");
        panel.add(label, BorderLayout.EAST);
        panel.add(field, BorderLayout.WEST);frame.setVisible(true);
    }
    public static void Run(){
        //для SelectMenuFrame - не удалять
    }
}
