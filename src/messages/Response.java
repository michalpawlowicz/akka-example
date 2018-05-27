package messages;

import java.io.Serializable;
import java.util.Optional;

public class Response implements Serializable {
    private String price;
    private String title;
    private int id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Optional<String> getPrice() {
        return (price == null) ? Optional.empty() : Optional.of(price);
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "| Title: " + title+ "; Price: " + ((getPrice().isPresent()) ? getPrice().get() : "NONE");
    }
}
