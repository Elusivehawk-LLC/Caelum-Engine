
package com.elusivehawk.caelum.lwjgl;

import org.lwjgl.system.glfw.GLFW;
import com.elusivehawk.caelum.CaelumEngine;
import com.elusivehawk.caelum.CaelumException;
import com.elusivehawk.caelum.IDisplayImpl;
import com.elusivehawk.caelum.IGameEnvironment;
import com.elusivehawk.caelum.input.EnumInputType;
import com.elusivehawk.caelum.input.Input;
import com.elusivehawk.caelum.input.InputManager;
import com.elusivehawk.caelum.render.gl.GL1;
import com.elusivehawk.caelum.render.gl.GL2;
import com.elusivehawk.caelum.render.gl.GL3;
import com.elusivehawk.util.CompInfo;
import com.elusivehawk.util.EnumOS;
import com.elusivehawk.util.json.JsonObject;
import com.google.common.collect.ImmutableList;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class LWJGLEnvironment implements IGameEnvironment
{
	public static final String[]
					WIN_32 = {"\\windows\\x64\\lwjgl.dll", "\\windows\\x64\\OpenAL32.dll"},
					WIN_64 = {"\\windows\\x86\\lwjgl.dll", "\\windows\\x86\\OpenAL64.dll"},
					MAC_32 = {"\\macosx\\x64\\liblwjgl.dylib", "\\macosx\\x64\\libopenal.dylib"},
					MAC_64 = {},
					LINUX_32 = {"\\linux\\x64\\liblwjgl.so", "/libnux/x64/libopenal.so"},
					LINUX_64 = {"\\linux\\x86\\liblwjgl.so", "/libnux/x86/libopenal.so"};
	
	@Override
	public boolean isCompatible(EnumOS os)
	{
		return os != EnumOS.ANDROID;
	}
	
	@Override
	public void preInit()
	{
		GL1.setImpl(new OpenGL1());
		GL2.setImpl(new OpenGL2());
		GL3.setImpl(new OpenGL3());
		
		System.setProperty("org.lwjgl.opengl.Display.noinput", "true");
		
	}
	
	@Override
	public void initiate(JsonObject json, String... args)
	{
		System.setProperty("org.lwjgl.librarypath", CaelumEngine.getNativeLocation().getAbsolutePath());
		
		if (GLFW.glfwInit() != 1)
		{
			throw new CaelumException("Unable to initiate GLFW");
		}
		
	}
	
	@Override
	public void destroy()
	{
		GLFW.glfwTerminate();
		
	}
	
	@Override
	public String getName()
	{
		return "CaelumLWJGL";
	}
	
	@Override
	public ImmutableList<String> getNatives()
	{
		String[] n = null;
		
		switch (CompInfo.OS)
		{
			case LINUX: n = CompInfo.IS_64_BIT ? LINUX_64 : LINUX_32;
			case MAC: n = CompInfo.IS_64_BIT ? MAC_64 : MAC_32;
			case WINDOWS: n = CompInfo.IS_64_BIT ? WIN_64 : WIN_32;
			
		}
		
		return n == null ? null : ImmutableList.copyOf(n);
	}
	
	@Override
	public IDisplayImpl createDisplay()
	{
		return new LWJGLDisplayImpl();
	}
	
	@Override
	public Input createInput(InputManager inmgr, EnumInputType type)
	{
		switch (type)
		{
			case KEYBOARD: return new LWJGLKeyboard(inmgr);
			case MOUSE: return new LWJGLMouse(inmgr);
			default: throw new CaelumException("Unsupported input type: %s", type);
		}
		
	}
	
	/*public static File determineLWJGLPath()
	{
		String path = null;
		
		if ((CompInfo.OS == EnumOS.LINUX && new File("/usr/lib/jni/liblwjgl.so").exists()))
		{
			path = "usr/lib/jni";//TODO: this only works on Debian... but we'll try it for now.
			
		}
		else if (FileHelper.createFile("lib/lwjgl/native").exists())
		{
			path = String.format("lib/lwjgl/native/%s", CompInfo.OS.toString());
			
		}
		
		if (path == null)
		{
			return FileHelper.getRootResDir();
		}
		
		return FileHelper.createFile(".", path);
	}*/
	
}
