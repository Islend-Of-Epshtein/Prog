package Base;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/// Шаблон клианта
public class Client {
    private final Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;
    private boolean In, Out;
    /// Используем свободный порт и локальный адрес
    public Client() throws IOException
    {
        clientSocket = new Socket("localhost" , 0);
    }
    /// Конструктор с кастомными адресом и портом
    public Client(InetAddress address, int port) throws IOException
    {
        clientSocket = new Socket(address , port);
    }
    public void initOutBuffer() throws IOException {
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
    public void Off() throws IOException {
        this.clientSocket.close();
        this.in.close();
        this.out.close();
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
    public BufferedReader getIn() { return in;}
    public BufferedWriter isOutInit() { return out;}
}
