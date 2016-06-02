package com.ethz.ugs.dataStructures;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ethz.ugs.server.ENV;

/**
 * Contains site map implemented on a HashMap
 * @author Aritra
 *
 */
public class SiteMap {

	//url/file location -> row of the table
	public static Map<String, FountainTableRow> TABLE_MAP = new HashMap<>();
	//title -> link
	public static Map<String, String> SITE_MAP = new HashMap<>();
	
	
	public static void insertRowToTable(String url, FountainTableRow tableRow)
	{
		SiteMap.TABLE_MAP.put(url, tableRow);
	}
	
	public static void saveTable() throws IOException
	{
		FileWriter fw = new FileWriter(ENV.SITE_TABLE_LOC);
		JSONObject jObject = new JSONObject();
		
		JSONArray jArray = new JSONArray();
		
		for(String key : TABLE_MAP.keySet())
		{
			JSONObject inJo = new JSONObject();
			inJo.put("key", key);
			inJo.put("value", TABLE_MAP.get(key).toString());
			jArray.put(inJo);
		}
		jObject.put("table", jArray);
		
		fw.write(jObject.toString(2));
		fw.close();
	}
	
	public static void loadTable() throws IOException
	{
		TABLE_MAP = new HashMap<>();
		
		BufferedReader br = new BufferedReader(new FileReader(ENV.SITE_TABLE_LOC));
		String st = null;
		StringBuffer stb = new StringBuffer();
		
		while((st = br.readLine())!= null)
			stb.append(st);
		
		System.out.println(stb);
		JSONObject jObject = new JSONObject(stb.toString());
		
		JSONArray jarray = jObject.getJSONArray("table");
		
		for(int i = 0; i < jarray.length(); i++)
		{
			JSONObject inObj = jarray.getJSONObject(i);
			String url = inObj.getString("key");
			inObj.get("value");
		}
		//jObject.ge
		
		br.close();
	}
	
	
	public static void inserToSiteMap(String title, String link)
	{
		SiteMap.SITE_MAP.put(title, link);
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
