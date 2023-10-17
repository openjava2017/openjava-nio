package com.openjava.nio.provider.processor;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.openjava.nio.infrastructure.ILifeCycle;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;

public interface IProcessor<T extends INioSession> extends IProcessorChain, ILifeCycle
{
    long id();
    
    void registerWriter(T session);
    
    void unregisterSession(T session);
}
