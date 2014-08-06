
package com.elusivehawk.engine;

import com.elusivehawk.util.Internal;


/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
@Internal
public final class TestGameFactory extends GameFactory
{
	@Override
	public Game createGame()
	{
		return new TestGame();
	}
	
}
