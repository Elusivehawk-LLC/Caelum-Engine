
package com.elusivehawk.engine.core;

import java.util.List;
import com.elusivehawk.engine.assets.AssetManager;
import com.elusivehawk.engine.physics.IPhysicsSimulator;
import com.elusivehawk.engine.render.IRenderHUB;
import com.elusivehawk.engine.util.IUpdatable;
import com.google.common.collect.Lists;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
@SuppressWarnings({"static-method", "unused"})
public abstract class Game implements IUpdatable
{
	private GameState state = null, nextState = null;
	private List<IGameStateListener> listeners = Lists.newArrayList();
	private boolean initiated = false;
	
	public final boolean initiate(GameArguments args)
	{
		if (this.initiated)
		{
			return false;
		}
		
		if (!this.initiateGame(args))
		{
			return false;
		}
		
		this.initiated = true;
		
		if (this.nextState != null)
		{
			this.nextState.initiate();
			
			this.state = this.nextState;
			this.nextState = null;
			
		}
		
		return true;
	}
	
	public void loadAssets(AssetManager mgr){}
	
	public void onScreenFlipped(boolean flip){}
	
	public int getUpdateCount()
	{
		return 30;
	}
	
	@Override
	public final void update(double delta) throws Throwable
	{
		if (this.state == null)
		{
			this.updateGame(delta);
			
		}
		else
		{
			this.state.updateGameState(this, delta);
			
		}
		
		if (this.nextState != null)
		{
			if (this.state != null)
			{
				this.state.finish();
				
			}
			
			this.nextState.initiate();
			
			this.state = this.nextState;
			this.nextState = null;
			
			if (!this.listeners.isEmpty())
			{
				for (IGameStateListener gsl : this.listeners)
				{
					gsl.onGameStateSwitch(this.state);
					
				}
				
			}
			
		}
		
	}
	
	public final void onShutdown()
	{
		this.onGameShutdown();
		
		if (this.state != null)
		{
			this.state.finish();
			
		}
		
	}
	
	public void swapGameStates(GameState gs)
	{
		this.nextState = gs;
		
	}
	
	public void addGameStateListener(IGameStateListener gsl)
	{
		this.listeners.add(gsl);
		
	}
	
	protected abstract boolean initiateGame(GameArguments args);
	
	/**
	 * 
	 * Called if there is no current game state.
	 * 
	 * @param delta
	 */
	protected abstract void updateGame(double delta);
	
	protected abstract void onGameShutdown();
	
	/**
	 * 
	 * Called during startup.
	 * 
	 * @return The rendering HUB to be used to render the game.
	 */
	public IRenderHUB getRenderHUB()
	{
		return this.state == null ? null : this.state.getRenderHUB();
	}
	
	/**
	 * 
	 * Called during startup.
	 * 
	 * @return The physics simulator to use during the game's lifespan.
	 */
	@Deprecated
	public IPhysicsSimulator getPhysicsSimulator()
	{
		return this.state == null ? null : this.state.getPhysicsSimulator();
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName();
	}
	
}
