
package com.elusivehawk.engine.sound;

import java.io.File;
import java.io.FileInputStream;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import com.elusivehawk.engine.core.FileHelper;
import com.elusivehawk.engine.core.GameLog;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class SoundDecoderWav implements ISoundDecoder
{
	@Override
	public int decodeSound(File file)
	{
		WaveData wd = null;
		
		try
		{
			FileInputStream fis = FileHelper.createInStream(file);
			
			if (fis != null)
			{
				wd = WaveData.create(fis);
				
			}
			
		}
		catch (Exception e)
		{
			GameLog.error(e);
			
		}
		
		int ret = 0;
		
		if (wd != null)
		{
			ret = AL10.alGenBuffers();
			
			AL10.alBufferData(ret, wd.format, wd.data, wd.samplerate);
			
			wd.dispose();
			
		}
		
		return ret;
	}
	
}
