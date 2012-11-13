package com.ibm.atp.auth;

/*
 * @(#)AuthenticationException.java
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
 * The <tt>AuthenticationException</tt> class is the authentication protocol
 * exception class.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class AuthenticationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 154613877253129357L;

	/**
	 * Default Constructor shows stack trace.
	 */
	public AuthenticationException() {
		this.printStackTrace();
	}

	/**
	 * Constructor shows stack trace with the specified detailed message.
	 * 
	 * @param msg
	 *            the detailed message
	 */
	public AuthenticationException(final String msg) {
		super(msg);
		this.printStackTrace();
	}
}
