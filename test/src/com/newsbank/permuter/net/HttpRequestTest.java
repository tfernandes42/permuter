package com.newsbank.permuter.net;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpRequestTest
	{
	private static final String kGetRequestTestUrl = "http://google.com";
	private static final String kReplyTestString = "Search the world's information";
	
	@Test
	public void testDoGetRequestString()
		{
		try
			{
			String theData = HttpRequest.doGetRequest(kGetRequestTestUrl);
			assertNotNull(theData);
			assertTrue(theData.length() > 0);
			assertTrue(theData.contains(kReplyTestString));
			}
		
		catch (Throwable e)
			{
			e.printStackTrace();
			}
		
		}

	}
