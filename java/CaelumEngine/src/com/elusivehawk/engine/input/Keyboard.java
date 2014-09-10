
package com.elusivehawk.engine.input;

import java.util.List;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public abstract class Keyboard extends Input
{
	public abstract boolean isKeyDown(Key key);
	
	public abstract List<Key> getPushedKeys();
	
	public abstract List<Key> getOldPushedKeys();
	
	public boolean useCapitals()
	{
		return this.isKeyDown(Key.SHIFT) || this.isKeyDown(Key.CAPS_LOCK);
	}
	
}
