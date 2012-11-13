package com.ibm.awb.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Hashtable;

/*
 * @(#)Archive.java
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

public class Archive implements java.io.Serializable {

	static final public class Entry implements java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -733774250305285238L;

		static final long _hashcode(final byte[] b) {
			long h = 0;

			for (final byte element : b) {
				h += (h * 37) + element;
			}
			return h;
		}

		String name;
		byte data[] = null;
		long digest = 0;

		Entry(final String n, final long d, final byte[] b) {
			name = n;
			digest = d;
			data = b;
		}

		public byte[] data() {
			return data;
		}

		public long digest() {
			if (digest == 0) {

				// System.out.println("computing digest");
				digest = _hashcode(digestGen.digest(data));
			}
			return digest;
		}

		public String name() {
			return name;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1400757867680413756L;;

	Hashtable cache = new Hashtable();

	static protected MessageDigest digestGen;
	static {
		try {
			digestGen = MessageDigest.getInstance("SHA");
		} catch (final java.security.NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
	}

	public synchronized Entry[] entries() {
		final Entry[] r = new Entry[cache.size()];
		final Enumeration e = cache.elements();
		int i = 0;

		while (e.hasMoreElements()) {
			r[i++] = (Entry) e.nextElement();
		}
		return r;

		/*
		 * String r[] = new String[cache.size()]; return r;
		 */
	}

	public synchronized Entry getEntry(final String name) {
		return (Entry) cache.get(name);
	}

	public byte[] getResourceAsByteArray(final String name) {
		return getResourceInCache(name);
	}

	public InputStream getResourceAsStream(final String name) {
		final byte[] b = getResourceInCache(name);

		if (b != null) {
			return new java.io.ByteArrayInputStream(b);
		}
		return null;
	}

	/*
	 * synchronized public ClassName[] getClassNames() { if (classtable == null)
	 * { ClassName classtable[] = new ClassName[cache.size()]; Enumeration e =
	 * cache.elements(); int i=0; while(e.hasMoreElements()) { Entry en =
	 * (Entry)e.nextElement(); byte b[] = DigestTable.toByteArray(en.digest());
	 * classtable[i] = new ClassName(en.name, b); } } return classtable; }
	 */

	/* package */
	protected byte[] getResourceInCache(final String name) {
		final Entry e = getEntry(name);

		if (e != null) {
			return e.data;
		}
		return null;
	}

	synchronized public void putResource(final String name, final byte[] res) {
		cache.put(name, new Entry(name, 0, res));
	}

	synchronized public void putResource(final String name, final long d, final byte[] res) {
		cache.put(name, new Entry(name, d, res));
	}

	/*
	 * private void writeObject(java.io.ObjectOutputStream s) throws IOException
	 * { s.defaultWriteObject(); System.out.println("writeObject" + cache); }
	 */
	private void readObject(final ObjectInputStream s)
	throws IOException,
	ClassNotFoundException {
		s.defaultReadObject();
	}

	synchronized public void removeResource(final String name) {
		cache.remove(name);
	}

	@Override
	public String toString() {
		final Enumeration e = cache.elements();
		final StringBuffer buffer = new StringBuffer();
		int i = 1;

		while (e.hasMoreElements()) {
			final Entry en = (Entry) e.nextElement();

			buffer.append("[" + i + "] " + en.name + ", " + en.digest() + '\n');
			i++;
		}
		return buffer.toString();
	}
}
