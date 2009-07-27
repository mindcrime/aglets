package com.ibm.maf.atp;

/*
 * @(#)HttpCGIRequestImpl.java
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

import com.ibm.maf.AgentProfile;
import com.ibm.maf.Name;
import com.ibm.maf.MAFUtil;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletID;

import com.ibm.aglet.message.Message;
import com.ibm.aglet.system.AgletRuntime;

import com.ibm.atp.AtpConstants;

import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.Encoding;
import com.ibm.awb.misc.Hexadecimal;
import com.ibm.awb.misc.URIEncoder;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * @version     1.10	$Date :$
 * @author	Mitsuru Oshima
 */
final class HttpCGIRequestImpl implements AtpRequest, AtpConstants {

	private InputStream in;
	private String requestLine = null;
	private String place = null;
	private Hashtable headers = null;
	private ByteArrayInputStream bin = null;
	private int length = 0;

	private static AgentProfile agent_profile = null;
	private Name name = null;

	static private String urlEncoding = null;

	static AgletRuntime runtime = null;

	static Resource res = null;
	static {
		res = Resource.getResourceFor("atp");
		urlEncoding = res.getString("atp.http.urlencoding", 
									Encoding.getDefault().getJavaEncoding());

		runtime = AgletRuntime.getAgletRuntime();

		short one = (short)1;

		agent_profile = new AgentProfile(one,		// Java
		one,										// Aglets
		"Aglets", one, one, one, null);
	} 

	public HttpCGIRequestImpl(InputStream in, 
							  Hashtable h) throws IOException {
		headers = h;
		this.in = in;
	}
	private StringTokenizer checkFormat(String f) throws IOException {
		if (f.charAt(0) != '/') {
			throw new IOException("Invalid Format :" 
								  + headers.get("requestline"));
		} 
		StringTokenizer t = new StringTokenizer(f, "/");

		if (f.endsWith("/")) {
			if (t.countTokens() < 3) {
				throw new IOException("Invalid Format :" 
									  + headers.get("requestline"));
			} 
		} else {
			if (t.countTokens() < 4) {
				throw new IOException("Invalid Format :" 
									  + headers.get("requestline"));
			} 
		} 
		return t;
	}
	/**
	 * Decode encoded URI string into String under aglet's encoding
	 * @param str encoded URI string
	 * @return decoded string under specified encoding
	 * @see java.net.URLEncoder
	 */
	static private String decode(String str) {
		if (str == null) {
			return null;
		} 
		String s = null;

		try {
			final String fStr = str;
			final String fEncoding = urlEncoding;

			s = 
				(String)AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() {
					try {
						return URIEncoder.decode(fStr, fEncoding);
					} catch (UnsupportedEncodingException excpt) {
						return URIEncoder.decode(fStr);
					} 
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		return s;
	}
	public Name getAgentName() {
		return name;
	}
	public String getAgentNameAsString() {
		return MAFUtil.decodeString(MAFUtil.encodeName(name));
	}
	public AgentProfile getAgentProfile() {
		return agent_profile;
	}
	public int getContentLength() {
		return length;

		/*
		 * try {
		 * return Integer.parseInt(getRequestParameter("content-length"));
		 * } catch (Exception ex) {
		 * return -1;
		 * }
		 */
	}
	public String getFetchClassFile() {
		return null;
	}
	public InputStream getInputStream() {
		return bin;
	}
	public int getMethod() {
		return MESSAGE;
	}
	public String getPlaceName() {
		return place;
	}
	public String getRequestLine() {
		return requestLine;
	}
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
		requestLine = headers.get("method") + " " + headers.get("requestURI") 
					  + " " + headers.get("protocol");

		String m = (String)headers.get("method");

		if ("GET".equalsIgnoreCase(m)) {}
		else if ("POST".equalsIgnoreCase(m)) {}
		else {
			throw new IOException("Invalid Request :" 
								  + headers.get("requestline"));
		} 

		String p = (String)headers.get("protocol");

		if (p == null || p.startsWith("HTTP/1.") == false) {
			throw new IOException("Invalid Protocol :" 
								  + headers.get("requestline"));
		} 

		String host = (String)headers.get("host");

		if (host == null) {
			host = "file:///";
		} else if (host.startsWith("http:") == false) {
			host = "http://" + host;
		} 

		URL requestURI = null;

		try {
			requestURI = new URL(new URL(host), 
								 (String)headers.get("requesturi"));

			// System.out.println("requestURI = " + requestURI);
		} catch (Exception ex) {
			ex.printStackTrace();
			requestURI = null;
			return;
		} 
		String kind = null;

		String f = requestURI.getFile();
		boolean found = false;
		String cgi = null;

		StringTokenizer t = checkFormat(f);

		String agentSystem = t.nextToken();

		if ("aglets".equals(agentSystem) == false) {
			throw new IOException("Invalid Agent System :" + agentSystem);
		} 

		String place = t.nextToken();
		String name_or_id = t.nextToken();

		if ("default".equals(place)) {
			place = "";
		} 

		AgletContext cxt = runtime.getAgletContext(place);

		if (cxt == null) {
			throw new IOException("Place Not Found :" + place);
		} 

		Object aid = cxt.getProperty("name." + name_or_id);

		if (aid instanceof AgletID) {
			name = MAFUtil.toName((AgletID)aid, null);
		} else {
			name = MAFUtil.toName(new AgletID(name_or_id), null);
		} 

		if (t.hasMoreTokens()) {
			cgi = t.nextToken();
		} else {
			cgi = "";
		} 
		int msgindex = cgi.indexOf('?');

		if (msgindex > 0) {
			kind = decode(cgi.substring(0, msgindex));
			cgi = cgi.substring(msgindex + 1);
		} else {
			kind = cgi;
			cgi = null;
		} 

		Message msg = new com.ibm.awb.misc.CGIMessage(kind);

		if (cgi != null) {

			StringTokenizer tt = new StringTokenizer(cgi, "&");

			msg.setArg("%querystring%", cgi);

			while (tt.hasMoreTokens()) {
				String token = tt.nextToken();
				int index = token.indexOf('=');

				if (index > 0) {
					String arg = decode(token.substring(0, index));
					String val = decode(token.substring(index + 1));

					msg.setArg(arg, val);
				} 
			} 
		} 

		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		bout.write(SYNC);
		new ObjectOutputStream(bout).writeObject(msg);
		length = bout.size();
		bin = new ByteArrayInputStream(bout.toByteArray());
	}
}
