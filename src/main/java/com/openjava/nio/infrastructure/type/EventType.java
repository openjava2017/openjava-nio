package com.openjava.nio.infrastructure.type;

public class EventType
{
    public static final int EVENT_USER_ONLINE = 1 << 1;
    
    public static final int EVENT_USER_OFFLINE = 1 << 2;
    
    public static final int EVENT_USER_INCOMING_SESSION = 1 << 3;
    
    public static final int EVENT_DELIVER_MESSAGE_FAILED = 1 << 4;
    
    public static final int EVENT_CLOSE_CHAT_SESSION = 1 << 5;
    
    public static final int EVENT_FORCE_USER_LOGOUT = 1 << 6;
    
    public static final int EVENT_CONVERSATION_TRANSFER = 1 << 10;
    
}
