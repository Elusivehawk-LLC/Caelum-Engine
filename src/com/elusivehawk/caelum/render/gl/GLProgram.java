
package com.elusivehawk.caelum.render.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import com.elusivehawk.caelum.render.RenderContext;
import com.elusivehawk.caelum.render.RenderHelper;
import com.elusivehawk.util.IDirty;
import com.elusivehawk.util.IPopulator;
import com.elusivehawk.util.storage.ArrayHelper;
import com.elusivehawk.util.storage.BufferHelper;
import com.elusivehawk.util.storage.SyncList;

/**
 * 
 * A class to help work with OpenGL's program system.
 * 
 * @author Elusivehawk
 */
public final class GLProgram implements IGLBindable, IDirty
{
	private final List<VertexAttrib> attribs = SyncList.newList();
	private final Shaders shaders;
	
	private int id = 0;
	private boolean bound = false, relink = true;
	
	public GLProgram()
	{
		this(new Shaders());
		
	}
	
	public GLProgram(IPopulator<GLProgram> pop)
	{
		this();
		
		pop.populate(this);
		
	}
	
	public GLProgram(Shader[] sh)
	{
		this();
		
		if (!ArrayHelper.isNullOrEmpty(sh))
		{
			for (Shader s : sh)
			{
				attachShader(s);
				
			}
			
		}
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public GLProgram(Shaders shs)
	{
		shaders = shs;
		
	}
	
	@Override
	public boolean isDirty()
	{
		return this.shaders.isDirty();
	}
	
	@Override
	public void setIsDirty(boolean b)
	{
		this.shaders.setIsDirty(b);
		
	}
	
	@Override
	public void delete(RenderContext rcon)
	{
		if (this.bound)
		{
			this.unbind(rcon);
			
		}
		
		this.shaders.delete(rcon);
		
		GL2.glDeleteProgram(this);
		
	}
	
	@Override
	public boolean bind(RenderContext rcon)
	{
		int bp = GL1.glGetInteger(GLConst.GL_CURRENT_PROGRAM);
		
		if (bp != 0)
		{
			return false;
		}
		
		if (this.id == 0)
		{
			this.id = GL2.glCreateProgram();
			
			try
			{
				RenderHelper.checkForGLError();
				
			}
			catch (GLException e)
			{
				GL2.glDeleteProgram(this.id);
				
				this.id = 0;
				
				throw e;
			}
			
			rcon.registerCleanable(this);
			
		}
		
		if ((this.relink || this.shaders.isDirty()) && !this.relink(rcon))
		{
			return false;
		}
		
		GL2.glUseProgram(this);
		
		this.bound = true;
		
		return true;
	}
	
	@Override
	public void unbind(RenderContext rcon)
	{
		if (!this.bound)
		{
			return;
		}
		
		GL2.glUseProgram(0);
		
		this.bound = false;
		
	}
	
	@Override
	public int hashCode()
	{
		return this.getId();
	}
	
	private boolean relink(RenderContext rcon)
	{
		if (!this.shaders.attachShaders(rcon, this))
		{
			return false;
		}
		
		if (this.attribs.isEmpty())
		{
			return false;
		}
		
		for (VertexAttrib pointer : this.attribs)
		{
			GL2.glVertexAttribPointer(this, pointer);
			
		}
		
		GL2.glLinkProgram(this);
		GL2.glValidateProgram(this);
		
		this.relink = false;
		this.shaders.setIsDirty(false);
		
		return true;
	}
	
	public boolean attachShader(Shader sh)
	{
		if (this.shaders.addShader(sh))
		{
			this.relink = true;
			return true;
		}
		
		return false;
	}
	
	public int attachShaders(Shaders shs)
	{
		int ret = 0;
		
		for (GLEnumShader st : GLEnumShader.values())
		{
			if (this.shaders.getShader(st) == null)
			{
				continue;
			}
			
			Shader s = shs.getShader(st);
			
			if (s != null && this.shaders.addShader(s))
			{
				ret++;
				
			}
			
		}
		
		return ret;
	}
	
	public void addVertexAttrib(String name, int size, int type, boolean normalized, int stride, long first)
	{
		this.addVertexAttrib(name, size, type, false, normalized, stride, first);
		
	}
	
	public void addVertexAttrib(String name, int size, int type, boolean unsigned, boolean normalized, int stride, long first)
	{
		if (!this.attribs.isEmpty())
		{
			for (VertexAttrib a : this.attribs)
			{
				if (a.name.equalsIgnoreCase(name))
				{
					return;
				}
				
			}
			
		}
		
		this.attribs.add(new VertexAttrib(name, size, type, unsigned, normalized, stride, first));
		this.relink = true;
		
	}
	
	public void attachUniform(RenderContext rcon, String name, float[] info, GLEnumUType type)
	{
		this.attachUniform(rcon, name, BufferHelper.makeFloatBuffer(info), type);
		
	}
	
	public void attachUniform(RenderContext rcon, String name, FloatBuffer info, GLEnumUType type)
	{
		if (!this.bound)
		{
			return;
		}
		
		int loc = GL2.glGetUniformLocation(this.id, name);
		
		if (loc != 0)
		{
			type.loadUniform(rcon, loc, info);
			
		}
		
	}
	
	public void attachUniform(RenderContext rcon, String name, int[] info, GLEnumUType type)
	{
		this.attachUniform(rcon, name, BufferHelper.makeIntBuffer(info), type);
		
	}
	
	public void attachUniform(RenderContext rcon, String name, IntBuffer info, GLEnumUType type)
	{
		if (!this.bound)
		{
			return;
		}
		
		int loc = GL2.glGetUniformLocation(this.id, name);
		
		if (loc != 0)
		{
			type.loadUniform(rcon, loc, info);
			
		}
		
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int shaderCount()
	{
		return this.shaders.getShaderCount();
	}
	
}
