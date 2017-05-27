package com.openjava.nio.exception;

import java.io.IOException;

public class SessionClosedException extends IOException
{
    public SessionClosedException(String message)
    {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace()
    {
        // We use UserTransactionException as business exception,
        // No need fill stack trace here for performance purpose
        return this;
    }
}
