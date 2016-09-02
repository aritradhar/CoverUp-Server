package com.ethz.ugs.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;


public class GraphPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3374366456807894314L;
	//private int width = 800;
    //private int heigth = 400;
    private int padding = 80;
    private int labelPadding = 25;
    //private Color lineColor = new Color(0, 102, 230, 180);
    //private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    private List<Long> scores1;

    public GraphPanel(List<Long> scores1) {
        this.scores1 = scores1;
    }

    @Override
    protected void paintComponent(Graphics g) {
    	      
        super.paintComponent(g);
        BufferedImage paintImage = new BufferedImage(500, 400, BufferedImage.TYPE_3BYTE_BGR);
        //g = paintImage.createGraphics();

        
        try {
			draw((Graphics2D) g, this.scores1, new Color(0, 102, 230, 180), new Color(100, 100, 100, 180));
						
		} catch (FileNotFoundException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void draw(Graphics2D g2,List<Long> scores1, Color lineColor, Color pointColor) throws FileNotFoundException, DocumentException
    {      
    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores1.size() - 1);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < scores1.size(); i++) {
            int x1 = (int) (i * xScale + padding + labelPadding);
            int y1 = (int) ((getMaxScore() - scores1.get(i)) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);
        

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (scores1.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                
                String yLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
               
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        for (int i = 0; i < scores1.size(); i++) {
            if (scores1.size() > 1) {
                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scores1.size() - 1) + padding + labelPadding;
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((scores1.size() / 20.0)) + 1)) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    String xLabel = i + "";
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }

        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(pointColor);
        for (int i = 0; i < graphPoints.size(); i++) {
            int x = graphPoints.get(i).x - pointWidth / 2;
            int y = graphPoints.get(i).y - pointWidth / 2;
            int ovalW = pointWidth;
            int ovalH = pointWidth;
            g2.fillOval(x, y, ovalW, ovalH);
        }
    }
    

//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(width, heigth);
//    }
    private double getMinScore() {
        double minScore = Double.MAX_VALUE;
        for (long score : scores1) {
            minScore = Math.min(minScore, score);
        }
        return minScore;
    }

    private double getMaxScore() {
        double maxScore = Double.MIN_VALUE;
        for (long score : scores1) {
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }

    public void setScores(List<Long> scores) {
        this.scores1 = scores;
        invalidate();
        this.repaint();
    }

    public List<Long> getScores() {
        return scores1;
    }

    

    private static void createAndShowGui() throws NumberFormatException, IOException, DocumentException {
        List<Long> scores1 = new ArrayList<>();
        
        BufferedReader br = new BufferedReader(new FileReader("MainServer.log.8"));
	
		String st = null;
		long k = 0;
		while((st = br.readLine()) != null)
		{
			//only consider sample size upto 50k
			if(k == 50000)
				break;	
			if(!st.startsWith("INFO"))
				continue;
			if(st.length() == 0)
				continue;		
			st = st.split(":")[2].trim().split(" ")[0].trim();
			k++;
			long l = Long.parseLong(st);
			scores1.add(l);
		}
		br.close();
		
		long min = scores1.get(0), max = 0;
		for(long i : scores1)
		{
			if(min >= i)
				min = i;
			
			if(max < i)
				max = i;
		}
		
		long blockLen = 500;
		int blockSize = (int) (((max - min) % blockLen == 0) ? ((max - min) / blockLen) : ((max - min) / blockLen) + 1);
		
		//System.out.println(blockSize);
		int[] block = new int[blockSize + 1];
		for(long i : scores1)
		{
			long diff = i - min;
			int pos = (int) ((diff % blockLen == 0) ? (diff / blockLen) : (diff / blockLen) + 1);
			//System.out.println(pos);
			block[pos]++;
			//System.out.println("---" + block[pos]);
		}
		
		List<Long> scores_n = new ArrayList<>();
		for(int i : block)
			scores_n.add((long) i);		
        
		Collections.shuffle(scores1, new SecureRandom());
		//take a random sample
		int sampleSize = 15000;
		List<Long> _scores1 = null;
		
		try
		{
			scores1 = scores1.subList(0, sampleSize);
		}
		catch(IndexOutOfBoundsException ex)
		{
			_scores1 = scores1;
		}
       // GraphPanel mainPanel = new GraphPanel(_scores1);
        
		List<Long> subScore = null;
		try
		{
			subScore = scores_n.subList(0, sampleSize);
		}
		catch(IndexOutOfBoundsException ex)
		{
			subScore = scores_n;
		}
		GraphPanel mainPanel = new GraphPanel(subScore);
        
        mainPanel.setPreferredSize(new Dimension(2560, 1440));
        JFrame frame = new JFrame("DrawGraph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        BufferedImage bi = new BufferedImage(frame.getSize().width, frame.getSize().height, BufferedImage.TYPE_INT_ARGB); 
        Graphics g = bi.createGraphics();
        frame.paint(g);  //this == JComponent
        g.dispose();
        try{ImageIO.write(bi,"png",new File("test.png"));}catch (Exception e) {}
        
        Document document = new Document(new Rectangle(frame.getSize().width, frame.getSize().height));
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("GraphOut.pdf"));
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        Graphics2D g2 = cb.createGraphics(frame.getSize().width, frame.getSize().height);
        frame.paint(g2);
        g2.dispose();
        document.close();
    }

    public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            try {
				createAndShowGui();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }
      });
   }
}
