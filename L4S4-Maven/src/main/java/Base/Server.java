package Base;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/// Шаблон сервера
public class Server {
    private final ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectInputStream objIn;
    private ObjectOutputStream objOut;

    /// Используем свободный порт и локальный адрес
    public Server() throws IOException
    {
        serverSocket = new ServerSocket(0);
    }
    /// Конструктор с кастомными адресом и портом
    public Server(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
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
    /// Отправить сообщение, log необходимо установить в true
    public void Write(String str, boolean log) throws IOException {
        //отправляем если true
        if(log) { out.write(str); }
    }
    public Object Write(Object obj) throws IOException, ClassNotFoundException {
        //отправляем
        objOut.writeObject(obj);
        objOut.flush();
        //ждем ответ
        return objIn.readObject();
    }

    /// Прочитать сообщение из буффера от сервера
    public String Read() throws IOException {
        return in.readLine();
    }
    /// log = true для чтения объекта из буфера
    public Object Read(boolean log) throws ClassNotFoundException, IOException {
        if(log) {return objIn.readObject(); }
        return null;
    }
    /// ждать новое соединие
    public void Accept() throws IOException, ClassNotFoundException {
        clientSocket = serverSocket.accept();
        initBuffer();
    }
    /// ждать новое соединие
    public void Off() throws IOException {
        if(clientSocket!=null) {
            this.clientSocket.close();
            this.in.close();
            this.objIn.close();
            this.out.close();
            this.objOut.close();
        }
        serverSocket.close();
    }
    /// Проверяет, установлено ли соединение
    public boolean IsBound(){ return serverSocket.isBound(); }
    /// Получает адрес сервеара
    public String GetAddress() { return serverSocket.getInetAddress().toString(); }
    /// Порт сервера, с которым соединен сервер
    public int GetPort() { return clientSocket.getPort(); }
    /// Локальный порт данного сокета
    public int GetLocalPort() { return clientSocket.getLocalPort(); }
    /// Для любой другой работы с сокетом
    public Socket GetSocket(){
        return clientSocket;
    }
}
