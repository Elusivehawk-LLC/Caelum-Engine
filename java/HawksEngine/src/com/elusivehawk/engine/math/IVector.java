
package com.elusivehawk.engine.math;

import java.nio.Buffer;
import com.elusivehawk.engine.util.IStoreable;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public interface IVector extends IStoreable
{
	public int getSize();
	
	public Buffer asBuffer();
	
	public float[] array();
	
}
