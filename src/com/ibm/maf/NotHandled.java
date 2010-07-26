package com.ibm.maf;

/*
 * @(#)NotHandled.java
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

public final class NotHandled extends MAFExtendedException {

    // constructor
    public NotHandled() {
	super();
    }

    public NotHandled(String msg) {
	super(msg);
    }
}
