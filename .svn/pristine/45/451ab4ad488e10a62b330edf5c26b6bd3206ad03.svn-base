package com.newsbank.permuter.util;

import java.nio.charset.Charset;
import java.util.Random;

public class StringUtil
	{
	private final static String	kDefaultCharSetName	=	"UTF-8";
	private final static Charset kDefaultCharSet = Charset.forName(kDefaultCharSetName);
	
	public final static String anonomize(String inString)
		{
		return StringUtil.anonomize(inString, kDefaultCharSet);
		}
	
	public final static String anonomize(String inString, String inCharsetName)
		{
		return StringUtil.anonomize(inString, Charset.forName(inCharsetName));
	
		}
	
	public final static String anonomize(String inString, Charset inCharset)
		{
		Random theRandom=new Random();
		
		byte[] theBytes=inString.getBytes(inCharset);
		byte[] theNewBytes = new byte[theBytes.length];
		
		int i=0;
		for (byte theByte : theBytes)
			{
			if (Character.isLetterOrDigit(theByte))
				{
				char theStart = Character.isUpperCase(theByte) ? 'A' : 'a';
				theByte =(byte)(theRandom.nextInt(26) + theStart);
				}
			
			theNewBytes[i++]=theByte;
			}
		
		return new String(theNewBytes);
		}
	}
