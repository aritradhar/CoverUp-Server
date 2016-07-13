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

		SiteMap.loadTable();

		File[] files = new File(ENV.SOURCE_DOCUMENT_LOCATION).listFiles();

		for(File file : files)
		{
			FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), ENV.FOUNTAIN_CHUNK_SIZE, 50);
			row.makeDroplets();
			SiteMap.insertRowToTable(file.getAbsolutePath(), row);
		}

		SiteMap.saveTable();

		//SiteMap.loadTable();

		System.out.println("---------------------done----------------------");
	}	

}
