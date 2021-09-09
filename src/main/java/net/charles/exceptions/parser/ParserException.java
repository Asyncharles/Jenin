package net.charles.exceptions.parser;

public class ParserException extends RuntimeException {
    private String message;

    public ParserException(String message) {
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
