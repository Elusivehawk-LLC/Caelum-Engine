
package com.elusivehawk.engine.util;

/**
 * 
 * Stores an object, which if modified will set off the {@link #isDirty()} flag.
 * 
 * @author Elusivehawk
 */
public class DirtableStorage<T> implements IDirty
{
	protected T obj;
	protected boolean dirty = false, enableNull = true;
	
	@SuppressWarnings("unqualified-field-access")
	public DirtableStorage(T object)
	{
		obj = object;
		
	}
	
	public T get()
	{
		return this.obj;
	}
	
	public boolean set(T object)
	{
		if (object == null && !this.enableNull)
		{
			return false;
		}
		
		this.obj = object;
		
		this.setIsDirty(true);
		
		return true;
	}
	
	public DirtableStorage<T> setEnableNull(boolean b)
	{
		this.enableNull = b;
		
		return this;
	}
	
	@Override
	public boolean isDirty()
	{
		return this.dirty;
	}
	
	@Override
	public void setIsDirty(boolean b)
	{
		this.dirty = b;
		
	}
	
}
