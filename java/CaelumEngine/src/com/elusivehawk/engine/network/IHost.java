
package com.elusivehawk.engine.network;

import java.io.Closeable;
import java.util.List;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public interface IHost extends IPacketHandler, Closeable
{
	public void beginCommunication();
	
	public void sendPackets(int client, Packet... pkts);
	
	public void onHandshakeEnd(boolean success, Connection connection, List<Packet> pkts);
	
}
