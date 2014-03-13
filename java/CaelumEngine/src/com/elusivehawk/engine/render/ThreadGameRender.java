
package com.elusivehawk.engine.render;

import java.io.IOException;
import java.util.Collection;
import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;
import com.elusivehawk.engine.core.GameState;
import com.elusivehawk.engine.core.IGameStateListener;
import com.elusivehawk.engine.render.opengl.GLConst;
import com.elusivehawk.engine.util.SemiFinalStorage;
import com.elusivehawk.engine.util.ThreadTimed;

/**
 * 
 * The primary thread for rendering the game's window.
 * 
 * @author Elusivehawk
 */
public class ThreadGameRender extends ThreadTimed implements IGameStateListener
{
	protected IRenderHUB hub;
	protected final RenderContext context;
	protected final SemiFinalStorage<IDisplay> display = new SemiFinalStorage<IDisplay>(null);
	protected int fps;
	
	@SuppressWarnings("unqualified-field-access")
	public ThreadGameRender(IRenderHUB rhub)
	{
		hub = rhub;
		context = new RenderContext(rhub);
		
		CaelumEngine.game().addGameStateListener(this);
		
	}
	
	@Override
	public boolean initiate()
	{
		if (!CaelumEngine.renderEnvironment().initiate())
		{
			CaelumEngine.log().log(EnumLogType.ERROR, "Unable to load render environment.");
			
			return false;
		}
		
		this.context.initiate();
		
		this.hub.initiate(this.context);
		
		DisplaySettings settings = this.hub.getSettings();
		
		if (settings == null)
		{
			settings = new DisplaySettings();
			
		}
		
		this.fps = settings.targetFPS;
		
		if (!this.display.locked())
		{
			this.display.set(CaelumEngine.renderEnvironment().createDisplay("default", settings));
			
		}
		
		return true;
	}
	
	@Override
	public void update(double delta) throws Throwable
	{
		if (this.isPaused())
		{
			return;
		}
		
		if (this.hub == null)
		{
			return;
		}
		
		if (this.display.get().isCloseRequested())
		{
			System.exit("WINDOW-CLOSED".hashCode());
			
			return;
		}
		
		this.context.setRenderStage(EnumRenderStage.PRERENDER);
		
		this.hub.updateHUB(delta, this.context);
		
		if (this.hub.updateDisplay())
		{
			DisplaySettings settings = this.hub.getSettings();
			
			this.display.get().resize(settings.height, settings.width);
			this.display.get().setFullscreen(settings.fullscreen);
			this.display.get().setVSync(settings.vsync);
			this.display.get().setFPS(settings.targetFPS);
			
			this.fps = settings.targetFPS;
			
		}
		
		this.hub.getCamera().updateCamera(this.context);
		
		Collection<IRenderEngine> engines = this.hub.getRenderEngines();
		
		if (engines != null && !engines.isEmpty())
		{
			this.context.setRenderStage(EnumRenderStage.RENDER);
			
			this.context.getGL1().glClear(GLConst.GL_COLOR_BUFFER_BIT | GLConst.GL_DEPTH_BUFFER_BIT);
			
			int priorityCount = Math.max(this.hub.getHighestPriority(), 1);
			int renderersUsed = 0;
			int priority = 0;
			boolean flag = true;
			
			for (int p = 0; p < priorityCount && flag; p++)
			{
				for (IRenderEngine engine : engines)
				{
					if (renderersUsed == engines.size())
					{
						flag = false;
						break;
					}
					
					priority = Math.min(engine.getPriority(this.hub), priorityCount - 1);
					
					if (priority != p)
					{
						continue;
					}
					
					engine.render(this.context);
					renderersUsed++;
					
					int tex = 0, texUnits = this.context.getGL1().glGetInteger(GLConst.GL_MAX_TEXTURE_UNITS);
					
					for (int c = 0; c < texUnits; c++)
					{
						tex = this.context.getGL1().glGetInteger(GLConst.GL_TEXTURE0 + c);
						
						if (tex != 0)
						{
							this.context.getGL1().glBindTexture(GLConst.GL_TEXTURE0 + c, 0);
							
						}
						
					}
					
					try
					{
						RenderHelper.checkForGLError(this.context);
						
					}
					catch (Exception e)
					{
						CaelumEngine.log().log(EnumLogType.ERROR, null, e);
						
					}
					
				}
				
			}
			
		}
		
		this.context.setRenderStage(EnumRenderStage.POSTEFFECTS);
		
		this.display.get().updateDisplay();
		
	}
	
	@Override
	public void onThreadStopped()
	{
		this.context.cleanup();
		
		try
		{
			this.display.get().close();
			
		}
		catch (IOException e){}
		
	}
	
	@Override
	public int getTargetUpdateCount()
	{
		return this.fps;
	}
	
	@Override
	public double getMaxDelta()
	{
		return 0.5;
	}
	
	@Override
	public synchronized void onGameStateSwitch(GameState gs)
	{
		this.hub = gs.getRenderHUB();
		
	}
	
}
