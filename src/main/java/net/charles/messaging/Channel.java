package net.charles.messaging;

public class Channel<O> {
    /**
     * The redis channel's name
     */
    private final String name;

    /**
     * The object class in which the messages will be parsed
     */
    private final Class<O> objClass;

    /**
     * The {@link MessageReceiver}
     */
    private MessageReceiver<O> receiver;


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
        subscriptionHandler.onSubscribe(name);
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
     * @return {@link #receiver}
     */
    private Class<O> getObjClass() {
        return objClass;
    }


    /**
     * Modifies the current receiver
     * @param receiver {@link MessageReceiver}
     */
    public void setReceiver(MessageReceiver<O> receiver) {
        this.receiver = receiver;
    }
}
