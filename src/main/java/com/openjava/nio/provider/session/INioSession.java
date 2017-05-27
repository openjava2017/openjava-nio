package com.openjava.nio.provider.session;

import com.openjava.nio.infrastructure.IExpirable;
import com.openjava.nio.provider.processor.IProcessor;
import com.openjava.nio.provider.session.data.IDataChannel;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface INioSession extends IExpirable
{
    long getId();

    SelectionKey getSelectionKey();
    
    SocketChannel getChannel();
    
    IProcessor<INioSession> getProcessor();

    IDataChannel getDataChannel();

    void send(byte[] packet);

    SessionState getState();
    
    void destroy();
}
