package com.ethz.server;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

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
	
	
	public MainServer() throws IOException {
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
			System.out.println("No keys. Generating...");
			keyGeneration();
		}
		this.broadCastMessage = this.readBroadcastFile();
		
		//System.out.println(this.broadCastMessage);
		
		System.out.println("Started...");


		// TODO Auto-generated constructor stub
	}
	
	private String readBroadcastFile() throws IOException
	{
		String os = System.getProperty("os.name");
		
		
		BufferedReader br = null;
		if(os.contains("Windows"))
			br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\workspace_Mars\\UndergroundServer\\broadcast.txt"));
		else
			br = new BufferedReader(new FileReader("/home/dhara/broadcast.txt"));
		
		String s = "";
		StringBuffer sb = new StringBuffer("");
		while((s = br.readLine()) != null)
		{
			sb.append(s).append("\n");
		}
		
		br.close();
		return sb.toString();
	}

	private void keyGeneration()
	{
		Stats.keygen_done = true;
		
		Curve25519KeyPair keypair = Curve25519.getInstance("best").generateKeyPair();
		this.publicKey = keypair.getPublicKey();
		this.privateKey = keypair.getPrivateKey();
	}
	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
		//response.getWriter().append("Get request to this server is not supported");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

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
			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;
			
			String otherPublicKey = request.getParameter("pk");
			String sessionCode = request.getParameter("code");
			
			this.sharedSecret = Curve25519.getInstance("best").calculateAgreement(Base64.getUrlDecoder().decode(otherPublicKey), this.privateKey);
			response.getWriter().append(Base64.getUrlEncoder().encodeToString(this.publicKey));
			
			byte[] sharedSecretHash = null;
			try {
				sharedSecretHash = MessageDigest.getInstance("sha-256").digest(sharedSecret);
			}
			catch (NoSuchAlgorithmException e) {

			}
			
			this.sharedSecretMap.put(sessionCode, sharedSecretHash);
			
			System.out.println(Base64.getUrlEncoder().encodeToString(sharedSecretHash));
		}
		
		else if(flag.equals("admin"))
		{
			StringBuffer responseStr = new StringBuffer("Total connection : " + Stats.TOTAL_CONNECTIONS + "\n live connections : " + Stats.LIVE_CONNECTIONS + "\n");
			responseStr.append("Unique IP addresses");
			
			for(String address : Stats.UNIQUE_IP_ADDRESSES)
				responseStr.append(address + "\n");
			
			
			response.getWriter().append(responseStr.toString());
			response.flushBuffer();
		}
		
		else if(flag.equals("rand"))
		{
			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;
			
			int responseSize = 256;
			SecureRandom rand = new SecureRandom();
			byte[] toSent = new byte[responseSize];
			rand.nextBytes(toSent);
			
			OutputStream output = response.getOutputStream();
		    output.write(toSent);
		    output.flush();
		    response.flushBuffer();
		}
		
		else if(flag.equals("randB"))
		{
			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;
			
			int responseSize = 256;
			SecureRandom rand = new SecureRandom();
			byte[] toSent = new byte[responseSize];
			rand.nextBytes(toSent);
			String responseStr = Base64.getUrlEncoder().encodeToString(toSent);
			
			response.getWriter().append(responseStr);
			response.flushBuffer();
		}
		
		else if(flag.equals("broadCast"))
		{
			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;
			
			response.getWriter().append(this.broadCastMessage);
			response.flushBuffer();
		}
		
		else if(flag.equals("broadCastjson"))
		{
			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;
			
			
			JSONObject jObject = new JSONObject();
			byte[] messageBytes = this.broadCastMessage.getBytes();
			byte[] messageHash = null;
			try {
				messageHash = MessageDigest.getInstance("sha-512").digest(messageBytes);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println(messageBytes.length);
			byte[] signature = Curve25519.getInstance("best").calculateSignature(this.privateKey, messageHash);
			//test
			//System.out.println("Hash : " + Base64.getUrlEncoder().encodeToString(messageHash));
			//System.out.println("sk : " + Base64.getUrlEncoder().encodeToString(this.privateKey));
			System.out.println("pk : " + Base64.getUrlEncoder().encodeToString(this.publicKey));
			System.out.println("signature : " + Base64.getUrlEncoder().encodeToString(signature));
			
			
			System.out.println("Signature verification : " + Curve25519.getInstance("best").verifySignature(this.publicKey, messageHash, signature));
			String signatureBase64 = Base64.getUrlEncoder().encodeToString(signature);
			jObject.put("version", ENV.VERSION_NO);
			jObject.put("message", this.broadCastMessage);
			jObject.put("signature", signatureBase64);
			
			response.getWriter().append(jObject.toString(2));
			response.flushBuffer();
		}
		
		else if(flag.equals("end"))
		{
			Stats.LIVE_CONNECTIONS--;
			
			response.getWriter().append("Connection terminated");
			response.flushBuffer();
		}
	}

}
