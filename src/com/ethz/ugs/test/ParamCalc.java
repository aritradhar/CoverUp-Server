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

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class ParamCalc 
{

	public static final int h = 1440;
	public static final int w = 2560;
	
	public static long bucketLen = 500;

	public static List<List<Double>> relativeScoresList = new ArrayList<>();
	
	public static void load(String...Files) throws NumberFormatException, IOException
	{
		long min, max = 0;
		for(String file : Files)
		{
			List<Long> scores1 = new ArrayList<>();

			BufferedReader br = new BufferedReader(new FileReader(file));

			String st = null;
			//TODO dummy to set the minimum
			scores1.add(950000L);

			while((st = br.readLine()) != null)
			{
				if(!st.startsWith("INFO"))
					continue;
				if(st.length() == 0)
					continue;		
				st = st.split(":")[2].trim().split(" ")[0].trim();
				long l = Long.parseLong(st);
				scores1.add(l);
			}
			br.close();

			System.out.println("Sample size : " + scores1.size());
			min = scores1.get(0);
			max = 0;
			for(long i : scores1)
			{
				if(min >= i)
					min = i;

				if(max < i)
					max = i;
			}
			int bucketCount = (int) (((max - min) % bucketLen == 0) ? ((max - min) / bucketLen) : ((max - min) / bucketLen) + 1);

			int[] bucket = new int[bucketCount + 1];
			for(long i : scores1)
			{
				long diff = i - min;
				int pos = (int) ((diff % bucketLen == 0) ? (diff / bucketLen) : (diff / bucketLen) + 1);
				bucket[pos]++;
			}

			List<Double> scores_n = new ArrayList<>();
			int score_max = 0;
			for(int i : bucket)
			{
				if(score_max < i)
					score_max = i;
				scores_n.add((double) i);		
			}
			List<Double> scores_relative = new ArrayList<>();

			for(int i : bucket)
			{
				Double d =  (double) (i/(double)score_max) * 100;
				scores_relative.add(d);
			}
			
			System.out.println(scores_relative.size());		
			relativeScoresList.add(scores_relative);
		}
	}
	
	
	public static double ratioCalc(double epsilon, int limit)
	{
		//int limit = 75000;
		
		List<Double> p1 = relativeScoresList.get(0);
		List<Double> p2 = relativeScoresList.get(1);
		
		//epsilon = 50d;
		double exp_epsilon = Math.exp(epsilon);
		
		double delta = 0d;
		
		for(int i = 0; i < limit; i++)
		{
			double p1_t = p1.get(i);
			double p2_t = p2.get(i);
			
			if(p1_t == 0 || p2_t == 0)
				continue;
			
			if((p1_t / p2_t) > exp_epsilon)
			{
				//System.out.println(p1_t - exp_epsilon * p2_t);
				delta += p1_t - exp_epsilon * p2_t;
			}
			else if((p2_t / p1_t) > exp_epsilon)
			{
				delta += p2_t - exp_epsilon * p1_t;
			}
		}
		
		System.out.println(epsilon + " : " + delta);
		
		return delta;
	}
	
	
	public static void createAndShowGui() throws NumberFormatException, IOException
	{
		
		int limit = 75000;
		
		load(new String[]{
				"Traces\\MainServer.log.18",
				"Traces\\MainServer.log.17"
		});
		
		System.out.println("----------------");
		
		double[] epsilons = {10000, 5000, 1000, 500, 100, 50, 10, 5, 3,2.5, 2.4, 2.3, 2.2, 2.1, 2, 1.997, 1.995,
				1.99, 1.989, 1.986, 1.982, 1.98,1.789, 1.786, 1.782, 1.97, 1.96, 1.95, 1.92, 1.9, 1.89, 1.88, 1.85, 1.8, 1.75, 1.7, 1.65, 
				1.6, 1.55, 1.5, 1.45, 1.4, 1.35, 1.3, 1.25, 1.2, 1.15, 1.1, 1.05, 
				1 , .9, .8, .7, .6, .5, .4, .3, .2, .1, 0};
		
		List<Double> deltas = new ArrayList<>();
		for(double epsilon : epsilons)
			deltas.add(ratioCalc(epsilon, limit));
		
		Collections.reverse(deltas);
		
		GraphPanel mainPanel = new GraphPanel(deltas, "", "t limit " + limit, deltas.size(), true);
		
		for(int i = 0; i < epsilons.length / 2; i++)
		{
		    double temp = epsilons[i];
		    epsilons[i] = epsilons[epsilons.length - i - 1];
		    epsilons[epsilons.length - i - 1] = temp;
		}
		
		mainPanel.addCustomX(epsilons);
		mainPanel.setPreferredSize(new Dimension(w, h));
		JFrame frame = new JFrame("Delta vs Epsilon");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException 
	{
			
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGui();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}});
		
		
		
		
	}


}
