package com.newsbank.permuter.permutation;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import com.newsbank.permuter.types.Relations;

public class FormPermuterTest {

	@Test
	public void testPermute() {
		File xmlFile = new File("jwa4.xml");
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
		
		FormPermuter thePermuter = new FormPermuter();
		Relations r = thePermuter.process(fileData);
		
		thePermuter.permute(r);

		String output = thePermuter.permute(r).toString();
		
		System.out.println(output);
		//System.out.println(thePermuter.terms.getTerms());
		
		//System.out.println(thePermuter.r.getTerms().get("location"));
		assertNotNull(output);
		assertTrue(!output.isEmpty());
		//assertTrue(output.contains("&p_field_primary-0=alltext&p_params_primary-0=weight:1&p_text_primary-0="));
	}
}


