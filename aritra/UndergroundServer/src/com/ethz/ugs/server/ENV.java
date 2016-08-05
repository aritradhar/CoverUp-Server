package com.ethz.ugs.server;


public class ENV {
	
	public static final String VERSION_NO = "1.00";
	
	public static final String SITE_TABLE_LOC = "SITE_TABLE.txt";
	public static final String SLICS_TABLE_LOC = "SLICE_TABLE.txt";
	
	
	public static final String INTR_SOURCE_DOCUMENT_LOC = "INTR_DOCUMENT";
	public static final String INTR_SLICE_OUTPUT_LOC = "INTR_DOCUMENT_OUT";
	
	public static final boolean ENABLE_COMPRESS = false;
	
	public static final int COMPRESSION_PRESET = 7;
	
	public static final int FIXED_PACKET_BASE_SIZE = 15000;
	//9 bytes for JSON => "pad":"",   9 bytes
	public static final int FIXED_PACKET_SIZE = FIXED_PACKET_BASE_SIZE - 9;	
	public static final int FIXED_PACKET_SIZE_BIN = FIXED_PACKET_BASE_SIZE;
	
	public static final long PEER_TIMEOUT = 5500;
	public static final int PEER_CHECK_SCHEDULE = 1000;
	
	public static  String SOURCE_DOCUMENT_LOCATION = null;
	
	public static final int FOUNTAIN_CHUNK_SIZE = 10000;
	
	public static String DELIM = "";
	public static String BROADCAST_LOCATION = null;
	
	public static final boolean PADDING_ENABLE = true;
	public static final boolean RANDOM_PADDING = false;
	//iff RANDOM_PADDING = false
	public static final byte PADDING_DETERMINISTIC_BYTE = (byte) 0xaa;
	public static final char PADDING_DETERMINISTIC_STRING = 'A';
	
	public static final boolean EXPERIMENTAL = false;
	
	static
	{
		String OS = System.getProperty("os.name");
		boolean OS_B = (OS.contains("Windows"));
		
		SOURCE_DOCUMENT_LOCATION = OS_B ? "C:\\Source" : "/home/dhara/contents/4k wallpapers/Space";
		DELIM = OS_B ? "\\" : "/";
		BROADCAST_LOCATION = OS_B ? "C:\\Users\\Aritra\\workspace_Mars\\UndergroundServer\\broadcast.txt" : "/home/dhara/broadcast.txt";
	}
}
