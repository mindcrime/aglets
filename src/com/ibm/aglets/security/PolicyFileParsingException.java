package com.ibm.aglets.security;

/*
 * @(#)PolicyFileParsingException.java
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

import java.io.IOException;

/**
 * The <tt>PolicyFileParsingException</tt> class shows that
 * the file cannot be parsed by the reason of illegal format.
 * 
 * @version     1.00    $Date: 2009/07/28 07:04:53 $
 * @author      ONO Kouichi
 */
public class PolicyFileParsingException extends IOException {
	public PolicyFileParsingException() {
		printStackTrace();
	}
	public PolicyFileParsingException(String msg) {
		super(msg);
		printStackTrace();
	}
}
