
package com.elusivehawk.engine.core;

import java.util.List;
import com.elusivehawk.engine.assets.AssetManager;
import com.elusivehawk.engine.physics.IPhysicsSimulator;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.engine.render.RenderHelper;
import com.elusivehawk.engine.render.old.IRenderHUB;
import com.elusivehawk.util.IPausable;
import com.elusivehawk.util.Version;
import com.google.common.collect.Lists;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
@SuppressWarnings({"static-method", "unused"})
public abstract class Game extends AbstractGameComponent implements IPausable
{
	private final List<IGameStateListener> listeners = Lists.newArrayList();
	
	private GameState state = null, nextState = null;
	private boolean initiated = false, paused = false;
	
	protected Game(String title)
	{
		super(title);
		
	}
	
	@Override
	public final void update(double delta) throws GameTickException
	{
		try
		{
			if (this.state == null)
			{
				this.tick(delta);
				
			}
			else
			{
				this.state.update(delta);
				
			}
			
		}
		catch (Throwable e)
		{
			throw new GameTickException(e);
		}
		finally
		{
			if (this.nextState != null)
			{
				if (this.state != null)
				{
					this.state.onShutdown();
					
				}
				
				this.state = this.nextState;
				this.nextState = null;
				
				try
				{
					this.state.initiate(CaelumEngine.gameArgs());
					
				}
				catch (Throwable e)
				{
					CaelumEngine.log().err("Error caught during game state initiation:", new GameTickException(e));
					
				}
				
				this.state.loadAssets(CaelumEngine.assetManager());
				
				if (!this.listeners.isEmpty())
				{
					for (IGameStateListener gsl : this.listeners)
					{
						gsl.onGameStateSwitch(this.state);
						
					}
					
				}
				
			}
			
		}
		
	}
	
	@Override
	public boolean isPaused()
	{
		return this.paused;
	}
	
	@Override
	public void setPaused(boolean p)
	{
		this.paused = p;
		
	}
	
	@Override
	public String getFormattedName()
	{
		return this.getGameVersion() == null ? this.name : String.format("%s %s", this.name, this.getGameVersion());
	}
	
	@Override
	public void loadAssets(AssetManager mgr)
	{
		if (this.nextState != null)
		{
			this.nextState.loadAssets(mgr);
			
		}
		
	}
	
	//XXX Optional/technical methods
	
	protected void preInit(GameArguments args){}
	
	@Override
	public final void initiate(GameArguments args) throws Throwable
	{
		this.initiateGame(args);
		
		if (this.nextState != null)
		{
			this.nextState.initiate(args);
			
			this.state = this.nextState;
			this.nextState = null;
			
		}
		
	}
	
	public int getUpdateCount()
	{
		return 30;
	}
	
	@Override
	public final void onShutdown()
	{
		this.onGameShutdown();
		
		if (this.state != null)
		{
			this.state.onShutdown();
			
		}
		
	}
	
	//XXX Game state stuff
	
	public void setGameState(GameState gs)
	{
		this.nextState = gs;
		
	}
	
	public void addGameStateListener(IGameStateListener gsl)
	{
		this.listeners.add(gsl);
		
	}
	
	/**
	 * 
	 * Called on every update, if there is no current game state.
	 * 
	 * @param delta
	 * @throws Throwable
	 */
	protected void tick(double delta) throws Throwable
	{
		this.updateModules(delta);
		
	}
	
	//XXX Abstract methods
	
	protected abstract void initiateGame(GameArguments args) throws Throwable;
	
	public abstract Version getGameVersion();
	
	protected abstract void onGameShutdown();
	
	//XXX Getters
	
	/**
	 * 
	 * Called during startup.
	 * 
	 * @return The rendering HUB to be used to render the game.
	 */
	@Override
	@Deprecated
	public IRenderHUB getRenderHUB()
	{
		return this.state == null ? null : this.state.getRenderHUB();
	}
	
	/**
	 * 
	 * NOTICE: THIS IS NOT THREAD SAFE!<br>
	 * I mean it people, sync your entities and crap!
	 * 
	 * @param rcon
	 * @param delta
	 * 
	 * @see RenderHelper
	 */
	@Override
	public void render(RenderContext rcon, double delta)
	{
		if (this.state != null)
		{
			this.state.render(rcon, delta);
			
		}
		
	}
	
	/**
	 * 
	 * Called during startup.
	 * 
	 * @return The physics simulator to use during the game's lifespan.
	 */
	@Override
	public IPhysicsSimulator getPhysicsSimulator()
	{
		return this.state == null ? null : this.state.getPhysicsSimulator();
	}
	
}
