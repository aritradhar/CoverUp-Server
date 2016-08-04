package com.ethz.ugs.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogReader {
	
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader("MainServer_HP.log"));
		List<Integer> el = new ArrayList<>();
		
		String st = null;
		int counter = 0, k = 0, tot = 0;
		while((st = br.readLine()) != null)
		{
			counter++;
			if(counter % 2 == 1)
				continue;
			if(st.length() == 0)
				continue;
			
			st = st.split(":")[2].trim().split(" ")[0].trim();
			k++;
			int l = Integer.parseInt(st);
			tot += l;
			el.add(l);
			//System.out.println(st);
		}
		br.close();
		double mean = (double) tot/k;
		
		double s = 0;
		for(int i : el)
			s += ((double)mean - i) * ((double)mean - i);
		
		double var = (double) s/ (k-1);
		
		System.out.println("sample size : " + k);
		System.out.println("Mean : " + mean);
		System.out.println("Variance : " + var);
	}
	
	
}
