package Server.stream;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import messages.Request;
import messages.StreamResponse;
import scala.concurrent.duration.FiniteDuration;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class streamer extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, r -> {
                    ActorMaterializer materializer = ActorMaterializer.create(context());
                    FileIO.fromPath(Paths.get("db/" + r.getTitle()))
                            .via(Framing.delimiter(ByteString.fromString("\n"), 256, FramingTruncation.ALLOW))
                            .map(line -> new StreamResponse(line.utf8String()))
                            .throttle(1, FiniteDuration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping())
                            .to(Sink.actorRef(getSender(), context()))
                            .run(materializer);
                }).matchAny(o -> {
                    log.error("Invalid request, dropping ..");
                }).build();
    }
}
