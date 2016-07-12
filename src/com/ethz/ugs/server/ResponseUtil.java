package com.ethz.ugs.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.ugs.dataStructures.SiteMap;

public class ResponseUtil 
{
	
	public static JSONObject tablePlease(byte[] privateKey) throws IOException
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
			return null;
		}
		
		jObject.put("table", theTable);
		jObject.put("signature", signatureBase64);
		
		if(ENV.PADDING_ENABLE)
		{
			String responseString = jObject.toString();
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			String randomPadding = ServerUtil.randomString(padLen);
			jObject.put("pad", randomPadding);
		}
		
		return jObject;
	}
	
	public static JSONObject dropletPlease(byte[] privateKey, String url) throws IOException
	{
		String dropletStr = SiteMap.getRandomDroplet(url);
		
		JSONObject jObject = new JSONObject();
		
		//sign droplet|url
		
		String dropletStrMod = dropletStr.concat(url);
		
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
			return null;
		}
		
		
		jObject.put("url", url);
		jObject.put("droplet", dropletStr);
		jObject.put("signature", signatureBase64);	
		
		if(ENV.PADDING_ENABLE)
		{
			String responseString = jObject.toString();
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			String randomPadding = ServerUtil.randomString(padLen);
			jObject.put("pad", randomPadding);
		}
		
		return jObject;
	}
}
