
package com.elusivehawk.engine.tag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.elusivehawk.engine.core.CaelumEngine;
import com.elusivehawk.engine.core.EnumLogType;
import com.elusivehawk.engine.util.FileHelper;
import com.elusivehawk.engine.util.io.ByteStream;
import com.elusivehawk.engine.util.io.ByteWrapper;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class SaveFile implements ITagList
{
	protected File save;
	protected List<ITag<?>> tags = new ArrayList<ITag<?>>();
	
	public SaveFile(String path)
	{
		this(FileHelper.createFile(path));
		
	}
	
	public SaveFile(String src, String path)
	{
		this (FileHelper.createFile(src, path));
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public SaveFile(File file)
	{
		save = file;
		
	}
	
	public void load()
	{
		FileInputStream fis = FileHelper.createInStream(this.save);
		
		if (fis == null)
		{
			return;
		}
		
		BufferedInputStream in = new BufferedInputStream(fis);
		ByteWrapper wrap = new ByteStream(in);
		
		try
		{
			while (in.available() > 0)
			{
				ITag<?> tag = TagReaderRegistry.instance().readTag(wrap);
				
				if (tag == null)
				{
					continue;
				}
				
				this.tags.add(tag);
				
			}
			
		}
		catch (IOException e)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.ERROR, null, e);
			
		}
		
		try
		{
			in.close();
			
		}
		catch (Exception e)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.ERROR, null, e);
			
		}
		
	}
	
	public void save()
	{
		if (this.tags.isEmpty())
		{
			return;
		}
		
		FileOutputStream fos = FileHelper.createOutStream(this.save, true);
		
		if (fos == null)
		{
			return;
		}
		
		BufferedOutputStream out = new BufferedOutputStream(fos);
		
		try
		{
			byte[] b;
			
			for (ITag<?> tag : this.tags)
			{
				b = TagReaderRegistry.instance().writeTag(tag);
				
				if (b == null)
				{
					continue;
				}
				
				out.write(b, 0, b.length);
				
			}
			
		}
		catch (IOException e)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.ERROR, null, e);
			
		}
		
		try
		{
			out.flush();
			out.close();
			
		}
		catch (IOException e)
		{
			CaelumEngine.instance().getLog().log(EnumLogType.ERROR, null, e);
			
		}
		
	}
	
	@Override
	public void addTag(ITag<?> tag)
	{
		int i = -1;
		
		for (int c = 0; c < this.tags.size(); c++)
		{
			if (this.tags.get(c).getName() == tag.getName())
			{
				i = c;
				
				break;
			}
			
		}
		
		if (i == -1)
		{
			this.tags.add(tag);
			
		}
		else
		{
			this.tags.set(i, tag);
			
		}
		
	}
	
	@Override
	public ITag<?> getTag(String name)
	{
		if (this.tags.isEmpty())
		{
			return null;
		}
		
		for (ITag<?> tag : this.tags)
		{
			if (tag.getName() == name)
			{
				return tag;
			}
			
		}
		
		return null;
	}
	
}
