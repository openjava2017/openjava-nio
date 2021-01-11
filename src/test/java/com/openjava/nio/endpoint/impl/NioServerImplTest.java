package com.openjava.nio.endpoint.impl;

import com.openjava.nio.endpoint.AbstractNioServer;
import com.openjava.nio.provider.NioNetworkProvider;
import com.openjava.nio.provider.session.INioSession;
import org.apache.commons.lang3.StringUtils;
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
            server.setHost("10.28.1.188");
            server.setPort(9527);
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
                String xmlRequest = new String(packet, "GBK");
                LOG.info("Received: " + xmlRequest);
                if (xmlRequest.indexOf("<tr_code>300001</tr_code>") >= 0) {
                    String xmlResponse = "<ap><head><tr_code>300001</tr_code><cms_corp_no></cms_corp_no><user_no></user_no><org_code></org_code><serial_no>12345678</serial_no><req_no></req_no><tr_acdt>20201228</tr_acdt><tr_time>093946</tr_time><succ_flag>0</succ_flag><ret_code>0000</ret_code><ret_info>success</ret_info><ext_info></ext_info><file_flag>0</file_flag><reserved></reserved></head><body></body></ap>";
                    session.send(wrapXmlResponse(xmlResponse).getBytes("GBK"));
                } else if (xmlRequest.indexOf("<tr_code>200205</tr_code>") >= 0) {
                    String xmlResponse = "<ap><head><tr_code>200205</tr_code><cms_corp_no></cms_corp_no><user_no></user_no><org_code></org_code><serial_no></serial_no><req_no></req_no><tr_acdt>20201228</tr_acdt><tr_time>093946</tr_time><succ_flag>0</succ_flag><ret_code>0000</ret_code><ret_info>fuck success</ret_info><ext_info></ext_info><file_flag>0</file_flag><reserved></reserved></head><body><stat>9</stat><cert_no>12345678</cert_no><req_no>12345678</req_no><serial_no>23456789</serial_no><error_info></error_info></body></ap>";
                    session.send(wrapXmlResponse(xmlResponse).getBytes("GBK"));
                }
            } catch (Exception ex) {
                LOG.info("Unknown exception", ex);
            }
        }

        private String wrapXmlResponse(String xmlResponse) {
            try {
                String body = "00".concat(xmlResponse);
                int bodySize = body.getBytes("GBK").length;
                String header = StringUtils.rightPad(String.valueOf(bodySize), 10);
                return header.concat(body);
            } catch (Exception ex) {
                throw new IllegalArgumentException("data protocol error");
            }
        }
    }
}
