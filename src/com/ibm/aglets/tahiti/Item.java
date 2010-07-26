package com.ibm.aglets.tahiti;

/*
 * @(#)CommandLine.java
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
import java.util.Date;
import java.util.StringTokenizer;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;

class Item {
    AgletProxy proxy;
    String text = "";

    Item(AgletProxy proxy) {
	this.proxy = proxy;
    }

    /**
     * Executs the command given as arguments to the aglet instance
     */
    public void command(String mtd, StringTokenizer st) throws Exception {
	st.countTokens();

	if ("clone".equalsIgnoreCase(mtd)) {
	    this.proxy.clone();
	} else if ("dispatch".equalsIgnoreCase(mtd)) {
	    if (st.hasMoreTokens()) {
		this.proxy.dispatch(new URL(st.nextToken()));
	    } else {
		new Error("aglet dispatch URL");
	    }
	} else if ("dialog".equalsIgnoreCase(mtd)) {
	    this.proxy.sendAsyncMessage(new Message("dialog"));
	} else if ("dispose".equalsIgnoreCase(mtd)) {
	    this.proxy.dispose();

	} else if ("activate".equalsIgnoreCase(mtd)) {
	    this.proxy.activate();
	} else if ("deactivate".equalsIgnoreCase(mtd)) {
	    if (st.hasMoreTokens()) {
		this.proxy.deactivate(Long.parseLong(st.nextToken()));
	    } else {
		this.proxy.deactivate(0);
	    }
	} else if ("property".equalsIgnoreCase(mtd)) {
	    System.out.println(this.toString());
	} else {
	    System.out.println("unknown method " + mtd);
	}
    }

    @Override
    public boolean equals(Object obj) {
	return this.proxy == obj;
    }

    public boolean isValid() {
	return this.proxy.isValid();
    }

    void setText(String t) {
	this.text = t;
    }

    /**
	 * 
	 */
    @Override
    public String toString() {
	StringBuffer all = new StringBuffer();

	try {
	    AgletInfo info = this.proxy.getAgletInfo();

	    // all.append(info.getPrivilegeName() + " Aglet" + "\n");
	    all.append(" Aglet" + "\n");
	    all.append(info.getAgletID() + "\n");
	    java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) info.getAuthorityCertificate();

	    all.append(cert.getSubjectDN().getName() + "\n");
	    all.append(new Date(info.getCreationTime()));
	    all.append("\n" + info.getAgletClassName() + "\n");
	    all.append((info.getCodeBase() == null) ? "Local host"
		    : info.getCodeBase().toString() + "\n");
	    all.append(info.getAPIMajorVersion() + '.'
		    + info.getAPIMinorVersion());
	} catch (InvalidAgletException ex) {
	    all.append("Unavailable");
	}
	return all.toString();
    }
}
