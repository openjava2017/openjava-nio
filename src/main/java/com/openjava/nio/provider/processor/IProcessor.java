package com.openjava.nio.provider.processor;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.openjava.nio.infrastructure.ILifeCycle;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;

public interface IProcessor<T extends INioSession> extends ILifeCycle
{
    long id();
    
    void registerServer(ServerSocketChannel serverSocket, ISessionEventListener eventListener,
        ISessionDataListener dataListner);
    
    void registerConnection(SocketChannel channel, ISessionEventListener eventListener,
        ISessionDataListener dataListner, long timeoutInMillis);
    
    void registerSession(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener);
    
    void registerWriter(INioSession session);
    
    void unregisterSession(T session);
}
