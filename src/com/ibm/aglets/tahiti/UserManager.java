package com.ibm.aglets.tahiti;

/*
 * @(#)UserManager.java
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;

import com.ibm.aglets.AgletRuntime;

/**
 * <tt>UserManager</tt> class specifies user manager of aglet server.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public abstract class UserManager {
    private static String DEFAULT_USERNAME = null;
    static {
	DEFAULT_USERNAME = (String) AccessController.doPrivileged(new PrivilegedAction() {
	    @Override
	    public Object run() {
		return System.getProperty("user.name");
	    }
	});
    }

    private String _username = null;
    private Certificate _certificate = null;

    public Certificate getCertificate() {
	return this._certificate;
    }

    static public final String getDefaultUsername() {
	return DEFAULT_USERNAME;
    }

    /* protected */
    public static Certificate getRegisteredCertificate(String username) {
	return AgletRuntime.getRegisteredCertificate(username);
    }

    public String getUsername() {
	return this._username;
    }

    protected static boolean isRegisteredUser(String username) {
	return (getRegisteredCertificate(username) != null);
    }

    public abstract Certificate login();

    protected void setCertificate(Certificate cert) {
	this._certificate = cert;
    }

    protected void setUsername(String username) {
	this._username = username;
    }
}
