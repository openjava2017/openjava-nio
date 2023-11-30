package com.openjava.nio.util;

import com.openjava.nio.infrastructure.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class QueuedThreadPool extends LifeCycle implements ThreadPool.SizedThreadPool
{
    private static Logger LOG = LoggerFactory.getLogger(QueuedThreadPool.class);

    private final AtomicInteger _threadsStarted = new AtomicInteger();
    private final AtomicInteger _threadsIdle = new AtomicInteger();
    private final AtomicLong _lastShrink = new AtomicLong();
    private final ConcurrentLinkedQueue<Thread> _threads = new ConcurrentLinkedQueue<Thread>();
    private final Object _joinLock = new Object();
    private final BlockingQueue<Runnable> _jobs;
    private String _name = "QueuedThreadPool" + hashCode();
    
    private int _idleTimeout;
    private int _maxThreads;
    private int _minThreads;
    private int _priority = Thread.NORM_PRIORITY;
    private boolean _daemon = false;
    private boolean _detailedDump = false;
    
    public QueuedThreadPool()
    {
        this(6, 1200);
    }

    private QueuedThreadPool(int minThreads, int maxThreads)
    {
        this(maxThreads, minThreads, 60000);
    }

    private QueuedThreadPool(int minThreads, int maxThreads, int idleTimeout)
    {
        setMinThreads(minThreads);
        setMaxThreads(maxThreads);
        setIdleTimeout(idleTimeout);
        _jobs = new LinkedBlockingQueue<Runnable>();
    }
    
    @Override
    protected void doStart() throws Exception
    {
        super.doStart();
        _threadsStarted.set(0);

        startThreads(_minThreads);
    }

    @Override
    protected void doStop() throws Exception
    {
        super.doStop();

        long timeout = 5000;
        BlockingQueue<Runnable> jobs = getQueue();

        // If no stop timeout, clear job queue
        if (timeout <= 0)
            jobs.clear();

        // Fill job Q with noop jobs to wakeup idle
        Runnable noop = new Runnable() {
            @Override
            public void run()
            {
            }
        };
        for (int i = _threadsStarted.get(); i-- > 0;)
            jobs.offer(noop);

        // try to jobs complete naturally for half our stop time
        long stopby = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(timeout) / 2;
        for (Thread thread : _threads) {
            long canwait = TimeUnit.NANOSECONDS.toMillis(stopby
                    - System.nanoTime());
            if (canwait > 0)
                thread.join(canwait);
        }

        // If we still have threads running, get a bit more aggressive

        // interrupt remaining threads
        if (_threadsStarted.get() > 0)
            for (Thread thread : _threads)
                thread.interrupt();

        // wait again for the other half of our stop time
        stopby = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout) / 2;
        for (Thread thread : _threads) {
            long canwait = TimeUnit.NANOSECONDS.toMillis(stopby
                    - System.nanoTime());
            if (canwait > 0)
                thread.join(canwait);
        }

        Thread.yield();
        int size = _threads.size();
        if (size > 0) {
            Thread.yield();

            for (Thread unstopped : _threads) {
                LOG.warn("{} Couldn't stop {}", this, unstopped);
            }
        }

        synchronized (_joinLock) {
            _joinLock.notifyAll();
        }
    }
    
    public static QueuedThreadPool create(int minThreads, int maxThreads)
    {
        return new QueuedThreadPool(minThreads, maxThreads);
    }
    
    public static QueuedThreadPool create(int minThreads, int maxThreads, int idleTimeout)
    {
        return new QueuedThreadPool(minThreads, maxThreads, idleTimeout);
    }

    /**
     * Delegated to the named or anonymous Pool.
     */
    public void setDaemon(boolean daemon)
    {
        _daemon = daemon;
    }

    /**
     * Set the maximum thread idle time. Threads that are idle for longer than
     * this period may be stopped. Delegated to the named or anonymous Pool.
     * 
     * @param idleTimeout
     *            Max idle time in ms.
     * @see #getIdleTimeout
     */
    public void setIdleTimeout(int idleTimeout)
    {
        _idleTimeout = idleTimeout;
    }

    /**
     * Set the maximum number of threads. Delegated to the named or anonymous
     * Pool.
     * 
     * @param maxThreads
     *            maximum number of threads.
     * @see #getMaxThreads
     */
    @Override
    public void setMaxThreads(int maxThreads)
    {
        _maxThreads = maxThreads;
        if (_minThreads > _maxThreads)
            _minThreads = _maxThreads;
    }

    /**
     * Set the minimum number of threads. Delegated to the named or anonymous
     * Pool.
     * 
     * @param minThreads
     *            minimum number of threads
     * @see #getMinThreads
     */
    @Override
    public void setMinThreads(int minThreads)
    {
        _minThreads = minThreads;

        if (_minThreads > _maxThreads)
            _maxThreads = _minThreads;

        int threads = _threadsStarted.get();
        if (isStarted() && threads < _minThreads)
            startThreads(_minThreads - threads);
    }

    /**
     * @param name
     *            Name of this thread pool to use when naming threads.
     */
    public void setName(String name)
    {
        if (isRunning())
            throw new IllegalStateException("started");
        _name = name;
    }

    /**
     * Set the priority of the pool threads.
     * 
     * @param priority
     *            the new thread priority.
     */
    public void setThreadsPriority(int priority)
    {
        _priority = priority;
    }

    /**
     * Get the maximum thread idle time. Delegated to the named or anonymous
     * Pool.
     */
    public int getIdleTimeout()
    {
        return _idleTimeout;
    }

    /**
     * Set the maximum number of threads. Delegated to the named or anonymous
     * Pool.
     */
    @Override
    public int getMaxThreads()
    {
        return _maxThreads;
    }

    /**
     * Get the minimum number of threads. Delegated to the named or anonymous
     * Pool.
     */
    @Override
    public int getMinThreads()
    {
        return _minThreads;
    }

    public String getName()
    {
        return _name;
    }

    /**
     * Get the priority of the pool threads.
     */
    public int getThreadsPriority()
    {
        return _priority;
    }

    /**
     * Get the size of the job queue.
     */
    public int getQueueSize()
    {
        return _jobs.size();
    }

    /**
     * Delegated to the named or anonymous Pool.
     */
    public boolean isDaemon()
    {
        return _daemon;
    }

    public boolean isDetailedDump()
    {
        return _detailedDump;
    }

    public void setDetailedDump(boolean detailedDump)
    {
        _detailedDump = detailedDump;
    }

    @Override
    public void execute(Runnable job)
    {
        if (!isRunning() || !_jobs.offer(job)) {
            LOG.warn("{} rejected {}", this, job);
            throw new RejectedExecutionException(job.toString());
        }
    }

    /**
     * Blocks until the thread pool is {@link LifeCycle#stop stopped}.
     */
    @Override
    public void join() throws InterruptedException
    {
        synchronized (_joinLock) {
            while (isRunning())
                _joinLock.wait();
        }
    }

    /**
     * @return The total number of threads currently in the pool
     */
    @Override
    public int getThreads()
    {
        return _threadsStarted.get();
    }

    /**
     * @return The number of idle threads in the pool
     */
    @Override
    public int getIdleThreads()
    {
        return _threadsIdle.get();
    }

    /**
     * @return True if the pool is at maxThreads and there are not more idle
     *         threads than queued jobs
     */
    @Override
    public boolean isLowOnThreads()
    {
        return _threadsStarted.get() == _maxThreads
                && _jobs.size() >= _threadsIdle.get();
    }

    private boolean startThreads(int threadsToStart)
    {
        while (threadsToStart > 0) {
            int threads = _threadsStarted.get();
            if (threads >= _maxThreads)
                return false;

            if (!_threadsStarted.compareAndSet(threads, threads + 1))
                continue;

            boolean started = false;
            try {
                Thread thread = newThread(_runnable);
                thread.setDaemon(isDaemon());
                thread.setPriority(getThreadsPriority());
                thread.setName(_name + "-" + thread.getId());
                _threads.add(thread);

                thread.start();
                started = true;
            } finally {
                if (!started)
                    _threadsStarted.decrementAndGet();
            }
            if (started)
                threadsToStart--;
        }
        return true;
    }

    protected Thread newThread(Runnable runnable)
    {
        return new Thread(runnable);
    }

    @Override
    public String toString()
    {
        return String.format("%s{%s,%d<=%d<=%d,i=%d,q=%d}", _name, getState(),
                getMinThreads(), getThreads(), getMaxThreads(),
                getIdleThreads(), (_jobs == null ? -1 : _jobs.size()));
    }

    private Runnable idleJobPoll() throws InterruptedException
    {
        return _jobs.poll(_idleTimeout, TimeUnit.MILLISECONDS);
    }

    private Runnable _runnable = new Runnable() {
        @Override
        public void run()
        {
            boolean shrink = false;
            try {
                Runnable job = _jobs.poll();

                if (job != null && _threadsIdle.get() == 0) {
                    startThreads(1);
                }

                loop: while (isRunning()) {
                    // Job loop
                    while (job != null && isRunning()) {
                        runJob(job);
                        if (Thread.interrupted())
                            break loop;
                        job = _jobs.poll();
                    }

                    // Idle loop
                    try {
                        _threadsIdle.incrementAndGet();

                        while (isRunning() && job == null) {
                            if (_idleTimeout <= 0)
                                job = _jobs.take();
                            else {
                                // maybe we should shrink?
                                final int size = _threadsStarted.get();
                                if (size > _minThreads) {
                                    long last = _lastShrink.get();
                                    long now = System.nanoTime();
                                    if (last == 0
                                            || (now - last) > TimeUnit.MILLISECONDS
                                                    .toNanos(_idleTimeout)) {
                                        shrink = _lastShrink.compareAndSet(
                                                last, now)
                                                && _threadsStarted
                                                        .compareAndSet(size,
                                                                size - 1);
                                        if (shrink) {
                                            return;
                                        }
                                    }
                                }
                                job = idleJobPoll();
                            }
                        }
                    } finally {
                        if (_threadsIdle.decrementAndGet() == 0) {
                            startThreads(1);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupted");
            } catch (Throwable e) {
                LOG.warn("Unknown exception in thread pool", e);
            } finally {
                if (!shrink)
                    _threadsStarted.decrementAndGet();
                _threads.remove(Thread.currentThread());
            }
        }
    };

    /**
     * Runs the given job in the {@link Thread#currentThread() current thread}.
     * Subclasses may override to perform pre/post actions before/after the job
     * is run.
     */
    protected void runJob(Runnable job)
    {
        job.run();
    }

    /**
     * @return the job queue
     */
    protected BlockingQueue<Runnable> getQueue()
    {
        return _jobs;
    }
}
