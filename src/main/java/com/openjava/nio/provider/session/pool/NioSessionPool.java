package com.openjava.nio.provider.session.pool;

import com.openjava.nio.infrastructure.LifeCycle;
import com.openjava.nio.provider.session.INioSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioSessionPool extends LifeCycle
{
    private static Logger logger = LoggerFactory.getLogger(NioSessionPool.class);

    private transient Node<INioSession> head = null;
    private transient Node<INioSession> tail = head;
    
    private volatile int count = 0;
    
    private NioSessionPool()
    {
    }
    
    public synchronized INioSession borrowSession()
    {
        checkState();
        
        INioSession session;
        for ( ; ;) {
            Node<INioSession> node = removeFirst();
            if (node != null) {
                session = node.item;
                if (validateSession(session)) { // validate the session
                    addLast(node);
                    return session;
                }
                count --;
                continue; // continue next loop
            } else {
                break;
            }
        }
        
        return null;
    }
    
    public synchronized void addSession(INioSession session)
    {
        if (!isRunning()) {
            logger.error("Invalid session pool state, close the incoming session");
            session.destroy();
        }
        
        logger.info("Session [SID={}] added in the pool", session.getId());
        Node<INioSession> node = new Node<INioSession>(session);
        addLast(node);
        count ++;
    }
    
    public synchronized void removeSession(INioSession session)
    {
        if (!isEmpty()) {
            Node<INioSession> previous = null;
            Node<INioSession> node = head;
            while (node != null) {
                if (node.item == session) {
                	if (tail == node) {
                		tail = previous;
                	}
                    if (previous == null) { // It's the first node who matches condition
                        head = head.next;
                    } else {
                        previous.next = node.next;
                        node.next = null;
                    }
                    count --;
                    break;
                } else {
                    previous = node;
                    node = node.next;
                }
            }
        }
    }
    
    public synchronized int sessionCount()
    {
        return count;
    }
    
    public static NioSessionPool create()
    {
        return new NioSessionPool();
    }
    
    protected boolean validateSession(INioSession session)
    {
        return session.getChannel().isConnected();
    }
    
    private void addLast(Node<INioSession> node)
    {
        if (isEmpty()) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
    }
    
    private Node<INioSession> removeFirst()
    {
        if (!isEmpty()) {
            Node<INioSession> node = head;
            head = head.next;
            node.next = null;
            return node;
        }
        
        return null;
    }
    
    private boolean isEmpty()
    {
        return head == null;
    }
    
    protected void doStart() throws Exception
    {
    }

    protected void doStop() throws Exception
    {
        INioSession session;
        for (Node<INioSession> node = removeFirst(); node != null; node = removeFirst()) {
            session = node.item;
            if (session != null && validateSession(session)) {
                session.destroy();
            }
        }
    }
    
    private void checkState()
    {
        if (!isRunning()) {
            throw new IllegalStateException("Invalid processor state, state:" + getState());
        }
    }
    
    class Node<E>
    {
        public Node(E item) 
        {
            this.item = item;
        }
        
        E item;
        Node<E> next;
    }
}
