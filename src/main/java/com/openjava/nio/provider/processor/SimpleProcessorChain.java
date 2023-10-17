package com.openjava.nio.provider.processor;

import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SimpleProcessorChain implements IProcessorChain {
    private volatile long pointer = 0L;
    private final IProcessor<INioSession>[] processors;

    public SimpleProcessorChain(IProcessor<INioSession>[] processors)
    {
        this.processors = processors;
    }

    @Override
    public void registerServer(ServerSocketChannel serverSocket, ISessionEventListener eventListener, ISessionDataListener dataListener)
    {
        nextProcessor().registerServer(serverSocket, eventListener, dataListener);
    }

    @Override
    public void registerConnection(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener, long timeoutInMillis)
    {
        nextProcessor().registerConnection(channel, eventListener, dataListener, timeoutInMillis);
    }

    @Override
    public void registerSession(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener)
    {
        nextProcessor().registerSession(channel, eventListener, dataListener);
    }

    private IProcessor<INioSession> nextProcessor()
    {
        // The ++ increment here is not atomic, but it does not matter,
        // so long as the value changes sometimes, then connections will
        // be distributed over the available selectors.
        long s = pointer++;
        int index = (int) (s % processors.length);
        return processors[index];
    }
}
