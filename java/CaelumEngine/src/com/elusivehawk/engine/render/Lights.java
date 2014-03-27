
package com.elusivehawk.engine.render;

import java.util.List;
import com.elusivehawk.engine.render.opengl.GLProgram;
import com.elusivehawk.engine.render.opengl.IGLManipulator;
import com.elusivehawk.engine.util.SimpleList;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class Lights implements IGLManipulator
{
	public static final int LIGHT_CAP = 1024;
	
	protected final List<Light> lights = SimpleList.newList(LIGHT_CAP, false);
	
	public Lights()
	{
		
	}
	
	public Light attachLight(Light l)
	{
		if (!this.lights.add(l))
		{
			return null;
		}
		
		return l;
	}
	
	@Override
	public boolean isModeValid(EnumRenderMode mode)
	{
		return mode.is3D();
	}
	
	@Override
	public void updateUniforms(RenderContext context){}
	
	@Override
	public void manipulateUniforms(RenderContext context, GLProgram p)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRender()
	{
		// TODO Auto-generated method stub
		
	}
	
}
