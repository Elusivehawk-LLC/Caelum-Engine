
package com.elusivehawk.caelum.assets;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public abstract class Asset implements IAsset
{
	private final String path;
	
	protected boolean read = false;
	
	@SuppressWarnings("unqualified-field-access")
	public Asset(String loc)
	{
		assert loc != null && !loc.isEmpty();
		
		path = loc;
		
	}
	
	@Override
	public String getLocation()
	{
		return this.path;
	}

	@Override
	public boolean isRead()
	{
		return this.read;
	}
	
}
