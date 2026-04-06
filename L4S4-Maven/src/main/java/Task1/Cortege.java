package Task1;

import java.io.File;
import java.time.LocalTime;

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
        for(var root : roots){
            if(root.equals(file)){ return true;}
        }
        return false;
    }
    public String getData(){ return data; }
    public LocalTime getTime(){ return time; }
    public boolean isRootElement(){ return root; }
}
