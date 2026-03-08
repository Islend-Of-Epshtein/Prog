package Operators;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class OperatorInt extends Operator
{
    private final int pageLenght = 512, countelement = 1500;

    public OperatorInt(String filename, long size, String arrayType){
        super(filename, size, arrayType);
        int i = 0, j =0, n= 0;
        Random random = new Random();
        byte[] bytes = new byte[pageLenght];

        for(; i<countelement; i++){
            int a =  random.nextInt();
            byte[] convert = ByteBuffer.allocate(4).putInt(a).array();
            for(byte data: convert){
                if(j>=pageLenght){
                    j=0;
                    try{
                        getFile().writePage(n, bytes);
                        System.out.println("!!! - " + n);
                        n++;
                        bytes = new byte[pageLenght];
                    }
                    catch (Exception ex){
                        throw new RuntimeException("Ошибка инциализации начального массива: "+ ex.getMessage());
                    }
                }
                bytes[j] = data;
                j++;
            }

        }
        try{
            getFile().writePage(n, bytes);
        }
        catch (Exception ex){
            throw new RuntimeException("Ошибка инциализации начального массива: "+ ex.getMessage());
        }


        System.out.println(getFile().getFileStats());
    }
    /*
        {
            если файл не существует, создаёт требуемые файлы в режиме rw (читать, писать), за-писывает сигнатуру и заполняет его нулями (0);
            считывает заданное количество страниц (> =3), модифицируя атрибуты страниц (аб-солютный номер, статус, время записи);
        }
         */
}
