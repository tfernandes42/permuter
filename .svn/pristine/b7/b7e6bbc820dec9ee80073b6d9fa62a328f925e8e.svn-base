package com.newsbank.permuter.server;

import java.awt.Desktop;
import java.net.URI;

import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

public class SimpleHandlerTest
	{

	@Test
	public void testStartup() throws Exception
		{
		BasicConfigurator.configure();

		//com.newsbank.permuter.server.SimpleHandler.startup(8888, "http://s072.newsbank.com:9090/");

		//com.newsbank.permuter.server.SimpleHandle(8888, "http://s072.newsbank.com:9090/");
		SimpleHandler sh = new SimpleHandler ("http://s072.newsbank.com:9090/",8888);
		
		try
			{
			Desktop.getDesktop().browse(new URI("http://localhost:8888/HelloWorld.txt"));
			do
				{

				}
			while (true);

			}

		catch(Throwable theErr)
			{
			theErr.printStackTrace();
			}
		}

	}


