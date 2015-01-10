
package com.elusivehawk.caelum.render;

import java.nio.FloatBuffer;
import com.elusivehawk.caelum.prefab.Rectangle;
import com.elusivehawk.caelum.render.gl.GL1;
import com.elusivehawk.caelum.render.gl.GLBuffer;
import com.elusivehawk.caelum.render.gl.GLConst;
import com.elusivehawk.caelum.render.gl.GLEnumBufferTarget;
import com.elusivehawk.caelum.render.gl.GLEnumDataType;
import com.elusivehawk.caelum.render.gl.GLEnumDataUsage;
import com.elusivehawk.caelum.render.gl.GLEnumDrawType;
import com.elusivehawk.caelum.render.gl.GLVertexArray;
import com.elusivehawk.caelum.render.tex.Material;
import com.elusivehawk.util.math.MathHelper;
import com.elusivehawk.util.storage.BufferHelper;
import com.elusivehawk.util.storage.DirtableStorage;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class CanvasLayer implements IRenderable
{
	private final Canvas parent;
	private final GLBuffer vertex;
	
	private final DirtableStorage<Material> mat = new DirtableStorage<Material>(null).setSync();
	private final GLVertexArray vao = new GLVertexArray();
	
	private FloatBuffer imgbuf = BufferHelper.createFloatBuffer(Canvas.FLOATS_PER_IMG * 12);
	private Rectangle sub = null;
	private int images = 0;
	
	private boolean expanded = false, updateImgBuf = false;
	
	@SuppressWarnings("unqualified-field-access")
	public CanvasLayer(Canvas cvs)
	{
		parent = cvs;
		vertex = new GLBuffer(GLEnumBufferTarget.GL_ARRAY_BUFFER, GLEnumDataUsage.GL_STREAM_DRAW, GLEnumDataType.GL_FLOAT, imgbuf);
		
		vertex.addAttrib(0, 2, GLConst.GL_FLOAT, false, 16, 0);		//Position data
		vertex.addAttrib(1, 2, GLConst.GL_FLOAT, false, 16, 8);		//Texture off
		
		vao.addVBO(vertex);
		
	}
	
	@Override
	public boolean render(RenderContext rcon) throws RenderException
	{
		if (this.images == 0)
		{
			return false;
		}
		
		if (!this.vao.bind(rcon))
		{
			return false;
		}
		
		if (!this.mat.isNull())
		{
			this.mat.get().bind(rcon);
			
		}
		
		GL1.glDrawArrays(GLEnumDrawType.GL_TRIANGLES, 0, this.images * 6);
		
		this.vao.unbind(rcon);
		
		if (!this.mat.isNull())
		{
			this.mat.get().unbind(rcon);
			
		}
		
		return true;
	}
	
	@Override
	public void preRender(RenderContext rcon)
	{
		if (!this.mat.isNull())
		{
			this.mat.get().preRender(rcon);
			
		}
		
		if (this.imgbuf.position() != 0)
		{
			this.imgbuf.position(0);
			
		}
		
		if (this.updateImgBuf)
		{
			if (this.expanded)
			{
				this.vertex.uploadBuffer(this.imgbuf);
				
				this.expanded = false;
				
			}
			else
			{
				this.vertex.updateVBO(this.imgbuf, 0);
				
			}
			
			this.updateImgBuf = false;
			
		}
		
	}
	
	@Override
	public void postRender(RenderContext rcon) throws RenderException
	{
		if (!this.mat.isNull())
		{
			this.mat.get().postRender(rcon);
			
		}
		
	}
	
	public int getImageCount()
	{
		return this.images;
	}
	
	public void setMaterial(Material m)
	{
		this.mat.set(m);
		
	}
	
	public void setSubCanvas(Rectangle r)
	{
		this.sub = r;
		
	}
	
	public int drawImage(Rectangle r, Icon icon)
	{
		int pos = this.images * Canvas.FLOATS_PER_IMG;
		
		if (this.imgbuf.position() != pos)
		{
			this.imgbuf.position(pos);
			
		}
		
		if (this.imgbuf.remaining() == 0)
		{
			this.imgbuf = BufferHelper.expand(this.imgbuf, Canvas.FLOATS_PER_IMG * 12);
			this.expanded = true;
			
		}
		
		this.addCorners(r, icon);
		
		this.images++;
		this.updateImgBuf = true;
		
		return this.images - 1;
	}
	
	public void redrawImage(int image, Rectangle r, Icon icon)
	{
		assert this.images > 0;
		assert MathHelper.bounds(image, 0, this.images - 1);
		
		synchronized (this)
		{
			this.imgbuf.position(image * Canvas.FLOATS_PER_IMG);
			
			this.addCorners(r, icon);
			
			this.imgbuf.position(0);
			
			this.updateImgBuf = true;
			
		}
		
	}
	
	public void clear()
	{
		this.imgbuf.clear();
		this.images = 0;
		this.sub = null;
		
		this.vertex.updateVBO(this.imgbuf, 0);
		
	}
	
	private void addCorners(Rectangle r, Icon icon)
	{
		if (this.sub != null)
		{
			r = this.sub.interpolate(r);
			
		}
		
		if (icon == null)
		{
			icon = Icon.BLANK_ICON;
			
		}
		
		this.addCorner(r.x, r.y, icon, 0);
		this.addCorner(r.z, r.y, icon, 1);
		this.addCorner(r.x, r.w, icon, 2);
		
		this.addCorner(r.z, r.y, icon, 1);
		this.addCorner(r.x, r.w, icon, 2);
		this.addCorner(r.z, r.w, icon, 3);
		
	}
	
	private void addCorner(float x, float y, Icon icon, int corner)
	{
		if (this.parent == null || this.parent.doCoordCorrection())
		{
			x = (x * 2 - 1);
			y = ((1 - y) * 2 - 1);
			
		}
		
		this.imgbuf.put(x);
		this.imgbuf.put(y);
		this.imgbuf.put(icon.getX(corner));
		this.imgbuf.put(icon.getY(corner));
		
	}
	
}
