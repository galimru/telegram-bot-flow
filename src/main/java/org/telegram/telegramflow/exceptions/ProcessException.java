package org.telegram.telegramflow.exceptions;

public class ProcessException extends Exception {

    public ProcessException(String message) {
        super(message);
    }

    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}