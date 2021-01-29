package com.newsbank.permuter.permutation;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import com.newsbank.permuter.net.DocFetch;
import com.newsbank.permuter.types.Relations;


public class FSObitPermuterTest {

	@Test
	public void testPermute() {
		File xmlFile = new File("fsObitMale4.xml");
		String fileData = null;
		
		if (xmlFile.exists())
		{
		try {
			char[] fileCharData = new char[(int) xmlFile.length()];
			FileReader theReader = new FileReader(xmlFile);
			theReader.read(fileCharData);
			theReader.close();
			fileData = new String(fileCharData);
			}
		catch (Throwable e) {
			e.printStackTrace();
			}
		}
		
		FSObitPermuter thePermuter = new FSObitPermuter();
		Relations r = thePermuter.process(fileData);
		
		thePermuter.permute(r);

		String output = thePermuter.permute(r).toString();
		
		System.out.println(output);
		
		assertNotNull(output);
		assertTrue(!output.isEmpty());
		assertTrue(output.contains("&p_field_primary-0=alltext&p_params_primary-0=weight:1&p_text_primary-0="));
	}
	
	@Test
	public void testFetch() {

		String ID = "obit/0FBAE9762AD539AE";
		String hostName = "http://s072.newsbank.com:9090/";
		
		DocFetch docFetch = new DocFetch();
		String url = docFetch.getURL(ID, hostName);
		
		String content = docFetch.getDocument(url);
		FSObitPermuter thePermuter = new FSObitPermuter();
		Relations r=thePermuter.process(content);
		thePermuter.permute(r);
		
		String output = thePermuter.permute(r).toString();
		//System.out.println(thePermuter.triplets.toString());
		
		assertNotNull(output);
		assertTrue(!output.isEmpty());
		assertTrue(output.contains("&p_field_primary-0=alltext&p_params_primary-0=weight:1&p_text_primary-0="));
	}
}
