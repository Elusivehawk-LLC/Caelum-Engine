
package com.elusivehawk.engine;

import java.util.List;
import com.elusivehawk.engine.assets.AssetManager;
import com.elusivehawk.engine.physics.IPhysicsSimulator;
import com.elusivehawk.engine.render.IRenderable;
import com.elusivehawk.util.IUpdatable;
import com.google.common.collect.Lists;

/**
 * 
 * An abstract class that is meant to unify some {@link Game} and {@link GameState} classes.
 * 
 * @author Elusivehawk
 * 
 * @see Game
 * @see GameArguments
 * @see GameState
 * @see IUpdatable
 */
public abstract class AbstractGameComponent implements IUpdatable, IRenderable
{
	private final AbstractGameComponent master;
	
	protected final String name;
	protected final List<IUpdatable> modules = Lists.newArrayList();
	
	protected AbstractGameComponent(String title)
	{
		this(null, title);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	protected AbstractGameComponent(AbstractGameComponent owner, String title)
	{
		master = owner;
		name = title;
		
	}
	
	@Override
	public void update(double delta, Object... extra) throws Throwable
	{
		this.updateModules(delta, extra);
		
	}
	
	@Override
	public String toString()
	{
		if (this.master == null)
		{
			return this.getFormattedName();
		}
		
		StringBuilder b = new StringBuilder(this.modules.size() * 2 + 2);
		
		b.append(this.name);
		b.append('{');
		
		for (int c = 0; c < this.modules.size(); c++)
		{
			b.append(this.modules.get(c));
			
			if (c < (this.modules.size() - 1))
			{
				b.append(", ");
				
			}
			
		}
		
		b.append('}');
		
		return String.format("%s.%s", this.master.master == null ? this.master.name : this.master.toString(), b.toString());
	}
	
	//XXX Module things
	
	protected final void updateModules(double delta, Object... extra) throws Throwable
	{
		for (IUpdatable m : this.modules)
		{
			m.update(delta, extra);
			
		}
		
	}
	
	public synchronized void addModule(IUpdatable m)
	{
		if (m instanceof Thread)
		{
			throw new CaelumException("Threads aren't modules. Silly Buttons..."/*[sic]*/);
		}
		
		this.modules.add(m);
		
	}
	
	public synchronized void removeModule(IUpdatable m)
	{
		this.modules.remove(m);
		
	}
	
	public String getFormattedName()
	{
		return this.name;
	}
	
	public void onScreenFlipped(boolean flip){}
	
	public abstract void initiate(GameArguments args, AssetManager assets) throws Throwable;
	
	public abstract void onShutdown();
	
	public abstract IPhysicsSimulator getPhysicsSimulator();
	
}
