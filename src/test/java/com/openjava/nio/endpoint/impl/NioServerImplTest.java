package com.openjava.nio.endpoint.impl;

import com.openjava.nio.endpoint.AbstractNioServer;
import com.openjava.nio.provider.NioNetworkProvider;
import com.openjava.nio.provider.session.INioSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioServerImplTest
{
    private static Logger LOG = LoggerFactory.getLogger(NioServerImplTest.class);

    public static void main(String... args)
    {
        NioNetworkProvider provider = new NioNetworkProvider();
        try {
            provider.start();
            NioServerImpl server = new NioServerImpl();
            server.setNetworkProvider(provider);
            server.setHost("127.0.0.1");
            server.setPort(6089);
            server.start();
        } catch (Exception ex) {
            LOG.error("Unknown exception", ex);
        }
    }

    private static class NioServerImpl extends AbstractNioServer
    {
        @Override
        public void sessionReceived(INioSession session, byte[] packet)
        {
            try {
                LOG.info("Received: " + new String(packet, "utf-8"));
                String message = "hell client";
                session.send(message.getBytes("utf-8"));
            } catch (Exception ex) {
                LOG.info("Unknown exception", ex);
            }
        }
    }
}
