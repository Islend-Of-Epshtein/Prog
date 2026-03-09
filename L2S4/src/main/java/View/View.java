package View;

import Operators.Operator;
import Operators.OperatorInt;

public class View {
    public static void main(String[] args){
        String newfile = "newfile", Int = "I";
        OperatorInt operatorint = new OperatorInt(newfile, 150, Int);
        for(int i =0; i<150; i = i+10)
        {
            System.out.println("Элемент : " + i + " - "+ operatorint.getValueByIndex(i));
        }
    }
}
