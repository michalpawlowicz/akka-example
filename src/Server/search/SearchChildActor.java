package Server.search;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.Request;
import messages.Response;
import java.io.*;

public class SearchChildActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private String path;
    private File file;

    public SearchChildActor(String path) {
        this.path = path;
        this.file = new File(path);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Request.class, r -> {
            log.info("-------------------------------------------------------");
            log.info("Got request");
            log.info(r.toString());
            log.info("-------------------------------------------------------");
            String price = read(r);
            Response response = new Response();
            response.setPrice(price);
            response.setTitle(r.getTitle());
            response.setId(r.getId());
            getContext().getParent().tell(response, getSender());
        }).matchAny(o -> {
            log.info("-------------------------------------------------------");
            log.error("Invalid request, dropping ..");
            log.info("-------------------------------------------------------");

        }).build();
    }

    private String read(Request request) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] ar = line.toLowerCase().split(";");
                if(ar[0].equals(request.getTitle())){
                    return ar[1];
                }
            }
        } catch (IOException e) {}
        return null;
    }
}
