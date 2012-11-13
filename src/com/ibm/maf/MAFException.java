package com.ibm.maf;

/*
 * @(#)MAFException.java
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

/*
 * File: ./CfMAF/MAFException.java
 */

public class MAFException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6032510223225478912L;

	// constructor
	public MAFException() {
		super();
	}

	public MAFException(final String msg) {
		super(msg);
	}
}
