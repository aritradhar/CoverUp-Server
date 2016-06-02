package com.ethz.ugs.dataStructures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import com.ethz.fountain.Fountain;

public class FountainTableRow 
{
	String url;
	int num_chunks, chunk_size, datalenBeforPadding, droplet_count;
	byte[] data;
	Fountain fountain;
	String dropletLoc;

	public FountainTableRow(String url, int chunk_size, int droplet_count) throws IOException 
	{
		this.url = url;
		this.chunk_size = chunk_size;
		this.droplet_count = droplet_count;
		File file = new File(url);
		this.data = Files.readAllBytes(file.toPath());

		System.out.println("Size : " + data.length);
		
		byte[] seed = new byte[32];
		new Random().nextBytes(seed);

		this.fountain = new Fountain(seed, chunk_size, seed);
		this.datalenBeforPadding = fountain.dataLenBeforPadding;

		this.dropletLoc = new Integer(new Random().nextInt(Integer.MAX_VALUE)).toString();
	}

	public void makeDroplets() throws IOException
	{
		String delm = (System.getProperty("os.name").contains("Windows")) ? "\\" : "/";

		for(int i = 0; i < this.droplet_count; i++)
		{
			FileWriter fw = new FileWriter(i  + ".json");
			System.out.println(dropletLoc + delm + i  + ".json");
			fw.write(this.fountain.droplet().toString());
			fw.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		
		new FountainTableRow("C:\\4k wallpapers\\Space\\wallhaven-4578.png", 10000, 15000).makeDroplets();;
		System.out.println("done");
	}

}
