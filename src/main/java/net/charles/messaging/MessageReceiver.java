package net.charles.messaging;

@FunctionalInterface
public interface MessageReceiver<O> {
    /**
     * Called when a message is received on the {@link Channel}
     * @param obj the parsed object published on the channel
     */
    void onMessage(O obj);
}
