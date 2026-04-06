package Task1;

import Base.Server;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

import static Task1.Cortege.isRoot;

public class FileServer extends Server
{
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private File file;
    private Thread pathIn;
    private volatile boolean running = true;
    public FileServer(int port) throws IOException {
        super(port);
    }
    public FileServer() throws IOException {
        super();
    }
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    public void serverStringThread(){
        pathIn = new Thread(() -> {
            while(running){
                try {
                    String str = Read();
                    if (str == null) { break; }
                    file = new File(str);
                    String[] result = new String[1];
                    if(file.getName().equals("..")){
                        file = new File(file.getParent());
                        result = file.list();
                    }
                    if(file.isFile()){
                        if (result != null) {
                            result[0] = Files.readString(Paths.get(file.getPath()));
                        }
                    }
                    if(file.isDirectory() || file.getName().isEmpty()){
                        result = file.list();
                    }

                    assert result != null;
                    for(String res: result){
                        Write(res, true);
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
            for (File item: File.listRoots()){
                Cortege outWithTime = new Cortege(item.getAbsolutePath(), LocalTime.now(), true);
                pcs.firePropertyChange("OutServerMassage", outWithTime.isRootElement(), outWithTime);
                super.Write(item.getAbsolutePath(), true);
            }
        }
    }
    @Override
    public void Off() throws IOException {
        super.Off();
        if(pathIn!=null){ running = false; pathIn.interrupt(); }
    }
    @Override
    public void Write(String str, boolean log) throws IOException {
        Cortege newData = new Cortege(str, LocalTime.now(), isRoot(str));
        pcs.firePropertyChange("OutServerMessage", newData.getData().length() , newData);
        super.Write(str, log);
    }
    @Override
    public String Read() throws IOException {
        String str = getIn().readLine();
        Cortege newData = new Cortege(str, LocalTime.now(), isRoot(str));
        pcs.firePropertyChange("InServerMessage", str.length(), newData);
        return str;
    }
}
