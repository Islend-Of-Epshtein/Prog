package Task1;

import Base.Client;

import java.io.IOException;
import java.net.*;
import java.net.InetSocketAddress;

public class ClientReqest extends Client
{
    private InetAddress serverAdress;
    private int serverPort;

    public ClientReqest(String address, int port) throws IOException {
        super(new InetSocketAddress(address, port).getAddress(), port);
    }
}
