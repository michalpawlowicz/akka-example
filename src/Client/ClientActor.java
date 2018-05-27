package Client;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import messages.*;
import scala.concurrent.duration.Duration;

public class ClientActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static final String SERVER_PATH = "akka.tcp://book_store@127.0.0.1:2553/user/";

    private SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("30 seconds"), DeciderBuilder
            .matchAny(o -> SupervisorStrategy.restart())
            .build());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Response.class, r -> {
                    log.info("|-------------------------------------------------------");
                    log.info("| Got response");
                    log.info(r.toString());
                    log.info("|-------------------------------------------------------");
                }).match(Request.class, r -> {
                    log.info("|-------------------------------------------------------");
                    log.info("| Got request");
                    log.info(r.toString());
                    log.info("|-------------------------------------------------------");
                    if(r.getType() == Type.ORDER) {
                        getContext().actorSelection(SERVER_PATH + "store" + "/order")
                                .tell(r, getSelf());
                    } else if(r.getType() == Type.STREAM) {
                        getContext().actorSelection(SERVER_PATH + "store" + "/stream")
                                .tell(r, getSelf());
                    }
                    else {
                        getContext().actorSelection(SERVER_PATH + "store" + "/search")
                                .tell(r, getSelf());
                    }
                }).match(OrderResponse.class, r -> {
                    log.info("|-------------------------------------------------------");
                    log.info("| Got order response");
                    log.info(r.toString());
                    log.info("|-------------------------------------------------------");
                }).match(StreamResponse.class, r -> {
                    log.info("|-------------------------------------------------------");
                    log.info("| Stream ");
                    log.info(r.toString());
                    log.info("|-------------------------------------------------------");
                }).matchAny(o -> {
                    log.error("Invalid request, dropping ..");
                }).build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Client starts ..");
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }
}
