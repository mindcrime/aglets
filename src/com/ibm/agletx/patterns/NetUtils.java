package com.ibm.agletx.patterns;

/*
 * @(#)NetUtils.java
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

public class NetUtils {

	static public int getDefaultPort(final String protocol) {
		if ("http".equals(protocol)) {
			return 80;
		} else if ("atp".equals(protocol)) {
			return 4434;
		} else if ("smtp".equals(protocol)) {
			return 25;
		} else if ("ftp".equals(protocol)) {
			return 21;
		}
		return -1;
	}

	static public void main(final String arg[]) throws java.io.IOException {
		System.out.println(sameURL(new URL("http://test/foo"), new URL("http://test:80/foo")));
		System.out.println(sameURL(new URL("http://test.trl.ibm.com/foo"), new URL("http://test:80/foo")));
		System.out.println(sameURL(new URL("http://test"), new URL("http://test/")));
		System.out.println(sameURL(new URL("http://test"), new URL("http://test/foo")));
	}

	static public boolean sameURL(final URL u1, final URL u2) {
		if ((u1 == null) || (u2 == null)) {
			return false;
		}
		final String u1_protocol = u1.getProtocol();
		final String u2_protocol = u2.getProtocol();

		final int u1_port = u1.getPort() == -1 ? getDefaultPort(u1_protocol)
				: u1.getPort();
		final int u2_port = u2.getPort() == -1 ? getDefaultPort(u2_protocol)
				: u2.getPort();

		return (u1_protocol.equals(u2_protocol)
				&& u1.getHost().equals(u2.getHost()) && (u1_port == u2_port) && u1.getFile().equals(u2.getFile()));
	}
}
