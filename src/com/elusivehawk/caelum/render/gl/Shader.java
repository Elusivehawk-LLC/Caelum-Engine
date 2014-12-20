
package com.elusivehawk.caelum.render.gl;

import java.io.DataInputStream;
import com.elusivehawk.caelum.assets.Asset;
import com.elusivehawk.caelum.assets.EnumAssetType;
import com.elusivehawk.caelum.render.GraphicAsset;
import com.elusivehawk.caelum.render.RenderContext;
import com.elusivehawk.util.CompInfo;
import com.elusivehawk.util.Logger;
import com.elusivehawk.util.string.StringHelper;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class Shader extends GraphicAsset
{
	public final GLEnumShader gltype;
	
	protected String source = null;
	protected int glId = 0;
	
	@SuppressWarnings("unqualified-field-access")
	public Shader(String filepath, GLEnumShader type)
	{
		super(filepath, EnumAssetType.SHADER);
		
		gltype = type;
		
	}
	
	@Override
	public void delete(RenderContext rcon)
	{
		if (this.glId != 0)
		{
			GL2.glDeleteShader(this.glId);
			
			this.glId = 0;
			
		}
		
	}
	
	@Override
	public void initiate(RenderContext rcon)
	{
		if (this.loaded)
		{
			return;
		}
		
		if (this.glId == 0 && this.source != null)
		{
			int id = GL2.glCreateShader(this.gltype);
			
			if (id == 0)
			{
				throw new GLException("Cannot load shader: Out of shader IDs");
			}
			
			if (!GL2.glIsShader(id))
			{
				throw new GLException("Shader ID %s is not a proper shader object", id);
			}
			
			GL2.glShaderSource(id, this.source);
			GL2.glCompileShader(id);
			
			int status = GL2.glGetShaderi(id, GLEnumSStatus.GL_COMPILE_STATUS);
			
			if (status == GLConst.GL_FALSE)
			{
				if (CompInfo.DEBUG)
				{
					Logger.warn("Cannot compile shader \"%s\"", this.filepath);
					Logger.info("Shader log for shader \"%s\" (ID %s) of type %s: %s", this.filepath, id, this.gltype, GL2.glGetShaderInfoLog(id, GL2.glGetShaderi(id, GLEnumSStatus.GL_INFO_LOG_LENGTH)));
					
				}
				
				GL2.glDeleteShader(id);
				
			}
			else
			{
				synchronized (this)
				{
					this.glId = id;
					this.loaded = true;
					
				}
				
				Logger.verbose("Successfully compiled shader \"%s\"", this.filepath);
				
			}
			
		}
		
	}
	
	@Override
	protected boolean readAsset(DataInputStream in) throws Throwable
	{
		String src = StringHelper.readToOneLine(in);
		
		synchronized (this)
		{
			this.source = src;
			
		}
		
		return this.source != null;
	}
	
	@Override
	public void onExistingAssetFound(Asset a)
	{
		super.onExistingAssetFound(a);
		
		if (a instanceof Shader)
		{
			Shader s = (Shader)a;
			
			synchronized (this)
			{
				this.source = s.source;
				this.glId = s.glId;
				
			}
			
		}
		
	}
	
	public int getShaderId()
	{
		return this.glId;
	}
	
}
