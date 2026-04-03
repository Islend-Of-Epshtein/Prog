package Base;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/// Шаблон клианта
public class Client {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectInputStream objIn;
    private ObjectOutputStream objOut;
    /// Используем свободный порт и локальный адрес
    public Client() throws IOException
    {
        clientSocket = new Socket("localhost" , 0);
        initBuffer();
    }
    /// Конструктор с кастомными адресом и портом
    public Client(InetAddress address, int port) throws IOException
    {
        clientSocket = new Socket(address , port);
        initBuffer();
    }
    private void initBuffer() throws IOException {
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        objOut = new ObjectOutputStream(clientSocket.getOutputStream());
        objIn = new ObjectInputStream(clientSocket.getInputStream());
    }

    /// Отправить сообщение, а затем получить ответ от сервера
    public String Write(String str) throws IOException {
        //отправляем
        out.write(str);
        //ждем ответ
        return in.readLine();
    }
    public Object Write(Object obj) throws IOException, ClassNotFoundException {
        //отправляем
        objOut.writeObject(obj);
        objOut.flush();
        //ждем ответ
        return objIn.readObject();
    }

    /// Отправить сообщение, log необходимо установить в true
    public void Write(String str, boolean log) throws IOException {
        //отправляем если true
        if(log) { out.write(str); }
    }

    /// Прочитать сообщение из буффера от сервера
    public String Read() throws IOException {
        return in.readLine();
    }
    public Object Read(boolean log) throws ClassNotFoundException, IOException {
        if(log) {return objIn.readObject(); }
        return null;
    }

    public void Off() throws IOException {
        this.clientSocket.close();
        this.in.close();
        this.objIn.close();
        this.out.close();
        this.objOut.close();
    }
    /// Проверяет, установлено ли соединение
    public boolean IsBound(){ return clientSocket.isBound(); }
    /// Получает адрес сокета
    public String GetAddress() { return clientSocket.getInetAddress().toString(); }
    /// Порт сервера, с которым соединен сокет
    public int GetPort() { return clientSocket.getPort(); }
    /// Локальный порт данного сокета
    public int GetLocalPort() { return clientSocket.getLocalPort(); }
    /// Для любой другой работы с сокетом
    public Socket GetSocket(){
        return clientSocket;
    }
}
