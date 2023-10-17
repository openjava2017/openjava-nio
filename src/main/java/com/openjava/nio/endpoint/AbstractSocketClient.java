package com.openjava.nio.endpoint;

import com.openjava.nio.exception.CreateSessionException;
import com.openjava.nio.provider.processor.*;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import com.openjava.nio.provider.session.listener.ISessionEventListener;
import com.openjava.nio.util.AssertUtils;
import com.openjava.nio.util.ScheduledExecutor;
import com.openjava.nio.util.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractSocketClient {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSocketClient.class);
    private static final int CONNECT_TIMEOUT_MILLIS = 10 *1000;

    private final long connTimeOutInMillis;
    private final IProcessor<INioSession>[] processors;
    private final IProcessorChain processorChain;

    public AbstractSocketClient() throws Exception {
        this(CONNECT_TIMEOUT_MILLIS, Runtime.getRuntime().availableProcessors(), Executors.newCachedThreadPool());
    }

    public AbstractSocketClient(int connTimeOutInMillis, int processorThreads, ExecutorService executor) throws Exception {
        AssertUtils.isTrue(connTimeOutInMillis > 0, "invalid connTimeOutInMillis value");
        AssertUtils.isTrue(processorThreads > 0, "invalid processorThreads value");
        AssertUtils.notNull(executor, "executor cannot be null");

        this.connTimeOutInMillis = connTimeOutInMillis;
        this.processors = new IProcessor[processorThreads];
        this.processorChain = new SimpleProcessorChain(this.processors);
        Scheduler scheduler = new ScheduledExecutor("connect-timeout-scanner", true);
        for (int i = 0; i < processors.length; i++) {
            this.processors[i] = new NioSessionProcessor(i, processorChain, executor, scheduler);
            this.processors[i].start();
        }

    }

    public INioSession getSession(String host, int port, ISessionDataListener dataListener) throws IOException {
        NioConnectFactory sessionFactory = new NioConnectFactory(host, port);
        INioSession session = sessionFactory.createSession(dataListener);
        if (session == null) {
            throw new CreateSessionException("Failed to create nio session");
        }
        return session;
    }

    private class NioConnectFactory implements ISessionEventListener {
        private final String host;
        private final int port;

        private volatile INioSession session;

        private final ReentrantLock lock = new ReentrantLock();

        /** Condition for waiting takes */
        private final Condition hasSession = lock.newCondition();

        public NioConnectFactory(String host, int port) {
            AssertUtils.notEmpty(host, "host cannot be null");
            AssertUtils.isTrue(port > 1024, "Invalid port value");

            this.host = host;
            this.port = port;
        }

        @Override
        public void onSessionCreated(INioSession session) {
            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();

                try {
                    this.session = session;
                    this.hasSession.signalAll();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("onSessionCreated thread interrupted");
            }
        }

        public INioSession createSession(ISessionDataListener dataListener) throws IOException {
            InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
            boolean result = false;
            SocketChannel channel = null;

            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(remoteAddress);
                processorChain.registerConnection(channel, this, dataListener, connTimeOutInMillis);
                result = true;
            } finally {
                if (!result) {
                    ProcessorUtils.closeQuietly(channel);
                }
            }

            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();
                try {
                    if (this.session == null) {
                        this.hasSession.await(connTimeOutInMillis, TimeUnit.MILLISECONDS);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("createSession thread interrupted");
            }

            return this.session;
        }

        @Override
        public void onSessionClosed(INioSession session) {
            // Ignore closed event
        }

        @Override
        public void onSocketConnectFailed(IOException ex) {
            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();

                try {
                    hasSession.signalAll();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("onSocketConnectFailed thread interrupted");
            }
        }
    }
}
