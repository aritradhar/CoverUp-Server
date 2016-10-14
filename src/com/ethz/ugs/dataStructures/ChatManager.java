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

/**
 * @author Aritra
 *
 */
public class ChatManager {


	private Map<String, List<byte[]>> AddressChatDataMap;


	public ChatManager() {
		this.AddressChatDataMap = new HashMap<>();
	}

	public byte[] getChat(String publicAddress)
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

	public void addChat(String publicAddress, byte[] data)
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
