package com.newsbank.permuter.permutation;

import java.util.Map;

import com.newsbank.permuter.PermutedResult;
import com.newsbank.permuter.types.Relations;

public abstract class AbstractPermutation implements Permutation {

	abstract public Relations process(String fileData);
	abstract public String permute(Relations r);

	@Override
	public PermutedResult convert(String inData, String inFormat, Map <String,String> options) throws Throwable {
		Relations r = process(inData);
		String tripletData = this.permute(r);
		
		return new PermutedResult("text/plain", tripletData);
	}
}