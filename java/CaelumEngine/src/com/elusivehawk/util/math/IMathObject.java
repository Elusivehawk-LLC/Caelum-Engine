
package com.elusivehawk.util.math;

import com.elusivehawk.util.IDirty;
import com.elusivehawk.util.storage.Buffer;
import com.elusivehawk.util.storage.IArray;
import com.elusivehawk.util.storage.IStorable;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public interface IMathObject<T extends Number> extends IDirty, IStorable<T>, IArray<T>
{
	public IMathObject<T> set(int pos, Number num, boolean notify);
	
	public IMathObject<T> add(IMathObject<T> obj, IMathObject<T> dest);
	
	public IMathObject<T> div(IMathObject<T> obj, IMathObject<T> dest);
	
	public IMathObject<T> mul(IMathObject<T> obj, IMathObject<T> dest);
	
	public IMathObject<T> sub(IMathObject<T> obj, IMathObject<T> dest);
	
	public IMathObject<T> normalize(IMathObject<T> dest);
	
	//Default methods
	
	@Override
	default void store(Buffer<T> buf)
	{
		for (int c = 0; c < this.size(); c++)
		{
			buf.add(this.get(c));
			
		}
		
	}
	
	@Override
	default IMathObject<T> set(int pos, T num)
	{
		return this.set(pos, num, true);
	}
	
	default IMathObject<T> setAll(T num)
	{
		for (int c = 0; c < this.size(); c++)
		{
			this.set(c, num, false);
			
		}
		
		this.onChanged();
		
		return this;
	}
	
	default IMathObject<T> normalize()
	{
		return this.normalize(this);
	}
	
	default Number[] multiget(int bitmask)
	{
		int count = 0;
		
		for (int bits : MathConst.BITMASKS)
		{
			if ((bitmask & bits) != 0)
			{
				count++;
				
			}
			
		}
		
		if (count == 0)
		{
			return new Float[0];
		}
		
		Number[] ret = new Number[count];
		count = 0;
		
		for (int c = 0; c < MathConst.BITMASKS.length; c++)
		{
			if ((bitmask & MathConst.BITMASKS[c]) != 0)
			{
				ret[count++] = this.get(c);
				
			}
			
		}
		
		return ret;
	}
	
	default IMathObject<T> set(IMathObject<T> obj)
	{
		int l = Math.min(this.size(), obj.size());
		
		for (int c = 0; c < l; c++)
		{
			this.set(c, obj.get(c), false);
			
		}
		
		this.onChanged();
		
		return this;
	}
	
	default IMathObject<T> add(IMathObject<T> obj)
	{
		return this.add(obj, this);
	}
	
	default IMathObject<T> div(IMathObject<T> obj)
	{
		return this.div(obj, this);
	}
	
	default IMathObject<T> mul(IMathObject<T> obj)
	{
		return this.mul(obj, this);
	}
	
	default IMathObject<T> sub(IMathObject<T> obj)
	{
		return this.sub(obj, this);
	}
	
	default IMathObject<T> cos()
	{
		return this.cos(this);
	}
	
	default IMathObject<T> cos(IMathObject<T> dest)
	{
		int i = Math.min(this.size(), dest.size());
	
		for (int c = 0; c < i; c++)
		{
			dest.set(c, Math.cos(this.get(c).doubleValue()), false);
			
		}
		
		dest.onChanged();
		
		return dest;
	}
	
	default IMathObject<T> sin()
	{
		return this.sin(this);
	}
	
	default IMathObject<T> sin(IMathObject<T> dest)
	{
		int i = Math.min(this.size(), dest.size());
		
		for (int c = 0; c < i; c++)
		{
			dest.set(c, Math.sin(this.get(c).doubleValue()), false);
			
		}
		
		dest.onChanged();
		
		return dest;
	}
	
	default void onChanged(){}
	
}
