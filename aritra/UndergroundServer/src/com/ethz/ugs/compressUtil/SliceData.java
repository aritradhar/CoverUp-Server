package com.ethz.ugs.compressUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SliceData {
	
	byte[] data;
	int chunk_size;
	int originalLength;
	
	List<byte[]> slicedData;
	public int sliceCount;
	
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
			ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		    buffer.putInt(i);
		    byte[] lenArray = buffer.array();
			byte[] data = new byte[lenArray.length + tempChunk.length];
			System.arraycopy(lenArray, 0, data, 0, lenArray.length);
			System.arraycopy(tempChunk, 0, data, lenArray.length, tempChunk.length);
			this.slicedData.add(data);
		}
		
		this.sliceCount = this.slicedData.size();
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
	
	public static void main(String[] args) 
	{
		byte[] data = new byte[1000000000];
		new Random().nextBytes(data);
		
		SliceData sd = new SliceData(data, 128);
		sd.getSlice(1);
	}

}
