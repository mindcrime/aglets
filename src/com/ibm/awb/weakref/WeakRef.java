package com.ibm.awb.weakref;

/*
 * @(#)WeakRef.java
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

import java.io.*;

public abstract class WeakRef implements Ref {

	/* package */
	long _ref_count = 0;
	protected WeakRefTable _table = null;

	protected WeakRef(WeakRefTable table) {
		_table = table;
	}
	/*
	 * Find new reference if any
	 */
	abstract protected Ref findRef();
	/*
	 * Find a reference in a table.
	 */
	abstract protected Ref findRef(ObjectInputStream s) 
			throws IOException, ClassNotFoundException;
	/* package */
	/* synchronized */
	public final Ref getRef(VirtualRef vref) {
		Ref found = findRef();

		// this reference must be updated.
		if (found != null) {
			vref.setRef(found);
			return found;
		} 
		return this;
	}
	/*
	 * Get Reference ID
	 */
	abstract protected Object getRefID();
	/*
	 * Called when referenced
	 */
	/* package */
	synchronized public final void referenced() {

		// System.out.println("-- referenced --");
		// Thread.dumpStack();
		_ref_count++;
	}
	/*
	 * Get reference and increment refernce count
	 */
	/* package */
	public final void setRef(VirtualRef vref, ObjectInputStream s) 
			throws IOException, ClassNotFoundException {

		// this reference must be already updated.
		Ref ref = findRef(s);

		if (ref != null) {
			vref.setRef(ref);
		} 
	}
	public String toString() {
		return "WeakRef[count=" + _ref_count + "]";
	}
	/*
	 * Called when the proxy unreferenced the ref.
	 */
	/* package */
	public /* synchronized */ final void unreferenced() {
		_table.unreference(this);
	}
	protected boolean updateRef(VirtualRef vref) {
		return vref.getRef() == this;
	}
}
