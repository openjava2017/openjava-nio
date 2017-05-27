package com.openjava.nio.provider.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionHandler
{
    private static Logger logger = LoggerFactory.getLogger(SessionHandler.class);
    
    public ByteBuffer processReadRequest(INioSession session) throws IOException
    {
        return null;
//        int numOfByte = -1;
//        ByteBuffer headerBuffer = session.getHeaderBuffer();
//        ByteBuffer body = session.getFlagmentBuffer();
//        SocketChannel channel = session.getChannel();
//
//        if (body == null) {
//            numOfByte = channel.read(headerBuffer);
//            if(numOfByte == -1) {
//                throw new IOException("Cannot read from socket session[SID=" + session.getId() + "]");
//            }
//
//            if (headerBuffer.hasRemaining()) {
//                return null;
//            } else {
//                headerBuffer.flip();
//                int bodySize = headerBuffer.getInt();
//                body = ByteBuffer.allocate(bodySize - 4).order(ByteOrder.LITTLE_ENDIAN);
//            }
//        }
//
//        numOfByte = channel.read(body);
//        if(numOfByte == -1) {
//            throw new IOException("Cannot read from socket session[SID=" + session.getId() + "]");
//        } else {
//            logger.debug("{} bytes read from session[SID={}]", numOfByte, session.getId());
//        }
//
//        if (body.hasRemaining()) {
//            session.setFlagmentBuffer(body);
//            return null;
//        } else {
//            headerBuffer.clear();
//            session.setFlagmentBuffer(null);
//            body.flip();
//            return body;
//        }
    }
    
    public void processWriteRequest(INioSession session) throws IOException
    {
//        Queue<ByteBuffer> dataBuffer = session.getDataBuffer();
//        try {
//            do {
//                ByteBuffer request = dataBuffer.peek();
//                if (request != null) {
//                    int num = session.getChannel().write(request);
//                    if(!request.hasRemaining()) {
//                        logger.debug("{} bytes written to session[SID={}]", num, session.getId());
//                        dataBuffer.remove();
//                    } else {
//                        logger.debug("{} bytes written, {} bytes left[SID={}]", num, request.remaining(), session.getId());
//                        break;
//                    }
//                }
//            } while (!dataBuffer.isEmpty());
//        } catch (IOException iex) {
//            logger.error("Failed to write to a session[SID={}]", session.getId(), iex);
//        } finally {
//            if(dataBuffer.isEmpty()) {
//                updateKeyWriteInterests(session, false);
//            }
//        }
    }
    
    protected void updateKeyWriteInterests(INioSession session, boolean isInterested)
    {
//        SelectionKey key = session.getSelectionKey();
//
//        if (key == null) {
//            return;
//        }
//
//        int newInterestOps = key.interestOps();
//
//        if (isInterested) {
//            newInterestOps |= SelectionKey.OP_WRITE;
//        } else {
//            newInterestOps &= ~SelectionKey.OP_WRITE;
//        }
//
//        key.interestOps(newInterestOps);
    }
}
