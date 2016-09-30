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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ethz.ugs.server.ENV;

/**
 * @author Aritra
 *
 */
public class ClientState {

	public Map<String, ClientStateDataStructure> stateMap;
	
	public ClientState()
	{
		this.stateMap = new ConcurrentHashMap<>();
	}
	
	public boolean containSSLId(String sslId)
	{
		return this.stateMap.containsKey(sslId);
	}
	
	public void addState(String sslId, List<Long> sliceIds)
	{
		this.stateMap.put(sslId, new ClientStateDataStructure(sliceIds, null));
	}
	
	public void addState(String sslId, List<Long> sliceIds, byte[] key)
	{
		this.stateMap.put(sslId, new ClientStateDataStructure(sliceIds, key));
	}
	
	public byte[] getkey(String sslId) throws RuntimeException
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		return this.stateMap.get(sslId).key;
	}
	
	public int getState(String sslId, long sliceId) throws RuntimeException
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		int state = cds.getState(sliceId);
		if(state == -1)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SLICE_ID_MISSING);
		
		return state;
	}
	
	public void setState(String sslId, long sliceId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		int state = cds.getState(sliceId);
		if(state == -1)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SLICE_ID_MISSING);
		
		cds.setState(sliceId, state);
	}
	
	public void setState(String sslId, List<Long> sliceIds) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		
		for(long sliceId : sliceIds)
		{
			int state = cds.getState(sliceId);
			if(state == -1) 
				continue;
			cds.setState(sliceId, state);
		}
	}

	public void incrementSeate(String sslId, long sliceId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		int state = cds.getState(sliceId);
		if(state == -1)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SLICE_ID_MISSING);
		
		cds.incrementState(sliceId);
	}
	
	public void incrementSeate(String sslId, List<Long> sliceIds) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		
		for(long sliceId : sliceIds)
		{
			int state = cds.getState(sliceId);
			if(state == -1)
				continue;
			cds.incrementState(sliceId);
		}
	}
	
	public void removeState(String sslId, List<Long> sliceIds) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		for(long sliceId : sliceIds)
			cds.removeState(sliceId);
	}
	
	public void removeState(String sslId, long sliceId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		cds.removeState(sliceId);
	}
	
	public long getAState(String sslId) throws RuntimeException 
	{
		if(!this.stateMap.containsKey(sslId))
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING);
		
		ClientStateDataStructure cds = this.stateMap.get(sslId);
		if(cds.clientStateMap.size() == 0)
			throw new RuntimeException(ENV.EXCEPTION_MESSAGE_EMPTY_STATE_TABLE);
		
		return cds.clientStateMap.keySet().iterator().next();
	}
}


class ClientStateDataStructure
{
	public Map<Long, Integer> clientStateMap;
	
	byte[] key;
	public ClientStateDataStructure(List<Long> sliceIds, byte[] key)
	{
		this.clientStateMap = new HashMap<>();
		if(sliceIds != null)
		{
			for(long sliceId : sliceIds)
				this.clientStateMap.put(sliceId, 0);
		}
		
		if(key != null)
			this.key = key;
	}
	
	public int getState(long sliceId)
	{
		return this.clientStateMap.containsKey(sliceId) ? this.clientStateMap.get(sliceId) : -1;
	}
	
	public void setState(long sliceId, int state)
	{
		 this.clientStateMap.put(sliceId, state);
	}
	
	public void incrementState(long sliceId)
	{
		if(!this.clientStateMap.containsKey(sliceId))
			this.clientStateMap.put(sliceId, 0);
		else
			this.clientStateMap.put(sliceId, this.clientStateMap.get(sliceId) + 1);
	}
	
	public void removeState(long sliceId)
	{
		if(this.clientStateMap.containsKey(sliceId))
			this.clientStateMap.remove(sliceId);
	}
}
