
package com.elusivehawk.engine.render;

import com.elusivehawk.engine.IContext;
import com.elusivehawk.engine.IThreadContext;
import com.elusivehawk.engine.ThreadCaelum;
import com.elusivehawk.util.Internal;
import com.elusivehawk.util.ShutdownHelper;

/**
 * 
 * The primary thread for rendering the game's window.
 * 
 * @author Elusivehawk
 */
@Internal
public class ThreadGameRender extends ThreadCaelum implements IThreadContext
{
	protected final RenderContext rcon;
	
	protected int fps = 30;
	
	@SuppressWarnings("unqualified-field-access")
	public ThreadGameRender(RenderContext con)
	{
		rcon = con;
		
	}
	
	@Override
	public boolean initiate()
	{
		if (!super.initiate())
		{
			return false;
		}
		
		if (!this.rcon.initContext())
		{
			return false;
		}
		
		this.fps = this.rcon.getFPS();
		
		return true;
	}
	
	@Override
	public void update(double delta) throws Throwable
	{
		if (this.rcon.getDisplay().isCloseRequested())
		{
			this.rcon.onDisplayClosed(this.rcon.getDisplay());
			
			ShutdownHelper.exit(0);
			
		}
		
		this.rcon.update(delta);
		this.rcon.getDisplay().updateDisplay();
		
	}
	
	@Override
	public synchronized void setPaused(boolean pause)
	{
		super.setPaused(pause);
		this.rcon.setPaused(pause);
		
	}
	
	@Override
	public int getTargetUpdateCount()
	{
		return this.fps;
	}
	
	@Override
	public IContext getContext()
	{
		return this.rcon;
	}
	
}
