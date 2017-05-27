package com.openjava.nio.infrastructure;

public interface IExpirable
{
    /**
     * Keep alive by resetting the last used time.
     */
    void kick();
    
    /**
     * Get the last used time for cleaning expired session
     */
    long getLastUsedTime();
}
