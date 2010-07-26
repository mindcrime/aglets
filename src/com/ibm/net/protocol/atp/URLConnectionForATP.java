package com.ibm.net.protocol.atp;

/*
 * @(#)URLConnectionForATP.java
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import com.ibm.awb.misc.Resource;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.MAFAgentSystem;

/**
 * An instance of this class creates a communication link between an application
 * and an atp server.
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:54 $
 * @author Mitsuru Oshima
 */

public class URLConnectionForATP extends URLConnection {

    protected static boolean verbose = false;

    static AgentProfile agent_profile = null;

    /*
	 * 
	 */
    static {
	Resource res = Resource.getResourceFor("aglets");

	verbose = res.getBoolean("aglets.verbose", false);

	short one = (short) 1;

	agent_profile = new AgentProfile(one, // Java
	one, // Aglets
	"Aglets", one, // major,
	one, // minor,
	one, // serialization,
	null);
    }

    /*
	 * 
	 */
    private InputStream _inputStream = null;

    // MAFAgentSystem_ATP agentsystem;
    MAFAgentSystem agentsystem;
    Properties request_properties = new Properties();

    /*
     * Header field
     */
    private Properties headers = new Properties();

    /**
     * Create a new instance of this class.
     * 
     * @param url
     *            a destination URL to which the application connects. The
     *            protocol is "atp".
     */
    public URLConnectionForATP(URL url) throws IOException {
	super(url);
    }

    /**
     * Make a comminucation link with the destination.
     * 
     * @exception IOException
     *                if can not make a communication link.
     */
    @Override
    synchronized public void connect() throws IOException {
	if (this.connected) {
	    return;
	}
	this.agentsystem = MAFAgentSystem.getMAFAgentSystem(this.url.toString());

	// MAFAgentSystem_ATPClient.getMAFAgentSystem_ATP(url.toString());
	if (this.agentsystem == null) {
	    throw new IOException("ConnectionFailed");
	}
	this.connected = true;
    }

    @Override
    public String getHeaderField(String key) {
	return this.headers.getProperty(key.toLowerCase());
    }

    public String getHeaderField(String key, String defValue) {
	String ret = this.getHeaderField(key);

	return ret == null ? defValue : ret;
    }

    /**
     * Get an input stream of the communication link.
     * 
     * @return an input stream.
     * @exception IOException
     *                if the communication link has a problem.
     */
    @Override
    synchronized public InputStream getInputStream() throws IOException {
	if (this._inputStream != null) {
	    return this._inputStream;
	}
	this.connect();
	if (!this.connected) {
	    return null;
	}

	try {
	    byte result[][] = this.agentsystem.fetch_class(null, this.url.toString(), agent_profile);

	    if ((result != null) && (result.length == 1)) {
		this.headers.put("content-length", String.valueOf(result[0].length));
		return new ByteArrayInputStream(result[0]);
	    } else {
		throw new IOException(this.url.toString());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new IOException(ex.getClass().getName() + ":"
		    + ex.getMessage());
	}
    }

    @Override
    public String getRequestProperty(String key) {
	return this.request_properties.getProperty(key);
    }

    /*
     * Sets request parameters
     */
    @Override
    public void setRequestProperty(String key, String value) {
	if (this.connected) {
	    throw new IllegalAccessError("Already connected");
	}
	this.request_properties.put(key, value);
    }
}
