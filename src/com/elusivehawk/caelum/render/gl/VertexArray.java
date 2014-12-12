
package com.elusivehawk.caelum.render.gl;

import java.util.List;
import com.elusivehawk.caelum.render.RenderContext;
import com.elusivehawk.util.IPopulator;
import com.google.common.collect.Lists;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class VertexArray implements IGLBindable
{
	private final List<VertexBuffer> vbos = Lists.newArrayList();
	private final List<Integer> attribs = Lists.newArrayList();
	
	private int id = 0;
	private boolean initiated = false;
	
	public VertexArray(){}
	
	public VertexArray(IPopulator<VertexArray> pop)
	{
		pop.populate(this);
		
	}
	
	@Override
	public void delete(RenderContext rcon)
	{
		if (!this.initiated)
		{
			return;
		}
		
		if (this.isBound(rcon))
		{
			this.unbind(rcon);
			
		}
		
		GL3.glDeleteVertexArray(this.id);
		
	}
	
	@Override
	public boolean bind(RenderContext rcon)
	{
		int old = GL1.glGetInteger(GLConst.GL_VERTEX_ARRAY_BINDING);
		
		if (old != 0)
		{
			return false;
		}
		
		if (this.vbos.isEmpty())
		{
			return false;
		}
		
		if (this.id == 0)
		{
			this.id = GL3.glGenVertexArray();
			rcon.registerCleanable(this);
			
		}
		
		GL3.glBindVertexArray(this);
		
		if (!this.initiated)
		{
			this.vbos.forEach(((vb) ->
			{
				vb.bind(rcon);
				
				vb.getAttribs().forEach(((attrib) ->
				{
					if (!this.attribs.contains(attrib.index))
					{
						GL2.glVertexAttribPointer(attrib);
						
						this.attribs.add(attrib.index);
						
					}
					
				}));
				
				vb.unbind(rcon);
				
			}));
			
			this.initiated = true;
			
		}
		
		GL2.glEnableVertexAttribArrays(this.attribs);
		
		return true;
	}
	
	@Override
	public void unbind(RenderContext rcon)
	{
		if (!this.isBound(rcon))
		{
			return;
		}
		
		GL2.glDisableVertexAttribArrays(this.attribs);
		
		GL3.glBindVertexArray(null);
		
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void addVBO(VertexBuffer vbo)
	{
		this.vbos.add(vbo);
		
	}
	
	public boolean isBound(RenderContext rcon)
	{
		int i = GL1.glGetInteger(GLConst.GL_VERTEX_ARRAY_BINDING);
		
		return i != 0 && i == this.id;
	}
	
}
