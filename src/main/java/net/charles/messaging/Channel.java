package net.charles.messaging;

import net.charles.Jenin;

public class Channel<O> {
    /**
     * The redis channel's name
     */
    private final String name;

    /**
     * The {@link MessageReceiver}
     */
    private MessageReceiver<O> receiver;

    /**
     * The {@link SubscriptionHandler}
     */
    private final SubscriptionHandler subscriptionHandler;

    /**
     * The object class in which the messages will be parsed
     */
    private final Class<O> objClass;

    /**
     *
     * @param name {@link #name}
     * @param receiver {@link #receiver}
     * @param subscriptionHandler {@link SubscriptionHandler}
     * @param obj the generic type of the messages object
     */
    public Channel(String name, MessageReceiver<O> receiver, SubscriptionHandler subscriptionHandler, O... obj) {
        this.name = name;
        this.receiver = receiver;
        this.subscriptionHandler = subscriptionHandler;
        this.objClass = (Class<O>) obj.getClass().getComponentType();
    }

    /**
     *
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return {@link #receiver}
     */
    public MessageReceiver<O> getReceiver() {
        return receiver;
    }

    /**
     *
     * @return {@link SubscriptionHandler}
     */
    public SubscriptionHandler getSubscriptionHandler() {
        return subscriptionHandler;
    }

    /**
     *
     * @return {@link #receiver}
     */
    public Class<O> getObjClass() {
        return objClass;
    }

    /**
     * Modifies the current receiver
     * @param receiver {@link MessageReceiver}
     */
    public void setReceiver(MessageReceiver<O> receiver) {
        this.receiver = receiver;
    }

    /**
     * Publish an object in the channel
     * @param obj the object
     */
    public void publish(Jenin jenin, O obj) {
        ChannelManager.getRawInstance().publish(jenin, name, obj);
    }
}
