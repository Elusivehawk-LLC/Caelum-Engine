
package com.elusivehawk.engine.assets;

import java.io.File;
import com.elusivehawk.engine.CaelumEngine;
import com.elusivehawk.util.FileHelper;
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
	protected final String assetLoc;
	protected final IAssetReceiver receiver;
	
	protected Asset fin = null;
	
	public TaskLoadAsset(String loc)
	{
		this(loc, null);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public TaskLoadAsset(String loc, IAssetReceiver r)
	{
		super((t) ->
		{
			Asset a = ((TaskLoadAsset)t).getCompleteAsset();
			boolean read = r != null;
			
			if (read)
			{
				read = r.onAssetLoaded(a);
				
			}
			
			CaelumEngine.assetManager().onAssetRead(a, read);
			
		});
		
		assert loc != null && !loc.isEmpty();
		
		assetLoc = loc;
		receiver = r;
		
	}
	
	@Override
	protected boolean finishTask() throws Throwable
	{
		AssetManager mgr = CaelumEngine.assetManager();
		
		if (mgr == null)
		{
			throw new NullPointerException("Asset manager not found! Aborting!!");
		}
		
		Asset a = mgr.getExistingAsset(this.assetLoc);
		
		if (a != null)
		{
			this.fin = a;
			
			return true;
		}
		
		File file = mgr.findFile(this.assetLoc);
		
		if (!FileHelper.canReadFile(file))
		{
			return false;
		}
		
		IAssetReader r = mgr.getReader(file);
		
		if (r == null)
		{
			return false;
		}
		
		a = r.readAsset(mgr, file);
		
		if (a == null)
		{
			return false;
		}
		
		this.fin = a;
		
		return true;
	}
	
	public Asset getCompleteAsset()
	{
		return this.fin;
	}
	
}
