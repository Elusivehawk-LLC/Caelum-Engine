
package com.elusivehawk.engine.prefab;

import com.elusivehawk.engine.render.IRenderable;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.util.IPopulator;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class RenderComponent extends Component
{
	protected final IRenderable renderable;
	
	public RenderComponent(Component parent, IRenderable r)
	{
		this(parent, 0, r);
		
	}
	
	public RenderComponent(Component parent, IRenderable r, IPopulator<Component> pop)
	{
		this(parent, 0, r, pop);
		
	}
	
	public RenderComponent(Component parent, int p, IRenderable r, IPopulator<Component> pop)
	{
		this(parent, p, r);
		
		pop.populate(this);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public RenderComponent(Component parent, int p, IRenderable r)
	{
		super(parent, p);
		
		assert r != null;
		
		renderable = r;
		
	}
	
	@Override
	public void render(RenderContext rcon)
	{
		this.renderable.render(rcon);
		
		super.render(rcon);
		
	}
	
}
