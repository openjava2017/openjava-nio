package com.openjava.nio.util;

import java.util.concurrent.TimeUnit;

public interface Scheduler
{
    interface Task
    {
        boolean cancel();
    }

    public Task schedule(Runnable task, long delay, TimeUnit units);
    
    public void shutdown();
}
