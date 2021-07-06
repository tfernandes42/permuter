package com.newsbank.permuter.server;

import java.io.IOException;
import java.io.OutputStream;

//from http://www.simpleframework.org/doc/tutorial/tutorial.php

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

//import org.apache.logging.log4j.LogManager;
import com.newsbank.logwrapper.Logger;

import org.apache.logging.log4j.ThreadContext;
//import org.apache.logging.log4j.ThreadContext;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.newsbank.permuter.PermutedResult;
import com.newsbank.permuter.net.DocFetch;
import com.newsbank.permuter.net.GatewaySearch;
import com.newsbank.permuter.net.HttpRequest;
import com.newsbank.permuter.permutation.Permutation;
import com.newsbank.permuter.types.LoggingFields;
import com.newsbank.permuter.util.CommandLineHelper;
import com.newsbank.permuter.util.SystemUtils;

public class SimpleHandler implements Container {
//	private final static Logger logger = LogManager.getLogger();
	private final static Logger logger = Logger.getLogger(SimpleHandler.class);
	private final static String kDefaultFormat = "txt";
	private final static String kPermutationClassPathStub = "com.newsbank.permuter.permutation.";
	private int port;
	private static Connection m_connection;
	private String hostName;
	private long pid;
	
	//for docFetch
	public String getHostServer() {
		//return hostServer;
		return DocFetch.getHostName();
	}

	public SimpleHandler(String hostServer, int port) throws Exception {
		setHostServer(hostServer);
		this.port = port;
		ContainerSocketProcessor socketProcessor = new ContainerSocketProcessor(this);
		m_connection = new SocketConnection(socketProcessor);
		SocketAddress theAddress = new InetSocketAddress(port);
		m_connection.connect(theAddress);
		InetAddress ip = InetAddress.getLocalHost();
        this.hostName = ip.getHostName();
        this.pid = SystemUtils.getPID();
//        ThreadContext.put("handler", this.hostName+":"+this.port);
//        ThreadContext.put("pid", ""+this.pid);
        
		logger.info("SimpleHandler started");
	}

	public void setHostServer(String hostServer) {
		DocFetch.setHostName(hostServer);
		GatewaySearch.setHostName(hostServer);
	}

	@Override
	public void handle(Request inRequest, Response inResponse) {
		Instant start = Instant.now();
//        ThreadContext.put("id", UUID.randomUUID().toString());
//        ThreadContext.put("serviceTime","0");
//        ThreadContext.put("handler", this.hostName+":"+this.port);
//        ThreadContext.put("pid", ""+this.pid);
		try {
			OutputStream theOutputBody = inResponse.getOutputStream();
			long time = System.currentTimeMillis();

			inResponse.setValue("Server", "PermutationServer/0.1 (Simple 6.01)");
			inResponse.setDate("Date", time);
			inResponse.setDate("Last-Modified", time);

			Path thePath = inRequest.getPath();
			String theName = thePath.getName();
			String theFormat = thePath.getExtension();

			if (theFormat != null) {
				int theIndex = theName.indexOf("." + theFormat);
				theName = theName.substring(0, theIndex);
			} else {
				theFormat = kDefaultFormat;
			}

			String content;
			inRequest.getQuery();
			
			try {
				if (inRequest.getParameter("ID") == null) {
					//call the request
					content = inRequest.getContent();
				}
				//else get ID & Host 
				else {
					String ID = inRequest.getParameter("ID");
					//String personID = inRequest.getParameter("person");
					String hostName = this.getHostServer();

					//do similar thing for GatewaySearch
					DocFetch docFetch = new DocFetch();
					String url = docFetch.getURL(ID, hostName);
					content = docFetch.getDocument(url);
				}
				Permutation thePermutation = (Permutation) ClassLoader.getSystemClassLoader().loadClass(kPermutationClassPathStub + theName).newInstance();
//		        ThreadContext.put("permuter", theName);
				PermutedResult theResult;
				//added try catch for more clear error messages (add error log)
				String theData = null;
				try {
					theResult = thePermutation.convert(content, theFormat, inRequest.getQuery());
					theData = theResult.getData();
					
					inResponse.setValue("Content-Type", theResult.getContentType());
				} catch (Exception thErr) {
					logger.error(thErr.getMessage(), thErr);
					inResponse.setStatus(Status.INTERNAL_SERVER_ERROR);
					inResponse.setValue("Error", "Issue with Fedsearch response or convert method");
				}
				Charset ch = StandardCharsets.UTF_8;
				byte[] outputData = null;
				if (theData != null) {
					outputData = theData.getBytes(ch);
				}
				//added this try catch block to give a more clear output on the issue at hand
				if (outputData != null) {
					try {
						theOutputBody.write(outputData, 0, outputData.length);
					} catch (Throwable thErr) {
						inResponse.setStatus(Status.INTERNAL_SERVER_ERROR);
						inResponse.setValue("Error", "Issue with gateway call / empty output");
					}
				} else {
					inResponse.setStatus(Status.INTERNAL_SERVER_ERROR);
					inResponse.setValue("Error", "Issue with gateway call / NULL output");
				}
			} catch (Throwable theErr) {
				logger.error(theErr);
				inResponse.setStatus(Status.INTERNAL_SERVER_ERROR);
				PrintStream ps = new PrintStream(theOutputBody);
				inResponse.setValue("Content-Type", "text/plain");
				ps.println("Class: " + theName);
				ps.println("Format: " + theFormat);
				theErr.printStackTrace(ps);
			}
			theOutputBody.close();
		} catch (Throwable theErr) {
//			logger.fatal("Error processing the request: {}", inRequest.getPath(), theErr);
			logger.fatal("Error processing the request: "+ inRequest.getPath(), theErr);
		}
		
		long timer = System.currentTimeMillis();
		
		Instant end = Instant.now();
		long timeElapsed = Duration.between(start, end).toMillis();
		//added for graylogs logging
		ThreadContext.put(LoggingFields.REQUESTTIME, String.valueOf(timer));
		ThreadContext.put(LoggingFields.REQUESTTYPE, "fedsearch");

		logger.info("elapsed time: "+ timeElapsed + ", Request Type: fedsearch");
		ThreadContext.clearAll();
		
//		long serviceTime = Long.valueOf(ThreadContext.get("serviceTime"));
//		int percentage = (int)(serviceTime * 100.0 / timeElapsed + 0.5);
//		logger.info("{}% service time, elapsed time: {}", percentage, timeElapsed);
        ThreadContext.clearMap();
	}

	public static void main(String[] inArgs)
	{
		CommandLineHelper.processArguments(SimpleHandler.class.getCanonicalName(), inArgs);
		if (CommandLineHelper.isShowHelp())
			System.out.println(CommandLineHelper.getHelp());
		else {
			try {
				SimpleHandler self = new SimpleHandler(CommandLineHelper.getHost(), CommandLineHelper.getPortNumber());
				Runtime.getRuntime().addShutdownHook(self.new ShutdownSimpleHandler());
			}
			catch(Exception e) {
				System.err.println("Error starting up, see log for detail.");
				logger.fatal("Error starting up", e);
				System.exit(-1);
			}
		}
	}

	class ShutdownSimpleHandler extends Thread {
		@Override
		public void run() {
			System.out.println("permuter SimpleHandler shutting down... ");
			HttpRequest.shutdown();
			if (m_connection != null) {
				try {
					m_connection.close();
				} catch (IOException e) {
					//logger may have already ended and shutting down anyway, so eating this.
				}
			}
		}
	}
}
