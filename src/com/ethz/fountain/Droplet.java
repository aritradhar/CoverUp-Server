package com.ethz.fountain;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.json.JSONObject;

public class Droplet {

	byte[] data;
	byte[] seed;
	int num_chunks;
	public SecureRandom rand;
	
	public boolean generated; 
	List<Integer> chunkNums;
	
	public Droplet(byte[] data, byte[] seed, int num_chunks)
	{
		this.data = data;
		this.seed = seed;
		this.num_chunks = num_chunks;
		chunkNums();
	}
	
	private void chunkNums()
	{
		rand = new SecureRandom(this.seed);	
		this.chunkNums = Util.randChunkNums(rand, this.num_chunks);	
	}
	
	@Override
	public String toString() {
		
		JSONObject jObject = new JSONObject();
		jObject.put("seed", Base64.getUrlEncoder().encodeToString(this.seed));
		jObject.put("num_chunks", this.num_chunks);
		jObject.put("data", Base64.getUrlEncoder().encodeToString(this.data));
		
		
		return jObject.toString(2);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		return this.seed == ((Droplet) obj).seed;
	}
}
