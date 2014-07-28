
package com.elusivehawk.engine.render;

import java.util.Collection;
import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;
import com.elusivehawk.engine.core.GameState;
import com.elusivehawk.engine.core.IContext;
import com.elusivehawk.engine.core.IGameStateListener;
import com.elusivehawk.engine.core.IThreadContext;
import com.elusivehawk.engine.render.old.EnumRenderStage;
import com.elusivehawk.engine.render.old.IRenderEngine;
import com.elusivehawk.engine.render.old.IRenderHUB;
import com.elusivehawk.engine.render.opengl.GLConst;
import com.elusivehawk.engine.render.opengl.IGL1;
import com.elusivehawk.util.IPausable;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public final class RenderSystem implements IPausable, IGameStateListener, IThreadContext
{
	private final IRenderEnvironment renv;
	private final RenderContext context;
	
	private IDisplay display = null;
	private IRenderHUB hub = null;
	private int fps;
	private boolean paused = false;
	
	@SuppressWarnings("unqualified-field-access")
	public RenderSystem(IRenderEnvironment renderEnv)
	{
		renv = renderEnv;
		context = new RenderContext(this);
		
	}
	
	@Deprecated
	@SuppressWarnings("unqualified-field-access")
	public RenderSystem(IRenderEnvironment renderEnv, IRenderHUB rhub)
	{
		this(renderEnv);
		
		hub = rhub;
		CaelumEngine.game().addGameStateListener(this);
		
	}
	
	@Override
	public IContext getContext()
	{
		return this.context;
	}
	
	@Deprecated
	@Override
	public void onGameStateSwitch(GameState gs)
	{
		this.hub = gs.getRenderHUB();
		
	}
	
	@Override
	public boolean isPaused()
	{
		return this.paused;
	}
	
	@Override
	public void setPaused(boolean p)
	{
		this.paused = p;
		
	}
	
	public boolean initiate()
	{
		if (!this.renv.initiate())
		{
			CaelumEngine.log().log(EnumLogType.ERROR, "Unable to load render environment.");
			
			return false;
		}
		
		this.context.initContext();
		
		DisplaySettings settings = this.context.getSettings();
		
		this.fps = settings.targetFPS;
		IDisplay d = this.renv.createDisplay("default", settings);
		
		if (d == null)
		{
			return false;
			
		}
		
		if (this.hub != null)
		{
			CaelumEngine.log().log(EnumLogType.WARN, "Rendering using render HUB system! Use Game.render()!");
			
			this.hub.initiate(d);
			
		}
		
		this.display = d;
		
		return true;
	}
	
	public boolean drawScreen(double delta) throws RenderException
	{
		if (this.isPaused())
		{
			return false;
		}
		
		boolean useHub = (this.hub != null);
		
		if (useHub)
		{
			this.hub.updateHUB(delta);
			
		}
		
		this.context.setRenderStage(EnumRenderStage.PRERENDER);
		
		if (useHub && this.hub.updateDisplay())
		{
			DisplaySettings settings = this.hub.getSettings();
			
			this.display.resize(settings.height, settings.width);
			this.display.setFullscreen(settings.fullscreen);
			this.display.setVSync(settings.vsync);
			this.display.setFPS(this.fps = settings.targetFPS);
			
		}
		
		this.context.setRenderStage(EnumRenderStage.RENDER);
		
		CaelumEngine.game().render(this.context, delta);
		
		if (useHub)
		{
			Collection<IRenderEngine> engines = this.hub.getRenderEngines();
			IGL1 gl1 = this.context.getGL1();
			
			if (engines != null && !engines.isEmpty())
			{
				int priority = 0, priorityCount = Math.max(this.hub.getHighestPriority(), 1);
				int renderersUsed = 0;
				boolean flag = true;
				
				gl1.glClear(GLConst.GL_COLOR_BUFFER_BIT | GLConst.GL_DEPTH_BUFFER_BIT);
				
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
						
						if (priority == -1)
						{
							continue;
						}
						
						if (priority != p)
						{
							continue;
						}
						
						try
						{
							engine.render(this.context, this.hub);
							
						}
						catch (RenderException e)
						{
							CaelumEngine.log().log(EnumLogType.ERROR, null, e);
							
						}
						
						renderersUsed++;
						
						int tex = 0, texUnits = gl1.glGetInteger(GLConst.GL_MAX_TEXTURE_UNITS);
						
						for (int c = 0; c < texUnits; c++)
						{
							tex = gl1.glGetInteger(GLConst.GL_TEXTURE0 + c);
							
							if (tex != 0)
							{
								gl1.glBindTexture(GLConst.GL_TEXTURE0 + c, 0);
								
							}
							
						}
						
						RenderHelper.checkForGLError(gl1);
						
					}
					
				}
				
			}
			
		}
		
		this.context.setRenderStage(EnumRenderStage.POSTEFFECTS);
		
		return true;
	}
	
	@SuppressWarnings("unused")
	public void onDisplayResized(IDisplay d)
	{
		//TODO
		
	}
		
	@SuppressWarnings("unused")
	public void onDisplayClosed(IDisplay d)
	{
		this.context.cleanup();
		
	}
	
	public synchronized void onScreenFlipped(boolean flip)
	{
		this.context.setScreenFlipped(flip);
		
	}
	
	public IRenderEnvironment getRenderEnv()
	{
		return this.renv;
	}
	
	@Deprecated
	public IRenderHUB getHUB()
	{
		return this.hub;
	}
	
	public IDisplay getDisplay()
	{
		return this.display;
	}
	
	public int getFPS()
	{
		return this.fps;
	}
	
}
