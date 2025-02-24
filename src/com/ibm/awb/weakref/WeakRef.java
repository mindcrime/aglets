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

import java.io.IOException;
import java.io.ObjectInputStream;

public abstract class WeakRef implements Ref {

	/* package */
	long _ref_count = 0;
	protected WeakRefTable _table = null;

	protected WeakRef(final WeakRefTable table) {
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
	throws IOException,
	ClassNotFoundException;

	/* package */
	/* synchronized */
	@Override
	public final Ref getRef(final VirtualRef vref) {
		final Ref found = this.findRef();

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
	@Override
	synchronized public final void referenced() {

		// System.out.println("-- referenced --");
		// Thread.dumpStack();
		_ref_count++;
	}

	/*
	 * Get reference and increment refernce count
	 */
	/* package */
	@Override
	public final void setRef(final VirtualRef vref, final ObjectInputStream s)
	throws IOException,
	ClassNotFoundException {

		// this reference must be already updated.
		final Ref ref = this.findRef(s);

		if (ref != null) {
			vref.setRef(ref);
		}
	}

	@Override
	public String toString() {
		return "WeakRef[count=" + _ref_count + "]";
	}

	/*
	 * Called when the proxy unreferenced the ref.
	 */
	/* package */
	@Override
	public/* synchronized */final void unreferenced() {
		_table.unreference(this);
	}

	protected boolean updateRef(final VirtualRef vref) {
		return vref.getRef() == this;
	}
}
