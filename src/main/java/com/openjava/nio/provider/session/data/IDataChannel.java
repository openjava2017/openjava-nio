package com.openjava.nio.provider.session.data;

import com.openjava.nio.provider.session.listener.ISessionDataListener;

import java.io.IOException;

public interface IDataChannel
{
    byte[] read() throws IOException;

    void send(byte[] packet);

    void write() throws IOException;

    void registerListeners(ISessionDataListener... listeners);
}
