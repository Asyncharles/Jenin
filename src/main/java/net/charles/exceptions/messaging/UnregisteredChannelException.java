package net.charles.exceptions.messaging;

public class UnregisteredChannelException extends MessagingException {
    public UnregisteredChannelException(String message) {
        super(message);
    }
}
