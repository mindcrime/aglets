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

import java.security.AccessController;

// - import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInput;

// - import java.io.FileInputStream;
// - import java.io.FileOutputStream;
// - import java.io.ObjectInputStream;
// - import java.io.ObjectOutputStream;
// - import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;

// - import java.security.PrivateKey;
// - import java.security.PublicKey;
// - import java.security.KeyPairGenerator;
// - import java.security.KeyPair;
// - import java.security.KeyManagementException;
// - import java.security.NoSuchAlgorithmException;

import com.ibm.atp.AtpConstants;

// - import com.ibm.aglets.security.Randoms;

// - import com.ibm.awb.misc.Resource;
// - import com.ibm.awb.misc.FileUtils;

/**
 * The <tt>Authentication</tt> class is the authentication protocol class.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:41 $
 * @author      ONO Kouichi
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

	// -   private InputStream  _inputStream  = null;
	private DataInput _dataInput = null;
	private OutputStream _outputStream = null;

	/**
	 * Shared Secret
	 */
	private static SharedSecrets _secrets = null;

	// -   /**
	// -    * The Key Pair Generator algorithm
	// -    */
	// -   private final static String KEYPAIRGENERATORALGORITHM = "DSA";
	// -
	// -   /**
	// -    * Strength of the key (modulus length)
	// -    */
	// -   private final static int KEYSTRENGTH = 1024;
	// -
	// -   /**
	// -    * length of seed
	// -    */
	// -   private final static int SEEDLENGTH = 32;
	// -
	// -   /**
	// -    * A public/private key pair generator
	// -    */
	// -   private static KeyPairGenerator _keyPairGen = null;
	// -
	// -   /**
	// -    * Private/Public Key
	// -    */
	// -   private static PrivateKey _privateKey        = null;
	// -   private static PublicKey  _publicKey         = null;
	// -   private static PublicKey  _publicKeyOpponent = null;

	/**
	 * Authentication manner
	 */
	private final static int DEFAULT_AUTHENTICATION_MANNER = 
		AtpConstants.AUTHENTICATION_MANNER_DIGEST;

	// -   private static int defaultAuthManner = DEFAULT_AUTHENTICATION_MANNER;
	// -
	// -   private static void setup() {
	// -     Resource res = Resource.getResourceFor("atp");
	// -     final String manner = res.getString("atp.defaultAuthManner");
	// -     defaultAuthManner = AuthPacket.toAuthManner(manner, DEFAULT_AUTHENTICATION_MANNER);
	// -   }
	// -
	// -   private int _manner = defaultAuthManner;
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
	 * Default constructor creates an challenge-response authentication protocol handler.
	 * @param turn turn of protocol
	 * @param di data input of packet
	 * @param socket socket to be connected/bound
	 */
	public Authentication(int turn, DataInput di, Socket socket) {
		this(turn, di, socket, DEFAULT_AUTHENTICATION_MANNER);
	}
	/**
	 * Constructor creates an challenge-response authentication protocol handler.
	 * @param turn turn of protocol
	 * @param di data input of packet
	 * @param socket socket to be connected/bound
	 * @param manner challenge-response authentication manner
	 */
	public Authentication(int turn, DataInput di, Socket socket, int manner) {
		setTurn(turn);
		setDataInput(di);
		setSocket(socket);
		setAuthManner(manner);
	}
	/**
	 * Process authentication protocol.
	 * @exception AuthenticationProtocolException incorrect protocol
	 * @exception IOException
	 */
	public final synchronized boolean authenticate() 
			throws AuthenticationProtocolException, IOException {
		if (_step != STEP_NOT_AUTHENTICATED) {
			_status = STATUS_ERROR;
			throw new AuthenticationProtocolException("Illegal initial step.");
		} 

		verboseOut("Authentication start.");

		if (_turn == Auth.FIRST_TURN) {
			authenticateFirstTurn();
		} else if (_turn == Auth.SECOND_TURN) {
			authenticateSecondTurn();
		} else {
			_status = STATUS_ERROR;
			throw new AuthenticationProtocolException("Illegal turn : " 
													  + _turn);
		} 

		verboseOut("Authentication end.");

		if (_status != STATUS_NORMAL) {
			return false;
		} 

		return true;
	}
	/**
	 * Process authentication protocol for first turn individual.
	 * @exception IOException
	 */
	private final synchronized void authenticateFirstTurn() 
			throws IOException {
		if (_turn != Auth.FIRST_TURN) {
			System.err.println("Not 1st turn.");
			_status = STATUS_ERROR;
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
		_step = STEP_START;
		SharedSecrets secrets = SharedSecrets.getSharedSecrets();

		packet = new AuthPacket(_step, _status, secrets.getDomainNames(), 
								AtpConstants.NO_AUTHENTICATION_MANNER, null, 
								null);
		verboseOut("Authentication : 1st turn : step=START : sending packet ... ");
		packet.writeTo(_outputStream);
		verboseOut("packet sent.");
		_status = STATUS_NORMAL;

		// 2 : STEP_FIRST_TURN
		// receive packet
		verboseOut("Authentication : 1st turn : step=FIRST_TURN");
		verboseOut("Authentication : 1st turn : step=FIRST_TURN : receiving packet ... ");

		// packet = new AuthPacket(_inputStream);
		packet = new AuthPacket(_dataInput);
		verboseOut("packet received.");
		_status = packet.getStatus();
		verboseOut("Authentication : status=" + _status);
		if (_status != STATUS_NORMAL) {

			// something wrong
			// do nothing ?
			return;		// #
		} 
		if (packet.getStep() != STEP_FIRST_TURN) {

			// something wrong
			verboseOut("Authentication : step=" + packet.getStep());
			_status = STATUS_ILLEGAL_STEP;
			return;		// #
		} 
		_selectedSecret = secrets.getSharedSecret(packet.getSecurityDomain());
		if (_selectedSecret == null) {

			// selected security domain is unknown
			verboseOut("Authentication : unknown domain=" 
					   + packet.getSecurityDomain());
			_status = STATUS_UNKNOWN_DOMAIN;
			return;		// #
		} else {

			// selected security domain
			_selectedDomainname = _selectedSecret.getDomainName();
			verboseOut("Authentication : selected domain=" 
					   + _selectedDomainname);
		} 

		// _status = STATUS_NORMAL;

		// 3 : STEP_SECOND_TURN
		// send packet
		verboseOut("Authentication : 1st turn : step=SECOND_TURN");
		_step = STEP_SECOND_TURN;
		manner = packet.getAuthManner();
		verboseOut("Authentication : 1st turn : step=SECOND_TURN : manner=" 
				   + manner);
		setAuthManner(manner);
		challenge = packet.getChallenge();
		if (challenge != null) {

			// challenge is given; to be authenticated
			verboseOut("Authentication : 1st turn : step=SECOND_TURN : response of challenge is requested.");
			if (manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST) {

				// authentication procedure with shared secret
				auth = new AuthByDigest(_selectedSecret);
			} else if (manner 
					   == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

				// authentication procedure with digital signature
				// ? 	auth = new AuthBySignature(_privateKey, _publicKeyOpponent);
			} else {

				// something wrong
				_status = STATUS_UNKNOWN_MANNER;
				return;			// #
			} 
			if (auth != null) {
				auth.setFirstTurnIdentifier(_localAddr.getHostAddress());
				auth.setSecondTurnIdentifier(_remoteAddr.getHostAddress());
				try {
					response = 
						new Response(auth.calculateResponse(Auth.FIRST_TURN, 
															challenge));
				} catch (AuthenticationException excpt) {

					// authentication is failed
					System.err.println(excpt);
					response = null;
					_status = STATUS_ERROR;
					return;		// #
				} 
			} 
		} else {

			// challenge is not given; not need to send response
			verboseOut("Authentication : 1st turn : step=SECOND_TURN : response of challenge is NOT requested.");
			response = null;
		} 

		// !     if(AuthenticationManager.isAuthenticated(packet.getServerID())) {
		// !       // already authenticated; need no more authentication
		// !       verboseOut("Authentication : 1st turn : step=SECOND_TURN : already authenticated.");
		// !       _authenticatedOpponent = true;
		// !       challenge = null;
		// !     } else {
		// !       // not authenticated; need authentication
		// !       verboseOut("Authentication : 1st turn : step=SECOND_TURN : NOT authenticated.");
		_authenticatedOpponent = false;
		challenge = new Challenge();

		// !     }
		packet = new AuthPacket(_step, _status, _selectedDomainname, manner, 
								challenge, response);
		verboseOut("Authentication : 1st turn : step=SECOND_TURN : sending packet ... ");
		packet.writeTo(_outputStream);
		verboseOut("packet sent.");
		_status = STATUS_NORMAL;

		// 4 : STEP_END
		// receive packet
		verboseOut("Authentication : 1st turn : step=END");
		verboseOut("Authentication : 1st turn : step=END : receiving packet ... ");

		// packet = new AuthPacket(_inputStream);
		packet = new AuthPacket(_dataInput);
		verboseOut("packet received.");
		_status = packet.getStatus();
		verboseOut("Authentication : status=" + _status);
		if (_status == STATUS_AUTHENTICATION_FAILED) {

			// Opponent did not authenticate me
			_authenticatedMyself = false;
			return;		// #
		} else {

			// Opponent authenticated me
			_authenticatedMyself = true;
		} 
		if (packet.getStep() != STEP_END) {

			// something wrong
			verboseOut("Authentication : step=" + packet.getStep());
			_status = STATUS_ILLEGAL_STEP;
			return;		// #
		} 
		if (!_selectedDomainname.equals(packet.getSecurityDomain())) {

			// something wrong
			verboseOut("Authentication : unexpected domain=" 
					   + packet.getSecurityDomain());
			_status = STATUS_UNKNOWN_DOMAIN;
			return;		// #
		} 
		if (challenge != null) {

			// need to authenticate remote
			verboseOut("Authentication : 1st turn : step=END : response of challenge is requested.");
			manner = packet.getAuthManner();
			if (manner != _manner) {

				// something wrong
				_status = STATUS_INCONSISTENT_MANNER;
				return;				// #
			} 
			response = packet.getResponse();
			if (manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST 
					|| manner 
					   == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

				// authentication procedure with shared secret, or
				// authentication procedure with digital signature
				try {
					if (auth != null && response != null 
							&& auth.verify(Auth.SECOND_TURN, challenge, 
										   response)) {

						// verified
						verboseOut("Authentication : 1st turn : step=END : verified.");

						// ! 	    AuthenticationManager.register(packet.getServerID());
						AuthenticationManager.register(_serverIdentifier, 
													   _selectedDomainname);
						verboseOut("Authentication : 1st turn : step=END : authenticated.");
						_authenticatedOpponent = true;
						_status = STATUS_NORMAL;
					} else {

						// not verified
						verboseOut("Authentication : 1st turn : step=END : NOT verified.");
						_authenticatedOpponent = false;
						_status = STATUS_AUTHENTICATION_FAILED;
						return;		// #
					} 
				} catch (AuthenticationException excpt) {

					// Authentication is failed
					System.err.println(excpt);
					_authenticatedOpponent = false;
					_status = STATUS_AUTHENTICATION_FAILED;
					return;			// #
				} 
			} else {

				// something wrong
				_status = STATUS_UNKNOWN_MANNER;
				return;				// #
			} 
		} else {

			// not need to authenticate remote
			verboseOut("Authentication : 1st turn : step=END : response of challenge is NOT requested.");
			_authenticatedOpponent = true;
			_status = STATUS_NORMAL;
		} 

		// _status = STATUS_NORMAL;
	}
	/**
	 * Process authentication protocol for second turn individual.
	 * @exception IOException
	 */
	private final synchronized void authenticateSecondTurn() 
			throws IOException {
		if (_turn != Auth.SECOND_TURN) {
			System.err.println("Not 2nd turn.");
			_status = STATUS_ERROR;
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
		packet = new AuthPacket(_dataInput);
		verboseOut("packet received.");
		_status = packet.getStatus();
		verboseOut("Authentication : status=" + _status);
		if (_status != STATUS_NORMAL) {

			// something wrong
			// do nothing ?
			return;		// #
		} 
		if (packet.getStep() != STEP_START) {

			// something wrong
			verboseOut("Authentication : step=" + packet.getStep());
			_status = STATUS_ILLEGAL_STEP;
			return;		// #
		} 
		SharedSecrets secrets = SharedSecrets.getSharedSecrets();

		_selectedSecret = 
			secrets.selectSharedSecret(packet.getSecurityDomains());
		if (_selectedSecret == null) {

			// selected security domain is unknown
			verboseOut("Authentication : unknown domain=" 
					   + packet.getSecurityDomain());
			_status = STATUS_UNKNOWN_DOMAIN;
			return;		// #
		} else {

			// selected security domain
			_selectedDomainname = _selectedSecret.getDomainName();
			verboseOut("Authentication : selected domain=" 
					   + _selectedDomainname);
		} 
		if (_manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST) {

			// authentication procedure with shared secret
			auth = new AuthByDigest(_selectedSecret);
		} else if (_manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

			// authentication procedure with digital signature
			// ?       auth = new AuthBySignature(_privateKey, _publicKeyOpponent);
		} else {

			// something wrong
			_status = STATUS_UNKNOWN_MANNER;
			return;		// #
		} 
		if (auth != null) {
			auth.setFirstTurnIdentifier(_remoteAddr.getHostAddress());
			auth.setSecondTurnIdentifier(_localAddr.getHostAddress());
		} 

		// _status = STATUS_NORMAL;

		// 2 : STEP_FIRST_TURN
		// send packet
		verboseOut("Authentication : 2nd turn : step=FIRST_TURN");
		_step = STEP_FIRST_TURN;
		manner = _manner;

		// !     if(AuthenticationManager.isAuthenticated(packet.getServerID())) {
		// !       // already authenticated; need no more authentication
		// !       verboseOut("Authentication : 2nd turn : step=FIRST_TURN : NOT request response of challenge.");
		// !       _authenticatedOpponent = true;
		// !       challenge = null;
		// !     } else {
		// !       // not authenticated; need authentication
		// !       verboseOut("Authentication : 2nd turn : step=FIRST_TURN : request response of challenge.");
		_authenticatedOpponent = false;
		challenge = new Challenge();
		if (manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST 
				|| manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

			// authentication procedure with shared secret, or
			// authentication procedure with digital signature
		} else {

			// something wrong
			_status = STATUS_UNKNOWN_MANNER;
			return;		// #
		} 

		// !     }
		packet = new AuthPacket(_step, _status, _selectedDomainname, manner, 
								challenge, null);
		verboseOut("Authentication : 2nd turn : step=FIRST_TURN : sending packet ... ");
		packet.writeTo(_outputStream);
		verboseOut("packet sent.");
		_status = STATUS_NORMAL;

		// 3 : STEP_SECOND_TURN
		// receive packet
		verboseOut("Authentication : 2nd turn : step=SECOND_TURN");
		verboseOut("Authentication : 2nd turn : step=SECOND_TURN : receiving packet ... ");

		// packet = new AuthPacket(_inputStream);
		packet = new AuthPacket(_dataInput);
		verboseOut("packet received.");
		_status = packet.getStatus();
		verboseOut("Authentication : status=" + _status);
		if (_status == STATUS_AUTHENTICATION_FAILED) {

			// Opponent did not authenticate me
			_authenticatedMyself = false;
			return;		// #
		} else {

			// Opponent authenticated me
			_authenticatedMyself = true;
		} 
		if (packet.getStep() != STEP_SECOND_TURN) {

			// something wrong
			verboseOut("Authentication : step=" + packet.getStep());
			_status = STATUS_ILLEGAL_STEP;
			return;		// #
		} 
		if (!_selectedDomainname.equals(packet.getSecurityDomain())) {

			// something wrong
			verboseOut("Authentication : unexpected domain=" 
					   + packet.getSecurityDomain());
			_status = STATUS_UNKNOWN_DOMAIN;
			return;		// #
		} 
		if (challenge != null) {

			// need to authenticate remote
			manner = packet.getAuthManner();
			if (manner != _manner) {

				// something wrong
				_status = STATUS_INCONSISTENT_MANNER;
				return;				// #
			} 
			response = packet.getResponse();
			if (manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST 
					|| manner 
					   == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

				// authentication procedure with shared secret
				// authentication procedure with digital signature
				try {
					if (auth != null && response != null 
							&& auth.verify(Auth.FIRST_TURN, challenge, 
										   response)) {

						// verified
						verboseOut("Authentication : 2nd turn : step=SECOND_TURN : verified.");

						// ! 	    AuthenticationManager.register(packet.getServerID());
						AuthenticationManager.register(_serverIdentifier, 
													   _selectedDomainname);
						_authenticatedOpponent = true;
						_status = STATUS_NORMAL;
					} else {

						// not verified
						verboseOut("Authentication : 2nd turn : step=SECOND_TURN : NOT verified.");
						_authenticatedOpponent = false;
						_status = STATUS_AUTHENTICATION_FAILED;
						return;		// #
					} 
				} catch (AuthenticationException excpt) {

					// Authentication is failed
					System.err.println(excpt);
					_authenticatedOpponent = false;
					_status = STATUS_AUTHENTICATION_FAILED;
					return;			// #
				} 
			} else {

				// something wrong
				_status = STATUS_UNKNOWN_MANNER;
				return;				// #
			} 
		} else {

			// not need to authenticate remote
			_authenticatedOpponent = true;
			_status = STATUS_NORMAL;
		} 

		// _status = STATUS_NORMAL;

		// 4 : STEP_END
		// send packet
		verboseOut("Authentication : 2nd turn : step=END");
		_step = STEP_END;
		if (_status == STATUS_AUTHENTICATION_FAILED) {

			// authentication failed, send no response
			manner = _manner;
			response = null;
		} else {
			manner = packet.getAuthManner();
			if (manner != _manner) {

				// something wrong
				_status = STATUS_INCONSISTENT_MANNER;
				return;				// #
			} 
			challenge = packet.getChallenge();
			if (challenge != null) {

				// challenge is given; to be authenticated
				verboseOut("Authentication : 2nd turn : step=END : response of challenge is requested.");
				if (manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST 
						|| manner 
						   == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {

					// authentication procedure with shared secret
					// authentication procedure with digital signature
					try {
						response = 
							new Response(auth
								.calculateResponse(Auth.SECOND_TURN, 
												   challenge));
					} catch (AuthenticationException excpt) {

						// authentication is failed
						System.err.println(excpt);
						response = null;
						_status = STATUS_ERROR;
						return;		// #
					} 
				} else {

					// something wrong
					_status = STATUS_UNKNOWN_MANNER;
					return;			// #
				} 
			} else {

				// challenge is not given; not need to send response
				verboseOut("Authentication : 2nd turn : step=END : response of challenge is NOT requested.");
				response = null;
				_status = STATUS_NORMAL;
			} 
		} 
		packet = new AuthPacket(_step, _status, _selectedDomainname, manner, 
								null, response);
		verboseOut("Authentication : 2nd turn : step=END : sending packet ... ");
		packet.writeTo(_outputStream);
		verboseOut("packet sent.");
		_status = STATUS_NORMAL;
	}
	/**
	 * Returns authentication manner
	 * @return authentication manner
	 */
	public final int getAuthManner() {
		return _manner;
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
	 * @return data input of packet
	 */
	public final DataInput getDataInput() {
		return _dataInput;
	}
	/**
	 * Returns local IP address of socket to be connected/bound
	 * @return local IP address of socket to be connected/bound
	 */
	public final InetAddress getLocalAddress() {
		return _localAddr;
	}
	/**
	 * Returns output stream of socket to be connected/bound
	 * @return output stream of socket to be connected/bound
	 */
	public final OutputStream getOutputStream() {
		return _outputStream;
	}
	/**
	 * Returns remote IP address of socket to be connected/bound
	 * @return remote IP address of socket to be connected/bound
	 */
	public final InetAddress getRemoteAddress() {
		return _remoteAddr;
	}
	/**
	 * Returns selected security domain name.
	 * @return selected security domain name
	 */
	public final String getSelectedDomainName() {
		return _selectedDomainname;
	}
	/**
	 * Returns shared secret for selected security domain.
	 * @return shared secret for selected security domain
	 */
	public final SharedSecret getSelectedSecret() {
		return _selectedSecret;
	}
	/**
	 * Returns socket to be connected/bound.
	 * @return socket to be connected/bound
	 */
	public final Socket getSocket() {
		return _socket;
	}
	/**
	 * Returns turn of protocol.
	 * @return turn of protocol
	 */
	public final int getTurn() {
		return _turn;
	}
	public boolean isAuthenticatedMyself() {
		return _authenticatedMyself;
	}
	public boolean isAuthenticatedOpponent() {
		return _authenticatedOpponent;
	}
	/**
	 * Sets authentication manner.
	 * @param manner authentication manner
	 * @exception java.lang.IllegalArgumentException
	 */
	private final void setAuthManner(int manner) 
			throws IllegalArgumentException {
		if (manner == AtpConstants.AUTHENTICATION_MANNER_DIGEST 
				|| manner == AtpConstants.AUTHENTICATION_MANNER_SIGNATURE) {
			_manner = manner;
		} else {
			_manner = AtpConstants.NO_AUTHENTICATION_MANNER;
			throw new IllegalArgumentException("Illegal manner : " + manner);
		} 
	}
	/**
	 * Sets data input of packet.
	 * @param di data input of packet
	 */
	private final void setDataInput(DataInput di) {
		_dataInput = di;
	}
	/**
	 * Sets socket to be connected/bound.
	 * @param socket socket to be connected/bound
	 */
	private final void setSocket(Socket socket) {
		_socket = socket;
		_localAddr = _socket.getLocalAddress();
		_remoteAddr = _socket.getInetAddress();
		_serverIdentifier = new ServerIdentifier(_socket);
		try {

			// _inputStream  = _socket.getInputStream();
			_outputStream = _socket.getOutputStream();
		} catch (IOException excpt) {

			// _inputStream = null;
			_outputStream = null;
		} 
	}
	/**
	 * Sets status
	 */
	private final void setStatus(int status) throws IllegalArgumentException {
		switch (status) {
		case STATUS_NORMAL:
		case STATUS_AUTHENTICATION_FAILED:
		case STATUS_ERROR:
			_status = status;
			break;
		default:
			throw new IllegalArgumentException("Illegal status : " + status);
		}
	}
	// -
	// -   /**
	// -    * Gets file name of private key for challenge-response authentication with digital signature.
	// -    * @return file name of private key for challenge-response authentication with digital signature
	// -    */
	// -   public static String getPrivateKeyFilename() {
	// -     final String securityDir = FileUtils.getSecurityDirectory();
	// -     final String default_file = securityDir + File.separator + "private.key";
	// -     Resource res = Resource.getResourceFor("atp");
	// -     if(res==null) {
	// -       return default_file;
	// -     }
	// -     return res.getString("atp.auth.privatekey", default_file);
	// -   }
	// -
	// -   /**
	// -    * Gets private key for challenge-response authentication with digital signature by loading private key file.
	// -    * If the file is already loaded, return it.
	// -    * If the private key file does not exist, return null.
	// -    * @return private key for challenge-response authentication with digital signature.
	// -    */
	// -   public synchronized static PrivateKey getPrivateKey() {
	// -     if(_privateKey!=null) {
	// -       return _privateKey;
	// -     }
	// -
	// -     loadPrivateKey();
	// -
	// -     return _privateKey;
	// -   }
	// -
	// -   /**
	// -    * Loads private key for challenge-response authentication with digital signature.
	// -    * If the private key file does not exist or something wrong, sets null.
	// -    */
	// -   public synchronized static void loadPrivateKey() {
	// -     FileInputStream fstprivate = null;
	// -     try {
	// -       final String file = getPrivateKeyFilename();
	// -       System.out.print("[Reading from private key file "+file+" ... ");
	// -       fstprivate = new FileInputStream(file);
	// -     }
	// -     catch(FileNotFoundException excpt) {
	// -       // private key file does not exist
	// -       System.out.println("no private key file.]");
	// -       System.err.println(excpt);
	// -       _privateKey = null;
	// -       return;
	// -     }
	// -
	// -     try {
	// -       ObjectInputStream istprivate = new ObjectInputStream(fstprivate);
	// -       _privateKey = (PrivateKey)istprivate.readObject();
	// -       fstprivate.close();
	// -     }
	// -     catch(ClassNotFoundException excpt) {
	// -       // something wrong
	// -       System.out.println("file seems to be broken.]");
	// -       System.err.println(excpt);
	// -       _privateKey = null;
	// -       return;
	// -     }
	// -     catch(IOException excpt) {
	// -       // something wrong
	// -       System.out.println("file has something wrong.]");
	// -       System.err.println(excpt);
	// -       _privateKey = null;
	// -       return;
	// -     }
	// -
	// -     System.out.println("done.]");
	// -   }
	// -
	// -   /**
	// -    * Gets file name of public key for challenge-response authentication with digital signature.
	// -    * @return file name of public key for challenge-response authentication with digital signature
	// -    */
	// -   public static String getPublicKeyFilename() {
	// -     final String securityDir = FileUtils.getSecurityDirectory();
	// -     final String default_file = securityDir + File.separator + "public.key";
	// -     Resource res = Resource.getResourceFor("atp");
	// -     if(res==null) {
	// -       return default_file;
	// -     }
	// -     return res.getString("atp.auth.publickey", default_file);
	// -   }
	// -
	// -   /**
	// -    * Gets public key for challenge-response authentication with digital signature by loading public key file.
	// -    * If the file is already loaded, return it.
	// -    * If the public key file does not exist, return null.
	// -    * @return public key for challenge-response authentication with digital signature.
	// -    */
	// -   public synchronized static PublicKey getPublicKey() {
	// -     if(_publicKey!=null) {
	// -       return _publicKey;
	// -     }
	// -
	// -     loadPublicKey();
	// -
	// -     return _publicKey;
	// -   }
	// -
	// -   /**
	// -    * Loads public key for challenge-response authentication with digital signature.
	// -    * If the public key file does not exist or something wrong, sets null.
	// -    */
	// -   public synchronized static void loadPublicKey() {
	// -     FileInputStream fstpublic = null;
	// -     try {
	// -       final String file = getPublicKeyFilename();
	// -       System.out.print("[Reading from public key file "+file+" ... ");
	// -       fstpublic = new FileInputStream(file);
	// -     }
	// -     catch(FileNotFoundException excpt) {
	// -       // public key file does not exist
	// -       System.out.println("no public key file.]");
	// -       System.err.println(excpt);
	// -       _publicKey = null;
	// -       return;
	// -     }
	// -
	// -     try {
	// -       ObjectInputStream istpublic = new ObjectInputStream(fstpublic);
	// -       _publicKey = (PublicKey)istpublic.readObject();
	// -       fstpublic.close();
	// -     }
	// -     catch(ClassNotFoundException excpt) {
	// -       // something wrong
	// -       System.out.println("file seems to be broken.]");
	// -       System.err.println(excpt);
	// -       _publicKey = null;
	// -       return;
	// -     }
	// -     catch(IOException excpt) {
	// -       // something wrong
	// -       System.out.println("file has something wrong.]");
	// -       System.err.println(excpt);
	// -       _publicKey = null;
	// -       return;
	// -     }
	// -
	// -     System.out.println("done.]");
	// -   }
	// -
	// -   /**
	// -    * Creates private/public key pair for challenge-response authentication
	// -    * with digital signature and
	// -    * write it into the private/public key file.
	// -    * @return public key for challenge-response authentication with digital signature
	// -    */
	// -   public synchronized static PublicKey createKeyPair() {
	// -     if(_keyPairGen==null) {
	// -       try {
	// - 	/* Get a Key Pair Generator */
	// - 	_keyPairGen = KeyPairGenerator.getInstance(KEYPAIRGENERATORALGORITHM);
	// - 	/* Initialize Key Pair Generator with randomize seed */
	// - 	_keyPairGen.initialize(KEYSTRENGTH, Randoms.getRandomGenerator(SEEDLENGTH));
	// -       }
	// -       catch(NoSuchAlgorithmException excpt) {
	// - 	System.err.println(excpt);
	// - 	_privateKey = null;
	// - 	_publicKey  = null;
	// - 	return null;
	// -       }
	// -     }
	// -
	// -     /* Generate a Key Pair */
	// -     KeyPair pair = _keyPairGen.generateKeyPair();
	// -     _privateKey = pair.getPrivate();
	// -     _publicKey  = pair.getPublic();
	// -
	// -     writePrivateKey(_privateKey);
	// -     writePublicKey(_publicKey);
	// -
	// -     return _publicKey;
	// -   }
	// -
	// -   /**
	// -    * Writes private key for challenge-response authentication
	// -    * with digital signature into a file.
	// -    * @param privateKey private key
	// -    */
	// -   public synchronized static void writePrivateKey(PrivateKey privateKey) {
	// -     FileOutputStream fstprivate = null;
	// -     ObjectOutputStream ostprivate = null;
	// -     try {
	// -       final String file = getPrivateKeyFilename();
	// -       fstprivate = new FileOutputStream(file);
	// -       ostprivate = new ObjectOutputStream(fstprivate);
	// -       ostprivate.writeObject(privateKey);
	// -       ostprivate.flush();
	// -       fstprivate.close();
	// -     }
	// -     catch(IOException excpt) {
	// -       // something wrong
	// -     }
	// -   }
	// -
	// -   /**
	// -    * Writes public key for challenge-response authentication
	// -    * with digital signature into a file.
	// -    * @param publicKey public key
	// -    */
	// -   public synchronized static void writePublicKey(PublicKey publicKey) {
	// -     FileOutputStream fstpublic = null;
	// -     ObjectOutputStream ostpublic = null;
	// -     try {
	// -       final String file = getPublicKeyFilename();
	// -       fstpublic = new FileOutputStream(file);
	// -       ostpublic = new ObjectOutputStream(fstpublic);
	// -       ostpublic.writeObject(publicKey);
	// -       ostpublic.flush();
	// -       fstpublic.close();
	// -     }
	// -     catch(IOException excpt) {
	// -       // something wrong
	// -     }
	// -   }
	// -
	// -   /**
	// -    * Sets public key of opponent for challenge-response authentication
	// -    * with digital signature.
	// -    */
	// -   public static void setPublicKeyOpponent(PublicKey publicKey) {
	// -     _publicKeyOpponent = publicKey;
	// -   }

	/**
	 * Sets turn of protocol.
	 * @param turn turn of protocol
	 * @exception java.lang.IllegalArgumentException
	 */
	private final void setTurn(int turn) throws IllegalArgumentException {
		if (turn == Auth.FIRST_TURN || turn == Auth.SECOND_TURN) {
			_turn = turn;
		} else {
			_turn = Auth.NO_TURNS;
			throw new IllegalArgumentException("Illegal turn : " + turn);
		} 
	}
	private static final void verboseOut(String msg) {
		AuthPacket.verboseOut(msg);
	}
}
