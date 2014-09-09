
package com.elusivehawk.engine;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.elusivehawk.engine.assets.AssetManager;
import com.elusivehawk.engine.input.Input;
import com.elusivehawk.engine.render.DisplaySettings;
import com.elusivehawk.engine.render.IDisplay;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.engine.render.ThreadGameRender;
import com.elusivehawk.engine.render.old.IRenderHUB;
import com.elusivehawk.engine.render.old.RenderTask;
import com.elusivehawk.util.CompInfo;
import com.elusivehawk.util.EnumLogType;
import com.elusivehawk.util.EnumOS;
import com.elusivehawk.util.FileHelper;
import com.elusivehawk.util.IPausable;
import com.elusivehawk.util.IUpdatable;
import com.elusivehawk.util.Internal;
import com.elusivehawk.util.Logger;
import com.elusivehawk.util.ReflectionHelper;
import com.elusivehawk.util.ShutdownHelper;
import com.elusivehawk.util.StringHelper;
import com.elusivehawk.util.Version;
import com.elusivehawk.util.concurrent.IThreadStoppable;
import com.elusivehawk.util.json.EnumJsonType;
import com.elusivehawk.util.json.JsonData;
import com.elusivehawk.util.json.JsonObject;
import com.elusivehawk.util.json.JsonParseException;
import com.elusivehawk.util.json.JsonParser;
import com.elusivehawk.util.storage.DirtableStorage;
import com.elusivehawk.util.storage.Pair;
import com.elusivehawk.util.storage.Tuple;
import com.elusivehawk.util.task.TaskManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * The core class for the Caelum Engine.
 * 
 * @author Elusivehawk
 */
public final class CaelumEngine
{
	private static final CaelumEngine INSTANCE = new CaelumEngine();
	
	public static final Version VERSION = new Version(Version.ALPHA, 1, 0, 0, 0);
	
	private final Map<EnumEngineFeature, IThreadStoppable> threads = Maps.newEnumMap(EnumEngineFeature.class);
	private final List<Input> inputs = Lists.newArrayList();
	private final Map<String, String> startargs = Maps.newHashMap();
	private final TaskManager tasks = new TaskManager();
	private final List<String> startupPrefixes = Lists.newArrayList();
	
	private File nativeLocation = null;
	private IGameEnvironment env = null;
	private JsonObject envConfig = null;
	private IDisplay display = null;
	
	private GameFactory factory = null;
	private Game game = null;
	private GameArguments gameargs = null;
	private AssetManager assets = new AssetManager();
	private RenderContext rcon = null;
	
	private CaelumEngine()
	{
		if (CompInfo.OS != EnumOS.ANDROID)
		{
			Runtime.getRuntime().addShutdownHook(new Thread((() ->
			{
				CaelumEngine.instance().shutDownGame();
				CaelumEngine.instance().clearGameEnv();
				
			})));
			
		}
		
		this.startupPrefixes.add("env:");
		this.startupPrefixes.add("gamefac:");
		this.startupPrefixes.add("verbose:");
		
		Iterator<String> itr = this.startupPrefixes.iterator();
		String prefix;
		
		while (itr.hasNext())
		{
			prefix = itr.next();
			
			if (!prefix.endsWith(":"))
			{
				Logger.log().log(EnumLogType.WTF, "Prefix is missing colon: %s", prefix);
				itr.remove();
				
			}
			
		}
		
	}
	
	public static CaelumEngine instance()
	{
		return INSTANCE;
	}
	
	public static Game game()
	{
		return instance().game;
	}
	
	public static AssetManager assetManager()
	{
		return instance().assets;
	}
	
	public static IGameEnvironment environment()
	{
		return instance().env;
	}
	
	public static TaskManager tasks()
	{
		return instance().tasks;
	}
	
	public static GameArguments gameArgs()
	{
		return instance().gameargs;
	}
	
	public static IDisplay display()
	{
		return instance().display;
	}
	
	public static File getNativeLocation()
	{
		return instance().nativeLocation;
	}
	
	public static IContext getContext(boolean safe)
	{
		Thread t = Thread.currentThread();
		
		if (safe && !(t instanceof IThreadContext))
		{
			return null;
		}
		
		return ((IThreadContext)t).getContext();
	}
	
	public static boolean isPaused()
	{
		Thread t = Thread.currentThread();
		
		if (!(t instanceof IPausable))
		{
			return false;
		}
		
		return ((IPausable)t).isPaused();
	}
	
	//XXX Hooks
	
	public static void scheduleRenderTask(RenderTask rt)
	{
		instance().rcon.scheduleRTask(rt);
		
	}
	
	public static void addInputListener(Class<? extends Input> type, IUpdatable lis)
	{
		instance().inputs.forEach(((input) ->
		{
			if (type.isInstance(input))
			{
				input.addListener(lis);
				
			}
			
		}));
		
	}
	
	@Internal
	public static void waitForDisplay()
	{
		while (!display().isCreated())
		{
			try
			{
				Thread.sleep(2);//Would be 1, but there's potential for multiple threads to use this; Bad thread locking, bad!
				
			}
			catch (InterruptedException e)
			{
				Logger.log().err(e);
				
			}
			
		}
		
	}
	
	@Internal
	public static void flipScreen(boolean flip)
	{
		if (instance().rcon != null)
		{
			instance().rcon.onScreenFlipped(flip);
			
		}
		
		if (game() != null)
		{
			game().onScreenFlipped(flip);
			
		}
		
	}
	
	@Internal
	public static void main(String... args)
	{
		instance().createGameEnv(args);
		instance().startGame();
		instance().pauseGame(false);
		
	}
	
	/**
	 * 
	 * ONLY FOR DEBUGGING AND SMALL GAMES!
	 * 
	 * @param gamefac
	 * @param args
	 */
	public static void start(GameFactory gamefac, String... args)
	{
		if (instance().factory != null)
		{
			throw new CaelumException("Nuh uh uh, you didn't say the magic word...");
		}
		
		instance().factory = gamefac;
		
		main(args);
		
	}
	
	@Internal
	public void createGameEnv(String... args)
	{
		if (this.game != null)
		{
			return;
		}
		
		//XXX Parsing the starting arguments
		
		List<String> gargs = Lists.newArrayList();
		Map<String, String> strs = Maps.newHashMap();
		Pair<String> spl;
		
		for (String str : args)
		{
			boolean testForGameArg = true;
			
			for (String prefix : this.startupPrefixes)
			{
				if (str.startsWith(prefix))
				{
					spl = StringHelper.splitFirst(str, ":");
					strs.put(spl.one, spl.two);
					testForGameArg = false;
					
					break;
				}
				
			}
			
			if (testForGameArg)
			{
				gargs.add(str);
				
			}
			
		}
		
		this.startargs.putAll(strs);
		this.gameargs = new GameArguments(gargs);
		
		Logger.log().log(EnumLogType.INFO, "Starting Caelum Engine %s on %s.", VERSION, CompInfo.OS);
		
		boolean verbose = !"false".equalsIgnoreCase(this.startargs.get("verbose"));
		
		Logger.log().setEnableVerbosity(verbose);
		
		Logger.log().log(EnumLogType.INFO, "Verbosity is set to \'%s\'", verbose);
		
		/*if (DEBUG)
		{
			for (Entry<Object, Object> entry : System.getProperties().entrySet())
			{
				this.log.log(EnumLogType.DEBUG, "Argument: \"%s\": \'%s\'", entry.getKey(), entry.getValue());
				
			}
			
		}*/
		
		//XXX Load natives
		
		this.loadNatives();
		
		//XXX Loading game environment
		
		if (this.env == null)
		{
			IGameEnvironment env = null;
			Class<?> clazz = null;
			String cl = this.startargs.get("env");
			
			if (cl == null)
			{
				clazz = this.loadEnvironmentFromJson();
				
			}
			else
			{
				try
				{
					clazz = Class.forName(cl);
					
				}
				catch (Exception e){}
				
			}
			
			if (clazz == null)
			{
				Logger.log().log(EnumLogType.VERBOSE, "Loading default game environment.");
				
				try
				{
					switch (CompInfo.OS)
					{
						case WINDOWS:
						case MAC:
						case LINUX: clazz = Class.forName("com.elusivehawk.engine.lwjgl.LWJGLEnvironment"); break;
						case ANDROID: clazz = Class.forName("com.elusivehawk.engine.android.AndroidEnvironment"); break;
						default: Logger.log().log(EnumLogType.WTF, "Unsupported OS! Enum: %s; OS: %s", CompInfo.OS, System.getProperty("os.name"));
						
					}
					
				}
				catch (Exception e){}
				
			}
			else
			{
				Logger.log().log(EnumLogType.WARN, "Loading custom game environment, this is gonna suck...");
				
			}
			
			env = (IGameEnvironment)ReflectionHelper.newInstance(clazz, new Class[]{IGameEnvironment.class}, null);
			
			if (env == null)
			{
				Logger.log().log(EnumLogType.ERROR, "Unable to load environment: Instance couldn't be created. Class: %s", clazz == null ? "NULL" : clazz.getCanonicalName());
				ShutdownHelper.exit("NO-ENVIRONMENT-FOUND");
				
				return;
			}
			
			if (!env.isCompatible(CompInfo.OS))
			{
				Logger.log().log(EnumLogType.ERROR, "Unable to load environment: Current OS is incompatible. Class: %s; OS: %s", clazz == null ? "NULL" : clazz.getCanonicalName(), CompInfo.OS);
				ShutdownHelper.exit("NO-ENVIRONMENT-FOUND");
				
				return;
			}
			
			env.initiate(this.envConfig, args);
			
			this.env = env;
			
		}
		
		//XXX Loading input
		
		if (this.inputs.isEmpty())
		{
			List<Input> inputList = null;
			
			try
			{
				inputList = this.env.loadInputs();
				
			}
			catch (ClassFormatError e)
			{
				Logger.log().err(e);
				
			}
			finally
			{

				if (inputList == null || inputList.isEmpty())
				{
					Logger.log().log(EnumLogType.WARN, "Unable to load input");
					
				}
				else if (CompInfo.DEBUG)
				{
					for (Input input : inputList)
					{
						this.inputs.add(input);
						
						Logger.log().log(EnumLogType.DEBUG, "Input found: %s", input.getClass().getSimpleName());
						
					}
					
				}
				
			}
			
		}
		
		if (CompInfo.DEBUG)
		{
			this.factory = (() -> {return new TestGame();});
			
		}
		else if (this.factory == null)
		{
			String gamefac = this.startargs.get("gamefac");
			
			if (gamefac == null)
			{
				Logger.log().log(EnumLogType.ERROR, "Game factory not found: Factory not provided");
				ShutdownHelper.exit("NO-FACTORY-FOUND");
				
				return;
			}
			
			this.factory = (GameFactory)ReflectionHelper.newInstance(gamefac, new Class<?>[]{GameFactory.class}, null);
			
		}
		
	}
	
	@Internal
	public void startGame()
	{
		//XXX Creating the game itself
		
		if (this.factory == null)
		{
			Logger.log().log(EnumLogType.ERROR, "Game factory not found: Factory not provided");
			ShutdownHelper.exit("NO-FACTORY-FOUND");
			
			return;
		}
		
		Game g = this.factory.createGame();
		
		if (g == null)
		{
			Logger.log().log(EnumLogType.ERROR, "Could not load game");
			ShutdownHelper.exit("NO-GAME-FOUND");
			
			return;
		}
		
		this.tasks.start();
		
		g.preInit(this.gameargs);
		
		Logger.log().log(EnumLogType.INFO,"Loading %s", g);
		
		if (g.getGameVersion() == null)
		{
			Logger.log().log(EnumLogType.WARN, "The game is missing a Version object!");
			
		}
		
		this.game = g;
		
		//XXX Create display
		
		DisplaySettings settings = this.game.getDisplaySettings();
		
		if (settings == null)
		{
			settings = new DisplaySettings(this.game);
			
		}
		
		IDisplay d = this.env.createDisplay(settings);
		
		if (d == null)
		{
			Logger.log().log(EnumLogType.ERROR, "Display could not be created: Display is null");
			ShutdownHelper.exit("DISPLAY-NOT-MADE");
			
			return;
		}
		
		this.display = d;
		
		this.rcon = new RenderContext(this.env, d);
		
		this.rcon.setSettings(settings);
		
		if (this.assets == null)
		{
			this.assets = new AssetManager();
			
		}
		
		try
		{
			g.initiateGame(this.gameargs);
			
		}
		catch (Throwable e)
		{
			Logger.log().err("Game failed to load!", e);
			ShutdownHelper.exit("GAME-LOAD-FAILURE");
			
			return;
		}
		
		this.game.loadAssets(this.assets);
		this.assets.initiate();
		
		//XXX Creating game threads
		
		ThreadGameLoop gameloop = new ThreadGameLoop(this.inputs, this.game);
		
		this.threads.put(EnumEngineFeature.LOGIC, gameloop);
		
		IRenderHUB rhub = this.game.getRenderHUB();
		
		if (rhub != null)
		{
			this.rcon.setRenderHUB(rhub);
			Logger.log().log(EnumLogType.WARN, "Game %s is using the rendering HUB system!!", this.game);
			
		}
		
		IThreadStoppable rt = this.env.createRenderThread(this.rcon);
		
		if (rt == null)
		{
			rt = new ThreadGameRender(this.rcon);
			
		}
		
		this.threads.put(EnumEngineFeature.RENDER, rt);
		
		/*this.threads.put(EnumEngineFeature.SOUND, new ThreadSoundPlayer());
		
		IPhysicsSimulator ph = this.game.getPhysicsSimulator();
		
		if (ph != null)
		{
			this.threads.put(EnumEngineFeature.PHYSICS, new ThreadPhysics(ph, this.game.getUpdateCount()));
			
		}*/
		
		//XXX Starting game threads
		
		IThreadStoppable t;
		
		for (EnumEngineFeature fe : EnumEngineFeature.values())
		{
			t = this.threads.get(fe);
			
			if (t instanceof Thread)
			{
				if (((Thread)t).isAlive())
				{
					continue;
				}
				
				t.setPaused(true);
				((Thread)t).start();
				
			}
			else
			{
				this.threads.remove(fe);
				
			}
			
		}
		
	}
	
	public void pauseGame(boolean pause)
	{
		this.pauseGame(pause, EnumEngineFeature.values());
		
	}
	
	public void pauseGame(boolean pause, EnumEngineFeature... features)
	{
		for (EnumEngineFeature fe : features)
		{
			IThreadStoppable t = this.threads.get(fe);
			
			if (t != null)
			{
				t.setPaused(pause);
				
			}
			
		}
		
	}
	
	@Internal
	public void shutDownGame()
	{
		if (this.threads.isEmpty())
		{
			return;
		}
		
		this.rcon = null;
		
		if (this.game != null)
		{
			this.game.onShutdown();
			this.game = null;
			
		}
		
		for (EnumEngineFeature fe : EnumEngineFeature.values())
		{
			IThreadStoppable t = this.threads.get(fe);
			
			if (t != null)
			{
				t.stopThread();
				
			}
			
		}
		
		this.tasks.stop();
		this.threads.clear();
		
	}
	
	@Internal
	public void clearGameEnv()
	{
		if (this.game != null)
		{
			throw new CaelumException("YOU CLUMSY POOP!");
		}
		
		this.inputs.clear();
		this.startargs.clear();
		
		this.env = null;
		this.envConfig = null;
		this.display = null;
		
		this.gameargs = null;
		this.assets = null;
		
	}
	
	private void loadNatives()
	{
		if (this.nativeLocation != null)
		{
			return;
		}
		
		File tmp = FileHelper.createFile(CompInfo.TMP_DIR, String.format(".caelum/%s/natives/%s", VERSION.formatted, CompInfo.BUILT ? "" : "dev/"));
		
		if (!tmp.exists() && !tmp.mkdirs())
		{
			throw new CaelumException("Could not load natives: Unable to create natives directory");
		}
		
		if (CompInfo.BUILT)
		{
			FileHelper.readZip(CompInfo.JAR_DIR, ((zip, entry, name) ->
			{
				if (!FileHelper.NATIVE_NAME_FILTER.accept(null, entry.getName()))
				{
					return;
				}
				
				try
				{
					copyNative(zip.getInputStream(entry), entry.getSize(), new File(tmp, name.contains("/") ? StringHelper.getSuffix(name, "/") : name));
					
				}
				catch (Exception e)
				{
					Logger.log().err(e);
					
				}
				
			}));
			
		}
		else
		{
			File nLoc = FileHelper.getChild("lib", CompInfo.JAR_DIR.getParentFile());
			List<File> natives = FileHelper.getFiles(nLoc, FileHelper.NATIVE_FILTER);
			
			if (natives.isEmpty())
			{
				throw new CaelumException("Could not load natives! THIS IS A BUG! Native directory: %s", nLoc.getAbsolutePath());
			}
			
			for (File n : natives)
			{
				copyNative(FileHelper.createInStream(n), n.getTotalSpace(), new File(tmp, n.getName()));
				
			}
			
		}
		
		this.nativeLocation = tmp;
		
	}
	
	private static void copyNative(InputStream is, long space, File dest)
	{
		if (dest.exists() && dest.getTotalSpace() == space)
		{
			if (CompInfo.DEBUG)
			{
				Logger.log().log(EnumLogType.VERBOSE, "Not copying native: %s", dest.getName());
			}
			
			return;
		}
		
		if (FileHelper.copy(is, dest))
		{
			if (CompInfo.DEBUG)
			{
				Logger.log().log(EnumLogType.VERBOSE, "Succesfully copied native: %s", dest.getName());
				
			}
			
		}
		else
		{
			Logger.log().log(EnumLogType.WARN, "Could not copy native: %s", dest.getName());
			
		}
		
	}
	
	private Class<?> loadEnvironmentFromJson()
	{
		File jsonFile = FileHelper.createFile(".", "gameEnv.json");
		
		if (!FileHelper.isReal(jsonFile))
		{
			return null;
		}
		
		JsonData j = null;
		
		try
		{
			j = JsonParser.parse(jsonFile);
			
		}
		catch (JsonParseException e)
		{
			Logger.log().err(e);
			
		}
		
		if (j == null)
		{
			return null;
		}
		
		if (!(j instanceof JsonObject))
		{
			return null;
		}
		
		JsonObject json = (JsonObject)j;
		
		JsonData curEnv = json.getValue(CompInfo.OS.toString());
		
		if (curEnv == null || curEnv.type != EnumJsonType.OBJECT)
		{
			return null;
		}
		
		this.envConfig = (JsonObject)curEnv;
		JsonData envLoc = this.envConfig.getValue("lib");
		
		if (envLoc == null || envLoc.type != EnumJsonType.STRING)
		{
			return null;
		}
		
		File envLibFile = FileHelper.createFile(envLoc.value);
		
		if (!FileHelper.canRead(envLibFile) || !envLibFile.getName().endsWith(".jar"))
		{
			return null;
		}
		
		Tuple<ClassLoader, Set<Class<?>>> tuple = ReflectionHelper.loadLibrary(envLibFile);
		Set<Class<?>> set = tuple.two;
		
		if (set == null || set.isEmpty())
		{
			return null;
		}
		
		for (Class<?> c : set)
		{
			if (IGameEnvironment.class.isAssignableFrom(c))
			{
				return c;
			}
			
		}
		
		return null;
	}
	
}
