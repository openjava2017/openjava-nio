package com.openjava.nio.provider.session;

import com.openjava.nio.provider.processor.IProcessor;
import com.openjava.nio.provider.session.data.IDataChannel;
import com.openjava.nio.provider.session.data.SessionDataChannel;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

public class NioSession implements INioSession
{
    private static final AtomicLong idGenerator = new AtomicLong(0);

    private final long sessionId;
    
    private final SelectionKey key;

    private final SocketChannel channel;

    private final IProcessor<INioSession> processor;

    private final IDataChannel dataChannel;

    private volatile long lastUsedTime;

    protected volatile SessionState state;

    protected NioSession(SocketChannel channel, SelectionKey key, IProcessor<INioSession> processor)
    {
        this.sessionId = idGenerator.incrementAndGet();
        this.channel = channel;
        this.key = key;
        this.processor = processor;
        this.dataChannel =  new SessionDataChannel(this);
        this.lastUsedTime = System.currentTimeMillis();
        this.state = SessionState.CONNECTED;
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
        return this.state;
    }
    

    @Override
    public void kick()
    {
        lastUsedTime = System.currentTimeMillis();
    }

    @Override
    public void destroy()
    {
        if (state == SessionState.CONNECTED) {
            state = SessionState.CLOSING;
            getProcessor().unregisterSession(this);
        }
    }

    private void checkState()
    {
        if (state != SessionState.CONNECTED) {
            throw new IllegalStateException("Invalid session state, state:" + getState());
        }
    }

    public static NioSession create(SocketChannel channel, SelectionKey key, IProcessor<INioSession> processor)
    {
        return new NioSession(channel, key, processor);
    }
}