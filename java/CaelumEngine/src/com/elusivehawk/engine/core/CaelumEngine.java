
package com.elusivehawk.engine.core;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.elusivehawk.engine.assets.AssetManager;
import com.elusivehawk.engine.assets.ThreadAssetLoader;
import com.elusivehawk.engine.render.IRenderEnvironment;
import com.elusivehawk.engine.render.IRenderHUB;
import com.elusivehawk.engine.render.RenderContext;
import com.elusivehawk.engine.render.ThreadGameRender;
import com.elusivehawk.engine.util.FileHelper;
import com.elusivehawk.engine.util.ReflectionHelper;
import com.elusivehawk.engine.util.StringHelper;
import com.elusivehawk.engine.util.ThreadStoppable;
import com.elusivehawk.engine.util.Version;
import com.elusivehawk.engine.util.json.EnumJsonType;
import com.elusivehawk.engine.util.json.JsonObject;
import com.elusivehawk.engine.util.json.JsonParser;
import com.elusivehawk.engine.util.json.JsonValue;
import com.elusivehawk.engine.util.storage.Tuple;
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
	
	public static final boolean DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
	public static final Version VERSION = new Version(1, 0, 0);
	
	private final Map<EnumEngineFeature, ThreadStoppable> threads = Maps.newEnumMap(EnumEngineFeature.class);
	private final Map<EnumInputType, Input> inputs = Maps.newEnumMap(EnumInputType.class);
	
	private ILog log = new GameLog();
	private IGameEnvironment env = null;
	private IRenderEnvironment renv = null;
	private JsonObject envConfig = null;
	
	private Game game = null;
	private AssetManager assets = null;
	
	private CaelumEngine(){}
	
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
	
	public static ILog log()
	{
		return instance().log;
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
	
	public static RenderContext renderContext()
	{
		return renderContext(true);
	}
	
	public static RenderContext renderContext(boolean safe)
	{
		return (RenderContext)getContext(safe);
	}
	
	public static void flipScreen(boolean flip)
	{
		if (game() == null)
		{
			return;
		}
		
		((ThreadGameRender)instance().threads.get(EnumEngineFeature.RENDER)).flipScreen(flip);
		game().onScreenFlipped(flip);
		
	}
	
	public static void main(String... args)
	{
		instance().start(args);
		
	}
	
	private void start(String... args)
	{
		if (this.game != null)
		{
			return;
		}
		
		List<String> gameargs = Lists.newArrayList();
		Map<String, String> strs = Maps.newHashMap();
		String[] spl;
		
		for (String str : args)
		{
			if (str.indexOf(":") == -1)
			{
				gameargs.add(str);
				
			}
			else
			{
				spl = StringHelper.splitOnce(str, ":");
				strs.put(spl[0], spl[1]);
				
			}
			
		}
		
		this.log.log(EnumLogType.INFO, String.format("Starting Caelum Engine v%s on %s", VERSION, EnumOS.getCurrentOS()));
		
		boolean verbose = !"false".equalsIgnoreCase(strs.get("verbose"));
		
		this.log.setEnableVerbosity(verbose);
		
		if (DEBUG)
		{
			this.log.log(EnumLogType.WARN, "Debugging is turned on!");
			
		}
		
		IGameEnvironment env = null;
		Class<?> clazz = null;
		String cl = strs.get("env");
		
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
			this.log.log(EnumLogType.VERBOSE, "Loading default game environment");
			
			try
			{
				switch (EnumOS.getCurrentOS())
				{
					case WINDOWS:
					case MAC:
					case LINUX: clazz = Class.forName("com.elusivehawk.engine.lwjgl.LWJGLEnvironment"); break;
					case ANDROID: clazz = Class.forName("com.elusivehawk.engine.android.AndroidEnvironment"); break;
					default: this.log.log(EnumLogType.WTF, String.format("Unsupported OS! Enum: %s; OS: %s", EnumOS.getCurrentOS(), System.getProperty("os.name")));
					
				}
				
			}
			catch (Exception e){}
			
		}
		else
		{
			this.log.log(EnumLogType.WARN, "Loading custom game environment, this is gonna suck...");
			
		}
		
		env = (IGameEnvironment)ReflectionHelper.newInstance(clazz, new Class[]{IGameEnvironment.class}, null);
		
		if (env == null)
		{
			this.log.log(EnumLogType.ERROR, String.format("Unable to load environment: Instance couldn't be created. Class: %s", clazz == null ? "NULL" : clazz.getCanonicalName()));
			System.exit("NO-ENVIRONMENT-FOUND".hashCode());
			
		}
		
		if (!env.isCompatible(EnumOS.getCurrentOS()))
		{
			this.log.log(EnumLogType.ERROR, String.format("Unable to load environment: Current OS is incompatible. Class: %s; OS: %s", clazz == null ? "NULL" : clazz.getCanonicalName(), EnumOS.getCurrentOS()));
			System.exit("NO-ENVIRONMENT-FOUND".hashCode());
			
		}
		
		env.initiate(this.envConfig, args);
		
		this.env = env;
		this.renv = env.getRenderEnv();
		
		ILog l = this.env.getLog();
		
		if (l != null)
		{
			this.log = l;
			
			l.setEnableVerbosity(verbose);
			
		}
		
		List<Input> inputList = env.loadInputs();
		
		if (inputList == null || inputList.isEmpty())
		{
			this.log.log(EnumLogType.WARN, "Unable to load input");
			
		}
		else
		{
			for (Input input : inputList)
			{
				this.inputs.put(input.getType(), input);
				
				this.log.log(EnumLogType.VERBOSE, String.format("Loaded input of type %s, with class %s", input.getType().name(), input.getClass().getCanonicalName()));
				
			}
			
		}
		
		Game g = null;
		
		String game = strs.get("game");
		
		if (game != null)
		{
			g = (Game)ReflectionHelper.newInstance(game, new Class<?>[]{Game.class}, null);
			
		}
		
		if (g == null)
		{
			this.log.log(EnumLogType.ERROR, "Could not load game");
			System.exit("NO-GAME-FOUND".hashCode());
			
		}
		
		g.preInit();
		
		Version v = g.getGameVersion();
		
		this.log.log(EnumLogType.INFO, String.format("Loading %s, version %s", g.name, v == null ? "MISSINGNO" : v));
		
		if (v == null)
		{
			this.log.log(EnumLogType.WARN, "The game is missing a Version object!");
			
		}
		
		if (!g.initiate(new GameArguments(gameargs)))
		{
			return;
		}
		
		this.game = g;
		
		ThreadAssetLoader al = new ThreadAssetLoader();
		this.assets = new AssetManager(al);
		
		g.loadAssets(this.assets);
		this.assets.initiate();
		
		this.threads.put(EnumEngineFeature.ASSET_LOADING, al);
		this.threads.put(EnumEngineFeature.LOGIC, new ThreadGameLoop(this.inputs, this.game));
		
		IRenderHUB hub = this.game.getRenderHUB();
		
		if (hub != null)
		{
			this.threads.put(EnumEngineFeature.RENDER, new ThreadGameRender(this.renv, hub));
			
		}
		
		//this.threads.put(EnumEngineFeature.SOUND, new ThreadSoundPlayer());
		
		/*IPhysicsSimulator ph = this.game.getPhysicsSimulator();
		
		if (ph != null)
		{
			this.threads.put(EnumEngineFeature.PHYSICS, new ThreadPhysics(ph, this.game.getUpdateCount()));
			
		}*/
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@SuppressWarnings("synthetic-access")
			@Override
			public void run()
			{
				CaelumEngine.instance().shutDownGame();
				
			}
			
		});
		
		for (EnumEngineFeature fe : EnumEngineFeature.values())
		{
			ThreadStoppable t = this.threads.get(fe);
			
			if (t != null)
			{
				t.start();
				
			}
			
		}
		
	}
	
	private Class<?> loadEnvironmentFromJson()
	{
		JsonObject j = JsonParser.parse(FileHelper.createFile(".", "/game.json"));
		
		if (j == null)
		{
			return null;
		}
		
		JsonValue curEnv = j.getValue(EnumOS.getCurrentOS().toString());
		
		if (curEnv == null || curEnv.type != EnumJsonType.OBJECT)
		{
			return null;
		}
		
		this.envConfig = (JsonObject)curEnv;
		JsonValue envLoc = this.envConfig.getValue("lib");
		
		if (envLoc == null || envLoc.type != EnumJsonType.STRING)
		{
			return null;
		}
		
		File envLibFile = FileHelper.createFile(envLoc.value);
		
		if (!FileHelper.canReadFile(envLibFile) || !envLibFile.getName().endsWith(".jar"))
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
	
	private void shutDownGame()
	{
		if (this.game == null)
		{
			return;
		}
		
		for (EnumEngineFeature fe : EnumEngineFeature.values())
		{
			ThreadStoppable t = this.threads.get(fe);
			
			if (t != null)
			{
				t.stopThread();
				
			}
			
		}
		
		this.threads.clear();
		this.inputs.clear();
		
		this.game.onShutdown();
		this.game = null;
		
	}
	
	public void pauseGame(boolean pause)
	{
		this.pauseGame(pause, EnumEngineFeature.values());
		
	}
	
	public void pauseGame(boolean pause, EnumEngineFeature... features)
	{
		for (EnumEngineFeature fe : features)
		{
			ThreadStoppable t = this.threads.get(fe);
			
			if (t != null)
			{
				t.setPaused(pause);
				
			}
			
		}
		
	}
	
}
