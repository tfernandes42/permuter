package com.newsbank.permuter.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilTest
	{
	private static final String kTestString = "Matthew E. Axsom\n49 Prospect St.\nKeene, NH\n03431";
	
	@Test
	public void testAnonomize()
		{
		String theData=StringUtil.anonomize(kTestString);
		assertEquals(theData.length(), kTestString.length());
		assertNotEquals(theData, kTestString);
		}

	}
