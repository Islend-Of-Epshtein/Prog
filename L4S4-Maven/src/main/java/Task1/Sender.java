package Task1;

import Base.Client;

import java.io.IOException;
import java.net.*;
import java.net.InetSocketAddress;

public class Sender extends Client
{
    private InetAddress server;
    private int serverPort;

    public Sender(String address, int port) throws IOException {
        super(new InetSocketAddress(address, port).getAddress(), port);
    }
    public boolean ConnectWithServer(){
        return this.IsBound();
    }
}
