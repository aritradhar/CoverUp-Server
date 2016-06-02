package com.ethz.ugs.test;

import java.io.File;
import java.io.IOException;

import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;

class DummyInit
{
	public static void init() throws IOException
	{
		Test.main(null);
	}
}


public class Test 
{
	
	public static void main(String[] args) throws IOException {
		
		SiteMap.loadTable();
		
		if(System.getProperty("os.name").contains("Windows"))
		{
			FountainTableRow row1 = new FountainTableRow("C:\\4k wallpapers\\Space\\wallhaven-4578.png", 10000, 1000);
			row1.makeDroplets();
		
			FountainTableRow row2 = new FountainTableRow("C:\\4k wallpapers\\Space\\wallhaven-26542.jpg", 10000, 1000);
			row2.makeDroplets();
		
		//System.out.println(row1.toString());
		
			SiteMap.insertRowToTable("C:\\4k wallpapers\\Space\\wallhaven-4578.png", row1);
			SiteMap.insertRowToTable("C:\\4k wallpapers\\Space\\wallhaven-26542.jpg", row2);
		}
		else
		{
			File[] files = new File("/home/dhara/contents/4k wallpapers/Space").listFiles();
			
			for(File file : files)
			{
				FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), 10000, 3000);
				row.makeDroplets();
				SiteMap.insertRowToTable(file.getAbsolutePath(), row);
			}
			
		}
		
		
		SiteMap.saveTable();
		
		//SiteMap.loadTable();
		
		System.out.println("---------------------done----------------------");
	}	
	
}
