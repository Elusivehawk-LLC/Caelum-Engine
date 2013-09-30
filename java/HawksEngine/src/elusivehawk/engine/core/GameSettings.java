
package elusivehawk.engine.core;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.DisplayMode;
import elusivehawk.engine.render.Color;
import elusivehawk.engine.render.EnumColorFormat;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class GameSettings
{
	public ByteBuffer[] icons = null;
	public int targetFPS = 0;
	public int targetUpdates = 60;
	public long fallbackDelay = 1000L;
	public String lwjglPath = Game.determineLWJGLPath();
	
	public String title = "Caelum Engine Game (Now with a streamlined Game class!)";
	public DisplayMode mode = new DisplayMode(800, 600);
	public boolean resize = false;
	public boolean fullscreen = false;
	public boolean vsync = false;
	public Color bg = new Color(EnumColorFormat.RGBA);
	public float gamma = 0;
	public float brightness = 0;
	public float constrast = 0;
	
}
