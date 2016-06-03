package com.ethz.ugs.test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;

class DummyInit
{
	public static void init() throws IOException, NoSuchAlgorithmException, NoSuchProviderException
	{
		Test.main(null);
	}
}


public class Test 
{
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		
		SiteMap.loadTable();
		
		if(System.getProperty("os.name").contains("Windows"))
		{
			FountainTableRow row1 = new FountainTableRow("C:\\1.txt", 10000, 50);
			row1.makeDroplets();
		
		//System.out.println(row1.toString());
		
			SiteMap.insertRowToTable("C:\\1.txt", row1);
		}
		else
		{
			File[] files = new File("/home/dhara/contents/4k wallpapers/Space").listFiles();
			
			for(File file : files)
			{
				FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), 10000, 50);
				row.makeDroplets();
				
				SiteMap.insertRowToTable(file.getAbsolutePath(), row);
			}
			
		}
		
		
		SiteMap.saveTable();
		
		//SiteMap.loadTable();
		
		System.out.println("---------------------done----------------------");
	}	
	
}
