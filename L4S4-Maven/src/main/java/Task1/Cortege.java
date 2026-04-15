package Task1;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Cortege {

    private static final File[] ROOTS = File.listRoots();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yy:MM:dd HH:mm:ss");

    private final String data;
    private final LocalDateTime time;
    private final boolean root;

    public Cortege(String data, LocalDateTime time, boolean root) {
        this.time = time;
        this.data = data;
        this.root = root;
    }

    public static boolean isRoot(String str) {
        File file = new File(str);
        for (File root : ROOTS) {
            if (root.equals(file)) {
                return true;
            }
        }
        return false;
    }

    public String getData() {
        return data;
    }

    public String getTime() {
        return time.format(FORMATTER);
    }

    public boolean isRootElement() {
        return root;
    }
}