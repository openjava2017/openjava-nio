package com.openjava.nio.infrastructure.type;

import java.util.List;

public class AckRequest
{
    public static final int MESSAGE_ACK = 0;
    public static final int NOTIFICATION_ACK = 1;
    
    private String userId;
    private List<String> ackIds;
    private int ackType;
    
    public String getUserId()
    {
        return userId;
    }

    public List<String> getAckIds()
    {
        return ackIds;
    }

    public int getAckType()
    {
        return ackType;
    }

    private AckRequest(String userId, List<String> ackIds, int ackType)
    {
        this.userId = userId;
        this.ackIds = ackIds;
        this.ackType = ackType;
    }
    
    public static AckRequest create(String userId, List<String> ackIds)
    {
        return new AckRequest(userId, ackIds, MESSAGE_ACK);
    }
    
    public static AckRequest create(String userId, List<String> ackIds, int ackType)
    {
        return new AckRequest(userId, ackIds, ackType);
    }
}
