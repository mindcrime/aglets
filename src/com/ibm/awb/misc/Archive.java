package com.ibm.awb.misc;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Enumeration;

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
		Entry(String n, long d, byte[] b) {
			name = n;
			digest = d;
			data = b;
		}
		String name;
		byte data[] = null;
		long digest = 0;

		public String name() {
			return name;
		} 

		public long digest() {
			if (digest == 0) {

				// System.out.println("computing digest");
				digest = _hashcode(digestGen.digest(data));
			} 
			return digest;
		} 

		public byte[] data() {
			return data;
		} 

		static final long _hashcode(byte[] b) {
			long h = 0;

			for (int i = 0; i < b.length; i++) {
				h += (h * 37) + (long)b[i];
			} 
			return h;
		} 
	}
	;

	Hashtable cache = new Hashtable();

	static protected MessageDigest digestGen;
	static {
		try {
			digestGen = MessageDigest.getInstance("SHA");
		} catch (java.security.NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} 
	} 
	public synchronized Entry[] entries() {
		Entry[] r = new Entry[cache.size()];
		Enumeration e = cache.elements();
		int i = 0;

		while (e.hasMoreElements()) {
			r[i++] = (Entry)e.nextElement();
		} 
		return r;

		/*
		 * String r[] = new String[cache.size()];
		 * return r;
		 */
	}
	public synchronized Entry getEntry(String name) {
		return (Entry)cache.get(name);
	}
	public byte[] getResourceAsByteArray(String name) {
		return getResourceInCache(name);
	}
	public InputStream getResourceAsStream(String name) {
		byte[] b = getResourceInCache(name);

		if (b != null) {
			return new java.io.ByteArrayInputStream(b);
		} 
		return null;
	}
	/*
	 * synchronized public ClassName[] getClassNames() {
	 * if (classtable == null) {
	 * ClassName classtable[] = new ClassName[cache.size()];
	 * Enumeration e = cache.elements();
	 * int i=0;
	 * while(e.hasMoreElements()) {
	 * Entry en = (Entry)e.nextElement();
	 * byte b[] = DigestTable.toByteArray(en.digest());
	 * classtable[i] = new ClassName(en.name, b);
	 * }
	 * }
	 * return classtable;
	 * }
	 */

	/* package */
	protected byte[] getResourceInCache(String name) {
		Entry e = getEntry(name);

		if (e != null) {
			return e.data;
		} 
		return null;
	}
	synchronized public void putResource(String name, byte[] res) {
		cache.put(name, new Entry(name, 0, res));
	}
	synchronized public void putResource(String name, long d, byte[] res) {
		cache.put(name, new Entry(name, d, res));
	}
	/*
	 * private void writeObject(java.io.ObjectOutputStream s) throws IOException {
	 * s.defaultWriteObject();
	 * System.out.println("writeObject" + cache);
	 * }
	 */
	private void readObject(ObjectInputStream s) 
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
	}
	synchronized public void removeResource(String name) {
		cache.remove(name);
	}
	public String toString() {
		Enumeration e = cache.elements();
		StringBuffer buffer = new StringBuffer();
		int i = 1;

		while (e.hasMoreElements()) {
			Entry en = (Entry)e.nextElement();

			buffer.append("[" + i + "] " + en.name + ", " + en.digest() 
						  + '\n');
			i++;
		} 
		return buffer.toString();
	}
}
