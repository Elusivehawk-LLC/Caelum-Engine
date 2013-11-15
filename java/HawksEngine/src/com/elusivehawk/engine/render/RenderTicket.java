
package com.elusivehawk.engine.render;

import java.nio.FloatBuffer;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import com.elusivehawk.engine.core.IDirty;
import com.elusivehawk.engine.math.Matrix;
import com.elusivehawk.engine.math.MatrixHelper;
import com.elusivehawk.engine.math.Vector3f;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class RenderTicket implements IDirty, ILogicalRender
{
	protected final HashMap<EnumVectorType, Vector3f> vecs = new HashMap<EnumVectorType, Vector3f>();
	protected final Model m;
	protected final GLProgram p;
	protected final VertexBufferObject vbo;
	protected final FloatBuffer buf;
	
	protected boolean dirty = false, animPause = false;
	protected int frame = 0;
	protected IModelAnimation anim = null, lastAnim = null;
	protected ITexture tex;
	
	public RenderTicket(Model model)
	{
		this(new GLProgram(), model);
		
	}
	
	public RenderTicket(GLProgram program, Model model)
	{
		p = program;
		m = model;
		buf = BufferUtils.createFloatBuffer(m.indiceCount * 3);
		vbo = new VertexBufferObject(GL.GL_VERTEX_ARRAY, this.buf, GL.GL_STREAM_DRAW);
		
		p.attachRenderTicket(this);
		
		for (EnumVectorType type : EnumVectorType.values())
		{
			vecs.put(type, type.getDefault());
			
		}
		
	}
	
	public synchronized void setVector(EnumVectorType type, Vector3f vec)
	{
		this.vecs.get(type).set(vec);
		
		this.dirty = true;
		
	}
	
	public synchronized void setModelAnimation(IModelAnimation a)
	{
		this.lastAnim = this.anim;
		
		this.anim = a;
		
	}
	
	public synchronized void setFrame(int f)
	{
		this.frame = f;
		
	}
	
	public synchronized void setTexture(ITexture texture)
	{
		this.tex = texture;
		
	}
	
	public synchronized void setAnimationPaused(boolean b)
	{
		this.animPause = b;
		
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
	public void setIndice(int pos, Vector3f rot, Vector3f trans, Vector3f scale)
	{
		this.buf.position(pos * 9);
		
		rot.store(this.getBuffer());
		trans.store(this.getBuffer());
		scale.store(this.getBuffer());
		
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
	
	public int getCurrentFrame()
	{
		return this.frame;
	}
	
	public ITexture getTexture()
	{
		return this.tex;
	}
	
	public boolean isAnimationPaused()
	{
		return this.animPause;
	}
	
	@Override
	public GLProgram getProgram()
	{
		return this.p;
	}
	
	@Override
	public boolean updateBeforeUse(IRenderHUB hub)
	{
		if (!hub.getRenderMode().is3D())
		{
			return false;
		}
		
		if (!this.isAnimationPaused() && this.anim != null)
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
				
				this.vbo.updateEntireVBO(this.buf);
				
			}
			
			this.frame = (fin ? 0 : this.frame + 1);
			
		}
		
		if (this.isDirty())
		{
			Matrix m = MatrixHelper.createHomogenousMatrix(this.vecs.get(EnumVectorType.ROTATION), this.vecs.get(EnumVectorType.SCALING), this.vecs.get(EnumVectorType.TRANSLATION));
			
			this.p.attachUniform("model", m.asBuffer(), GLProgram.EnumUniformType.M_FOUR);
			
			hub.getCamera().updateUniform(this.getProgram());
			
			this.setIsDirty(false);
			
		}
		
		return true;
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
	
	public static enum EnumVectorType
	{
		ROTATION(new Vector3f()),
		TRANSLATION(new Vector3f()),
		SCALING(new Vector3f(1.0f, 1.0f, 1.0f));
		
		private final Vector3f vec;
		
		EnumVectorType(Vector3f d)
		{
			vec = d;
			
		}
		
		protected Vector3f getDefault()
		{
			return this.vec.clone();
		}
		
	}
	
}
