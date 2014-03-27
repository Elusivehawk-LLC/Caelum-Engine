
package com.elusivehawk.engine.render;

import java.nio.FloatBuffer;
import java.util.HashMap;
import com.elusivehawk.engine.assets.Asset;
import com.elusivehawk.engine.assets.EnumAssetType;
import com.elusivehawk.engine.assets.IAssetReceiver;
import com.elusivehawk.engine.math.Matrix;
import com.elusivehawk.engine.math.MatrixHelper;
import com.elusivehawk.engine.math.Vector;
import com.elusivehawk.engine.render.old.Model;
import com.elusivehawk.engine.render.opengl.GLConst;
import com.elusivehawk.engine.render.opengl.GLProgram;
import com.elusivehawk.engine.render.opengl.VertexBufferObject;
import com.elusivehawk.engine.util.BufferHelper;
import com.elusivehawk.engine.util.IDirty;

/**
 * 
 * Used to render static {@link Model}s with non-static information (i.e. rotation)
 * 
 * @author Elusivehawk
 */
public class RenderTicket implements IDirty, ILogicalRender, IAssetReceiver
{
	protected final HashMap<EnumVectorType, Vector> vecs = new HashMap<EnumVectorType, Vector>();
	protected final Model m;
	protected final GLProgram p;
	protected final VertexBufferObject vbo;
	protected final FloatBuffer buf;
	
	protected boolean dirty = false, zBuffer = true;//, animPause = false;
	//protected int frame = 0;
	//protected IModelAnimation anim = null, lastAnim = null;
	protected Asset tex;
	
	public RenderTicket(Model model)
	{
		this(new GLProgram(), model);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public RenderTicket(GLProgram program, Model model)
	{
		assert program != null;
		assert model != null;
		
		p = program;
		m = model;
		buf = BufferHelper.createFloatBuffer(m.indiceCount.get() * 3);
		vbo = new VertexBufferObject(GLConst.GL_ARRAY_BUFFER, buf, GLConst.GL_STREAM_DRAW);
		
		p.attachRenderTicket(this);
		
		for (EnumVectorType type : EnumVectorType.values())
		{
			vecs.put(type, type.getDefault());
			
		}
		
	}
	
	@Override
	public boolean isDirty()
	{
		return this.dirty;
	}
	
	@Override
	public void setIsDirty(boolean b)
	{
		this.dirty = b;
		
	}
	
	@Override
	public GLProgram getProgram()
	{
		return this.p;
	}
	
	@Override
	public boolean updateBeforeUse()
	{
		if (!this.m.isFinished())
		{
			return false;
		}
		
		/*if (this.anim != null && !this.isAnimationPaused())
		{
			boolean usedBefore = this.anim == this.lastAnim;
			
			if (!usedBefore)
			{
				this.frame = 0;
				
			}
			
			boolean fin = (this.frame == this.anim.getFrameCount());
			
			if (this.anim.update(this, usedBefore, fin))
			{
				this.buf.rewind();
				
				this.vbo.updateEntireVBO(this.buf, context);
				
			}
			
			this.frame = (fin ? 0 : this.frame + 1);
			
		}*/
		
		if (this.isDirty())
		{
			Matrix m = MatrixHelper.createHomogenousMatrix(this.vecs.get(EnumVectorType.ROTATION), this.vecs.get(EnumVectorType.SCALING), this.vecs.get(EnumVectorType.TRANSLATION));
			
			this.p.attachUniform("model", m.asBuffer(), GLProgram.EnumUniformType.M_FOUR);
			
			this.setIsDirty(false);
			
		}
		
		return true;
	}
	
	@Override
	public synchronized void onAssetLoaded(Asset asset)
	{
		if (asset.getType() == EnumAssetType.TEXTURE && this.tex == null)
		{
			this.tex = asset;
			
		}
		
	}
	
	public synchronized void setVector(EnumVectorType type, Vector vec)
	{
		this.vecs.get(type).set(vec);
		
		this.dirty = true;
		
	}
	
	/*public synchronized void setModelAnimation(IModelAnimation a)
	{
		this.lastAnim = this.anim;
		
		this.anim = a;
		
	}
	
	public synchronized void setFrame(int f)
	{
		this.frame = f;
		
	}*/
	
	public synchronized void setTexture(Asset texture)
	{
		assert texture.getType() == EnumAssetType.TEXTURE;
		
		this.tex = texture;
		
	}
	
	public synchronized void setEnableZBuffer(boolean b)
	{
		this.zBuffer = b;
		
	}
	
	/**
	 * 
	 * NOTICE: Do *NOT* call this outside of the designated model animation, since it's not synchronized.
	 * 
	 * @param pos
	 * @param rot
	 * @param trans
	 * @param scale
	 */
	public void setIndice(int pos, Vector rot, Vector trans, Vector scale)
	{
		this.buf.position(pos * 9);
		
		for (int c = 0; c < rot.getSize(); c++)
		{
			this.buf.put(rot.get(c));
			
		}
		
		for (int c = 0; c < trans.getSize(); c++)
		{
			this.buf.put(trans.get(c));
			
		}
		
		for (int c = 0; c < scale.getSize(); c++)
		{
			this.buf.put(scale.get(c));
			
		}
		
	}
	
	public FloatBuffer getBuffer()
	{
		return this.buf;
	}
	
	public Model getModel()
	{
		return this.m;
	}
	
	public VertexBufferObject getExtraVBO()
	{
		return this.vbo;
	}
	
	/*public int getCurrentFrame()
	{
		return this.frame;
	}*/
	
	public Asset getTexture()
	{
		return this.tex;
	}
	
	public boolean enableZBuffering()
	{
		return this.zBuffer;
	}
	
	public static enum EnumVectorType
	{
		ROTATION(new Vector(0f, 0f, 0f)),
		TRANSLATION(new Vector(0f, 0f, 0f)),
		SCALING(new Vector(1.0f, 1.0f, 1.0f));
		
		private final Vector vec;
		
		@SuppressWarnings("unqualified-field-access")
		EnumVectorType(Vector d)
		{
			vec = d;
			
		}
		
		protected Vector getDefault()
		{
			return new Vector(this.vec);
		}
		
	}
	
}
