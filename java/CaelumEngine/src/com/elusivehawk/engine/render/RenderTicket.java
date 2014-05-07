
package com.elusivehawk.engine.render;

import java.nio.FloatBuffer;
import com.elusivehawk.engine.assets.Asset;
import com.elusivehawk.engine.assets.IAssetReceiver;
import com.elusivehawk.engine.assets.Material;
import com.elusivehawk.engine.assets.Shader;
import com.elusivehawk.engine.assets.Texture;
import com.elusivehawk.engine.math.MathHelper;
import com.elusivehawk.engine.math.Vector;
import com.elusivehawk.engine.render.opengl.GLConst;
import com.elusivehawk.engine.render.opengl.GLProgram;
import com.elusivehawk.engine.render.opengl.VertexBuffer;
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
	protected final Model m;
	protected final Material[] mats = RenderHelper.createMaterials();
	protected final Shader[] sh = RenderHelper.createShaders();
	
	protected GLProgram p = null;
	protected FloatBuffer buf = null;
	protected VertexBuffer vbo = null;
	
	protected boolean dirty = true, zBuffer = true, initiated = false;//, animPause = false;
	//protected int frame = 0;
	//protected IModelAnimation anim = null, lastAnim = null;
	protected Texture tex;
	protected int matCount = 0;
	
	@SuppressWarnings("unqualified-field-access")
	public RenderTicket(Model mdl)
	{
		assert mdl != null;
		
		m = mdl;
		
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
	public boolean updateBeforeUse(RenderContext context)
	{
		if (!this.initiated)
		{
			if (!this.m.isFinished())
			{
				this.m.finish();
				
			}
			
			if (!this.m.isFinished())
			{
				return false;
			}
			
			this.buf = BufferHelper.createFloatBuffer(this.m.getTotalPointCount() * RenderConst.FLOATS_PER_POINT);
			this.vbo = new VertexBuffer(GLConst.GL_ARRAY_BUFFER, this.buf, GLConst.GL_STREAM_DRAW);
			
			this.p = new GLProgram(context, this.sh);
			this.p.attachRenderTicket(this);
			
			this.initiated = true;
			
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
			/*Matrix m = MatrixHelper.createHomogenousMatrix(this.vecs.get(EnumVectorType.ROTATION), this.vecs.get(EnumVectorType.SCALING), this.vecs.get(EnumVectorType.TRANSLATION));
			
			this.p.attachUniform("model", m.asBuffer(), GLProgram.EnumUniformType.M_FOUR);*/
			
			
			
			this.setIsDirty(false);
			
		}
		
		return true;
	}
	
	@Override
	public void onAssetLoaded(Asset a)
	{
		if (a instanceof Shader)
		{
			synchronized (this)
			{
				if (this.p == null)
				{
					Shader s = (Shader)a;
					
					this.sh[s.gltype.ordinal()] = s;
					
				}
				else
				{
					this.p.attachShader((Shader)a);
					
				}
				
			}
			
		}
		else if (a instanceof Texture)
		{
			this.setTexture((Texture)a);
			
		}
		else if (a instanceof Material)
		{
			this.addMaterials((Material)a);
			
		}
		
	}
	
	/*public synchronized void setVector(EnumVectorType type, Vector vec)
	{
		this.vecs.get(type).set(vec);
		
		this.setIsDirty(true);
		
	}
	
	public synchronized void setModelAnimation(IModelAnimation a)
	{
		this.lastAnim = this.anim;
		
		this.anim = a;
		
	}
	
	public synchronized void setFrame(int f)
	{
		this.frame = f;
		
	}*/
	
	public synchronized void setTexture(Texture texture)
	{
		this.tex = texture;
		
	}
	
	public synchronized void setEnableZBuffer(boolean b)
	{
		this.zBuffer = b;
		
	}
	
	public synchronized void addMaterials(Material... materials)
	{
		for (Material mat : materials)
		{
			if (this.matCount == RenderConst.MATERIAL_CAP)
			{
				return;
			}
			
			this.mats[this.matCount++] = mat;
			this.setIsDirty(true);
			
		}
		
	}
	
	public synchronized void setMaterial(int i, Material m)
	{
		assert MathHelper.bounds(i, 0, 15);
		
		if (this.mats[i] == null)
		{
			this.matCount++;
			
		}
		
		this.mats[i] = m;
		
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
	
	public VertexBuffer getExtraVBO()
	{
		return this.vbo;
	}
	
	/*public int getCurrentFrame()
	{
		return this.frame;
	}*/
	
	public Texture getTexture()
	{
		return this.tex;
	}
	
	public boolean enableZBuffering()
	{
		return this.zBuffer;
	}
	
}
