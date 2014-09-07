
package com.elusivehawk.engine.prefab;

import com.elusivehawk.engine.assets.Asset;
import com.elusivehawk.engine.render.RenderTicket;
import com.elusivehawk.util.IPopulator;
import com.elusivehawk.util.math.IQuatListener;
import com.elusivehawk.util.math.IVectorListener;
import com.elusivehawk.util.math.Quaternion;
import com.elusivehawk.util.math.Vector;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class RenderTicketComponent extends RenderComponent implements IVectorListener, IQuatListener
{
	public RenderTicketComponent(ComponentGroup parent, RenderTicket tkt)
	{
		super(parent, tkt);
		
	}
	
	public RenderTicketComponent(ComponentGroup parent, RenderTicket tkt, IPopulator<Component> pop)
	{
		super(parent, tkt, pop);
		
	}
	
	@Override
	public void onQuatChanged(Quaternion q)
	{
		this.getRenderTicket().onQuatChanged(q);
		
	}
	
	@Override
	public void onVecChanged(Vector vec)
	{
		this.getRenderTicket().onVecChanged(vec);
		
	}
	
	@Override
	public void onAssetLoaded(Asset a)
	{
		this.getRenderTicket().onAssetLoaded(a);
		
		super.onAssetLoaded(a);
		
	}
	
	public RenderTicket getRenderTicket()
	{
		return (RenderTicket)this.renderable;
	}
	
}
