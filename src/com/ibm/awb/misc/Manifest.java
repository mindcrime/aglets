package com.ibm.awb.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import sun.net.www.MessageHeader;

/*
 * @(#)Manifest.java
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

public class Manifest implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2955273033902072906L;
	private final Hashtable sections = new Hashtable();

	public Manifest(final InputStream in) throws IOException {
		final sun.tools.jar.Manifest m = new sun.tools.jar.Manifest(in, false);
		final Enumeration e = m.entries();

		while (e.hasMoreElements()) {
			final MessageHeader mh = (MessageHeader) e.nextElement();

			final Hashtable h = new Hashtable();

			for (int i = 0; true; i++) {
				final String k = mh.getKey(i);
				final String v = mh.getValue(i);

				if (k == null) {
					break;
				} else if (k.equalsIgnoreCase("name")) {
					sections.put(v, h);
				} else if (k.equalsIgnoreCase("manifest-version")) {
				}
				h.put(k.toUpperCase(), v);
			}
		}
	}

	public boolean contains(final String name) {
		return sections.get(name) != null;
	}

	/*
	 * public DigestTable getDigestTable() { return null; }
	 */

	public String[] getDependencies(final String name) {
		final Hashtable h = (Hashtable) sections.get(name);

		if (h != null) {
			final String s = (String) h.get("DEPENDS-ON");
			final StringTokenizer st = new StringTokenizer(s, " ,");
			final Vector v = new Vector();

			while (st.hasMoreTokens()) {
				v.addElement(st.nextToken());
			}
			final String ret[] = new String[v.size()];

			v.copyInto(ret);
			return ret;
		}
		return null;
	}

	public boolean isAglet(final String classname) {
		final Hashtable h = (Hashtable) sections.get(classname);

		if (h != null) {
			return h.get("AGLETS") != null;
		}
		return false;
	}
}
