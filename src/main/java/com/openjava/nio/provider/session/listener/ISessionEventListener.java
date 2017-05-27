package com.openjava.nio.provider.session.listener;

import com.openjava.nio.provider.session.INioSession;

public interface ISessionEventListener
{
    /**
     * For both server and client session,
     * fired when a new socket channel is connected or accepted
     */
    void onSessionCreated(INioSession session);

    /**
     * For both server and client session,
     * fired when a new socket channel is disconnected or closed
     */
    void onSessionClosed(INioSession session);

    /**
     * Only for client socket, fired when a client socket connect timeout
     */
    void onSocketConnectTimeout();
}
