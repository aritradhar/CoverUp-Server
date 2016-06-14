package com.ethz.ugs.compressUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

public class CompressUtil {
	
	public static void compress(String infileString, String outfileString) throws IOException
	{
		FileInputStream inFile = new FileInputStream(infileString);
		FileOutputStream outfile = new FileOutputStream(outfileString);

		LZMA2Options options = new LZMA2Options();

		options.setPreset(1); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)

		XZOutputStream out = new XZOutputStream(outfile, options);

		byte[] buf = new byte[8192];
		int size;
		while ((size = inFile.read(buf)) != -1)
		   out.write(buf, 0, size);

		out.finish();
		
		
		inFile.close();
		out.close();
	}

	public static void deCompress(String infileString, String outfileString) throws IOException
	{
		FileInputStream inFile = new FileInputStream(infileString);
		FileOutputStream outfile = new FileOutputStream(outfileString);

		XZInputStream in = new XZInputStream(inFile);
				
		byte[] buf = new byte[8192];
		int size;
		while ((size = in.read(buf)) != -1)
			outfile.write(buf, 0, size);

		in.close();
		inFile.close();
		outfile.close();
	}
	
	
}
