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
package com.ethz.ugs.dataStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ethz.ugs.server.ENV;

/**
 * @author Aritra
 *
 */
public class ChatManager {


	private Map<String, List<byte[]>> AddressChatDataMap;
	private Map<String, String> sslIfPublicAddressMap;


	public ChatManager() {
		this.AddressChatDataMap = new HashMap<>();
		this.sslIfPublicAddressMap = new HashMap<>();
	}
	
	public boolean containSSLId(String sslId)
	{
		return this.sslIfPublicAddressMap.containsKey(sslId);
	}
	
	public byte[] getChat(String sslId)
	{
		if(!this.sslIfPublicAddressMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		String publicAddress = this.sslIfPublicAddressMap.get(sslId);
		return getChatbyAddress(publicAddress);
	}

	public byte[] getChatbyAddress(String publicAddress)
	{
		if(!AddressChatDataMap.containsKey(publicAddress))
			return null;
		List<byte[]> chatData = this.AddressChatDataMap.get(publicAddress);

		if(chatData.size() == 0)
			return null;

		byte[] dataToRet = chatData.get(0);
		chatData.remove(0);

		return dataToRet;
	}
	
	public void addChat(String sslId, String publicAddress, byte[] data)
	{
		sslIfPublicAddressMap.put(sslId, publicAddress);
		this.addChatByAddress(publicAddress, data);
	}
	
	public void addChat(String sslId, byte[] data)
	{
		String publicAddress = this.sslIfPublicAddressMap.get(sslId);
		this.addChatByAddress(publicAddress, data);
	}

	public void addChatByAddress(String publicAddress, byte[] data)
	{
		if(!AddressChatDataMap.containsKey(publicAddress))
		{
			List<byte[]> chataData = new ArrayList<>();
			chataData.add(data);
			this.AddressChatDataMap.put(publicAddress, chataData);

		}
		else
		{
			List<byte[]> chataData = AddressChatDataMap.get(publicAddress);
			chataData.add(data);
			this.AddressChatDataMap.put(publicAddress, chataData);
		}

	}
}
