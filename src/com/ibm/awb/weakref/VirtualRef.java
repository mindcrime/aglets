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

import java.io.*;

public class VirtualRef implements java.io.Serializable {
	private Ref _ref;

	public class NullRef implements Ref {
		public NullRef() {}

		public void referenced() {}

		public void unreferenced() {}

		public String getRefClassName() {
			return NullRef.class.getName();
		} 

		public Ref getRef(VirtualRef vref) {
			return null;
		} 

		public void writeInfo(ObjectOutputStream s) {

			// do nothing.
		} 

		public void setRef(VirtualRef vref, ObjectInputStream s) 
				throws IOException, ClassNotFoundException {}
	}

	static java.util.Hashtable cache = new java.util.Hashtable();

	public VirtualRef(Ref ref) {
		_ref = ref;
	}
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} 

		if (obj instanceof VirtualRef) {
			return getRef().equals(obj);

			/*
			 * ((VirtualRef)obj)._ref == _ref) {
			 * return true;
			 */
		} else {
			return _ref == obj;
		} 
	}
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
	private void readObject(ObjectInputStream s) 
			throws IOException, ClassNotFoundException {
		String name = (String)s.readObject();
		Ref tmp = (Ref)cache.get(name);

		if (tmp == null) {
			try {
				tmp = (Ref)Class.forName(name).newInstance();
				cache.put(name, tmp);
			} catch (Error ex) {
				System.out.println("Error:" + name);
				throw ex;
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			} 
		} 
		tmp.setRef(this, s);
	}
	/* package */
	public synchronized final void setRef(Ref new_ref) {
		if (_ref == new_ref) {
			System.out.println("Don't set the same reference!");
			Thread.dumpStack();
		} 
		if (_ref != null) {
			_ref.unreferenced();
		} 
		_ref = new_ref;
	}
	private void writeObject(ObjectOutputStream s) throws IOException {
		if (_ref == null) {
			_ref = new NullRef();
		} 
		s.writeObject(_ref.getRefClassName());
		_ref.writeInfo(s);

		/*
		 * } else {
		 * s.writeObject(_ref.getRefClassName());
		 * _ref.writeInfo(s);
		 * }
		 */
	}
}
