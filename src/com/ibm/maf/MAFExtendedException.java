package com.ibm.maf;

/*
 * @(#)MAFExtendedException.java
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
 * File: ./CfMAF/MAFExtendedException.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public class MAFExtendedException extends MAFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2834711930048438069L;

	// constructor
	public MAFExtendedException() {
		super();
	}

	public MAFExtendedException(final String msg) {
		super(msg);
	}
}
