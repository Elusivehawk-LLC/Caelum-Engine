
package elusivehawk.engine.task;

import java.io.File;
import java.util.List;
import elusivehawk.engine.util.TextParser;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class TaskRead extends Task<List<String>>
{
	protected final File txt;
	protected boolean read = false;
	
	public TaskRead(int idNo, ITaskReceiver<List<String>> handler, File file)
	{
		super(idNo, handler);
		
		txt = file;
		
	}
	
	@Override
	public boolean isTaskFinished()
	{
		return this.read;
	}
	
	@Override
	protected List<String> tryTask()
	{
		List<String> ret = TextParser.read(txt);
		this.read = ret != null;
		
		return ret;
	}
	
}
