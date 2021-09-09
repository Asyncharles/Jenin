package net.charles.exceptions.messaging;

public class MessagingException extends NullPointerException {
    private String message;

    public MessagingException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
