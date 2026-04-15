package Task1;

import Base.Client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;

import static Task1.Cortege.isRoot;

/**
 * Расширение базового клиента с поддержкой PropertyChange и обработкой сообщений.
 *
 * @version 1.0
 */
public class ClientRequest extends Client {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Thread strIn;
    private volatile boolean running = true;

    /**
     * Конструктор: инициализирует сокет и буферы ввода/вывода.
     *
     * @param address IP-адрес сервера
     * @param port    порт подключения
     * @throws IOException если не удалось инициализировать соединение
     */
    public ClientRequest(InetAddress address, int port) throws IOException {
        super(address, port);
        initInBuffer();
        initOutBuffer();
    }

    /**
     * Запуск потока чтения сообщений от сервера.
     */
    public void clientStringThread() {
        strIn = new Thread(() -> {
            while (running) {
                try {
                    String str = Read();
                    if (str == null) {
                        break;
                    }
                } catch (Exception ignored) {
                    System.out.println("Прерывание потока чтения клиента");
                    break;
                }
            }
        });
        strIn.start();
    }

    /**
     * Регистрация слушателя событий.
     *
     * @param propertyName имя свойства
     * @param listener     слушатель
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Удаление слушателя событий.
     *
     * @param listener слушатель
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Переопределённый метод отключения - останавливает поток чтения.
     *
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public void Off() throws IOException {
        super.Off();
        if (strIn != null) {
            running = false;
            strIn.interrupt();
        }
    }

    /**
     * Отправка строки на сервер с генерацией события.
     *
     * @param str строка для отправки
     * @param log флаг логирования
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public void Write(String str, boolean log) throws IOException {
        Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
        pcs.firePropertyChange("OutClientMessage", newData.getData().length(), newData);
        super.Write(str, true);
    }

    /**
     * Чтение строки от сервера с разбором и генерацией событий.
     *
     * @return прочитанная строка
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public String Read() throws IOException {
        String str = getIn().readLine();
        if (str != null) {
            String[] array = str.split(" {2}");
            Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
            pcs.firePropertyChange("InClientMessage", 1, newData);

            for (String arr : array) {
                Cortege arrData = new Cortege(arr, LocalDateTime.now(), isRoot(arr));
                pcs.firePropertyChange("InClientMessage", null, arrData);
            }
        }
        return str;
    }
}