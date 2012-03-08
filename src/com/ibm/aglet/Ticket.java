package com.ibm.aglet;

/*
 * @(#)Ticket.java
 * 
 * (c) Copyright IBM Corp. 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import com.ibm.awb.misc.PortPattern;
import com.ibm.awb.misc.URIPattern;

/**
 * <tt>Ticket</tt>
 * @version     0.20    $Date: 2009/07/28 07:04:53 $
 * @author      ONO Kouichi
 */

/**
 * An agent who wants to trip somewhere, he has a ticket to there. A ticket
 * denotes destination of the trip and way to trip.
 */
public class Ticket implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3043680975007352670L;
    //
    // Scheme
    /**
     * <pre>
     * SCHEME = Protocol of agent transfer
     * </pre>
     * 
     * Scheme Name : Agent Transfer Protocol.
     */
    public static final String ATP = "atp";
    /**
     * Scheme Name : Hyper Text Transfer Protocol <a
     * href="http://www.w3.org/Protocols/">HTTP</a> is defined by <a
     * href="http://www.w3.org/">World Wide Web Consortium</a>.
     */
    public static final String HTTP = "http";
    /**
     * Scheme Name : Remote Method Invocation
     */
    public static final String RMI = "rmi";
    /**
     * Scheme Name : Internet Interoperable Object Reference <a
     * href="http://www.omg.org/corba/corbiiop.htm">IIOP</a> is defined by <a
     * href="http://www.omg.org/">OMG</a>.
     */
    public static final String IIOP = "iiop";
    /**
     * Scheme Name : default scheme name.
     */
    public static final String DEFAULTSCHEME = ATP;

    //
    // The destination to where the aglet wants to trip.
    //
    private String _protocol = null;
    private String _host = null;
    private String _file = null;
    private int _port = -1;

    //
    // The quality of aglet communication.
    //
    private QoC _qoc = null;

    //
    // The port number for connection.
    //
    private static final int DEFAULTATPPORTNO = 4434; // ATP default port
    private static int defaultPortNo = DEFAULTATPPORTNO;

    /**
     * Constructor with desitnation address of the trip. The quality of
     * communication for the trip is null. The scheme name is default("atp").
     * The port number is default.
     * 
     * @param host
     *            destination host of the trip
     * @exception MalformedURLException
     */
    public Ticket(String host) throws MalformedURLException {
	this(host, null);
    }

    /**
     * Constructor with desitnation address of the trip. The quality of
     * communication for the trip is null. The scheme name is default("atp").
     * The port number is default.
     * 
     * @param destination
     *            destination host of the trip
     * @param qoc
     *            quality of communication for the trip
     * @exception MalformedURLException
     * @see QoC
     */
    public Ticket(String destination, QoC qoc) throws MalformedURLException {
	this.set(destination, qoc);
    }

    /**
     * Constructor with desitnation address of the trip, quality of aglet
     * cummunication in the trip and port number of the connection.
     * 
     * @param host
     *            destination host of the trip
     * @param qoc
     *            quality of communication for the trip
     * @param scheme
     *            scheme name of communication
     * @param portNo
     *            number of the connection
     * @exception MalformedURLException
     * @see QoC
     */
    public Ticket(String host, QoC qoc, String scheme, int portNo)
    throws MalformedURLException {
	this.set(host, qoc, scheme, portNo);
    }

    /**
     * Constructor with desitnation address of the trip, quality of aglet
     * cummunication in the trip and port number of the connection.
     * 
     * @param address
     *            destination of the trip
     * @see QoC
     */
    public Ticket(URL address) {
	this(address, null);
    }

    /**
     * Constructor with desitnation address of the trip and quality of aglet
     * cummunication in the trip.
     * 
     * @param address
     *            destination of the trip
     * @param qoc
     *            quality of communication for the trip
     * @see QoC
     */
    public Ticket(URL address, QoC qoc) {
	this.set(address, qoc);
    }

    /**
     * Get default port number for connection.
     * 
     * @return default port number of the connection
     */
    public static int getDefaultPort() {
	return defaultPortNo;
    }

    /**
     * Get destination to where the aglet wants to trip.
     * 
     * @return destination of the trip
     */
    public URL getDestination() {
	URL url = null;
	if ((this._file != null) && !this._file.startsWith("/")) {
	} else {
	}
	try {
	    if (this.isDefaultPort()) {
		url = new URL(this._protocol, this._host, this._file);
	    } else {
		url = new URL(this._protocol, this._host, this._port, this._file);
	    }
	} catch (MalformedURLException excpt) {
	    excpt.printStackTrace();
	    return null;
	}
	return url;
    }

    /**
     * Returns a string representation of the destination.
     * 
     * @return a string representation of the destination
     * @see java.lang.Object#toString
     */
    private String getDestinationString() {
	String file = null;

	if ((this._file != null) && !this._file.startsWith("/")) {
	    file = "/" + this._file;
	}
	if ((this._protocol != null) && this._protocol.equalsIgnoreCase("file")) {
	    return this._protocol + ":" + file;
	} else {
	    if (this.isDefaultPort()) {
		return this._protocol + "://" + this._host + file;
	    } else {
		return this._protocol + "://" + this._host + ":" + this._port
		+ file;
	    }
	}
    }

    /**
     * Get specified file.
     * 
     * @return specified file
     */
    public String getFile() {
	return this._file;
    }

    /**
     * Get specified host.
     * 
     * @return specified host
     */
    public String getHost() {
	return this._host;
    }

    /**
     * Get specified port.
     * 
     * @return specified port
     */
    public int getPort() {
	return this._port;
    }

    /**
     * Get specified protocol.
     * 
     * @return specified protocol
     */
    public String getProtocol() {
	return this._protocol;
    }

    /**
     * Get quality of aglet communication.
     * 
     * @return quality of aglet communication in the trip
     */
    public QoC getQoC() {
	return this._qoc;
    }

    /**
     * Returns whether prot number is default.
     */
    public boolean isDefaultPort() {
	if (this._port == -1) {
	    return true;
	}

	// needed ?
	if (this._port == defaultPortNo) {
	    return true;
	}

	return false;
    }

    // for test
    static public void main(String[] args) {
	if (args.length == 0) {
	    return;
	}
	String destination = args[0];

	URL url = null;

	try {
	    url = new URL(destination);
	} catch (MalformedURLException excpt) {
	    excpt.printStackTrace();
	}
	if (url != null) {
	    System.out.println(url.toString());
	    System.out.println(url.getFile());
	    Ticket ticket = new Ticket(url);

	    if (ticket != null) {
		System.out.println(ticket.getDestination().toString());
		System.out.println(ticket.toString());
	    }
	}
    }

    /**
     * Set desitnation address of the trip, quality of aglet cummunication in
     * the trip and port number of the connection.
     * 
     * @param address
     *            destination of the trip
     * @param qoc
     *            quality of communication for the trip
     * @exception MalformedURLException
     * @see QoC
     */
    private void set(String address, QoC qoc) throws MalformedURLException {
	this.setDestination(address);
	this.setQoC(qoc);
    }

    /**
     * Set desitnation address of the trip, quality of aglet cummunication in
     * the trip and port number of the connection.
     * 
     * @param address
     *            destination of the trip
     * @param qoc
     *            quality of communication for the trip
     * @param scheme
     *            scheme name of communication
     * @param portNo
     *            number of the connection
     * @exception MalformedURLException
     * @see QoC
     */
    private void set(String address, QoC qoc, String scheme, int portNo)
    throws MalformedURLException {
	this.setDestination(address, scheme, portNo);
	this.setQoC(qoc);
    }

    /**
     * Set desitnation address of the trip, quality of aglet cummunication in
     * the trip and port number of the connection.
     * 
     * @param address
     *            destination of the trip
     * @param qoc
     *            quality of communication for the trip
     * @see QoC
     */
    private void set(URL address, QoC qoc) {
	this.setDestination(address);
	this.setQoC(qoc);
    }

    /**
     * Set default port number for connection.
     * 
     * @param portNo
     *            default port number of the connection
     */
    public static void setDefaultPort(int portNo) {
	defaultPortNo = portNo;
    }

    /**
     * Set destination to where the aglet wants to trip.
     * 
     * @param urlstr
     *            destination of the trip
     */
    public void setDestination(String urlstr) throws MalformedURLException {
	URIPattern destination = new URIPattern(urlstr);

	if (destination != null) {
	    this._protocol = destination.getProtocol();
	    this._host = destination.getHost();
	    this._file = destination.getFile();
	    PortPattern ppat = destination.getPortPattern();

	    if (ppat.isSinglePort()) {
		this._port = ppat.getFromPort();
	    }
	}
    }

    /**
     * Set destination to where the aglet wants to trip.
     * 
     * @param address
     *            destination of the trip
     * @param scheme
     *            scheme name of communication
     * @param portNo
     *            number of the connection
     * @exception MalformedURLException
     */
    public void setDestination(String address, String scheme, int portNo)
    throws MalformedURLException {
	String protocol = ATP;

	if (scheme.equalsIgnoreCase(ATP)) {
	    protocol = ATP;
	} else if (scheme.equalsIgnoreCase(HTTP)) {
	    protocol = HTTP;
	} else if (scheme.equalsIgnoreCase(RMI)) {
	    protocol = RMI;
	} else if (scheme.equalsIgnoreCase(IIOP)) {
	    protocol = IIOP;
	} else {

	    // no changes
	    // Why? M.O.
	    protocol = scheme;
	}

	URL url = null;

	try {
	    url = new URL(protocol, address, portNo, "");
	} catch (MalformedURLException excpt) {
	    url = null;
	    throw excpt;
	}
	this.setDestination(url);
    }

    /**
     * Set destination to where the aglet wants to trip.
     * 
     * @param destination
     *            destination of the trip
     */
    public void setDestination(URL destination) {
	if (destination != null) {
	    this._protocol = destination.getProtocol().toLowerCase();
	    this._host = destination.getHost();
	    this._file = destination.getFile();
	    this._port = destination.getPort();
	}
    }

    /**
     * Set quality of aglet communication.
     * 
     * @param qoc
     *            quality of aglet communication in the trip
     * @see QoC
     */
    public void setQoC(QoC qoc) {
	if (qoc == null) {

	    // _qoc = new QoC(NOINTEGRITY, NOCONFIDENTIALITY); // should be ?
	    this._qoc = new QoC();
	} else {
	    this._qoc = qoc;
	}
    }

    /**
     * Returns a string representation of the ticket.
     * 
     * @return a string representation of the ticket
     * @see java.lang.Object#toString
     */
    @Override
    public String toString() {
	URL url = this.getDestination();
	String destination = null;

	if (url != null) {
	    destination = url.toString();
	} else {
	    destination = this.getDestinationString();
	}
	final String qoc = this._qoc.toString();
	final String str = destination + ", " + qoc;

	return str;
    }
}
