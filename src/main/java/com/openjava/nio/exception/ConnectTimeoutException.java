package com.openjava.nio.exception;

import java.io.IOException;

public class ConnectTimeoutException extends IOException
{
    public ConnectTimeoutException(String message)
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
