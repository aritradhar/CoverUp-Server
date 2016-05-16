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
	
	public MainServer() throws IOException {
		super();
		
		this.sharedSecretMap = new HashMap<>();
		
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\workspace_Mars\\UndergroundClient\\codes.bin"));
		String str = "";
		this.codes = new HashSet<>();

		while((str = br.readLine()) != null)
			codes.add(str);

		br.close();
		keyGeneration();
		
		System.out.println("Started...");


		// TODO Auto-generated constructor stub
	}

	private void keyGeneration()
	{
		Curve25519KeyPair keypair = Curve25519.getInstance("best").generateKeyPair();
		this.publicKey = keypair.getPublicKey();
		this.privateKey = keypair.getPrivateKey();
	}
	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Get request to this server is not supported");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		String flag = request.getParameter("flag");
		
		System.err.println("Total connections " + Stats.TOTAL_CONNECTIONS);
		System.err.println("Live connection " + Stats.LIVE_CONNECTIONS);
		
		if(flag == null)
		{
			response.getWriter().append("No valid parameter");
		}

		else if(flag.equals("init"))
		{
			
			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;
			
			String code = request.getParameter("code");
			if(this.codes.contains(code))
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
			String responseStr = "Total connection : " + Stats.TOTAL_CONNECTIONS + "\n live connections : " + Stats.LIVE_CONNECTIONS;
			response.getWriter().append(responseStr);
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
		
		else if(flag.equals("end"))
		{
			Stats.LIVE_CONNECTIONS--;
			
			response.getWriter().append("Connection terminated");
			response.flushBuffer();
		}
	}

}
