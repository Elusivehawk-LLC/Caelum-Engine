
package com.elusivehawk.caelum.render.old;

import com.elusivehawk.caelum.CaelumEngine;
import com.elusivehawk.caelum.render.RenderContext;
import com.elusivehawk.caelum.render.RenderException;
import com.elusivehawk.util.Logger;
import com.elusivehawk.util.task.ITaskListener;
import com.elusivehawk.util.task.Task;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 * 
 * @deprecated To be removed when OpenGL NG support comes out.
 */
@Deprecated
public abstract class RenderTask extends Task
{
	private int glId = 0;
	
	public RenderTask(ITaskListener tlis)
	{
		super(tlis);
		
	}
	
	@Override
	protected final boolean finishTask() throws Throwable
	{
		boolean ret = true;
		
		try
		{
			this.glId = this.finishRTask(CaelumEngine.renderContext());
			
		}
		catch (RenderException e)
		{
			ret = false;
			
			Logger.log().err(null, e);
			
		}
		
		return ret;
	}
	
	public int getGLId()
	{
		return this.glId;
	}
	
	protected abstract int finishRTask(RenderContext rcon) throws RenderException;
	
}
