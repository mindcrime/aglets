package com.ibm.awb.weakref;

/*
 * @(#)WeakRefTable.java
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

import java.util.Enumeration;
import java.util.Hashtable;

public class WeakRefTable {
    private Hashtable _ref_table = new Hashtable();

    public void add(WeakRef ref) {
	this._ref_table.put(ref.getRefID(), ref);
    }

    protected void debug(WeakRef current, String title) {
	System.out.println(title);
	System.out.println(this.toString(current));
    }

    public WeakRef getWeakRef(Object id) {
	return (WeakRef) this._ref_table.get(id);
    }

    @Override
    public String toString() {
	return this.toString(null);
    }

    protected String toString(WeakRef current) {
	Enumeration e = this._ref_table.elements();
	int i = 0;
	StringBuffer buf = new StringBuffer();

	while (e.hasMoreElements()) {
	    WeakRef w = (WeakRef) e.nextElement();

	    if (w == current) {
		buf.append("*[" + i + "]" + w.toString() + "\n");
	    } else {
		buf.append(" [" + i + "]" + w.toString() + "\n");
	    }
	    i++;
	}
	return buf.toString();
    }

    /* package */
    synchronized void unreference(WeakRef wref) {
	synchronized (wref) {

	    // debug(wref, "unreferenced");

	    // System.out.println("current reference =" + wref._ref_count);
	    wref._ref_count--;

	    // System.out.println("current reference =" + wref._ref_count);
	    if (wref._ref_count == 0) {

		// remove the reference from table
		if (this._ref_table.contains(wref)) {
		    this._ref_table.remove(wref.getRefID());
		}
	    }
	}
    }
}
