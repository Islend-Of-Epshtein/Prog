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

    public ClientRequest(InetAddress address, int port) throws IOException {
        super(address, port);
        initInBuffer();
        initOutBuffer();
    }
    public void clientStringThread(){
        strIn = new Thread(() -> {
            while(true){
                try {
                    String str = Read();
                    System.out.println("Новое сообщение от сервера:" + str);
                    if (str == null) { break; }
                    Cortege newData = new Cortege(str, LocalTime.now(), isRoot(str));
                    messages.add(newData);

                    pcs.firePropertyChange("message", messages.size() , newData);
                }
                catch (Exception _) {
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
    public Cortege[] getRoots(){
        int i = 0, j=0;
        for (var message: messages){
            if(message.isRootElement()){
                i++;
            }
        }
        Cortege[] res = new Cortege[i];
        for (var message: messages){
            if(message.isRootElement()){
                res[j] = message;
                j++;
            }
        }
        return res;
    }

    @Override
    public void Off() throws IOException {
        super.Off();
        if(strIn!=null) { strIn.interrupt(); }
    }

}