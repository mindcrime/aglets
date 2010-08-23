package com.ibm.atp.auth;

/*
 * @(#)AuthPacket.java
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
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.atp.AtpConstants;
import com.ibm.awb.misc.Hexadecimal;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.URIEncoder;

/**
 * The <tt>AuthPacket</tt> class is the challenge-response authentication packet
 * class.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class AuthPacket extends Object {

    // Logging
    private static AgletsLogger logger = AgletsLogger.getLogger(AuthPacket.class.getName());

    /**
     * carriage return & line feed
     */
    protected final static String CRLF = "\r\n";

    /**
     * end of packet
     */
    protected final static String END_OF_PACKET = ".";

    /**
     * Authentication protocol version
     */
    protected final static String AUTHENTICATION_PROTOCOL_VERSION = "AUTH/0.1";

    /**
     * Field separator
     */
    protected final static String FIELD_SEPARATOR = ":";

    /**
     * Identifier of aglet server
     */
    protected final static String SERVERID_FIELDNAME = "Server-ID";

    /**
     * Security Domain
     */
    protected final static String DOMAIN_SEPARATOR = ",";
    protected final static String DOMAINS_FIELDNAME = "Domains";
    protected final static String DOMAIN_FIELDNAME = "Domain";

    /**
     * Authentication manner
     */
    protected final static String AUTH_MANNER_FIELDNAME = "AuthManner";
    protected final static String AUTH_MANNER_DIGEST = "DIGEST";
    protected final static String AUTH_MANNER_SIGNATURE = "SIGNATURE";

    /**
     * Challenge
     */
    protected final static String CHALLENGE_FIELDNAME = "Challenge";

    /**
     * Response
     */
    protected final static String RESPONSE_FIELDNAME = "Response";

    /**
     * Identifier of aglet server
     */
    private static String SERVERID = null;
    private String _serverid = null;

    /**
     * verbose
     */
    private static boolean verbose = false;
    static {
	Resource res = Resource.getResourceFor("system");

	if (res != null) {
	    verbose = res.getBoolean("verbose", false);
	}
	if (SERVERID == null) {
	    StringBuffer buf = new StringBuffer();
	    String serveraddress = null;
	    String ownerName = null;
	    String date = null;
	    AgletRuntime runtime = AgletRuntime.getAgletRuntime();

	    if (runtime != null) {
		serveraddress = runtime.getServerAddress();
		ownerName = runtime.getOwnerName();
	    }
	    Calendar cal = Calendar.getInstance();
	    Date time = cal.getTime();
	    long mills = time.getTime();

	    date = Hexadecimal.valueOf(mills);
	    buf.append(serveraddress);
	    buf.append(":");
	    buf.append(ownerName);
	    buf.append(":");
	    buf.append(date);
	    SERVERID = buf.toString();
	}
    }

    /**
     * Step of authentication protocol
     */
    protected final static String STEP_START = "AUTH_START";
    protected final static String STEP_FIRST_TURN = "AUTH_FIRST_TURN";
    protected final static String STEP_SECOND_TURN = "AUTH_SECOND_TURN";
    protected final static String STEP_END = "AUTH_END";

    /**
     * 
     */
    private int _step = Authentication.STEP_NOT_AUTHENTICATED;

    /**
     * Status of authentication
     */
    private int _status = Authentication.STATUS_NORMAL;

    /**
     * Security domain
     */

    // - private String[] _domains = null;
    private Vector _domains = null;
    private String _domain = null;

    /**
     * Authentication manner
     */
    private int _manner = AtpConstants.NO_AUTHENTICATION_MANNER;

    /**
     * Challenge
     */
    private Challenge _challenge = null;

    /**
     * ByteSequence
     */
    private Response _response = null;

    /**
     * Constructor for sending packet
     * 
     * @param step
     *            step of authentication protocol
     * @param status
     *            status of authentication protocol
     * @param domain
     *            security domain name
     * @param manner
     *            authenticatoin manner by challenge-response
     * @param challenge
     *            challenge for authentication
     * @param response
     *            response of challenge
     */
    public AuthPacket(int step, int status, String domain, int manner,
                      Challenge challenge, Response response) {
	this.setServerID(SERVERID);
	this.setStep(step);
	this.setStatus(status);
	this.setSecurityDomain(domain);
	this.setAuthManner(manner);
	this.setChallenge(challenge);
	this.setResponse(response);
    }

    /**
     * Constructor for sending packet
     * 
     * @param step
     *            step of authentication protocol
     * @param status
     *            status of authentication protocol
     * @param domains
     *            security domain names
     * @param manner
     *            authenticatoin manner by challenge-response
     * @param challenge
     *            challenge for authentication
     * @param response
     *            response of challenge
     */
    public AuthPacket(int step, int status, Enumeration domains, int manner,
                      Challenge challenge, Response response) {
	this.setServerID(SERVERID);
	this.setStep(step);
	this.setStatus(status);
	this.setSecurityDomains(domains);
	this.setAuthManner(manner);
	this.setChallenge(challenge);
	this.setResponse(response);
    }

    // /**
    // * Constructor for receiving packet
    // * @param in input stream for packet
    // */
    // public AuthPacket(InputStream in) {
    // try {
    // readFrom(in);
    // }
    // catch(IOException excpt) {
    // clear();
    // }
    // }
    //
    // /**
    // * Constructor for receiving packet
    // * @param topLine top line of packet
    // * @param in input stream for packet
    // */
    // public AuthPacket(String topLine, InputStream in) {
    // try {
    // readFrom(topLine, in);
    // }
    // catch(IOException excpt) {
    // clear();
    // }
    // }
    //
    /**
     * Constructor for receiving packet
     * 
     * @param in
     *            data input stream for packet
     */
    public AuthPacket(DataInput di) {
	try {
	    this.readFrom(di);
	} catch (IOException excpt) {
	    System.err.println("IOException : " + excpt);
	    this.clear();
	}
    }

    /**
     * Clear
     */
    private final void clear() {
	this._step = Authentication.STEP_NOT_AUTHENTICATED;
	this._status = Authentication.STATUS_NORMAL;
	this._domains = null;
	this._domain = null;
	this._manner = AtpConstants.NO_AUTHENTICATION_MANNER;
	this._challenge = null;
	this._response = null;
    }

    private static String decode(String str) {
	if (str == null) {
	    return null;
	}
	String s = null;

	try {
	    final String fStr = str;

	    s = (String) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    return URIEncoder.decode(fStr);
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return s;
    }

    private static String encode(String str) {
	if (str == null) {
	    return null;
	}
	String s = null;

	try {
	    final String fStr = str;

	    s = (String) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    return URIEncoder.encode(fStr);
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return s;
    }

    /**
     * Gets authentication manner.
     * 
     * @return authentication manner
     */
    public final int getAuthManner() {
	return this._manner;
    }

    /**
     * Gets authentication manner field.
     * 
     * @return authentication manner field
     */
    protected final String getAuthMannerField() {
	String str = toAuthMannerString(this._manner);

	if (str != null) {
	    str = AUTH_MANNER_FIELDNAME + FIELD_SEPARATOR + " " + str;
	}
	return str;
    }

    /**
     * Gets challenge.
     * 
     * @return challenge
     */
    public final Challenge getChallenge() {
	return this._challenge;
    }

    /**
     * Gets challenge field.
     * 
     * @return challenge field
     */
    protected final String getChallengeField() {
	String str = null;

	if (this._challenge != null) {
	    str = CHALLENGE_FIELDNAME + FIELD_SEPARATOR + " "
	    + this._challenge.toString();
	}
	return str;
    }

    /**
     * Gets response to challenge.
     * 
     * @return response to challenge
     */
    public final Response getResponse() {
	return this._response;
    }

    /**
     * Gets response to challenge field.
     * 
     * @return response to challenge field
     */
    protected final String getResponseField() {
	String str = null;

	if (this._response != null) {
	    str = RESPONSE_FIELDNAME + FIELD_SEPARATOR + " "
	    + this._response.toString();
	}
	return str;
    }

    /**
     * Gets security domain name.
     * 
     * @return security domain name
     */
    public final String getSecurityDomain() {
	return this._domain;
    }

    /**
     * Gets security domain field.
     * 
     * @return security domain field
     */
    protected final String getSecurityDomainField() {
	String str = null;

	if (this._domain != null) {
	    str = DOMAIN_FIELDNAME + FIELD_SEPARATOR + " " + this._domain;
	}
	return str;
    }

    /**
     * Gets security domain name list.
     * 
     * @return security domain name list (URI encoded)
     */
    public final String getSecurityDomainList() {
	Enumeration domains = this.getSecurityDomains();

	if (domains == null) {
	    return null;
	}

	StringBuffer buf = new StringBuffer();
	boolean bFirst = true;

	while (domains.hasMoreElements()) {
	    String domainName = (String) domains.nextElement();

	    if (!bFirst) {
		buf.append(DOMAIN_SEPARATOR);
	    }
	    buf.append(encode(domainName));
	    bFirst = false;
	}

	return buf.toString();
    }

    /**
     * Gets security domain names.
     * 
     * @return security domain names
     */
    public final Enumeration getSecurityDomains() {
	if (this._domains == null) {
	    return null;
	}

	return this._domains.elements();
    }

    /**
     * Gets security domains field.
     * 
     * @return security domains field
     */
    protected final String getSecurityDomainsField() {
	String str = null;
	String domainlist = this.getSecurityDomainList();

	if (domainlist != null) {
	    str = DOMAINS_FIELDNAME + FIELD_SEPARATOR + " " + domainlist;
	}
	return str;
    }

    /**
     * Gets server ID.
     * 
     * @return server ID
     */
    public final String getServerID() {
	return this._serverid;
    }

    /**
     * Gets server ID field.
     * 
     * @return server ID field
     */
    protected final String getServerIDField() {
	String str = null;

	if (this._serverid != null) {
	    str = SERVERID_FIELDNAME + FIELD_SEPARATOR + " " + this._serverid;
	}
	return str;
    }

    /**
     * Gets authentication status.
     * 
     * @return authentication status
     */
    public final int getStatus() {
	return this._status;
    }

    /**
     * Gets string of authentication status.
     * 
     * @return string of authentication status
     */
    protected final String getStatusString() {
	return Integer.toString(this._status);
    }

    /**
     * Gets authentication protocol step.
     * 
     * @return authentication protocol step
     */
    public final int getStep() {
	return this._step;
    }

    /**
     * Gets string of authentication protocol step.
     * 
     * @return string of authentication protocol step
     */
    protected final String getStepString() {
	String str = null;

	switch (this._step) {
	case Authentication.STEP_START:
	    str = STEP_START;
	    break;
	case Authentication.STEP_FIRST_TURN:
	    str = STEP_FIRST_TURN;
	    break;
	case Authentication.STEP_SECOND_TURN:
	    str = STEP_SECOND_TURN;
	    break;
	case Authentication.STEP_END:
	    str = STEP_END;
	    break;
	default:
	    str = "";
	}
	return str;
    }

    /**
     * Checks the line is top line.
     * 
     * @param line
     *            line of protocol
     * @return true if the line is top line of authentication protocol,
     *         otherwise false.
     */
    public static boolean isTopLine(String line) {
	StringTokenizer st = new StringTokenizer(line);

	if (st.countTokens() != 3) {
	    return false;
	}

	// step
	final String step = st.nextToken();

	if (!step.equalsIgnoreCase(STEP_START)
		&& !step.equalsIgnoreCase(STEP_FIRST_TURN)
		&& !step.equalsIgnoreCase(STEP_SECOND_TURN)
		&& !step.equalsIgnoreCase(STEP_END)) {
	    return false;
	}

	// status
	final String status = st.nextToken().trim();
	final Integer statusInt = new Integer(status);

	if (!status.equals(statusInt.toString())) {
	    return false;
	}

	// version
	final String version = st.nextToken();

	if (!version.equalsIgnoreCase(AUTHENTICATION_PROTOCOL_VERSION)) {
	    return false;
	}
	return true;
    }

    static final boolean isVerbose() {
	return verbose;
    }

    // /**
    // * Parses body of packet from input stream.
    // * @param in input stream for packet
    // */
    // public synchronized void parseBody(InputStream in) throws IOException {
    // DataInput di = new DataInputStream(in);
    // parseBody(di);
    // }
    //
    /**
     * Parses body of packet from data input stream.
     * 
     * @param in
     *            data input stream for packet
     */
    public synchronized void parseBody(DataInput di) throws IOException {
	String line;

	while (true) {
	    line = di.readLine().trim();
	    if (line.equalsIgnoreCase(END_OF_PACKET)) {
		verboseOut("end of authentication packet.");
		break;
	    }
	    this.parseLine(line);
	}
    }

    /**
     * Parse line.
     * 
     * @param line
     *            line of authentication protocol
     */
    private synchronized void parseLine(String line) throws IOException {
	final String fieldName = line.substring(0, line.indexOf(FIELD_SEPARATOR)).trim();
	final String fieldValue = line.substring(line.indexOf(FIELD_SEPARATOR) + 1).trim();

	verboseOut("Parse Packet Body : field name=" + fieldName);
	verboseOut("Parse Packet Body : field value=" + fieldValue);
	if (fieldName.equalsIgnoreCase(SERVERID_FIELDNAME)) {
	    this.setServerID(fieldValue);
	} else if (fieldName.equalsIgnoreCase(DOMAINS_FIELDNAME)) {
	    this.setSecurityDomains(fieldValue);
	} else if (fieldName.equalsIgnoreCase(DOMAIN_FIELDNAME)) {
	    this.setSecurityDomain(fieldValue);
	} else if (fieldName.equalsIgnoreCase(AUTH_MANNER_FIELDNAME)) {
	    this.setAuthManner(fieldValue);
	} else if (fieldName.equalsIgnoreCase(CHALLENGE_FIELDNAME)) {
	    this.setChallenge(fieldValue);
	} else if (fieldName.equalsIgnoreCase(RESPONSE_FIELDNAME)) {
	    this.setResponse(fieldValue);
	} else {
	    throw new IOException("Invalid authentication field name : "
		    + fieldName);
	}
    }

    /**
     * Parses top line.
     * 
     * @param topLine
     *            top line of authentication protocol
     */
    public synchronized void parseTopLine(String topLine) throws IOException {
	verboseOut("top line=" + topLine);
	StringTokenizer st = new StringTokenizer(topLine);

	if (st.countTokens() != 3) {
	    throw new IOException("Invalid top line : " + topLine);
	}

	// step
	final String step = st.nextToken();

	verboseOut("step=" + step);
	this.setStep(step);

	// status
	final String status = st.nextToken();

	verboseOut("status=" + status);
	this.setStatus(status);

	// version
	final String version = st.nextToken();

	verboseOut("version=" + version);
	if (!version.equalsIgnoreCase(AUTHENTICATION_PROTOCOL_VERSION)) {
	    throw new IOException("Invalid authentication version : " + version);
	}
    }

    // /**
    // * Reads packet from input stream.
    // * @param in input stream for packet
    // */
    // public synchronized void readFrom(InputStream in) throws IOException {
    // DataInput di = new DataInputStream(in);
    // final String topLine = di.readLine().trim();
    // readFrom(topLine, in);
    // }
    //
    // /**
    // * Reads packet from input stream.
    // * @param topLine top line of packet
    // * @param in input stream for packet
    // */
    // public synchronized void readFrom(String topLine, InputStream in) throws
    // IOException {
    // // top line
    // parseTopLine(topLine);
    // // next lines
    // parseBody(in);
    // }
    //
    /**
     * Reads packet from data input stream.
     * 
     * @param in
     *            input stream for packet
     */
    public synchronized void readFrom(DataInput di) throws IOException {
	final String topLine = di.readLine().trim();

	this.readFrom(topLine, di);
    }

    /**
     * Reads packet from data input stream.
     * 
     * @param topLine
     *            top line of packet
     * @param in
     *            data input stream for packet
     */
    public synchronized void readFrom(String topLine, DataInput di)
    throws IOException {

	// top line
	this.parseTopLine(topLine);

	// next lines
	this.parseBody(di);
    }

    /**
     * Sets authentication manner.
     * 
     * @param manner
     *            authentication manner
     * @exception java.lang.IllegalArgumentException
     */
    private final void setAuthManner(int manner)
    throws IllegalArgumentException {
	switch (manner) {
	case AtpConstants.NO_AUTHENTICATION_MANNER:
	case AtpConstants.AUTHENTICATION_MANNER_DIGEST:
	case AtpConstants.AUTHENTICATION_MANNER_SIGNATURE:
	    this._manner = manner;
	    break;
	default:
	    this._manner = AtpConstants.NO_AUTHENTICATION_MANNER;
	    throw new IllegalArgumentException("Illegal manner : " + manner);
	}
    }

    /**
     * Sets authentication manner.
     * 
     * @param manner
     *            string of authentication manner
     * @exception java.lang.IllegalArgumentException
     */
    private final void setAuthManner(String manner)
    throws IllegalArgumentException {
	try {
	    this.setAuthManner(toAuthManner(manner));
	} catch (IllegalArgumentException excpt) {
	    throw new IllegalArgumentException("Illegal authentication manner : "
		    + manner);
	}
    }

    /**
     * Sets challenge.
     * 
     * @param challenge
     *            challenge
     */
    private final void setChallenge(Challenge challenge) {
	this._challenge = challenge;
    }

    /**
     * Sets challenge.
     * 
     * @param challenge
     *            string of challenge
     */
    private final void setChallenge(String challenge) {
	if ((challenge == null) || challenge.equals("")) {
	    this._challenge = null;
	} else {
	    this.setChallenge(new Challenge(challenge.trim()));
	}
    }

    /**
     * Sets response to challenge.
     * 
     * @param response
     *            response to challenge
     */
    private final void setResponse(Response response) {
	this._response = response;
    }

    /**
     * Sets response to challenge.
     * 
     * @param response
     *            string of response to challenge
     */
    private final void setResponse(String response) {
	if ((response == null) || response.equals("")) {
	    this._response = null;
	} else {
	    this.setResponse(new Response(response.trim()));
	}
    }

    /**
     * Sets security domain name.
     * 
     * @param name
     *            security domain name
     */
    private final void setSecurityDomain(String name) {
	this._domain = name;
    }

    /**
     * Sets security domain names.
     * 
     * @param names
     *            security domain names (URI encoded)
     */
    private final void setSecurityDomains(String namelist) {
	this.setSecurityDomains(new StringTokenizer(namelist, DOMAIN_SEPARATOR));
    }

    /**
     * Sets security domain names.
     * 
     * @param names
     *            security domain names
     */
    private final void setSecurityDomains(Enumeration names) {
	if (names == null) {
	    return;
	}

	this._domains = new Vector();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();

	    this._domains.addElement(decode(name.trim()));
	}
    }

    /**
     * Sets server ID.
     * 
     * @param name
     *            server ID
     */
    private final void setServerID(String id) {
	this._serverid = id;
    }

    /**
     * Sets authentication status.
     * 
     * @Param status authentication status
     */
    private final void setStatus(int status) {
	this._status = status;
    }

    /**
     * Sets authentication status.
     * 
     * @Param string of authentication status
     */
    private final void setStatus(String status) {
	this.setStatus(Integer.parseInt(status.trim()));
    }

    /**
     * Sets authentication protocol step.
     * 
     * @param step
     *            authentication protocol step
     * @exception java.lang.IllegalArgumentException
     */
    private final void setStep(int step) throws IllegalArgumentException {
	switch (step) {
	case Authentication.STEP_START:
	case Authentication.STEP_FIRST_TURN:
	case Authentication.STEP_SECOND_TURN:
	case Authentication.STEP_END:
	    this._step = step;
	    break;
	default:
	    this._step = Authentication.STEP_NOT_AUTHENTICATED;
	    throw new IllegalArgumentException("Illegal step : " + step);
	}
    }

    /**
     * Sets authentication protocol step.
     * 
     * @Param step string of authentication protocol step
     * @exception java.lang.IllegalArgumentException
     */
    private final void setStep(String step) throws IllegalArgumentException {
	final String s = step.trim();
	int st = Authentication.STEP_NOT_AUTHENTICATED;

	if (s.equalsIgnoreCase(STEP_START)) {
	    st = Authentication.STEP_START;
	} else if (s.equalsIgnoreCase(STEP_FIRST_TURN)) {
	    st = Authentication.STEP_FIRST_TURN;
	} else if (s.equalsIgnoreCase(STEP_SECOND_TURN)) {
	    st = Authentication.STEP_SECOND_TURN;
	} else if (s.equalsIgnoreCase(STEP_END)) {
	    st = Authentication.STEP_END;
	} else {
	    st = Authentication.STEP_NOT_AUTHENTICATED;
	}
	this.setStep(st);
    }

    /**
     * Converts to authentication manner.
     * 
     * @param manner
     *            string of authentication manner
     * @return authentication manner code
     */
    public static int toAuthManner(String manner) {
	return toAuthManner(manner, AtpConstants.NO_AUTHENTICATION_MANNER);
    }

    /**
     * Converts to authentication manner.
     * 
     * @param manner
     *            string of authentication manner
     * @param defaultManner
     *            default authentication manner code
     * @return authentication manner code
     */
    public static int toAuthManner(String manner, int defaultManner) {
	int authManner = defaultManner;

	if (manner == null) {
	    return defaultManner;
	}

	final String m = manner.trim();

	if (m.equalsIgnoreCase(AUTH_MANNER_DIGEST)) {
	    authManner = AtpConstants.AUTHENTICATION_MANNER_DIGEST;
	} else if (m.equalsIgnoreCase(AUTH_MANNER_SIGNATURE)) {
	    authManner = AtpConstants.AUTHENTICATION_MANNER_SIGNATURE;
	}

	return authManner;
    }

    /**
     * Converts to string of authentication manner.
     * 
     * @param manner
     *            authentication manner code
     * @return string of authentication manner
     */
    public static String toAuthMannerString(int manner) {
	String authManner = null;

	switch (manner) {
	case AtpConstants.AUTHENTICATION_MANNER_DIGEST:
	    authManner = AUTH_MANNER_DIGEST;
	    break;
	case AtpConstants.AUTHENTICATION_MANNER_SIGNATURE:
	    authManner = AUTH_MANNER_SIGNATURE;
	    break;
	default:
	    authManner = null;
	}

	return authManner;
    }

    static final void verboseOut(String msg) {
	logger.debug("VO:" + msg);
    }

    /**
     * Writes packet to output stream.
     * 
     * @param out
     *            output stream for packet
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
	final String topLine = this.getStepString() + " "
	+ this.getStatusString() + " "
	+ AUTHENTICATION_PROTOCOL_VERSION + CRLF;
	final String serverIDField = this.getServerIDField();
	final String domainsField = this.getSecurityDomainsField();
	final String domainField = this.getSecurityDomainField();
	final String mannerField = this.getAuthMannerField();
	final String challengeField = this.getChallengeField();
	final String responseField = this.getResponseField();
	String packet = topLine;

	if (serverIDField != null) {
	    packet = packet + serverIDField + CRLF;
	}
	if (domainsField != null) {
	    packet = packet + domainsField + CRLF;
	}
	if (domainField != null) {
	    packet = packet + domainField + CRLF;
	}
	if (mannerField != null) {
	    packet = packet + mannerField + CRLF;
	}
	if (challengeField != null) {
	    packet = packet + challengeField + CRLF;
	}
	if (responseField != null) {
	    packet = packet + responseField + CRLF;
	}
	packet = packet + END_OF_PACKET + CRLF;
	verboseOut("packet=" + packet);
	out.write(packet.getBytes());
    }
}
