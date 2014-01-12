
package com.elusivehawk.engine.util;

/**
 * 
 * Abstract class for timed threading.
 * 
 * @author Elusivehawk
 */
public abstract class ThreadTimed extends ThreadStoppable implements IUpdatable
{
	private int updates = 0, updateCount;
	private long sleepTime = 0L;
	private double time, nextTime, delta;
	private boolean initiated = false;
	
	@Override
	public boolean initiate()
	{
		this.updateCount = this.getTargetUpdateCount();
		this.delta = (1000000000.0 / this.updateCount);
		this.nextTime = (System.nanoTime() / 1000000000.0) + this.delta;
		this.initiated = true;
		
		return true;
	}
	
	@Override
	public final void rawUpdate()
	{
		if (!this.initiated)
		{
			return;
		}
		
		if (this.getTargetUpdateCount() != this.updateCount)
		{
			this.nextTime -= this.delta;
			
			this.updateCount = this.getTargetUpdateCount();
			this.delta = (1000000000.0 / this.updateCount);
			
			this.nextTime += this.delta;
			
		}
		
		this.time = System.nanoTime() / 1000000000.0;
		
		if (this.getMaxDelta() > 0 && (this.nextTime - this.time) > this.getMaxDelta()) this.nextTime = this.time;
		
		if ((this.time + this.delta) >= this.nextTime)
		{
			this.updates++;
			
			try
			{
				this.update(this.time - this.nextTime);
				
			}
			catch (Throwable e)
			{
				this.handleException(e);
				
			}
			
			this.nextTime += this.delta;
			
			if (this.updates >= this.updateCount)
			{
				this.sleepTime = 1L;
				
				this.updates = 0;
				
			}
			
		}
		else this.sleepTime = (long)(1000.0 * (this.time - this.nextTime));
		
		if (this.sleepTime > 0L)
		{
			try
			{
				Thread.sleep(this.sleepTime);
				
			}
			catch (Exception e){}
			
			this.sleepTime = 0L;
			
		}
		
	}
	
	public abstract int getTargetUpdateCount();
	
	public abstract double getMaxDelta();
	
}
