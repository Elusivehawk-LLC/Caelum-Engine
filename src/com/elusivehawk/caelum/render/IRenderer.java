
package com.elusivehawk.caelum.render;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public interface IRenderer extends IDeletable, IPostRenderer, IPreRenderer
{
	void render(RenderContext rcon) throws RenderException;
	
	default void render(RenderContext rcon, Camera cam) throws RenderException
	{
		Camera cam_tmp = rcon.getCamera();
		
		rcon.setCamera(cam);
		
		try
		{
			this.render(rcon);
			
		}
		catch (RenderException e)
		{
			throw e;
		}
		finally
		{
			rcon.setCamera(cam_tmp);
			
		}
		
	}
	
}
