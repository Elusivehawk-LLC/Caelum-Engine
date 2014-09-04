
package com.elusivehawk.engine.lwjgl;

import java.io.File;
import java.util.List;
import com.elusivehawk.engine.IGameEnvironment;
import com.elusivehawk.engine.input.Input;
import com.elusivehawk.engine.render.DisplaySettings;
import com.elusivehawk.engine.render.IDisplay;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.util.CompInfo;
import com.elusivehawk.util.EnumOS;
import com.elusivehawk.util.FileHelper;
import com.elusivehawk.util.concurrent.IThreadStoppable;
import com.elusivehawk.util.json.EnumJsonType;
import com.elusivehawk.util.json.JsonData;
import com.elusivehawk.util.json.JsonObject;
import com.google.common.collect.Lists;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class LWJGLEnvironment implements IGameEnvironment
{
	protected final OpenGL3 GL_3 = new OpenGL3();
	protected final Object GL_4 = null;
	
	@Override
	public boolean isCompatible(EnumOS os)
	{
		return os != EnumOS.ANDROID;
	}
	
	@Override
	public void initiate(JsonObject json, String... args)
	{
		System.setProperty("org.lwjgl.opengl.Display.noinput", "true");
		
		String lib = null;
		
		if (json != null)
		{
			JsonData val = json.getValue("debugNativeLocation");
			
			if (val != null)
			{
				if (val.type == EnumJsonType.STRING)
				{
					lib = val.value;
					
				}
				
			}
			
		}
		
		if (lib == null)
		{
			lib = determineLWJGLPath().getAbsolutePath();
			
		}
		
		System.setProperty("org.lwjgl.librarypath", FileHelper.fixPath(lib));
		
	}
	
	@Override
	public String getName()
	{
		return "CaelumLWJGL";
	}
	
	@Override
	public IDisplay createDisplay(DisplaySettings settings)
	{
		LWJGLDisplay ret = new LWJGLDisplay();
		
		ret.updateSettings(settings);
		
		return ret;
	}
	
	@Override
	public List<Input> loadInputs()
	{
		List<Input> ret = Lists.newArrayList();
		
		ret.add(new LWJGLMouse());
		ret.add(new LWJGLKeyboard());
		
		return ret;
	}
	
	@Override
	public Object getGL(int version)
	{
		switch (version)
		{
			case 1:
			case 2:
			case 3: return this.GL_3;
			case 4: return this.GL_4;
			default: return null;
		}
		
	}
	
	@Override
	public IThreadStoppable createRenderThread(RenderContext rcon)
	{
		return null;
	}
	
	public static File determineLWJGLPath()
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
			return FileHelper.getResource("res").getParentFile();
		}
		
		return FileHelper.createFile(".", path);
	}
	
}
