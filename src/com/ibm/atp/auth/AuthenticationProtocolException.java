package com.ibm.atp.auth;

/*
 * @(#)AuthenticationProtocolException.java
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

import java.net.ProtocolException;

/**
 * The <tt>AuthenticationProtocolException</tt> class is the authentication
 * protocol exception class.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class AuthenticationProtocolException extends ProtocolException {
    /**
     * 
     */
    private static final long serialVersionUID = 4612183429410290207L;

    /**
     * Default Constructor shows stack trace.
     */
    public AuthenticationProtocolException() {
	this.printStackTrace();
    }

    /**
     * Constructor shows stack trace with the specified detailed message.
     * 
     * @param msg
     *            the detailed message
     */
    public AuthenticationProtocolException(String msg) {
	super(msg);
	this.printStackTrace();
    }
}
