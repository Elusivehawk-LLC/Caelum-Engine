
package com.elusivehawk.caelum.render;

import java.nio.FloatBuffer;
import com.elusivehawk.caelum.prefab.Rectangle;
import com.elusivehawk.caelum.render.gl.GL1;
import com.elusivehawk.caelum.render.gl.GLConst;
import com.elusivehawk.caelum.render.gl.GLEnumBufferTarget;
import com.elusivehawk.caelum.render.gl.GLEnumDataType;
import com.elusivehawk.caelum.render.gl.GLEnumDataUsage;
import com.elusivehawk.caelum.render.gl.GLEnumDrawType;
import com.elusivehawk.caelum.render.gl.GLProgram;
import com.elusivehawk.caelum.render.gl.VertexBuffer;
import com.elusivehawk.util.math.MathHelper;
import com.elusivehawk.util.storage.BufferHelper;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class Canvas extends RenderableObj
{
	public static final Icon BLANK_ICON = new Icon(0, 0, 0, 0);
	
	private FloatBuffer vertex = BufferHelper.createFloatBuffer(RenderConst.FLOATS_PER_IMG * 12);
	
	private final VertexBuffer floatbuf;
	
	private Rectangle sub = null;
	private int images = 0;
	private boolean expanded = false;
	
	public Canvas()
	{
		this(new GLProgram());
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public Canvas(GLProgram program)
	{
		super(program);
		
		zBuffer = false;
		
		floatbuf = new VertexBuffer(GLEnumBufferTarget.GL_ARRAY_BUFFER, GLEnumDataUsage.GL_DYNAMIC_DRAW, GLEnumDataType.GL_FLOAT, this.vertex);
		
		program.addVertexAttrib("in_pos", 2, GLConst.GL_UNSIGNED_INT, false, 0, 0);
		program.addVertexAttrib("in_tex", 2, GLConst.GL_UNSIGNED_INT, false, 2, 0);
		program.addVertexAttrib("in_mat", 1, GLConst.GL_UNSIGNED_INT, false, 4, 0);
		
		vao.attachVBO(this.floatbuf, 0, 1);
		
	}
	
	@Override
	protected boolean initiate(RenderContext rcon)
	{
		return true;
	}
	
	@Override
	protected void doRender(RenderContext rcon) throws RenderException
	{
		GL1.glDrawElements(GLEnumDrawType.GL_TRIANGLES, this.images * 2, GLConst.GL_UNSIGNED_INT, 0);
		
	}
	
	@Override
	public void preRender(RenderContext rcon, double delta)
	{
		if (this.images == 0)
		{
			return;
		}
		
		if (this.expanded)
		{
			this.floatbuf.uploadBuffer(this.vertex);
			
			this.expanded = false;
			
		}
		else if (this.isDirty())
		{
			this.floatbuf.updateVBO(this.vertex, 0);
			
		}
		
	}
	
	@Override
	public void postRender(RenderContext rcon)
	{
		this.setIsDirty(false);
		
	}
	
	@SuppressWarnings("sync-override")
	@Override
	public RenderableObj setEnableZBuffer(boolean z)
	{
		if (z)
		{
			throw new UnsupportedOperationException("One does not simply enable Z buffering for a 2D canvas");
		}
		
		return this;
	}
	
	public int getImageCount()
	{
		return this.images;
	}
	
	public void createSubCanvas(float xmin, float ymin, float xmax, float ymax)
	{
		this.createSubCanvas(new Rectangle(xmin, ymin, xmax, ymax));
		
	}
	
	public void createSubCanvas(Rectangle r)
	{
		if (this.sub == null)
		{
			this.sub = r;
			
		}
		
	}
	
	public boolean destroySubCanvas()
	{
		if (this.sub == null)
		{
			return false;
		}
		
		this.sub = null;
		
		return true;
	}
	
	public void drawImage(float x, float y, float z, float w, int mat)
	{
		this.drawImage(x, y, z, w, BLANK_ICON, mat);
		
	}
	
	public void drawImage(float x, float y, float z, float w, Icon icon)
	{
		this.drawImage(x, y, z, w, icon, 0);
		
	}
	
	public void drawImage(float x, float y, float z, float w, Icon icon, int mat)
	{
		this.drawImage(new Rectangle(x, y, z, w), icon, mat);
		
	}
	
	public void drawImage(Rectangle r, Icon icon)
	{
		this.drawImage(r, icon, 0);
		
	}
	
	public void drawImage(Rectangle r, int mat)
	{
		this.drawImage(r, BLANK_ICON, mat);
		
	}
	
	public void drawImage(Rectangle r, Icon icon, int mat)
	{
		if (this.sub != null)
		{
			r = this.sub.interpolate(r);
			
		}
		
		if (icon == null)
		{
			icon = BLANK_ICON;
			
		}
		
		mat = MathHelper.clamp(mat, 0, RenderConst.MATERIAL_CAP - 1);
		
		if (this.vertex.remaining() == 0)
		{
			FloatBuffer fb = BufferHelper.expand(this.vertex, RenderConst.FLOATS_PER_IMG * 4);
			
			synchronized (this)
			{
				this.vertex = fb;
				this.expanded = true;
				
			}
			
		}
		
		this.addCorner(r.x, r.y, mat, 0, icon);
		this.addCorner(r.z, r.y, mat, 1, icon);
		this.addCorner(r.x, r.w, mat, 2, icon);
		
		this.addCorner(r.z, r.y, mat, 1, icon);
		this.addCorner(r.x, r.w, mat, 2, icon);
		this.addCorner(r.z, r.w, mat, 3, icon);
		
		this.images++;
		
	}
	
	public void clear()
	{
		this.vertex.clear();
		this.images = 0;
		
	}
	
	private void addCorner(float a, float b, int m, int corner, Icon icon)
	{
		this.vertex.put(a);
		this.vertex.put(b);
		this.vertex.put(icon.getCorner(corner)[0]);
		this.vertex.put(icon.getCorner(corner)[1]);
		this.vertex.put(m);
		
	}
	
}
