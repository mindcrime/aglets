package com.ibm.atp.auth;

/*
 * @(#)Authentication.java
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
import java.net.InetAddress;
import java.net.Socket;

import com.ibm.atp.AtpConstants;

// - import com.ibm.aglets.security.Randoms;

// - import com.ibm.awb.misc.Resource;
// - import com.ibm.awb.misc.FileUtils;

/**
 * The <tt>Authentication</tt> class is the authentication protocol class.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class Authentication {
    /**
     * 
     */
    private boolean _authenticatedMyself = false;
    private boolean _authenticatedOpponent = false;

    /**
     * Turn of protocol
     */
    private int _turn = Auth.NO_TURNS;

    /**
     * Socket to be connected/bound
     */
    private Socket _socket = null;

    /**
     * Local/Remote IP address of socket
     */
    private InetAddress _localAddr = null;
    private InetAddress _remoteAddr = null;

    /**
     * Server Identifier
     */
    private ServerIdentifier _serverIdentifier = null;

    /**
     * Input/Output stream of socket
     */

    // - private InputStream _inputStream = null;
    private DataInput _dataInput = null;
    private OutputStream _outputStream = null;

    /**
     * Authentication manner
     */
    private final static int DEFAULT_AUTHENTICATION_MANNER = AtpConstants.AUTHENTICATION_MANNER_DIGEST;

    // - private static int defaultAuthManner = DEFAULT_AUTHENTICATION_MANNER;
    // -
    // - private static void setup() {
    // - Resource res = Resource.getResourceFor("atp");
    // - final String manner = res.getString("atp.defaultAuthManner");
    // - defaultAuthManner = AuthPacket.toAuthManner(manner,
    // DEFAULT_AUTHENTICATION_MANNER);
    // - }
    // -
    // - private int _manner = defaultAuthManner;
    private int _manner = DEFAULT_AUTHENTICATION_MANNER;

    /**
     * Step of authentication protocol
     */
    final static int STEP_NOT_AUTHENTICATED = 0;
    final static int STEP_START = 1;
    final static int STEP_FIRST_TURN = 2;
    final static int STEP_SECOND_TURN = 3;
    final static int STEP_END = 4;

    private int _step = STEP_NOT_AUTHENTICATED;

    /**
     * Status of authentication
     */
    final static int STATUS_NORMAL = 0;
    final static int STATUS_AUTHENTICATION_FAILED = 1;
    final static int STATUS_ILLEGAL_STEP = 2;
    final static int STATUS_UNKNOWN_DOMAIN = 3;
    final static int STATUS_UNKNOWN_MANNER = 4;
    final static int STATUS_INCONSISTENT_MANNER = 5;
    final static int STATUS_ERROR = 9;

    private int _status = STATUS_NORMAL;

    /**
     * Security domains
     */
    private SharedSecret _selectedSecret = null;
    private String _selectedDomainname = null;

    /**
     * Default constructor creates an challenge-response authentication protocol
     * handler.
     * 
     * @param turn
     *            turn of protocol
     * @param di
     *            data input of packet
     * @param socket
     *            socket to be connected/bound
     */
    public Authentication(int turn, DataInput di, Socket socket) {
	this(turn, di, socket, DEFAULT_AUTHENTICATION_MANNER);
    }

    /**
     * Constructor creates an challenge-response authentication protocol
     * handler.
     * 
     * @param turn
     *            turn of protocol
     * @param di
     *            data input of packet
     * @param socket
     *            socket to be connected/bound
     * @param manner
     *            challenge-response authentication manner
     */
    public Authentication(int turn, DataInput di, Socket socket, int manner) {
	this.setTurn(turn);
	this.setDataInput(di);
	this.setSocket(socket);
	this.setAuthManner(manner);
    }

    /**
     * Process authentication protocol.
     * 
     * @exception AuthenticationProtocolException
     *                incorrect protocol
     * @exception IOException
     */
    public final synchronized boolean authenticate()
    throws AuthenticationProtocolException,
    IOException {
	if (this._step != STEP_NOT_AUTHENTICATED) {
	    this._status = STATUS_ERROR;
	    throw new AuthenticationProtocolException("Illegal initial step.");
	}

	verboseOut("Authentication start.");

	if (this._turn == Auth.FIRST_TURN) {
	    this.authenticateFirstTurn();
	} else if (this._turn == Auth.SECOND_TURN) {
	    this.authenticateSecondTurn();
	} else {
	    this._status = STATUS_ERROR;
	    throw new AuthenticationProtocolException("Illegal turn : "
		    + this._turn);
	}

	verboseOut("Authentication end.");

	if (this._status != STATUS_NORMAL) {
	    return false;
	}

	return true;
    }

    /**
     * Process authentication protocol for first turn individual.
     * 
     * @exception IOException
     */
    private final synchronized void authenticateFirstTurn() throws IOException {
	if (this._turn != Auth.FIRST_TURN) {
	    System.err.println("Not 1st turn.");
	    this._status = STATUS_ERROR;
	    return;
	}

	verboseOut("Authentication : 1st turn.");

	int manner = AtpConstants.NO_AUTHENTICATION_MANNER;

	Auth auth = null;
	Challenge challenge = null;
	Response response = null;

	AuthPacket packet = null;

	// 1 : STEP_START
	// send packet
	verboseOut("Authentication : 1st turn : step=START");
	this._step = STEP_START;
	SharedSecrets secrets = SharedSecrets.getSharedSecrets();

	packet = new AuthPacket(this._step, this._status, secrets.getDomainNames(), AtpConstants.NO_AUTHENTICATION_MANNER, null, null);
	verboseOut("Authentication : 1st turn : step=START : sending packet ... ");
	packet.writeTo(this._outputStream);
	verboseOut("packet sent.");
	this._status = STATUS_NORMAL;

	// 2 : STEP_FIRST_TURN
	// receive packet
	verboseOut("Authentication : 1st turn : step=FIRST_TURN");
	verboseOut("Authentication : 1st turn : step=FIRST_TURN : receiving packet ... ");

	// packet = new AuthPacket(_inputStream);
	packet = new AuthPacket(this._dataInput);
	verboseOut("packet received.");
	this._status = packet.getStatus();
	verboseOut("Authentication : status=" + this._status);
	if (this._status != STATUS_NORMAL) {

	    // something wrong
	    // do nothing ?
	    return; // #
	}
	if (packet.getStep() != STEP_FIRST_TURN) {

	    // something wrong
	    verboseOut("Authentication : step=" + packet.getStep());
	    this._status = STATUS_ILLEGAL_STEP;
	    return; // #
	}
	this._selectedSecret = secrets.getSharedSecret(packet.getSecurityDomain());
	if (this._selectedSecret == null) {

	    // selected security domain is unknown
	    verboseOut("Authentication : unknown domain="
		    + packet.getSecurityDomain());
	    this._status = STATUS_UNKNOWN_DOMAIN;
	    return; // #
	} else {

	    // selected security domain
	    this._selectedDomainname = this._selectedSecret.getDomainName();
	    verboseOut("Authentication : selected domain="
		    + this._selectedDomainname);
	}

	// _status = STATUS_NORMAL;

	// 3 : STEP_SECOND_TURN
	// send packet
	verboseOut("Authentication : 1st turn : step=SECOND_TURN");
	this._step = STEP_SECOND_TURN;
	manner = packet.getAuthManner();
	verboseOut("Authentication : 1st turn : step=SECOND_TURN : manner="
		+ manner);
	this.setAuthManner(manner);
	challenge = packet.getChallenge();
	if (challenge != null) {

	    // challenge is given; to be authenticated
	    verboseOut("Authentication : 1st turn : step=SECOND_TURN : response of challenge is requested.");
	    if (manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST) {

		// authentication procedure with shared secret
		auth = new AuthByDigest(this._selectedSecret);
	    } else if (manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

		// authentication procedure with digital signature
		// ? auth = new AuthBySignature(_privateKey,
		// _publicKeyOpponent);
	    } else {

		// something wrong
		this._status = STATUS_UNKNOWN_MANNER;
		return; // #
	    }
	    if (auth != null) {
		auth.setFirstTurnIdentifier(this._localAddr.getHostAddress());
		auth.setSecondTurnIdentifier(this._remoteAddr.getHostAddress());
		try {
		    response = new Response(auth.calculateResponse(Auth.FIRST_TURN, challenge));
		} catch (AuthenticationException excpt) {

		    // authentication is failed
		    System.err.println(excpt);
		    response = null;
		    this._status = STATUS_ERROR;
		    return; // #
		}
	    }
	} else {

	    // challenge is not given; not need to send response
	    verboseOut("Authentication : 1st turn : step=SECOND_TURN : response of challenge is NOT requested.");
	    response = null;
	}

	// ! if(AuthenticationManager.isAuthenticated(packet.getServerID())) {
	// ! // already authenticated; need no more authentication
	// !
	// verboseOut("Authentication : 1st turn : step=SECOND_TURN : already authenticated.");
	// ! _authenticatedOpponent = true;
	// ! challenge = null;
	// ! } else {
	// ! // not authenticated; need authentication
	// !
	// verboseOut("Authentication : 1st turn : step=SECOND_TURN : NOT authenticated.");
	this._authenticatedOpponent = false;
	challenge = new Challenge();

	// ! }
	packet = new AuthPacket(this._step, this._status, this._selectedDomainname, manner, challenge, response);
	verboseOut("Authentication : 1st turn : step=SECOND_TURN : sending packet ... ");
	packet.writeTo(this._outputStream);
	verboseOut("packet sent.");
	this._status = STATUS_NORMAL;

	// 4 : STEP_END
	// receive packet
	verboseOut("Authentication : 1st turn : step=END");
	verboseOut("Authentication : 1st turn : step=END : receiving packet ... ");

	// packet = new AuthPacket(_inputStream);
	packet = new AuthPacket(this._dataInput);
	verboseOut("packet received.");
	this._status = packet.getStatus();
	verboseOut("Authentication : status=" + this._status);
	if (this._status == STATUS_AUTHENTICATION_FAILED) {

	    // Opponent did not authenticate me
	    this._authenticatedMyself = false;
	    return; // #
	} else {

	    // Opponent authenticated me
	    this._authenticatedMyself = true;
	}
	if (packet.getStep() != STEP_END) {

	    // something wrong
	    verboseOut("Authentication : step=" + packet.getStep());
	    this._status = STATUS_ILLEGAL_STEP;
	    return; // #
	}
	if (!this._selectedDomainname.equals(packet.getSecurityDomain())) {

	    // something wrong
	    verboseOut("Authentication : unexpected domain="
		    + packet.getSecurityDomain());
	    this._status = STATUS_UNKNOWN_DOMAIN;
	    return; // #
	}
	if (challenge != null) {

	    // need to authenticate remote
	    verboseOut("Authentication : 1st turn : step=END : response of challenge is requested.");
	    manner = packet.getAuthManner();
	    if (manner != this._manner) {

		// something wrong
		this._status = STATUS_INCONSISTENT_MANNER;
		return; // #
	    }
	    response = packet.getResponse();
	    if ((manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST)
		    || (manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE)) {

		// authentication procedure with shared secret, or
		// authentication procedure with digital signature
		try {
		    if ((auth != null)
			    && (response != null)
			    && auth.verify(Auth.SECOND_TURN, challenge, response)) {

			// verified
			verboseOut("Authentication : 1st turn : step=END : verified.");

			// !
			// AuthenticationManager.register(packet.getServerID());
			AuthenticationManager.register(this._serverIdentifier, this._selectedDomainname);
			verboseOut("Authentication : 1st turn : step=END : authenticated.");
			this._authenticatedOpponent = true;
			this._status = STATUS_NORMAL;
		    } else {

			// not verified
			verboseOut("Authentication : 1st turn : step=END : NOT verified.");
			this._authenticatedOpponent = false;
			this._status = STATUS_AUTHENTICATION_FAILED;
			return; // #
		    }
		} catch (AuthenticationException excpt) {

		    // Authentication is failed
		    System.err.println(excpt);
		    this._authenticatedOpponent = false;
		    this._status = STATUS_AUTHENTICATION_FAILED;
		    return; // #
		}
	    } else {

		// something wrong
		this._status = STATUS_UNKNOWN_MANNER;
		return; // #
	    }
	} else {

	    // not need to authenticate remote
	    verboseOut("Authentication : 1st turn : step=END : response of challenge is NOT requested.");
	    this._authenticatedOpponent = true;
	    this._status = STATUS_NORMAL;
	}

	// _status = STATUS_NORMAL;
    }

    /**
     * Process authentication protocol for second turn individual.
     * 
     * @exception IOException
     */
    private final synchronized void authenticateSecondTurn() throws IOException {
	if (this._turn != Auth.SECOND_TURN) {
	    System.err.println("Not 2nd turn.");
	    this._status = STATUS_ERROR;
	    return;
	}

	verboseOut("Authentication : 2nd turn.");

	int manner = AtpConstants.NO_AUTHENTICATION_MANNER;

	Auth auth = null;
	Challenge challenge = null;
	Response response = null;

	AuthPacket packet = null;

	// 1 : STEP_START
	// receive packet
	verboseOut("Authentication : 2nd turn : step=START");
	verboseOut("Authentication : 2nd turn : step=START : receiving packet ... ");

	// packet = new AuthPacket(_inputStream);
	packet = new AuthPacket(this._dataInput);
	verboseOut("packet received.");
	this._status = packet.getStatus();
	verboseOut("Authentication : status=" + this._status);
	if (this._status != STATUS_NORMAL) {

	    // something wrong
	    // do nothing ?
	    return; // #
	}
	if (packet.getStep() != STEP_START) {

	    // something wrong
	    verboseOut("Authentication : step=" + packet.getStep());
	    this._status = STATUS_ILLEGAL_STEP;
	    return; // #
	}
	SharedSecrets secrets = SharedSecrets.getSharedSecrets();

	this._selectedSecret = secrets.selectSharedSecret(packet.getSecurityDomains());
	if (this._selectedSecret == null) {

	    // selected security domain is unknown
	    verboseOut("Authentication : unknown domain="
		    + packet.getSecurityDomain());
	    this._status = STATUS_UNKNOWN_DOMAIN;
	    return; // #
	} else {

	    // selected security domain
	    this._selectedDomainname = this._selectedSecret.getDomainName();
	    verboseOut("Authentication : selected domain="
		    + this._selectedDomainname);
	}
	if (this._manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST) {

	    // authentication procedure with shared secret
	    auth = new AuthByDigest(this._selectedSecret);
	} else if (this._manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

	    // authentication procedure with digital signature
	    // ? auth = new AuthBySignature(_privateKey, _publicKeyOpponent);
	} else {

	    // something wrong
	    this._status = STATUS_UNKNOWN_MANNER;
	    return; // #
	}
	if (auth != null) {
	    auth.setFirstTurnIdentifier(this._remoteAddr.getHostAddress());
	    auth.setSecondTurnIdentifier(this._localAddr.getHostAddress());
	}

	// _status = STATUS_NORMAL;

	// 2 : STEP_FIRST_TURN
	// send packet
	verboseOut("Authentication : 2nd turn : step=FIRST_TURN");
	this._step = STEP_FIRST_TURN;
	manner = this._manner;

	// ! if(AuthenticationManager.isAuthenticated(packet.getServerID())) {
	// ! // already authenticated; need no more authentication
	// !
	// verboseOut("Authentication : 2nd turn : step=FIRST_TURN : NOT request response of challenge.");
	// ! _authenticatedOpponent = true;
	// ! challenge = null;
	// ! } else {
	// ! // not authenticated; need authentication
	// !
	// verboseOut("Authentication : 2nd turn : step=FIRST_TURN : request response of challenge.");
	this._authenticatedOpponent = false;
	challenge = new Challenge();
	if ((manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST)
		|| (manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE)) {

	    // authentication procedure with shared secret, or
	    // authentication procedure with digital signature
	} else {

	    // something wrong
	    this._status = STATUS_UNKNOWN_MANNER;
	    return; // #
	}

	// ! }
	packet = new AuthPacket(this._step, this._status, this._selectedDomainname, manner, challenge, null);
	verboseOut("Authentication : 2nd turn : step=FIRST_TURN : sending packet ... ");
	packet.writeTo(this._outputStream);
	verboseOut("packet sent.");
	this._status = STATUS_NORMAL;

	// 3 : STEP_SECOND_TURN
	// receive packet
	verboseOut("Authentication : 2nd turn : step=SECOND_TURN");
	verboseOut("Authentication : 2nd turn : step=SECOND_TURN : receiving packet ... ");

	// packet = new AuthPacket(_inputStream);
	packet = new AuthPacket(this._dataInput);
	verboseOut("packet received.");
	this._status = packet.getStatus();
	verboseOut("Authentication : status=" + this._status);
	if (this._status == STATUS_AUTHENTICATION_FAILED) {

	    // Opponent did not authenticate me
	    this._authenticatedMyself = false;
	    return; // #
	} else {

	    // Opponent authenticated me
	    this._authenticatedMyself = true;
	}
	if (packet.getStep() != STEP_SECOND_TURN) {

	    // something wrong
	    verboseOut("Authentication : step=" + packet.getStep());
	    this._status = STATUS_ILLEGAL_STEP;
	    return; // #
	}
	if (!this._selectedDomainname.equals(packet.getSecurityDomain())) {

	    // something wrong
	    verboseOut("Authentication : unexpected domain="
		    + packet.getSecurityDomain());
	    this._status = STATUS_UNKNOWN_DOMAIN;
	    return; // #
	}
	if (challenge != null) {

	    // need to authenticate remote
	    manner = packet.getAuthManner();
	    if (manner != this._manner) {

		// something wrong
		this._status = STATUS_INCONSISTENT_MANNER;
		return; // #
	    }
	    response = packet.getResponse();
	    if ((manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST)
		    || (manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE)) {

		// authentication procedure with shared secret
		// authentication procedure with digital signature
		try {
		    if ((auth != null)
			    && (response != null)
			    && auth.verify(Auth.FIRST_TURN, challenge, response)) {

			// verified
			verboseOut("Authentication : 2nd turn : step=SECOND_TURN : verified.");

			// !
			// AuthenticationManager.register(packet.getServerID());
			AuthenticationManager.register(this._serverIdentifier, this._selectedDomainname);
			this._authenticatedOpponent = true;
			this._status = STATUS_NORMAL;
		    } else {

			// not verified
			verboseOut("Authentication : 2nd turn : step=SECOND_TURN : NOT verified.");
			this._authenticatedOpponent = false;
			this._status = STATUS_AUTHENTICATION_FAILED;
			return; // #
		    }
		} catch (AuthenticationException excpt) {

		    // Authentication is failed
		    System.err.println(excpt);
		    this._authenticatedOpponent = false;
		    this._status = STATUS_AUTHENTICATION_FAILED;
		    return; // #
		}
	    } else {

		// something wrong
		this._status = STATUS_UNKNOWN_MANNER;
		return; // #
	    }
	} else {

	    // not need to authenticate remote
	    this._authenticatedOpponent = true;
	    this._status = STATUS_NORMAL;
	}

	// _status = STATUS_NORMAL;

	// 4 : STEP_END
	// send packet
	verboseOut("Authentication : 2nd turn : step=END");
	this._step = STEP_END;
	if (this._status == STATUS_AUTHENTICATION_FAILED) {

	    // authentication failed, send no response
	    manner = this._manner;
	    response = null;
	} else {
	    manner = packet.getAuthManner();
	    if (manner != this._manner) {

		// something wrong
		this._status = STATUS_INCONSISTENT_MANNER;
		return; // #
	    }
	    challenge = packet.getChallenge();
	    if (challenge != null) {

		// challenge is given; to be authenticated
		verboseOut("Authentication : 2nd turn : step=END : response of challenge is requested.");
		if ((manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST)
			|| (manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE)) {

		    // authentication procedure with shared secret
		    // authentication procedure with digital signature
		    try {
			response = new Response(auth.calculateResponse(Auth.SECOND_TURN, challenge));
		    } catch (AuthenticationException excpt) {

			// authentication is failed
			System.err.println(excpt);
			response = null;
			this._status = STATUS_ERROR;
			return; // #
		    }
		} else {

		    // something wrong
		    this._status = STATUS_UNKNOWN_MANNER;
		    return; // #
		}
	    } else {

		// challenge is not given; not need to send response
		verboseOut("Authentication : 2nd turn : step=END : response of challenge is NOT requested.");
		response = null;
		this._status = STATUS_NORMAL;
	    }
	}
	packet = new AuthPacket(this._step, this._status, this._selectedDomainname, manner, null, response);
	verboseOut("Authentication : 2nd turn : step=END : sending packet ... ");
	packet.writeTo(this._outputStream);
	verboseOut("packet sent.");
	this._status = STATUS_NORMAL;
    }

    /**
     * Returns authentication manner
     * 
     * @return authentication manner
     */
    public final int getAuthManner() {
	return this._manner;
    }

    // /**
    // * Returns input stream of socket to be connected/bound
    // * @return input stream of socket to be connected/bound
    // */
    // public final InputStream getInputStream() {
    // return _inputStream;
    // }
    //
    /**
     * Returns data input of packet
     * 
     * @return data input of packet
     */
    public final DataInput getDataInput() {
	return this._dataInput;
    }

    /**
     * Returns local IP address of socket to be connected/bound
     * 
     * @return local IP address of socket to be connected/bound
     */
    public final InetAddress getLocalAddress() {
	return this._localAddr;
    }

    /**
     * Returns output stream of socket to be connected/bound
     * 
     * @return output stream of socket to be connected/bound
     */
    public final OutputStream getOutputStream() {
	return this._outputStream;
    }

    /**
     * Returns remote IP address of socket to be connected/bound
     * 
     * @return remote IP address of socket to be connected/bound
     */
    public final InetAddress getRemoteAddress() {
	return this._remoteAddr;
    }

    /**
     * Returns selected security domain name.
     * 
     * @return selected security domain name
     */
    public final String getSelectedDomainName() {
	return this._selectedDomainname;
    }

    /**
     * Returns shared secret for selected security domain.
     * 
     * @return shared secret for selected security domain
     */
    public final SharedSecret getSelectedSecret() {
	return this._selectedSecret;
    }

    /**
     * Returns socket to be connected/bound.
     * 
     * @return socket to be connected/bound
     */
    public final Socket getSocket() {
	return this._socket;
    }

    /**
     * Returns turn of protocol.
     * 
     * @return turn of protocol
     */
    public final int getTurn() {
	return this._turn;
    }

    public boolean isAuthenticatedMyself() {
	return this._authenticatedMyself;
    }

    public boolean isAuthenticatedOpponent() {
	return this._authenticatedOpponent;
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
	if ((manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST)
		|| (manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE)) {
	    this._manner = manner;
	} else {
	    this._manner = AtpConstants.NO_AUTHENTICATION_MANNER;
	    throw new IllegalArgumentException("Illegal manner : " + manner);
	}
    }

    /**
     * Sets data input of packet.
     * 
     * @param di
     *            data input of packet
     */
    private final void setDataInput(DataInput di) {
	this._dataInput = di;
    }

    /**
     * Sets socket to be connected/bound.
     * 
     * @param socket
     *            socket to be connected/bound
     */
    private final void setSocket(Socket socket) {
	this._socket = socket;
	this._localAddr = this._socket.getLocalAddress();
	this._remoteAddr = this._socket.getInetAddress();
	this._serverIdentifier = new ServerIdentifier(this._socket);
	try {

	    // _inputStream = _socket.getInputStream();
	    this._outputStream = this._socket.getOutputStream();
	} catch (IOException excpt) {

	    // _inputStream = null;
	    this._outputStream = null;
	}
    }

    /**
     * Sets turn of protocol.
     * 
     * @param turn
     *            turn of protocol
     * @exception java.lang.IllegalArgumentException
     */
    private final void setTurn(int turn) throws IllegalArgumentException {
	if ((turn == Auth.FIRST_TURN) || (turn == Auth.SECOND_TURN)) {
	    this._turn = turn;
	} else {
	    this._turn = Auth.NO_TURNS;
	    throw new IllegalArgumentException("Illegal turn : " + turn);
	}
    }

    private static final void verboseOut(String msg) {
	AuthPacket.verboseOut(msg);
    }
}
