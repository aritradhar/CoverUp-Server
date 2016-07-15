package com.ethz.ugs.server;


public class ENV {
	
	public static final String VERSION_NO = "1.00";
	
	public static final String SITE_TABLE_LOC = "SITE_TABLE.txt";
	public static final String SLICS_TABLE_LOC = "SLICE_TABLE.txt";
	
	
	public static final String INTR_SOURCE_DOCUMENT_LOC = "Source";
	public static final String INTR_SLICE_OUTPUT_LOC = "INTR_DOCUMENT";
	
	
	public static final boolean ENABLE_COMPRESS = false;
	
	public static final int COMPRESSION_PRESET = 7;
	
	public static final int FIXED_PACKET_SIZE = 15000;
	
	public static final long PEER_TIMEOUT = 5500;
	
	public static final int PEER_CHECK_SCHEDULE = 1000;
	
	public static  String SOURCE_DOCUMENT_LOCATION = null;
	
	public static final int FOUNTAIN_CHUNK_SIZE = 10000;
	
	public static String DELIM = "";
	public static String BROADCAST_LOCATION = null;
	
	public static final boolean PADDING_ENABLE = true;
	
	static
	{
		String OS = System.getProperty("os.name");
		boolean OS_B = (OS.contains("Windows"));
		
		SOURCE_DOCUMENT_LOCATION = OS_B ? "C:\\Source" : "/home/dhara/contents/4k wallpapers/Space";
		DELIM = OS_B ? "\\" : "/";
		BROADCAST_LOCATION = OS_B ? "C:\\Users\\Aritra\\workspace_Mars\\UndergroundServer\\broadcast.txt" : "/home/dhara/broadcast.txt";
	}
}
