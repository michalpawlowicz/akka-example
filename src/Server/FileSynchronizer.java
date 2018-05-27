package Server;

import java.io.*;

public class FileSynchronizer {
    private final File file;
    public FileSynchronizer(String fname) {
        this.file = new File(fname);
    }

    public boolean append(String string) {
        boolean res;
        synchronized (file) {
            try (Writer bw = new BufferedWriter(new FileWriter("db/orders.txt", true))) {
                bw.append(string + "\n");
                res = true;
            } catch (IOException e) {
                e.printStackTrace();
                res = false;
            }
        }
        return res;
    }
}
