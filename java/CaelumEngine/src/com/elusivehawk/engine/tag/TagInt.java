
package com.elusivehawk.engine.tag;

import com.elusivehawk.engine.util.io.ByteReader;
import com.elusivehawk.engine.util.io.ByteWriter;
import com.elusivehawk.engine.util.io.Serializer;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class TagInt implements ITag<Integer>
{
	protected final String name;
	protected final int i;
	
	@SuppressWarnings("unqualified-field-access")
	public TagInt(String title, int in)
	{
		name = title;
		i = in;
		
	}
	
	@Override
	public byte getType()
	{
		return TagReaderRegistry.INT_ID;
	}
	
	@Override
	public Integer getData()
	{
		return this.i;
	}

	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public int save(ByteWriter w)
	{
		return Serializer.INTEGER.toBytes(w, this.i);
	}
	
	public static class IntReader implements ITagReader<Integer>
	{
		@Override
		public ITag<Integer> readTag(String name, ByteReader wrap)
		{
			return new TagInt(name, Serializer.INTEGER.fromBytes(wrap));
		}
		
	}
	
}
