package com.openjava.nio.provider.session.listener;

import com.openjava.nio.provider.session.INioSession;

public interface ISessionDataListener
{
    void onDataReceived(INioSession session, byte[] packet);
}
