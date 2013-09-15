
package elusivehawk.engine.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import elusivehawk.engine.util.GameLog;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class AnimatedTexture implements ITexture
{
	private final IntBuffer tex;
	private final int w, h;
	
	public AnimatedTexture(File gif, int width, int height, boolean is3D)
	{
		tex = RenderHelper.processGifFile(gif, is3D);
		w = width;
		h = height;
		
	}
	
	public AnimatedTexture(File file, boolean is3D, int y)
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
			GameLog.error(e);
			
		}
		
		if (img == null)
		{
			tex = BufferUtils.createIntBuffer(1);
			w = 0;
			h = 0;
			
		}
		else
		{
			if (img.getHeight() % y != 0)
			{
				throw new RuntimeException("Image is not fit for animating!");
			}
			
			tex = BufferUtils.createIntBuffer(img.getHeight() / y);
			w = img.getWidth();
			h = img.getHeight();
			
			for (int c = 0; c < img.getHeight(); c += y)
			{
				BufferedImage sub = img.getSubimage(0, c, img.getWidth(), y);
				
				tex.put(RenderHelper.processImage(sub, is3D, false));
				
			}
			
		}
		
	}
	
	@Override
	public int getTexture()
	{
		if (this.tex.remaining() == 0)
		{
			this.tex.rewind();
			
		}
		
		return this.tex.get();
	}
	
	@Override
	public int getHeight()
	{
		return this.h;
	}
	
	@Override
	public int getWidth()
	{
		return this.w;
	}
	
}
