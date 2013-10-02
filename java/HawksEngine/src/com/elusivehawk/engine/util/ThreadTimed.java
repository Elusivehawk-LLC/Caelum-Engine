
package com.elusivehawk.engine.util;

/**
 * 
 * Helper class for timed threading.
 * 
 * @author Elusivehawk
 */
public abstract class ThreadTimed extends ThreadStoppable
{
	@Override
	public void run()
	{
		int updates = 0;
		long delta, lastTime = System.currentTimeMillis();
		
		while (this.running)
		{
			delta = (System.currentTimeMillis() - this.getDelta());
			
			if (delta >= lastTime)
			{
				this.update();
				
				updates++;
				
				if (updates == this.getTargetUpdateCount())
				{
					try
					{
						Thread.sleep(1L);
						
					}
					catch (InterruptedException e){}
					
				}
				
			}
			
		}
		
	}
	
	public abstract void update();
	
	public abstract int getDelta();
	
	public abstract int getTargetUpdateCount();
	
}
