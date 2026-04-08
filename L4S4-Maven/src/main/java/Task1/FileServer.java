package Task1;

import Base.Server;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static Task1.Cortege.isRoot;

// Сервер для работы с файловой системой
public class FileServer extends Server
{
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);  // поддержка событий
    private File file;           // текущий файл/директория
    private Thread pathIn;       // поток для чтения запросов от клиента
    private volatile boolean running = true;  // флаг работы потока

    public FileServer(int port) throws IOException {
        super(port);
    }
    public FileServer() throws IOException {
        super();
    }

    // Регистрация слушателя событий
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    // Удаление слушателя событий
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    // Запуск потока обработки запросов от клиента
    public void serverStringThread(){
        pathIn = new Thread(() -> {
            while(running){
                try {
                    String str = Read();  // читаем запрос (путь к файлу/директории)
                    if (str == null) { break; }

                    file = new File(str);
                    String[] result = new String[1];

                    // Обработка перехода на уровень выше
                    if(file.getName().equals("..")){
                        file = new File(file.getParent());
                        result = file.list();
                    }

                    // Запуск exe
                    if(file.getName().endsWith(".exe") ||file.getName().endsWith(".exe\\")){
                        System.out.println(file);
                         ProcessBuilder pcb =  new ProcessBuilder("cmd.exe", "/c", "start", file.toString());
                         pcb.start();
                         continue;
                    }
                    // Если это файл - читаем его содержимое
                    if(file.isFile()){
                        if (result != null) {
                            result[0] = Files.readString(Paths.get(file.getPath()));
                        }
                    }

                    // Если это директория - получаем список содержимого
                    if(file.isDirectory() || file.getName().isEmpty()){
                        result = file.list();
                    }

                    // Формируем ответ (список файлов через пробел)
                    StringBuilder res = new StringBuilder();
                    assert result != null;
                    for(var f : result){
                        res.append("  ").append(f);
                    }
                    Write(res.toString(), true);  // отправляем ответ клиенту
                }
                catch (Exception _) {
                }
            }
        });
        pathIn.start();
    }

    // Принятие соединения и отправка списка корневых дисков
    @Override
    public void Accept() throws IOException, ClassNotFoundException {
        super.Accept();
        if(this.IsBound()){
            StringBuilder str = new StringBuilder();
            for(var root : File.listRoots()){
                str.append("  ").append(root.toString());
            }
            Write(str.toString(), true);
        }
    }

    // Остановка сервера и потока обработки
    @Override
    public void Off() throws IOException {
        super.Off();
        if(pathIn!=null){
            running = false;    // останавливаем цикл
            pathIn.interrupt(); // прерываем поток
        }

    }

    // Отправка строки с генерацией события
    @Override
    public void Write(String str, boolean log) throws IOException {
        if(str!=null)
        {
            Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
            pcs.firePropertyChange("OutServerMessage", newData.getData().length(), newData);
        }
        super.Write(str, log);
    }

    // Чтение строки с генерацией события
    @Override
    public String Read() throws IOException {
        String str = getIn().readLine();
        Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
        pcs.firePropertyChange("InServerMessage", str.length(), newData);
        return str;
    }
}