package net.charles.messaging;

public class Channel<O> {
    private final String name;
    private final Class<O> objClass;
    private MessageReceiver<O> receiver;

    public Channel(String name, MessageReceiver<O> receiver, O... obj) {
        this.name = name;
        this.receiver = receiver;
        this.objClass = (Class<O>) obj.getClass().getComponentType();
    }

    public String getName() {
        return name;
    }

    public MessageReceiver<O> getReceiver() {
        return receiver;
    }

    private Class<O> getObjClass() {
        return objClass;
    }

    public void setReceiver(MessageReceiver<O> receiver) {
        this.receiver = receiver;
    }
}
