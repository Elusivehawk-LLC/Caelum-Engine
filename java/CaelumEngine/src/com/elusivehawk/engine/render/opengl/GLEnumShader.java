
package com.elusivehawk.engine.render.opengl;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public enum GLEnumShader
{
	VERTEX(GLConst.GL_VERTEX_SHADER),
	TESS(GLConst.GL_TESS_CONTROL_SHADER),
	EVAL(GLConst.GL_TESS_EVALUATION_SHADER),
	GEOM(GLConst.GL_GEOMETRY_SHADER),
	FRAG(GLConst.GL_FRAGMENT_SHADER),
	COMP(GLConst.GL_COMPUTE_SHADER);
	
	public final int gl;
	
	@SuppressWarnings("unqualified-field-access")
	GLEnumShader(int glenum)
	{
		gl = glenum;
		
	}
	
}
