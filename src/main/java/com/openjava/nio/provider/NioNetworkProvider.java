package com.openjava.nio.provider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.openjava.nio.provider.processor.ProcessorUtils;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openjava.nio.infrastructure.LifeCycle;
import com.openjava.nio.exception.MultiException;
import com.openjava.nio.provider.processor.IProcessor;
import com.openjava.nio.provider.processor.NioSessionProcessor;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionEventListener;
import com.openjava.nio.util.ScheduledExecutor;
import com.openjava.nio.util.Scheduler;

public class NioNetworkProvider extends LifeCycle implements INetworkProvider
{
    private static Logger logger = LoggerFactory.getLogger(NioNetworkProvider.class);

    private static final int DEFAULT_SERVER_BACKLOG = 50;
    
    private volatile long pointer = 0L;
    
    private int processors = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final IProcessor<INioSession>[] pool =  new NioSessionProcessor[processors];
    private final Scheduler scheduler = new ScheduledExecutor();

    @Override
    public void registerConnection(SocketAddress remoteAddress, ISessionEventListener eventListener,
        ISessionDataListener dataListener, long timeoutInMillis) throws IOException
    {
        checkState();
        boolean result = false;
        SocketChannel channel = null;
        
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(remoteAddress);
            nextProcessor().registerConnection(channel, eventListener, dataListener, timeoutInMillis);
            result = true;
        } finally {
            if (!result) {
                ProcessorUtils.closeQuietly(channel);
            }
        }
    }

    @Override
    public void registerServer(SocketAddress localAddress, ISessionEventListener eventListener,
        ISessionDataListener dataListener) throws IOException
    {
        checkState();
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        
        boolean result = false;
        try {
            socketChannel.configureBlocking(false);
            ServerSocket serverSocket = socketChannel.socket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(localAddress, DEFAULT_SERVER_BACKLOG);
            nextProcessor().registerServer(socketChannel, eventListener, dataListener);
            result = true;
        } finally {
            if (!result) {
                ProcessorUtils.closeQuietly(socketChannel);
            }
        }
    }

    @Override
    public void registerSession(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener)
    {
        checkState();
        nextProcessor().registerSession(channel, eventListener, dataListener);
    }

    @Override
    protected void doStart() throws Exception
    {
        for (int i = 0; i < pool.length; i++) {
            boolean result = false;
            try {
                pool[i] = new NioSessionProcessor(i, this, executor, scheduler);
                pool[i].start();
                result = true;
            } finally {
                if (!result) {
                    pool[i].stop();
                }
            }
        }
        logger.info("Socket processor manager started, pool size=" + pool.length);
    }

    @Override
    protected void doStop() throws Exception
    {
        MultiException exception = new MultiException();
        for (IProcessor<INioSession> processor : pool) {
            try {
                processor.stop();
            } catch (Exception ex) {
                exception.add(ex);
            }
        }
        exception.ifExceptionThrow();
        executor.shutdown();
        logger.info("Socket processor manager stopped");
    }
    
    private IProcessor<INioSession> nextProcessor()
    {
        // The ++ increment here is not atomic, but it does not matter,
        // so long as the value changes sometimes, then connections will
        // be distributed over the available selectors.
        long s = pointer++;
        int index = (int)(s % pool.length);
        return pool[index];
    }
    
    private void checkState()
    {
        if (!isRunning()) {
            throw new IllegalStateException("Invalid processor pool state, state:" + getState());
        }
    }
}
