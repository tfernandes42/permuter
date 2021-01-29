package com.newsbank.permuter.permutation;

import java.util.Map;

import com.newsbank.permuter.PermutedResult;

public class ThrowError implements Permutation
	{

	@Override
	public PermutedResult convert(String inData, String inFormat, Map <String,String> options)
			throws Throwable
		{
		throw new Exception("An Exception for format: " + inFormat);		
		}

	}
