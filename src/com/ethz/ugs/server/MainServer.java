package com.ethz.ugs.server;

import static net.fec.openrq.parameters.ParameterChecker.minDataLength;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import com.ethz.dataStructures.SiteMap;

import net.fec.openrq.ArrayDataEncoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;

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
		
		//initialize site map
		//random initialization for testing
		SiteMap.randomInitialization(20);
		
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
			StringBuffer responseStr = new StringBuffer("Total connection : " + Stats.TOTAL_CONNECTIONS + "\nlive connections : " + Stats.LIVE_CONNECTIONS + "\n");
			responseStr.append("Unique IP addresses\n");
			
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
		
		//request for sending broadcast json
		else if(flag.equals("broadCastjson"))
		{
			Stats.TOTAL_CONNECTIONS++;
			Stats.LIVE_CONNECTIONS++;
			
			String requestBody = ServerUtil.GetBody(request);
			
			//System.out.println("Body " + requestBody);
			
			JSONObject jObject = null;
			if(requestBody.length() == 0)
				jObject = ServerUtil.broadcastJson(this.broadCastMessage, this.publicKey, this.privateKey);
			
				
			else if(requestBody.equals("tableRequest"))
				jObject = new JSONObject(SiteMap.SITE_MAP);
			
			//test
			/*JSONObject tmp = new JSONObject(jObject.toString());
			Iterator<String> keys = tmp.keys();
			while(keys.hasNext())
			{
				String key = keys.next();
				String value = tmp.getString(key);
				
				System.out.println("key : " + key + " | value : " + value);
			}
			*/
			
			//System.out.println(jObject.toString());
			
			response.getWriter().append(jObject.toString(2));
			response.flushBuffer();
			
		}
		
		//request for the sitemap table
		else if(flag.equals("tableRequest"))
		{
			JSONObject jObject = new JSONObject(SiteMap.SITE_MAP);
			//System.out.println(jObject.toString(2));
			response.getWriter().append(jObject.toString(2));
			response.flushBuffer();
		}
		
		else if(flag.equals("getFountain"))
		{
			JSONObject jObject = new JSONObject();
			
			byte[] data = new byte[1024];
			Arrays.fill(data, (byte)0x01);
			
			int dataLen = data.length;
			if (dataLen < minDataLength())
	            throw new IllegalArgumentException("data length is too small");
	        if (dataLen > ENV.MAX_DATA_LEN)
	            throw new IllegalArgumentException("data length is too large");
	        
			FECParameters param = FECParameters.deriveParameters(dataLen, ENV.PAY_LEN, ENV.MAX_DEC_MEM);
			
			byte[] paramByte = param.asArray();
			String parambase64 = Base64.getUrlEncoder().encodeToString(paramByte);
			jObject.put("param", parambase64);
			
			JSONArray jArray = new JSONArray();
			
			ArrayDataEncoder enc = OpenRQ.newEncoder(data, param); 
			
			for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) 
			{
				//send individual packet to the client
				 for (EncodingPacket pac : sbEnc.sourcePacketsIterable()) 			 
					 jArray.put(Base64.getUrlEncoder().encodeToString(pac.asArray()));

			}
			jObject.put("packet", jArray);
			
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
