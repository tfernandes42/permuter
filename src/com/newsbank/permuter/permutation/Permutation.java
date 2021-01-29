package com.newsbank.permuter.permutation;

import java.util.Map;

import com.newsbank.permuter.PermutedResult;

public interface Permutation
	{
	public PermutedResult convert(String inData, String inFormat, Map <String,String> options) throws Throwable;
	}
