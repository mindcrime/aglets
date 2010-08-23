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

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

import com.ibm.atp.auth.Auth;
import com.ibm.atp.auth.Authentication;
import com.ibm.atp.auth.AuthenticationProtocolException;
import com.ibm.awb.misc.Resource;

/**
 * @version 1.00 $Date :$
 * @author Mitsuru Oshima
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
	this._socket = new Socket(url.getHost(), port);

	Resource res = Resource.getResourceFor("atp");
	boolean authentication = res.getBoolean("atp.authentication", false);

	if (authentication) {
	    DataInput di = new DataInputStream(this._socket.getInputStream());

	    // auth = new Authentication(Auth.FIRST_TURN, _socket);
	    this.auth = new Authentication(Auth.FIRST_TURN, di, this._socket);
	    boolean authenticated = true;

	    try {
		authenticated = this.auth.authenticate();
	    } catch (AuthenticationProtocolException excpt) {

		// protocol error
		System.err.println(excpt);
	    } catch (IOException excpt) {

		// protocol error
		System.err.println(excpt);
	    }
	    if (authenticated && this.auth.isAuthenticatedMyself()
		    && this.auth.isAuthenticatedOpponent()) {

		// connection is established
		this.established = true;
	    } else {
		this.close();
		this.message = "Authentication failed";
	    }
	} else {

	    // connection is established
	    this.established = true;
	}
    }

    @Override
    public void close() throws IOException {
	this._socket.close();

	// connection is NOT established
	this.established = false;
	this.message = "Socket closed";
    }

    @Override
    public String getAuthenticatedSecurityDomain() {
	if (!this.established || (this.auth == null)) {
	    return null;
	}
	return this.auth.getSelectedDomainName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
	return new BufferedInputStream(this._socket.getInputStream(), BUFFSIZE);
    }

    @Override
    public String getMessage() {
	return this.message;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
	this.output_type = true;

	// return new BufferedOutputStream(_socket.getOutputStream(),BUFFSIZE);
	return this._socket.getOutputStream();
    }

    @Override
    public boolean isEstablished() {
	return this.established;
    }

    @Override
    public void sendRequest() throws IOException {
	if (this.output_type) {
	    this._socket.getOutputStream().flush();
	}
    }
}
