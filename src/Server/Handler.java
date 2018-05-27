package Server;

import Server.order.OrderActor;
import Server.stream.StreamActor;
import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import messages.Request;
import Server.search.SearchActor;
import messages.Response;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.resume;


public class Handler extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final FileSynchronizer fileSynchronizer = new FileSynchronizer("db/orders.txt");
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, (r) -> {
                    log.info("Received request: " + r.getType());
                    switch (r.getType()) {
                        case SEARCH:
                            context().child("search").get().forward(r, context());
                        case ORDER:
                            context().child("order").get().forward(r, context());
                            break;
                        case STREAM:
                            context().child("stream").get().forward(r, context());
                            break;
                        default:
                            log.error("Invalid type, dropping ..");
                            break;
                    }
                }).matchAny(o -> {
                    log.error("Unknown request, dropping ..");
                }).build();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        System.out.println("Handler starts");
        context().actorOf(Props.create(SearchActor.class), "search");
        context().actorOf(Props.create(OrderActor.class, fileSynchronizer), "order");
        context().actorOf(Props.create(StreamActor.class), "stream");
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder.
            matchAny(o -> resume()).
            build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("Handler stops");
    }
}
