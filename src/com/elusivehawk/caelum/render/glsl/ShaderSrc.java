
package com.elusivehawk.caelum.render.glsl;

import com.elusivehawk.caelum.render.RenderContext;
import com.elusivehawk.caelum.render.RenderException;
import com.elusivehawk.caelum.render.RenderHelper;
import com.elusivehawk.caelum.render.gl.GL2;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class ShaderSrc implements IShader
{
	private final GLSLEnumShaderType type;
	private final String src;
	
	private int id = 0;
	private RenderContext boundRcon = null;
	private boolean compiled = false;
	
	public ShaderSrc(GLSLEnumShaderType stype, ShaderBuilder builder)
	{
		this(stype, builder.toString());
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public ShaderSrc(GLSLEnumShaderType stype, String source)
	{
		assert source != null && !source.equals("");
		
		type = stype;
		src = source;
		
	}

	@Override
	public void delete(RenderContext rcon)
	{
		GL2.glDeleteShader(this.id);
		
	}

	@Override
	public void dispose(Object... args)
	{
		if (!this.compiled)
		{
			return;
		}
		
		this.boundRcon.scheduleDeletion(this);
		
	}

	@Override
	public void compile(RenderContext rcon) throws RenderException
	{
		if (this.compiled)
		{
			throw new RenderException("Already compiled!");
		}
		
		int glid = RenderHelper.compileShader(this);
		
		rcon.registerDeletable(this);
		
		this.boundRcon = rcon;
		
		synchronized (this)
		{
			this.id = glid;
			this.compiled = true;
			
		}
		
	}

	@Override
	public boolean isCompiled()
	{
		return this.compiled;
	}

	@Override
	public String getSource()
	{
		return this.src;
	}

	@Override
	public int getShaderId()
	{
		return this.id;
	}

	@Override
	public GLSLEnumShaderType getType()
	{
		return this.type;
	}
	
}
