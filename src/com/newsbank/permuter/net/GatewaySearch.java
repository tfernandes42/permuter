package com.newsbank.permuter.net;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.log4j.Logger;

public class GatewaySearch {
	private static String hostName;
//	private static final Logger logger = LogManager.getLogger();
	private static final Logger logger = Logger.getLogger(GatewaySearch.class);
	public static String getHostName() 
	{
		return hostName;
	}

	public static void setHostName(String hostName) 
	{
		GatewaySearch.hostName = hostName;
	}
	
	//no host required (use default host)
	public String getURL(String info, String searchType) 
	{
		return getURL(info,searchType, getHostName());
	}
	
	public String getURL (String info, String searchType, String host) 
	{
		//make sure host starts with http://
		if(!host.startsWith("http://")) 
		{
			host = "http://" + host;
		}
		
		if(!host.contains(":")) 
		{
//			System.err.println("No port #");
			logger.warn("No port #");
		}
		
		//http://s072.newsbank.com:9090
		String [] stringArr = host.split(":");
		String part1 = stringArr[0];
		String part2 = stringArr[1];
		if(part2.contains("//")) {
			//			part2 = null;
			part2= part2.replace("//", "");
		}		
		
		int part3 = Integer.parseInt(stringArr[2].replace("/", ""));
		//int part3 = Integer.parseInt(stringArr[2]);

		String theUrl=null;
		URI theURI  = null;
		try 
		{
			if(searchType.contains("fed")) {
				theURI = new URI(part1, null, part2, part3, "/gateway2/platform/fedsearch6", URLDecoder.decode(info,"UTF-8"), null);
				//URI theURI = new URI("http", null,"s072.newsbank.com", 9090, "/gateway2/platform/fedsearch6", info, null);
			}
			else if(searchType.contains("unq")) {
				theURI = new URI(part1, null, part2, part3, "/gateway2/directsearch/MultiDocRequest", URLDecoder.decode(info, "UTF-8"), null);
			}
			else if(searchType.contains("doc")) {
				theURI = new URI(part1, null, part2, part3, "/gateway2/directsearch/DocumentSearch", URLDecoder.decode(info, "UTF-8"), null);
			}
			if (theURI != null) {
				theUrl=theURI.toASCIIString();
			}
		}
		catch (URISyntaxException use) 
		{
			logger.error(use);
		}
		catch (UnsupportedEncodingException uee) {
			logger.error(uee);
		}
		return theUrl;
	}
	
	public String getDocument(String URL) 
	{
		String theData = null;
		try 
		{
			 theData = HttpRequest.doGetRequest(URL);
		} catch (Throwable e) {
			logger.error("swallowing exception",e);
		}
		return theData; 
	}
	
	public String getDocument(String URL, String inBody)
		{
		String theData = null;
		
		try 
		{
			 theData = HttpRequest.doPostRequest(URL, inBody);
		} catch (Throwable e) {
			logger.error("swallowing exception",e);
		}
		return theData; 
		}
	}