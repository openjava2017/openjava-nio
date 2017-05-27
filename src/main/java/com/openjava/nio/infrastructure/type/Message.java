package com.openjava.nio.infrastructure.type;

import java.util.UUID;

import com.openjava.nio.util.StringUtils;

public class Message
{
    public static final int STATE_UNREAD = 0;
    public static final int STATE_READ = 1;
    public static final int STATE_OFFLINE = 2;
    
    private String sessionId; // Chat session id
    
    private String messageId;
    
    private String fromUser;
    
    private String fromRealName;
    
    private String fromNickName;
    
    private String toUser;
    
    private String toUserName;
    
    private String message;
    
    private String style;
    
    private int businessType;
    
    private String businessId;
    
    private int sequenceNo;
    
    private long timestamp;
    
    private int state;
    
    private Message(String sessionId, String messageId, String fromUser, String toUser, String message, String style,
        int businessType, String businessId, int sequenceNo, long timestamp, int state)
    {
        this.sessionId = sessionId;
        this.messageId = messageId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.message = message;
        this.style = style;
        this.businessType = businessType;
        this.businessId = businessId;
        this.sequenceNo = sequenceNo;
        this.timestamp = timestamp;
        this.state = state;
    }
    
    private Message(String sessionId, String messageId, String fromUser, String toUser, String message, String style,
            int businessType, String businessId, int sequenceNo, long timestamp, int state, String fromRealName, String fromNickName, String toUserName)
        {
            this.sessionId = sessionId;
            this.messageId = messageId;
            this.fromUser = fromUser;
            this.toUser = toUser;
            this.message = message;
            this.style = style;
            this.businessType = businessType;
            this.businessId = businessId;
            this.sequenceNo = sequenceNo;
            this.timestamp = timestamp;
            this.state = state;
            this.fromRealName = fromRealName;
            this.fromNickName = fromNickName;
            this.toUserName = toUserName;
        }

    public String getSessionId()
    {
        return sessionId;
    }
    
    public String getMessageId()
    {
        return messageId;
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
    
    public String getStyle()
    {
        return style;
    }
    
    public int getBusinessType()
    {
        return businessType;
    }

    public String getBusinessId()
    {
        return businessId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }
    
    public int getSequenceNo()
    {
        return sequenceNo;
    }
    
    public int getState()
    {
        return state;
    }
    
    public void setState(int state)
    {
        this.state = state;
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

	public static Message create2(String sessionId, String fromUser, String toUser, String message, String style,
        int businessType, String businessId, int sequenceNo)
    {
        if (StringUtils.isBlank(fromUser) || StringUtils.isBlank(toUser)) {
            throw new IllegalArgumentException("fromUser and toUser must be not empty");
        }
        return new Message(sessionId, newId(), fromUser, toUser, message, style, businessType, businessId, sequenceNo,
            System.currentTimeMillis(), STATE_UNREAD);
    }
	
	public static Message create(String sessionId, String fromUser, String toUser, String message, String style,
	        int businessType, String businessId, int sequenceNo, String fromRealName, String fromNickName, String toUserName)
	    {
	        if (StringUtils.isBlank(fromUser) || StringUtils.isBlank(toUser)) {
	            throw new IllegalArgumentException("fromUser and toUser must be not empty");
	        }
	        return new Message(sessionId, newId(), fromUser, toUser, message, style, businessType, businessId, sequenceNo,
	            System.currentTimeMillis(), STATE_UNREAD, fromRealName, fromNickName, toUserName);
	    }
	
	public static Message create3(String sessionId, String fromUser, String toUser, String message, String style,
        int businessType, String businessId, int sequenceNo, int state)
    {
        if (StringUtils.isBlank(fromUser) || StringUtils.isBlank(toUser)) {
            throw new IllegalArgumentException("fromUser and toUser must be not empty");
        }
        return new Message(sessionId, newId(), fromUser, toUser, message, style, businessType, businessId, sequenceNo,
            System.currentTimeMillis(), state);
    }
	
	public static Message create(String sessionId, String fromUser, String toUser, String message, String style,
	        int businessType, String businessId, int sequenceNo, int state, String fromRealName, String fromNickName, String toUserName)
	    {
	        if (StringUtils.isBlank(fromUser) || StringUtils.isBlank(toUser)) {
	            throw new IllegalArgumentException("fromUser and toUser must be not empty");
	        }
	        return new Message(sessionId, newId(), fromUser, toUser, message, style, businessType, businessId, sequenceNo,
	            System.currentTimeMillis(), state, fromRealName, fromNickName, toUserName);
	    }
    
    public static Message create4(String sessionId, String messageId, String fromUser, String toUser, String message,
        String style, int businessType, String businessId, int sequenceNo, long timestamp, int state)
    {
        if (StringUtils.isBlank(fromUser) || StringUtils.isBlank(toUser)) {
            throw new IllegalArgumentException("fromUser and toUser must be not empty");
        }
        return new Message(sessionId, messageId, fromUser, toUser, message, style, businessType, businessId, sequenceNo,
            timestamp, STATE_UNREAD);
    }
    
    public static Message create(String sessionId, String messageId, String fromUser, String toUser, String message,
            String style, int businessType, String businessId, int sequenceNo, long timestamp, int state, String fromRealName, String fromNickName, String toUserName)
        {
            if (StringUtils.isBlank(fromUser) || StringUtils.isBlank(toUser)) {
                throw new IllegalArgumentException("fromUser and toUser must be not empty");
            }
            return new Message(sessionId, messageId, fromUser, toUser, message, style, businessType, businessId, sequenceNo,
                timestamp, STATE_UNREAD, fromRealName, fromNickName, toUserName);
        }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("messageId: ").append(messageId).append(", fromUser: " ).append(fromUser)
            .append(", toUser: ").append(toUser).append(", message: ").append(message).append(", style: ").append(style)
            .append(", Time: ").append(timestamp).append(", fromRealName: ").append(fromRealName).append(", fromNickName: ").append(fromNickName).append(", toUserName: ").append(toUserName);
        return builder.toString();
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
