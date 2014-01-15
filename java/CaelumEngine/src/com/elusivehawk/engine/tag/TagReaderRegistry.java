
package com.elusivehawk.engine.tag;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public final class TagReaderRegistry
{
	public static final byte BYTE_ID = 0x00;
	public static final byte SHORT_ID = 0x01;
	public static final byte INT_ID = 0x02;
	public static final byte LONG_ID = 0x03;
	public static final byte FLOAT_ID = 0x04;
	public static final byte DOUBLE_ID = 0x05;
	public static final byte STRING_ID = 0x06;
	public static final byte LIST_ID = 0x07;
	
	private static final TagReaderRegistry INSTANCE = new TagReaderRegistry();
	
	private final ITagReader<?>[] readers = new ITagReader<?>[256];
	
	private TagReaderRegistry()
	{
		this.register(BYTE_ID, new TagByte.ByteReader());
		this.register(SHORT_ID, new TagShort.ShortReader());
		this.register(INT_ID, new TagInt.IntReader());
		this.register(LONG_ID, new TagLong.LongReader());
		this.register(FLOAT_ID, new TagFloat.FloatReader());
		this.register(DOUBLE_ID, new TagDouble.DoubleReader());
		this.register(STRING_ID, new TagString.StringReader());
		this.register(LIST_ID, new TagList.ListReader());
		
	}
	
	public static TagReaderRegistry instance()
	{
		return INSTANCE;
	}
	
	public void register(byte id, ITagReader<?> reader)
	{
		if (this.getReader(id) != null)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.WARN, "Overriding tag reader ID #" + id);
			
		}
		
		this.readers[id] = reader;
		
	}
	
	public ITagReader<?> getReader(byte id)
	{
		return this.readers[id];
	}
	
	public ITag<?> readTag(DataInputStream in) throws IOException
	{
		String name = in.readUTF();
		
		byte id = in.readByte();
		ITagReader<?> r = this.getReader(id);
		
		if (r == null)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.ERROR, "Could not continue reading tag, invalid tag ID: " + id);
			
			return null;
		}
		
		return r.readTag(name, in);
	}
	
	public void writeTag(DataOutputStream out, ITag<?> tag) throws IOException
	{
		out.writeUTF(tag.getName());
		
		byte type = tag.getType();
		
		if (this.getReader(type) == null)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.WARN, "Tag " + tag.getName() + " has invalid type ID " + type + ", please rectify.");
			
		}
		
		out.writeByte(type);
		
		tag.save(out);
		
	}
	
}
