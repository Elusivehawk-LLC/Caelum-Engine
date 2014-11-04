
package com.elusivehawk.caelum;

import java.io.IOException;
import com.elusivehawk.caelum.assets.AssetManager;
import com.elusivehawk.util.network.Client;
import com.elusivehawk.util.network.ConnectionType;
import com.elusivehawk.util.network.IHost;
import com.elusivehawk.util.network.INetworkMaster;
import com.elusivehawk.util.network.Server;
import com.elusivehawk.util.network.Side;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public abstract class NetworkGame extends Game implements INetworkMaster
{
	protected final Side side;
	protected final int port;
	
	protected IHost host = null;
	
	@SuppressWarnings("unqualified-field-access")
	protected NetworkGame(String title, Side s, int gameport)
	{
		super(title);
		
		if (s == null)
		{
			throw new NullPointerException("Side cannot be null!");
		}
		
		side = s;
		port = gameport;
		
	}
	
	public abstract int getMaxPlayerCount(GameArguments args);
	
	@Override
	protected void preInit(GameArguments args)
	{
		switch (this.side)
		{
			case CLIENT: this.host = new Client(this);
			case SERVER: this.host = new Server(this, this.port, this.getMaxPlayerCount(args));
		}
		
	}
	
	@Override
	public void initiateGame(GameArguments args, AssetManager assets) throws Throwable
	{
		if (this.side != Side.SERVER)
		{
			this.host.connect(ConnectionType.TCP, this.port);
			
		}
		
		this.host.beginComm();
		
	}
	
	@Override
	public void update(double delta) throws Throwable
	{
		if (this.host != null)
		{
			this.host.receivePackets();
			
		}
		
		super.update(delta);
		
	}
	
	@Override
	protected void onGameShutdown()
	{
		if (this.host != null)
		{
			try
			{
				this.host.close();
				
			}
			catch (IOException e){}
			
		}
		
	}
	
}
