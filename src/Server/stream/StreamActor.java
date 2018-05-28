package Server.stream;

import Server.order.IOActor;
import akka.NotUsed;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import messages.Request;
import messages.StreamResponse;
import scala.collection.Iterator;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.io.Codec;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.restart;

public class StreamActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, r -> {
                    context().actorOf(Props.create(streamer.class)).forward(r, context());
                }).matchAny(o -> log.info("Received unknown message."))
                .build();
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
