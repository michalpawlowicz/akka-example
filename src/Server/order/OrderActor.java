package Server.order;

import Server.FileSynchronizer;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.OrderResponse;
import messages.Request;


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
                    context().actorOf(Props.create(IOActor.class, fileSynchronizer)).forward(r, context());
                }).match(OrderResponse.class, r -> {
                    getSender().tell(r, getSelf());
                }).matchAny(o -> {
                    log.error("Invalid request, dropping ..");
                }).build();
    }

    private boolean save(String name) {
        return fileSynchronizer.append(name);
    }
}
