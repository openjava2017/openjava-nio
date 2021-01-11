package com.openjava.nio.exception;

public class CreateSessionException extends NioSessionException
{
    public CreateSessionException(String message) {
        super(message);
    }

    public CreateSessionException(int code, String message) {
        super(code, message);
    }

    public CreateSessionException(String message, Throwable ex) {
        super(message, ex);
    }
}