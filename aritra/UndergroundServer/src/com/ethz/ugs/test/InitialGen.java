package com.ethz.ugs.test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;
import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.server.ENV;


public class InitialGen 
{
	
	public static SliceManager sdm = null;
	
	
	public static void init() throws IOException, NoSuchAlgorithmException, NoSuchProviderException
	{
		
		sdm = new SliceManager(ENV.FOUNTAIN_CHUNK_SIZE);
		
		
		SiteMap.loadTable();

		File[] files = new File(ENV.SOURCE_DOCUMENT_LOCATION).listFiles();

		for(File file : files)
		{
			if(SiteMap.TABLE_MAP.containsKey(file.getAbsolutePath()))
			{
				System.out.println(file + "  skipped ..");
				continue;
			}
			
			FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), ENV.FOUNTAIN_CHUNK_SIZE, 50);
			row.makeDroplets();
			SiteMap.insertRowToTable(file.getAbsolutePath(), row);
		}

		SiteMap.saveTable();

		//SiteMap.loadTable();

		System.out.println("---------------------done----------------------");
	}

/*	
 * public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {

		SiteMap.loadTable();

		File[] files = new File(ENV.SOURCE_DOCUMENT_LOCATION).listFiles();

		for(File file : files)
		{
			if(SiteMap.SITE_MAP.containsKey(file.getAbsolutePath()))
			{
				System.out.println(file + "  skipped ..");
				continue;
			}
			
			FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), ENV.FOUNTAIN_CHUNK_SIZE, 50);
			row.makeDroplets();
			SiteMap.insertRowToTable(file.getAbsolutePath(), row);
		}

		SiteMap.saveTable();

		//SiteMap.loadTable();

		System.out.println("---------------------done----------------------");
	}	
	*/

}
