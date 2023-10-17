package com.openjava.nio.endpoint;

import com.openjava.nio.exception.MultiException;
import com.openjava.nio.infrastructure.LifeCycle;
import com.openjava.nio.provider.processor.*;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;
import com.openjava.nio.provider.session.pool.NioSessionPool;
import com.openjava.nio.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSocketServer extends LifeCycle implements ISessionEventListener, ISessionDataListener
{
    private static final int DEFAULT_SERVER_BACKLOG = 50;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSocketServer.class);

    private final String host;
    private final int port;
    private final int backlog;
    private final IProcessor<INioSession>[] processors;
    private final IProcessorChain processorChain;

    private long sessionScanPeriodMillis = 5000;
    private long sessionTimeOutInMillis = 15 * 1000;

    private final ExecutorService executor;
    private final NioSessionPool pool = NioSessionPool.create();
    private final Scheduler scheduler =  new ScheduledExecutor("connect-timeout-scanner", true);

    public AbstractSocketServer(String host, int port)
    {
        this(host, port, DEFAULT_SERVER_BACKLOG, Runtime.getRuntime().availableProcessors(), Executors.newCachedThreadPool());
    }

    public AbstractSocketServer(String host, int port, int processorThreads, ExecutorService executor)
    {
        this(host, port, DEFAULT_SERVER_BACKLOG, processorThreads, executor);
    }

    public AbstractSocketServer(String host, int port, int backlog, int processorThreads, ExecutorService executor)
    {
        AssertUtils.notEmpty(host, "host cannot be empty");
        AssertUtils.isTrue(port > 1024, "invalid port value");
        AssertUtils.isTrue(backlog > 0, "invalid backlog value");
        AssertUtils.isTrue(processorThreads > 0, "invalid processorThreads value");
        AssertUtils.notNull(executor, "executor cannot be null");

        this.host = host;
        this.port = port;
        this.backlog = backlog;
        this.processors = new IProcessor[processorThreads];
        this.processorChain = new SimpleProcessorChain(processors);
        this.executor = executor;
    }

    @Override
    public void onSessionCreated(INioSession session)
    {
        pool.addSession(session);
    }

    @Override
    public void onSessionClosed(INioSession session)
    {
        pool.removeSession(session);
    }

    @Override
    public void onSocketConnectFailed(IOException ex)
    {
        // never happened
    }

    public abstract void sessionDataReceived(INioSession session, byte[] packet);

    @Override
    public void onDataReceived(final INioSession session, final byte[] packet)
    {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sessionDataReceived(session, packet);
            }
        });
    }

    @SuppressWarnings("unused")
    public void setSessionScanPeriodMillis(long sessionScanPeriodMillis)
    {
        this.sessionScanPeriodMillis = sessionScanPeriodMillis;
    }

    @SuppressWarnings("unused")
    public void setSessionTimeOutInMillis(long sessionTimeOutInMillis)
    {
        this.sessionTimeOutInMillis = sessionTimeOutInMillis;
    }
    
    @Override
    protected void doStart() throws Exception
    {
        pool.start();
        for (int i = 0; i < processors.length; i++) {
            boolean result = false;
            try {
                processors[i] = new NioSessionProcessor(i, processorChain, executor, scheduler);
                processors[i].start();
                result = true;
            } finally {
                if (!result) {
                    processors[i].stop();
                }
            }
        }
        LOG.info("Socket processor manager started, pool size=" + processors.length);

        InetSocketAddress address = new InetSocketAddress(host, port);
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        boolean result = false;
        try {
            socketChannel.configureBlocking(false);
            ServerSocket serverSocket = socketChannel.socket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(address, backlog);
            processorChain.registerServer(socketChannel, this, this);
            result = true;
        } finally {
            if (!result) {
                ProcessorUtils.closeQuietly(socketChannel);
            }
        }

        scheduler.schedule(new Scavenger(), sessionScanPeriodMillis, TimeUnit.MILLISECONDS);
    }
    
    @Override
    protected void doStop() throws Exception
    {
        MultiException exception = new MultiException();
        for (int i = 0; i < processors.length; i++) {
            try {
                processors[i].stop();
            } catch (Exception ex) {
                exception.add(ex);
            }
        }
        exception.ifExceptionThrow();
        LOG.info("Socket processor manager stopped");

        // no need invoke pool.stop()? since we already stop processor, session will be closed automatically inside.
        pool.stop();
        scheduler.shutdown();
    }
    
    private class Scavenger implements Runnable
    {
        @Override
        public void run()
        {
            if (!isRunning()) {
                return;
            }
            
            long now = System.currentTimeMillis();
            try {
                INioSession session = pool.borrowSession();
                if (session != null) {
                    touchSession(session, now);
                }
            } finally {
                scheduler.schedule(this, sessionScanPeriodMillis, TimeUnit.MILLISECONDS);
            }
        }
        
        private void touchSession(INioSession session, long when)
        {
            if (session != null) {
                long lastUsedTime = session.getLastUsedTime();
                if (when - lastUsedTime > sessionTimeOutInMillis) {
                    LOG.info("Expired NIO session found, close it[SID={}]", session.getId());
                    // Close the expired session, no need remove it in the pool
                    // NioSessionPool will remove the closed session automatically
                    // See NioSessionPool.borrowSession method for details
                    session.destroy();
                }
            }
        }
    }
}
