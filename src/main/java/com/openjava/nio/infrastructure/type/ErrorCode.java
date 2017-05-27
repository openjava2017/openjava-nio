package com.openjava.nio.infrastructure.type;

public class ErrorCode
{
    public static final int ERROR_INTERNAL_EXCEPTION = 1 << 1;
    
    public static final int ERROR_REMOTE_REQUEST_TIMEOUT = 1 << 2;
    
    public static final int ERROR_USER_NOT_ONLINE = 1 << 3;
    
    public static final int ERROR_DELIVER_MESSAGE_FAILED = 1 << 4;
    
    public static final int ERROR_NO_RESOURCE_FOUND = 1 << 5;
    
    // It's for customer service allocation
    public static final int ERROR_EXPIRED_ALLOCATION = 1 << 6; 
}
