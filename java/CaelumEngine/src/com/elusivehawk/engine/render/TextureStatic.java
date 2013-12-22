
package com.elusivehawk.engine.render;

import java.io.File;
import java.io.IOException;
import com.elusivehawk.engine.render.opengl.GL;
import com.elusivehawk.engine.render.opengl.ITexture;

/**
 * 
 * NOTICE: This is simply a wrapper for {@link ITexture}.
 * 
 * @author Elusivehawk
 */
public class TextureStatic implements ITexture
{
	protected final int tex;
	
	@SuppressWarnings("unqualified-field-access")
	public TextureStatic(int texture)
	{
		tex = texture;
		
		GL.register(this);
		
	}
	
	public TextureStatic(String path)
	{
		this(path, EnumRenderMode.MODE_2D, EnumColorFormat.RGBA);
		
	}
	
	public TextureStatic(String path, EnumRenderMode mode, EnumColorFormat format)
	{
		this(new File(ClassLoader.getSystemResource(path).getFile()), mode, format);
		
	}
	
	public TextureStatic(File file, EnumRenderMode mode, EnumColorFormat format)
	{
		this(RenderHelper.processImage(file, mode, format));
		
	}
	
	public TextureStatic(ILegibleImage img, EnumRenderMode mode, EnumColorFormat format)
	{
		this(RenderHelper.processImage(img, mode, format));
		
	}
	
	@Override
	public void glDelete()
	{
		GL.glDeleteTextures(this.getTexture(true));
		
	}
	
	@Override
	public int getTexture(boolean next)
	{
		return this.tex;
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
}
