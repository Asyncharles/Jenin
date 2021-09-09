package net.charles.messaging;

public interface MessageReceiver<O> {
    void onMessage(O obj);
}
