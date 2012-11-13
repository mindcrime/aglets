package com.ibm.awb.misc;

import java.util.Enumeration;
import java.util.Hashtable;

/*
 * @(#)Debug.java
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

final public class Debug {
	static private Hashtable h = new Hashtable();

	static public boolean debug = false;

	static public void check() {
		if (debug) {
			final Exception e = (Exception) h.get(Thread.currentThread());

			if (e != null) {
				e.fillInStackTrace();
			}
		}
	}

	static public void check(final Object obj) {
		if (debug) {
			System.out.println(obj);
			final Exception e = (Exception) h.get(Thread.currentThread());

			if (e != null) {
				e.fillInStackTrace();
			}
		}
	}

	static public void debug(final boolean b) {
		debug = b;
	}

	static public void end() {
		if (debug) {
			h.remove(Thread.currentThread());
		}
	}

	static public void list(final java.io.PrintStream p) {
		if (debug == false) {
			p.println("Debug off");
		}
		final Enumeration e = h.keys();

		while (e.hasMoreElements()) {
			final Thread t = (Thread) e.nextElement();
			final Exception ex = (Exception) h.get(t);

			p.println("Thread = " + t);
			p.println("Latest StackTrace = ");
			ex.printStackTrace(p);
		}
	}

	static public void start() {
		if (debug) {
			h.put(Thread.currentThread(), new Exception());
		}
	}
}
