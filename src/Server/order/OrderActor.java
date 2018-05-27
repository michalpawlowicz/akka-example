package Server.order;

import Server.FileSynchronizer;
import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import messages.OrderResponse;
import messages.Request;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.resume;

public class OrderActor extends AbstractActor {
    private final FileSynchronizer fileSynchronizer;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public OrderActor(FileSynchronizer fileSynchronizer) {
        this.fileSynchronizer = fileSynchronizer;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, r -> {
                    OrderResponse response = new OrderResponse();
                    if(save(r.getTitle())) {
                        response.setStatus(OrderResponse.Type.OK);
                    }
                    getSender().tell(response, getSelf());
                }).matchAny(o -> {
                    log.error("Invalid request, dropping ..");
                }).build();
    }

    private boolean save(String name) {
        return fileSynchronizer.append(name);
    }
}
