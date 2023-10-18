package com.openjava.nio.infrastructure;

public abstract class LifeCycle implements ILifeCycle 
{
	private final static int FAILED = -1;
	private final static int STOPPED = 0;
	private final static int STARTING = 1;
	private final static int STARTED = 2;
	private final static int STOPPING = 3;
	
    private final Object lock = new Object();
    private volatile int state = STOPPED;

	@Override
	public void start() throws Exception
	{
		// Double check
		if (state == STARTED || state == STARTING) {
			return;
		}
		
		synchronized (lock) {
            try {
            	if (state == STARTED || state == STARTING) {
                    return;
            	}
            	
                setState(STARTING);
                doStart();
                setState(STARTED);
            } catch (Throwable ex) {
            	setState(FAILED);
                throw new Exception("Start the component exception", ex);
            }
        }
	}

	@Override
	public void stop() throws Exception
	{
		// Double check
		if (state == STOPPING || state == STOPPED) {
            return;
        }
		
		synchronized (lock) {
            try {
                if (state == STOPPING || state == STOPPED) {
                    return;
                }
                setState(STOPPING);
                doStop();
                setState(STOPPED);
            } catch (Throwable ex) {
            	setState(FAILED);
            	throw new Exception("Stop the component exception", ex);
            }
        }
	}
	
	protected void doStart() throws Exception
    {
    }

    protected void doStop() throws Exception
    {
    }

	@Override
	public boolean isRunning() 
	{
		final int _state = state;
        return _state == STARTED || _state == STARTING;
	}

	@Override
	public boolean isStarted()
	{
		return state == STARTED;
	}
	
	@Override
	public String getState()
	{
	    switch (state) {
	    case STARTED:
	        return "started";
	    case STOPPED:
	        return "stopped";
	    case STARTING:
	        return "starting";
	    case STOPPING:
	        return "stopping";
	    case FAILED:
	        return "failed";
	    default:
	        return "unknown";
	    }
	}

	private void setState(int state)
	{
		this.state = state;
	}
}
