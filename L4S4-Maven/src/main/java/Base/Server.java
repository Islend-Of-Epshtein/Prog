package Base;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/// Шаблон сервера
public class Server {
    private final ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;
    private boolean In, Out;

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
    private void initOutBuffer() throws IOException {
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        Out = true;
    }
    public void initInBuffer() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        In = true;
    }
    /// Отправить сообщение, а затем получить ответ от сервера
    public String Write(String str) throws IOException {
        //отправляем
        out.write(str);
        out.flush();
        //ждем ответ
        return in.readLine();
    }
    /// Отправить сообщение, log необходимо установить в true
    public void Write(String str, boolean log) throws IOException {
        //отправляем если true
        if(log) {
            out.write(str);
            out.newLine();
            out.flush();
        }
    }

    /// Прочитать сообщение из буффера от сервера
    public String Read() throws IOException {
        return in.readLine();
    }
    /// ждать новое соединие
    public void Accept() throws IOException, ClassNotFoundException {
        clientSocket = serverSocket.accept();
        initOutBuffer();
        out.flush();
        initInBuffer();
    }
    /// ждать новое соединие
    public void Off() throws IOException {
        if(clientSocket!=null) {
            this.clientSocket.close();
            this.in.close();
            this.out.close();
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
    public BufferedReader getIn() { return in;}
}
