package com.ibm.maf.atp;

/*
 * @(#)AtpConnectionImpl.java
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

import com.ibm.atp.AtpConstants;
import com.ibm.atp.ContentBuffer;
import com.ibm.atp.ContentOutputStream;

import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

import com.ibm.awb.misc.Hexadecimal;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.TeeOutputStream;

import java.security.AccessController;
import java.security.Permission;
import java.net.SocketPermission;

import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.DataInput;
import java.io.DataInputStream;

import java.util.Hashtable;
import java.util.Date;

/**
 * An instance of this class creates a communication link between an
 * application and an atp server.
 * 
 * @version     1.10	$Date :$
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 */

public class AtpConnectionImpl extends URLConnection implements AtpConstants {
	/**
	 * Default port number to receive atp messages.
	 */
	public static final int ATP_DEFAULT_PORT = 4434;

	protected static final String CRLF = "\r\n";
	protected static final String ATP_VERSION = "ATP/0.1";

	/*
	 * 
	 */
	static Hashtable default_request_properties = new Hashtable();
	static AgentProfile agent_profile = null;

	static {
		short one = (short)1;

		agent_profile = new AgentProfile(one,		// Java
		one,										// Aglets
		"Aglets", one, one, one, null);

		/*
		 * default_request_properties.put("agent-language", "java");
		 * default_request_properties.put("agent-system", "aglets");
		 */
		default_request_properties.put("user-agent", "Aglets");
		default_request_properties.put("from", "");
		default_request_properties.put("content-type", 
									   "application/x-aglets");
		default_request_properties.put("content-language", "");
		default_request_properties.put("content-encoding", "");
	} 

	/*
	 * 
	 */
	private int _request_type = REQUEST_TYPE_BASE;

	private InputStream _inputStream = null;

	private ContentOutputStream _outputStream = null;

	private Connection _connection = null;

	// properties
	// private String code_base = null;
	private String from = null;
	private Name agent_name = null;
	private String securityDomainname = null;
	private byte[] mic = null;

	/*
	 * 
	 */
	Hashtable request_properties;

	/*
	 * Header field
	 */
	private Hashtable headers = new Hashtable();

	/**
	 * Create a new instance of this class.
	 * @param url a destination URL to which the application connects.
	 * The protocol is "atp".
	 */
	public AtpConnectionImpl(URL url) throws IOException {
		super(url);
		request_properties = (Hashtable)default_request_properties.clone();
	}
	public void close() throws IOException {
		if (_connection != null) {
			_connection.close();
		} 
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

		if (_request_type == REQUEST_TYPE_BASE) {
			_request_type = guessRequestTypeFromURL(url);
		} 

		// -- choose real target (keep the order)
		if (HttpProxyConnection.useHttpProxy(url)) {
			int port = url.getPort();
			String target = url.getHost() + ':' 
							+ (port == -1 ? ATP_DEFAULT_PORT : port);
			Permission p = new SocketPermission(target, "connect");

			AccessController.checkPermission(p);

			// http proxy is not under the security manager

			_connection = new HttpProxyConnection(url, ATP_DEFAULT_PORT);

		} else {
			_connection = new SocketConnection(url, ATP_DEFAULT_PORT);
		} 
		if (_connection != null && _connection.isEstablished()) {
			connected = true;
			securityDomainname = _connection.getAuthenticatedSecurityDomain();
		} else {
			String msg = null;

			if (_connection != null) {
				msg = _connection.getMessage();
			} 
			if (msg == null) {
				msg = "connection not available";
			} 
			_connection = null;
			throw new IOException(msg);
		} 
	}
	public String getHeaderField(String key) {
		try {
			getInputStream();
		} catch (IOException ex) {}
		String ret = (String)headers.get(key.toLowerCase());

		return ret;
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

		/*
		 * if (_request_type == MESSAGE) {
		 * throw new IllegalAccessError("Request Type" + _request_type +
		 * "doesn't support input");
		 * }
		 */
		connect();
		if (!connected) {
			return null;
		} 

		if (_request_type == RETRACT || _request_type == FETCH 
				|| _request_type == PING) {
			OutputStream out = _connection.getOutputStream();
			PrintStream p = null;

			if (Daemon.isVerbose()) {
				p = new PrintStream(new TeeOutputStream(System.err, out));
			} else {
				p = new PrintStream(out);
			} 
			sendRequestHeaders(p);
			p.print(CRLF);
			p.flush();

			if (out instanceof ContentBuffer) {
				((ContentBuffer)out).sendContent();
			} 
		} 

		_inputStream = _connection.getInputStream();
		parseHeaders();
		return _inputStream;
	}
	public byte[] getMIC() {
		return mic;
	}
	/**
	 * Get an output stream of the communication link. If the destination is
	 * out of firewall and a proxy is specified, an output stream connected
	 * to the proxy will be returned, The stream will add the destination
	 * atp address to top of an output data. The proxy reads the address,
	 * takes it away and forewards the ramained data to the specified
	 * destination.
	 * @return an output stream.
	 * @exception IOException if the communication link has a problem.
	 */
	synchronized public OutputStream getOutputStream() throws IOException {
		if (_outputStream != null) {
			return _outputStream;
		} 

		if (_request_type != DISPATCH && _request_type != MESSAGE) {
			throw new IllegalAccessError("Request Type " + _request_type 
										 + " doesn't support output");
		} 

		connect();
		if (!connected) {
			return null;
		} 

		if (_request_type == MESSAGE) {
			_outputStream = 
				new ContentOutputStream(_connection.getOutputStream());
		} else {
			_outputStream = 
				new ContentOutputStream(_connection.getOutputStream());
		} 

		PrintStream p = null;

		if (Daemon.isVerbose()) {
			p = new PrintStream(new TeeOutputStream(System.err, 
													_outputStream));
		} else {
			p = new PrintStream(_outputStream);
		} 
		sendRequestHeaders(p);

		/*
		 * send EntityHeaders
		 */
		p.print("Content-Type:" + getProperty("content-type") + CRLF 
				+ "Content-Encoding:" + getProperty("content-encoding") 
				+ CRLF + "Content-Language:" 
				+ getProperty("content-language") + CRLF);
		_outputStream.startContent();

		// _outputStream = out;
		// _outputStream = new ContentOutputStream(out);
		return _outputStream;
	}
	/*
	 * public void setCodeBase(String cb) {
	 * code_base = cb;
	 * }
	 */

	public String getProperty(String key) {
		return (String)request_properties.get(key);
	}
	public String getReasonPhase() {
		try {
			getInputStream();
		} catch (IOException ex) {}
		return getHeaderField("reason-phase");
	}
	SharedSecret getSharedSecret() {
		if (securityDomainname == null) {
			return null;
		} 
		SharedSecrets secrets = SharedSecrets.getSharedSecrets();

		if (secrets == null) {
			return null;
		} 
		return secrets.getSharedSecret(securityDomainname);
	}
	public int getStatusCode() {
		int tmp = -1;

		try {
			getInputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
		try {
			String f = getHeaderField("status-code");

			if (f == null) {
				f = "-1";
			} 
			tmp = Integer.parseInt(f);
		} catch (Exception e) {}
		return tmp;
	}
	/*
	 * 
	 */
	static protected int guessRequestTypeFromURL(URL url) throws IOException {
		String file = url.getFile();
		String ref = url.getRef();

		if (file != null && file.startsWith("#")) {
			return RETRACT;
		} 

		if (file == null || "".equals(file) || "/".equals(file) 
				|| "//".equals(file) || "///".equals(file)) {

			if (ref == null) {
				return DISPATCH;
			} else {
				return RETRACT;
			} 
		} else {
			if (ref != null || "".equals(ref)) {
				throw new MalformedURLException("Unknown request : " + url);
			} 
			return FETCH;
		} 
	}
	/*
	 * public URL getResultURI() {
	 * try {
	 * getInputStream();
	 * } catch (IOException ex) {}
	 * try {
	 * return new URL(getHeaderField("location"));
	 * } catch (MalformedURLException ex) {
	 * return null;
	 * }
	 * }
	 */

	public void parseHeaders() throws IOException {
		DataInputStream di = new DataInputStream(_inputStream);

		parseHeaders(di, headers);
	}
	public static void parseHeaders(DataInputStream di, 
									Hashtable headers) throws IOException {
		String statusLine = di.readLine();
		int statusCode = -1;

		verboseOut("[stausLine = " + statusLine + ']');

		int atpv_index = statusLine.indexOf(' ');

		if (atpv_index < 1) {
			throw new IOException("Invalid Response :" + statusLine);
		} 

		int sts_index = statusLine.indexOf(' ', atpv_index + 1);

		if (sts_index < 1) {
			throw new IOException("Invalid Response :" + statusLine);
		} 
		int rsn_index = sts_index + 1;		// statusLine.indexOf(' ', sts_index+1);

		String tmp = statusLine.substring(0, atpv_index);

		if (!tmp.equalsIgnoreCase(ATP_VERSION)) {
			throw new IOException("ATP Version Mismatch");
		} 
		headers.put("version", tmp);

		tmp = statusLine.substring(atpv_index + 1, sts_index);
		headers.put("status-code", tmp);
		try {
			statusCode = Integer.parseInt(tmp);
		} catch (Exception ex) {
			statusCode = -1;
		} 

		if (rsn_index > 0) {
			tmp = statusLine.substring(rsn_index);
			headers.put("reason-phase", tmp);
		} 

		String line;

		while (true) {
			line = di.readLine();
			if (line != null) {
				verboseOut(">" + line);
			} 
			if (statusCode != OKAY && statusCode != MOVED) {
				verboseOut("|" + line);
			} 
			if (line == null || line.length() == 0) {
				break;
			} 
			String key = line.substring(0, line.indexOf(':')).trim();
			String value = line.substring(line.indexOf(':') + 1).trim();

			headers.put(key.toLowerCase(), value);
		} 
	}
	/*
	 * 
	 */
	public void sendRequest() throws IOException {
		switch (_request_type) {

		case DISPATCH:
		case MESSAGE:
			if (_outputStream == null) {
				throw new IllegalAccessError("No content input");
			} 
			_outputStream.sendContent();
			break;

		case RETRACT:
		case FETCH:
		case PING:
			connect();
			if (connected) {
				getInputStream();
			} 

			break;
		default:
			throw new RuntimeException("Invalid Request Type : " 
									   + _request_type);
		}
	}
	/**
	 * 
	 */
	protected void sendRequestHeaders(PrintStream p) throws IOException {
		String absURI = null;

		switch (_request_type) {
		case DISPATCH:
			p.print("DISPATCH " + url.toExternalForm().replace(' ', '+'));
			break;
		case RETRACT:
			p.print("RETRACT " + url.toExternalForm().replace(' ', '+'));
			break;
		case FETCH:
			p.print("FETCH " + url.toExternalForm().replace(' ', '+'));
			break;
		case MESSAGE:
			try {
				url = new URL(url, 
							  '#' + MAFUtil.toAgletID(agent_name).toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
			p.print("MESSAGE " + url.toExternalForm().replace(' ', '+'));
			break;
		}

		String agent_system = 
			MAFUtil.toAgentSystem(agent_profile.agent_system_type);
		String language = MAFUtil.toLanguage(agent_profile.language_id);

		p.print(" " + ATP_VERSION + CRLF + "date:" + (new Date()).toString() 
				+ CRLF + "user-agent:" + getProperty("user-agent") + CRLF 

		// + "codebase:" + code_base + CRLF
		+ "from:" + from + CRLF + "host:" + url.getHost() + CRLF 
				  + "agent-system:" + agent_system + CRLF + "agent-language:" 
				  + language + CRLF + "agent-id:" 
				  + ((agent_name == null) ? null : MAFUtil.decodeString(MAFUtil.encodeName(agent_name)))
				  + CRLF 
				  + (mic == null ? "" 
					 : ("mic:" + Hexadecimal.valueOf(mic) + CRLF)));
	}
	public void setAgentName(Name name) {
		agent_name = name;
	}
	public void setAgentProfile(AgentProfile profile) {
		agent_profile = profile;
	}
	public void setMIC(byte[] bytes) {
		mic = bytes;
	}
	/*
	 * Sets request parameters
	 */
	public void setRequestProperty(String key, String value) {
		if (connected) {
			throw new IllegalAccessError("Already connected");
		} 
		key = key.toLowerCase();
		request_properties.put(key, value);
		if (key.equals("agent-language")) {
			agent_profile.language_id = MAFUtil.toLanguageID(value);
		} else if (key.equals("agent-system")) {
			agent_profile.agent_system_type = 
				MAFUtil.toAgentSystemType(value);

			// } else if (key.equals("code-base")) {
			// code_base = value;
		} else if (key.equals("from")) {
			from = value;
		} else if (key.equals("agent-id")) {
			agent_name = MAFUtil.decodeName(MAFUtil.encodeString(value));
		} else if (key.equals("mic")) {
			try {
				mic = Hexadecimal.parseSeq(value);
			} catch (NumberFormatException excpt) {
				mic = null;
			} 
		} 
	}
	/*
	 * 
	 */
	public void setRequestType(int i) {
		if (connected) {
			throw new IllegalAccessError("Already connected");
		} 
		if (_request_type != REQUEST_TYPE_BASE) {
			throw new IllegalAccessError("Request type Already set");
		} 
		if (REQUEST_TYPE_BASE >= i || MAX_REQUEST_TYPE <= i) {
			throw new IllegalArgumentException("setRequestType (" + i + ")");
		} 
		_request_type = i;
	}
	public void setSender(String address) {
		from = address;
	}
	private static void verboseOut(String msg) {
		Daemon.verboseOut(msg);
	}
}
