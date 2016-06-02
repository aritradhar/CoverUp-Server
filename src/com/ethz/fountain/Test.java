package com.ethz.fountain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import org.json.JSONObject;

public class Test {

	
	public static double total = 0;
	
	public static void main(String[] args) throws IOException, InterruptedException {

		FileWriter logWriter = new FileWriter("ResultsTest.log", false);
		File file = new File("C:\\Droplets");

		System.out.println("Deleting droplets..");
		if(file.exists())
			for(File f : file.listFiles())
				f.delete();
		
		
		File f =new File("C:\\4k wallpapers\\Space\\wallhaven-4578.png");
		byte[] data = Files.readAllBytes(f.toPath());

		test(data, 10000, true, logWriter);

		logWriter.close();

	}

	public static void test(byte[] data, int chunk_size, boolean seq, FileWriter logWriter) throws IOException, InterruptedException {


		Random rand = new Random();

		byte[] seed = new byte[32]; 
		rand.nextBytes(seed);
		
		Fountain fountain = new Fountain(data, chunk_size, seed);

		int fa = (data.length / chunk_size);

		System.out.println("Creating droplets...");	

		for(int i = 0; i < fa * 5; i++)
		{
			if(i % 1000 == 0)
				System.out.println(i);

			FileWriter fw = new FileWriter("C:\\Droplets\\" + i + ".json");
			Droplet d = fountain.droplet();
			fw.write(d.toString());
			fw.close();
		}


		//System.out.println("Droplets created...");

		Glass glass = new Glass(fountain.num_chunks);

		byte[] decodedData = new byte[data.length];

		int req_s = 0;


		Droplet d;
		while(true)
		{
			BufferedReader br = null;
			
			try
			{
				if(!seq)
					br = new BufferedReader(new FileReader("C:\\Droplets\\" + rand.nextInt(fa * 5) + ".json"));
				else
					br = new BufferedReader(new FileReader("C:\\Droplets\\" + req_s + ".json"));

			}
			catch(FileNotFoundException ex)
			{
				System.err.println("Not enough to decode");
				break;
			}
			
			StringBuffer stb = new StringBuffer();
			String st = "";

			while((st = br.readLine()) != null)
				stb.append(st);

			br.close();

			req_s++;
			JSONObject jObject = new JSONObject(stb.toString());

			d = new Droplet(Base64.getUrlDecoder().decode(jObject.get("data").toString()), Base64.getUrlDecoder().decode(jObject.get("seed").toString()), jObject.getInt("num_chunks"));
			glass.addDroplet(d);

			if(glass.isDone())
			{
				//for(int i = 0; i < Glass.chunks.length; i++)
				//	System.arraycopy(Glass.chunks[i], 0, decodedData, i * chunk_size, chunk_size);

				decodedData = glass.getDecodedData();

				if(Arrays.equals(data, Arrays.copyOfRange(decodedData, 0, fountain.dataLenBeforPadding)))
					System.err.println("Decoding sucess");
				else
					System.err.println("Decoding ERROR");

				break;
			}
		}

		logWriter.flush();

		/*System.out.print("Counter : " + req_s);
		System.out.println((double)req_s/fa);	
		 */
	}

}
