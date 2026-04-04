package Task1;

import Base.Server;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FileServer extends Server {
    private File file;
    public FileServer(int port) throws IOException {
        super(port);
    }
    public FileServer() throws IOException {
        super();
    }
    @Override
    public void Accept() throws IOException, ClassNotFoundException {
        super.Accept();
        if(this.IsBound()){
            super.Write(File.listRoots(), true);
            super.Write((Object)null, true);
        }
    }
}
