package com.ethz.ugs.dataStructures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import com.ethz.ugs.compressUtil.SliceData;
import com.ethz.ugs.server.ENV;

public class SliceManager 
{
	//slice url -> fragment location (id)
	public static Map<String, Long> SLICE_MAP = new HashMap<>();
	
	public static final String INVALID_SLICE_URL = "invalid slice url";
	public static final String INVALID_SLICE_FILE = "slice file in local storage does not exist";
	public static final String INVALID_SLICE_ERROR = "unknown error related to I/O";
	
	
	public SliceManager(int chunk_size) throws IOException 
	{
		File files = new File(ENV.INTR_SOURCE_DOCUMENT_LOC);
		
		Random rand = new Random();
		
		for(File file: files.listFiles())
		{
			Long id = rand.nextLong();
			File sliceDir = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + id.toString());
			
			if(!sliceDir.exists())
				sliceDir.mkdir();
			
			byte[] data = Files.readAllBytes(file.toPath());
			SliceData sd = new SliceData(data, ENV.FOUNTAIN_CHUNK_SIZE);
		
			int i = 0;
			
			for(byte[] slice : sd.getAllSlices())
			{
				FileWriter fw_slice = new FileWriter(sliceDir + ENV.DELIM + i);
				fw_slice.append(Base64.getUrlEncoder().encodeToString(slice));
				fw_slice.close();
			}
			
			System.out.println("Slice added : " + file.getName());
			SLICE_MAP.put(file.getName(), id);
		}
	}
	
	public String getSlice(String url, int index)
	{
		Long sliceId = SLICE_MAP.get(url);
		if(sliceId == null)
			return INVALID_SLICE_URL;
		
		System.out.println("Slice with " + url + " found");
		//File sliceDir = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId.toString());
		File sliceFile = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId.toString() + index + ".json");
		
		if(!sliceFile.exists())
			return INVALID_SLICE_FILE;
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(sliceFile));
			String st = null;
			StringBuffer stb = new StringBuffer("");

			while((st = br.readLine()) != null)
				stb.append(st);

			br.close();

			return st;
		}
		catch(IOException ex)
		{
			return INVALID_SLICE_ERROR;
		}
	}
	
	public void saveSliceTable() throws IOException
	{
		JSONObject jObject = new JSONObject(SLICE_MAP);
		FileWriter fw_slice = new FileWriter(ENV.SLICS_TABLE_LOC);
		fw_slice.append(jObject.toString());
		fw_slice.close();
	}
	
	
}
