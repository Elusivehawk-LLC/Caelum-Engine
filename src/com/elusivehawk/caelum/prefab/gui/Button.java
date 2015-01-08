
package com.elusivehawk.caelum.prefab.gui;

import com.elusivehawk.caelum.Display;
import com.elusivehawk.caelum.input.InputConst;
import com.elusivehawk.caelum.prefab.Rectangle;
import com.elusivehawk.caelum.render.Canvas;
import com.elusivehawk.caelum.render.Icon;
import com.elusivehawk.util.math.Vector;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class Button implements IGuiComponent
{
	private final Rectangle bounds;
	
	private final Icon[] icons = new Icon[Gui.STATE_COUNT];
	private final IButtonListener[] clickers = new IButtonListener[InputConst.MOUSE_BUTTONS];
	
	private Object attachment = null;
	private boolean active = true;
	private int img = -1, lastState = -1;
	
	public Button(float x, float y, float z, float w)
	{
		this(new Rectangle(x, y, z, w));
		
	}
	
	@SuppressWarnings("unqualified-field-access")
	public Button(Rectangle r)
	{
		assert r != null;
		
		bounds = r;
		
	}
	
	@Override
	public void drawComponent(Canvas canvas, int state)
	{
		if (this.img == -1)
		{
			this.img = canvas.drawImage(this.bounds, this.icons[state]);
			
		}
		else if (state != this.lastState)
		{
			canvas.redrawImage(this.img, this.bounds, this.icons[state]);
			
		}
		
		this.lastState = state;
		
	}
	
	@Override
	public void onClicked(Display display, int button)
	{
		IButtonListener lis = this.clickers[button];
		
		if (lis != null)
		{
			lis.onButtonClicked(display, this);
			
		}
		
	}
	
	@Override
	public void onDragged(Vector deltaPos){}
	
	@Override
	public Rectangle getBounds()
	{
		return this.bounds;
	}
	
	@Override
	public boolean isActive()
	{
		return this.active;
	}
	
	public Object getAttachment()
	{
		return this.attachment;
	}
	
	public Button setLeftClick(IButtonListener lis)
	{
		return this.setClick(InputConst.MOUSE_LEFT, lis);
	}
	
	public Button setRightClick(IButtonListener lis)
	{
		return this.setClick(InputConst.MOUSE_RIGHT, lis);
	}
	
	public Button setClick(int button, IButtonListener lis)
	{
		assert lis != null;
		
		this.clickers[button] = lis;
		
		return this;
	}
	
	public Button setIcon(int state, Icon icon)
	{
		this.icons[state] = icon;
		
		return this;
	}
	
	public Button setAttachment(Object obj)
	{
		if (obj != this.attachment)
		{
			synchronized (this)
			{
				this.attachment = obj;
				
			}
			
		}
		
		return this;
	}
	
	public synchronized Button setActive(boolean a)
	{
		this.active = a;
		
		return this;
	}
	
}
