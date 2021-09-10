package net.charles.messaging;

@FunctionalInterface
public interface SubscriptionHandler {
    /**
     * Called when a {@link Channel} is registered
     * @param channel the {@link Channel#getName()}
     */
    void onSubscribe(String channel);
}
