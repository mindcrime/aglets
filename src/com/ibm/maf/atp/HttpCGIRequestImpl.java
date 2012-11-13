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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.atp.AtpConstants;
import com.ibm.awb.misc.Encoding;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.URIEncoder;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.Name;

/**
 * @version 1.10 $Date :$
 * @author Mitsuru Oshima
 */
final class HttpCGIRequestImpl implements AtpRequest, AtpConstants {

	/**
	 * Decode encoded URI string into String under aglet's encoding
	 * 
	 * @param str
	 *            encoded URI string
	 * @return decoded string under specified encoding
	 * @see java.net.URLEncoder
	 */
	static private String decode(final String str) {
		if (str == null) {
			return null;
		}
		String s = null;

		try {
			final String fStr = str;
			final String fEncoding = urlEncoding;

			s = (String) AccessController.doPrivileged(new PrivilegedExceptionAction() {
				@Override
				public Object run() {
					try {
						return URIEncoder.decode(fStr, fEncoding);
					} catch (final UnsupportedEncodingException excpt) {
						return URIEncoder.decode(fStr);
					}
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return s;
	}
	private String requestLine = null;
	private final String place = null;
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
		urlEncoding = res.getString("atp.http.urlencoding", Encoding.getDefault().getJavaEncoding());

		runtime = AgletRuntime.getAgletRuntime();

		final short one = (short) 1;

		agent_profile = new AgentProfile(one, // Java
				one, // Aglets
				"Aglets", one, one, one, null);
	}

	public HttpCGIRequestImpl(final InputStream in, final Hashtable h) throws IOException {
		headers = h;
	}

	private StringTokenizer checkFormat(final String f) throws IOException {
		if (f.charAt(0) != '/') {
			throw new IOException("Invalid Format :"
					+ headers.get("requestline"));
		}
		final StringTokenizer t = new StringTokenizer(f, "/");

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

	@Override
	public Name getAgentName() {
		return name;
	}

	@Override
	public String getAgentNameAsString() {
		return MAFUtil.decodeString(MAFUtil.encodeName(name));
	}

	@Override
	public AgentProfile getAgentProfile() {
		return agent_profile;
	}

	@Override
	public int getContentLength() {
		return length;

		/*
		 * try { return Integer.parseInt(getRequestParameter("content-length"));
		 * } catch (Exception ex) { return -1; }
		 */
	}

	@Override
	public String getFetchClassFile() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return bin;
	}

	@Override
	public int getMethod() {
		return MESSAGE;
	}

	@Override
	public String getPlaceName() {
		return place;
	}

	@Override
	public String getRequestLine() {
		return requestLine;
	}

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
		requestLine = headers.get("method") + " "
		+ headers.get("requestURI") + " "
		+ headers.get("protocol");

		final String m = (String) headers.get("method");

		if ("GET".equalsIgnoreCase(m)) {
		} else if ("POST".equalsIgnoreCase(m)) {
		} else {
			throw new IOException("Invalid Request :"
					+ headers.get("requestline"));
		}

		final String p = (String) headers.get("protocol");

		if ((p == null) || (p.startsWith("HTTP/1.") == false)) {
			throw new IOException("Invalid Protocol :"
					+ headers.get("requestline"));
		}

		String host = (String) headers.get("host");

		if (host == null) {
			host = "file:///";
		} else if (host.startsWith("http:") == false) {
			host = "http://" + host;
		}

		URL requestURI = null;

		try {
			requestURI = new URL(new URL(host), (String) headers.get("requesturi"));

			// System.out.println("requestURI = " + requestURI);
		} catch (final Exception ex) {
			ex.printStackTrace();
			requestURI = null;
			return;
		}
		String kind = null;

		final String f = requestURI.getFile();
		String cgi = null;

		final StringTokenizer t = checkFormat(f);

		final String agentSystem = t.nextToken();

		if ("aglets".equals(agentSystem) == false) {
			throw new IOException("Invalid Agent System :" + agentSystem);
		}

		String place = t.nextToken();
		final String name_or_id = t.nextToken();

		if ("default".equals(place)) {
			place = "";
		}

		final AgletContext cxt = runtime.getAgletContext(place);

		if (cxt == null) {
			throw new IOException("Place Not Found :" + place);
		}

		final Object aid = cxt.getProperty("name." + name_or_id);

		if (aid instanceof AgletID) {
			name = MAFUtil.toName((AgletID) aid, null);
		} else {
			name = MAFUtil.toName(new AgletID(name_or_id), null);
		}

		if (t.hasMoreTokens()) {
			cgi = t.nextToken();
		} else {
			cgi = "";
		}
		final int msgindex = cgi.indexOf('?');

		if (msgindex > 0) {
			kind = decode(cgi.substring(0, msgindex));
			cgi = cgi.substring(msgindex + 1);
		} else {
			kind = cgi;
			cgi = null;
		}

		final Message msg = new com.ibm.awb.misc.CGIMessage(kind);

		if (cgi != null) {

			final StringTokenizer tt = new StringTokenizer(cgi, "&");

			msg.setArg("%querystring%", cgi);

			while (tt.hasMoreTokens()) {
				final String token = tt.nextToken();
				final int index = token.indexOf('=');

				if (index > 0) {
					final String arg = decode(token.substring(0, index));
					final String val = decode(token.substring(index + 1));

					msg.setArg(arg, val);
				}
			}
		}

		final ByteArrayOutputStream bout = new ByteArrayOutputStream();

		bout.write(SYNC);
		new ObjectOutputStream(bout).writeObject(msg);
		length = bout.size();
		bin = new ByteArrayInputStream(bout.toByteArray());
	}
}
