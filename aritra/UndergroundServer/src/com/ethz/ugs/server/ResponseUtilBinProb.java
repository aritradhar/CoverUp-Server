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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

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
import com.sun.xml.internal.ws.model.RuntimeModelerException;

/**
 * @author Aritra
 *
 */
public class ResponseUtilBinProb {

	public static SecureRandom rand = new SecureRandom();


	/**
	 * Broadcast
	 * @param request
	 * @param response
	 * @param privateKey
	 * @param key
	 * @param iv
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static void dropletPleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] key, byte[] iv) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime();

		SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		byte[] randMessage = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		Arrays.fill(randMessage, (byte) 0x00);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
		byte[] cipherText = cipher.doFinal(randMessage);

		//garbage
		if( Math.random() <= ENV.PROB_THRESHOLD )
		{
			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");
			if(MainServer.clientState.containSSLId(sslId))
			{
				byte[] postBody = null;
				byte[] toSend = getEncSlice(request, postBody, privateKey);

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
		
		//droplet
		else
		{
			ResponseUtilBin.dropletPleaseBin(request, response, privateKey, false);

			long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin Prob : " + (end - start)  + " ns");
			response.flushBuffer();
		}


	}


	/**
	 * Interactive 
	 * @param request
	 * @param response
	 * @param privateKey
	 * @param key
	 * @param iv
	 * @param postBody
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static void dropletPleaseIntrBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] key, byte[] iv, byte[] postBody) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime();

		//garbage -> interactive droplet
		if( Math.random() <= ENV.PROB_THRESHOLD )
		{
			byte[] toSend = getEncSlice(request, postBody, privateKey);
			
			//send garbage
			if(toSend == null)
			{
				SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
				IvParameterSpec ivSpec = new IvParameterSpec(iv);

				byte[] randMessage = new byte[ENV.FIXED_PACKET_SIZE_BIN];
				rand.nextBytes(randMessage);
				Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
				cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
				byte[] cipherText = cipher.doFinal(randMessage);
				
				OutputStream out = response.getOutputStream();
				out.write(cipherText);
				out.flush();
				out.close();
			}
			//everything is going great. Send the encrypted slice 
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
		//normal droplet
		else
		{
			ResponseUtilBin.dropletPleaseBin(request, response, privateKey, false);

			long end = System.nanoTime();
			MainServer.logger.info("Droplet Bin Prob : " + (end - start)  + " ns");
			response.flushBuffer();
		}
	}

	/**
	 * Get encrypted slice data with signature :D
	 * @param request
	 * @param postBody
	 * @return
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] getEncSlice(HttpServletRequest request, byte[] postBody, byte[] privateKey) throws InvalidKeyException, InvalidAlgorithmParameterException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		long start = System.nanoTime();

		boolean flag = false;
		//0/1,slice_index, slice_id, key:padding

		int sliceIndex = -1;
		byte[] aesKeyByte = null;

		String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");

		if(postBody == null && MainServer.clientState.containSSLId(sslId))
		{		
			aesKeyByte = MainServer.clientState.getkey(sslId);
			flag = true;
		}
		else if(postBody != null)
		{
			//0x00/0x01 (1) | reserved (3) | key (16) | len (4) | slice id (8 * n)| 
			aesKeyByte = new byte[ENV.AES_KEY_SIZE];
			System.arraycopy(postBody, 4, aesKeyByte, 0, ENV.AES_KEY_SIZE);
			byte[] lenBytes = new byte[4];
			System.arraycopy(postBody, ENV.AES_KEY_SIZE + 4, lenBytes, 0, 4);
			int len = ByteBuffer.wrap(lenBytes).getInt();
			System.out.println("intr len : " + len);
			int numSliceId = len / 8;

			List<Long> sliceIds = new ArrayList<>();
			for(int i = 0; i < numSliceId; i++)
			{
				byte[] sliceIdBytes = new byte[8];
				System.arraycopy(postBody, ENV.AES_KEY_SIZE + 8 + i * 8, sliceIdBytes, 0, 8);
				long sliceId = ByteBuffer.wrap(sliceIdBytes).getLong();
				sliceIds.add(sliceId);
			}
			MainServer.clientState.addState(sslId, sliceIds, aesKeyByte);
		}

		String sliceData = null;

		long sliceId = 0x00;
		try
		{
			sliceId = MainServer.clientState.getAState(sslId);
		}
		catch(RuntimeException ex)
		{
			//in this case send the garbage
			if(ex.getMessage() != null && ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_EMPTY_STATE_TABLE))
			{
				return null;
			}
		}
		sliceIndex = MainServer.clientState.getState(sslId, sliceId);
		sliceData = InitialGen.sdm.getSlice(sliceId, sliceIndex);

		byte[] sliceDataBytes = null;

		//in case the slice index is oveflown, remove it from the client state table
		if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			MainServer.clientState.removeState(sslId, sliceId);		
			return sliceDataBytes;
		}

		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);

		byte[] iv = new byte[ENV.AES_IV_SIZE];	  
		//generate a secure IV
		//rand.nextBytes(iv);
		//bad idea but for now
		Arrays.fill(iv, (byte) 0x00);
		SecretKeySpec aesKey = new SecretKeySpec(aesKeyByte, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		byte[] sliceIndexBytes = ByteBuffer.allocate(Integer.BYTES).putInt(sliceIndex).array();
		byte[] sliceIdBytes = ByteBuffer.allocate(Long.BYTES).putLong(sliceId).array();
		byte[] sliceDatalenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(sliceDataBytes.length).array();

		//IV (16) | packet len (4) | seedlen (4) ->0 | Magic (8) | Data | Padding
		//Data -> slice id (8) | slice index (4) | slice_data_len (4) | slice data (n) | signature (64)
		
		byte[] packetlenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(0x00).array();

	
		//TODO old structure. Needs to incorporate signature and IV
		byte[] toSendWOpadding = new byte[24 + ENV.INTR_MARKER_LEN + sliceDataBytes.length];
		
		byte[] magicBytes = new byte[ENV.INTR_MARKER_LEN];
		Arrays.fill(magicBytes, ENV.INTR_MARKER);
		int tillNow = 0;
		System.arraycopy(packetlenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(seedLenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(magicBytes, 0, toSendWOpadding, tillNow, magicBytes.length);
		tillNow += magicBytes.length;
		System.arraycopy(sliceIdBytes, 0, toSendWOpadding, tillNow, 8);
		tillNow += 8;
		System.arraycopy(sliceIndexBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(sliceDatalenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(sliceDataBytes, 0, toSendWOpadding, tillNow, sliceDataBytes.length);

		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - toSendWOpadding.length];
		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);
		byte[] toSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		System.arraycopy(toSendWOpadding, 0, toSend, 0, toSendWOpadding.length);
		System.arraycopy(padding, 0, toSend, toSendWOpadding.length, padding.length);


		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
		byte[] encryptedSlicePacket = cipher.doFinal(toSend);      
		
		//increase state by 1
		if(flag)
		{
			try
			{
				MainServer.clientState.incrementSeate(sslId, sliceId);
			}
			catch(RuntimeModelerException ex)
			{
				if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING) 
						|| ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING))
					
					return null;
			}
		}
		flag = false;

		long end = System.nanoTime();
		MainServer.logger.info("get slice prob : " + (end - start)  + " ns");
		return encryptedSlicePacket;
	}
}