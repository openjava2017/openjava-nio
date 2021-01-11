package com.openjava.nio.exception;

public class ClosedSessionException extends NioSessionException
{
    public ClosedSessionException(String message) {
        super(message);
    }

    public ClosedSessionException(int code, String message) {
        super(code, message);
    }

    public ClosedSessionException(String message, Throwable ex) {
        super(message, ex);
    }
}