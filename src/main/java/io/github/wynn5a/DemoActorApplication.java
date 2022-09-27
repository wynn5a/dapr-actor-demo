package io.github.wynn5a;
import io.dapr.actors.runtime.ActorRuntime;
import io.dapr.actors.runtime.ActorRuntimeConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;

/**
 * How to run: <br/>
 * dapr run --components-path ./components --app-id demo-actor-server --app-port 8080 \
 * -- java -jar target/dapr-actor-demo-1.0.0.jar
 */
@SpringBootApplication
public class DemoActorApplication {
    public static void main(String[] args) throws Exception {
        // Idle timeout until actor instance is deactivated.
        ActorRuntimeConfig config = ActorRuntime.getInstance().getConfig();
        config.setActorIdleTimeout(Duration.ofSeconds(30));
        // How often actor instances are scanned for deactivation and balance.
        config.setActorScanInterval(Duration.ofSeconds(10));
        // How long to wait until for draining an ongoing API call for an actor instance.
        config.setDrainOngoingCallTimeout(Duration.ofSeconds(10));
        // Determines whether to drain API calls for actors instances being balanced.
        config.setDrainBalancedActors(true);

        // Register the Actor class.
        ActorRuntime.getInstance().registerActor(DemoActorImpl.class);

        SpringApplication.run(DemoActorApplication.class);
    }
}
