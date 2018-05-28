package Server.search;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;
import akka.japi.pf.DeciderBuilder;
import messages.Request;
import messages.Response;
import scala.concurrent.duration.Duration;
import java.util.HashMap;
import java.util.Map;
import static akka.actor.SupervisorStrategy.restart;

public class SearchActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String DB1 = "db/1";
    private final String DB2 = "db/2";

    private Map<Integer, Response> responseMap = new HashMap<>();
    private Map<ActorRef, ActorRef> actorRefMap = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Request.class, r -> {
            ActorRef first = context().actorOf(Props.create(SearchChildActor.class, DB1));
            first.forward(r, context());
            ActorRef second = context().actorOf(Props.create(SearchChildActor.class, DB2));
            second.forward(r, context());
            actorRefMap.put(first, second);
            actorRefMap.put(second, first);
        }).match(Response.class, r -> {
            log.info("GOT RESPONSE!");
            if(handleResponse(r, getSelf())) {
                getSender().tell(responseMap.get(r.getId()), getSelf());
                responseMap.remove(r.getId());
            }
        }).matchAny(o -> {
            log.info("-------------------------------------------------------");
            log.error("Invalid request, dropping ..");
            log.info("-------------------------------------------------------");
        }).build();

    }

    private boolean handleResponse(Response res, ActorRef ref) {
        if (responseMap.containsKey(res.getId())) {
            Response r = responseMap.get(res.getId());
            if (!r.getPrice().isPresent()) {
                responseMap.remove(res.getId());
                responseMap.put(res.getId(), res);
                if(actorRefMap.containsKey(ref)) {
                    ActorRef sec = actorRefMap.get(ref);
                    context().stop(actorRefMap.get(ref));
                    actorRefMap.remove(ref);
                    if(actorRefMap.containsKey(sec)) {
                        actorRefMap.remove(sec);
                    }
                }
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
}
