package com.ibm.maf.atp;

/*
 * @(#)AtpRequestImpl.java
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

import com.ibm.atp.AtpConstants;

import com.ibm.maf.AgentProfile;
import com.ibm.maf.Name;
import com.ibm.maf.MAFUtil;

import com.ibm.awb.misc.Resource;

import java.io.InputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import java.net.URL;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * @version     1.10	$Date :$
 * @author	Mitsuru Oshima
 */
public final class AtpRequestImpl implements AtpRequest {
	final static String ATP_VERSION = "ATP/0.1";

	private InputStream in;
	private int method = 0;
	private URL requestURI = null;
	private String requestLine = null;
	private Hashtable headers = new Hashtable();

	static AgentProfile agent_profile = null;

	static {
		short one = (short)1;

		agent_profile = new AgentProfile(one,		// Java
		one,										// Aglets
		"Aglets", one, one, one, null);
	} 

	public AtpRequestImpl(InputStream in) throws IOException {
		this.in = in;
	}
	public Name getAgentName() {
		return MAFUtil
			.decodeName(MAFUtil
				.encodeString(getRequestParameter("agent-id")));
	}
	public String getAgentNameAsString() {
		return getRequestParameter("agent-id");
	}
	/*
	 * public byte[] getContent() {
	 * try {
	 * int l = Integer.parseInt(getRequestParameter("content-length"));
	 * byte b[] = new byte[l];
	 * DataInput din = new DataInputStream(getInputStream());
	 * din.readFully(b);
	 * return b;
	 * } catch (Exception ex) {
	 * ex.printStackTrace();
	 * return null;
	 * }
	 * }
	 */
	public AgentProfile getAgentProfile() {
		return agent_profile;
	}
	public int getContentLength() {
		try {
			return Integer.parseInt(getRequestParameter("content-length"));
		} catch (Exception ex) {
			return -1;
		} 
	}
	public String getFetchClassFile() {
		return requestURI.toString();
	}
	public InputStream getInputStream() {
		return in;
	}
	public int getMethod() {
		return method;
	}
	public String getPlaceName() {
		String name = requestURI.getFile();

		if (name == null || name.length() == 0) {
			return "";
		} 

		// cut off "/servlet"
		if (name.startsWith("/servlet")) {
			name = name.substring(9);		// cut off "/servlet/"
			int i = name.indexOf('/', 1);

			if (i > 0) {
				name = name.substring(i);
			} else {
				return "";
			} 
		} 

		// cut off "/aglets"
		if (name.startsWith("/aglets")) {
			name = name.substring(7);
		} 
		int i = name.indexOf('/', 1);

		if (i > 0) {
			name = name.substring(name.charAt(0) == '/' ? 1 : 0, i);
		} else {
			name = name.substring(name.charAt(0) == '/' ? 1 : 0);
		} 
		if (name.equals("default") || name.equals("cxt")) {		// legacy
			name = "";
		} 
		return name;
	}
	/*
	 * public URL getRequestURI() {
	 * return requestURI;
	 * }
	 */

	public String getRequestLine() {
		return requestLine;
	}
	/*
	 * public String getCodeBase() {
	 * return getRequestParameter("codebase");
	 * }
	 */
	public String getRequestParameter(String key) {
		return getRequestParameter(key, null);
	}
	public String getRequestParameter(String key, String defValue) {
		String r = (String)headers.get(key.toLowerCase());

		return r == null ? defValue : r;
	}
	public String getSender() {
		return getRequestParameter("from");
	}
	public void parseHeaders() throws IOException {
		DataInput di = new DataInputStream(in);

		requestLine = di.readLine();
		headers.put("requestline", requestLine);
		verboseOut("[requestLine : " + requestLine + ']');

		StringTokenizer st = new StringTokenizer(requestLine, " ", false);

		if (st.countTokens() != 3) {
			throw new IOException("Invalid Request :" + requestLine);
		} 

		String t = st.nextToken().trim();

		headers.put("method", t);
		if ("dispatch".equalsIgnoreCase(t)) {
			method = AtpConstants.DISPATCH;
		} else if ("retract".equalsIgnoreCase(t)) {
			method = AtpConstants.RETRACT;

		} else if ("fetch".equalsIgnoreCase(t)) {
			method = AtpConstants.FETCH;
		} else if ("ping".equalsIgnoreCase(t)) {
			method = AtpConstants.PING;
		} else if ("message".equalsIgnoreCase(t)) {
			method = AtpConstants.MESSAGE;
		} else if ("reply".equalsIgnoreCase(t)) {
			method = AtpConstants.REPLY;
		} else {
			throw new IOException("Invalid Request :" + requestLine);
		} 

		t = st.nextToken().trim().replace('+', ' ');
		headers.put("requesturi", t);
		try {
			requestURI = new URL(t);
		} catch (Exception ex) {
			requestURI = null;
		} 
		String version = st.nextToken();

		headers.put("version", version);

		if (ATP_VERSION.equalsIgnoreCase(version) == false) {
			throw new IOException("Invalid Protocol: " + requestLine);
		} 

		String line;

		while (true) {
			line = di.readLine().trim();

			// REMIND: debug code
			if (method == AtpConstants.ILLEGAL_REQUEST) {
				verboseOut("|" + line);
			} 
			if (line.length() == 0) {
				break;
			} 
			String key = line.substring(0, line.indexOf(':')).trim();
			String value = line.substring(line.indexOf(':') + 1).trim();

			headers.put(key.toLowerCase(), value);
		} 

		agent_profile.agent_system_type = 
			MAFUtil.toAgentSystemType(getRequestParameter("agent-system", 
				"aglets"));
		agent_profile.language_id = 
			MAFUtil.toLanguageID(getRequestParameter("agent-language", 
				"java"));

	}
	private static void verboseOut(String msg) {
		Daemon.verboseOut(msg);
	}
}
