/*
 * Created on May 2, 2007
 * 
 * Copyright 2007 (c) NewsBank, Inc.
 * All Rights Reserved
 */
package com.newsbank.permuter.net;

import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

public class HttpRequestor implements Runnable {
	private String name;
	private String request;
	private String body;
	private String result;
	private Throwable throwable;
	private final static Logger logger = Logger.getLogger(HttpRequestor.class);

	/**
	 * @param request parameter for a GET request
	 */
	public HttpRequestor(String name, String request) {
		this(name, request, null);
	}

	/**
	 * @param request
	 * @param body    String representing body of a POST request. If null, request
	 *                will be a GET.
	 */
	public HttpRequestor(String name, String request, String body) {
		this.name = name;
		this.request = request;
		this.body = body;
	}

	public String getResult() throws Throwable {
		if (null != this.throwable) {
			throw throwable;
		}
		return result;
	}

	@Override
	public void run() {
		try {
			Instant start = Instant.now();
			if (null == body) {
				result = HttpRequest.doGetRequest(request);
			} else {
				result = HttpRequest.doPostRequest(request, body);
			}
			long elapsed = Duration.between(start, Instant.now()).toMillis();
//			logger.debug("{} request elapsed time: {}, result length: {}", this.name, elapsed, result.length());
			logger.debug(this.name+" request elapsed time: "+elapsed+ ", result length: "+ result.length());

		} catch (Throwable t) {
			this.throwable = t;
		}
	}
}
