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
package com.ethz.ugs.test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONObject;

/**
 * @author Aritra
 *
 */
public class T {
	
	public static void main(String[] args) {
		
		byte[] b = Base64.getDecoder().decode("MC0CFQCSKaf1VxzqzqwqLScQKj4be/foLAIURkjIjFsu2Jmo8szJiCOULOAQtL4=");
		
		System.out.println(new String(b, StandardCharsets.UTF_8));
		System.out.println(new String(b, StandardCharsets.UTF_16));
		System.out.println(new String(b, StandardCharsets.US_ASCII));
		System.out.println(new String(b, StandardCharsets.ISO_8859_1));
	}

}
