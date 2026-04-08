package Task1;

import Base.Client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetAddress;
import java.time.*;

import static Task1.Cortege.isRoot;

// Расширение базового клиента с поддержкой PropertyChange и обработкой сообщений
public class ClientRequest extends Client
{
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);  // поддержка событий
    //private final List<Cortege> messages = new ArrayList<>();  // хранилище полученных сообщений
    private Thread strIn;          // поток для чтения сообщений от сервера
    private volatile boolean running = true;  // флаг работы потока

    // Конструктор: инициализирует сокет и буферы ввода/вывода
    public ClientRequest(InetAddress address, int port) throws IOException {
        super(address, port);
        initInBuffer();   // инициализация входного буфера
        initOutBuffer();  // инициализация выходного буфера
    }

    // Запуск потока чтения сообщений от сервера
    public void clientStringThread(){
        strIn = new Thread(() -> {
            while(running){
                try {
                    String str = Read();  // читаем строку от сервера
                    if (str == null) { break; }
                }
                catch (Exception _) {
                    System.out.println("Прерывание потока чтения клиента");
                    break;
                }
            }
        });
        strIn.start();
    }

    // Регистрация слушателя событий
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    // Удаление слушателя событий
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    // Переопределённый метод отключения - останавливает поток чтения
    @Override
    public void Off() throws IOException {
        super.Off();
        if(strIn!=null) {
            running = false;    // останавливаем цикл
            strIn.interrupt();  // прерываем поток
        }
    }

    // Отправка строки на сервер с генерацией события
    @Override
    public void Write(String str, boolean log) throws IOException {
        Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
        pcs.firePropertyChange("OutClientMessage", newData.getData().length(), newData);
        super.Write(str, true);  // отправляем строку
    }

    // Чтение строки от сервера с разбором и генерацией событий
    @Override
    public String Read() throws IOException {
        String str = getIn().readLine();  // читаем строку
        if(str!=null) {
            String[] array = str.split(" {2}");  // разбиваем по пробелам
            Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
            pcs.firePropertyChange("InClientMessage", 1, newData);

            // Для каждого слова создаём отдельное событие
            for (String arr : array) {
                Cortege arrData = new Cortege(arr, LocalDateTime.now(), isRoot(arr));
                //messages.add(arrData);
                pcs.firePropertyChange("InClientMessage", null, arrData);
            }
        }
        return str;
    }
}