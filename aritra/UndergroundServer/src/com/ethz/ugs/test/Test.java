package com.ethz.ugs.test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;
import com.ethz.ugs.server.ENV;

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

		boolean exists = SiteMap.loadTable();

		if(System.getProperty("os.name").contains("Windows"))
		{
			if(!exists)
			{
				FountainTableRow row1 = new FountainTableRow(ENV.SOURCE_DOCUMENT_LOCATION, 10000, 50);
				row1.makeDroplets();
			
			//System.out.println(row1.toString());

				SiteMap.insertRowToTable("C:\\1.txt", row1);
			}
			else
				System.out.println("Already exists in table. Skipped...");
			
		}
		else
		{
			File[] files = new File(ENV.SOURCE_DOCUMENT_LOCATION).listFiles();

			for(File file : files)
			{
				if(!exists)
				{
					FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), 10000, 50);
					row.makeDroplets();

					SiteMap.insertRowToTable(file.getAbsolutePath(), row);
				}
				else
					System.out.println("Already exists in table. Skipped...");
			}

		}


		SiteMap.saveTable();

		//SiteMap.loadTable();

		System.out.println("---------------------done----------------------");
	}	

}
