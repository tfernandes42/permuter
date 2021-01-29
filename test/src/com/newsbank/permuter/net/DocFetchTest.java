package com.newsbank.permuter.net;

import static org.junit.Assert.*;

import org.junit.Test;

public class DocFetchTest {

	@Test
	public void testGetURL() {
		
		DocFetch x = new DocFetch();
		String y = x.getURL("obit/0F91B4291DA7508A", "http://s072.newsbank.com:9090/");
		assertNotNull(y);
		assertTrue(y.length() > 0);
		assertEquals(y, "http://s072.newsbank.com:9090/DocService/news/0F91B4291DA7508A");
			System.out.println(y);
	}
	
	@Test
	public void testGetDoc() {
		DocFetch x = new DocFetch();
		String y = x.getURL("obit/0F91B4291DA7508A", "http://s072.newsbank.com:9090/");

		String z = x.getDocument(y);
		
		assertNotNull(z);
		assertTrue(z.length() > 0);

		//System.out.println(z);
	}

}
