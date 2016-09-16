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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

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
	    	byte[] toSend = getEncSlice(postBody);
	    	
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
	
	public static byte[] getEncSlice(String postBody) throws InvalidKeyException, InvalidAlgorithmParameterException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		long start = System.nanoTime();

		//0/1,slice_index, slice_id, key:padding
		String fountainIdString = postBody.split(":")[0];
		String[] fountains = fountainIdString.split(",");

		int sliceIndex = Integer.parseInt(fountains[1]);
		//3rd element is the requested id
		String intrSliceId = fountains[2];
		
		
		String sliceData = InitialGen.sdm.getSlice(intrSliceId, sliceIndex);
		byte[] sliceDataBytes = null;

		if(sliceData.equals(SliceManager.INVALID_SLICE_FILE) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			sliceDataBytes = new byte[ENV.FOUNTAIN_CHUNK_SIZE];
			Arrays.fill(sliceDataBytes, ENV.PADDING_DETERMINISTIC_BYTE);
			
			return null;
		}

		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);
		
		//4th element is the AES key
		byte[] aesKeyByte = Base64.getDecoder().decode(fountains[3]);
		byte[] iv = new byte[16];	  
		//bad idea
		Arrays.fill(iv, (byte)0x00);
		SecretKeySpec aesKey = new SecretKeySpec(aesKeyByte, "AES");
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
					
		byte[] sliceIndeBytes = java.nio.ByteBuffer.allocate(Integer.BYTES).putInt(sliceIndex).array();
		byte[] sliceidBytes = intrSliceId.getBytes();
		
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] cipherText = cipher.doFinal(sliceDataBytes);      
        
        
        byte[] toSendWOpadding = new byte[ENV.INTR_MARKER_LEN + sliceIndeBytes.length + sliceidBytes.length + cipherText.length];
        byte[] magicBytes = new byte[ENV.INTR_MARKER_LEN];
        Arrays.fill(magicBytes, ENV.INTR_MARKER);
        System.arraycopy(magicBytes, 0, toSendWOpadding, 0, magicBytes.length);
        System.arraycopy(sliceidBytes, 0, toSendWOpadding, magicBytes.length, sliceidBytes.length);
        System.arraycopy(sliceIndeBytes, 0, toSendWOpadding, magicBytes.length + sliceidBytes.length, sliceIndeBytes.length);
        System.arraycopy(cipherText, 0, toSendWOpadding, magicBytes.length + sliceidBytes.length + sliceIndeBytes.length, cipherText.length);
        
        byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - toSendWOpadding.length];
        if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);
        byte[] toSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
        System.arraycopy(toSendWOpadding, 0, toSend, 0, toSendWOpadding.length);
        System.arraycopy(padding, 0, toSend, toSendWOpadding.length, padding.length);
        
		long end = System.nanoTime();
		MainServer.logger.info("get slice prob : " + (end - start)  + " ns");
		return toSend;
	}
}
