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

package com.ethz.ugs.server;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import com.ethz.ugs.dataStructures.ClientState;
import com.ethz.ugs.experiments.InitialGen;
import com.lowagie.text.pdf.codec.Base64.OutputStream;


/**
 * Main server class for underground server implementation
 * @author Aritra
 *
 */
@WebServlet("/MainServer")
public class MainServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */

	public Set<String> codes;
	private byte[] sharedSecret;
	public byte[] publicKey;
	private byte[] privateKey;

	private Map<String, byte[]> sharedSecretMap;
	public String broadCastMessage;

	public static Logger logger = Logger.getLogger(MainServer.class.getName());

	public static volatile int C = 0;
	public static final char[] charC = {'|', '/', '-', '\\'};
	
	public static ClientState clientState;
	
	public MainServer() throws IOException, InterruptedException, NoSuchAlgorithmException, NoSuchProviderException {
		super();

		MainServer.clientState = new ClientState();
		
		FileHandler fileH = new FileHandler("MainServer.log", true);
		fileH.setFormatter(new SimpleFormatter());
		MainServer.logger.addHandler(fileH);

		this.sharedSecretMap = new HashMap<>();

		String os = System.getProperty("os.name");
		System.out.println(os);

		
		BufferedReader br = null;
		try
		{
			if(os.contains("Windows"))
				br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\workspace_Mars\\UndergroundClient\\codes.bin"));
			else
				br = new BufferedReader(new FileReader("/home/dhara/codes.bin"));
			String str = "";
			this.codes = new HashSet<>();

			while((str = br.readLine()) != null)
				codes.add(str);

			br.close();
		}
		catch(Exception ex)
		{
			System.err.println("Code file unavailable");
		}
		if(!Stats.keygen_done)
		{
			//System.out.println("No keys. Generating...");
			keyGeneration();
		}
		this.broadCastMessage = this.readBroadcastFile();
		 

		//dummy initialization
		try
		{
			InitialGen.init();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.err.println("Fatal error..");
		}
		
		System.out.println("Started...");

		System.out.println("Default Charset=" + Charset.defaultCharset());    	
	}

	private String readBroadcastFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(ENV.BROADCAST_LOCATION));

		String s = "";
		StringBuffer sb = new StringBuffer("");
		while((s = br.readLine()) != null)
		{
			sb.append(s).append("\n");
		}

		br.close();
		return sb.toString();
	}

	/**
	 * Generate Curve25519 private and public key pairs
	 * @throws IOException
	 */
	private void keyGeneration() throws IOException
	{
		Stats.keygen_done = true;

		if(new File("pk.key").exists() && new File("pk.key").length() > 0 && new File("sk.key").exists() && new File("sk.key").length() > 0 )
		{
			System.err.println("Key file exists");

			BufferedReader pkBr = new BufferedReader(new FileReader("pk.key")); 
			this.publicKey = Base64.getUrlDecoder().decode(pkBr.readLine());
			pkBr.close();

			BufferedReader skBr = new BufferedReader(new FileReader("sk.key")); 
			this.privateKey = Base64.getUrlDecoder().decode(skBr.readLine());
			skBr.close();

			return;
		}
		else
		{
			System.err.println("Key file not found. Regenerating keyfile");


			FileWriter pkFw = new FileWriter("pk.key");
			FileWriter skFw = new FileWriter("sk.key");

			Curve25519KeyPair keypair = Curve25519.getInstance("best").generateKeyPair();
			this.publicKey = keypair.getPublicKey();
			pkFw.write(Base64.getUrlEncoder().encodeToString(this.publicKey));
			this.privateKey = keypair.getPrivateKey();
			skFw.write(Base64.getUrlEncoder().encodeToString(this.privateKey));

			pkFw.close();
			skFw.close();
		}
	}

	/**
	 * Right now we are supporting both GET and POST message. Later we have to stop supporting any GET call to this server
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request,response);
		//response.getWriter().append("Get request to this server is not supported");

	}


	/**
	 * Post. Default
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		synchronized (request) 
		{
			C += 1;
			C %= 4;
		}


		String flag = request.getParameter("flag");
		String flag1 = request.getParameter("prob");


		response.addHeader("Access-Control-Allow-Origin", "*");

		if(flag1 != null)
		{
			ENV.PROB_THRESHOLD = Double.parseDouble(flag1);
			
			response.getWriter().append("Prob reset");
			response.flushBuffer();
			return;
		}
		
		if(flag == null)
		{
			response.getWriter().append("No valid parameter");
			response.flushBuffer();
		}

		else if(flag.equals("init"))
		{

			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;

			String code = request.getParameter("code");
			if(code == null)
				response.getWriter().append("No valid code ");
			else if(this.codes.contains(code))
				response.getWriter().append("code authenticated ");
		}

		else if(flag.equals("ke"))
		{
			ResponseUtil.ke(request, response, this.publicKey, this.privateKey, this.sharedSecret, this.sharedSecretMap);		
		}

		else if(flag.equals("admin"))
		{
			StringBuffer responseStr = new StringBuffer("Total connection : " + Stats.TOTAL_CONNECTIONS + "\nlive connections : " + Stats.LIVE_CONNECTIONS + "\n");
			//responseStr.append("Unique IP addresses\n");

			//for(String address : Stats.UNIQUE_IP_ADDRESSES)
			//	responseStr.append(address + "\n");

			responseStr.append(Base64.getUrlEncoder().encodeToString(publicKey)).append("\n");

			File[] files = new File(".").listFiles();

			for(File file : files)
			{
				if(file.getName().contains(".lck"))
					continue;
				
				if(!file.getName().contains("MainServer.log"))
					continue;
				
				
				BufferedReader br = new BufferedReader(new FileReader(file));
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

				responseStr.append("Log file : " + file.getName()).append("\n");
				responseStr.append("sample size : " + k).append("\n");
				responseStr.append("Mean : " + mean).append("\n");
				responseStr.append("Variance : " + var).append("\n");
				responseStr.append("----------------------------------\n");
			}
			response.getWriter().append(responseStr.toString());
			response.flushBuffer();
		}

		else if(flag.equals("rand"))
		{
			ResponseUtil.rand(request, response);
		}

		else if(flag.equals("randB"))
		{
			ResponseUtil.randB(request, response);
		}

		else if(flag.equals("broadCast"))
		{
			ResponseUtil.broadCast(request, response, this.broadCastMessage);
		}

		//request for sending broadcast json
		else if(flag.equals("broadCastjson"))
		{
			ResponseUtil.broadCastjson(request, response, this.broadCastMessage, this.publicKey, this.privateKey);
		}


		//request for the sitemap table
		else if(flag.equals("tablePlease"))
		{
			ResponseUtil.tablePlease(request, response, this.privateKey);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}


		else if(flag.equals("dropletPlease"))
		{
			//String postBody = ServerUtil.GetBody(request);

			BufferedReader payloadReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = payloadReader.readLine())!= null)
				stb.append(st);

			String postBody = stb.toString();

			//System.out.println("BODY : " + postBody);

			if(postBody == null || postBody.length() == 0)
			{
				try
				{
					ResponseUtil.dropletPlease(request, response, this.privateKey);
				}
				catch(IOException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			else if(postBody.startsWith("0"))
				ResponseUtil.dropletPlease(request, response, this.privateKey);

			else if(postBody.startsWith("1"))
				ResponseUtil.dropletPleaseIntr(request, response, this.privateKey,postBody);

			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");

		}

		else if(flag.equals("tablePleaseBin"))
		{
			ResponseUtilBin.tablePleaseBin(request, response, this.privateKey);
			//System.out.println(0);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}

		else if(flag.equals("dropletPleaseBin"))
		{
			BufferedReader payloadReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = payloadReader.readLine())!= null)
				stb.append(st);

			String postBody = stb.toString();

			System.out.println("BODY : " + postBody);

			if(postBody == null || postBody.length() == 0)
			{
				try
				{
					ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, false);
				}
				catch(IOException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			else if(postBody.startsWith("0"))
				ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, false);

			else if(postBody.startsWith("1"))
				ResponseUtilBinHP.dropletPleaseIntrBin(request, response, this.privateKey, postBody);

			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}
		
		else if(flag.equals("slicePleaseBin"))
		{
			ResponseUtilBinHP.slicePleaseBin(request, response, privateKey, null);
			
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}

		else if(flag.equals("dropletPleaseBin_1"))
		{
			ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, true);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}

		//the fake one
		else if(flag.equals("dropletPleaseBinFake"))
		{
			ResponseUtilBin.dropletPleaseBin(request, response, this.privateKey, true);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
			//System.out.println(3);
		}	
		
		else if(flag.equals("dropletPleaseBinProb"))
		{
			byte[] postBody = IOUtils.toByteArray(request.getInputStream());
		
			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(sslId == null)
			{
				response.getWriter().append("Non TLS/SSL connection terminated");
				response.flushBuffer();
				return;
			}

			byte[] randAESkey = new byte[16];
			byte[] randAESiv = new byte[16];
			SecureRandom rand = new SecureRandom();
			rand.nextBytes(randAESkey);
			
			if(postBody == null || postBody.length == 0)
			{
				try
				{
					ResponseUtilBinProb.dropletPleaseBin(request, response, this.privateKey, randAESkey, randAESiv);
				}
				catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			else if(postBody[0] == 0x00)
				try {
					ResponseUtilBinProb.dropletPleaseBin(request, response, this.privateKey, randAESkey, randAESiv);
				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
					
					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					rand.nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			else if(postBody[0] == 0x01)
				try {
					ResponseUtilBinProb.dropletPleaseIntrBin(request, response, this.privateKey, randAESkey, randAESiv, postBody);
					
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
					
					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					rand.nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}
		
		//constant time response
		else if(flag.equals("dropletPleaseBinConst"))
		{
			byte[] postBody = IOUtils.toByteArray(request.getInputStream());
		
			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(sslId == null)
			{
				response.getWriter().append("Non TLS/SSL connection terminated");
				response.flushBuffer();
				return;
			}
			
			if(postBody == null || postBody.length == 0)
			{
				try
				{
					ResponseUtilBinConstantTime.dropletPleaseBin(request, response, this.privateKey);
				}
				catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
				{
					response.getWriter().append(ex.getMessage());
					response.flushBuffer();
				}
			}
			else if(postBody[0] == 0x00)
				try {
					ResponseUtilBinConstantTime.dropletPleaseBin(request, response, this.privateKey);
				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
						InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
					
					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			else if(postBody[0] == 0x01)
				try {
					ResponseUtilBinConstantTime.dropletPleaseIntrBin(request, response, this.privateKey, postBody);
					
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
					
					byte[] ret = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					new SecureRandom().nextBytes(ret);
					ServletOutputStream out = response.getOutputStream();
					out.write(ret);
					out.flush();
					out.close();
					response.flushBuffer();
				}
			else
			{
				response.getWriter().append("Header against specification");
				response.flushBuffer();
			}
			//System.out.println(1);
			System.out.println(flag + " " + request.getRemoteAddr());
			System.out.println(charC[C]);
			System.out.println("-------------------------------------");
		}
		
		
		else if(flag.equals("end"))
		{
			Stats.LIVE_CONNECTIONS--;

			response.getWriter().append("Connection terminated");
			response.flushBuffer();
		}
		
		else
		{
			response.getWriter().append("Wrong url");
			response.flushBuffer();
		}
	}
}
