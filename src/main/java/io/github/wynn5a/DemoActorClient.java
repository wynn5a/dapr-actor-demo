package io.github.wynn5a;

import io.dapr.actors.ActorId;
import io.dapr.actors.client.ActorClient;
import io.dapr.actors.client.ActorProxyBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * How to run client: <br/>
 * dapr run --components-path ./components --app-id actor-client \
 * -- mvn compile exec:java -Dexec.mainClass="io.github.wynn5a.DemoActorClient"
 */
public class DemoActorClient {
    private static final int NUM_ACTORS = 2;

    public static void main(String[] args) throws InterruptedException {
        try (ActorClient client = new ActorClient()) {
            ActorProxyBuilder<DemoActor> builder = new ActorProxyBuilder<>(DemoActor.class, client);
            List<Thread> threads = new ArrayList<>(NUM_ACTORS);

            // Creates multiple actors.
            for (int i = 0; i < NUM_ACTORS; i++) {
                ActorId actorId = ActorId.createRandom();
                DemoActor actor = builder.build(actorId);

                // Start a thread per actor.
                Thread thread = new Thread(() -> callActorForever(actorId.toString(), actor));
                thread.start();
                threads.add(thread);
            }

            // Waits for threads to finish.
            for (Thread thread : threads) {
                thread.join();
            }
        }

        System.out.println("Done.");
    }

    /**
     * Makes multiple method calls into actor until interrupted.
     *
     * @param actorId Actor's identifier.
     * @param actor   Actor to be invoked.
     */
    private static void callActorForever(String actorId, DemoActor actor) {
        // First, register reminder.
        actor.registerReminder();

        // Now, we run until thread is interrupted.
        while (!Thread.currentThread().isInterrupted()) {
            // Invoke actor method to increment counter by 1, then build message.
            actor.incrementAndGet(1).map(i -> String.format("Actor %s said message #%d", actorId, i))
                    .map(actor::say)
                    .subscribe(s -> System.out.printf("Actor %s got a reply: %s%n", actorId, s));
            try {
                // Waits for up to 2 second.
                Thread.sleep((long) (2000 * Math.random()));
            } catch (InterruptedException e) {
                // We have been interrupted, so we set the interrupted flag to exit gracefully.
                Thread.currentThread().interrupt();
            }
        }
    }
}
