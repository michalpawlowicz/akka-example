package Server.search;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import messages.Request;
import messages.Response;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;

public class SearchActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String DB1 = "db/1";
    private final String DB2 = "db/2";

    private Map<Integer, Response> responseMap = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Request.class, r -> {
            context().child("searchChild1").get().forward(r, context());
            context().child("searchChild2").get().forward(r, context());
        }).match(Response.class, r -> {
            log.info("GOT RESPONSE!");
            if(handleResponse(r)) {
                getSender().tell(responseMap.get(r.getId()), getContext().getParent());
            }
        }).matchAny(o -> {
            log.info("-------------------------------------------------------");
            log.error("Invalid request, dropping ..");
            log.info("-------------------------------------------------------");
        }).build();

    }

    private boolean handleResponse(Response res) {
        if (responseMap.containsKey(res.getId())) {
            Response r = responseMap.get(res.getId());
            if (!r.getPrice().isPresent()) {
                responseMap.remove(res.getId());
                responseMap.put(res.getId(), res);
            }
            return true;
        } else {
            responseMap.put(res.getId(), res);
        }
        return false;
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder.
            matchAny(o -> restart()).
            build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        context().actorOf(Props.create(SearchChildActor.class, DB1), "searchChild1");
        context().actorOf(Props.create(SearchChildActor.class, DB2), "searchChild2");
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }
}
