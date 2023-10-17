package com.openjava.nio.provider.processor;

import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface IProcessorChain
{
    void registerServer(ServerSocketChannel serverSocket, ISessionEventListener eventListener, ISessionDataListener dataListener);

    void registerConnection(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener, long timeoutInMillis);

    void registerSession(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener);
}
