package com.openjava.nio.endpoint.impl;

import com.openjava.nio.provider.NioNetworkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodNioClientTest
{
    private static Logger LOG = LoggerFactory.getLogger(PeriodNioClientTest.class);

    public static void main(String... args)
    {
        String message = "hell server";
        NioNetworkProvider provider = new NioNetworkProvider();
        try {
            provider.start();
            PeriodNioClient client = new PeriodNioClient();
            client.setNetworkProvider(provider);
            client.setHost("127.0.0.1");
            client.setPort(6089);
            byte[] packet = client.sendAndReceived(message.getBytes("utf-8"), 4000L);
            LOG.info("Received: " + new String(packet, "utf-8"));
        } catch (Exception ex) {
            LOG.error("Unknown exception", ex);
        }
    }
}
