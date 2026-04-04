package Task1;

import Base.Client;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.InetSocketAddress;

public class ClientReqest extends Client implements PropertyChangeListener
{
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private InetAddress serverAdress;
    private int serverPort;
    private File[] roots;
    public void setRoots(File[] _roots){
        var oldName = roots;
        roots = _roots;
        pcs.firePropertyChange("File", oldName, roots);
    }

    public ClientReqest(String address, int port) throws IOException {
        super(new InetSocketAddress(address, port).getAddress(), port);
    }
    public void clientObjectThread(ClientReqest client){
        Thread objIn = new Thread(() -> {
            while(true){
                try {
                    Object obj = client.Read(true);
                    if (obj == null) { break; }
                } catch (Exception e) {
                }
            }
        });
        objIn.start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
