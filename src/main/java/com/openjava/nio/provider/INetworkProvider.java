package com.openjava.nio.provider;

import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public interface INetworkProvider
{
    void registerConnection(SocketAddress remoteAddress, ISessionEventListener eventListener,
        ISessionDataListener dataListner, long timeoutInMillis) throws IOException;

    void registerServer(SocketAddress localAddress, ISessionEventListener eventListener,
        ISessionDataListener dataListner) throws IOException;

    void registerSession(SocketChannel channel, ISessionEventListener eventListener,
        ISessionDataListener dataListener);
}
