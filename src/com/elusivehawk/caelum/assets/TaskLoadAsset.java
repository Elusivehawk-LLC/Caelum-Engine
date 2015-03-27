
package com.elusivehawk.caelum.assets;

import com.elusivehawk.caelum.CaelumEngine;
import com.elusivehawk.util.Internal;
import com.elusivehawk.util.task.Task;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
@Internal
public class TaskLoadAsset extends Task
{
	protected final IAsset asset;
	
	protected IAsset fin = null;
	protected boolean duplicate = false;
	
	@SuppressWarnings("unqualified-field-access")
	public TaskLoadAsset(IAsset a)
	{
		super(null);
		
		asset = a;
		
	}
	
	@Override
	protected boolean finishTask() throws Throwable
	{
		CaelumEngine.assets().readAsset(this.asset);
		
		return true;
	}
	
	@Override
	public boolean doTryAgain()
	{
		return false;
	}
	
	public IAsset getCompletedIAsset()
	{
		return this.fin;
	}
	
	public boolean foundDuplicate()
	{
		return this.duplicate;
	}
	
}
