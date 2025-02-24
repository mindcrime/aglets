package com.ibm.aglets;

/*
 * @(#)AgletsSecurityException.java
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

public class AgletsSecurityException extends SecurityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5259324066007646020L;

	/*
	 * Constructs a AgletsSecurityException.
	 */
	public AgletsSecurityException() {
		this.printStackTrace();
	}

	/*
	 * Constructs a AgletsSecurityException with the specified detailed message.
	 * 
	 * @param s the detailed message
	 */
	public AgletsSecurityException(final String s) {
		super(s);
		this.printStackTrace();
	}
}
