package com.ibm.aglets.tahiti;

/*
 * @(#)TahitiUserManager.java
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

import java.security.cert.Certificate;

public final class TahitiUserManager extends UserManager {
    public TahitiUserManager() {
    }

    @Override
    public Certificate login() {
	LoginDialog d = new LoginDialog();

	d.popupAtCenterOfScreen();
	d.toFront();
	d.waitForAuthentication();
	if (d.checkAuthentication() == false) {
	    System.out.println("Authentication Failed");
	    System.exit(1);
	}
	this.setUsername(d.getUsername());
	this.setCertificate(d.getCertificate());
	return this.getCertificate();
    }
}
