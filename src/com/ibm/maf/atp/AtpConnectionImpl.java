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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.Permission;
import java.util.Date;
import java.util.Hashtable;

import com.ibm.atp.AtpConstants;
import com.ibm.atp.ContentBuffer;
import com.ibm.atp.ContentOutputStream;
import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.awb.misc.Hexadecimal;
import com.ibm.awb.misc.TeeOutputStream;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.Name;

/**
 * An instance of this class creates a communication link between an application
 * and an atp server.
 * 
 * @version 1.10 $Date :$
 * @author Danny B. Lange
 * @author Mitsuru Oshima
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
	short one = (short) 1;

	agent_profile = new AgentProfile(one, // Java
	one, // Aglets
	"Aglets", one, one, one, null);

	/*
	 * default_request_properties.put("agent-language", "java");
	 * default_request_properties.put("agent-system", "aglets");
	 */
	default_request_properties.put("user-agent", "Aglets");
	default_request_properties.put("from", "");
	default_request_properties.put("content-type", "application/x-aglets");
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
     * 
     * @param url
     *            a destination URL to which the application connects. The
     *            protocol is "atp".
     */
    public AtpConnectionImpl(URL url) throws IOException {
	super(url);
	this.request_properties = (Hashtable) default_request_properties.clone();
    }

    public void close() throws IOException {
	if (this._connection != null) {
	    this._connection.close();
	}
    }

    /**
     * Make a comminucation link with the destination.
     * 
     * @exception IOException
     *                if can not make a communication link.
     */
    @Override
    synchronized public void connect() throws IOException {
	if (this.connected) {
	    return;
	}

	if (this._request_type == REQUEST_TYPE_BASE) {
	    this._request_type = guessRequestTypeFromURL(this.url);
	}

	// -- choose real target (keep the order)
	if (HttpProxyConnection.useHttpProxy(this.url)) {
	    int port = this.url.getPort();
	    String target = this.url.getHost() + ':'
		    + (port == -1 ? ATP_DEFAULT_PORT : port);
	    Permission p = new SocketPermission(target, "connect");

	    AccessController.checkPermission(p);

	    // http proxy is not under the security manager

	    this._connection = new HttpProxyConnection(this.url, ATP_DEFAULT_PORT);

	} else {
	    this._connection = new SocketConnection(this.url, ATP_DEFAULT_PORT);
	}
	if ((this._connection != null) && this._connection.isEstablished()) {
	    this.connected = true;
	    this.securityDomainname = this._connection.getAuthenticatedSecurityDomain();
	} else {
	    String msg = null;

	    if (this._connection != null) {
		msg = this._connection.getMessage();
	    }
	    if (msg == null) {
		msg = "connection not available";
	    }
	    this._connection = null;
	    throw new IOException(msg);
	}
    }

    @Override
    public String getHeaderField(String key) {
	try {
	    this.getInputStream();
	} catch (IOException ex) {
	}
	String ret = (String) this.headers.get(key.toLowerCase());

	return ret;
    }

    /**
     * Get an input stream of the communication link.
     * 
     * @return an input stream.
     * @exception IOException
     *                if the communication link has a problem.
     */
    @Override
    synchronized public InputStream getInputStream() throws IOException {
	if (this._inputStream != null) {
	    return this._inputStream;
	}

	/*
	 * if (_request_type == MESSAGE) { throw new
	 * IllegalAccessError("Request Type" + _request_type +
	 * "doesn't support input"); }
	 */
	this.connect();
	if (!this.connected) {
	    return null;
	}

	if ((this._request_type == RETRACT) || (this._request_type == FETCH)
		|| (this._request_type == PING)) {
	    OutputStream out = this._connection.getOutputStream();
	    PrintStream p = null;

	    if (Daemon.isVerbose()) {
		p = new PrintStream(new TeeOutputStream(System.err, out));
	    } else {
		p = new PrintStream(out);
	    }
	    this.sendRequestHeaders(p);
	    p.print(CRLF);
	    p.flush();

	    if (out instanceof ContentBuffer) {
		((ContentBuffer) out).sendContent();
	    }
	}

	this._inputStream = this._connection.getInputStream();
	this.parseHeaders();
	return this._inputStream;
    }

    public byte[] getMIC() {
	return this.mic;
    }

    /**
     * Get an output stream of the communication link. If the destination is out
     * of firewall and a proxy is specified, an output stream connected to the
     * proxy will be returned, The stream will add the destination atp address
     * to top of an output data. The proxy reads the address, takes it away and
     * forewards the ramained data to the specified destination.
     * 
     * @return an output stream.
     * @exception IOException
     *                if the communication link has a problem.
     */
    @Override
    synchronized public OutputStream getOutputStream() throws IOException {
	if (this._outputStream != null) {
	    return this._outputStream;
	}

	if ((this._request_type != DISPATCH) && (this._request_type != MESSAGE)) {
	    throw new IllegalAccessError("Request Type " + this._request_type
		    + " doesn't support output");
	}

	this.connect();
	if (!this.connected) {
	    return null;
	}

	if (this._request_type == MESSAGE) {
	    this._outputStream = new ContentOutputStream(this._connection.getOutputStream());
	} else {
	    this._outputStream = new ContentOutputStream(this._connection.getOutputStream());
	}

	PrintStream p = null;

	if (Daemon.isVerbose()) {
	    p = new PrintStream(new TeeOutputStream(System.err, this._outputStream));
	} else {
	    p = new PrintStream(this._outputStream);
	}
	this.sendRequestHeaders(p);

	/*
	 * send EntityHeaders
	 */
	p.print("Content-Type:" + this.getProperty("content-type") + CRLF
		+ "Content-Encoding:" + this.getProperty("content-encoding")
		+ CRLF + "Content-Language:"
		+ this.getProperty("content-language") + CRLF);
	this._outputStream.startContent();

	// _outputStream = out;
	// _outputStream = new ContentOutputStream(out);
	return this._outputStream;
    }

    /*
     * public void setCodeBase(String cb) { code_base = cb; }
     */

    public String getProperty(String key) {
	return (String) this.request_properties.get(key);
    }

    public String getReasonPhase() {
	try {
	    this.getInputStream();
	} catch (IOException ex) {
	}
	return this.getHeaderField("reason-phase");
    }

    SharedSecret getSharedSecret() {
	if (this.securityDomainname == null) {
	    return null;
	}
	SharedSecrets secrets = SharedSecrets.getSharedSecrets();

	if (secrets == null) {
	    return null;
	}
	return secrets.getSharedSecret(this.securityDomainname);
    }

    public int getStatusCode() {
	int tmp = -1;

	try {
	    this.getInputStream();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
	try {
	    String f = this.getHeaderField("status-code");

	    if (f == null) {
		f = "-1";
	    }
	    tmp = Integer.parseInt(f);
	} catch (Exception e) {
	}
	return tmp;
    }

    /*
	 * 
	 */
    static protected int guessRequestTypeFromURL(URL url) throws IOException {
	String file = url.getFile();
	String ref = url.getRef();

	if ((file != null) && file.startsWith("#")) {
	    return RETRACT;
	}

	if ((file == null) || "".equals(file) || "/".equals(file)
		|| "//".equals(file) || "///".equals(file)) {

	    if (ref == null) {
		return DISPATCH;
	    } else {
		return RETRACT;
	    }
	} else {
	    if ((ref != null) || "".equals(ref)) {
		throw new MalformedURLException("Unknown request : " + url);
	    }
	    return FETCH;
	}
    }

    /*
     * public URL getResultURI() { try { getInputStream(); } catch (IOException
     * ex) {} try { return new URL(getHeaderField("location")); } catch
     * (MalformedURLException ex) { return null; } }
     */

    public void parseHeaders() throws IOException {
	DataInputStream di = new DataInputStream(this._inputStream);

	parseHeaders(di, this.headers);
    }

    public static void parseHeaders(DataInputStream di, Hashtable headers)
	    throws IOException {
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
	int rsn_index = sts_index + 1; // statusLine.indexOf(' ', sts_index+1);

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
	    if ((statusCode != OKAY) && (statusCode != MOVED)) {
		verboseOut("|" + line);
	    }
	    if ((line == null) || (line.length() == 0)) {
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
	switch (this._request_type) {

	case DISPATCH:
	case MESSAGE:
	    if (this._outputStream == null) {
		throw new IllegalAccessError("No content input");
	    }
	    this._outputStream.sendContent();
	    break;

	case RETRACT:
	case FETCH:
	case PING:
	    this.connect();
	    if (this.connected) {
		this.getInputStream();
	    }

	    break;
	default:
	    throw new RuntimeException("Invalid Request Type : "
		    + this._request_type);
	}
    }

    /**
	 * 
	 */
    protected void sendRequestHeaders(PrintStream p) throws IOException {
	switch (this._request_type) {
	case DISPATCH:
	    p.print("DISPATCH " + this.url.toExternalForm().replace(' ', '+'));
	    break;
	case RETRACT:
	    p.print("RETRACT " + this.url.toExternalForm().replace(' ', '+'));
	    break;
	case FETCH:
	    p.print("FETCH " + this.url.toExternalForm().replace(' ', '+'));
	    break;
	case MESSAGE:
	    try {
		this.url = new URL(this.url, '#' + MAFUtil.toAgletID(this.agent_name).toString());
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    p.print("MESSAGE " + this.url.toExternalForm().replace(' ', '+'));
	    break;
	}

	String agent_system = MAFUtil.toAgentSystem(agent_profile.agent_system_type);
	String language = MAFUtil.toLanguage(agent_profile.language_id);

	p.print(" "
		+ ATP_VERSION
		+ CRLF
		+ "date:"
		+ (new Date()).toString()
		+ CRLF
		+ "user-agent:"
		+ this.getProperty("user-agent")
		+ CRLF

		// + "codebase:" + code_base + CRLF
		+ "from:"
		+ this.from
		+ CRLF
		+ "host:"
		+ this.url.getHost()
		+ CRLF
		+ "agent-system:"
		+ agent_system
		+ CRLF
		+ "agent-language:"
		+ language
		+ CRLF
		+ "agent-id:"
		+ ((this.agent_name == null) ? null
			: MAFUtil.decodeString(MAFUtil.encodeName(this.agent_name)))
		+ CRLF
		+ (this.mic == null ? "" : ("mic:"
			+ Hexadecimal.valueOf(this.mic) + CRLF)));
    }

    public void setAgentName(Name name) {
	this.agent_name = name;
    }

    public void setAgentProfile(AgentProfile profile) {
	agent_profile = profile;
    }

    public void setMIC(byte[] bytes) {
	this.mic = bytes;
    }

    /*
     * Sets request parameters
     */
    @Override
    public void setRequestProperty(String key, String value) {
	if (this.connected) {
	    throw new IllegalAccessError("Already connected");
	}
	key = key.toLowerCase();
	this.request_properties.put(key, value);
	if (key.equals("agent-language")) {
	    agent_profile.language_id = MAFUtil.toLanguageID(value);
	} else if (key.equals("agent-system")) {
	    agent_profile.agent_system_type = MAFUtil.toAgentSystemType(value);

	    // } else if (key.equals("code-base")) {
	    // code_base = value;
	} else if (key.equals("from")) {
	    this.from = value;
	} else if (key.equals("agent-id")) {
	    this.agent_name = MAFUtil.decodeName(MAFUtil.encodeString(value));
	} else if (key.equals("mic")) {
	    try {
		this.mic = Hexadecimal.parseSeq(value);
	    } catch (NumberFormatException excpt) {
		this.mic = null;
	    }
	}
    }

    /*
	 * 
	 */
    public void setRequestType(int i) {
	if (this.connected) {
	    throw new IllegalAccessError("Already connected");
	}
	if (this._request_type != REQUEST_TYPE_BASE) {
	    throw new IllegalAccessError("Request type Already set");
	}
	if ((REQUEST_TYPE_BASE >= i) || (MAX_REQUEST_TYPE <= i)) {
	    throw new IllegalArgumentException("setRequestType (" + i + ")");
	}
	this._request_type = i;
    }

    public void setSender(String address) {
	this.from = address;
    }

    private static void verboseOut(String msg) {
	Daemon.verboseOut(msg);
    }
}
