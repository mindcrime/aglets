package com.ibm.maf;

/*
 * @(#)DeserializationFailed.java
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
 * File: ./CfMAF/DeserializationFailed.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class DeserializationFailed extends MAFException {

    /**
     * 
     */
    private static final long serialVersionUID = -1899395305534997050L;

    // constructor
    public DeserializationFailed() {
	super();
    }

    public DeserializationFailed(String msg) {
	super(msg);
    }
}
