
package com.elusivehawk.engine.network;

import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;
import com.elusivehawk.engine.util.ThreadTimed;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
abstract class ThreadNetwork extends ThreadTimed
{
	protected final int updateCount;
	protected final IPacketHandler handler;
	protected final Connection connect;
	
	@SuppressWarnings("unqualified-field-access")
	ThreadNetwork(IPacketHandler h, Connection con, int ups)
	{
		assert con != null;
		assert h != null;
		assert ups > 0;
		
		handler = h;
		connect = con;
		updateCount = ups;
		
	}
	
	@Override
	public void handleException(Throwable e)
	{
		CaelumEngine.instance().getLog().log(EnumLogType.ERROR, null, e);
		
	}
	
	@Override
	public int getTargetUpdateCount()
	{
		return this.updateCount;
	}
	
	@Override
	public double getMaxDelta()
	{
		return 0.5;
	}
	
	@Override
	public void onThreadStopped()
	{
		try
		{
			this.update(0);
			
		}
		catch (Throwable e)
		{
			this.handleException(e);
			
		}
		
	}
	
}
