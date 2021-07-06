package com.newsbank.permuter.util;

import gnu.getopt.Getopt;
//import org.apache.logging.log4j.Level;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.core.LoggerContext;
//import org.apache.logging.log4j.core.appender.ConsoleAppender;
//import org.apache.logging.log4j.core.config.Configurator;
//import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
//import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
//import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
//import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import org.apache.log4j.BasicConfigurator;
import com.newsbank.logwrapper.Logger;
import org.apache.log4j.extras.DOMConfigurator;


public class CommandLineHelper
	{
//	private final static Logger kLogger =	LogManager.getLogger();
	private final static Logger kLogger =	Logger.getLogger(CommandLineHelper.class);
	
	private final static String kHelpString = "-p {port-number} - port number to use\n"
			+ "-H {host} - protocol, hostname and port, e.g. http://localhost:8080\n"
			+ "-l {log4j-config} -  path to log4j config file\n"
			+ "-h - print help.. all other options are ignored and program terminates";
	
	private final static int kDefaultPortNumber	=	8080;
	/* 
	 * -p port number, defaults to 8080
	 * 
	 * -l log4j-config path to log4j config file
	 * -h help.. all other options are ignored and program terminates
	 */
	private static final String			kDefaultOptions		= "hl:p:H:";
	private static int				s_portNumber			= kDefaultPortNumber;
	private static String			s_argument			= null;
	private static String			s_host				= "http://s072.newsbank.com:9090/";
	private static String			s_log4JConf			= null;
	private static boolean			s_showHelp			= false;
	
	public static void processArguments(String inProcessName, String[] inArgs)
		{
		CommandLineHelper.processArguments(inProcessName, inArgs, kDefaultOptions);
		}

	public static void processArguments(String inProcessName, String[] inArgs, String inOptions)
		{
		Getopt theOpts = new Getopt(inProcessName, inArgs, inOptions);

		int theOption;
		while ((theOption = theOpts.getopt()) != -1)
			{
			switch (theOption) {
			case 'l':
				setLog4JConf(theOpts.getOptarg());
				break;
			case 'p':
				setPortNumber(theOpts.getOptarg());
				break;
			case 'H':
				setHost(theOpts.getOptarg());
				break;
			case 'h':
				setShowHelp(true);
				break;
		}
			}
		
		int theOptIndex=theOpts.getOptind();
		if (theOptIndex > 0 && (inArgs.length > 0 && inArgs.length > theOptIndex))
			{
			setArgument(inArgs[theOpts.getOptind()]);
			}
		/*if (s_log4JConf != null)
		{
		DOMConfigurator.configure(s_log4JConf);
		kLogger.info("Log4J Configured from: " + s_log4JConf);
		}
	else
		{
		BasicConfigurator.configure();
		}*/
	}



//		if (s_log4JConf != null)
//			{
//				initializeLog4j(s_log4JConf);
//			kLogger.info("Log4J Configured from: " + s_log4JConf);
//			}
//		else
//			{
//				initializeDefaultLog4j();
//			}
//		}

//		public static LoggerContext initializeLog4j(String lv4path) {
//			LoggerContext context = (LoggerContext) LogManager.getContext(false);
//			File file = new File(lv4path);
//// this will force a reconfiguration - at least in org.apache.logging.log4j:log4j-core:2.13.3
//			context.setConfigLocation(file.toURI());
//			return context;
//		}
//
//		public static LoggerContext initializeDefaultLog4j() {
//			ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
//			builder.setStatusLevel(Level.ERROR); // show internal log4j2 errors
//			builder.setConfigurationName("QuickAndDirtySetup");
//			AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
//					.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
//			appenderBuilder.add(builder.newLayout("PatternLayout")
//					.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
//			builder.add(appenderBuilder);
//			//builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
//			//        .add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
//			builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("Stdout").addAttribute("level", Level.DEBUG)));
//			return Configurator.initialize(builder.build());
//		}



		/**
	 * @return the log4JConf
	 */
	public static String getLog4JConf()
		{
		return s_log4JConf;
		}

	/**
	 * @param inLog4jConf
	 *            the log4JConf to set
	 */
	public static void setLog4JConf(String inLog4jConf)
		{
		s_log4JConf = inLog4jConf;
		}

	/**

	/**
	 * @return the argument
	 */
	public static String getArgument()
		{
		return s_argument;
		}

	/**
	 * @param inArgument
	 *            the argument to set
	 */
	public static void setArgument(String inArgument)
		{
		s_argument = inArgument;
		}

	public static int getPortNumber()
		{
		return s_portNumber;
		}

	public static void setPortNumber(int inPortNumber)
		{
		s_portNumber = inPortNumber;
		}

	public static void setPortNumber(String inPortNumber)
		{
		try
			{
			CommandLineHelper.setPortNumber(Integer.parseInt(inPortNumber));
			}
		
		catch(Throwable theErr)
			{
			theErr.printStackTrace(System.err);
			}
		}
	
	public static boolean isShowHelp()
		{
		return s_showHelp;
		}

	public static void setShowHelp(boolean inShowHelp)
		{
		s_showHelp = inShowHelp;
		}
	
	public static String getHelp()
		{
		return kHelpString;
		}

	public static String getHost()
		{
		return s_host;
		}

	public static void setHost(String inHost)
		{
		s_host = inHost;
		}
	}