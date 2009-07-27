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

import com.ibm.aglet.*;
import com.ibm.aglet.message.*;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglet.system.AgletRuntime;

import com.ibm.awb.misc.Resource;

import java.awt.Frame;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Date;
import java.security.Identity;

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
		int c = st.countTokens();

		if ("clone".equalsIgnoreCase(mtd)) {
			proxy.clone();
		} else if ("dispatch".equalsIgnoreCase(mtd)) {
			if (st.hasMoreTokens()) {
				proxy.dispatch(new URL(st.nextToken()));
			} else {
				new Error("aglet dispatch URL");
			} 
		} else if ("dialog".equalsIgnoreCase(mtd)) {
			proxy.sendAsyncMessage(new Message("dialog"));
		} else if ("dispose".equalsIgnoreCase(mtd)) {
			proxy.dispose();

		} else if ("activate".equalsIgnoreCase(mtd)) {
			proxy.activate();
		} else if ("deactivate".equalsIgnoreCase(mtd)) {
			if (st.hasMoreTokens()) {
				proxy.deactivate(Long.parseLong(st.nextToken()));
			} else {
				proxy.deactivate(0);
			} 
		} else if ("property".equalsIgnoreCase(mtd)) {
			System.out.println(toString());
		} else {
			System.out.println("unknown method " + mtd);
		} 
	}
	public boolean equals(Object obj) {
		return proxy == obj;
	}
	public boolean isValid() {
		return proxy.isValid();
	}
	void setText(String t) {
		text = t;
	}
	/**
	 * 
	 */
	public String toString() {
		StringBuffer all = new StringBuffer();

		try {
			AgletInfo info = proxy.getAgletInfo();

			// all.append(info.getPrivilegeName() + " Aglet" + "\n");
			all.append(" Aglet" + "\n");
			all.append(info.getAgletID() + "\n");
			java.security.cert
				.X509Certificate cert = 
					(java.security.cert
						.X509Certificate)info.getAuthorityCertificate();

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
