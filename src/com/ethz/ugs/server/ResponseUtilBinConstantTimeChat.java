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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tukaani.xz.simple.PowerPC;

import com.ethz.ugs.dataStructures.ClientState;
import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.test.InitialGen;
import com.sun.xml.internal.ws.model.RuntimeModelerException;

/**
 * @author Aritra
 *
 */
public class ResponseUtilBinConstantTimeChat {

	public static SecureRandom rand = new SecureRandom();
	public static Random guRand = new Random();

	/**
	 * Broadcast
	 * Fixed time execution
	 * Always calculate the execution time before writing the bytes on the line
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
	public static void dropletPleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime(), end = 0;

		long additionalDelay = (long) ((Math.abs(Math.round(rand.nextGaussian() * 3 + 12))) * Math.pow(10, 6));
				
		OutputStream out = response.getOutputStream();
		//garbage
		if( Math.random() <= ENV.PROB_THRESHOLD )
		{
			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");

			if(MainServer.chatManager.containSSLId(sslId))
			{
				byte[] toSend = MainServer.chatManager.getChat(sslId);

				if(toSend == null)	
				{
					//send garbage in this case
					byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					rand.nextBytes(garbageReturn);
					
					//additional delay start
					long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
					try {
						TimeUnit.NANOSECONDS.sleep(offset);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					end = System.nanoTime();
					//additional delay end
					
					out.write(garbageReturn);
				}
				//chat data :D
				else	
				{			
					//additional delay start
					long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
					try {
						TimeUnit.NANOSECONDS.sleep(offset);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					end = System.nanoTime();
					//additional delay end					
					out.write(toSend);
				}
				
			}
			else
			{
				byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
				rand.nextBytes(garbageReturn);	
				
				//additional delay start
				long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
				try {
					TimeUnit.NANOSECONDS.sleep(offset);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				end = System.nanoTime();
				//additional delay end
				
				out.write(garbageReturn);
			}
			
			MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}

		//droplet
		else
		{
			byte[] packetToSend = ResponseUtilBin.dropletPleaseBinNew(request, privateKey, null);

			//additional delay start
			long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
			try {
				TimeUnit.NANOSECONDS.sleep(offset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			end = System.nanoTime();
			//additional delay end
			out.write(packetToSend);
			MainServer.logger.info("Droplet noInt packet : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}
	}
		
	/**
	 * Serves chat data
	 * @param request
	 * @param response
	 * @param postBody
	 */
	public static void dropletPleaseChatBin(HttpServletRequest request, HttpServletResponse response, byte[] postBody) 
	{
		long start = System.nanoTime(), end = 0;
		long additionalDelay = (long) ((Math.abs(Math.round(rand.nextGaussian() * 3 + 12))) * Math.pow(10, 6));

		
		//0x00/0x01 (1) | reserved (3) | p1 | p2 | ...
		//<p_i = sr//R_adder(8) | S_addr(8) | iv(16) | len(4) | enc_Data(n) | sig(64) (on 0|1|2|3|4)
		
		int pointer = 4;
		
		while(true)
		{
			//reached to the end
			if(pointer >= postBody.length)
				break;
			
			try
			{
			//get the target address for storing in the chat manager class
			byte[] targetAddressBytes = new byte[8];
			System.arraycopy(postBody, 4, targetAddressBytes, 0, 8);
			
			int newPointer = pointer + 16; //traverse source and dest address
			byte[] datalenBytes = new byte[4]; //data len
			System.arraycopy(postBody, newPointer, datalenBytes, 0, 4);
			int datalen = ByteBuffer.wrap(datalenBytes).getInt();
			newPointer += 4; //add offset for datalen
			
			newPointer += datalen; //add offset for data length
			newPointer += 64; //add offset for ed25519 signature length
			
			byte[] dataChunk = new byte[newPointer - pointer + 1];
			System.arraycopy(postBody, pointer, dataChunk, 0, dataChunk.length);
			
			MainServer.chatManager.addChat(Base64.getUrlEncoder().encodeToString(targetAddressBytes), dataChunk);		
			
			pointer = newPointer;
			}
			catch(Exception ex)
			{
				//mostly a wrongly formatted post body. Simply exit
				ex.printStackTrace(System.out);
			}
		}
		
		
		//additional delay start
		long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
		try {
			TimeUnit.NANOSECONDS.sleep(offset);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		end = System.nanoTime();
		
		MainServer.logger.info("Chat packet : " + (end - start)  + " ns");
		//additional delay end
		
	}
}