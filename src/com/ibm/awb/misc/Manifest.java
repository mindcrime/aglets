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
    private Hashtable sections = new Hashtable();

    public Manifest(InputStream in) throws IOException {
	sun.tools.jar.Manifest m = new sun.tools.jar.Manifest(in, false);
	Enumeration e = m.entries();

	while (e.hasMoreElements()) {
	    MessageHeader mh = (MessageHeader) e.nextElement();

	    Hashtable h = new Hashtable();

	    for (int i = 0; true; i++) {
		String k = mh.getKey(i);
		String v = mh.getValue(i);

		if (k == null) {
		    break;
		} else if (k.equalsIgnoreCase("name")) {
		    this.sections.put(v, h);
		} else if (k.equalsIgnoreCase("manifest-version")) {
		}
		h.put(k.toUpperCase(), v);
	    }
	}
    }

    public boolean contains(String name) {
	return this.sections.get(name) != null;
    }

    /*
     * public DigestTable getDigestTable() { return null; }
     */

    public String[] getDependencies(String name) {
	Hashtable h = (Hashtable) this.sections.get(name);

	if (h != null) {
	    String s = (String) h.get("DEPENDS-ON");
	    StringTokenizer st = new StringTokenizer(s, " ,");
	    Vector v = new Vector();

	    while (st.hasMoreTokens()) {
		v.addElement(st.nextToken());
	    }
	    String ret[] = new String[v.size()];

	    v.copyInto(ret);
	    return ret;
	}
	return null;
    }

    public boolean isAglet(String classname) {
	Hashtable h = (Hashtable) this.sections.get(classname);

	if (h != null) {
	    return h.get("AGLETS") != null;
	}
	return false;
    }
}
