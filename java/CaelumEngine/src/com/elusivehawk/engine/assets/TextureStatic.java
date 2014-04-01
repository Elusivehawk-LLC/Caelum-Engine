
package com.elusivehawk.engine.assets;

import com.elusivehawk.engine.render.ILegibleImage;
import com.elusivehawk.engine.render.RenderHelper;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class TextureStatic extends Texture
{
	protected final ILegibleImage image;
	
	@SuppressWarnings("unqualified-field-access")
	public TextureStatic(String filename, ILegibleImage img)
	{
		super(filename);
		image = img;
		
	}
	
	@Override
	public int[] getIds()
	{
		return this.ids;
	}
	
	@Override
	protected boolean finishAsset()
	{
		this.ids[0] = RenderHelper.processImage(this.image);
		
		return this.ids[0] != 0;
	}
	
	@Override
	public ILegibleImage getSourceImg(int frame)
	{
		return this.image;
	}
	
}
