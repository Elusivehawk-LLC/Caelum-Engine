
package com.elusivehawk.engine.render;

import com.elusivehawk.util.math.MathHelper;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class Material implements IRenderable
{
	protected ITexture tex = null;
	protected IRenderableTexture fboTex = null;
	protected float shininess = 0f;
	protected Color filter = new Color();
	
	private boolean locked = false;
	
	public Material(){}
	
	@SuppressWarnings("unqualified-field-access")
	public Material(Material m)
	{
		tex = m.tex;
		shininess = m.shininess;
		filter = m.filter;
		
	}
	
	@Override
	public void render(RenderContext rcon) throws RenderException
	{
		if (this.fboTex != null)
		{
			this.fboTex.render(rcon);
			
		}
		
	}
	
	@Override
	public void preRender(RenderContext rcon, double delta)
	{
		if (this.tex != null)
		{
			this.tex.preRender(rcon, delta);
			
		}
		
		if (this.fboTex != null)
		{
			this.fboTex.preRender(rcon, delta);
			
		}
		
	}
	
	@Override
	public void postRender(RenderContext rcon)
	{
		if (this.fboTex != null)
		{
			this.fboTex.postRender(rcon);
			
		}
		
	}
	
	@Override
	public Material clone()
	{
		return new Material(this);
	}
	
	public Material fboTex(IRenderableTexture texture)
	{
		assert texture != null;
		
		if (!this.locked)
		{
			this.fboTex = texture;
			
		}
		
		return this;
	}
	
	public Material filter(Color col)
	{
		assert col != null;
		
		if (!this.locked)
		{
			this.filter = col;
			
		}
		
		return this;
	}
	
	public Material shine(float shine)
	{
		if (!this.locked)
		{
			this.shininess = MathHelper.clamp(shine, 0, 1f);
			
		}
		
		return this;
	}
	
	public Material tex(ITexture texture)
	{
		assert texture != null;
		
		if (!this.locked)
		{
			this.tex = texture;
			
		}
		
		return this;
	}
	
	public Material lock()
	{
		this.locked = true;
		
		return this;
	}
	
	public IRenderableTexture fboTex()
	{
		return this.fboTex;
	}
	
	public Color filter()
	{
		return this.filter;
	}
	
	public float shine()
	{
		return this.shininess;
	}
	
	public ITexture tex()
	{
		return this.tex;
	}
	
	public boolean isStatic()
	{
		return this.fboTex != null || (this.tex != null && !this.tex.isAnimated());
	}
	
}
