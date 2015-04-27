
package com.elusivehawk.caelum.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.elusivehawk.caelum.CaelumException;
import com.elusivehawk.caelum.prefab.Entity;
import com.elusivehawk.caelum.prefab.IComponent;
import com.elusivehawk.caelum.prefab.Position;
import com.elusivehawk.caelum.render.gl.GL1;
import com.elusivehawk.caelum.render.gl.GL2;
import com.elusivehawk.caelum.render.gl.GLBuffer;
import com.elusivehawk.caelum.render.gl.GLConst;
import com.elusivehawk.caelum.render.gl.GLEnumBufferTarget;
import com.elusivehawk.caelum.render.gl.GLEnumDataUsage;
import com.elusivehawk.caelum.render.gl.GLEnumDrawType;
import com.elusivehawk.caelum.render.gl.GLProgram;
import com.elusivehawk.caelum.render.gl.GLVertexArray;
import com.elusivehawk.caelum.render.tex.Material;
import com.elusivehawk.util.math.MatrixF;
import com.elusivehawk.util.math.QuaternionF;
import com.elusivehawk.util.math.VectorF;
import com.elusivehawk.util.storage.BufferHelper;
import com.elusivehawk.util.storage.DirtableStorage;

/**
 * 
 * Used to render static meshes with non-static information (i.e. rotation)
 * 
 * @author Elusivehawk
 * 
 * @see IMeshDataProvider
 * @see ProgramRenderable
 */
public class MeshRenderer extends ProgramRenderable implements IComponent, QuaternionF.Listener, VectorF.Listener
{
	private final VectorF
			offset = new VectorF(),
			pos = new VectorF(),
			scale = new VectorF(1f, 1f, 1f);
	
	private final QuaternionF
			rotOff = new QuaternionF(),
			rot = new QuaternionF();
	
	private final DirtableStorage<Material> mat = new DirtableStorage<Material>().setSync();
	private final DirtableStorage<MeshData> mesh = new DirtableStorage<MeshData>().setSync();
	
	private final GLVertexArray vao = new GLVertexArray();
	private final GLBuffer animbuf = new GLBuffer(GLEnumBufferTarget.GL_ARRAY_BUFFER);
	
	private FloatBuffer buf = null;
	private GLEnumDrawType draw = null;
	
	private int texFrame = 0, polyCount = 0;
	
	public MeshRenderer()
	{
		this(new GLProgram());
		
	}
	
	public MeshRenderer(GLProgram p)
	{
		this(p, null);
		
	}
	
	public MeshRenderer(MeshData m)
	{
		this(new GLProgram(), m);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public MeshRenderer(GLProgram program, MeshData m)
	{
		super(program);
		
		mesh.set(m);
		
	}
	
	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onVecChanged(VectorF vec)
	{
		this.offset.add(vec, this.pos);
		
	}
	
	@Override
	public void onQuatChanged(QuaternionF q)
	{
		this.rotOff.add(q, this.rot);
		
	}
	
	@Override
	public void initiate(Entity parent)
	{
		Position position = (Position)parent.getChild(((comp) -> {return comp instanceof Position;}));
		
		if (position != null)
		{
			position.addListener(this);
			
		}
		
	}
	
	@Override
	protected void doRender(RenderContext rcon) throws RenderException
	{
		if (!this.mat.isNull())
		{
			this.mat.get().render(rcon);
			
		}
		
		if (this.vao.bind(rcon))
		{
			Material m = this.mat.get();
			
			if (m == null || m.bind(rcon))
			{
				GL1.glDrawElements(this.draw, this.polyCount, GLConst.GL_UNSIGNED_INT, 0);
				
			}
			
			if (m != null)
			{
				m.unbind(rcon);
				
			}
			
		}
		
		this.vao.unbind(rcon);
		
	}
	
	@Override
	public void setMaterial(Material mat)
	{
		this.mat.set(mat);
		
	}
	
	@Override
	public boolean preRenderImpl(RenderContext rcon)
	{
		if (this.mesh.isNull())
		{
			return false;
		}
		
		if (this.mesh.isDirty())
		{
			this.mesh.setIsDirty(false);
			
			if (!this.loadMesh(rcon))
			{
				return false;
			}
			
		}
		
		if (!this.mat.isNull())
		{
			this.mat.get().preRender(rcon);
			
		}
		
		if (this.pos.isDirty() || this.rot.isDirty())
		{
			if (this.program.bind(rcon))
			{
				GL2.glUniformMatrix4("model", new MatrixF().homogenous(this.rot, this.scale, this.pos));
				
			}
			
			this.program.unbind(rcon);
			
			this.pos.setIsDirty(false);
			this.rot.setIsDirty(false);
			
		}
		
		return true;
	}
	
	@Override
	public void postRenderImpl(RenderContext rcon) throws RenderException
	{
		if (!this.mat.isNull())
		{
			this.mat.get().postRender(rcon);
			
		}
		
	}
	
	@Override
	protected boolean initiate(RenderContext rcon)
	{
		return this.loadMesh(rcon);
	}
	
	@Override
	public String toString()
	{
		return String.format("%s:%s-%s-%s", this.mesh, this.pos.toString(), this.scale.toString(), this.rot.toString());
	}
	
	private boolean loadMesh(RenderContext rcon)
	{
		MeshData data = this.mesh.get();
		
		if (data == null)
		{
			return false;
		}
		
		if (!data.isLoaded())
		{
			data.load(rcon);
			
		}
		
		FloatBuffer vtx = data.vertex();
		IntBuffer ind = data.indices();
		
		if (data.isStrip())
		{
			this.draw = GLEnumDrawType.GL_TRIANGLE_STRIP;
			this.polyCount = ind.capacity() - 2;
			
		}
		else
		{
			this.draw = GLEnumDrawType.GL_TRIANGLES;
			this.polyCount = ind.capacity() / 3;
			
		}
		
		this.buf = BufferHelper.createFloatBuffer(vtx.capacity() / 3 * 16 * 3);
		
		this.animbuf.init(rcon, this.buf, GLEnumDataUsage.GL_DYNAMIC_DRAW);
		
		this.animbuf.addAttrib(RenderConst.SCALE, 4, GLConst.GL_FLOAT, 48, 0);
		this.animbuf.addAttrib(RenderConst.ROTATE, 4, GLConst.GL_FLOAT, 48, 16);
		this.animbuf.addAttrib(RenderConst.TRANS, 4, GLConst.GL_FLOAT, 48, 32);
		
		this.vao.addVBO(data.getVBO());
		this.vao.addVBO(data.getIBO());
		this.vao.addVBO(this.animbuf);
		
		return true;
	}
	
	public MeshData getMesh()
	{
		return this.mesh.get();
	}
	
	public boolean setMesh(MeshData mesh)
	{
		return this.mesh.set(mesh);
	}
	
	public int getCurrentTexFrame()
	{
		return this.texFrame;
	}
	
	public MeshRenderer setPosOffset(VectorF off)
	{
		this.offset.set(off);
		this.pos.add(this.offset);
		
		return this;
	}
	
	public MeshRenderer setScale(VectorF s)
	{
		for (int c = 0; c < s.size(); c++)
		{
			if (s.get(c) > 0f)
			{
				continue;
			}
			
			throw new CaelumException("[%s]: Vector %s has an invalid scaling float at position %s", this, s, c);
		}
		
		this.scale.set(s);
		
		return this;
	}
	
	public MeshRenderer setRotOffset(QuaternionF qoff)
	{
		this.rotOff.set(qoff);
		this.rot.add(this.rotOff);
		
		return this;
	}
	
}
