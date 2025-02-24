package com.ibm.maf;

/*
 * @(#)AuthInfo.java
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
 * File: ./CfMAF/AuthInfo.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class AuthInfo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3218233482062226510L;
	// instance variables
	public boolean is_auth;
	public short authenticator;

	// constructors
	public AuthInfo() {
	}

	public AuthInfo(final boolean __is_auth, final short __authenticator) {
		is_auth = __is_auth;
		authenticator = __authenticator;
	}
}
