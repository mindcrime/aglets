package com.ibm.awb.weakref;

/*
 * @(#)VirtualRef.java
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
import java.io.ObjectOutputStream;

public class VirtualRef implements java.io.Serializable {
	public class NullRef implements Ref {
		public NullRef() {
		}

		@Override
		public Ref getRef(final VirtualRef vref) {
			return null;
		}

		@Override
		public String getRefClassName() {
			return NullRef.class.getName();
		}

		@Override
		public void referenced() {
		}

		@Override
		public void setRef(final VirtualRef vref, final ObjectInputStream s)
		throws IOException,
		ClassNotFoundException {
		}

		@Override
		public void unreferenced() {
		}

		@Override
		public void writeInfo(final ObjectOutputStream s) {

			// do nothing.
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1428089129052202920L;

	private Ref _ref;

	static java.util.Hashtable cache = new java.util.Hashtable();

	public VirtualRef(final Ref ref) {
		_ref = ref;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof VirtualRef) {
			return getRef().equals(obj);

			/*
			 * ((VirtualRef)obj)._ref == _ref) { return true;
			 */
		} else {
			return _ref == obj;
		}
	}

	@Override
	synchronized protected final void finalize() {
		if (_ref != null) {
			_ref.unreferenced();
			_ref = null;
		}
	}

	public final Ref getCurrentRef() {
		return _ref;
	}

	public final Ref getRef() {
		return _ref.getRef(this);
	}

	private void readObject(final ObjectInputStream s)
	throws IOException,
	ClassNotFoundException {
		final String name = (String) s.readObject();
		Ref tmp = (Ref) cache.get(name);

		if (tmp == null) {
			try {
				tmp = (Ref) Class.forName(name).newInstance();
				cache.put(name, tmp);
			} catch (final Error ex) {
				System.out.println("Error:" + name);
				throw ex;
			} catch (final Exception ex) {
				ex.printStackTrace();
				return;
			}
		}
		tmp.setRef(this, s);
	}

	/* package */
	public synchronized final void setRef(final Ref new_ref) {
		if (_ref == new_ref) {
			System.out.println("Don't set the same reference!");
			Thread.dumpStack();
		}
		if (_ref != null) {
			_ref.unreferenced();
		}
		_ref = new_ref;
	}

	private void writeObject(final ObjectOutputStream s) throws IOException {
		if (_ref == null) {
			_ref = new NullRef();
		}
		s.writeObject(_ref.getRefClassName());
		_ref.writeInfo(s);

		/*
		 * } else { s.writeObject(_ref.getRefClassName()); _ref.writeInfo(s); }
		 */
	}
}
