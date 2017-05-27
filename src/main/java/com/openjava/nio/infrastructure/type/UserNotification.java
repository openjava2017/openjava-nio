package com.openjava.nio.infrastructure.type;

import java.util.UUID;

public class UserNotification
{
    private String id;
    private int type;
    private String sessionId;
    private String fromUser;
    private String toUser;
    private String message;
    private String businessId;
    private long sentTime;
    private String fromRealName;
    private String fromNickName;
    private String toUserName;
    
    private UserNotification(int type, String sessionId, String fromUser, String toUser, 
        String businessId, String message, String fromRealName, String fromNickName, String toUserName)
    {
        this(newId(), type, sessionId, fromUser, toUser, businessId, message, System.currentTimeMillis(), fromRealName, fromNickName, toUserName);
    }
    
    private UserNotification(String id, int type, String sessionId, String fromUser, String toUser,
        String businessId, String message, long sentTime, String fromRealName, String fromNickName, String toUserName)
    {
        this.id = id;
        this.type = type;
        this.sessionId = sessionId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.businessId = businessId;
        this.message = message;
        this.sentTime = sentTime;
        this.fromRealName = fromRealName;
        this.fromNickName = fromNickName;
        this.toUserName = toUserName;
    }
    
    public String getId()
    {
        return id;
    }

    public int getType()
    {
        return type;
    }

    public String getSessionId()
    {
        return sessionId;
    }
    
    public String getFromUser()
    {
        return fromUser;
    }

    public String getToUser()
    {
        return toUser;
    }

    public String getMessage()
    {
        return message;
    }

    public String getBusinessId()
    {
        return businessId;
    }

    public long getSentTime()
    {
        return sentTime;
    }

    public String getFromRealName() 
    {
		return fromRealName;
	}

	public String getFromNickName() 
	{
		return fromNickName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public static UserNotification create(int type, String sessionId, String fromUser, String toUser,
        String businessId, String message, String fromRealName, String fromNickName, String toUserName)
    {
        return new UserNotification(type, sessionId, fromUser, toUser, businessId, message, fromRealName, fromNickName, toUserName);
    }
    
    public static UserNotification create(String id, int type, String sessionId, String fromUser, String toUser,
        String businessId, String message, long sentTime, String fromRealName, String fromNickName, String toUserName)
    {
        return new UserNotification(id, type, sessionId, fromUser, toUser, businessId, message, sentTime, fromRealName, fromNickName, toUserName);
    }
    
    private static String newId()
    {
        String uuid = UUID.randomUUID().toString();
        String result = "";
        for (int i = 0; i < uuid.length(); i++) {
            char c = uuid.charAt(i);
            result += (c == '-' ? "" : c);
        }
        return result;
    }
}
