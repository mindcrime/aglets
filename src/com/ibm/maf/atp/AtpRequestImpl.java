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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.ibm.atp.AtpConstants;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.Name;

/**
 * @version 1.10 $Date :$
 * @author Mitsuru Oshima
 */
public final class AtpRequestImpl implements AtpRequest {
	final static String ATP_VERSION = "ATP/0.1";

	private static void verboseOut(final String msg) {
		Daemon.verboseOut(msg);
	}
	private final InputStream in;
	private int method = 0;
	private URL requestURI = null;
	private String requestLine = null;

	private final Hashtable headers = new Hashtable();

	static AgentProfile agent_profile = null;

	static {
		final short one = (short) 1;

		agent_profile = new AgentProfile(one, // Java
				one, // Aglets
				"Aglets", one, one, one, null);
	}

	public AtpRequestImpl(final InputStream in) throws IOException {
		this.in = in;
	}

	@Override
	public Name getAgentName() {
		return MAFUtil.decodeName(MAFUtil.encodeString(this.getRequestParameter("agent-id")));
	}

	@Override
	public String getAgentNameAsString() {
		return this.getRequestParameter("agent-id");
	}

	/*
	 * public byte[] getContent() { try { int l =
	 * Integer.parseInt(getRequestParameter("content-length")); byte b[] = new
	 * byte[l]; DataInput din = new DataInputStream(getInputStream());
	 * din.readFully(b); return b; } catch (Exception ex) {
	 * ex.printStackTrace(); return null; } }
	 */
	@Override
	public AgentProfile getAgentProfile() {
		return agent_profile;
	}

	@Override
	public int getContentLength() {
		try {
			return Integer.parseInt(this.getRequestParameter("content-length"));
		} catch (final Exception ex) {
			return -1;
		}
	}

	@Override
	public String getFetchClassFile() {
		return requestURI.toString();
	}

	@Override
	public InputStream getInputStream() {
		return in;
	}

	@Override
	public int getMethod() {
		return method;
	}

	/*
	 * public URL getRequestURI() { return requestURI; }
	 */

	@Override
	public String getPlaceName() {
		String name = requestURI.getFile();

		if ((name == null) || (name.length() == 0)) {
			return "";
		}

		// cut off "/servlet"
		if (name.startsWith("/servlet")) {
			name = name.substring(9); // cut off "/servlet/"
			final int i = name.indexOf('/', 1);

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
		final int i = name.indexOf('/', 1);

		if (i > 0) {
			name = name.substring(name.charAt(0) == '/' ? 1 : 0, i);
		} else {
			name = name.substring(name.charAt(0) == '/' ? 1 : 0);
		}
		if (name.equals("default") || name.equals("cxt")) { // legacy
			name = "";
		}
		return name;
	}

	@Override
	public String getRequestLine() {
		return requestLine;
	}

	/*
	 * public String getCodeBase() { return getRequestParameter("codebase"); }
	 */
	@Override
	public String getRequestParameter(final String key) {
		return this.getRequestParameter(key, null);
	}

	public String getRequestParameter(final String key, final String defValue) {
		final String r = (String) headers.get(key.toLowerCase());

		return r == null ? defValue : r;
	}

	@Override
	public String getSender() {
		return this.getRequestParameter("from");
	}

	@Override
	public void parseHeaders() throws IOException {
		final DataInput di = new DataInputStream(in);

		requestLine = di.readLine();
		headers.put("requestline", requestLine);
		verboseOut("[requestLine : " + requestLine + ']');

		final StringTokenizer st = new StringTokenizer(requestLine, " ", false);

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
		} catch (final Exception ex) {
			requestURI = null;
		}
		final String version = st.nextToken();

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
			final String key = line.substring(0, line.indexOf(':')).trim();
			final String value = line.substring(line.indexOf(':') + 1).trim();

			headers.put(key.toLowerCase(), value);
		}

		agent_profile.agent_system_type = MAFUtil.toAgentSystemType(this.getRequestParameter("agent-system", "aglets"));
		agent_profile.language_id = MAFUtil.toLanguageID(this.getRequestParameter("agent-language", "java"));

	}
}
