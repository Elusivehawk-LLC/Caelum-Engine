
package com.elusivehawk.engine.render;

import java.util.List;
import com.elusivehawk.engine.math.Vector3f;

/**
 * 
 * Implement this to all entities in your game world.
 * 
 * @author Elusivehawk
 */
public interface IModelGroup
{
	public String getName();
	
	public List<Model> getModels();
	
	public Vector3f getPosition(int index);
	
	public Vector3f getRotation(int index);
	
	public Vector3f getScale(int index);
	
	public ITexture getTexture(int index);
	
	public GLProgram getProgram(int index);
	
	public boolean updateBeforeRendering(int index);
	
}
