
package com.elusivehawk.engine.render.opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.engine.render.RenderHelper;
import com.elusivehawk.engine.render.RenderTicket;
import com.elusivehawk.engine.render.old.Model;

/**
 * 
 * A class to help work with OpenGL's program system.
 * 
 * @author Elusivehawk
 */
public class GLProgram implements IGLCleanable
{
	private final int id, vba;
	private final int sVertex, sFrag;
	private HashMap<VertexBufferObject, List<Integer>> vbos = new HashMap<VertexBufferObject, List<Integer>>();
	private boolean bound = false;
	
	private GLProgram(RenderContext context)
	{
		this(context, context.getDefaultVertexShader(), context.getDefaultFragmentShader());
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	private GLProgram(RenderContext context, int vertex, int frag)
	{
		id = context.getGL2().glCreateProgram();
		
		vba = context.getGL3().glGenVertexArrays();
		
		sVertex = vertex;
		sFrag = frag;
		
		context.getGL2().glAttachShader(id, vertex);
		context.getGL2().glAttachShader(id, frag);
		
		context.getGL2().glLinkProgram(this);
		context.getGL2().glValidateProgram(this);
		
		try
		{
			RenderHelper.checkForGLError(context);
			
		}
		catch (Exception e)
		{
			CaelumEngine.log().log(EnumLogType.ERROR, null, e);
			
		}
		
		context.registerCleanable(this);
		
	}
	
	public static GLProgram create(RenderContext context)
	{
		return new GLProgram(context);
	}
	
	public static GLProgram create(RenderContext context, int v, int f)
	{
		assert v != 0;
		assert f != 0;
		
		return new GLProgram(context, v, f);
	}
	
	public void attachVBO(VertexBufferObject vbo, List<Integer> attribs)
	{
		if (attribs == null || attribs.size() == 0)
		{
			this.vbos.put(vbo, null);
			
			return;
		}
		
		List<Integer> valid = new ArrayList<Integer>(attribs);
		
		for (Entry<VertexBufferObject, List<Integer>> entry : this.vbos.entrySet())
		{
			for (int a : attribs)
			{
				if (entry.getValue().contains(a))
				{
					valid.remove((Integer)a);
					
				}
				
			}
			
		}
		
		if (valid.isEmpty())
		{
			return;
		}
		
		this.vbos.put(vbo, valid);
		
	}
	
	public void attachModel(Model m)
	{
		this.attachVBO(m.finBuf.get(), Arrays.asList(0, 1, 2));
		this.attachVBO(m.indiceBuf.get(), null);
		
	}
	
	public void attachRenderTicket(RenderTicket tkt, RenderContext context)
	{
		this.attachModel(tkt.getModel());
		this.attachVBO(tkt.getExtraVBO(), Arrays.asList(3, 4, 5));
		
		if (!this.bound && !this.bind(context))
		{
			return;
		}
		
		context.getGL2().glVertexAttribPointer(3, 3, GLConst.GL_FLOAT, false, 0, tkt.getBuffer());
		context.getGL2().glVertexAttribPointer(4, 3, GLConst.GL_FLOAT, false, 3, tkt.getBuffer());
		context.getGL2().glVertexAttribPointer(5, 3, GLConst.GL_FLOAT, false, 6, tkt.getBuffer());
		
		this.unbind(context);
		
	}
	
	public void attachUniform(String name, FloatBuffer info, EnumUniformType type, RenderContext context)
	{
		if (!this.bound && !this.bind(context))
		{
			return;
		}
		
		int loc = context.getGL2().glGetUniformLocation(this.id, name);
		
		if (loc == 0)
		{
			CaelumEngine.log().log(EnumLogType.WARN, "You can't use the non-existent uniform: " + name);
			return;
		}
		
		type.loadUniform(loc, info, context);
		
	}
	
	public void attachUniform(String name, IntBuffer info, EnumUniformType type, RenderContext context)
	{
		if (!this.bound && !this.bind(context))
		{
			return;
		}
		
		int loc = context.getGL2().glGetUniformLocation(this.id, name);
		
		if (loc == 0)
		{
			CaelumEngine.log().log(EnumLogType.WARN, "You can't use the non-existent uniform: " + name);
			return;
		}
		
		type.loadUniform(loc, info, context);
		
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getShaderFrag()
	{
		return this.sFrag;
	}
	
	public int getShaderVertex()
	{
		return this.sVertex;
	}
	
	public boolean bind(RenderContext context)
	{
		if (!this.bind0(context))
		{
			this.unbind(context);
			
			return false;
		}
		
		return true;
	}
	
	private boolean bind0(RenderContext context)
	{
		if (this.bound)
		{
			return true;
		}
		
		if (context.getGL1().glGetInteger(GLConst.GL_CURRENT_PROGRAM) != 0)
		{
			return false;
		}
		
		context.getGL2().glUseProgram(this);
		
		context.getGL3().glBindVertexArray(this.vba);
		
		if (!this.vbos.isEmpty())
		{
			for (Entry<VertexBufferObject, List<Integer>> entry : this.vbos.entrySet())
			{
				context.getGL1().glBindBuffer(entry.getKey());
				
				if (entry.getValue() != null)
				{
					for (int attrib : entry.getValue())
					{
						context.getGL2().glEnableVertexAttribArray(attrib);
						
					}
					
				}
				
			}
			
		}
		
		this.bound = true;
		
		return true;
	}
	
	public void unbind(RenderContext context)
	{
		if (!this.bound)
		{
			return;
		}
		
		if (!this.vbos.isEmpty())
		{
			for (Entry<VertexBufferObject, List<Integer>> entry : this.vbos.entrySet())
			{
				if (entry.getValue() != null)
				{
					for (int a : entry.getValue())
					{
						context.getGL2().glDisableVertexAttribArray(a);
						
					}
					
				}
				
				context.getGL1().glBindBuffer(entry.getKey().t, 0);
				
			}
			
		}
		
		context.getGL3().glBindVertexArray(0);
		
		context.getGL2().glUseProgram(0);
		
		this.bound = false;
		
	}
	
	@Override
	public void glDelete(RenderContext context)
	{
		if (this.bound)
		{
			this.unbind(context);
			
		}
		
		context.getGL3().glDeleteVertexArrays(this.vba);
		
		context.getGL2().glDeleteShader(this.sVertex);
		context.getGL2().glDeleteShader(this.sFrag);
		
		context.getGL2().glDeleteProgram(this);
		
	}
	
	private static interface IUniformType
	{
		public void loadUniform(int loc, FloatBuffer buf, RenderContext context);
		
		public void loadUniform(int loc, IntBuffer buf, RenderContext context);
		
	}
	
	@Override
	public int hashCode()
	{
		return this.getId();
	}
	
	public static enum EnumUniformType implements IUniformType
	{
		ONE
		{
			@Override
			public void loadUniform(int loc, FloatBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform1f(loc, buf.get());
				
			}

			@Override
			public void loadUniform(int loc, IntBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform1i(loc, buf.get());
				
			}
			
		},
		TWO
		{
			@Override
			public void loadUniform(int loc, FloatBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform2f(loc, buf.get(), buf.get());
				
			}

			@Override
			public void loadUniform(int loc, IntBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform2i(loc, buf.get(), buf.get());
				
			}
			
		},
		THREE
		{
			@Override
			public void loadUniform(int loc, FloatBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform3f(loc, buf.get(), buf.get(), buf.get());
				
			}

			@Override
			public void loadUniform(int loc, IntBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform3i(loc, buf.get(), buf.get(), buf.get());
				
			}
			
		},
		FOUR
		{
			@Override
			public void loadUniform(int loc, FloatBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform4f(loc, buf.get(), buf.get(), buf.get(), buf.get());
				
			}

			@Override
			public void loadUniform(int loc, IntBuffer buf, RenderContext context)
			{
				context.getGL2().glUniform4i(loc, buf.get(), buf.get(), buf.get(), buf.get());
				
			}
			
		},
		M_TWO
		{
			@Override
			public void loadUniform(int loc, FloatBuffer buf, RenderContext context)
			{
				context.getGL2().glUniformMatrix2fv(loc, 1, false, buf);
				
			}

			@Override
			public void loadUniform(int loc, IntBuffer buf, RenderContext context){}
			
		},
		M_THREE
		{
			@Override
			public void loadUniform(int loc, FloatBuffer buf, RenderContext context)
			{
				context.getGL2().glUniformMatrix3fv(loc, 1, false, buf);
				
			}

			@Override
			public void loadUniform(int loc, IntBuffer buf, RenderContext context){}
			
		},
		M_FOUR
		{
			@Override
			public void loadUniform(int loc, FloatBuffer buf, RenderContext context)
			{
				context.getGL2().glUniformMatrix4fv(loc, 1, false, buf);
				
			}

			@Override
			public void loadUniform(int loc, IntBuffer buf, RenderContext context){}
			
		};
		
	}
	
}
