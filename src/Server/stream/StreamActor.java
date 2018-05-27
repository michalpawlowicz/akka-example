package Server.stream;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import messages.Request;
import messages.StreamResponse;
import scala.collection.Iterator;
import scala.concurrent.duration.FiniteDuration;
import scala.io.Codec;

import java.util.concurrent.TimeUnit;

public class StreamActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, r -> {
                    ActorMaterializer materializer = ActorMaterializer.create(context());
                    ActorRef run = Source.actorRef(1, OverflowStrategy.dropNew())
                            .throttle(1, FiniteDuration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping())
                            .to(Sink.actorRef(getSender(), NotUsed.getInstance()))
                            .run(materializer);
                    Iterator<String> lines = scala.io.Source.fromFile("db/" + r.getTitle(), Codec.UTF8()).getLines();
                    scala.collection.JavaConversions.asJavaIterator(lines).forEachRemaining(line -> {
                        run.tell(new StreamResponse(line), getSelf());
                    });
                })
                .matchAny(o -> log.info("Received unknown message."))
                .build();
    }
}
