package com.ibm.aglets.tahiti;

/*
 * @(#)CacheManager.java
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
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.Resource;

public class CacheManager {
	private static CacheManager _singleton = null;

	private long _cache_size = 0;

	String _pool_dir = "";
	int _pool_index = 0;

	private LinkedList _cache = null;
	private Hashtable _pool = null;

	// t    static final int _hashcode(byte[] b) {
	// t	int h = 0;
	// t	for(int i=0; i<b.length; i++) {
	// t	    h += (h * 37) + (int)b[i];
	// t	}
	// t	return h;
	// t    }

	private final class LinkedList {
		Hashtable tab;		// (key, val) = (entry, ListElem)
		ListElem first = null;
		long size;			// size in bytes.
		long maxSize;		// max size in bytes.

		private final class ListElem {
			Entry entry;
			ListElem prev;
			ListElem next;

			ListElem(Entry e) {
				entry = e;
				prev = null;
				next = null;
			}
			ListElem(Entry e, ListElem n) {
				entry = e;
				prev = null;
				next = n;
			}
			ListElem(Entry e, ListElem p, ListElem n) {
				entry = e;
				prev = p;
				next = n;
			}
			public boolean equals(Object e) {
				if (e == null) {
					return false;
				} 
				if (e instanceof Entry) {
					return entry.equals((Entry)e);
				} else if (e instanceof ListElem) {
					if (entry == null) {
						return ((((ListElem)e)).entry == null);
					} else {
						return entry.equals(((ListElem)e).entry);
					} 
				} else {
					return false;
				} 
			} 

			public int hashCode() {
				if (entry == null) {
					return 0;
				} else {
					return entry.hashCode();
				}
			} 
		}

		LinkedList(long max) {
			tab = new Hashtable();
			first = null;
			size = 0;
			maxSize = max;
		}

		void printOut() {
			int i = 0;
			ListElem elm = first;

			System.out.println("LinkedList: size=" + size + ", maxSize=" 
							   + maxSize + ", tabSize=" + tab.size());
			while (elm != null && elm.next != first) {
				System.out.println("Queue[" + i + "]=" + elm.entry);
				i++;
				elm = elm.next;
			} 
		} 

		boolean contains(Entry e) {
			return tab.contains(e);
		} 

		synchronized Entry getFirst() {
			return (first == null) ? null : first.entry;
		} 

		synchronized Entry get(Entry e) {
			ListElem elem = (ListElem)tab.get(e);

			if (elem == null) {
				return null;
			} else {
				return elem.entry;
			} 
		} 

		synchronized Entry[] putAtFirst(Entry e) {
			if (maxSize == 0 || e == null) {
				return null;
			} 
			Entry purged[] = null;
			ListElem elem = (ListElem)tab.get(e);

			if (elem != null) {

				// It's already in the queue. Move it to first posision.
				if (elem != first) {

					// (first) ... (pre) -> (elm) -> (nxt)
					// :
					// (first) -> (elm) ... (pre) -> (nxt)
					ListElem pre = elem.prev;		// must be non-null.
					ListElem nxt = elem.next;		// may be null.

					nxt.prev = pre;
					pre.next = nxt;
					elem.next = first;
					elem.prev = first.prev;
					first.prev.next = elem;
					first.prev = elem;
					first = elem;
				} 
			} else {

				// It must be new entry. Insert an element.
				if (first == null) {
					first = new ListElem(e);
					first.prev = first;
					first.next = first;
					tab.put(e, first);
					size += e.getSize();
					return purged;
				} 
				Vector pp = new Vector();

				while (size > 0 && size + e.getSize() > maxSize) {
					ListElem last = first.prev;
					Entry p = last.entry;

					pp.addElement(p);
					tab.remove(p);
					size -= p.getSize();

					if (last == first) {
						first = null;
					} else {
						first.prev = last.prev;
						last.prev.next = first;
						last.entry = null;
						last.prev = null;
						last.next = null;
					} 
				} 
				if (pp.size() > 0) {
					purged = new Entry[pp.size()];
					pp.copyInto(purged);
				} 

				// (first) -> (?) ...
				// (first) -> (elm) -> (?) ...
				if (first != null) {
					elem = new ListElem(e, first.prev, first);
					first.prev.next = elem;
					first.prev = elem;
				} else {
					elem = new ListElem(e);
					elem.prev = elem;
					elem.next = elem;
				} 
				first = elem;
				tab.put(e, elem);
				size += e.getSize();
			} 
			return purged;
		} 

		synchronized Entry remove(Entry e) {
			ListElem elem = (ListElem)tab.remove(e);

			if (elem == null) {
				return null;
			} 

			// (first) -> ... (pre) -> (elm) -> (nxt)
			ListElem pre = elem.prev;
			ListElem nxt = elem.next;

			nxt.prev = pre;
			pre.next = nxt;
			if (elem == first) {
				if (elem.next == elem) {
					first = null;
				} else {
					first = first.next;
				} 
			} 

			size -= elem.entry.getSize();
			Entry entry = elem.entry;

			elem.entry = null;
			elem.prev = null;
			elem.next = null;
			return entry;
		} 
	}

	private final class Entry implements java.io.Serializable {
		String name;
		long version;
		String filename;
		byte[] data;
		int refCount;

		Entry(String n, long v, byte[] d) {
			name = n;
			version = v;
			data = d;
			refCount = 0;
			hash = name.hashCode();		// t + _hashcode(version);
		}


		int getSize() {
			return name.length() * 2 + 8 + 

			// ((filename != null) ? filename.length() * 2 : 0) +
			4 + ((data != null) ? data.length : 0);
		} 

		void delete() {
			if (filename != null) {
				try {
					final String fn = filename;

					AccessController
						.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws IOException {
							new File(fn).delete();
							return null;
						} 
					});
				} catch (Exception ex) {
					System.out.println("Failed to delete a cache entry: " 
									   + filename);
					ex.printStackTrace();
				} 
			} 
		} 

		/* synchronized */
		void fillInData() throws IOException {
			if (data != null) {
				return;
			} 

			debug("<Reading cache from disk...'" + name + "'");
			final File fFile = new File(filename);

			try {
				data = 
					(byte[])AccessController
						.doPrivileged(new PrivilegedExceptionAction() {
					public Object run() throws IOException {
						int i = 0;

						DataInputStream oi = 
							new DataInputStream(new FileInputStream(fFile));

						// may skip those..
						oi.readUTF();		// name
						long version = oi.readLong();

						byte[] dd = null;
						int len = oi.readInt();

						if (len >= 0) {
							dd = new byte[len];
							oi.readFully(dd);
						} 
						if (dd != null && dd.length == 0) {
							dd = null;
						} 
						return dd;
					} 
				});
			} catch (PrivilegedActionException ex) {
				Exception e = ex.getException();

				if (e instanceof IOException) {
					throw (IOException)e;
				} else {
					ex.printStackTrace();
				} 
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
			debugln("done>");
		} 

		public void save() throws IOException {
			if (filename != null) {
				return;
			} 

			_pool_index++;
			filename = _pool_dir + File.separator + "c@" + _pool_index;

			debug("<Saving cache for '" + name + "'... ");
			try {
				final String fFileName = filename;
				final String fName = name;
				final long fVersion = version;
				final byte[] fData = data;

				AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
					public Object run() throws IOException {
						DataOutputStream out = 
							new DataOutputStream(new FileOutputStream(fFileName));

						out.writeUTF(fName);
						out.writeLong(fVersion);
						if (fData == null) {
							out.writeInt(-1);
						} else {
							out.writeInt(fData.length);
							out.write(fData);
						} 
						out.close();
						return null;
					} 
				});
			} catch (PrivilegedActionException ex) {
				Exception e = ex.getException();

				if (e instanceof IOException) {
					throw (IOException)e;
				} 
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
			debugln("done>");
		} 

		public boolean equals(Object anObject) {
			if (this == anObject) {
				return true;
			} 
			if (anObject instanceof Entry) {

				// hash == anObject.hashCode()) {
				// byte v1[] = version;
				// byte v2[] = ((Entry)anObject).version;
				long v2 = ((Entry)anObject).version;

				return name.equals(((Entry)anObject).name) 
					   && (version == 0 || v2 == 0 || version == v2);

				// byte v1[] = version;
				// byte v2[] = ((Entry)anObject).version;
				// return name.equals( ((Entry)anObject).name ) &&
				// (v1 == null ||
				// v2 == null ||
				// MessageDigest.isEqual(v1, v2));
			} 
			return false;
		} 

		void remove() {
			if (filename == null) {
				return;
			} 
			final java.io.File f = new File(filename);

			try {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						f.delete();
						return null;
					} 
				});
			} catch (Exception ex) {
				System.out.println("CacheManager: couldn't delete a file " 
								   + filename);
			} 
		} 

		int hash;

		public int hashCode() {
			return hash;
		} 

		public String toString() {
			if (data != null) {
				return "Entry:[" + name + '.' + version + '.' + data.length 
					   + ']';
			} else {
				return "Entry:[" + name + '.' + version + '.' + null + ']';
			} 
		} 
	}

	/*
	 * ==================================================
	 * public static void main(String a[]) {
	 * CacheManager cache = new CacheManager(a[0],
	 * Long.parseLong(a[1]),
	 * Long.parseLong(a[2]));
	 * byte val_a[] = {'a'};
	 * byte val_b[] = {'b'};
	 * byte val_c[] = {'c'};
	 * byte val_d[] = {'d'};
	 * byte val_e[] = {'e'};
	 * byte val_f[] = {'f'};
	 * byte val_g[] = {'g'};
	 * byte val[];
	 * long v1 = 1;
	 * long v2 = 2;
	 * long v3 = 3;
	 * long v4 = 4;
	 * long v5 = 5;
	 * //	if (a.length == 2) {
	 * for (int i = 0; i < 5; i++) {
	 * System.out.println("---------------------");
	 * cache.putData("a", v1, val_a);
	 * cache.putData("b", v1, val_b);
	 * cache.putData("c", v1, val_c);
	 * cache.putData("d", v2, val_d);
	 * cache.putData("e", v2, val_e);
	 * cache.putData("f", v2, val_f);
	 * cache.putData("g", v3, val_g);
	 * //	    cache.close();
	 * //	} else {
	 * val = cache.getData("a", v1);
	 * if (val == null) {
	 * cache.putData("a", v1, val_a);
	 * }
	 * val = cache.getData("a", v1);
	 * if (val != null) System.out.println("*************found 'a'");
	 * val = cache.getData("b", v1);
	 * if (val != null) System.out.println("*************found 'b'");
	 * val = cache.getData("c", v1);
	 * if (val != null) System.out.println("*************found 'c'");
	 * val = cache.getData("d", v2);
	 * if (val != null) System.out.println("*************found 'd'");
	 * val = cache.getData("e", v2);
	 * if (val != null) System.out.println("*************found 'e'");
	 * val = cache.getData("f", v2);
	 * if (val != null) System.out.println("*************found 'f'");
	 * val = cache.getData("g", v3);
	 * if (val != null) System.out.println("*************found 'g'");
	 * val = cache.getData("d", v2);
	 * if (val != null) System.out.println("*************found 'd'");
	 * val = cache.getData("e", v2);
	 * if (val != null) System.out.println("*************found 'e'");
	 * //	}
	 * }
	 * System.exit(0);
	 * }
	 * ==================================================
	 */
	public CacheManager() throws IOException {
		Resource res = Resource.getResourceFor("aglets");

		try {
			String server = AgletRuntime.getAgletRuntime().getServerAddress();
			URL u = new URL(server);

			_pool_dir = res.getString("aglets.cache") + File.separator 
						+ u.getHost() + '@' + u.getPort();
		} catch (MalformedURLException ex) {
			throw new IOException("Could not get host info.");
		} 
		debugln("pool_dir = " + _pool_dir);
		final File dir = new File(_pool_dir);
		boolean exists = false;
		boolean isDir = false;

		try {
			Boolean[] bb = 
				(Boolean[])AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					Boolean[] result = new Boolean[2];

					result[0] = new Boolean(dir.exists());
					result[1] = new Boolean(dir.isDirectory());
					return result;
				} 
			});

			exists = bb[0].booleanValue();
			isDir = bb[1].booleanValue();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		if (exists && (isDir == false)) {
			throw new IOException(_pool_dir + " is not a directory.");
		} 
		if (exists == false) {
			debugln("Creating pool dir " + _pool_dir);
			if (FileUtils.ensureDirectory(_pool_dir + File.separator) 
					== false) {
				throw new IOException("Failed to create new cache directory : " 
									  + _pool_dir);
			} 
		} 
		_cache_size = res.getInteger("aglets.cache.size", 1024 * 512);
		_cache = new LinkedList(_cache_size);
		_pool = new Hashtable();
		try {
			readPool();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	public CacheManager(String dir, long size) {
		_pool_dir = dir;
		_cache_size = size;
		_cache = new LinkedList(size);
		_pool = new Hashtable();
		readPool();
	}
	void debug(String a) {

		// System.out.print(a);
	}
	void debugln(String a) {

		// System.out.println(a);
	}
	private void decRefCount(Entry e) {
		synchronized (e) {
			debugln("refCount--[" + (e.refCount - 1) + "]: " + e.toString());
			if (--e.refCount <= 0) {
				_cache.remove(e);
				_pool.remove(e);
				e.remove();
				debugln("refCount--: removed " + e.toString());
			} 
		} 
	}
	public static CacheManager getCacheManager() {
		if (_singleton == null) {
			try {
				_singleton = new CacheManager();
			} catch (IOException ex) {
				ex.printStackTrace();
				_singleton = null;
			} 
		} 
		return _singleton;
	}
	synchronized public byte[] getData(String name) {
		return getData(name, 0);
	}
	synchronized public byte[] getData(String name, long version) {
		Entry key = new Entry(name, version, null);
		Entry e = getFromCache(key);

		return ((e == null) ? null : e.data);
	}
	synchronized Entry getFromCache(Entry key) {
		Entry e = _cache.get(key);

		if (e == null) {
			e = (Entry)_pool.get(key);
			if (e == null) {
				return null;
			} 
		} 
		putIntoCache(e);
		Entry result = _cache.getFirst();

		return result;
	}
	private void incRefCount(Entry e) {
		synchronized (e) {
			e.refCount++;
			debugln("refCount++[" + e.refCount + "]: " + e.toString());
		} 
	}
	synchronized public void putData(String name, long version, byte[] data) {
		Entry e = new Entry(name, version, data);

		putIntoCache(e);
	}
	synchronized public void putData(String name, long version, byte[] data, 
									 boolean refCount) {
		Entry e = new Entry(name, version, data);

		putIntoCache(e);
		if (refCount) {
			incRefCount(_cache.getFirst());
		} 
	}
	synchronized void putIntoCache(Entry e) {
		Entry purged[] = _cache.putAtFirst(e);

		if (purged != null) {
			for (int i = purged.length - 1; i >= 0; i--) {
				debugln("memcache purge: " + purged[i].name);
				if (!_pool.contains(purged[i])) {
					try {
						purged[i].save();
						_pool.put(purged[i], purged[i]);
					} catch (IOException ex) {
						System.out
							.println("Failed to write a bytecode cache entry to disk: " 
									 + purged[i].toString());
					} 
				} 
			} 
		} 
	}
	private void readPool() {
		debugln("<readPool: " + _pool_dir);
		final File f = new File(_pool_dir);
		String[] lists = null;

		try {
			lists = 
				(String[])AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return f.list();
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		if (lists != null) {
			debugln("<Reading Pool..." + lists.length);
		} 
		for (int i = 0; lists != null && i < lists.length; i++) {
			try {
				int index = Integer.parseInt(lists[i].substring(2));

				if (_pool_index < index) {
					_pool_index = index;
				} 
			} catch (Exception ex) {

				// just skip the illegal file
				continue;
			} 
			String filename = _pool_dir + File.separator + lists[i];

			debugln("  Reading Cache file: " + filename);
			DataInputStream oi = null;

			try {
				oi = new DataInputStream(new FileInputStream(filename));
				String name = oi.readUTF();
				long version = oi.readLong();
				int available = oi.readInt();

				if (oi.available() != available) {
					System.out.println("Cache size mismatch.!");
					try {

						// remove illegal cache.!
						final String fFileName = filename;

						AccessController.doPrivileged(new PrivilegedAction() {
							public Object run() {
								new File(fFileName).delete();
								return null;
							} 
						});
					} catch (Exception ex) {
						ex.printStackTrace();
					} 
					continue;
				} 
				Entry e = new Entry(name, version, null);

				e.filename = filename;
				e.fillInData();
				putIntoCache(e);
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
			finally {
				try {
					oi.close();
				} catch (Exception ex) {}
				;
			} 
		} 
		debugln("done>");
	}
	synchronized public void releaseData(String name, long version) {
		Entry key = new Entry(name, version, null);
		Entry e = getFromCache(key);

		decRefCount(e);
	}
}
