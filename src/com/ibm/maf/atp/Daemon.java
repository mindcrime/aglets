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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.Ticket;
import com.ibm.aglets.security.Randoms;
import com.ibm.atp.auth.Challenge;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.NullOutputStream;
import com.ibm.awb.misc.Resource;
import com.ibm.maf.MAFAgentSystem;

/**
 * <tt> Daemon </tt> is a listener of incoming ATP request.
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Danny D. Langue
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */
final public class Daemon {
	private static AgletsLogger logger = AgletsLogger.getLogger(Daemon.class.getName());

	static Hashtable locals = new Hashtable();

	/**
	 * Write a access log
	 */
	static public void access(
	                          final InetAddress host,
	                          final long time,
	                          final String requestLine,
	                          final int statusCode,
	                          final String misc) {
		final DateFormat dfmt = DateFormat.getDateInstance();

		access.println(host.getHostName() + " - - ["
				+ dfmt.format(new Date(time)) + "] \"" + requestLine + "\" "
				+ statusCode + " " + misc);
	}
	/**
	 * Write a error message into a log file
	 */
	static public void error(
	                         final InetAddress host,
	                         final long time,
	                         final String err,
	                         final String reason) {
		final DateFormat dfmt = DateFormat.getDateInstance();

		error.println("[" + dfmt.format(new Date(time)) + "] " + err + " from "
				+ host.getHostName() + " reason: " + reason);
		error.flush();
	}

	public static MAFAgentSystem getLocalAgentSystem(final String address) {
		return (MAFAgentSystem) locals.get(address);
	}

	public static MAFAgentSystem getLocalAgentSystem(final Ticket ticket) {
		String address = ticket.getHost();
		final int port = ticket.getPort();

		address += (port == -1 ? "" : (":" + port));
		return getLocalAgentSystem(address);
	}

	static final boolean isVerbose() {
		return verbose;
	}

	/*
	 * 
	 */
	static private OutputStream openStream(final String filename) throws IOException {
		if (FileUtils.ensureDirectory(filename) == false) {
			throw new IOException("Cannot create diretory for " + filename);
		}
		if (FileUtils.ensureFile(filename) == false) {
			throw new IOException("Cannot create file : " + filename);
		}
		final RandomAccessFile rfs = new RandomAccessFile(filename, "rw");

		rfs.seek(new File(filename).length());
		return new FileOutputStream(rfs.getFD());
	}

	static public void setAccessLogFile(final String filename) throws IOException {
		setAccessLogStream(openStream(filename));
	}

	static public void setAccessLogStream(final OutputStream out) {
		try {
			final OutputStream fOut = out;

			access = (PrintStream) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return new PrintStream(fOut);
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	static public void setErrorLogFile(final String filename) throws IOException {
		setErrorLogStream(openStream(filename));
	}

	static public void setErrorLogStream(final OutputStream out) {
		try {
			final OutputStream fOut = out;

			error = (PrintStream) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return new PrintStream(fOut);
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
	static public void setMessageLogFile(final String filename) throws IOException {
		setMessageLogStream(openStream(filename));
	}

	static public void setMessageLogStream(final OutputStream out) {
		try {
			final OutputStream fOut = out;

			message = (PrintStream) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return new PrintStream(fOut);
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * updating resource.
	 */
	static public void update() {
		final Resource res = Resource.getResourceFor("system");

		if (res != null) {
			verbose = res.getBoolean("verbose", false);
		}
		ConnectionHandler.update();
	}
	static final void verboseOut(final String msg) {
		if (verbose) {
			System.out.println(msg);
		}
	}

	String _username = null;

	Certificate _certificate = null;

	/*
	 * Max number of handlerThreads. Default value is set in the constructor.
	 */
	private int _maxHandlerThreads = 32;

	/*
	 * The request handler cache.
	 */
	private final Hashtable _handlerCache = new Hashtable();

	private int _port = -1;

	private static boolean verbose = false;

	private URL hosting = null;

	private Resource resource = null;

	MAFAgentSystem _maf = null;

	private static PrintStream nullStream = null;

	static {
		try {
			nullStream = (PrintStream) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return new PrintStream(new NullOutputStream());
				}
			});
		} catch (final Exception ex) {
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
	Daemon(final MAFAgentSystem maf) {
		_maf = maf;
		try {
			hosting = new URL(maf.getAddress());
			_port = hosting.getPort() == -1 ? 4434
					: hosting.getPort();
			final String addr = hosting.getHost() + ":" + _port;

			locals.put(addr, maf);
		} catch (final Exception ex) {
			ex.printStackTrace();
			try {
				hosting = new URL("atp://localhost:" + _port);
			} catch (final MalformedURLException exx) {
				throw new RuntimeException("couldn't make hosting URL");
			}
		}

		resource = Resource.getResourceFor("atp");
		resolveResources();
	}

	/**
	 * Write a message into a log file
	 */
	public void message(final long time, final String msg) {
		final DateFormat dfmt = DateFormat.getDateInstance();
		final String mes = "[" + dfmt.format(new Date(time)) + "] " + msg;

		message.println(mes);
		message.flush();
	}

	/**
	 * Resolve resource.
	 */
	private void resolveResources() {
		if (_port == -1) {
			try {
				final String s_port = (String) AccessController.doPrivileged(new PrivilegedAction() {
					@Override
					public Object run() {
						return System.getProperty("maf.port");
					}
				});

				_port = Integer.parseInt(s_port);
			} catch (final Exception ex) {
				ex.printStackTrace();
				_port = 4434;
			}
		}
		_maxHandlerThreads = resource.getInteger("atp.maxHandlerThread", 32);
	}

	/**
	 * username and certificate.
	 */
	public void setUser(final String username, final Certificate cert) {
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
		final boolean secureseed = resource.getBoolean("atp.secureseed", false);

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
		if (auth && (SharedSecrets.getSharedSecrets() == null)) {
			logger.error("No shared secret file for authentication."
					+ "Authentication requires a shared secret file"
					+ "which is duplicated from other host,"
					+ "or newly created file.");
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

			socket = (ServerSocket) AccessController.doPrivileged(new PrivilegedExceptionAction() {
				@Override
				public Object run() throws IOException {
					return new ServerSocket(fPort);
				}
			});
			message(System.currentTimeMillis(), "IBM Agent Daemon started with atp:"
					+ _port
					+ " "
					+ _maxHandlerThreads
					+ " agentSystem: aglets");
			resource.setDefaultResource("atp.server", "true");
		} catch (final Exception ex) {
			socket = null;
			message(System.currentTimeMillis(), "IBM Agent Server failed to open the protocol atp:"
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

		// - while(true) {
		// - try {
		// - h = handlerPool.getAvailableHandler();
		// - AccessController.beginPrivileged();
		// - h.handleConnection(socket.accept());
		// - } catch (Exception ex) {
		// - ex.printStackTrace();
		// - } finally {
		// - AccessController.endPrivileged();
		// - }
		// - }
	}
}
