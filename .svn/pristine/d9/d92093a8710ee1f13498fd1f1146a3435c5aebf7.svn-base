package com.newsbank.permuter.net;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;

public class DocFetch {
//	private final static Logger logger = LogManager.getLogger();
	private final static Logger logger = Logger.getLogger(DocFetch.class);
	private static String hostName;
	
	public static String getHostName() {
		return hostName;
	}

	public static void setHostName(String hostName) {
		DocFetch.hostName = hostName;
	}
	
	public String getURL(String id) {
		return getURL(id, getHostName());
	}

	public String getURL (String ID, String host) {
	
//		String coll = null;
		if(ID.contains("/") ) {
			int i = ID.indexOf("/");
//			coll = ID.substring(0,i+1);
			ID = ID.substring(i+1);
		}
		
		//Make sure ID contains 16 digits
		if(ID.length() != 16) {
			logger.warn("Not an ID");
		}
//can't ever happen
//		if(host.substring(host.length()).equals("/") ) {
//			host = host.substring(0, host.length() -1 );
//		}
		
		//make sure host starts with http://
		if(!host.startsWith("http://")) {
			host = "http://" + host;
		}
		
		//make sure host ends with /
		//if(!host.endsWith("/")) {
		//	host += "/";
		//}
		
		if(!host.contains(":")) {
			logger.warn("No port #");
		}
		
		return host + "DocService/news/" + ID;
	}

	public String getDocument(String URL) {
		Instant start = Instant.now();
		String theData = null;
		
		try {
			 theData = HttpRequest.doGetRequest(URL);
		} catch (Throwable e) {
//			logger.warn("failed retrieving document: {}",URL, e);
			logger.warn("failed retrieving document: "+URL, e);
		}
		Instant end = Instant.now();
		long timeElapsed = Duration.between(start, end).toMillis();
//		logger.debug("elapsed time: {}", timeElapsed);
		logger.debug("elapsed time: "+ timeElapsed);
		return theData; 
	}
}