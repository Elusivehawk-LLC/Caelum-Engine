
package com.elusivehawk.engine.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;
import com.elusivehawk.engine.math.Vector;
import com.elusivehawk.engine.math.VectorF;
import com.elusivehawk.engine.render.opengl.GLConst;
import com.elusivehawk.engine.render.opengl.VertexBufferObject;
import com.elusivehawk.engine.render2.Color;
import com.elusivehawk.engine.render2.EnumColorFilter;
import com.elusivehawk.engine.render2.EnumColorFormat;
import com.elusivehawk.engine.render2.RenderContext;
import com.elusivehawk.engine.render2.RenderHelper;
import com.elusivehawk.engine.util.Buffer;
import com.elusivehawk.engine.util.BufferHelper;
import com.elusivehawk.engine.util.SemiFinalStorage;
import com.elusivehawk.engine.util.SemiFinalStorage.IStorageListener;
import com.elusivehawk.engine.util.Tuple;

/**
 * 
 * The modelling system used by This Very Engine!
 * 
 * @author Elusivehawk
 */
@Deprecated
@SuppressWarnings("rawtypes")
public class Model implements IStorageListener
{
	@SuppressWarnings("unchecked")
	public final SemiFinalStorage<Integer> indiceCount = new SemiFinalStorage<Integer>(0, this);
	@SuppressWarnings("unchecked")
	public final SemiFinalStorage<VertexBufferObject> finBuf = new SemiFinalStorage<VertexBufferObject>(null, this);
	@SuppressWarnings("unchecked")
	public final SemiFinalStorage<VertexBufferObject> indiceBuf = new SemiFinalStorage<VertexBufferObject>(null, this);
	
	private boolean finished = false;
	private List<VectorF> polys = new ArrayList<VectorF>();
	private List<Color> color = new ArrayList<Color>();
	private List<VectorF> texOffs = new ArrayList<VectorF>();
	private int glMode = Integer.MIN_VALUE;
	private int pointCount = 0, oldPointCount = 0;
	private Color globalColor = null;
	private HashMap<Integer, Tuple<Integer, Integer>> arrays = new HashMap<Integer, Tuple<Integer, Integer>>();
	
	public void finish(RenderContext context)
	{
		if (this.finished)
		{
			return;
		}
		
		if (this.polys.size() == 0)
		{
			throw new RuntimeException("You forgot to load polygons!");
			
		}
		
		if (this.glMode != Integer.MIN_VALUE)
		{
			this.end();
			
		}
		
		this.finished = true;
		
		List<VectorF> vecs = new ArrayList<VectorF>();
		List<Integer> indiceList = new ArrayList<Integer>();
		
		List<Float> temp = new ArrayList<Float>();
		
		for (int c = 0; c < this.polys.size(); c++)
		{
			VectorF vec = this.polys.get(c);
			
			int index = vecs.indexOf(vec);
			
			if (index == -1)
			{
				Color col = this.color.get(c);
				VectorF tex = this.texOffs.get(c);
				
				index = vecs.size();
				vecs.add(vec);
				
				temp.add(vec.get(Vector.X));
				temp.add(vec.get(Vector.Y));
				temp.add(vec.get(Vector.Z));
				
				if (this.globalColor == null)
				{
					temp.add(col.getColorFloat(EnumColorFilter.RED));
					temp.add(col.getColorFloat(EnumColorFilter.GREEN));
					temp.add(col.getColorFloat(EnumColorFilter.BLUE));
					temp.add(col.getColorFloat(EnumColorFilter.ALPHA));
					
				}
				else
				{
					Buffer<Float> mixed = RenderHelper.mixColors(col, this.globalColor);
					
					for (float f : mixed)
					{
						temp.add(f);
						
					}
					
				}
				
				temp.add(tex.get(Vector.X));
				temp.add(tex.get(Vector.Y));
				
			}
			
			indiceList.add(index);
			
		}
		
		FloatBuffer fin = BufferHelper.makeFloatBuffer(temp).asReadOnlyBuffer();
		IntBuffer indices = BufferHelper.makeIntBuffer(indiceList).asReadOnlyBuffer();
		
		int vb = context.getGL1().glGetInteger(GLConst.GL_VERTEX_ARRAY);
		
		if (vb != 0)
		{
			CaelumEngine.log().log(EnumLogType.WARN, "Temporarily unbinding vertex array!");
			context.getGL3().glBindVertexArray(0);
			
		}
		
		int[] vbos = RenderHelper.createVBOs(2, context);
		
		this.finBuf.set(new VertexBufferObject(vbos[0], GLConst.GL_ARRAY_BUFFER, fin, GLConst.GL_STATIC_DRAW, context));
		this.indiceBuf.set(new VertexBufferObject(vbos[1], GLConst.GL_ELEMENT_ARRAY_BUFFER, indices, GLConst.GL_STATIC_DRAW, context));
		
		if (vb != 0)
		{
			CaelumEngine.log().log(EnumLogType.WARN, "Rebinding vertex array");
			context.getGL3().glBindVertexArray(vb);
			
		}
		
		this.polys = null;
		this.color = null;
		this.texOffs = null;
		
		this.indiceCount.set(this.pointCount);
		
	}
	
	public final void begin(int gl)
	{
		if (this.finished)
		{
			return;
		}
		
		if (this.glMode != Integer.MIN_VALUE)
		{
			throw new RuntimeException("You're already adding vertices!");
			
		}
		
		if (RenderHelper.getPointCount(gl) == 0)
		{
			throw new RuntimeException("Invalid GL mode!");
			
		}
		
		if (this.arrays.containsKey(gl))
		{
			throw new RuntimeException("You already used this mode!");
			
		}
		
		this.glMode = gl;
		this.oldPointCount = this.polys.size();
		
	}
	
	public final void end()
	{
		if (this.finished)
		{
			return;
		}
		
		if (this.glMode == Integer.MIN_VALUE)
		{
			throw new RuntimeException("You need to call begin() first!");
			
		}
		
		int points = RenderHelper.getPointCount(this.glMode);
		int vectors = (this.polys.size() - this.oldPointCount);
		
		if (vectors % points != 0)
		{
			throw new RuntimeException("Odd number of vectors loaded: " + vectors + " in mode " + this.glMode);
			
		}
		
		this.pointCount += vectors;
		
		while (this.color.size() < this.polys.size())
		{
			this.color.add(new Color(EnumColorFormat.RGBA));
			
		}
		
		while (this.texOffs.size() < this.polys.size())
		{
			this.texOffs.add(new VectorF(2, 0f, 0f));
			
		}
		
		Tuple<Integer, Integer> t = new Tuple<Integer, Integer>(this.oldPointCount, this.pointCount);
		
		this.arrays.put(this.glMode, t);
		
		this.glMode = Integer.MIN_VALUE;
		
	}
	
	public final void vertex(float x, float y, float z)
	{
		if (this.finished)
		{
			return;
		}
		
		this.vertex(new VectorF(3, x, y, z));
		
	}
	
	public final void vertex(VectorF vec)
	{
		if (this.finished)
		{
			return;
		}
		
		if (vec.getSize() < 3)
		{
			return;
		}
		
		this.polys.add(vec);
		
	}
	
	public final void color(int r, int g, int b, int a)
	{
		if (this.finished)
		{
			return;
		}
		
		this.color(new Color(EnumColorFormat.RGBA, r, g, b, a));
		
	}
	
	public final void color(Color col)
	{
		if (this.finished)
		{
			return;
		}
		
		this.color.add(col);
		
	}
	
	public final void globalColor(int r, int g, int b, int a)
	{
		if (this.finished)
		{
			return;
		}
		
		this.globalColor(new Color(EnumColorFormat.RGBA, r, g, b, a));
		
	}
	
	public final void globalColor(Color col)
	{
		if (this.finished)
		{
			return;
		}
		
		this.globalColor = col;
		
	}
	
	public final void texOff(float x, float y)
	{
		if (this.finished)
		{
			return;
		}
		
		this.texOffs.add(new VectorF(2, x, y));
		
	}
	
	public HashMap<Integer, Tuple<Integer, Integer>> getOffsets()
	{
		return this.arrays;
	}
	
	public boolean isFinished()
	{
		return this.finished;
	}
	
	@Override
	public boolean canChange(SemiFinalStorage stor)
	{
		return this.isFinished();
	}
	
}
