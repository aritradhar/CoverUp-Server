package com.ethz.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

/**
 * Servlet implementation class MainServer
 */
@WebServlet("/MainServer")
public class MainServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */

	public Set<String> codes;
	private byte[] sharedSecret;
	public byte[] publicKey;
	private byte[] privateKey;
	
	public MainServer() throws IOException {
		super();

		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\workspace_Mars\\UndergroundClient\\codes.bin"));
		String str = "";
		this.codes = new HashSet<>();

		while((str = br.readLine()) != null)
			codes.add(str);

		br.close();
		keyGeneration();
		
		System.out.println("Started...");


		// TODO Auto-generated constructor stub
	}

	private void keyGeneration()
	{
		Curve25519KeyPair keypair = Curve25519.getInstance("best").generateKeyPair();
		
	}
	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		String flag = request.getParameter("flag");
		String code = "";

		if(flag.equals("init"))
		{
			code = request.getParameter("code");
			if(this.codes.contains(code))
				response.getWriter().append("code authenticated ").append(request.getContextPath());
		}
		
		else if(flag.equals("ke"))
		{
			
		}
	}

}
