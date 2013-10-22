
package com.elusivehawk.engine.render;

import com.elusivehawk.engine.render.gl.GLProgram;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public interface ILogicalRender
{
	public boolean updateBeforeUse(IRenderHUB hub);
	
	public GLProgram getProgram();
	
}
