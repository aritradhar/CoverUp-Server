package com.ethz.ugs.test;

import java.io.IOException;

import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;

public class Test 
{
	
	public static void main(String[] args) throws IOException {
		
		
		FountainTableRow row1 = new FountainTableRow("C:\\4k wallpapers\\Space\\wallhaven-4578.png", 10000, 1000);
		row1.makeDroplets();
		
		FountainTableRow row2 = new FountainTableRow("C:\\4k wallpapers\\Space\\wallhaven-26542.jpg", 10000, 1000);
		row2.makeDroplets();
		
		//System.out.println(row1.toString());
		
		SiteMap.insertRowToTable("C:\\4k wallpapers\\Space\\wallhaven-4578.png", row1);
		SiteMap.insertRowToTable("C:\\4k wallpapers\\Space\\wallhaven-26542.jpg", row2);
		SiteMap.saveTable();
		
		SiteMap.loadTable();
		
		System.out.println("done");
	}

	
	
}
