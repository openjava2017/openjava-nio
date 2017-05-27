package com.openjava.nio.endpoint;

import com.openjava.nio.exception.ConnectTimeoutException;
import com.openjava.nio.provider.NioNetworkProvider;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;
import com.openjava.nio.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractNioClient
{
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private String host;
    private int port;

    private NioNetworkProvider networkProvider;
    private long connTimeOutInMillis = 10 * 1000;

    public abstract byte[] sendAndReceived(byte[] packet, long receivedTimeOutInMillis) throws IOException, InterruptedException;

    public INioSession getSession(ISessionDataListener dataListener) throws IOException
    {
        NioConnectFactory sessionFactory = new NioConnectFactory();
        INioSession session = sessionFactory.createSession(dataListener);
        if (session == null) {
            throw new ConnectTimeoutException("Session created timeout");
        }
        return session;
    }


    @SuppressWarnings("unused")
    public void setHost(String host)
    {
        this.host = host;
    }

    @SuppressWarnings("unused")
    public void setPort(int port)
    {
        this.port = port;
    }

    @SuppressWarnings("unused")
    public void setConnTimeOutInMillis(long connTimeOutInMillis)
    {
        this.connTimeOutInMillis = connTimeOutInMillis;
    }

    @SuppressWarnings("unused")
    public void setNetworkProvider(NioNetworkProvider networkProvider)
    {
        this.networkProvider = networkProvider;
    }

    private class NioConnectFactory implements ISessionEventListener
    {
        private volatile INioSession session;

        private final ReentrantLock lock = new ReentrantLock();

        /** Condition for waiting takes */
        private final Condition hasSession = lock.newCondition();

        @Override
        public void onSessionCreated(INioSession session)
        {
            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();

                try {
                    this.session = session;
                    hasSession.signalAll();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("onSessionCreated thread interrupted");
            }
        }

        public INioSession createSession(ISessionDataListener dataListener) throws IOException
        {
            if (StringUtils.isBlank(host) || port <= 1024) {
                throw new IllegalArgumentException("Invalid host and port: " + host + "," + port);
            }

            InetSocketAddress address = new InetSocketAddress(host, port);
            networkProvider.registerConnection(address, this,  dataListener, connTimeOutInMillis);

            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();
                try {
                    if (session == null) {
                        hasSession.await(connTimeOutInMillis, TimeUnit.MILLISECONDS);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("createSession thread interrupted");
            }

            return session;
        }

        @Override
        public void onSessionClosed(INioSession session)
        {
            // Ignore closed event
        }

        @Override
        public void onSocketConnectTimeout()
        {
            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();

                try {
                    hasSession.signalAll();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("onSocketConnectTimeout thread interrupted");
            }
        }
    }
}
