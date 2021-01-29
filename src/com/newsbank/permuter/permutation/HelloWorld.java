package com.newsbank.permuter.permutation;

import java.util.Map;

import com.newsbank.permuter.PermutedResult;

public class HelloWorld implements Permutation
	{
	@Override
	public PermutedResult convert(String inData, String inFormat, Map <String,String> options)
		throws Throwable
		{
		return new PermutedResult("text/plain", "Hello World - the format is: " + inFormat + "\n\n" + inData);
		}
	}
