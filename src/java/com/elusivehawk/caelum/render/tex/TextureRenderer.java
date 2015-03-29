
package com.elusivehawk.caelum.render.tex;

import com.elusivehawk.caelum.render.Renderable;
import com.elusivehawk.caelum.render.RenderContext;
import com.elusivehawk.caelum.render.RenderException;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class TextureRenderer extends RenderableTexture
{
	private final Renderable renderer;
	
	public TextureRenderer(Renderable r)
	{
		this(r, true);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public TextureRenderer(Renderable r, boolean depth)
	{
		super(depth);
		
		assert r != null;
		
		renderer = r;
		
	}
	
	@Override
	public void preRender(RenderContext rcon)
	{
		this.renderer.preRender(rcon);
		
	}
	
	@Override
	public void renderTexture(RenderContext rcon) throws RenderException
	{
		this.renderer.render(rcon);
		
	}
	
	@Override
	public void postRender(RenderContext rcon)
	{
		super.postRender(rcon);
		
		this.renderer.postRender(rcon);
		
	}
	
}
