package com.ibm.awb.misc;

import java.util.Hashtable;
import java.util.Enumeration;

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
			Exception e = (Exception)h.get(Thread.currentThread());

			if (e != null) {
				e.fillInStackTrace();
			} 
		} 
	}
	static public void check(Object obj) {
		if (debug) {
			System.out.println(obj);
			Exception e = (Exception)h.get(Thread.currentThread());

			if (e != null) {
				e.fillInStackTrace();
			} 
		} 
	}
	static public void debug(boolean b) {
		debug = b;
	}
	static public void end() {
		if (debug) {
			h.remove(Thread.currentThread());
		} 
	}
	static public void list(java.io.PrintStream p) {
		if (debug == false) {
			p.println("Debug off");
		} 
		Enumeration e = h.keys();

		while (e.hasMoreElements()) {
			Thread t = (Thread)e.nextElement();
			Exception ex = (Exception)h.get(t);

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
