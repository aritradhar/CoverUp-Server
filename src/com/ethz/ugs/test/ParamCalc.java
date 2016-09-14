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
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;


public class ParamCalc 
{

	public static final int h = 1440;
	public static final int w = 2560;

	public static long bucketLenGlobal = 500;

	public static List<List<Double>> relativeScoresList = new ArrayList<>();
	public static List<List<Double>> scoresList = new ArrayList<>();

	public static void peakDetect()
	{
		long min = 950000L;
		List<Double> p1 = scoresList.get(0);
		List<Double> p2 = scoresList.get(1);

		int c = 0;
		List<Long> p1_peak = new ArrayList<>();
		List<Long> p2_peak = new ArrayList<>();

		for(Double d : p1)
		{
			if(d == 0 || c == 0 || c == p1.size() - 1)
			{
				c++;
				continue;
			}
			if(d > p1.get(c - 1) && d > p1.get(c + 1))
				p1_peak.add(min + c * bucketLenGlobal);
			c++;
		}
		c = 0;
		for(Double d : p2)
		{
			if(c == 0 || c == p2.size() - 1)
			{
				c++;
				continue;
			}
			if(d > p2.get(c - 1) && d > p2.get(c + 1))
				p2_peak.add(min + c * bucketLenGlobal);
			c++;
		}

		//System.out.println();
	}

	public static double corr()
	{
		List<Double> p1 = scoresList.get(0);
		List<Double> p2 = scoresList.get(1);

		int len = (p1.size() <= p2.size()) ? p1.size() : p2.size();

		double sum_xi_yi = 0d, sum_xi =0d, sum_yi = 0, sum_xi_sqd = 0d, sum_yi_sqd = 0d;
		for(int i = 0; i < len; i++)
		{
			double xi = p1.get(i);
			double yi = p2.get(i);

			sum_xi += xi;
			sum_yi += yi;

			double xi_yi = xi * yi;
			sum_xi_yi += xi_yi;

			sum_xi_sqd += xi * xi;
			sum_yi_sqd += yi * yi;
		}

		return ((len * sum_xi_yi) - sum_xi * sum_yi) / (Math.sqrt(len * sum_xi_sqd - sum_xi * sum_xi) * Math.sqrt(len * sum_yi_sqd - sum_yi * sum_yi));

	}

	public static int load(long bucketLen, String...Files) throws NumberFormatException, IOException
	{
		long min, max = 0;
		List<Integer> buckC = new ArrayList<>();
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

			//System.out.println("Sample size : " + scores1.size());
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
			buckC.add(bucketCount);

			int[] bucket = new int[bucketCount + 1];
			for(long i : scores1)
			{
				long diff = i - min;
				int pos = (int) ((diff % bucketLen == 0) ? (diff / bucketLen) : (diff / bucketLen) + 1);
				bucket[pos]++;
			}
			//System.out.println("Bucket count : " + bucketCount);
			int nzb = 0;
			for(int buck : bucket)
				if(buck > 0)
					nzb++;
			//System.out.println("non zero Bucket count : " + nzb);

			List<Double> scores_n = new ArrayList<>();
			int score_max = 0;
			for(int i : bucket)
			{
				if(score_max < i)
					score_max = i;
				scores_n.add((double) i);		
			}
			scoresList.add(scores_n);

			List<Double> scores_relative = new ArrayList<>();

			for(int i : bucket)
			{
				Double d =  (double) (i/(double)score_max) * 100;
				scores_relative.add(d);
			}
			List<Double> scores_prob = new ArrayList<>();
			double tot = 0d;
			for(int i : bucket)
				tot += i;

			for(int i : bucket)
				scores_prob.add((double)i/tot);

			//System.out.println(scores_prob.size());		
			relativeScoresList.add(scores_prob);
		}
		Collections.sort(buckC);
		return buckC.get(0);
	}


	public static double ratioCalc(double epsilon, int limit)
	{
		//int limit = 75000;

		List<Double> p1 = relativeScoresList.get(0);
		List<Double> p2 = relativeScoresList.get(1);

		//epsilon = 50d;
		double exp_epsilon = Math.exp(epsilon);

		double delta = 0d;

		limit = (limit == 0) ? p1.size() >= p2.size() ? p2.size() : p1.size() : limit;

		for(int i = 0; i < limit; i++)
		{
			double p1_t = p1.get(i);
			double p2_t = p2.get(i);		
			if(p1_t == 0 || p2_t == 0)
				continue;

			if(p1_t > exp_epsilon *  p2_t)
				delta += p1_t - exp_epsilon * p2_t;

			/*else if(p2_t > exp_epsilon * p1_t)
					delta += p2_t - exp_epsilon * p1_t;
			 */
		}
		//System.out.println("chi sqd : " + chiSqd);
		//System.out.println(epsilon + " : " + String.format("%.18f", delta));

		return delta;
	}

	public static double[] chiSqd(int limit)
	{
		List<Double> p1 = relativeScoresList.get(0);
		List<Double> p2 = relativeScoresList.get(1);
		limit = (limit == 0) ? p1.size() >= p2.size() ? p2.size() : p1.size() : limit;

		double chiSqd = 0.0d;
		double N = 0d;
		for(int i = 0; i < limit; i++)
		{
			double p1_t = p1.get(i);
			double p2_t = p2.get(i);

			if(p1_t > 0)
			{
				chiSqd += ((p2_t - p1_t) * (p2_t - p1_t)) / p1_t;
				N++;
			}
		}

		return new double[]{chiSqd, N};
	}


	public static void createAndShowGui() throws NumberFormatException, IOException, DocumentException
	{

		int limit = 0;
		int i1 = 0;
		Long[] arr = new Long[]{50L, 100L, 200L, 500L, 1000L, 5000L, 10000L, 
				50000L, 100000L, 500000L, 1000000L, 1200000L, 1500000L, 2000000L}; 
		List<Long> bucketLenArr = Arrays.asList(arr);

		List<Double> chiSq = new ArrayList<>();
		List<Double> corr = new ArrayList<>();
		Collections.sort(bucketLenArr);
		for(long bucketLen : bucketLenArr)
		{
			scoresList.clear();
			relativeScoresList.clear();

			load(bucketLen, new String[]{
					"Traces\\MainServer.log.18",
					"Traces\\MainServer.log.17"
			});

			//System.out.println("----------------");

			double[] epsilons = {10000, 5000, 1000, 500, 100, 50, 10, 5, 3, 2.8,  2.7,  2.6, 2.5, 2.4, 2.3, 2.2, 2.1, 2, 1.997, 1.995,
					1.99, 1.989, 1.986, 1.982, 1.98,1.987, 1.986, 1.982, 1.97, 1.96, 1.95, 1.92, 1.9, 1.89, 1.88, 1.85, 1.8, 1.75, 1.7, 1.65, 
					1.6, 1.55, 1.5, 1.45, 1.4, 1.35, 1.3, 1.25, 1.2, 1.15, 1.1, 1.05, 
					1 , .9, .8, .7, .6, .5, .4, .3, .2, .1, .09, .08, .07, .06, .05, .04, .03,.02,.01, 0};

			List<Double> deltas = new ArrayList<>();
			for(double epsilon : epsilons)
				deltas.add(ratioCalc(epsilon, limit));			
			//peakDetect();

			Collections.reverse(deltas);

			for(int i = 0; i < epsilons.length / 2; i++)
			{
				double temp = epsilons[i];
				epsilons[i] = epsilons[epsilons.length - i - 1];
				epsilons[epsilons.length - i - 1] = temp;
			}

			//corr
			System.out.println(bucketLen + "," + corr());
			corr.add( corr());
			double[] chiSArr = chiSqd(0);
			double chiS = chiSArr[0];
			//System.out.println(chiSArr[1] + " : " + chiS);
			chiSq.add(chiS);

			GraphPanel mainPanel = new GraphPanel(deltas, "", "bucket len: " + bucketLen + " ns", deltas.size(), "line");
			mainPanel.addCustomX(epsilons);
			mainPanel.setPreferredSize(new Dimension(w, h));
			JFrame frame = new JFrame("Delta vs Epsilon");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			frame.getContentPane().add(mainPanel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			Document document = new Document(new Rectangle(frame.getSize().width, frame.getSize().height));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("Traces\\dve\\dve_" + (i1++) + ".pdf"));   
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			Graphics2D g2 = cb.createGraphics(frame.getSize().width, frame.getSize().height);
			frame.paint(g2);
			g2.dispose();
			document.close();
		}
		{
			GraphPanel mainPanel = new GraphPanel(chiSq, "", "chi sq vs bucket len", chiSq.size(), "line");
			mainPanel.addCustomX(arr);
			mainPanel.setPreferredSize(new Dimension(w, h));
			JFrame frame = new JFrame("chi sq vs bucket len");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(mainPanel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			Document document = new Document(new Rectangle(frame.getSize().width, frame.getSize().height));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("Traces\\dve\\dve_" + (i1++) + ".pdf"));   
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			Graphics2D g2 = cb.createGraphics(frame.getSize().width, frame.getSize().height);
			frame.paint(g2);
			g2.dispose();
			document.close();
		}
		
		{
			GraphPanel mainPanel = new GraphPanel(corr, "", "correlation vs bucket len", corr.size(), "line");
			mainPanel.addCustomX(arr);
			mainPanel.setPreferredSize(new Dimension(w, h));
			JFrame frame = new JFrame("correlation vs bucket len");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(mainPanel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			Document document = new Document(new Rectangle(frame.getSize().width, frame.getSize().height));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("Traces\\dve\\dve_" + (i1++) + ".pdf"));   
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			Graphics2D g2 = cb.createGraphics(frame.getSize().width, frame.getSize().height);
			frame.paint(g2);
			g2.dispose();
			document.close();
		}

	}

	public static void main(String[] args) throws NumberFormatException, IOException 
	{

		File loc = new File("Traces\\dve");

		for(File file : loc.listFiles())
		{
			file.delete();
		}

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					createAndShowGui();

					List<InputStream> pdfs = new ArrayList<InputStream>();

					for(int i = 0; i < loc.listFiles().length; i++)
						pdfs.add(new FileInputStream("Traces\\dve\\dve_" + i + ".pdf"));

					new File("Traces\\DeltaVEpsilon.pdf").delete();
					OutputStream output = new FileOutputStream("Traces\\DeltaVEpsilon.pdf");
					GraphPanel.concatPDFs(pdfs, output, true);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}});
	}
}
