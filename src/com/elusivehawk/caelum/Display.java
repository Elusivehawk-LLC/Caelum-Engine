
package com.elusivehawk.caelum;

import java.io.Closeable;
import com.elusivehawk.caelum.input.EnumInputType;
import com.elusivehawk.caelum.input.IInputListener;
import com.elusivehawk.caelum.input.InputManager;
import com.elusivehawk.caelum.render.IRenderable;
import com.elusivehawk.caelum.render.RenderContext;
import com.elusivehawk.caelum.render.ThreadGameRender;
import com.elusivehawk.caelum.render.gl.GL1;
import com.elusivehawk.util.IUpdatable;
import com.elusivehawk.util.Logger;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class Display implements Closeable, IUpdatable
{
	private final String name;
	private final IRenderable renderer;
	private final InputManager input;
	
	private DisplaySettings settings = null;
	private RenderContext rcon = null;
	private IDisplayImpl impl = null;
	
	private int width = 0, height = 0;
	private boolean refresh = false, closed = false, close = false, initiated = false;
	
	@SuppressWarnings("unqualified-field-access")
	public Display(String str, DisplaySettings ds, IRenderable r)
	{
		assert ds != null;
		assert r != null;
		
		name = str;
		settings = ds;
		renderer = r;
		
		input = new InputManager(this);
		
	}
	
	@Override
	public void update(double delta) throws Throwable
	{
		if (!this.initiated)
		{
			throw new NullPointerException("Cannot render, display wasn't initiated");
		}
		
		if (this.impl.isCloseRequested() || this.close)
		{
			this.close();
			
			return;
		}
		
		this.impl.preRenderDisplay();
		
		if (this.refresh)
		{
			this.impl.updateSettings(this.settings);
			
			synchronized (this)
			{
				this.height = this.impl.getHeight();
				this.width = this.impl.getWidth();
				
			}
			
			GL1.glClearColor(this.settings.bg);
			
			this.refresh = false;
			
		}
		
		this.rcon.update(delta);
		
		this.impl.updateDisplay();
		
		this.input.update(delta);
		
	}
	
	@Override
	public void close()
	{
		try
		{
			this.rcon.close();
			this.input.close();
			this.impl.close();
			
		}
		catch (Throwable e)
		{
			Logger.log().err(e);
			
		}
		
		synchronized (this)
		{
			this.closed = true;
			
		}
		
	}
	
	public void initDisplay(IGameEnvironment ge) throws Throwable
	{
		assert Thread.currentThread() instanceof ThreadGameRender : "Cannot initiate display outside of rendering thread";
		
		IDisplayImpl imp = ge.createDisplay();
		
		if (imp == null)
		{
			return;
		}
		
		imp.createDisplay(this.settings);
		
		this.impl = imp;
		
		this.input.initiateInput(ge);
		
		this.rcon = new RenderContext(this, this.renderer);
		
		if (!this.rcon.initContext())
		{
			return;
		}
		
		imp.postInit();
		
		synchronized (this)
		{
			this.height = this.impl.getHeight();
			this.width = this.impl.getWidth();
			
			this.initiated = true;
			
		}
		
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public int getWidth()
	{
		return this.width;
	}
	
	public boolean isClosed()
	{
		return this.closed;
	}
	
	public IDisplayImpl getImpl()
	{
		return this.impl;
	}
	
	public boolean isInitiated()
	{
		return this.initiated;
	}
	
	public synchronized void updateSettings(DisplaySettings ds)
	{
		assert ds != null;
		
		this.settings = ds;
		this.refresh = true;
		
	}
	
	public synchronized void closeWindow()
	{
		this.close = true;
		
	}
	
	public void addInputListener(EnumInputType type, IInputListener lis)
	{
		this.input.addListener(type, lis);
		
	}
	
	public void createInputType(EnumInputType type)
	{
		assert type != null;
		
		this.input.createInputType(type);
		
	}
	
	public void sendInputEvents(double delta)
	{
		this.input.sendInputEvents(delta);
		
	}
	
}
