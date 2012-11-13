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
	private final Hashtable _ref_table = new Hashtable();

	public void add(final WeakRef ref) {
		_ref_table.put(ref.getRefID(), ref);
	}

	protected void debug(final WeakRef current, final String title) {
		System.out.println(title);
		System.out.println(this.toString(current));
	}

	public WeakRef getWeakRef(final Object id) {
		return (WeakRef) _ref_table.get(id);
	}

	@Override
	public String toString() {
		return this.toString(null);
	}

	protected String toString(final WeakRef current) {
		final Enumeration e = _ref_table.elements();
		int i = 0;
		final StringBuffer buf = new StringBuffer();

		while (e.hasMoreElements()) {
			final WeakRef w = (WeakRef) e.nextElement();

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
	synchronized void unreference(final WeakRef wref) {
		synchronized (wref) {

			// debug(wref, "unreferenced");

			// System.out.println("current reference =" + wref._ref_count);
			wref._ref_count--;

			// System.out.println("current reference =" + wref._ref_count);
			if (wref._ref_count == 0) {

				// remove the reference from table
				if (_ref_table.contains(wref)) {
					_ref_table.remove(wref.getRefID());
				}
			}
		}
	}
}
