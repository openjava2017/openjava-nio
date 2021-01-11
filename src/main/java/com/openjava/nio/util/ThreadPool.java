package com.openjava.nio.util;

import java.util.concurrent.Executor;

import com.openjava.nio.infrastructure.ILifeCycle;
import com.openjava.nio.infrastructure.LifeCycle;

public interface ThreadPool extends Executor, ILifeCycle
{
    /**
     * Blocks until the thread pool is {@link LifeCycle#stop stopped}.
     */
    void join() throws InterruptedException;

    /**
     * @return The total number of threads currently in the pool
     */
    int getThreads();

    /* ------------------------------------------------------------ */
    /**
     * @return The number of idle threads in the pool
     */
    int getIdleThreads();
    
    /**
     * @return True if the pool is low on threads
     */
    boolean isLowOnThreads();

    interface SizedThreadPool extends ThreadPool
    {
        int getMinThreads();
        int getMaxThreads();
        void setMinThreads(int threads);
        void setMaxThreads(int threads);
    }
}
