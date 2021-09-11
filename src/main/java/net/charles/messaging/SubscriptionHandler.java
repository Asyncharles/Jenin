package net.charles.messaging;

public interface SubscriptionHandler {
    /**
     * Called when a {@link Channel} is registered
     * @param channel the {@link Channel#getName()}
     */
    void onSubscribe(String channel);

    /**
     * Called when a {@link Channel} is destroyed
     * @param channel the {@link Channel#getName()}
     */
    void onUnSubscribe(String channel);
}
