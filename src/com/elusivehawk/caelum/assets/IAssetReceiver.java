
package com.elusivehawk.caelum.assets;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
@FunctionalInterface
public interface IAssetReceiver
{
	void onAssetLoaded(IAsset a);
	
}
