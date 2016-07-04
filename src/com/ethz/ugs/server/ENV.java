package com.ethz.ugs.server;


public class ENV {
	
	public static final String VERSION_NO = "1.00";
	
	public static final String SITE_TABLE_LOC = "SITE_TABLE.txt";
	
	public static final boolean ENABLE_COMPRESS = false;
	
	public static final int COMPRESSION_PRESET = 7;
	
	public static final int FIXED_PACKET_SIZE = 15000;
	
	public static final long PEER_TIMEOUT = 5500;
	
	public static final int PEER_CHECK_SCHEDULE = 1000;
	
	public static  String SOURCE_DOCUMENT_LOCATION = null;
	
	public static String DELIM = "";
	
	static
	{
		String OS = System.getProperty("os.name");
		
		SOURCE_DOCUMENT_LOCATION = (OS.contains("windows")) ? "C:\\1.txt" : "/home/dhara/contents/4k wallpapers/Space";
		DELIM = (OS.contains("windows")) ? "\\" : "/"; 
	}
}
