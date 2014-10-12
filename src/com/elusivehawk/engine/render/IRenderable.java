
package com.elusivehawk.engine.render;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
@FunctionalInterface
public interface IRenderable extends IPreRenderer, IPostRenderer
{
	@Override
	default void preRender(RenderContext rcon, double delta){}
	
	@Override
	default void postRender(RenderContext rcon){}
	
	/**
	 * 
	 * @param rcon
	 * 
	 * @throws RenderException
	 * 
	 * @see RenderHelper
	 */
	void render(RenderContext rcon) throws RenderException;
	
}
