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

import java.net.URL;

/*
 * @version     1.20    $Date: 2009/07/28 07:04:53 $
 * @author      Yariv Aridor
 */

class SlaveError implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7395042054370501312L;
    public String host = null;
    public String text = null;

    public SlaveError(URL host, Throwable ex) {
	this.host = host.toString();
	this.text = ex.getClass().getName() + "::" + ex.getMessage();
    }
}
