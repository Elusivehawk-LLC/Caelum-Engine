
package com.elusivehawk.engine.meta;

import com.elusivehawk.util.StringHelper;

/**
 * 
 * Test log:
 * <p>
 * String index testing.<br>
 * Refactor helper.<br>
 * Tokenizer testing.<br>
 * RHINO benchmarking.<br>
 * Comparing vs. Instanceof check.<br>
 * Simple vs. Array List.<br>
 * Testing matrix stuff.<br>
 * Testing sound decoding.<br>
 * Testing file byte reading.<br>
 * Testing "++".<br>
 * Using PrintStream.<br>
 * More buffer testing.<br>
 * List iterating.<br>
 * Buffer.put(int, int) testing.<br>
 * Instanceof speed benchmarking.<br>
 * 
 * @author Elusivehawk
 */
public class BenchmarkTest
{
	public static final int TESTS = 128;
	
	public static void main(String[] args)
	{
		System.out.println("Beginning bench testing...");
		
		String test = "Test {$testing123}";
		
		System.out.println(StringHelper.substring(test, "{$", "}"));
		
		System.out.println("Th-th-th-th-That's all, folks!");
		
	}
	
}
