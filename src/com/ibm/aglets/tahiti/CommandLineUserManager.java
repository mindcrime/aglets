package com.ibm.aglets.tahiti;

/*
 * @(#)CommandLineUserManager.java
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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.cert.Certificate;

import com.ibm.aglet.system.AgletRuntime;

public final class CommandLineUserManager extends UserManager {
    public CommandLineUserManager() {
    }

    private String input(String title, String defval, boolean enforce) {
	String line = null;
	LineNumberReader r = new LineNumberReader(new InputStreamReader(System.in));

	while (true) {
	    System.out.print(title
		    + ((defval == null) || (defval.length() == 0) ? ":" : "["
			    + defval + "]:"));
	    System.out.flush();
	    try {
		line = r.readLine();
	    } catch (IOException ex) {
	    }
	    if (line == null) {
		System.exit(1);
	    }
	    if (line.trim().length() != 0) {
		return line.trim();
	    } else if ((defval != null) && (defval.length() != 0)) {
		return defval;
	    } else if (enforce == false) {
		return null;
	    }
	}
    }

    /*
	 * 
	 */
    private String inputUsername(String title) {
	return this.inputUsername(title, getDefaultUsername());
    }

    /*
	 * 
	 */
    private String inputUsername(String title, String defaultUsername) {
	return this.input(title, defaultUsername, true);
    }

    @Override
    public Certificate login() {
	AgletRuntime runtime = AgletRuntime.getAgletRuntime();

	if (runtime == null) {
	    return null;
	}
	Certificate cert = null;
	String username = null;

	while (cert == null) {
	    while (username == null) {
		username = this.inputUsername("login");

		// if(!isRegisteredUser(username)) {
		// System.out.println("The username is not registered.");
		// username = null;
		// }
	    }
	    String password = this.input("password", "", false);

	    if (password == null) {
		password = "";
	    }
	    cert = runtime.authenticateOwner(username, password);
	    if (cert == null) {
		System.out.println("Password is incorrect.");
		username = null;
	    }
	}
	this.setUsername(username);
	this.setCertificate(cert);
	return cert;
    }
}
