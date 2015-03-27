
package com.elusivehawk.caelum.render.gl;

import java.util.concurrent.atomic.AtomicBoolean;
import com.elusivehawk.caelum.render.Deletables;
import com.elusivehawk.caelum.render.IBindable;
import com.elusivehawk.caelum.render.IDeletable;
import com.elusivehawk.caelum.render.RenderContext;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public abstract class GLObject implements IBindable, IDeletable
{
	private final AtomicBoolean deleted = new AtomicBoolean(false), init = new AtomicBoolean(false);
	
	@Override
	public void delete()
	{
		if (this.isDeleted())
		{
			return;
		}
		
		if (!this.isInitiated())
		{
			return;
		}
		
		assert !this.isBound();
		
		this.deleteImpl();
		
		this.deleted.set(true);
		
	}
	
	@Override
	public boolean bind(RenderContext rcon)
	{
		if (this.isDeleted())
		{
			return false;
		}
		
		if (!this.isInitiated())
		{
			this.initiate(rcon);
			
			Deletables.instance().register(this);
			
			this.init.set(true);
			
		}
		
		return this.bindImpl(rcon);
	}
	
	public boolean isInitiated()
	{
		return this.init.get();
	}
	
	public boolean isDeleted()
	{
		return this.deleted.get();
	}
	
	protected abstract boolean bindImpl(RenderContext rcon);
	
	protected abstract void initiate(RenderContext rcon);
	
	protected abstract void deleteImpl();
	
}
