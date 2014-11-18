
package com.elusivehawk.caelum.render.tex;

import com.elusivehawk.caelum.render.IRenderable;
import com.elusivehawk.caelum.render.RenderContext;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class TextureRenderer extends RenderableTexture
{
	private final IRenderable renderer;
	
	public TextureRenderer(IRenderable r)
	{
		this(r, true);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public TextureRenderer(IRenderable r, boolean depth)
	{
		super(depth);
		
		assert r != null;
		
		renderer = r;
		
	}
	
	@Override
	public void preRender(RenderContext rcon, double delta)
	{
		this.renderer.preRender(rcon, delta);
		
	}
	
	@Override
	public void renderTexture(RenderContext rcon)
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
