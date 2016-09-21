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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.test.InitialGen;

/**
 * @author Aritra
 *
 */
public class ResponseUtilBinProb {

	public static SecureRandom rand = new SecureRandom();
	
	//Client SSL session id -> AES key
	public static Map<String, byte[]> CLIENT_KEY_MAP = new HashMap<>();
	public static Map<String, SliceIdIndexPair> CLIENT_PAIR_MAP = new HashMap<>();
	
	
	public static void dropletPleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] key, byte[] iv) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime();
		
		SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
	    
	    byte[] randMessage = new byte[ENV.FIXED_PACKET_SIZE_BIN];
    	rand.nextBytes(randMessage);
    	Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] cipherText = cipher.doFinal(randMessage);
        
	    if( Math.random() <= ENV.PROB_THRESHOLD )
	    {
	    	
	    	String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(CLIENT_KEY_MAP.containsKey(sslId))
			{
				String postBody = null;
				byte[] toSend = getEncSlice(request, postBody);
		    	
		    	if(toSend == null)
		    	{
		    		OutputStream out = response.getOutputStream();
					out.write(cipherText);
					out.flush();
					out.close();
		    	}
		    	else
		    	{
		    		OutputStream out = response.getOutputStream();
		    		out.write(toSend);
		    		out.flush();
		    		out.close();
		    	}
			}
			
			else
			{
	    	OutputStream out = response.getOutputStream();
			out.write(cipherText);
			out.flush();
			out.close();
			}
			long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin : " + (end - start)  + " ns");
			response.flushBuffer();
	    }
	    else
	    {
	    	ResponseUtilBin.dropletPleaseBin(request, response, privateKey, false);
	    	
	    	long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin Prob : " + (end - start)  + " ns");
			response.flushBuffer();
	    }
	    
	    
	}
	
	
	public static void dropletPleaseIntrBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] key, byte[] iv, String postBody) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime();
				
		SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
	    
	    byte[] randMessage = new byte[ENV.FIXED_PACKET_SIZE_BIN];
	    rand.nextBytes(randMessage);
    	Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] cipherText = cipher.doFinal(randMessage);
        
        if( Math.random() <= ENV.PROB_THRESHOLD )
	    {
	    	OutputStream out = response.getOutputStream();
			out.write(cipherText);
			out.flush();
			out.close();
			
			long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin : " + (end - start)  + " ns");
			response.flushBuffer();
	    }
	    else
	    {
	    	byte[] toSend = getEncSlice(request, postBody);
	    	
	    	if(toSend == null)
	    	{
	    		OutputStream out = response.getOutputStream();
				out.write(cipherText);
				out.flush();
				out.close();
	    	}
	    	else
	    	{
	    		OutputStream out = response.getOutputStream();
	    		out.write(toSend);
	    		out.flush();
	    		out.close();
	    	}
	    	long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin Intr Prob : " + (end - start)  + " ns");
			response.flushBuffer();
	    }
	}
	
	public static void dropletPleaseIntrBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] key, byte[] iv, byte[] postBody) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime();
				
		SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
	    
	    byte[] randMessage = new byte[ENV.FIXED_PACKET_SIZE_BIN];
	    rand.nextBytes(randMessage);
    	Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] cipherText = cipher.doFinal(randMessage);
        
        if( Math.random() <= ENV.PROB_THRESHOLD )
	    {
	    	OutputStream out = response.getOutputStream();
			out.write(cipherText);
			out.flush();
			out.close();
			
			long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin : " + (end - start)  + " ns");
			response.flushBuffer();
	    }
	    else
	    {
	    	byte[] toSend = getEncSlice(request, postBody);
	    	
	    	if(toSend == null)
	    	{
	    		OutputStream out = response.getOutputStream();
				out.write(cipherText);
				out.flush();
				out.close();
	    	}
	    	else
	    	{
	    		OutputStream out = response.getOutputStream();
	    		out.write(toSend);
	    		out.flush();
	    		out.close();
	    	}
	    	long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin Intr Prob : " + (end - start)  + " ns");
			response.flushBuffer();
	    }
	}
	
	public static byte[] getEncSlice(HttpServletRequest request, String postBody) throws InvalidKeyException, InvalidAlgorithmParameterException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		long start = System.nanoTime();

		boolean flag = false;
		//0/1,slice_index, slice_id, key:padding
		String fountainIdString = null;
		String[] fountains = null;
		int sliceIndex = -1;
		String intrSliceId = null;
		byte[] aesKeyByte = null;
		
		String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
		
		if(postBody == null && CLIENT_PAIR_MAP.containsKey(sslId) && CLIENT_KEY_MAP.containsKey(sslId))
		{
			SliceIdIndexPair pair = CLIENT_PAIR_MAP.get(sslId);
			intrSliceId = pair.sliceid;
			sliceIndex = pair.sliceIndex;
			aesKeyByte = CLIENT_KEY_MAP.get(sslId);
			flag = true;
		}
		else
		{
			//0/1,slice_index, slice_id, key:padding
			fountainIdString = postBody.split(":")[0];
			fountains = fountainIdString.split(",");

			 sliceIndex = Integer.parseInt(fountains[1]);
			//3rd element is the requested id
			intrSliceId = fountains[2];
			//4th element is the AES key
			aesKeyByte = Base64.getDecoder().decode(fountains[3]);
			
			//does not matter if the ssl id is already in the map or not. This is handled as a new connection
			CLIENT_KEY_MAP.put(sslId, aesKeyByte);
			CLIENT_PAIR_MAP.put(sslId, new SliceIdIndexPair(intrSliceId, sliceIndex));
		}
		
		String sliceData = null;
		try
		{
			Long sliceID = Long.parseLong(intrSliceId);
			sliceData = InitialGen.sdm.getSlice(sliceID, sliceIndex);
		}
		catch(Exception ex)
		{
			sliceData = InitialGen.sdm.getSlice(intrSliceId, sliceIndex);
		}
		 
								
		byte[] sliceDataBytes = null;

		if(sliceData.equals(SliceManager.INVALID_SLICE_FILE) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			sliceDataBytes = new byte[ENV.FOUNTAIN_CHUNK_SIZE];
			Arrays.fill(sliceDataBytes, ENV.PADDING_DETERMINISTIC_BYTE);
			return null;
		}

		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);
		
		byte[] iv = new byte[16];	  
		//bad idea
		Arrays.fill(iv, (byte)0x00);
		SecretKeySpec aesKey = new SecretKeySpec(aesKeyByte, "AES");
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
					
		byte[] sliceIndeBytes = java.nio.ByteBuffer.allocate(Integer.BYTES).putInt(sliceIndex).array();
		byte[] sliceidBytes = intrSliceId.getBytes(StandardCharsets.UTF_8);
		
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] cipherText = cipher.doFinal(sliceDataBytes);      
        
        //packet len(4) | seedlen (4) ->0 | Magic (16) | Data | Padding
        byte[] packetlenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();
        byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(0).array();
        		
        byte[] toSendWOpadding = new byte[packetlenBytes.length + seedLenBytes.length + ENV.INTR_MARKER_LEN + sliceIndeBytes.length + sliceidBytes.length + cipherText.length];
        byte[] magicBytes = new byte[ENV.INTR_MARKER_LEN];
        Arrays.fill(magicBytes, ENV.INTR_MARKER);
        int tillNow = 0;
        System.arraycopy(packetlenBytes, 0, toSendWOpadding, tillNow, packetlenBytes.length);
        tillNow += packetlenBytes.length;
        System.arraycopy(seedLenBytes, 0, toSendWOpadding, tillNow, seedLenBytes.length);
        tillNow += seedLenBytes.length;
        System.arraycopy(magicBytes, 0, toSendWOpadding, tillNow, magicBytes.length);
        tillNow += magicBytes.length;
        System.arraycopy(sliceidBytes, 0, toSendWOpadding, tillNow, sliceidBytes.length);
        tillNow += sliceidBytes.length;
        System.arraycopy(sliceIndeBytes, 0, toSendWOpadding, tillNow, sliceIndeBytes.length);
        tillNow += sliceIndeBytes.length;
        System.arraycopy(cipherText, 0, toSendWOpadding, tillNow, cipherText.length);
        
        byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - toSendWOpadding.length];
        if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);
        byte[] toSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
        System.arraycopy(toSendWOpadding, 0, toSend, 0, toSendWOpadding.length);
        System.arraycopy(padding, 0, toSend, toSendWOpadding.length, padding.length);
        
        
        //increase slice index by 1
        if(flag)
        	CLIENT_PAIR_MAP.put(sslId, new SliceIdIndexPair(intrSliceId, sliceIndex + 1));
        flag = false;
        
		long end = System.nanoTime();
		MainServer.logger.info("get slice prob : " + (end - start)  + " ns");
		return toSend;
	}
	
	
	public static byte[] getEncSlice(HttpServletRequest request, byte[] postBody) throws InvalidKeyException, InvalidAlgorithmParameterException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		long start = System.nanoTime();

		boolean flag = false;
		//0/1,slice_index, slice_id, key:padding
		String fountainIdString = null;
		String[] fountains = null;
		int sliceIndex = -1;
		String intrSliceId = null;
		byte[] aesKeyByte = null;
		
		String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
		
		if(postBody == null && MainServer.clientState.containSSLId(sslId))
		{
			
			
			SliceIdIndexPair pair = CLIENT_PAIR_MAP.get(sslId);
			intrSliceId = pair.sliceid;
			sliceIndex = pair.sliceIndex;
			aesKeyByte = CLIENT_KEY_MAP.get(sslId);
			flag = true;
		}
		else if(postBody != null)
		{
			//0x00/0x01 (1) | reserved (3) | key (16) | len (4) | slice id (8 * n)| 
			System.arraycopy(postBody, 4, aesKeyByte, 0, 16);
			byte[] lenBytes = new byte[4];
			System.arraycopy(postBody, 20, lenBytes, 0, 4);
			int len = ByteBuffer.wrap(lenBytes).getInt();
			int numSliceId = len / 8;
			
			List<Long> sliceIds = new ArrayList<>();
			for(int i = 0; i < numSliceId; i++)
			{
				byte[] sliceIdBytes = new byte[8];
				System.arraycopy(postBody, 24 + i * 8, sliceIdBytes, 0, 8);
				long sliceId = ByteBuffer.wrap(sliceIdBytes).getLong();
				sliceIds.add(sliceId);
			}
			MainServer.clientState.addState(sslId, sliceIds, aesKeyByte);
		}
		
		String sliceData = null;
		try
		{
			Long sliceID = Long.parseLong(intrSliceId);
			sliceData = InitialGen.sdm.getSlice(sliceID, sliceIndex);
		}
		catch(Exception ex)
		{
			sliceData = InitialGen.sdm.getSlice(intrSliceId, sliceIndex);
		}
		 
								
		byte[] sliceDataBytes = null;

		if(sliceData.equals(SliceManager.INVALID_SLICE_FILE) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			sliceDataBytes = new byte[ENV.FOUNTAIN_CHUNK_SIZE];
			Arrays.fill(sliceDataBytes, ENV.PADDING_DETERMINISTIC_BYTE);
			return null;
		}

		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);
		
		byte[] iv = new byte[16];	  
		//bad idea
		Arrays.fill(iv, (byte)0x00);
		SecretKeySpec aesKey = new SecretKeySpec(aesKeyByte, "AES");
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
					
		byte[] sliceIndeBytes = java.nio.ByteBuffer.allocate(Integer.BYTES).putInt(sliceIndex).array();
		byte[] sliceidBytes = intrSliceId.getBytes(StandardCharsets.UTF_8);
		
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] cipherText = cipher.doFinal(sliceDataBytes);      
        
        //packet len(4) | seedlen (4) ->0 | Magic (16) | Data | Padding
        byte[] packetlenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();
        byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(0).array();
        		
        byte[] toSendWOpadding = new byte[packetlenBytes.length + seedLenBytes.length + ENV.INTR_MARKER_LEN + sliceIndeBytes.length + sliceidBytes.length + cipherText.length];
        byte[] magicBytes = new byte[ENV.INTR_MARKER_LEN];
        Arrays.fill(magicBytes, ENV.INTR_MARKER);
        int tillNow = 0;
        System.arraycopy(packetlenBytes, 0, toSendWOpadding, tillNow, packetlenBytes.length);
        tillNow += packetlenBytes.length;
        System.arraycopy(seedLenBytes, 0, toSendWOpadding, tillNow, seedLenBytes.length);
        tillNow += seedLenBytes.length;
        System.arraycopy(magicBytes, 0, toSendWOpadding, tillNow, magicBytes.length);
        tillNow += magicBytes.length;
        System.arraycopy(sliceidBytes, 0, toSendWOpadding, tillNow, sliceidBytes.length);
        tillNow += sliceidBytes.length;
        System.arraycopy(sliceIndeBytes, 0, toSendWOpadding, tillNow, sliceIndeBytes.length);
        tillNow += sliceIndeBytes.length;
        System.arraycopy(cipherText, 0, toSendWOpadding, tillNow, cipherText.length);
        
        byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - toSendWOpadding.length];
        if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);
        byte[] toSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
        System.arraycopy(toSendWOpadding, 0, toSend, 0, toSendWOpadding.length);
        System.arraycopy(padding, 0, toSend, toSendWOpadding.length, padding.length);
        
        
        //increase slice index by 1
        if(flag)
        	CLIENT_PAIR_MAP.put(sslId, new SliceIdIndexPair(intrSliceId, sliceIndex + 1));
        flag = false;
        
		long end = System.nanoTime();
		MainServer.logger.info("get slice prob : " + (end - start)  + " ns");
		return toSend;
	}
}

class SliceIdIndexPair
{
	public String sliceid;
	public int sliceIndex;
	
	public SliceIdIndexPair(String sliceid, int sliceIndex)
	{
		this.sliceid = sliceid;
		this.sliceIndex = sliceIndex;
	}
}