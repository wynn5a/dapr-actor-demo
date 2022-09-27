package io.github.wynn5a;

import io.dapr.actors.ActorMethod;
import io.dapr.actors.ActorType;
import reactor.core.publisher.Mono;

@ActorType(name = "some-actor")
public interface DemoActor {
    void registerReminder();

    @ActorMethod(name = "say_something")
    String say(String something);

    @ActorMethod(returns = Integer.class)
    Mono<Integer> incrementAndGet(int delta);
}
