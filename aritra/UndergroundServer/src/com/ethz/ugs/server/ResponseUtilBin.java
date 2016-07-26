package com.ethz.ugs.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
import com.ethz.ugs.dataStructures.FountainTableRow;
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
		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		byte[] tableLen = ByteBuffer.allocate(Integer.BYTES).putInt(theTableBytes.length).array();
		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - tableLen.length - theTableBytes.length - signatureBytes.length];
		rand.nextBytes(padding);
		
		System.arraycopy(tableLen, 0, packetToSend, 0, tableLen.length);
		System.arraycopy(theTableBytes, 0, packetToSend, tableLen.length, theTableBytes.length);
		System.arraycopy(signatureBytes, 0, packetToSend, tableLen.length + theTableBytes.length, signatureBytes.length);
		System.arraycopy(padding, 0, packetToSend, tableLen.length + theTableBytes.length + signatureBytes.length, padding.length);
		

		response.getOutputStream().write(packetToSend);
		response.addHeader("x-flag", "0");
		System.out.println("len (byte) :: " + packetToSend.length);
		
		response.flushBuffer();
	}
	
	/**
	 * P = fixed packet size
	 * <br>
	 * table-> deoplet_len (4) | droplet (n) | signature (64) | url_len (4) | url (n_1) | f_id (8) | padding (p - 72 - n - n_1) |</br>
	 * 
	 * Signature is on droplet
	 * <br>
	 * droplet -> seedlen (4) | seed(n) | num_chunk (4) | datalen (4) | data (n)
	 * <br>
	 * @param request
	 * @param response
	 * @param privateKey
	 * @throws IOException
	 */
	
	public static void dropletPleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) throws IOException
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

			try
			{
				int urlId = Integer.parseInt(url);
				url = FountainTableRow.dropletLocUrlMap.get(urlId);
				
				if(url == null)
				{
					response.getWriter().append("Invalid fountain id");
					response.flushBuffer();
					
					return;
				}
			}
			catch(NullPointerException ex)
			{
				response.getWriter().append("Invalid fountain id");
				response.flushBuffer();
				
				return;
			}
			catch(Exception ex)
			{
				
			}
			try
			{
				dropletStr = SiteMap.getRandomDroplet(url);
			}
			catch(Exception ex)
			{
				response.getWriter().append(ex.getMessage());
				response.flushBuffer();
				
				return;
			}
			//System.out.println(dropletStr[0]);
		}
		
		System.err.println("Fountain served : " + url);
		
		JSONObject jObject = new JSONObject();


		JSONObject jObject2 = new JSONObject(dropletStr[0]);
		byte[] seedBytes = Base64.getUrlDecoder().decode(jObject2.getString("seed"));
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(seedBytes.length).array();
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(jObject2.getInt("num_chunks")).array();
		byte[] data = Base64.getUrlDecoder().decode(jObject2.getString("data"));
		byte[] dataLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
		
		
		byte[] dropletByte = new byte[seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length + data.length];
		
		System.arraycopy(seedLenBytes, 0, dropletByte, 0, seedLenBytes.length);
		System.arraycopy(seedBytes, 0, dropletByte, seedLenBytes.length, seedBytes.length);
		System.arraycopy(num_chunksBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length, num_chunksBytes.length);
		System.arraycopy(dataLenBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length, dataLenBytes.length);
		System.arraycopy(data, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length, data.length);
		
		byte[] dropletLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(dropletByte.length).array();
		
		byte[] signatureBytes = null;

		System.out.println("Droplet " + dropletByte.length);
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(dropletByte);
			System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception in signature calculation!");
			response.flushBuffer();
		}


		byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
		byte[] urlLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(urlBytes.length).array();
		byte[] f_idBytes = ByteBuffer.allocate(Long.BYTES).putLong(FountainTableRow.dropletLocUrlMapRev.get(url)).array();
		
		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - dropletLenBytes.length - dropletByte.length - signatureBytes.length - urlLenBytes.length - urlBytes.length - f_idBytes.length];
		rand.nextBytes(padding);
		
		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		
		System.arraycopy(dropletLenBytes, 0, packetToSend, 0, dropletLenBytes.length);
		System.arraycopy(dropletByte, 0, packetToSend, dropletLenBytes.length, dropletByte.length);
		System.arraycopy(signatureBytes, 0, packetToSend, dropletLenBytes.length + dropletByte.length, signatureBytes.length);
		System.arraycopy(urlLenBytes, 0, packetToSend, dropletLenBytes.length + dropletByte.length + signatureBytes.length, urlLenBytes.length);
		System.arraycopy(urlBytes, 0, packetToSend, dropletLenBytes.length + dropletByte.length + signatureBytes.length + urlLenBytes.length, urlBytes.length);
		System.arraycopy(f_idBytes, 0, packetToSend, dropletLenBytes.length + dropletByte.length + signatureBytes.length + urlLenBytes.length + urlBytes.length, f_idBytes.length);
		System.arraycopy(padding, 0, packetToSend, dropletLenBytes.length + dropletByte.length + signatureBytes.length + urlLenBytes.length + urlBytes.length + f_idBytes.length, padding.length);
		
		response.getOutputStream().write(packetToSend);
		
		response.addHeader("x-flag", "0");
		
		System.out.println("len (String) :: " + jObject.toString().length());	
		response.flushBuffer();
	}


}
