package Task1;

import Base.Server;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

import static Task1.Cortege.isRoot;

public class FileServer extends Server {
    private File file;
    private Thread pathIn;
    public FileServer(int port) throws IOException {
        super(port);
    }
    public FileServer() throws IOException {
        super();
    }
    public void serverStringThread(){
        pathIn = new Thread(() -> {
            while(true){
                try {
                    String str = Read();
                    System.out.println("Сервер принял:" + str);
                    if (str == null) { break; }
                    file = new File(str);
                    String[] result = new String[1];
                    if(file.getName().equals("..")){
                        file = new File(file.getParent());
                        result = file.list();
                    }
                    if(file.isFile()){
                        result[0] = Files.readString(Paths.get(file.getPath()));
                    }
                    if(file.isDirectory() || file.getName().equals("")){
                        result = file.list();
                    }
                    for(var res: result){
                        System.out.println("Сервер передал(ответ): "+ res);
                        super.Write(res, true);
                    }
                }
                catch (Exception _) {
                }
            }
        });
        pathIn.start();
    }
    @Override
    public void Accept() throws IOException, ClassNotFoundException {
        super.Accept();
        if(this.IsBound()){
            for (var item: File.listRoots()){
                System.out.println("Сервер передал: "+ item.getAbsolutePath());
                super.Write(item.getAbsolutePath(), true);
            }
        }
    }
    @Override
    public void Off() throws IOException {
        super.Off();
        pathIn.interrupt();
    }
}
