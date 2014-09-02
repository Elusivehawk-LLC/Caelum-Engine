
package com.elusivehawk.engine.render;

import java.io.File;
import java.util.Collection;
import java.util.List;
import com.elusivehawk.engine.CaelumEngine;
import com.elusivehawk.engine.GameState;
import com.elusivehawk.engine.IContext;
import com.elusivehawk.engine.IGameEnvironment;
import com.elusivehawk.engine.IGameStateListener;
import com.elusivehawk.engine.render.old.IRenderEngine;
import com.elusivehawk.engine.render.old.IRenderHUB;
import com.elusivehawk.engine.render.old.RenderTask;
import com.elusivehawk.engine.render.opengl.GLConst;
import com.elusivehawk.engine.render.opengl.GLEnumShader;
import com.elusivehawk.engine.render.opengl.GLEnumTexture;
import com.elusivehawk.engine.render.opengl.IGL1;
import com.elusivehawk.engine.render.opengl.IGL2;
import com.elusivehawk.engine.render.opengl.IGL3;
import com.elusivehawk.engine.render.opengl.IGLDeletable;
import com.elusivehawk.util.EnumLogType;
import com.elusivehawk.util.IPausable;
import com.elusivehawk.util.IUpdatable;
import com.elusivehawk.util.Logger;
import com.google.common.collect.Lists;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public final class RenderContext implements IUpdatable, IPausable, IGameStateListener, IContext
{
	private final IGameEnvironment env;
	private final IDisplay display;
	
	@Deprecated
	private IRenderHUB hub = null;
	
	private IGL1 gl1;
	private IGL2 gl2;
	private IGL3 gl3;
	
	private int fps;
	private boolean paused = false;
	
	private int notex, maxTexCount;
	
	private Shaders shaders = new Shaders();
	
	private final List<IGLDeletable> cleanables = Lists.newArrayList();
	@Deprecated
	private final List<RenderTask> rtasks = Lists.newArrayList();
	
	private DisplaySettings settings = new DisplaySettings();
	private boolean //Hey, I sorta like this...
			initiated = false,
			refreshScreen = false,
			flipScreen = false;
	
	@SuppressWarnings("unqualified-field-access")
	public RenderContext(IGameEnvironment gameEnv, IDisplay d)
	{
		assert gameEnv != null;
		assert d != null;
		
		env = gameEnv;
		display = d;
		
	}
	
	@Override
	public boolean initContext()
	{
		if (this.initiated)
		{
			return false;
		}
		
		try
		{
			this.display.createDisplay();
			
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
		
		this.gl1 = (IGL1)this.env.getGL(1);
		this.gl2 = (IGL2)this.env.getGL(2);
		this.gl3 = (IGL3)this.env.getGL(3);
		
		this.gl1.glViewport(0, 0, this.display.getWidth(), this.display.getHeight());
		
		for (GLEnumShader sh : GLEnumShader.values())
		{
			String loc = String.format("res/%s.glsl", sh.name().toLowerCase());
			
			if (new File(loc).exists())
			{
				this.shaders.addShader(new Shader(loc, sh));
				
			}
			
		}
		
		/*PixelGrid ntf = new PixelGrid(32, 32, ColorFormat.RGBA);
		
		for (int x = 0; x < ntf.getWidth(); x++)
		{
			for (int y = 0; y < ntf.getHeight(); y++)
			{
				ntf.setPixel(x, y, (x <= 16 && y >= 16) || (x >= 16 && y <= 16) ? 0xFF00FF : 0xFFFFFF);
				
			}
			
		}
		
		try
		{
			this.notex = RenderHelper.processImage(ntf);
			
		}
		catch (GLException e)
		{
			CaelumEngine.log().err(e);
			
		}*/
		
		this.maxTexCount = this.gl1.glGetInteger(GLConst.GL_MAX_TEXTURE_UNITS);
		
		this.fps = this.settings.targetFPS;
		
		if (this.hub != null)
		{
			Logger.log().log(EnumLogType.WARN, "Rendering using render HUB system! Override Game.render() instead!");
			
			this.hub.initiate(this.display);
			
		}
		
		this.initiated = true;
		
		return true;
	}
	
	@Override
	public void cleanup()
	{
		//this.gl1.glDeleteTextures(this.notex);
		
		for (IGLDeletable gl : this.cleanables)
		{
			gl.delete(this);
			
		}
		
		this.cleanables.clear();
		
	}
	
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
	
	@Override
	public void update(double delta) throws RenderException
	{
		boolean useHub = (this.hub != null);
		
		if (useHub)
		{
			this.hub.updateHUB(delta);
			
		}
		
		if ((useHub && this.hub.updateDisplay()) || this.refreshScreen)
		{
			if (useHub)
			{
				this.settings = this.hub.getSettings();
				
			}
			
			this.display.updateSettings(this.settings);
			this.refreshScreen = false;
			
		}
		
		this.gl1.glClear(0b0100010100000000);
		this.renderGame(delta);
		this.display.updateDisplay();
		
	}
	
	private void renderGame(double delta)
	{
		this.preRender();
		
		this.renderGameDepre(delta);
		
		CaelumEngine.game().render(this, delta);
		
		this.postRender();
		
	}
	
	public void setRenderHUB(IRenderHUB rhub)
	{
		if (this.hub != null)
		{
			return;
		}
		
		this.hub = rhub;
		
		CaelumEngine.game().addGameStateListener(this);
		
	}
	
	private void preRender(){}
	
	private void postRender()
	{
		if (!this.rtasks.isEmpty())
		{
			RenderTask t = this.rtasks.get(0);
			boolean rem = false;
			
			try
			{
				rem = t.completeTask();
				
			}
			catch (Throwable e)
			{
				Logger.log().err("Error caught whilst finishing render task:", e);
				
			}
			
			if (rem)
			{
				this.rtasks.remove(0);
				
			}
			
		}
		
	}
	
	@Deprecated
	private void renderGameDepre(double delta)
	{
		if (this.hub != null)
		{
			Collection<IRenderEngine> engines = this.hub.getRenderEngines();
			
			if (engines != null && !engines.isEmpty())
			{
				for (IRenderEngine engine : engines)
				{
					engine.render(this, this.hub, delta);
					
					for (int c = 0; c < this.getMaxTextureCount(); c++)
					{
						this.gl1.glActiveTexture(GLConst.GL_TEXTURE0 + c);
						this.gl1.glBindTexture(GLEnumTexture.GL_TEXTURE_2D, 0);
						
					}
					
					RenderHelper.checkForGLError(this.gl1);
					
				}
				
			}
			
		}
		
	}
	
	@SuppressWarnings("unused")
	public void onDisplayResized(IDisplay d)
	{
		//TODO
		
	}
	
	@SuppressWarnings("unused")
	public void onDisplayClosed(IDisplay d)
	{
		this.cleanup();
		
	}
	
	public synchronized void onScreenFlipped(boolean flip)
	{
		this.flipScreen = flip;
		
	}
	
	public IDisplay getDisplay()
	{
		return this.display;
	}
	
	@Deprecated
	public IRenderHUB getHUB()
	{
		return this.hub;
	}
	
	public IGL1 getGL1()
	{
		return this.gl1;
	}
	
	public IGL2 getGL2()
	{
		return this.gl2;
	}
	
	public IGL3 getGL3()
	{
		return this.gl3;
	}
	
	public int getFPS()
	{
		return this.fps;
	}
	
	public Shaders getDefaultShaders()
	{
		return this.shaders;
	}
	
	public int getDefaultTexture()
	{
		return this.notex;
	}
	
	public int getMaxTextureCount()
	{
		return this.maxTexCount;
	}
	
	public boolean isScreenFlipped()
	{
		return this.flipScreen;
	}
	
	public void setSettings(DisplaySettings ds)
	{
		assert ds != null;
		
		this.settings = ds;
		this.refreshScreen = true;
		
	}
	
	public void registerCleanable(IGLDeletable gl)
	{
		if (this.cleanables.contains(gl))
		{
			return;
		}
		
		this.cleanables.add(gl);
		
	}
	
	public synchronized void setScreenFlipped(boolean b)
	{
		this.flipScreen = b;
		
	}
	
	@Deprecated
	public synchronized void scheduleRTask(RenderTask rt)
	{
		this.rtasks.add(rt);
		
	}
	
}
