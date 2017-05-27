package com.openjava.nio.provider.session;

import com.openjava.nio.provider.session.listener.ISessionEventListener;

public class SessionContext
{
    private NioSession session;
    private ISessionEventListener listener;

    private SessionContext(NioSession session, ISessionEventListener listener)
    {
        this.session = session;
        this.listener = listener;
    }

    public INioSession session()
    {
        return this.session;
    }

    public void fireSessionCreated()
    {
        if (listener != null) {
            listener.onSessionCreated(session);
        }
    }

    public void fireSessionClosed()
    {
        session.state = SessionState.CLOSED;
        if (listener != null) {
            listener.onSessionClosed(session);
        }
    }

    public static SessionContext create(NioSession session, ISessionEventListener listener)
    {
        return new SessionContext(session, listener);
    }
}
