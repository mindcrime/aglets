package com.ibm.net.protocol.rmi;

/*
 * @(#)URLConnectionForRMI.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import com.ibm.maf.*;
import com.ibm.maf.rmi.*;
import com.ibm.awb.misc.*;

import java.rmi.*;

import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

/**
 * An instance of this class creates a communication link between an
 * application and an atp server.
 * 
 * @version     1.10	$Date: 2001/07/28 06:34:15 $
 * @author      Mitsuru Oshima
 */

public class URLConnectionForRMI extends URLConnection {

	protected static boolean verbose = false;

	static AgentProfile agent_profile = null;

	/*
	 * 
	 */
	static {
		Resource res = Resource.getResourceFor("aglets");

		verbose = res.getBoolean("aglets.verbose", false);

		short one = (short)1;

		agent_profile = new AgentProfile(one,		// Java
		one,										// Aglets
		"Aglets", one,								// major,
		one,										// minor,
		one,										// serialization,
		null);
	} 

	/*
	 * 
	 */
	private InputStream _inputStream = null;

	// MAFAgentSystem_RMI agentsystem;
	MAFAgentSystem agentsystem;
	Properties request_properties = new Properties();

	/*
	 * Header field
	 */
	private Properties headers = new Properties();

	/**
	 * Create a new instance of this class.
	 * @param url a destination URL to which the application connects.
	 * The protocol is "atp".
	 */
	public URLConnectionForRMI(URL url) throws IOException {
		super(url);
	}
	/**
	 * Make a comminucation link with the destination.
	 * 
	 * @exception IOException if can not make a communication link.
	 */
	synchronized public void connect() throws IOException {
		if (connected) {
			return;
		} 
		agentsystem = MAFAgentSystem.getMAFAgentSystem(url.toString());

		// MAFAgentSystem_RMIClient.getMAFAgentSystem_RMI(url.toString());
		if (agentsystem == null) {
			throw new IOException("ConnectionFailed");
		} 
		connected = true;
	}
	public String getHeaderField(String key) {
		return headers.getProperty(key.toLowerCase());
	}
	public String getHeaderField(String key, String defValue) {
		String ret = getHeaderField(key);

		return ret == null ? defValue : ret;
	}
	/**
	 * Get an input stream of the communication link.
	 * @return an input stream.
	 * @exception IOException if the communication link has a problem.
	 */
	synchronized public InputStream getInputStream() throws IOException {
		if (_inputStream != null) {
			return _inputStream;
		} 
		connect();
		if (!connected) {
			return null;
		} 

		try {
			byte result[][] = agentsystem.fetch_class(null, url.toString(), 
													  agent_profile);

			headers.put("content-length", String.valueOf(result[0].length));
			if (result != null && result.length == 1) {
				_inputStream = new ByteArrayInputStream(result[0]);
			} 
			return _inputStream;
		} catch (Exception ex) {
			throw new IOException(ex.getClass().getName() + ":" 
								  + ex.getMessage());
		} 
	}
	public String getRequestProperty(String key) {
		return request_properties.getProperty(key);
	}
	/*
	 * Sets request parameters
	 */
	public void setRequestProperty(String key, String value) {
		if (connected) {
			throw new IllegalAccessError("Already connected");
		} 
		request_properties.put(key, value);
	}
}
