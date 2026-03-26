package Base;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class Client {
    private Socket clientSocket;
    private PrintWriter in;
    private BufferedReader out;

    public Client() throws IOException
    {
        clientSocket = new Socket("localhost" , 0);
        in = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
    }
    public Client(InetAddress address, int port) throws IOException
    {
        clientSocket = new Socket(address , port);
    }

}
