package com.ibm.agletx.patterns;

/*
 * @(#)SlaveError.java
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

import java.net.*;
import java.io.IOException;

/*
 * @version     1.20    $Date: 2009/07/27 10:31:41 $
 * @author      Yariv Aridor
 */

class SlaveError implements java.io.Serializable {

	public String host = null;
	public String text = null;

	public SlaveError(URL host, Throwable ex) {
		this.host = host.toString();
		text = ex.getClass().getName() + "::" + ex.getMessage();
	}
}
