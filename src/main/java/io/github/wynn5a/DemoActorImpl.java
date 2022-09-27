package io.github.wynn5a;

import io.dapr.actors.ActorId;
import io.dapr.actors.runtime.AbstractActor;
import io.dapr.actors.runtime.ActorRuntimeContext;
import io.dapr.actors.runtime.Remindable;
import io.dapr.utils.TypeRef;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DemoActorImpl extends AbstractActor implements DemoActor, Remindable<Integer> {

    public static final String COUNTER_KEY = "counter";
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Random random = new Random();

    /**
     * Instantiates a new Actor.
     *
     * @param runtimeContext Context for the runtime.
     * @param id             Actor identifier.
     */
    public DemoActorImpl(ActorRuntimeContext runtimeContext, ActorId id) {
        super(runtimeContext, id);
    }

    /**
     * create reminder
     */
    @Override
    public void registerReminder() {
        super.registerReminder("reminder-1", random.nextInt(1000), Duration.ofSeconds(5), Duration.ofSeconds(2)).block();
    }

    @Override
    public String say(String something) {
        System.out.println("Get from client: " + something);
        return LocalDateTime.now().format(formatter) + " --> " + something;
    }

    /**
     * use dapr state manager to update and save counter
     *
     * @param delta amount to be added to counter
     * @return new counter value
     */
    @Override
    public Mono<Integer> incrementAndGet(int delta) {
        return getActorStateManager().contains(COUNTER_KEY)
                .flatMap(exists -> exists ? super.getActorStateManager().get(COUNTER_KEY, int.class) : Mono.just(0))
                .map(c -> c + delta).flatMap(c -> super.getActorStateManager().set(COUNTER_KEY, c).thenReturn(c));
    }

    /**
     * return type of reminder to consume
     * @return type reference
     */
    @Override
    public TypeRef<Integer> getStateType() {
        return TypeRef.INT;
    }

    /**
     * do something to consume reminders
     *
     * @param reminderName The name of reminder provided during registration.
     * @param state        The user state provided during registration.
     * @param dueTime      The invocation due time provided during registration.
     * @param period       The invocation period provided during registration.
     * @return nothing
     */
    @Override
    public Mono<Void> receiveReminder(String reminderName, Integer state, Duration dueTime, Duration period) {
        return Mono.fromRunnable(() -> {
            String message = String.format("Server received reminder from actor id:%s and name:%s with state: %d @ %s",
                    this.getId(), reminderName, state, LocalDateTime.now().format(formatter));

            // Handles the request by printing message.
            System.out.println(message);
        });
    }
}
