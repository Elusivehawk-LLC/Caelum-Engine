
package com.elusivehawk.engine.render.opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import com.elusivehawk.engine.assets.Asset;
import com.elusivehawk.engine.assets.IAssetReceiver;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.engine.render.RenderHelper;
import com.elusivehawk.engine.render.Shader;
import com.elusivehawk.engine.render.Shaders;
import com.elusivehawk.util.ArrayHelper;
import com.elusivehawk.util.IDirty;
import com.elusivehawk.util.IPopulator;
import com.elusivehawk.util.storage.SyncList;

/**
 * 
 * A class to help work with OpenGL's program system.
 * 
 * @author Elusivehawk
 */
public final class GLProgram implements IGLBindable, IAssetReceiver, IDirty
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
	public synchronized void onAssetLoaded(Asset a)
	{
		this.shaders.onAssetLoaded(a);
		
	}
	
	@Override
	public void delete(RenderContext rcon)
	{
		if (this.bound)
		{
			this.unbind(rcon);
			
		}
		
		IGL2 gl2 = rcon.getGL2();
		
		this.shaders.deleteShaders(rcon, this);
		
		gl2.glDeleteProgram(this);
		
	}
	
	@Override
	public boolean bind(RenderContext rcon)
	{
		int bp = rcon.getGL1().glGetInteger(GLConst.GL_CURRENT_PROGRAM);
		
		if (bp != 0)
		{
			return false;
		}
		
		if (this.id == 0)
		{
			this.id = rcon.getGL2().glCreateProgram();
			
			if (!this.relink(rcon))
			{
				return false;
			}
			
			rcon.registerCleanable(this);
			
			RenderHelper.checkForGLError(rcon);
			
		}
		
		if ((this.relink || this.shaders.isDirty()) && !this.relink(rcon))
		{
			return false;
		}
		
		rcon.getGL2().glUseProgram(this);
		
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
		
		rcon.getGL2().glUseProgram(0);
		
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
		
		IGL2 gl2 = rcon.getGL2();
		
		for (VertexAttrib pointer : this.attribs)
		{
			gl2.glVertexAttribPointer(this, pointer);
			
		}
		
		gl2.glLinkProgram(this);
		gl2.glValidateProgram(this);
		
		this.relink = false;
		this.shaders.setIsDirty(false);
		
		return true;
	}
	
	public synchronized boolean attachShader(Shader sh)
	{
		if (this.shaders.addShader(sh))
		{
			this.relink = true;
			return true;
		}
		
		return false;
	}
	
	public void addVertexAttrib(String name, int size, int type, boolean normalized, int stride, long first)
	{
		this.addVertexAttrib(name, size, type, false, normalized, stride, first);
		
	}
	
	public void addVertexAttrib(String name, int size, int type, boolean unsigned, boolean normalized, int stride, long first)
	{
		this.attribs.add(new VertexAttrib(name, size, type, unsigned, normalized, stride, first));
		
	}
	
	public void attachUniform(RenderContext rcon, String name, FloatBuffer info, EnumUniformType type)
	{
		if (!this.bound)
		{
			return;
		}
		
		int loc = rcon.getGL2().glGetUniformLocation(this.id, name);
		
		if (loc != 0)
		{
			type.loadUniform(rcon, loc, info);
			
		}
		
	}
	
	public void attachUniform(RenderContext rcon, String name, IntBuffer info, EnumUniformType type)
	{
		if (!this.bound)
		{
			return;
		}
		
		int loc = rcon.getGL2().glGetUniformLocation(this.id, name);
		
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
	
	private static interface IUniformType
	{
		public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf);
		
		public void loadUniform(RenderContext rcon, int loc, IntBuffer buf);
		
	}
	
	public static enum EnumUniformType implements IUniformType
	{
		ONE
		{
			@Override
			public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf)
			{
				rcon.getGL2().glUniform1f(loc, buf.get());
				
			}
			
			@Override
			public void loadUniform(RenderContext rcon, int loc, IntBuffer buf)
			{
				rcon.getGL2().glUniform1i(loc, buf.get());
				
			}
			
		},
		TWO
		{
			@Override
			public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf)
			{
				rcon.getGL2().glUniform2f(loc, buf.get(), buf.get());
				
			}
			
			@Override
			public void loadUniform(RenderContext rcon, int loc, IntBuffer buf)
			{
				rcon.getGL2().glUniform2i(loc, buf.get(), buf.get());
				
			}
			
		},
		THREE
		{
			@Override
			public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf)
			{
				rcon.getGL2().glUniform3f(loc, buf.get(), buf.get(), buf.get());
				
			}
			
			@Override
			public void loadUniform(RenderContext rcon, int loc, IntBuffer buf)
			{
				rcon.getGL2().glUniform3i(loc, buf.get(), buf.get(), buf.get());
				
			}
			
		},
		FOUR
		{
			@Override
			public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf)
			{
				rcon.getGL2().glUniform4f(loc, buf.get(), buf.get(), buf.get(), buf.get());
				
			}
			
			@Override
			public void loadUniform(RenderContext rcon, int loc, IntBuffer buf)
			{
				rcon.getGL2().glUniform4i(loc, buf.get(), buf.get(), buf.get(), buf.get());
				
			}
			
		},
		M_TWO
		{
			@Override
			public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf)
			{
				rcon.getGL2().glUniformMatrix2fv(loc, 1, false, buf);
				
			}
			
			@Override
			public void loadUniform(RenderContext rcon, int loc, IntBuffer buf){}
			
		},
		M_THREE
		{
			@Override
			public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf)
			{
				rcon.getGL2().glUniformMatrix3fv(loc, 1, false, buf);
				
			}
			
			@Override
			public void loadUniform(RenderContext rcon, int loc, IntBuffer buf){}
			
		},
		M_FOUR
		{
			@Override
			public void loadUniform(RenderContext rcon, int loc, FloatBuffer buf)
			{
				rcon.getGL2().glUniformMatrix4fv(loc, 1, false, buf);
				
			}
			
			@Override
			public void loadUniform(RenderContext rcon, int loc, IntBuffer buf){}
			
		};
		
	}
	
	public static class VertexAttrib
	{
		public final String name;
		public final int size, type, stride;
		public final boolean unsigned, normalized;
		public final long first;
		
		@SuppressWarnings("unqualified-field-access")
		public VertexAttrib(String attrib, int length, int t, boolean u, boolean n, int off, long f)
		{
			name = attrib;
			size = length;
			type = t;
			unsigned = u;
			normalized = n;
			stride = off;
			first = f;
			
		}
		
	}
	
}
