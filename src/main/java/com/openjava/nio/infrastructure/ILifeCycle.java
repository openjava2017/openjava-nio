package com.openjava.nio.infrastructure;

public interface ILifeCycle 
{
	void start() throws Exception;
	
	void stop() throws Exception;
	
	boolean isRunning();
	
	boolean isStarted();
	
	String getState();
}