package com.openjava.nio.infrastructure.type;

public class ConversationEvent extends UserEvent
{
    private String userId;
    private int businessType;
    private String businessId;
    private String reason;
    
    ConversationEvent(int type, String userId, String fromUser, String fromUserName, String toUser, String toUserName, 
        String sessionId, int businessType, String businessId, String reason)
    {
        super(type, sessionId, fromUser, toUser, fromUserName, toUserName);
        this.userId = userId;
        this.businessType = businessType;
        this.businessId = businessId;
        this.reason = reason;
    }

    public String getUserId()
    {
        return userId;
    }

    public int getBusinessType()
    {
        return businessType;
    }

    public String getBusinessId()
    {
        return businessId;
    }
    
    public String getReason()
    {
        return reason;
    }
    
    public static ConversationEvent create(int type, String userId, String fromUser, String fromUserName, 
        String toUser, String toUserName, String sessionId, int businessType, String businessId, String reason)
    {
        return new ConversationEvent(type, userId, fromUser, fromUserName, toUser, toUserName, sessionId,
            businessType, businessId, reason);
    }
}
