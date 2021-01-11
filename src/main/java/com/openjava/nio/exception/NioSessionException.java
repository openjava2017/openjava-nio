package com.openjava.nio.exception;

import java.io.IOException;

public class NioSessionException extends IOException
{
    // 错误码
    private int code = 1000;
    // 是否打印异常栈
    private boolean stackTrace = true;

    public NioSessionException(String message) {
        super(message);
    }

    public NioSessionException(int code, String message) {
        super(message);
        this.code = code;
        this.stackTrace = false;
    }

    public NioSessionException(String message, Throwable ex) {
        super(message, ex);
    }

    @Override
    public Throwable fillInStackTrace() {
        return stackTrace ? super.fillInStackTrace() : this;
    }

    public int getCode() {
        return code;
    }
}
