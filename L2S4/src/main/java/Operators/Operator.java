package Operators;

import BufferWorker.BufferWorker;
import BufferWorker.IBufferWorker;
import FileWorker.FileWorker;
import FileWorker.IFileWorker;

import java.io.IOException;
import java.util.Objects;

public abstract class Operator
{
        private final byte pageCount = 5;
        private final IBufferWorker buffer;
        private final IFileWorker file = new FileWorker();

        public IFileWorker getFile() {return file;}
        public IBufferWorker getBuffer() {return buffer;}

        // По заднию, для открытия или создания ( не нужен в тестировке )
        public Operator(String filename, long size, String arrayType){

                try{
                       file.open(filename);
                }
                catch (Exception ex1){
                        try{
                                file.initialize(filename , size, arrayType, 0);
                        }
                        catch (IOException ex2)
                        {
                                throw new RuntimeException("Ошибка создания файла: "+ ex2.getMessage());
                        }

                }
                buffer = new BufferWorker(file, pageCount);
        }
        // Конструктор для создания
        public Operator(String filename, long size, String arrayType, int lengthStr) {

                try{
                    if (Objects.equals(filename, "") || filename == null)
                    {throw new RuntimeException("Имя файла пустое!");}

                    file.initialize(filename , size, arrayType, lengthStr);
                }
                catch (IOException ex2)
                {
                        throw new RuntimeException("Ошибка создания файла: "+ ex2.getMessage());
                }
                buffer = new BufferWorker(file, pageCount);
        }
        //Конструктор для открытия файла
        public Operator(String filename) {

                try{
                    if (Objects.equals(filename, "") || filename == null) {throw new RuntimeException("Имя файла пустое!");}
                        file.open(filename);
                }
                catch (IOException ex2)
                {
                        throw new RuntimeException("Ошибка открытия файла: "+ ex2.getMessage());
                }
                buffer = new BufferWorker(file, pageCount);
        }
}
