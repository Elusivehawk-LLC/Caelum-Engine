
package com.elusivehawk.engine.util.io;

import com.elusivehawk.engine.util.Buffer;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class ByteBufWrapper implements IByteReader, IByteWriter
{
	protected final Buffer<Byte> in, out;
	
	public ByteBufWrapper(Buffer<Byte> buf)
	{
		this(buf, buf);
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public ByteBufWrapper(Buffer<Byte> i, Buffer<Byte> o)
	{
		in = i;
		out = o;
		
	}
	
	@Override
	public int remaining()
	{
		return this.in.remaining();
	}
	
	@Override
	public byte read()
	{
		return this.out.next();
	}
	
	@Override
	public byte[] readAll()
	{
		byte[] ret = new byte[this.remaining()];
		
		for (int c = 0; c < ret.length; c++)
		{
			ret[c] = this.read();
			
		}
		
		return ret;
	}
	
	@Override
	public void write(byte... bytes)
	{
		for (byte b : bytes)
		{
			this.in.add(b);
			
		}
		
	}
	
}
