package com.ethz.ugs.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.ugs.compressUtil.CompressUtil;
import com.ethz.ugs.dataStructures.SiteMap;

public class ResponseUtil 
{
	
	public static void tablePlease(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) throws IOException
	{
		JSONObject jObject = new JSONObject();

		String theTable = SiteMap.getTable();


		byte[] theTableBytes = theTable.getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = null;
		String signatureBase64 = null;

		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(theTableBytes);
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
			signatureBase64 = Base64.getUrlEncoder().encodeToString(signatureBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception happed in crypto part!!");
			response.flushBuffer();
		}

		jObject.put("table", theTable);
		jObject.put("signature", signatureBase64);

		String responseString = jObject.toString();

		if(ENV.PADDING_ENABLE)
		{
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			String randomPadding = ServerUtil.randomString(padLen);
			jObject.put("pad", randomPadding);
		}

		if(ENV.ENABLE_COMPRESS)
		{
			byte[] bytes = jObject.toString().getBytes();

			OutputStream output = response.getOutputStream();
			output.write(CompressUtil.compress(bytes, ENV.COMPRESSION_PRESET));
			output.flush();
			output.close();
		}

		else
			response.getWriter().append(jObject.toString());

		response.flushBuffer();

	}
	
	public static void dropletPlease(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) throws IOException
	{
		String url = request.getParameter("url");

		String[] dropletStr = new String[2];
		if(url == null)
		{
			//response.getWriter().append("Request contains no url id");
			//response.flushBuffer();
			//return;
			dropletStr = SiteMap.getRandomDroplet(null);
			url = dropletStr[1];
		}	
		else
		{
			System.err.println("Request droplet url : " + url);

			dropletStr = SiteMap.getRandomDroplet(url);
			
			System.out.println(dropletStr[0]);
		}
		
		JSONObject jObject = new JSONObject();

		//sign droplet|url

		String dropletStrMod = dropletStr[0].concat(url);

		byte[] dropletByte = dropletStrMod.getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = null;
		String signatureBase64 = null;


		System.out.println("Droplet " + dropletByte.length);
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(dropletByte);

			System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));

			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
			signatureBase64 = Base64.getUrlEncoder().encodeToString(signatureBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception in signature calculation!");
			response.flushBuffer();
		}


		jObject.put("url", url);
		jObject.put("droplet", dropletStr[0]);
		jObject.put("signature", signatureBase64);	

		if(ENV.PADDING_ENABLE)
		{
			String responseString = jObject.toString();
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			String randomPadding = ServerUtil.randomString(padLen);
			jObject.put("pad", randomPadding);
		}
		
		
		if(ENV.ENABLE_COMPRESS)
		{
			byte[] bytes = jObject.toString().getBytes();

			OutputStream output = response.getOutputStream();
			output.write(CompressUtil.compress(bytes, ENV.COMPRESSION_PRESET));
			output.flush();
			output.close();
		}
		else
			response.getWriter().append(jObject.toString());

		response.flushBuffer();
	}
	
	public static void broadCastjson(HttpServletRequest request, HttpServletResponse response, String broadCastMessage, byte[] publicKey, byte[] privateKey) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		String requestBody = ServerUtil.GetBody(request);

		//System.out.println("Body " + requestBody);

		JSONObject jObject = null;
		if(requestBody.length() == 0)
			jObject = ServerUtil.broadcastJson(broadCastMessage, publicKey, privateKey);


		else if(requestBody.equals("tableRequest"))
			jObject = new JSONObject(SiteMap.SITE_MAP);



		if(ENV.ENABLE_COMPRESS)
		{
			byte[] bytes = jObject.toString(2).getBytes();

			OutputStream output = response.getOutputStream();
			output.write(CompressUtil.compress(bytes, ENV.COMPRESSION_PRESET));
			output.flush();
			output.close();
		}

		else
			response.getWriter().append(jObject.toString(2));

		response.flushBuffer();
	}
	public static void broadCast(HttpServletRequest request, HttpServletResponse response, String broadCastMessage) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		response.getWriter().append(broadCastMessage);
		response.flushBuffer();
	}
	
	public static void randB(HttpServletRequest request, HttpServletResponse response) throws IOException
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
	
	public static void rand(HttpServletRequest request, HttpServletResponse response) throws IOException
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
	
	public static void ke(HttpServletRequest request, HttpServletResponse response, byte[] publicKey, byte[] privateKey, byte[] sharedSecret, Map<String, byte[]> sharedSecretMap) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		String otherPublicKey = request.getParameter("pk");
		String sessionCode = request.getParameter("code");

		sharedSecret = Curve25519.getInstance("best").calculateAgreement(Base64.getUrlDecoder().decode(otherPublicKey), privateKey);
		response.getWriter().append(Base64.getUrlEncoder().encodeToString(publicKey));

		byte[] sharedSecretHash = null;
		try {
			sharedSecretHash = MessageDigest.getInstance("sha-256").digest(sharedSecret);
		}
		catch (NoSuchAlgorithmException e) {

		}

		sharedSecretMap.put(sessionCode, sharedSecretHash);

		System.out.println(Base64.getUrlEncoder().encodeToString(sharedSecretHash));
	}
}
