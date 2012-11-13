package com.ibm.aglets;

/*
 * @(#)ShuttingDownException.java
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
 * 
 * @version 1.10 96/10/01
 * @author Mitsuru Oshima
 */
public class ShuttingDownException extends com.ibm.aglet.AgletException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6898173772708562507L;

	/*
	 * Constructs a ShuttingDownException.
	 * 
	 * @param s the detailed message
	 */
	public ShuttingDownException() {
	}

	/*
	 * Constructs a ShuttingDownException with the specified detailed message.
	 * 
	 * @param s the detailed message
	 */
	public ShuttingDownException(final String s) {
		super(s);
	}
}
