package com.ibm.maf.atp;

/*
 * @(#)ConnectionHandler.java
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

import com.ibm.maf.ClassName;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.Name;
import com.ibm.maf.AgentNotFound;
import com.ibm.maf.ClassUnknown;
import com.ibm.maf.DeserializationFailed;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MessageEx;
import com.ibm.maf.NotHandled;

import com.ibm.atp.AtpConstants;
import com.ibm.atp.auth.Auth;
import com.ibm.atp.auth.AuthPacket;
import com.ibm.atp.auth.Authentication;

// - import com.ibm.atp.auth.AuthenticationManager;
import com.ibm.atp.auth.AuthenticationProtocolException;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.atp.auth.SharedSecret;

// - import com.ibm.aglets.AgletsSystem;

import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.Hexadecimal;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.URL;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.IOException;

/**
 * @version     1.10	$Date: 2001/07/28 06:31:34 $
 * @author	Danny D. Langue
 * @author	Gaku Yamamoto
 * @author	Mitsuru Oshima
 */
final class ConnectionHandler extends Thread implements AtpConstants {
	/**
	 * message digest algorithm.
	 */
	final private static String MESSAGE_DIGEST_ALGORITHM = "SHA";
	private static MessageDigest _mdigest = null;

	private MAFAgentSystem _maf = null;
	private ServerSocket _serversocket = null;
	private Socket _connection = null;
	private Authentication _auth = null;
	private boolean _authenticated = false;

	private static ThreadGroup group = new ThreadGroup("ConnectionHandler");

	private static int BUFFSIZE = 2048;
	private static int number = 0;
	private static boolean authentication = false;
	private static int max_handlers = 32;
	private static int num_handlers = 0;
	private static int idle_handlers = 0;

	static {
		try {
			_mdigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} 
		update();
	} 

	static boolean http_tunneling = false;
	static boolean http_messaging = false;

	private java.util.Hashtable headers = new java.util.Hashtable();

	// - 	    AgletID aid = null;
	// - 	    if (obj instanceof AgletID) {
	// - 		aid = (AgletID)obj;
	// - 	    } else {
	// - 		int len = ref.length();
	// - 		byte[] b = new byte[len/2];
	// - 		for(int i = 0, j=0; j<len; i++, j++) {
	// - 		    b[i] = (byte)(Character.digit(ref.charAt(j++), 16) << 4);
	// - 		    b[i] += (byte)Character.digit(ref.charAt(j), 16);
	// - 		}
	// - 		aid = new AgletID(b);
	// - 	    }
	// -
	// - //	    AgletID aid = new AgletID(b);
	// - //	    String t =
	// - 	    context.handleMessageRequest(aid,
	// - 					 request.getAgentLanguage(),
	// - 					 request.getInputStream(),
	// - 					 response);
	// - //	    response.setContentType(t);
	// - //	    response.setStatusCode(OKAY);
	// - //	    response.sendResponse();
	// - 	} catch (InvalidAgletException ex) {
	// - 	    response.sendError(NOT_FOUND);
	// - 	} catch (Exception ex) {
	// - 	    response.sendError(INTERNAL_ERROR);
	// - 	}
	// -     }
	byte future_reply[] = null;

	/*
	 * 
	 */
	private static final String CRLF = "\r\n";

	public ConnectionHandler(MAFAgentSystem maf, ServerSocket s) {
		super(group, "handler:" + (number++));
		_serversocket = s;
		_maf = maf;
		num_handlers++;
		start();
	}
	static byte[] calculateMIC(SharedSecret secret, byte[] agent) {
		if (secret == null) {

			// No shared secret
			return null;
		} 
		if (agent == null) {

			// No aglet byte sequence
			return null;
		} 
		_mdigest.reset();
		_mdigest.update(agent);
		_mdigest.update(secret.secret());
		return _mdigest.digest();
	}
	private static void dumpBytes(byte[] bytes) {
		if (bytes != null) {
			for (int i = 0; i < bytes.length; i++) {
				System.out.print(Hexadecimal.valueOf(bytes[i]));
				if (i % 16 == 15) {
					System.out.println();
				} else {
					System.out.print(" ");
				} 
			} 
			if (bytes.length % 16 != 0) {
				System.out.println();
			} 
		} 
	}
	private static boolean equalsSeq(byte[] seqa, byte[] seqb) {
		if (seqa == null && seqb == null) {
			return true;
		} 
		if (seqa == null || seqb == null || seqa.length != seqb.length) {
			return false;
		} 
		for (int i = 0; i < seqa.length; i++) {
			if (seqa[i] != seqb[i]) {
				return false;
			} 
		} 
		return true;
	}
	private static byte[] getMIC(AtpRequest request) {
		String micstr = request.getRequestParameter("mic");

		if (micstr == null) {
			return null;
		} 
		byte[] mic = null;

		try {
			mic = Hexadecimal.parseSeq(micstr);
		} catch (NumberFormatException excpt) {
			System.err.println("Illegal MIC in ATP request header : " 
							   + micstr);
		} 
		return mic;
	}
	private void handle() throws IOException {
		AtpRequest request = null;
		AtpResponse response = null;
		InetAddress remoteHost = null;
		long time = System.currentTimeMillis();

		remoteHost = _connection.getInetAddress();

		verboseOut("[Connected from " + remoteHost + ']');

		InputStream in = new BufferedInputStream(_connection.getInputStream(), 
												 BUFFSIZE);

		in.mark(128);
		DataInput di = new DataInputStream(in);
		String topLine = di.readLine();

		in.reset();
		if (topLine == null) {
			try {
				_connection.close();
			} catch (IOException exx) {
				System.err.println(exx.toString());
			} 
			return;
		} 

		_auth = null;
		_authenticated = false;
		if (AuthPacket.isTopLine(topLine)) {
			if (SharedSecrets.getSharedSecrets() == null) {
				throw new IOException("Authentication failed : no shared secrets.");
			} 

			// authentication protocol
			// _auth = new Authentication(Auth.SECOND_TURN, _connection);
			_auth = new Authentication(Auth.SECOND_TURN, di, _connection);
			try {
				_authenticated = _auth.authenticate();
			} catch (AuthenticationProtocolException excpt) {

				// protocol error
				System.err.println(excpt.toString());
				try {
					_connection.close();
				} catch (IOException exx) {
					System.err.println(exx.toString());
				} 
				throw new IOException("Authentication failed : " 
									  + excpt.getMessage());
			} catch (IOException excpt) {

				// protocol error
				System.err.println(excpt.toString());
				try {
					_connection.close();
				} catch (IOException exx) {
					System.err.println(exx.toString());
				} 
				throw new IOException("Authentication failed : " 
									  + excpt.getMessage());
			} 

			if (!_authenticated) {
				response = 
					new AtpResponseImpl(new BufferedOutputStream(_connection
						.getOutputStream(), BUFFSIZE));
				response.sendError(NOT_AUTHENTICATED);
				try {
					_connection.close();
				} catch (IOException exx) {
					System.err.println(exx.toString());
				} 
				return;
			} 

			in.mark(128);
			topLine = di.readLine();
			in.reset();
			if (topLine == null) {
				try {
					_connection.close();
				} catch (IOException exx) {
					System.err.println(exx.toString());
				} 
				return;
			} 
		} 

		String protocol = topLine.trim();

		protocol = protocol.substring(protocol.lastIndexOf(' ') + 1, 
									  protocol.lastIndexOf('/'));
		if (protocol.equalsIgnoreCase("ATP")) {
			if (authentication && (_auth == null ||!_authenticated)) {
				System.err
					.println("ATP connection from unauthenticated host is closed.");
				response = 
					new AtpResponseImpl(new BufferedOutputStream(_connection
						.getOutputStream(), BUFFSIZE));
				response.sendError(NOT_AUTHENTICATED);
				try {
					_connection.close();
				} catch (IOException exx) {
					System.err.println(exx.toString());
				} 
				return;
			} 

			// verboseOut("creating ATP Request & Response");

			request = new AtpRequestImpl(in);
			response = 
				new AtpResponseImpl(new BufferedOutputStream(_connection
					.getOutputStream(), BUFFSIZE));
		} else if (protocol.equalsIgnoreCase("HTTP")) {
			verboseOut("[Accepting HTTP..]");

			// Trim http headers
			HttpFilter.readHttpHeaders(in, headers);

			String r = (String)headers.get("method");
			String type = (String)headers.get("content-type");

			if ("GET".equalsIgnoreCase(r) && http_messaging) {

				// may be HTTP browser...

				verboseOut("[Http/GET request received.]");

				request = new HttpCGIRequestImpl(in, headers);
				response = 
					new HttpCGIResponseImpl(_connection.getOutputStream());

			} else if ("POST".equalsIgnoreCase(r) 
					   && "application/x-atp".equalsIgnoreCase(type) 
					   && http_tunneling) {

				// http tunneling
				verboseOut("[Http/POST request received.]");

				request = new AtpRequestImpl(in);
				response = 
					new AtpResponseImpl(new HttpResponseOutputStream(_connection
						.getOutputStream()));

			} else if ("POST".equalsIgnoreCase(r) 
					   && "application/x-www-url-form"
						   .equalsIgnoreCase(type)) {

				verboseOut("[POST request received.]");
				sendHttpResponse();
				verboseOut("[Sending responser.]");

				return;

			} else {
				throw new IOException("Unknown Content-Type:" + type);
			} 
		} else {
			throw new IOException("unknown protocol " + protocol);
		} 

		try {
			request.parseHeaders();
		} catch (IOException ex) {
			try {
				_connection.close();
			} catch (IOException exx) {
				System.err.println(exx.toString());
			} 
			Daemon.error(remoteHost, System.currentTimeMillis(), "", 
						 ex.toString());
			Daemon.access(remoteHost, System.currentTimeMillis(), 
						  request.getRequestLine(), response.getStatusCode(), 
						  String.valueOf('-'));
			return;
		} 

		try {
			handleRequest(request, response);

			// - 	    String agentSystem = request.getAgentSystem();
			// - 	    AgentRequestHandler handler =
			// - 		    _daemon.getRequestHandler(agentSystem);
			// -
			// - 	    verboseOut("[Start handling : method = "+request.getMethod());
			// -
			// - 	    if (handler != null) {
			// - //		com.ibm.awb.misc.Debug.check();
			// - 		handler.handleRequest(request, response);
			// - 	    } else {
			// - 		throw new ClassNotFoundException("[Agent System '" +
			// - 						 agentSystem +
			// - 						 "' not found]");
			// - 	    }
		} catch (IOException ioe) {
			if (Daemon.isVerbose()) {
				ioe.printStackTrace();
			} 
			Daemon.error(remoteHost, System.currentTimeMillis(), "", 
						 ioe.toString());
			ioe.printStackTrace();
			try {
				response.sendError(INTERNAL_ERROR);
			} catch (IOException ex) {
				System.err.println(ex.toString());
			} 

			// - 	} catch (ClassNotFoundException cnfe) {
			// - 	    _daemon.error(remoteHost,
			// - 			  System.currentTimeMillis(),
			// - 			  "Error: SERVICE_UNAVAILABLE",
			// - 			  cnfe.getMessage());
			// - 	    cnfe.printStackTrace();
			// - 	    try {
			// - 		response.sendError(NOT_FOUND);
			// - 	    } catch (Exception ex) {
			// - 		ex.printStackTrace();
			// - 	    }
		} 
		finally {

			// com.ibm.awb.misc.Debug.check();
			try {
				_connection.close();
			} catch (IOException e) {
				System.err.println(e.toString());
			} 

			Daemon.access(remoteHost, System.currentTimeMillis(), 
						  request.getRequestLine(), response.getStatusCode(), 
						  String.valueOf('-'));
		} 
	}
	/**
	 * Handles Dispatch Requests
	 */
	protected void handleDispatchRequest(AtpRequest request, AtpResponse response) 
			throws IOException {
		response.setContentType("application/x-aglets");
		boolean sent = false;

		try {

			MAFAgentSystem class_sender = 
				MAFAgentSystem.getMAFAgentSystem(request.getSender());

			DataInputStream in = 
				new DataInputStream(request.getInputStream());

			// CodeBase
			String codebase = in.readUTF();

			// ClassNames
			int len = in.readInt();
			ClassName class_names[] = new ClassName[len];

			for (int i = 0; i < len; i++) {
				String name = in.readUTF();
				byte desc[] = new byte[in.readInt()];

				in.readFully(desc, 0, desc.length);
				class_names[i] = new ClassName(name, desc);
			} 

			// Agent
			byte agent[] = new byte[in.readInt()];

			in.readFully(agent, 0, agent.length);

			if (_auth != null && _authenticated) {
				byte[] mic = getMIC(request);
				SharedSecret secret = _auth.getSelectedSecret();

				if (mic != null && secret != null && agent != null) {
					if (!verifyMIC(mic, secret, agent)) {
						throw new IOException("Incorrect MIC of transfered aglet.");
					} 
					verboseOut("MIC is CORRECT.");
				} 
			} 

			_maf.receive_agent(request.getAgentName(), 
							   request.getAgentProfile(), agent, 
							   request.getPlaceName(), class_names, codebase, 
							   class_sender);
			response.getOutputStream();
			response.setStatusCode(OKAY);
			response.sendResponse();
			sent = true;
		} catch (SecurityException ex) {
			response.sendError(FORBIDDEN);
			sent = true;
		} catch (ClassUnknown ex) {
			response.sendError(NOT_FOUND);
			sent = true;
		} catch (DeserializationFailed ex) {
			response.sendError(NOT_FOUND);
			sent = true;
		} catch (MAFExtendedException ex) {
			response.sendError(INTERNAL_ERROR);
			sent = true;
		} 
		finally {
			if (sent == false) {
				response.sendError(INTERNAL_ERROR);
			} 
		} 
	}
	/**
	 * Handles fetch requests.
	 * @param request
	 * @param response
	 */
	protected void handleFetchRequest(AtpRequest request, AtpResponse response) 
			throws IOException {
		byte[] bytecode = null;

		response.setContentType("application/x-aglets");
		boolean sent = false;

		try {
			byte b[][] = _maf.fetch_class(null, request.getFetchClassFile(), 
										  request.getAgentProfile());

			OutputStream out = response.getOutputStream();

			verboseOut("fetch_class(" + request.getFetchClassFile() + ") : " 
					   + b[0].length + "bytes");

			// if(Daemon.isVerbose()) {
			// dumpBytes(b[0]);
			// }
			out.write(b[0]);

			response.setStatusCode(OKAY);
			response.sendResponse();
			sent = true;

		} catch (ClassUnknown ex) {

			response.sendError(NOT_FOUND);
			sent = true;

		} catch (MAFExtendedException ex) {
			response.sendError(INTERNAL_ERROR);
			sent = true;

		} 
		finally {
			if (sent == false) {
				response.sendError(NOT_FOUND);
			} 
		} 
	}
	// - 	    String filename = request.getRequestURI().getFile();
	// - 	    int index = filename.lastIndexOf('/');
	// -
	// - 	    String dirname = filename.substring(0,index);
	// -
	// - 	    filename = filename.substring(index+1,filename.length());
	// -
	// - 	    if (AgletsSystem.getSecurityManager() != null) {
	// - 		//
	// - 		// REMIND: this part should be moved to SecurityManager
	// - 		//
	// - 		if (FileUtils.checkFile(dirname, agletsExportPath) == false) {
	// - 		    throw new AgletsSecurityException("Illegal Fetch Request "
	// - 						      + dirname + ' '
	// - 						      + filename);
	// - 		}
	// - 	    }
	// -
	// - 	    bytecode = getClassData(dirname,filename);
	// -
	// - 	    response.setStatusCode(OKAY);
	// -
	// - 	    OutputStream out = response.getOutputStream();
	// - 	    out.write(bytecode);
	// -
	// - 	    response.sendResponse();
	// - 	} catch (SecurityException ce) {
	// - 	    response.sendError(FORBIDDEN);
	// - 	} catch (ClassNotFoundException ce) {
	// - 	    response.sendError(NOT_FOUND);
	// - 	} catch (IOException ie) {
	// - 	    response.sendError(INTERNAL_ERROR);
	// - 	}

	protected void handleMessageRequest(AtpRequest request, AtpResponse response) 
			throws IOException {

		response.setContentType("application/x-aglets");
		boolean sent = false;

		Name name = request.getAgentName();
		InputStream in = request.getInputStream();
		byte type = (byte)in.read();

		int len = request.getContentLength();
		byte content[] = new byte[len - 1];

		new DataInputStream(in).readFully(content);

		try {
			switch (type) {
			case SYNC:
				try {
					byte ret[] = _maf.receive_message(name, content);
					OutputStream out = response.getOutputStream();

					out.write(HANDLED);
					out.write(ret, 0, ret.length);

					// DataOutput out = new DataOutputStream(...);
					// out.writeByte(HANDLED);

					response.setStatusCode(OKAY);
					response.sendResponse();
				} catch (NotHandled ex) {
					response.getOutputStream().write(NOT_HANDLED);

					// DataOutput out = new DataOutputStream(
					// out.writeByte(NOT_HANDLED);
					response.setStatusCode(OKAY);
					response.sendResponse();

				} catch (MessageEx ex) {
					DataOutput out = 
						new DataOutputStream(response.getOutputStream());

					out.writeByte(EXCEPTION);
					ex.write(out);
					response.setStatusCode(OKAY);
					response.sendResponse();
				} 
				break;
			case FUTURE:
				String sender_address = request.getSender();
				MAFAgentSystem sender = 
					MAFAgentSystem.getMAFAgentSystem(sender_address);
				long id = _maf.receive_future_message(name, content, sender);

				if (sender instanceof MAFAgentSystem_ATPClient) {
					((MAFAgentSystem_ATPClient)sender)
						.registerFutureReply(this, id);
				} 
				OutputStream out = response.getOutputStream();

				response.setStatusCode(OKAY);
				response.sendResponse();

				synchronized (this) {
					while (future_reply == null) {
						try {
							wait();
						} catch (InterruptedException ex) {}
					} 
					out.write(future_reply);
					out.flush();
					out.close();
					future_reply = null;
				} 

				// - 	try {
				// - 	    DataOutput out = new DataOutputStream(response.getOutputStream());
				// - 	    out.write(reply);
				// - 	    response.setStatusCode(OKAY);
				// - 	    response.sendResponse();
				// - 	} catch (IOException ex) {
				// - 	    throw new MAFExtendedException("UnexpectedException " +ex);
				// - 	}
				break;
			case ONEWAY:
				_maf.receive_oneway_message(name, content);
				response.getOutputStream();
				response.setStatusCode(OKAY);
				response.sendResponse();
				break;
			}
			sent = true;

		} catch (AgentNotFound ex) {
			response.sendError(NOT_FOUND);
			sent = true;

		} catch (ClassUnknown ex) {
			response.sendError(NOT_FOUND);
			sent = true;

		} catch (DeserializationFailed ex) {
			response.sendError(NOT_FOUND);
			sent = true;

		} catch (MAFExtendedException ex) {
			response.sendError(INTERNAL_ERROR);
			sent = true;

		} 
		finally {
			if (sent == false) {
				response.sendError(INTERNAL_ERROR);
			} 
		} 
	}
	/**
	 * Handle ATP Requests
	 */
	void handleRequest(AtpRequest request, 
					   AtpResponse response) throws IOException {
		switch (request.getMethod()) {
		case DISPATCH:
			handleDispatchRequest(request, response);
			break;
		case RETRACT:
			handleRetractRequest(request, response);
			break;
		case FETCH:
			handleFetchRequest(request, response);

			break;
		case MESSAGE:
			handleMessageRequest(request, response);
			break;
		default:
			response.sendError(BAD_REQUEST);
			break;
		}
	}
	// - 	    AgletContextImpl context = getAgletContext(request);
	// - 	    if (context == null) {
	// - 		response.sendError(NOT_FOUND);
	// - 		return;
	// - 	    }
	// - 	    AgletID aid =
	// - 		    context.receiveAglet(request.getInputStream());
	// - 	    response.setStatusCode(OKAY);
	// - 	    response.getOutputStream().write(aid.toByteArray());
	// - 	    response.sendResponse();
	// - 	} catch (ShuttingDownException ex) {
	// - 	    response.sendError(SERVICE_UNAVAILABLE);
	// - 	} catch (AgletException ae) {
	// - 	    response.sendError(INTERNAL_ERROR);
	// - 	} catch (ClassNotFoundException cnfe) {
	// - 	    response.sendError(NOT_FOUND);
	// - 	} catch (SecurityException ex) {
	// - 	    response.sendError(FORBIDDEN);
	// - 	}
	// -     }

	/**
	 * Handles retract requests.
	 * @param request
	 * @param response
	 */
	protected void handleRetractRequest(AtpRequest request, AtpResponse response) 
			throws IOException {
		response.setContentType("application/x-aglets");
		boolean sent = false;

		try {
			byte b[] = _maf.retract_agent(request.getAgentName());
			OutputStream out = response.getOutputStream();

			out.write(b);

			// - 	    String idstr = request.getAgentId();
			// - 	    int len = idstr.length();
			// - 	    byte[] b = new byte[len/2];
			// - 	    for(int i = 0, j=0; j<len; i++, j++) {
			// - 		b[i] = (byte)(Character.digit(idstr.charAt(j++), 16) << 4);
			// - 		b[i] += (byte)Character.digit(idstr.charAt(j), 16);
			// - 	    }
			// - 	    AgletID aid = new AgletID(b);
			// -
			// -
			// -
			// - 	    // REMIND: This is supposed to be replaced with
			// - 	    // other information. The remote URL is always null for the time
			// - 	    // being
			// - 	    URL remote = null;
			// - 	    AgletContextImpl context = getAgletContext(request);
			// - 	    if (context == null) {
			// - 		response.sendError(NOT_FOUND);
			// - 		return;
			// - 	    }
			// - 	    context.revertAglet(aid, remote, response.getOutputStream());
			response.setStatusCode(OKAY);
			response.sendResponse();
			sent = true;
		} catch (SecurityException ex) {
			response.sendError(FORBIDDEN);
			sent = true;

			// - 	} catch (DeserializationFailed ae) {
			// - 	    response.sendError(NOT_FOUND);
		} catch (AgentNotFound ex) {
			response.sendError(NOT_FOUND);
			sent = true;

		} catch (MAFExtendedException ex) {
			ex.printStackTrace();
			response.sendError(INTERNAL_ERROR);
			sent = true;
		} 
		finally {
			if (sent == false) {
				response.sendError(INTERNAL_ERROR);
			} 
		} 
	}
	synchronized public void run() {
		try {
			while (true) {
				try {
					idle_handlers++;
					try {
						final ServerSocket fServerSocket = _serversocket;

						_connection = 
							(Socket)AccessController
								.doPrivileged(new PrivilegedExceptionAction() {
							public Object run() throws IOException {
								return fServerSocket.accept();
							} 
						});
					} catch (Exception ex) {
						ex.printStackTrace();
					} 
					idle_handlers--;

					// synchronized (this.class) {
					if (idle_handlers == 0 && num_handlers < max_handlers) {
						new ConnectionHandler(_maf, _serversocket);
					} 

					// }
					handle();
					_connection = null;
					headers.clear();
				} catch (Exception ex) {
					ex.printStackTrace();
				} 
			} 
		} 
		finally {
			num_handlers--;
		} 
	}
	synchronized void sendFutureReply(byte b[]) {
		future_reply = b;
		notify();
	}
	private void sendHttpResponse() throws IOException {
		PrintStream p = new PrintStream(_connection.getOutputStream());

		// auto flush must be false.
		p.print("HTTP/1.0 200 OKAY" + CRLF);
		p.print("Content-type: text/html" + CRLF);

		String s = "<HTTP><HEAD> ATP DAEMON/0.1 </HEAD><BODY>" 
				   + "<H1> IBM ATP DAEMON/0.1 </H1><BR>" + "</BODY></HTTP>";

		p.print("Content-length: " + s.length() + CRLF + CRLF);
		p.println(s);
		_connection.getOutputStream().flush();
		_connection.close();

		// _connection.getOutputStream().close();
		// p.flush();
		// p.close();
	}
	public String toString() {
		return super.toString() + ", handling = " + (_connection != null);
	}
	static public void update() {
		Resource res = Resource.getResourceFor("atp");

		BUFFSIZE = res.getInteger("atp.buffersize", 2048);
		authentication = res.getBoolean("atp.authentication", false);
		http_tunneling = res.getBoolean("atp.http.tunneling", false);
		http_messaging = res.getBoolean("atp.http.messaging", false);
		max_handlers = res.getInteger("atp.maxHandlerThread", 32);
	}
	static private void verboseOut(String msg) {
		Daemon.verboseOut(msg);
	}
	static boolean verifyMIC(byte[] mic, SharedSecret secret, byte[] agent) {
		if (mic == null) {

			// No MIC
			System.err.println("No MIC");
			return false;
		} 
		if (secret == null) {

			// No shared secret
			System.err.println("No authenticated security domain");
			return false;
		} 
		if (agent == null) {

			// No aglet byte sequence
			System.err.println("No Aglet");
			return false;
		} 
		byte[] digest = calculateMIC(secret, agent);

		verboseOut("MIC=" + Hexadecimal.valueOf(mic));
		verboseOut("digest=" + Hexadecimal.valueOf(digest));
		return equalsSeq(mic, digest);
	}
}
