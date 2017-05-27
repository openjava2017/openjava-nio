package com.openjava.nio.infrastructure;

public interface ILifeCycle 
{
	public void start() throws Exception;
	
	public void stop() throws Exception;
	
	public boolean isRunning();
	
	public boolean isStarted();
	
	public String getState();
}