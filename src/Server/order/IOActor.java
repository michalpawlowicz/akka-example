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

import static akka.actor.SupervisorStrategy.restart;


public class IOActor extends AbstractActor {
    private FileSynchronizer fileSynchronizer;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public IOActor(FileSynchronizer fileSynchronizer) {
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
                    getContext().getParent().tell(response, getSender());
                }).matchAny(o -> {
                    log.error("Invalid request, dropping .. ");
                }).build();
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder.
            matchAny(o -> restart()).
            build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    private boolean save(String name) {
        return fileSynchronizer.append(name);
    }
}
