package Task1;

import Base.Client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.time.*;

import static Task1.Cortege.isRoot;

public class ClientRequest extends Client
{
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final List<Cortege> messages = new ArrayList<>();
    private Thread strIn;
    private volatile boolean running = true;

    public ClientRequest(InetAddress address, int port) throws IOException {
        super(address, port);
        initInBuffer();
        initOutBuffer();
    }
    public void clientStringThread(){
        strIn = new Thread(() -> {
            while(running){
                try {
                    String str = Read();
                    if (str == null) { break; }
                }
                catch (Exception _) {
                    System.out.println("Прерывание потока чтения клианта");
                    break;
                }
            }
        });
        strIn.start();
    }
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void Off() throws IOException {
        super.Off();
        if(strIn!=null) { running = false; strIn.interrupt(); }
    }
    @Override
    public void Write(String str, boolean log) throws IOException {
        Cortege newData = new Cortege(str, LocalTime.now(), isRoot(str));
        pcs.firePropertyChange("OutClientMessage", newData.getData().length() , newData);
        super.Write(str, true);
    }
    @Override
    public String Read() throws IOException {
        String str = getIn().readLine();
        Cortege newData = new Cortege(str, LocalTime.now(), isRoot(str));
        pcs.firePropertyChange("InClientMessage", str.length(), newData);
        messages.add(newData);
        return str;
    }
}