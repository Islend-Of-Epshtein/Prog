package Operators;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class OperatorInt extends Operator
{
    private final int pageLenght = 512, countelement = (int)getFile().getHeader().getArraySize();

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
        try{
            for(i=0; i<5; i++)
            {
                if (getFile().getHeader().getTotalPages() > i) {
                    getBuffer().loadPage(i);
                }

            }
        }
        catch (IOException ex){
            throw new RuntimeException("Ошибка заполнения буффера: " + ex.getMessage());
        }
    }

    public int NumPageByIndex(int index){
        return (int)(index/((float)pageLenght/4));
    }

    public int getValueByIndex(int index) {
        int page = NumPageByIndex(index);

        try{
            int inBuffer = getBuffer().loadPage(page);
            byte[] data = getBuffer().getPageData(inBuffer);
            int a = index;
            for(;a>pageLenght/4;a-=pageLenght/4);
            byte[] convert = new byte[4];
            for(int i =0; i<4; i++)
            {
                convert[i] = data[a+i];
            }
            return ByteBuffer.wrap(convert).getInt();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла: "+ e.getMessage());
        }
    }

    public void input(int index, int value)  {
        int a = NumPageByIndex(index);
        if(getBuffer().isPageLoaded(a))
        {
           write(index, value, a);
        }
        else{
            try{
                getBuffer().loadPage(a);
                write(index, value, a);
            }
            catch (Exception ex){
                throw new RuntimeException("Ошибка замены страницы в буфере: "+ ex.getMessage());
            }
        }
    }
    private void write(int index, int value, int page)
    {
        int b = getBuffer().findPageInBuffer(page);
        for(;index>pageLenght/4;index-=pageLenght/4);
        byte[] convert = ByteBuffer.allocate(4).putInt(value).array();
        getBuffer().writeToPage(b,index,convert);
        getBuffer().markPageDirty(page);
    }

}
