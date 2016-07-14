package com.ethz.ugs.server;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import com.ethz.ugs.test.Test;


/**
 * Servlet implementation class MainServer
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


	public MainServer() throws IOException, InterruptedException, NoSuchAlgorithmException, NoSuchProviderException {
		super();

		this.sharedSecretMap = new HashMap<>();

		String os = System.getProperty("os.name");
		System.out.println(os);

		BufferedReader br = null;

		if(os.contains("Windows"))
			br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\workspace_Mars\\UndergroundClient\\codes.bin"));
		else
			br = new BufferedReader(new FileReader("/home/dhara/codes.bin"));
		String str = "";
		this.codes = new HashSet<>();

		while((str = br.readLine()) != null)
			codes.add(str);

		br.close();
		if(!Stats.keygen_done)
		{
			//System.out.println("No keys. Generating...");
			keyGeneration();
		}
		this.broadCastMessage = this.readBroadcastFile();

		//System.out.println(this.broadCastMessage);

		//initialize site map
		//random initialization for testing
		//SiteMap.randomInitialization(20);
		//TODO test
		//dummy initialization
		try
		{
			Test.init();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.err.println("Fatal error..");
			System.exit(1);
		}
		System.out.println("Started...");
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


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

		System.out.println(Base64.getUrlEncoder().encodeToString(publicKey));

		System.out.println(request.getHeader("x-test"));
		
		String flag = request.getParameter("flag");

		String remoteAddress = request.getRemoteAddr();
		Stats.UNIQUE_IP_ADDRESSES.add(remoteAddress);

		System.err.println("Total connections " + Stats.TOTAL_CONNECTIONS);
		System.err.println("Live connection " + Stats.LIVE_CONNECTIONS);

		response.addHeader("Access-Control-Allow-Origin", "*");

		if(flag == null)
		{
			response.getWriter().append("No valid parameter");
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
			responseStr.append("Unique IP addresses\n");

			for(String address : Stats.UNIQUE_IP_ADDRESSES)
				responseStr.append(address + "\n");


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
			ResponseUtil.dropletPlease(request, response, this.privateKey);
		}


		else if(flag.equals("dropletPlease"))
		{
			ResponseUtil.dropletPlease(request, response, this.privateKey);
		}

		else if(flag.equals("end"))
		{
			Stats.LIVE_CONNECTIONS--;

			response.getWriter().append("Connection terminated");
			response.flushBuffer();
		}
	}

}
