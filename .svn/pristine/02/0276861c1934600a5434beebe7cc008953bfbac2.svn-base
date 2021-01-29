package com.newsbank.permuter.util;

public class SystemUtils {

	private SystemUtils() {
//		  intentionally private to avoid instantiation
	}

	public static long getPID() {
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		return Long.parseLong(processName.split("@")[0]);
	}

}
