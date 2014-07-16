
package com.elusivehawk.engine.render.two;

import com.elusivehawk.engine.render.ILogicalRender;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.engine.render.opengl.GLProgram;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class CanvasLayer implements ILogicalRender
{
	@Override
	public boolean updateBeforeUse(RenderContext context)
	{
		return false;
	}
	
	@Override
	public GLProgram getProgram()
	{
		return null;
	}
	
}
