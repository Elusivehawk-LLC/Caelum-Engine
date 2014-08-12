
package com.elusivehawk.engine.render;

import java.awt.image.BufferedImage;

/**
 * 
 * Wrapper class for ILegibleImage and {@link BufferedImage}.
 * 
 * @author Elusivehawk
 */
public class LegibleBufferedImage implements ILegibleImage
{
	protected final BufferedImage img;
	protected final ColorFormat format;
	
	@SuppressWarnings("unqualified-field-access")
	public LegibleBufferedImage(BufferedImage image)
	{
		img = image;
		
		ColorFormat f;
		
		switch (image.getType())
		{
			case BufferedImage.TYPE_3BYTE_BGR: f = ColorFormat.BGRA;
			case BufferedImage.TYPE_4BYTE_ABGR: f = ColorFormat.ABGR;
			case BufferedImage.TYPE_4BYTE_ABGR_PRE: f = ColorFormat.ABGR;
			case BufferedImage.TYPE_INT_ARGB: f = ColorFormat.ARGB;
			case BufferedImage.TYPE_INT_ARGB_PRE: f = ColorFormat.ARGB;
			case BufferedImage.TYPE_INT_BGR: f = ColorFormat.BGRA;
			case BufferedImage.TYPE_INT_RGB: f = ColorFormat.RGBA;
			default: f = ColorFormat.RGBA;
		}
		
		format = f;
		
	}
	
	@Override
	public int getPixel(int x, int y)
	{
		return this.img.getRGB(x, y);
	}
	
	@Override
	public int getHeight()
	{
		return this.img.getHeight();
	}
	
	@Override
	public int getWidth()
	{
		return this.img.getWidth();
	}
	
	@Override
	public ColorFormat getFormat()
	{
		return this.format;
	}
	
}
