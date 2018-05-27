package messages;

import java.io.Serializable;

public class StreamResponse implements Serializable {
    private String line;

    public StreamResponse(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return line;
    }
}
