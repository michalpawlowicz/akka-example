package Client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import messages.Request;
import messages.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Config 1/2 ?");
        String configFileName = "client.config";
        try {
            String num = br.readLine();
            if(num.equals("1")) {
                configFileName = "client.conf";
            } else {
                configFileName = "client2.conf";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File configFile = new File(configFileName);
        Config config = ConfigFactory.parseFile(configFile);
        final ActorSystem system = ActorSystem.create("client_system", config);
        final ActorRef actorRef = system.actorOf(Props.create(ClientActor.class), "client");

        while (true) {
            String line;

            try {
                line = br.readLine();
                Request request = new Request();
                if (line.equals("\\q")) {
                    break;
                } else if (line.equals("\\p")) {
                    System.out.println("Title: ");
                    String title = br.readLine();
                    request.setTitle(title);
                    request.setType(Type.SEARCH);
                    actorRef.tell(request, null);
                } else if(line.equals("\\o")) {
                    System.out.println("Title: ");
                    String title = br.readLine();
                    request.setTitle(title);
                    request.setType(Type.ORDER);
                    actorRef.tell(request, null);
                } else if(line.equals("\\s")) {
                    System.out.println("Title: ");
                    String title = br.readLine();
                    request.setTitle(title);
                    request.setType(Type.STREAM);
                    actorRef.tell(request, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        system.terminate();
    }
}
