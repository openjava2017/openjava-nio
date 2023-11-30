package com.openjava.nio.provider.processor;

import com.openjava.nio.infrastructure.ILifeCycle;
import com.openjava.nio.provider.session.INioSession;

public interface IProcessor<T extends INioSession> extends IProcessorChain, ILifeCycle
{
    long id();
    
    void registerWriter(T session);
    
    void unregisterSession(T session);
}
