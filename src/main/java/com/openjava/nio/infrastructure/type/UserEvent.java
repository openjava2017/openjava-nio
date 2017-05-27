package com.openjava.nio.infrastructure.type;

public class UserEvent
{
    private int type;
    private String sessionId;
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String toUserName;
    
    UserEvent(int type, String sessionId, String fromUserId, String toUserId, String fromUserName, String toUserName)
    {
        this.type = type;
        this.sessionId = sessionId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
    }
    
    public int getType()
    {
        return type;
    }
    
    public String getSessionId()
    {
        return sessionId;
    }

    public String getFromUserId()
    {
        return fromUserId;
    }

    public String getFromUserName() {
		return fromUserName;
	}

	public String getToUserId()
    {
        return toUserId;
    }

    public String getToUserName() {
		return toUserName;
	}

	public static UserEvent create(int type, String sessionId, String fromUserId, 
        String toUserId, String fromUserName, String toUserName)
    {
        return new UserEvent(type, sessionId, fromUserId, toUserId, fromUserName, toUserName);
    }
    
    @Override
    public String toString()
    {
        return "type: " + type + ", fromUserId: " + fromUserId + ", fromUserName: " + fromUserName + ", toUserId: " + toUserId + ", toUserName: " + toUserName;
    }
}
