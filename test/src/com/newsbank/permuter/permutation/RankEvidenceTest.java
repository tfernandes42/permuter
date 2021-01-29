package com.newsbank.permuter.permutation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import com.newsbank.permuter.permutation.RankEvidence;

public class RankEvidenceTest {

	@Test
	public void testPermute() {
		File xmlFile = new File("jwRank.xml");
		String gateway = null;
		String docInfo = null;
		String queryInfo = null;
		
		if (xmlFile.exists())
		{
		try {
			char[] fileCharData = new char[(int) xmlFile.length()];
			FileReader theReader = new FileReader(xmlFile);
			theReader.read(fileCharData);
			theReader.close();
			
			docInfo = new String(fileCharData);
			
			SAXBuilder builder1 = new SAXBuilder();
			Document document1 = (Document) builder1.build(new StringReader(docInfo));
			Element rootNode = document1.getRootElement();
			
			//still getting it from the test file
			/*if(rootNode.getChildText("doc") != null) {
				docInfo = "<docs>"+rootNode.getChildText("doc")+"</docs>";
			}
			else if(rootNode.getChildText("id") != null) {
				docInfo = rootNode.getChildText("id");
			}*/
			queryInfo = rootNode.getChildText("query");
		}
		catch (Throwable e) {
			e.printStackTrace();
			}
		}

		//RankEvidence thePermuter = new RankEvidence();
		String output = RankEvidence.process(gateway, docInfo, queryInfo);
		
		//String output = result;
		
		System.out.println(output);

		assertNotNull(output);
		assertTrue(!output.isEmpty());
	}

}

