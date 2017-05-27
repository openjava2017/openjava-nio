package com.openjava.nio.endpoint;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.openjava.nio.infrastructure.LifeCycle;
import com.openjava.nio.provider.NioNetworkProvider;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;
import com.openjava.nio.provider.session.pool.NioSessionPool;
import com.openjava.nio.util.QueuedThreadPool;
import com.openjava.nio.util.StringUtils;
import com.openjava.nio.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.util.ScheduledExecutor;

public abstract class AbstractNioServer extends LifeCycle implements ISessionEventListener, ISessionDataListener
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String host;
    private int port;

    private NioNetworkProvider networkProvider;
    private long sessionScanPeriodMillis = 5000;
    private long sessionTimeOutInMillis = 15 * 1000;
    private NioSessionPool pool = NioSessionPool.create();
    private ThreadPool threadPool = new QueuedThreadPool();
    private ScheduledExecutor scheduler =  new ScheduledExecutor(this.getClass().getSimpleName() + "-Scanner", true);

    @Override
    public void onSessionCreated(INioSession session)
    {
        pool.addSession(session);
    }

    @Override
    public void onSessionClosed(INioSession session)
    {
        pool.removeSession(session);
    }

    @Override
    public void onSocketConnectTimeout()
    {
        // Ignore
    }

    public abstract void sessionReceived(INioSession session, byte[] packet);

    @Override
    public void onDataReceived(final INioSession session, final byte[] packet)
    {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                sessionReceived(session, packet);
            }
        });
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
    public void setNetworkProvider(NioNetworkProvider networkProvider)
    {
        this.networkProvider = networkProvider;
    }

    @SuppressWarnings("unused")
    public void setSessionScanPeriodMillis(long sessionScanPeriodMillis)
    {
        this.sessionScanPeriodMillis = sessionScanPeriodMillis;
    }

    @SuppressWarnings("unused")
    public void setSessionTimeOutInMillis(long sessionTimeOutInMillis)
    {
        this.sessionTimeOutInMillis = sessionTimeOutInMillis;
    }
    
    @Override
    protected void doStart() throws Exception
    {
        if (StringUtils.isBlank(host) || port <= 1024) {
            throw new IllegalArgumentException("Invalid host and port: " + host + "," + port);
        }

        pool.start();
        threadPool.start();

        InetSocketAddress address = new InetSocketAddress(host, port);
        networkProvider.registerServer(address, this, this);
        scheduler.schedule(new Scavenger(), sessionScanPeriodMillis, TimeUnit.MILLISECONDS);
    }
    
    @Override
    protected void doStop() throws Exception
    {
        pool.stop();
        scheduler.shutdown();
    }
    
    private class Scavenger implements Runnable
    {
        @Override
        public void run()
        {
            if (!isRunning()) {
                return;
            }
            
            long now = System.currentTimeMillis();
            try {
                INioSession session = pool.borrowSession();
                if (session != null) {
                    touchSession(session, now);
                }
            } finally {
                scheduler.schedule(this, sessionScanPeriodMillis, TimeUnit.MILLISECONDS);
            }
        }
        
        private void touchSession(INioSession session, long when)
        {
            if (session != null) {
                long lastUsedTime = session.getLastUsedTime();
                if (when - lastUsedTime > sessionTimeOutInMillis) {
                    logger.info("Expired NIO session found, close it[SID={}]", session.getId());
                    // Close the expired session, no need remove it in the pool
                    // NioSessionPool will remove the closed session automatically
                    // See NioSessionPool.borrowSession method for details
                    session.destroy();
                }
            }
        }
    }
}
