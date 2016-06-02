package com.ethz.ugs.dataStructures;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Contains site map implemented on a HashMap
 * @author Aritra
 *
 */
public class SiteMap {

	
	//title -> link
	public static Map<String, String> SITE_MAP = new HashMap<>();
	
	
	public static void inserToSiteMap(String title, String link)
	{
		SITE_MAP.put(title, link);
	}
	
	
	//random initialization for testing
	public static void randomInitialization(int enrty)
	{
		byte[] randBytes1 = new byte[32];
		byte[] randBytes2 = new byte[32];
		Random rand = new Random();
		
		for(int i = 0; i < enrty; i++)
		{
			rand.nextBytes(randBytes1);
			rand.nextBytes(randBytes2);
			
			inserToSiteMap(Base64.getUrlEncoder().encodeToString(randBytes1), Base64.getUrlEncoder().encodeToString(randBytes2));
		}
	}
}
