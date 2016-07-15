package com.ethz.ugs.dataStructures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ethz.ugs.server.ENV;

public class SliceManager 
{
	//slice url -> fragment location (id)
	public static Map<String, String> SLICE_MAP = new HashMap<>();
	
	
	public SliceManager(int chunk_size) throws IOException 
	{
		File files = new File(ENV.INTR_SOURCE_DOCUMENT_LOC);
		
		Random rand = new Random();
		
		for(File file: files.listFiles())
		{
			Long id = rand.nextLong();
			SLICE_MAP.put(file.getAbsolutePath(), id.toString());
			File sliceDir = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + id.toString());
			
			if(!sliceDir.exists())
				sliceDir.mkdir();
			
			byte[] data = Files.readAllBytes(file.toPath());
		}
	}
}
