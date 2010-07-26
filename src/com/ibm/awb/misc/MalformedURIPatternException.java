package com.ibm.awb.misc;

import java.net.MalformedURLException;

/*
 * @(#)MalformedURIPatternException.java
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

/**
 * The <tt>MalformendURIPatternException</tt> class shows the specified URI
 * pattern is malformed.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class MalformedURIPatternException extends MalformedURLException {
    static private boolean debug = false;

    public MalformedURIPatternException() {
	if (debug) {
	    this.printStackTrace();
	}
    }

    public MalformedURIPatternException(String msg) {
	super(msg);
	if (debug) {
	    this.printStackTrace();
	}
    }
}
