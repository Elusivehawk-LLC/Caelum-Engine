
package com.elusivehawk.caelum.render;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class Icon
{
	private final float[] stats;
	private final float[][] corners;
	
	@SuppressWarnings("unqualified-field-access")
	public Icon(float x, float y, float w, float h)
	{
		stats = new float[]{x, y, w, h};
		corners = new float[][]{{x, y}, {w, y}, {x, h}, {w, h}};
		
	}
	
	public float[] getCorner(int c)
	{
		return this.corners[c];
	}
	
	public float[] getRawCornerInfo()
	{
		return this.stats;
	}
	
}
