package com.ethz.ugs.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.ugs.compressUtil.CompressUtil;
import com.ethz.ugs.dataStructures.SiteMap;

public class ResponseUtilBin {
	
	public static SecureRandom rand = new SecureRandom();
	
	/**
	 * P = fixed packet size
	 * <p>
	 * table-> table_len | table | signature | padding |</p><p>
	 * 				4		 x		  64	   P-(68+x)</p>
	 * @param request
	 * @param response
	 * @param privateKey
	 * @throws IOException
	 */
	
	public static void tablePleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) throws IOException
	{
		String theTable = SiteMap.getTable();

		byte[] theTableBytes = theTable.getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = null;

		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(theTableBytes);
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception happed in crypto part!!");
			response.flushBuffer();
		}
		
		//P = fixed packet size
		//table-> table_len | table | signature | padding |
		//			4			x		64		  P-(68+x)
		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE];
		byte[] tableLen = ByteBuffer.allocate(Integer.BYTES).putInt(theTableBytes.length).array();
		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE - tableLen.length - theTableBytes.length - signatureBytes.length];
		rand.nextBytes(padding);
		
		System.arraycopy(tableLen, 0, packetToSend, 0, tableLen.length);
		System.arraycopy(theTableBytes, 0, packetToSend, tableLen.length, theTableBytes.length);
		System.arraycopy(signatureBytes, 0, packetToSend, tableLen.length + theTableBytes.length, signatureBytes.length);
		System.arraycopy(padding, 0, packetToSend, tableLen.length + theTableBytes.length + signatureBytes.length, packetToSend.length);
		

		response.getOutputStream().write(packetToSend);
		response.addHeader("x-flag", "0");
		System.out.println("len (byte) :: " + packetToSend.length);
		
		response.flushBuffer();
	}

}
