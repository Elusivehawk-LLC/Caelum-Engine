
package com.elusivehawk.engine.render;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.elusivehawk.engine.core.Buffer;
import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class TextureAnimated implements ITexture
{
	private final Buffer<Integer> tex;
	
	@SuppressWarnings("unqualified-field-access")
	public TextureAnimated(File gif, EnumRenderMode mode, EnumColorFormat format)
	{
		tex = RenderHelper.processGifFile(gif, mode, format);
		
		GL.register(this);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public TextureAnimated(File file, EnumRenderMode mode, EnumColorFormat format, int y)
	{
		if (file.getName().endsWith(".gif"))
		{
			throw new RuntimeException("Wrong constructor, Einstein.");
		}
		
		BufferedImage img = null;
		
		try
		{
			img = ImageIO.read(file);
			
		}
		catch (Exception e)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.ERROR, null, e);
			
		}
		
		if (img == null)
		{
			tex = new Buffer<Integer>();
			
		}
		else
		{
			if (img.getHeight() % y != 0)
			{
				throw new RuntimeException("Image is not fit for animating!");
			}
			
			tex = new Buffer<Integer>();
			
			for (int c = 0; c < img.getHeight(); c += y)
			{
				BufferedImage sub = img.getSubimage(0, c, img.getWidth(), y);
				
				tex.put(RenderHelper.processImage(new LegibleBufferedImage(sub), mode, format));
				
			}
			
		}
		
		GL.register(this);
		
	}
	
	@Override
	public void glDelete()
	{
		this.tex.rewind();
		
		for (int i : this.tex)
		{
			GL.glDeleteTextures(i);
			
		}
		
	}
	
	@Override
	public int getTexture(boolean next)
	{
		if (this.tex.remaining() == 0)
		{
			this.tex.rewind();
			
		}
		
		return this.tex.next(next);
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
}
