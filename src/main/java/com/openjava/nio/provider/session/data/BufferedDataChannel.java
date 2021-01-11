package com.openjava.nio.provider.session.data;

import com.openjava.nio.exception.ClosedSessionException;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class BufferedDataChannel implements IDataChannel
{
    private static Logger LOG = LoggerFactory.getLogger(BufferedDataChannel.class);

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private INioSession session;

    private final Queue<ByteBuffer> dataBuffer = new ConcurrentLinkedQueue<ByteBuffer>();

    private final List<ISessionDataListener> listeners = new CopyOnWriteArrayList<ISessionDataListener>();

    public BufferedDataChannel(INioSession session)
    {
        this.session = session;
    }

    @Override
    public byte[] read() throws IOException
    {
        SocketChannel channel = session.getChannel();
        ByteBuffer data = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        int numOfByte = channel.read(data);
        if (numOfByte == 0) {
            return null;
        }
        if(numOfByte == -1) {
            throw new ClosedSessionException("Nio session[SID=" + session.getId() + "] being closed by remote end");
        }
        LOG.debug("{} bytes read from session[SID={}]", numOfByte, session.getId());
        data.flip();
        byte[] packet = new byte[data.remaining()];
        data.get(packet);
        // fire data received event
        fireDataReceived(packet);
        return packet;
    }

    @Override
    public void send(byte[] packet)
    {
        if (packet != null) {
            ByteBuffer data = ByteBuffer.allocate(packet.length).order(ByteOrder.LITTLE_ENDIAN);
            data.put(packet);
            data.flip();
            dataBuffer.add(data);
            session.getProcessor().registerWriter(session);
        }
    }

    @Override
    public void write() throws IOException
    {
        try {
            do {
                ByteBuffer request = dataBuffer.peek();
                if (request != null) {
                    int num = session.getChannel().write(request);
                    if(!request.hasRemaining()) {
                        LOG.debug("{} bytes written to session[SID={}]", num, session.getId());
                        dataBuffer.remove();
                    } else {
                        LOG.debug("{} bytes written, {} bytes left[SID={}]", num, request.remaining(), session.getId());
                        break;
                    }
                }
            } while (!dataBuffer.isEmpty());
        } catch (IOException iex) {
            LOG.error("Failed to write to a session[SID={}]", session.getId());
            throw iex;
        } finally {
            if(dataBuffer.isEmpty()) {
                updateKeyWriteInterests(session, false);
            }
        }
    }

    @Override
    public void registerListeners(ISessionDataListener... listeners)
    {
        if (listeners != null) {
            Collections.addAll(this.listeners, listeners);
        }
    }

    private void fireDataReceived(byte[] packet)
    {
        for (ISessionDataListener listener : listeners) {
            listener.onDataReceived(session, packet);
        }
    }

    protected void updateKeyWriteInterests(INioSession session, boolean isInterested)
    {
        SelectionKey key = session.getSelectionKey();

        if (key == null) {
            return;
        }

        int newInterestOps = key.interestOps();

        if (isInterested) {
            newInterestOps |= SelectionKey.OP_WRITE;
        } else {
            newInterestOps &= ~SelectionKey.OP_WRITE;
        }

        key.interestOps(newInterestOps);
    }
}
