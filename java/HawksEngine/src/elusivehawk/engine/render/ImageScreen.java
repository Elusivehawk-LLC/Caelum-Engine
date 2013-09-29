
package elusivehawk.engine.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import elusivehawk.engine.util.BufferHelper;

/**
 * 
 * The new system for 2D rendering.
 * 
 * @author Elusivehawk
 */
public class ImageScreen
{
	public final GLProgram p;
	public final VertexBufferObject vbo, indices;
	private final FloatBuffer buf;
	private final IntBuffer indiceBuf;
	private final List<ImageData> data = new ArrayList<ImageData>();
	
	public ImageScreen(int maxImgs)
	{
		this(new GLProgram(), maxImgs);
		
	}
	
	public ImageScreen(GLProgram program, int maxImgs)
	{
		p = program;
		
		buf = BufferUtils.createFloatBuffer(maxImgs * 40);
		indiceBuf = BufferUtils.createIntBuffer(maxImgs * 6);
		
		vbo = new VertexBufferObject(GL.GL_ARRAY_BUFFER);
		indices = new VertexBufferObject(GL.GL_ELEMENT_ARRAY_BUFFER);
		
		p.attachVBOs(vbo, indices);
		p.createAttribPointers(buf);
		
	}
	
	public int addImage(ImageData info, int xPos, int yPos)
	{
		if (this.getImgCount() == this.buf.limit() / 40)
		{
			throw new ArrayIndexOutOfBoundsException("Image limit hit!");
		}
		
		int position = this.data.size();
		
		info.pos.one = xPos;
		info.pos.two = yPos;
		
		FloatBuffer img = this.generateImgBuffer(info);
		
		this.buf.position(position * 40);
		this.buf.put(img);
		
		IntBuffer ind = BufferUtils.createIntBuffer(6);
		
		int indiceOff = position * 4;
		
		ind.put(indiceOff).put(indiceOff + 1).put(indiceOff + 2);
		ind.put(indiceOff + 1).put(indiceOff + 2).put(indiceOff + 3);
		
		ind.flip();
		
		this.indiceBuf.position(position * 6);
		this.indiceBuf.put(ind);
		
		this.data.add(info);
		
		return position;
	}
	
	public ImageData getImg(int index)
	{
		return this.data.get(index);
	}
	
	public void removeImg(int index)
	{
		int offset = index * 40;
		
		FloatBuffer remains = BufferHelper.makeFloatBuffer(this.buf, offset + 40, (this.getImgCount() - index) * 40);
		
		this.vbo.updateVBO(remains, offset);
		
		this.buf.position(offset);
		this.buf.put(remains);
		
		for (int c = 0; c < 40; c++)
		{
			this.buf.put(0f);
			
		}
		
		this.data.remove(index);
		
	}
	
	public void updateImages()
	{
		for (int c = 0; c < this.getImgCount(); c++)
		{
			ImageData info = this.getImg(c);
			IExtraImageData mgr = info.mgr;
			
			if (mgr.updateImagePosition(c, info))
			{
				FloatBuffer img = this.generateImgBuffer(info);
				
				this.buf.position(c * 40);
				this.buf.put(img);
				
				this.vbo.updateVBO(img, c * 40);
				
			}
			
		}
		
	}
	
	public int getImgCount()
	{
		return this.data.size();
	}
	
	public FloatBuffer generateImgBuffer(ImageData info)
	{
		FloatBuffer ret = BufferUtils.createFloatBuffer(40);
		
		int x = info.pos.one;
		int y = info.pos.two;
		
		float a = x / Display.getWidth();
		float b = y / Display.getHeight();
		float c = (x + info.width) / Display.getWidth();
		float d = (y + info.height) / Display.getHeight();
		
		ret.put(a).put(b).put(0).put(1f);
		info.mgr.getColor(0).store(ret);
		info.mgr.getTextureOffset(0).store(ret);
		
		ret.put(c).put(b).put(0).put(1f);
		info.mgr.getColor(1).store(ret);
		info.mgr.getTextureOffset(1).store(ret);
		
		ret.put(a).put(d).put(0).put(1f);
		info.mgr.getColor(2).store(ret);
		info.mgr.getTextureOffset(2).store(ret);
		
		ret.put(c).put(d).put(0).put(1f);
		info.mgr.getColor(3).store(ret);
		info.mgr.getTextureOffset(3).store(ret);
		
		ret.flip();
		
		return ret;
	}
	
}
