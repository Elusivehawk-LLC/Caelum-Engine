
package com.elusivehawk.engine;

import com.elusivehawk.engine.physics.IPhysicsSimulator;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.engine.render.RenderException;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class GameState extends AbstractGameComponent
{
	protected IPhysicsSimulator psim = null;
	
	public GameState(Game owner, String title)
	{
		super(owner, title);
		
		assert owner != null;
		
	}
	
	public GameState setPhysicsSim(IPhysicsSimulator sim)
	{
		this.psim = sim;
		
		return this;
	}
	
	@Override
	public void initiate(GameArguments args) throws Throwable{}
	
	@Override
	public void render(RenderContext rcon, double delta) throws RenderException{}
	
	@Override
	public void onShutdown(){}
	
	@Override
	public IPhysicsSimulator getPhysicsSimulator()
	{
		return this.psim;
	}
	
}
