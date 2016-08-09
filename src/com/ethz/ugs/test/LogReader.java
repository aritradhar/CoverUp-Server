//*************************************************************************************
//*********************************************************************************** *
//author Aritra Dhar 																* *
//PhD Researcher																  	* *
//ETH Zurich													   				    * *
//Zurich, Switzerland															    * *
//--------------------------------------------------------------------------------- * * 
///////////////////////////////////////////////// 									* *
//This program is meant to do world domination... 									* *
///////////////////////////////////////////////// 									* *
//*********************************************************************************** *
//*************************************************************************************

package com.ethz.ugs.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import sun.misc.Unsafe;

public class LogReader {
	
    public static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SecurityException e) {
            throw e;
        } catch (NoSuchFieldException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        }
    }

	
	public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
		
		//getUnsafe();
		
		BufferedReader br = new BufferedReader(new FileReader("MainServer.log.11"));
		List<Long> el = new ArrayList<>();
		
		String st = null;
		long counter = 0, k = 0;
		long tot = 0;
		while((st = br.readLine()) != null)
		{
			//only consider sample size upto 50k
			if(k == 50000)
				break;
			
			counter++;
			if(!st.startsWith("INFO"))
				continue;
			if(st.length() == 0)
				continue;
			
			st = st.split(":")[2].trim().split(" ")[0].trim();
			k++;
			long l = Long.parseLong(st);
			tot += l;
			el.add(l);
			//System.out.println(st);
		}
		br.close();
		double mean = (double) tot/k;
		
		double s = 0;
		for(long i : el)
		{
			long x = (long) (mean - i);
			s += Math.pow((mean - i), 2);//((double)mean - i) * ((double)mean - i);
		}
		double var = (double) s/ (k-1);
		var /= 1000000;
		var /= 1000000;
		
		System.out.println("sample size : " + k);
		System.out.println("Mean : " + mean);
		//System.out.println("Variance : " + var + " ms");
	}
	
	
}
