package com.ethz.ugs.dataStructures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Random;

import org.json.JSONObject;

import com.ethz.fountain.Fountain;

public class FountainTableRow 
{
	String url;
	int num_chunks, chunk_size, datalenBeforPadding, droplet_count;
	byte[] data;
	Fountain fountain;
	String dropletLoc;
	byte[] seed;
	
	public FountainTableRow(String url, int chunk_size, int droplet_count) throws IOException 
	{
		this.url = url;
		this.chunk_size = chunk_size;
		this.droplet_count = droplet_count;
		File file = new File(url);
		this.data = Files.readAllBytes(file.toPath());

		System.out.println("Size : " + data.length);
		
		this.seed = new byte[32];
		new Random().nextBytes(seed);

		this.fountain = new Fountain(data, chunk_size, seed);
		this.num_chunks = fountain.chunk_size;
		this.datalenBeforPadding = fountain.dataLenBeforPadding;

		this.dropletLoc = new Integer(new Random().nextInt(Integer.MAX_VALUE)).toString();
		File f = new File(dropletLoc);
		f.mkdir();
	}
	
	public FountainTableRow(JSONObject jObject)
	{
		this.url = jObject.getString("url");
		this.num_chunks = jObject.getInt("num_chunks");
		this.chunk_size = jObject.getInt("chunk_size");
		this.seed = Base64.getUrlDecoder().decode(jObject.getString("seed"));
		this.datalenBeforPadding = jObject.getInt("len");
		this.dropletLoc = jObject.getString("dropletLoc");
	}
	
	public void makeDroplets() throws IOException
	{
		String delm = (System.getProperty("os.name").contains("Windows")) ? "\\" : "/";

		for(int i = 0; i < this.droplet_count; i++)
		{
			FileWriter fw = new FileWriter(this.dropletLoc + delm + i  + ".json");
			fw.write(this.fountain.droplet().toString());
			fw.close();
		}
	}
	
	
	@Override
	public String toString() 
	{
		JSONObject jObject = new JSONObject();
		jObject.put("url", this.url);
		jObject.put("num_chunks", this.num_chunks);
		jObject.put("chunk_size", this.chunk_size);
		jObject.put("seed", Base64.getUrlEncoder().encodeToString(this.seed));
		jObject.put("len", this.datalenBeforPadding);
		jObject.put("dropletLoc", this.dropletLoc);
		
		return jObject.toString();
	}
}
