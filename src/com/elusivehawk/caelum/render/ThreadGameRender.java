
package com.elusivehawk.caelum.render;

import org.lwjgl.glfw.GLFW;
import com.elusivehawk.util.Internal;
import com.elusivehawk.util.Logger;
import com.elusivehawk.util.concurrent.ThreadTimed;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
@Internal
public final class ThreadGameRender extends ThreadTimed
{
	private final DisplayManager displays;
	private final int fps;
	
	@SuppressWarnings("unqualified-field-access")
	public ThreadGameRender(DisplayManager dmgr, int i)
	{
		super("Renderer");
		
		assert dmgr != null;
		assert i > 0;
		
		displays = dmgr;
		fps = i;
		
	}
	
	@Override
	public boolean initiate()
	{
		super.initiate();
		
		if (GLFW.glfwInit() == 0)
		{
			return false;
		}
		
		GLFW.glfwDefaultWindowHints();
		
		Logger.verbose("OpenGL version: %s", RenderConst.GL_VERSION);
		Logger.verbose("OpenGL vendor: %s", RenderConst.GL_VENDOR);
		Logger.verbose("OpenGL renderer: %s", RenderConst.GL_RENDERER);
		Logger.verbose("OpenGL texture count: %s", RenderConst.GL_MAX_TEX_COUNT);
		
		return true;
	}
	
	@Override
	public void update(double delta) throws Throwable
	{
		if (this.isPaused())
		{
			return;
		}
		
		this.displays.update(delta);
		
	}
	
	@Override
	public boolean doPostUpdate()
	{
		return true;
	}
	
	@Override
	public void postUpdate(double delta) throws Throwable
	{
		this.displays.updateInput(delta);
		
	}
	
	@Override
	public int getTargetUpdateCount()
	{
		return this.fps;
	}
	
	@Override
	public void onThreadStopped(boolean failure)
	{
		this.displays.close();
		
		GLFW.glfwTerminate();
		
	}
	
	@Override
	public void handleException(Throwable e)
	{
		Logger.err("Error caught during render phase", e);
		
	}
	
}
