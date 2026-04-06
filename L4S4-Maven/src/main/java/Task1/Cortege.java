package Task1;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Cortege
{
    private final String data;
    private final LocalTime time;
    private final boolean root;
    public Cortege(String data, LocalTime time, boolean root){
        this.time = time;
        this.data = data;
        this.root = root;
    }
    public static File[] roots = File.listRoots();
    public static boolean isRoot(String str){
        File file = new File(str);
        for(File root : roots){
            if(root.equals(file)){ return true;}
        }
        return false;
    }
    public String getData(){ return data; }
    public String getTime(){ return time.format(DateTimeFormatter.ofPattern("HH:mm:ss")); }
    public boolean isRootElement(){ return root; }
}
