package messages;

import java.io.Serializable;

public class Request implements Serializable {

    private static int baseId = 0;

    public Request() {
        id = baseId++;
    }

    private Type type;
    private String title;
    private int id;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "| Type: " + type + "; Title: " + title;
    }
}
