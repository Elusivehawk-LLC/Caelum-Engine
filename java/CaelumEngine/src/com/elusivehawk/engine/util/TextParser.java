
package com.elusivehawk.engine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 
 * Helper class for simplifying the usage of {@link String}s.
 * <p>
 * In particular:<br>
 * Reading/writing text files<br>
 * Concatenation (With arguments for how the text is spliced together)<br>
 * Parsing the arguments of a given method (Helpful for method sorting)<br>
 * Removing the last instance of a given String<br>
 * Splitting a string once
 * 
 * @author Elusivehawk
 */
public final class TextParser
{
	private TextParser(){}
	
	public static List<String> read(String path)
	{
		return read(FileHelper.createFile(path));
	}
	
	public static List<String> read(File file)
	{
		List<String> text = new ArrayList<String>();
		FileReader fr = FileHelper.createReader(file);
		
		if (fr != null)
		{
			try
			{
				BufferedReader r = new BufferedReader(fr);
				
				for (String line = r.readLine(); line != null; line = r.readLine())
				{
					text.add(line);
					
				}
				
				r.close();
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
			}
			
		}
		
		return text;
	}
	
	public static boolean write(String path, boolean append, boolean makeFileIfNotFound, String... text)
	{
		return write(FileHelper.createFile(path), append, makeFileIfNotFound, text);
	}
	
	public static boolean write(String path, List<String> text, boolean append, boolean makeFileIfNotFound)
	{
		return write(FileHelper.createFile(path), text, append, makeFileIfNotFound);
	}
	
	public static boolean write(File file, boolean append, boolean makeFileIfNotFound, String... text)
	{
		return write(file, Arrays.asList(text), append, makeFileIfNotFound);
	}
	
	public static boolean write(File file, List<String> text, boolean append, boolean makeFileIfNotFound)
	{
		if (!file.exists() && makeFileIfNotFound)
		{
			try
			{
				file.createNewFile();
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
				return false;
			}
			
		}
		
		if (!file.canWrite())
		{
			System.err.println("File with path " + file.getPath() + " cannot be written to! This is a bug!");
			
			return false;
		}
		
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
			
			for (String line : text)
			{
				writer.write(line);
				writer.newLine();
				
			}
			
			writer.close();
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		
		return false;
	}
	
	public static String replaceLast(String str, String textToRemove, String replace)
	{
		int lastIn = str.lastIndexOf(textToRemove);
		
		if (lastIn == -1)
		{
			System.err.println("Failed to remove last instance of " + textToRemove + " from " + str + ".");
			
			return str;
		}
		
		StringBuilder b = new StringBuilder(str);
		b.replace(lastIn, lastIn + textToRemove.length(), replace);
		
		return b.toString();
	}
	
	public static String concat(String separator, String endWith, String d, String... strs)
	{
		return concat(Arrays.asList(strs), separator, endWith, d);
	}
	
	public static String concat(List<String> strs, String separator, String endWith, String d)
	{
		if (strs == null || strs.size() == 0)
		{
			return d;
		}
		
		StringBuilder b = new StringBuilder(strs.size() * 2);
		
		for (int c = 0; c < strs.size(); ++c)
		{
			b.append(strs.get(c));
			b.append(c + 1 == strs.size() ? endWith : separator);
			
		}
		
		return b.toString();
	}
	
	public static String parseArguments(Method m, boolean includeClassNames, boolean bracket)
	{
		StringBuilder ret = new StringBuilder();
		
		try
		{
			Class<?>[] argsC = m.getParameterTypes();
			
			if (bracket) ret.append("(");
			
			if (argsC.length > 0)
			{
				for (int c = 0; c < argsC.length; c++)
				{
					if (includeClassNames)
					{
						ret.append(argsC[c].getSimpleName());
						ret.append(" ");
						
					}
					
					ret.append("arg" + c);
					
					if (c != (argsC.length - 1))
					{
						ret.append(", ");
						
					}
					
				}
				
			}
			
			if (bracket) ret.append(")");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		
		return ret.toString();
	}
	
	public static String[] splitOnce(String str, String out)
	{
		if (str == null)
		{
			return null;
		}
		
		int ind = str.indexOf(out);
		
		if (ind == -1)
		{
			return new String[]{null, str};
		}
		
		String[] ret = new String[2];
		
		ret[0] = str.substring(0, ind - 1);
		ret[1] = str.substring(ind + 1, out.length());
		
		return null;
	}
	
	public static String parseDate(Calendar cal)
	{
		StringBuilder b = new StringBuilder(15);
		
		b.append(cal.get(Calendar.DATE));
		b.append("-");
		b.append(cal.get(Calendar.MONTH) + 1);
		b.append("-");
		b.append(cal.get(Calendar.YEAR));
		b.append(" ");
		int minute = cal.get(Calendar.MINUTE);
		b.append(cal.get(Calendar.HOUR));
		b.append(":");
		b.append((minute < 10 ? "0" : "") + minute);
		b.append(":");
		b.append(cal.get(Calendar.SECOND));
		b.append(":");
		b.append(cal.get(Calendar.MILLISECOND));
		b.append(" ");
		b.append(cal.get(Calendar.AM_PM) == Calendar.PM ? "PM" : "AM");
		
		return b.toString();
	}
	
}
