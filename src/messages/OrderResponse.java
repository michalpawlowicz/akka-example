package messages;

import java.io.Serializable;

public class OrderResponse implements Serializable {
    private Type status;

    public OrderResponse(){
        status = Type.ERROR;
    }

    public Type getStatus() {
        return status;
    }

    public void setStatus(Type status) {
        this.status = status;
    }

    public enum Type implements Serializable {
        OK, ERROR
    }

    @Override
    public String toString() {
        return "| OrderResponse status: " + getStatus();
    }
}
