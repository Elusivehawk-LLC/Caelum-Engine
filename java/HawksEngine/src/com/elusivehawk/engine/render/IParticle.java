
package com.elusivehawk.engine.render;

import com.elusivehawk.engine.math.Vector3f;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public interface IParticle
{
	public void updateParticle();
	
	public Vector3f getPosition();
	
	public Color getColor();
	
	public boolean updatePositionOrColor();
	
	public boolean flaggedForRemoval();
	
}
