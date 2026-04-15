package Task1;

import Base.Server;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static Task1.Cortege.isRoot;

/**
 * Сервер для работы с файловой системой.
 */
public class FileServer extends Server {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private File file;
    private Thread pathIn;
    private volatile boolean running = true;

    public FileServer(int port) throws IOException {
        super(port);
    }

    public FileServer() throws IOException {
        super();
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Запуск потока обработки запросов от клиента.
     */
    public void serverStringThread() {
        pathIn = new Thread(() -> {
            while (running) {
                try {
                    String str = Read();
                    if (str == null) {
                        break;
                    }

                    file = new File(str);
                    String[] result = new String[1];

                    if (file.getName().equals("..")) {
                        file = new File(file.getParent());
                        result = file.list();
                    }

                    if (file.getName().endsWith(".exe") || file.getName().endsWith(".exe\\")) {
                        System.out.println(file);
                        ProcessBuilder pcb = new ProcessBuilder("cmd.exe", "/c", "start", file.toString());
                        pcb.start();
                        continue;
                    }

                    if (file.isFile()) {
                        openWithNotepad(file.getPath());
                        if (result != null) {
                            result[0] = Files.readString(Paths.get(file.getPath()));
                        }
                    }

                    if (file.isDirectory() || file.getName().isEmpty()) {
                        result = file.list();
                    }

                    StringBuilder res = new StringBuilder();
                    assert result != null;
                    for (String f : result) {
                        res.append("  ").append(f);
                    }
                    Write(res.toString(), true);
                } catch (Exception ignored) {
                    // Игнорируем исключения в цикле
                }
            }
        });
        pathIn.start();
    }

    private void openWithNotepad(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("notepad.exe", filePath);
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Accept() throws IOException, ClassNotFoundException {
        super.Accept();
        if (this.IsBound()) {
            StringBuilder str = new StringBuilder();
            for (File root : File.listRoots()) {
                str.append("  ").append(root.toString());
            }
            Write(str.toString(), true);
        }
    }

    @Override
    public void Off() throws IOException {
        super.Off();
        if (pathIn != null) {
            running = false;
            pathIn.interrupt();
        }
    }

    @Override
    public void Write(String str, boolean log) throws IOException {
        if (str != null) {
            Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
            pcs.firePropertyChange("OutServerMessage", newData.getData().length(), newData);
        }
        super.Write(str, log);
    }

    @Override
    public String Read() throws IOException {
        String str = getIn().readLine();
        Cortege newData = new Cortege(str, LocalDateTime.now(), isRoot(str));
        pcs.firePropertyChange("InServerMessage", str.length(), newData);
        return str;
    }
}