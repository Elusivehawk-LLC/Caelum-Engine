
package com.elusivehawk.caelum.lwjgl;

import static com.elusivehawk.caelum.input.EnumMouseClick.DOWN;
import static com.elusivehawk.caelum.input.EnumMouseClick.DRAG;
import static com.elusivehawk.caelum.input.EnumMouseClick.LIFTED;
import static com.elusivehawk.caelum.input.EnumMouseClick.UP;
import com.elusivehawk.caelum.CaelumEngine;
import com.elusivehawk.caelum.CaelumException;
import com.elusivehawk.caelum.Display;
import com.elusivehawk.caelum.IDisplayImpl;
import com.elusivehawk.caelum.input.EnumMouseClick;
import com.elusivehawk.caelum.input.Mouse;
import com.elusivehawk.util.EnumLogType;
import com.elusivehawk.util.Logger;
import com.elusivehawk.util.math.Vector;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class LWJGLMouse extends com.elusivehawk.caelum.input.Mouse
{
	protected EnumMouseClick[]
			buttons = null,
			oldButtons = null;
	protected final Vector
			mousePos = new Vector(2),
			mousePosDelta = new Vector(2);
	
	protected float wheel = 0f;
	
	public LWJGLMouse(Display window)
	{
		super(window);
		
	}
	
	@Override
	public void close()
	{
		Mouse.destroy();
		
	}
	
	@Override
	public Vector getMousePos()
	{
		return this.mousePos;
	}
	
	@Override
	public Vector getMousePosDelta()
	{
		return this.mousePosDelta;
	}
	
	@Override
	public EnumMouseClick[] getClickStatus()
	{
		return this.buttons;
	}
	
	@Override
	public EnumMouseClick[] getOldClickStatus()
	{
		return this.oldButtons;
	}
	
	@Override
	public int getButtonCount()
	{
		return this.buttons.length;
	}
	
	@Override
	public float getWheelScroll()
	{
		return this.wheel;
	}
	
	@Override
	public void setGrabMouse(boolean grab)
	{
		Mouse.setGrabbed(grab);
		
	}
	
	@Override
	public boolean initiateInput()
	{
		super.initiateInput();
		
		try
		{
			Mouse.create();
			
		}
		catch (Exception e)
		{
			Logger.log().err(e);
			
			return false;
		}
		
		if (!Mouse.isCreated())
		{
			return false;
		}
		
		this.buttons = new EnumMouseClick[Mouse.getButtonCount()];
		this.oldButtons = new EnumMouseClick[Mouse.getButtonCount()];
		
		for (int b = 0; b < this.buttons.length; b++)
		{
			this.buttons[b] = EnumMouseClick.UP;
			this.oldButtons[b] = EnumMouseClick.UP;
			
		}
		
		return true;
	}
	
	@Override
	protected void pollInput(double delta)
	{
		if (!Mouse.isCreated())
		{
			try
			{
				Mouse.create();
				
			}
			catch (LWJGLException e)
			{
				e.printStackTrace();
				
			}
			
			if (!Mouse.isCreated())
			{
				throw new CaelumException("Cannot poll mouse: It wasn't created!");
			}
			
			Logger.log().log(EnumLogType.WARN, "Mouse recreated; Let's not do anything stupid again...");
			
		}
		
		IDisplayImpl display = CaelumEngine.defaultDisplay();
		int b;
		
		while (Mouse.next())
		{
			boolean upd = false;
			
			this.mousePos.set(((float)Mouse.getEventX() / (float)display.getWidth()), 1.0f - ((float)Mouse.getEventY() / (float)display.getHeight()));
			this.mousePosDelta.set(((float)Mouse.getEventDX() / (float)display.getWidth()), 1.0f - ((float)Mouse.getEventDY() / (float)display.getHeight()));
			this.wheel = (float)Mouse.getEventDWheel() / (float)display.getHeight();
			
			if (this.mousePos.isDirty() || this.mousePosDelta.isDirty())
			{
				upd = true;
				
			}
			
			b = Mouse.getEventButton();
			
			if (b != -1)
			{
				EnumMouseClick cur = this.buttons[b];
				
				if (Mouse.getEventButtonState())
				{
					if (!cur.isDown())
					{
						this.oldButtons[b] = cur;
						this.buttons[b] = DOWN;
						
					}
					else if (cur == DOWN && this.mousePos.isDirty())
					{
						this.oldButtons[b] = DOWN;
						this.buttons[b] = DRAG;
						
					}
					
				}
				else
				{
					if (cur.isDown())
					{
						this.oldButtons[b] = cur;
						this.buttons[b] = LIFTED;
						
					}
					else if (cur == LIFTED)
					{
						this.oldButtons[b] = LIFTED;
						this.buttons[b] = UP;
						
					}
					
				}
				
				upd = true;
				
			}
			
			if (upd)
			{
				this.sendUpdateToListeners(delta);
				
			}
			
		}
		
	}
	
	@Override
	protected void postUpdate()
	{
		this.mousePos.setIsDirty(false);
		this.mousePosDelta.setIsDirty(false);
		
	}
	
}
