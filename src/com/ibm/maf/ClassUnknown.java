package com.ibm.maf;

/*
 * @(#)ClassUnknown.java
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
 * File: ./CfMAF/ClassUnknown.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class ClassUnknown extends MAFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1862741292374671849L;

	// constructor
	public ClassUnknown() {
		super();
	}

	public ClassUnknown(final String msg) {
		super(msg);
	}
}
