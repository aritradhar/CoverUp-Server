package com.ethz.ugs.compressUtil;

import java.util.ArrayList;
import java.util.List;

public class SliceData {
	
	byte[] data;
	int chunk_size;
	int originalLength;
	
	List<byte[]> slicedData;
	
	public SliceData(byte[] data, int chunk_size)
	{
		this.data = data;
		this.chunk_size = chunk_size;
		this.originalLength = data.length;
		this.slice();
	}
	
	private void slice()
	{
		int num_chunks = (this.data.length % chunk_size == 0) ? data.length / chunk_size : data.length / chunk_size + 1;
		this.slicedData = new ArrayList<>();
		
		for(int i = 0; i < num_chunks; i++)
		{
			byte[] tempChunk = new byte[this.chunk_size];
			
			if(i < num_chunks - 1 || data.length % chunk_size == 0)
				System.arraycopy(data, i * this.chunk_size, tempChunk, 0, this.chunk_size);
			else
			{
				int offSet = data.length % chunk_size;
				int toPad = chunk_size - offSet;
				byte[] pad = new byte[toPad];
				System.arraycopy(data, i * this.chunk_size, tempChunk, 0, offSet);
				System.arraycopy(pad, 0, tempChunk, offSet, pad.length);			
			}
			this.slicedData.add(tempChunk);
		}
	}
	
	public List<byte[]> getAllSlices()
	{
		return this.slicedData;
	}
	
	public byte[] getSlice(int index)
	{
		if(index < 0)
			throw new IllegalArgumentException("Index < 0");
		if(index > this.slicedData.size())
			throw new IllegalArgumentException("Index higer than maximum");
		
		return this.slicedData.get(index);
	}

}
