package com.openjava.nio.endpoint.impl;

import com.openjava.nio.endpoint.AbstractNioClient;
import com.openjava.nio.exception.NioSessionException;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class PeriodNioClient extends AbstractNioClient
{
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Override
    public byte[] sendAndReceived(byte[] packet, long receivedTimeOutInMillis) throws IOException
    {
        INioSession session = null;
        SessionDataContext context = new SessionDataContext();

        try {
            (session = getSession(context)).send(packet);
            return context.read(receivedTimeOutInMillis);
        } finally {
            if (session != null) {
                session.destroy();
            }
        }
    }

    private class SessionDataContext implements ISessionDataListener
    {
        private BlockingQueue<byte[]> data = new ArrayBlockingQueue<>(8);

        @Override
        public void onDataReceived(INioSession session, byte[] packet)
        {
            if (!data.offer(packet)) {
                LOG.error("onDataReceived: Exceed max data buffer size");
            }
        }

        public byte[] read(long receivedTimeOutInMillis) throws NioSessionException
        {
            try {
                return data.poll(receivedTimeOutInMillis, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                throw new NioSessionException("Session read data timeout");
            }
        }
    }
}
