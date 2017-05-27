package com.openjava.nio.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutor implements Scheduler
{
    private ScheduledThreadPoolExecutor scheduler;
    
    public ScheduledExecutor()
    {
        this(null, false);
    }
    
    public ScheduledExecutor(final String name, final boolean daemon)
    {
        this(name, 1, daemon);
    }
    
    public ScheduledExecutor(final String name, final int threadNum, final boolean daemon)
    {
        scheduler = new ScheduledThreadPoolExecutor(threadNum, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r, name == null ? "Scheduler-" + hashCode() : name);
                thread.setDaemon(daemon);
                return thread;
            }
        });
        scheduler.setRemoveOnCancelPolicy(true);
    }
    
    @Override
    public Task schedule(Runnable task, long delay, TimeUnit unit)
    {
        ScheduledFuture<?> result = scheduler.schedule(task, delay, unit);
        return new ScheduledFutureTask(result);
    }
    
    @Override
    public void shutdown()
    {
        scheduler.shutdownNow();
        scheduler = null;
    }
    
    private class ScheduledFutureTask implements Task
    {
        private final ScheduledFuture<?> scheduledFuture;

        public ScheduledFutureTask(ScheduledFuture<?> scheduledFuture)
        {
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public boolean cancel()
        {
            return scheduledFuture.cancel(false);
        }
    }
}
