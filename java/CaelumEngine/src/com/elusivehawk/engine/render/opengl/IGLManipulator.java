
package com.elusivehawk.engine.render.opengl;

import com.elusivehawk.engine.render.RenderContext;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public interface IGLManipulator
{
	/**
	 * 
	 * Called once every frame; Do whatever you need to before rendering in this method.
	 * 
	 * @param context The current rendering context
	 */
	public void updateUniforms(RenderContext context);
	
	public void manipulateUniforms(RenderContext context, GLProgram p);
	
	/**
	 * 
	 * Called once every frame, after rendering.
	 */
	public void postRender();
	
}
