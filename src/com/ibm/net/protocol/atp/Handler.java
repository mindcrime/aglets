package com.ibm.net.protocol.atp;

/*
 * @(#)Handler.java
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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A stream protocol handler for atp protocol.
 * 
 * @version 1.00 96/06/20
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

public class Handler extends URLStreamHandler {

    /**
     * Opens a connection to the object referenced by the URL argument.
     * 
     * @param url
     *            the URL that this connect to.
     * @return an AtpURLConnection object for the URL.
     */
    @Override
    public URLConnection openConnection(URL url) throws IOException {
	return new URLConnectionForATP(url);
    }
}
