package com.openjava.nio.infrastructure.type;

public class UserAnnouncement
{
    public static final int USER_MESSAGE = 0;
    public static final int USER_INQUIRY = 1;
    public static final int USER_QUOTATION = 2;

    private String fromUser;
    private String toUser;
    private int type;
    private int unreadNum;
    private String fromUserName;
    private String toUserName;
    
    private UserAnnouncement(String fromUser, String toUser, int type, int unreadNum, String fromUserName, String toUserName)
    {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.type = type;
        this.unreadNum = unreadNum;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
    }
    
    public String getFromUser()
    {
        return fromUser;
    }

    public String getToUser()
    {
        return toUser;
    }
    
    public int getType()
    {
        return type;
    }
    
    public int getUnreadNum()
    {
        return unreadNum;
    }
    
    public void incUnreadNum()
    {
        unreadNum = unreadNum + 1;
    }

    public String getFromUserName() {
		return fromUserName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public static UserAnnouncement create(String fromUser, String toUser, int type, int messageNum, String fromUserName, String toUserName)
    {
        return new UserAnnouncement(fromUser, toUser, type, messageNum, fromUserName, toUserName);
    }
}