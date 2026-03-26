import java.awt.*;

/// Меню выбора задания (Task1, Task2, Task3)
public class SelectTaskMenu {
    static void main(String[] args){
        InitFrame();
    }
    public static void InitFrame(){
        Frame frame = new Frame("Select Task");
        frame.setSize(400,100);
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.GRAY);
        Container container = new Container();

        frame.setVisible(true);
    }
}
