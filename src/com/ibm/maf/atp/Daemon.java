package com.ibm.maf.atp;

/*
 * @(#)Daemon.java
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

import com.ibm.maf.MAFAgentSystem;

import com.ibm.atp.auth.SharedSecrets;
import com.ibm.atp.auth.Challenge;

import com.ibm.aglet.Ticket;

import com.ibm.aglets.security.Randoms;

import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.NullOutputStream;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import java.security.SecureRandom;
import java.security.cert.Certificate;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.PrintStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Date;
import java.text.DateFormat;
import org.aglets.log.*;

/**
 * <tt> Daemon </tt> is a listener of incoming ATP request.
 * 
 * @version     1.10	$Date: 2009/07/28 07:04:53 $
 * @author	Danny D. Langue
 * @author	Gaku Yamamoto
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */
final public class Daemon {
    	private static AgletsLogger logger =  AgletsLogger.getLogger(Daemon.class.getName());
    
	static Hashtable locals = new Hashtable();

	String _username = null;
	Certificate _certificate = null;

	/*
	 * Max number of handlerThreads. Default value is set in the constructor.
	 */
	private int _maxHandlerThreads = 32;

	/*
	 * The request handler cache.
	 */
	private Hashtable _handlerCache = new Hashtable();

	private int _port = -1;

	private static boolean verbose = false;

	private URL hosting = null;

	private Resource resource = null;

	MAFAgentSystem _maf = null;

	private static PrintStream nullStream = null;
	static {
		try {
			nullStream = 
				(PrintStream)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return new PrintStream(new NullOutputStream());
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	} 

	static private PrintStream message = nullStream;
	static private PrintStream error = nullStream;
	static private PrintStream access = nullStream;

	/**
	 * Constructs a daemon
	 */

	/* package */
	Daemon(MAFAgentSystem maf) {
		_maf = maf;
		try {
			hosting = new URL(maf.getAddress());
			_port = hosting.getPort() == -1 ? 4434 : hosting.getPort();
			String addr = hosting.getHost() + ":" + _port;

			locals.put(addr, maf);
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				hosting = new URL("atp://localhost:" + _port);
			} catch (MalformedURLException exx) {
				throw new RuntimeException("couldn't make hosting URL");
			} 
		} 

		resource = Resource.getResourceFor("atp");
		resolveResources();
	}
	/**
	 * Write a access log
	 */
	static public void access(InetAddress host, long time, 
							  String requestLine, int statusCode, 
							  String misc) {
		DateFormat dfmt = DateFormat.getDateInstance();

		access.println(host.getHostName() + " - - [" 
					   + dfmt.format(new Date(time)) + "] \"" + requestLine 
					   + "\" " + statusCode + " " + misc);
	}
	/**
	 * Write a error message into a log file
	 */
	static public void error(InetAddress host, long time, String err, 
							 String reason) {
		DateFormat dfmt = DateFormat.getDateInstance();

		error.println("[" + dfmt.format(new Date(time)) + "] " + err 
					  + " from " + host.getHostName() + " reason: " + reason);
		error.flush();
	}
	public static MAFAgentSystem getLocalAgentSystem(Ticket ticket) {
		String address = ticket.getHost();
		int port = ticket.getPort();

		address += (port == -1 ? "" : (":" + port));
		return getLocalAgentSystem(address);
	}
	public static MAFAgentSystem getLocalAgentSystem(String address) {
		return (MAFAgentSystem)locals.get(address);
	}
	static final boolean isVerbose() {
		return verbose;
	}
	/**
	 * Write a message into a log file
	 */
	public void message(long time, String msg) {
		DateFormat dfmt = DateFormat.getDateInstance();
		String mes = "[" + dfmt.format(new Date(time)) + "] " + msg;

		message.println(mes);
		message.flush();
	}
	/*
	 * 
	 */
	static private OutputStream openStream(String filename) 
			throws IOException {
		if (FileUtils.ensureDirectory(filename) == false) {
			throw new IOException("Cannot create diretory for " + filename);
		} 
		if (FileUtils.ensureFile(filename) == false) {
			throw new IOException("Cannot create file : " + filename);
		} 
		RandomAccessFile rfs = new RandomAccessFile(filename, "rw");

		rfs.seek(new File(filename).length());
		return new FileOutputStream(rfs.getFD());
	}
	/**
	 * Resolve resource.
	 */
	private void resolveResources() {
		if (_port == -1) {
			try {
				String s_port = 
					(String)AccessController
						.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return System.getProperty("maf.port");
					} 
				});

				_port = Integer.parseInt(s_port);
			} catch (Exception ex) {
				ex.printStackTrace();
				_port = 4434;
			} 
		} 
		_maxHandlerThreads = resource.getInteger("atp.maxHandlerThread", 32);
	}
	static public void setAccessLogFile(String filename) throws IOException {
		setAccessLogStream(openStream(filename));
	}
	static public void setAccessLogStream(OutputStream out) {
		try {
			final OutputStream fOut = out;

			access = 
				(PrintStream)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return new PrintStream(fOut);
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	static public void setErrorLogFile(String filename) throws IOException {
		setErrorLogStream(openStream(filename));
	}
	static public void setErrorLogStream(OutputStream out) {
		try {
			final OutputStream fOut = out;

			error = 
				(PrintStream)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return new PrintStream(fOut);
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	static public void setMessageLogFile(String filename) throws IOException {
		setMessageLogStream(openStream(filename));
	}
	static public void setMessageLogStream(OutputStream out) {
		try {
			final OutputStream fOut = out;

			message = 
				(PrintStream)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return new PrintStream(fOut);
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	/**
	 * username and certificate.
	 */
	public void setUser(String username, Certificate cert) {
		_username = username;
		_certificate = cert;
	}
	/*
	 * Starts the daemon with specific agentsystem
	 */
	public void start() {

		// === should be moved into com.ibm.aglets.tahiti.Main#main() ?
		// check resource of authentication
		boolean auth = resource.getBoolean("atp.authentication", false);

		// check resource of use of secure random seed
		final boolean secureseed = resource.getBoolean("atp.secureseed", 
				false);

		Randoms.setUseSecureRandomSeed(secureseed);
		if (secureseed) {
			logger.info("USE SECURE RANDOM SEED.");
		} else {
			logger.info("USE UNSECURE PSEUDO RANDOM SEED.");
		} 
		if (auth) {
			logger.info("Generating random seed ... wait for a while.");
			Randoms.getRandomGenerator(Challenge.LENGTH);
			logger.info("done.");
		} 
		if (auth && SharedSecrets.getSharedSecrets() == null) {
			logger.error("No shared secret file for authentication."+
			    "Authentication requires a shared secret file"+
                "which is duplicated from other host,"+
                "or newly created file.");
			resource.setResource("atp.authentication", "false");
			resource.setOptionResource("atp.authentication", "false");
			auth = false;
		} 
		if (auth) {
			logger.info("AUTHENTICATION MODE ON.");
		} else {
			logger.info("AUTHENTICATION MODE OFF.");
		} 
		ServerSocket socket = null;

		try {
			final int fPort = _port;

			socket = 
				(ServerSocket)AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws IOException {
					return new ServerSocket(fPort);
				} 
			});
			message(System.currentTimeMillis(), 
					"IBM Agent Daemon started with atp:" + _port + " " 
					+ _maxHandlerThreads + " agentSystem: aglets");
			resource.setDefaultResource("atp.server", "true");
		} catch (Exception ex) {
			socket = null;
			message(System.currentTimeMillis(), 
					"IBM Agent Server failed to open the protocol atp:" 
					+ _port);
			resource.setDefaultResource("atp.server", "false");
		} 
		if (socket == null) {
			return;
		} 

		// ConnectionHandler h;
		// HandlerPool handlerPool = new HandlerPool(_maf, _maxHandlerThreads);

		// initial number is 4
		new ConnectionHandler(_maf, socket);
		new ConnectionHandler(_maf, socket);
		new ConnectionHandler(_maf, socket);
		new ConnectionHandler(_maf, socket);

		// - 	while(true) {
		// - 	    try {
		// - 		h = handlerPool.getAvailableHandler();
		// - 		AccessController.beginPrivileged();
		// - 		h.handleConnection(socket.accept());
		// - 	    } catch (Exception ex) {
		// - 		ex.printStackTrace();
		// - 	    } finally {
		// - 		AccessController.endPrivileged();
		// - 	    }
		// - 	}
	}
	/**
	 * updating resource.
	 */
	static public void update() {
		Resource res = Resource.getResourceFor("system");

		if (res != null) {
			verbose = res.getBoolean("verbose", false);
		} 
		ConnectionHandler.update();
	}
	static final void verboseOut(String msg) {
		if (verbose) {
			System.out.println(msg);
		} 
	}
}
