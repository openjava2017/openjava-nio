package com.openjava.nio.provider.session;

import com.openjava.nio.provider.processor.IProcessor;
import com.openjava.nio.provider.session.data.BufferedDataChannel;
import com.openjava.nio.provider.session.data.IDataChannel;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class NioSession implements INioSession
{
    private static final AtomicLong idGenerator = new AtomicLong(0);

    private final long sessionId;
    
    private final SelectionKey key;

    private final SocketChannel channel;

    private final IProcessor<INioSession> processor;

    private final IDataChannel dataChannel;

    private volatile long lastUsedTime;

    protected final AtomicReference<SessionState> state;

    protected NioSession(SocketChannel channel, SelectionKey key, IProcessor<INioSession> processor)
    {
        this.sessionId = idGenerator.incrementAndGet();
        this.channel = channel;
        this.key = key;
        this.processor = processor;
//        this.dataChannel =  new SessionDataChannel(this);
        this.dataChannel =  new BufferedDataChannel(this);
        this.lastUsedTime = System.currentTimeMillis();
        this.state = new AtomicReference<>(SessionState.CONNECTED);
    }
    
    @Override
    public long getId()
    {
        return this.sessionId;
    }

    @Override
    public SocketChannel getChannel()
    {
        return this.channel;
    }
    
    @Override
    public SelectionKey getSelectionKey()
    {
        return this.key;
    }

    @Override
    public IProcessor<INioSession> getProcessor()
    {
        return this.processor;
    }

    @Override
    public IDataChannel getDataChannel()
    {
        checkState();
        return this.dataChannel;
    }

    @Override
    public void send(byte[] packet)
    {
        getDataChannel().send(packet);
    }

    @Override
    public long getLastUsedTime()
    {
        return lastUsedTime;
    }

    @Override
    public SessionState getState()
    {
        return this.state.get();
    }
    

    @Override
    public void kick()
    {
        lastUsedTime = System.currentTimeMillis();
    }

    @Override
    public void destroy()
    {
        if (state.compareAndSet(SessionState.CONNECTED, SessionState.CLOSING)) {
            getProcessor().unregisterSession(this);
        }
    }

    private void checkState()
    {
        if (getState() != SessionState.CONNECTED) {
            throw new IllegalStateException("Invalid session state, state:" + getState());
        }
    }

    public static NioSession create(SocketChannel channel, SelectionKey key, IProcessor<INioSession> processor)
    {
        return new NioSession(channel, key, processor);
    }
}