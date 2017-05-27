package com.openjava.nio.util;

public final class StringUtils
{
    public static boolean isBlank(String str)
    {
        int strLen;
        
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                 return false;
            }
        }
        
        return true;
    }
    
    public static boolean isNotBlank(String str)
    {
        return !isBlank(str);
    }
    
    public static String trimEmptyString(String str)
    {
        return str == null ? "" : str.trim();
    }
    
    public static String extractCommand(String pathInfo)
    {
        if (pathInfo == null) {
            throw new IllegalArgumentException("pathInfo cannot be null");
        }
        
        String command = pathInfo;
        // the request path /webchat/poll.action ==> poll
        int index = pathInfo.lastIndexOf(Constants.SLASH_CHAR);
        if (index != -1) {
            command = pathInfo.substring(index + 1, pathInfo.length());
        }
        
        index = command.indexOf(Constants.DOT_CHAR);
        if (index != -1) {
            command = command.substring(0, index);
        }
        
        
        return command;
    }
    
    public static int getSeparatorIndexOf(String name, int fromIndex) 
    {
        int dotIndex = name.indexOf(Constants.SLASH_CHAR, fromIndex);
        return dotIndex;
    }
}
