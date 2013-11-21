
package com.elusivehawk.engine.core.concurrent;

import com.elusivehawk.engine.core.IUpdatable;

/**
 * 
 * Helper class for timed threading.
 * 
 * @author Elusivehawk
 */
public abstract class ThreadTimed extends ThreadStoppable implements IUpdatable
{
	private int updates = 0, updateCount;
	private long sleepTime = 0L;
	private double time, nextTime, delta;
	
	@Override
	public boolean initiate()
	{
		this.updateCount = this.getTargetUpdateCount();
		this.delta = (1000000000.0 / this.updateCount);
		this.nextTime = (System.nanoTime() / 1000000000.0) + this.delta;
		
		return true;
	}
	
	@Override
	public final void rawUpdate()
	{
		if (this.getTargetUpdateCount() != this.updateCount)
		{
			this.nextTime -= this.delta;
			
			this.updateCount = this.getTargetUpdateCount();
			this.delta = (1000000000.0 / this.updateCount);
			
			this.nextTime += this.delta;
			
		}
		
		this.time = System.nanoTime() / 1000000000.0;
		
		if ((this.nextTime - this.time) > this.getMaxDelta()) this.nextTime = this.time;
		
		if ((this.time + this.delta) >= this.nextTime)
		{
			this.nextTime += this.delta;
			this.updates++;
			
			this.update(this.time - this.nextTime);
			
			if (this.updates >= this.updateCount)
			{
				this.sleepTime = 1L;
				
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
