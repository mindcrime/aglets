package com.ibm.atp.auth;

/*
 * @(#)ServerIdentifier.java
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
import java.security.PrivilegedAction;

import java.net.Socket;
import java.net.InetAddress;

/**
 * The <tt>ServerIdentifier</tt> class is the identifier of alget server.
 * 
 * @version     1.00    $Date: 2001/07/28 06:33:49 $
 * @author      ONO Kouichi
 */
public class ServerIdentifier {
	/**
	 * IP address of the aglet server
	 */
	private InetAddress _address = null;

	/**
	 * port of socket used by the aglet server
	 */
	private int _port = -1;

	/**
	 * Constructor for server identifier.
	 * @param address IP address of the aglet server
	 * @param port port of socket used by the aglet server
	 */
	protected ServerIdentifier(InetAddress address, int port) {
		setInetAddress(address);
		setPort(port);
	}
	/**
	 * Constructor for server identifier.
	 * @param socket socket used by the aglet server
	 */
	public ServerIdentifier(Socket socket) {
		this(socket.getInetAddress(), socket.getPort());
	}
	/**
	 * Returns equality.
	 * @return true if the IP address and port equals to given server identifier,
	 * otherwise false.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ServerIdentifier)) {
			return false;
		} 
		final ServerIdentifier id = (ServerIdentifier)obj;
		boolean addreq = false;

		try {
			final InetAddress fAddr = _address;
			Boolean b = 
				(Boolean)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					try {
						return new Boolean(id.getInetAddress().equals(fAddr));
					} catch (Exception ex) {
						return new Boolean(false);
					} 
				} 
			});

			addreq = b.booleanValue();
		} catch (Exception ex) {
			ex.printStackTrace();
			addreq = false;
		} 
		return (addreq && id.getPort() == _port);
	}
	/**
	 * Gets IP address of the aglet server.
	 * @return IP address of aglet server
	 */
	private final InetAddress getInetAddress() {
		return _address;
	}
	/**
	 * Gets port of socket used by the aglet server.
	 * @return port of socket used by the aglet server
	 */
	private final int getPort() {
		return _port;
	}
	/**
	 * Sets IP address of the aglet server.
	 * @param address IP address of aglet server
	 */
	private final void setInetAddress(InetAddress address) {
		_address = address;
	}
	/**
	 * Sets port of socket used by the aglet server.
	 * @param port port of socket used by the aglet server
	 */
	private final void setPort(int port) {
		_port = port;
	}
}
