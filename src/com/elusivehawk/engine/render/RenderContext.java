
package com.elusivehawk.engine.render;

import java.util.List;
import com.elusivehawk.engine.CaelumEngine;
import com.elusivehawk.engine.IContext;
import com.elusivehawk.engine.IGameEnvironment;
import com.elusivehawk.engine.render.old.RenderTask;
import com.elusivehawk.engine.render.opengl.GLConst;
import com.elusivehawk.engine.render.opengl.GLEnumShader;
import com.elusivehawk.engine.render.opengl.GLException;
import com.elusivehawk.engine.render.opengl.GLProgram;
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
public final class RenderContext implements IUpdatable, IPausable, IContext
{
	private final IGameEnvironment env;
	private final IDisplay display;
	
	private IGL1 gl1;
	private IGL2 gl2;
	private IGL3 gl3;
	
	private int notex, maxTexCount, renders = 0;
	
	private final Shaders shaders = new Shaders();
	private final GLProgram p = new GLProgram(this.shaders);
	
	private final List<IGLDeletable> cleanables = Lists.newArrayList();
	private final List<IPreRenderer> preRenderers = Lists.newArrayList();
	private final List<IPostRenderer> postRenderers = Lists.newArrayList();
	@Deprecated
	private final List<RenderTask> rtasks = Lists.newArrayList();
	
	private DisplaySettings settings = new DisplaySettings();
	private boolean
			initiated = false,
			paused = false,
			refreshScreen = false,
			flipScreen = false,
			updateCameraUniforms = true;
	
	private ICamera camera = null;
	
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
			Logger.log().err(e);
			
		}
		
		if (!this.display.isCreated())
		{
			return false;
		}
		
		this.gl1 = (IGL1)this.env.getGL(1);
		this.gl2 = (IGL2)this.env.getGL(2);
		this.gl3 = (IGL3)this.env.getGL(3);
		
		this.gl1.glViewport(0, 0, this.display.getWidth(), this.display.getHeight());
		
		Logger.log().log(EnumLogType.VERBOSE, "GL version: %s", this.gl1.glGetString(GLConst.GL_VERSION));
		Logger.log().log(EnumLogType.VERBOSE, "GL vendor: %s", this.gl1.glGetString(GLConst.GL_VENDOR));
		Logger.log().log(EnumLogType.VERBOSE, "GL renderer %s", this.gl1.glGetString(GLConst.GL_RENDERER));
		
		for (GLEnumShader sh : GLEnumShader.values())
		{
			this.shaders.addShader(new Shader(String.format("/res/shaders/%s.glsl", sh.name().toLowerCase()), sh));
			
		}
		
		PixelGrid ntf = new PixelGrid(32, 32, ColorFormat.RGBA);
		
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
			Logger.log().err(e);
			
		}
		
		this.maxTexCount = this.gl1.glGetInteger(GLConst.GL_MAX_TEXTURE_UNITS);
		
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
	public void update(double delta, Object... extra)
	{
		try
		{
			if (this.refreshScreen)
			{
				this.display.updateSettings(this.settings);
				this.gl1.glViewport(0, 0, this.display.getWidth(), this.display.getHeight());
				
				this.refreshScreen = false;
				
			}
			
			this.gl1.glClear(0b0100010100000000);
			
			this.preRender(delta);
			
			this.renderGame(delta);
			
			this.postRender();
			
		}
		catch (Throwable e)
		{
			Logger.log().err(e);
			
		}
		
	}
	
	public void renderGame(ICamera cam, double delta)
	{
		assert cam != null;
		
		ICamera cam_tmp = this.camera;
		
		this.camera = cam;
		this.updateCameraUniforms = true;
		
		this.renderGame(delta);
		
		this.camera = cam_tmp;
		this.updateCameraUniforms = false;
		
	}
	
	private void renderGame(double delta)
	{
		if (this.renders == RenderConst.RECURSIVE_LIMIT)
		{
			return;
		}
		
		this.renders++;
		
		CaelumEngine.game().render(this, delta);
		
		this.renders--;
		
	}
	
	private void preRender(double delta)
	{
		this.preRenderers.forEach(((preR) -> {preR.preRender(this, delta);}));
		
	}
	
	private void postRender()
	{
		this.postRenderers.forEach(((postR) -> {postR.postRender(this);}));
		
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
		return this.settings.targetFPS;
	}
	
	public GLProgram getDefaultProgram()
	{
		return this.p;
	}
	
	public int getDefaultTexture()
	{
		return this.notex;
	}
	
	public int getMaxTextureCount()
	{
		return this.maxTexCount;
	}
	
	public int getRenderCount()
	{
		return this.renders;
	}
	
	public boolean isScreenFlipped()
	{
		return this.flipScreen;
	}
	
	public ICamera getCamera()
	{
		return this.camera;
	}
	
	public boolean updateCamera()
	{
		return this.camera == null ? false : (this.updateCameraUniforms || this.camera.isDirty());
	}
	
	public void setSettings(DisplaySettings ds)
	{
		assert ds != null;
		
		this.settings = ds;
		this.refreshScreen = true;
		
	}
	
	public void registerCleanable(IGLDeletable gl)
	{
		if (!this.cleanables.contains(gl))
		{
			this.cleanables.add(gl);
			
		}
		
	}
	
	public void registerRenderer(IRenderable r)
	{
		this.registerPreRenderer(r);
		this.registerPostRenderer(r);
		
	}
	
	public void registerPreRenderer(IPreRenderer preR)
	{
		if (!this.preRenderers.contains(preR))
		{
			this.preRenderers.add(preR);
			
		}
		
	}
	
	public void registerPostRenderer(IPostRenderer postR)
	{
		if (!this.postRenderers.contains(postR))
		{
			this.postRenderers.add(postR);
			
		}
		
	}
	
	public void setCamera(ICamera cam)
	{
		assert cam != null;
		
		this.camera = cam;
		
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
