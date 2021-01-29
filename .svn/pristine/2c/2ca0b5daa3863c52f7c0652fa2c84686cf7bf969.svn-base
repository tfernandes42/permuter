/*
 * Created on May 2, 2007
 * 
 * Copyright 2007 (c) NewsBank, Inc.
 * All Rights Reserved
 */
package com.newsbank.permuter.net;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;

public class HttpRequest {
	private static final int kMaxTotalConnection = 100;
	private static final int kMaxConnPerRoute = 100;

//	private static final Logger kLogger = LogManager.getLogger();
	private static final Logger kLogger = Logger.getLogger(HttpRequest.class);

	private static final PoolingHttpClientConnectionManager s_httpConnManager;
	private static final CloseableHttpClient s_httpClient;

	static {
		s_httpConnManager = new PoolingHttpClientConnectionManager();
		s_httpConnManager.setMaxTotal(kMaxTotalConnection);
		s_httpConnManager.setDefaultMaxPerRoute(kMaxConnPerRoute);

		s_httpClient = HttpClients.custom().setConnectionManager(s_httpConnManager).build();
	}

	private static CloseableHttpClient getClient() {
		return s_httpClient;
	}

	public static void shutdown() {
		s_httpConnManager.shutdown();
	}

	// create POST request map with all mappings of url component
	public static String doGetRequest(String inRequest) throws Throwable {
		Instant start = Instant.now();
		String result = null;

		HttpGet theGetRequest = new HttpGet(inRequest);
		CloseableHttpResponse theHttpResponse = null;
		try {
			theHttpResponse = getClient().execute(theGetRequest);
			StatusLine theStatusLine = theHttpResponse.getStatusLine();
			if (theStatusLine.getStatusCode() < 300) {
				HttpEntity theEntity = theHttpResponse.getEntity();
				if (theEntity != null) {
					result = EntityUtils.toString(theEntity, "UTF-8");
					EntityUtils.consumeQuietly(theEntity);
				} else
					kLogger.info("no entity returned for request");
			}
		}

		catch (Throwable theErr) {
			// fix; takeoutquotes from inRequest
			kLogger.error(theErr);
		}

		finally {
			if (theHttpResponse != null) {
				theHttpResponse.close();
			}
		}
		Instant end = Instant.now();
		long timeElapsed = Duration.between(start, end).toMillis();
//			kLogger.debug("elapsed time: {}", timeElapsed);
		kLogger.debug("elapsed time: " + timeElapsed);

		return result;
	}

	// body is assumed to already be encoded
	public static String doPostRequest(String inRequest, String inBody) throws Throwable {
		Instant start = Instant.now();
		String result = null;
		HttpPost thePostRequest = new HttpPost(inRequest);
		CloseableHttpResponse theResponse = null;
		try {
			thePostRequest.setEntity(new StringEntity(inBody, ContentType.APPLICATION_FORM_URLENCODED));

			// Execute and get the response
			theResponse = getClient().execute(thePostRequest);
			HttpEntity entity = theResponse.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
				EntityUtils.consumeQuietly(entity);
			} else
				kLogger.info("no entity returned for request");
		} catch (Throwable theErr) {
			// fix; takeoutquotes from inRequest
			kLogger.error(theErr);
		} finally {
			if (theResponse != null) {
				theResponse.close();
			}
		}
		Instant end = Instant.now();
		long timeElapsed = Duration.between(start, end).toMillis();
//			kLogger.debug("elapsed time: {}", timeElapsed);
		kLogger.debug("elapsed time: " + timeElapsed);

		return result;
	}
}
