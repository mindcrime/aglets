package com.ibm.maf.atp;

/*
 * @(#)SocketConnection.java
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

import com.ibm.atp.auth.Auth;
import com.ibm.atp.auth.Authentication;
import com.ibm.atp.auth.AuthenticationProtocolException;

import com.ibm.awb.misc.Resource;

import java.net.URL;
import java.net.Socket;
import java.net.InetAddress;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * @version     1.00	$Date :$
 * @author      Mitsuru Oshima
 */
class SocketConnection implements Connection {

	private static int BUFFSIZE = 2048;
	private Socket _socket;
	private boolean established = false;
	private boolean output_type = false;
	private Authentication auth = null;
	private String message = null;

	static {
		Resource res = Resource.getResourceFor("atp");

		BUFFSIZE = res.getInteger("atp.buffersize", 2048);
	} 

	SocketConnection(URL url, int defaultPort) throws IOException {
		int port = url.getPort();

		if (port == -1) {
			port = defaultPort;
		} 
		_socket = new Socket(url.getHost(), port);

		Resource res = Resource.getResourceFor("atp");
		boolean authentication = res.getBoolean("atp.authentication", false);

		if (authentication) {
			DataInput di = new DataInputStream(_socket.getInputStream());

			// auth = new Authentication(Auth.FIRST_TURN, _socket);
			auth = new Authentication(Auth.FIRST_TURN, di, _socket);
			boolean authenticated = true;

			try {
				authenticated = auth.authenticate();
			} catch (AuthenticationProtocolException excpt) {

				// protocol error
				System.err.println(excpt);
			} catch (IOException excpt) {

				// protocol error
				System.err.println(excpt);
			} 
			if (authenticated && auth.isAuthenticatedMyself() 
					&& auth.isAuthenticatedOpponent()) {

				// connection is established
				established = true;
			} else {
				close();
				message = "Authentication failed";
			} 
		} else {

			// connection is established
			established = true;
		} 
	}
	public void close() throws IOException {
		_socket.close();

		// connection is NOT established
		established = false;
		message = "Socket closed";
	}
	public String getAuthenticatedSecurityDomain() {
		if (!established || auth == null) {
			return null;
		} 
		return auth.getSelectedDomainName();
	}
	public InputStream getInputStream() throws IOException {
		return new BufferedInputStream(_socket.getInputStream(), BUFFSIZE);
	}
	public String getMessage() {
		return message;
	}
	public OutputStream getOutputStream() throws IOException {
		output_type = true;

		// return new BufferedOutputStream(_socket.getOutputStream(),BUFFSIZE);
		return _socket.getOutputStream();
	}
	public boolean isEstablished() {
		return established;
	}
	public void sendRequest() throws IOException {
		if (output_type) {
			_socket.getOutputStream().flush();
		} 
	}
}
