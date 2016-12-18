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
import java.io.FileWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Aritra
 *
 */
public class JsDataProcess {
	
	public static void diff() throws Exception
	{
		Scanner s = new Scanner(System.in);
		String fileName = s.next();
		
		List<Double> nos = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String str = null;
		while((str = br.readLine()) != null)
			nos.add(Double.parseDouble(str));
		s.close();
		br.close();
		FileWriter fw = new FileWriter(fileName + "_diff.csv");
		
		for(int i = 0; i < nos.size() - 1; i++)
			fw.append((nos.get(i+1) - nos.get(i)) + "\n");
		
		fw.close();
	}
	
	public static void addNoise() throws Exception
	{
		int min = 600, max = 1000;
		Scanner s = new Scanner(System.in);
		String fileName = s.next();
		
		List<Double> nos = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String str = null;
		while((str = br.readLine()) != null)
			nos.add(Double.parseDouble(str));
		s.close();
		br.close();
		FileWriter fw = new FileWriter(fileName + "_noise.csv");
		SecureRandom rand = new SecureRandom();
		
		for(Double d : nos)
			fw.append((d + rand.nextDouble() * (max - min) + min) + "\n");
		
		fw.close();
	}
	
	public static void main(String[] args) throws Exception{
		
		System.out.println("1. diff \n2. add noise");
		Scanner s = new Scanner(System.in);
		int choise = s.nextInt();
		if(choise == 1)
			diff();
		else
			addNoise();
		s.close();
	}

}
