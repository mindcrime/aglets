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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.Resource;

public class CacheManager {
	private final class Entry implements java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 146119643340357304L;
		String name;
		long version;
		String filename;
		byte[] data;
		int refCount;

		int hash;

		Entry(final String n, final long v, final byte[] d) {
			name = n;
			version = v;
			data = d;
			refCount = 0;
			hash = name.hashCode(); // t + _hashcode(version);
		}

		@Override
		public boolean equals(final Object anObject) {
			if (this == anObject) {
				return true;
			}
			if (anObject instanceof Entry) {

				// hash == anObject.hashCode()) {
				// byte v1[] = version;
				// byte v2[] = ((Entry)anObject).version;
				final long v2 = ((Entry) anObject).version;

				return name.equals(((Entry) anObject).name)
				&& ((version == 0) || (v2 == 0) || (version == v2));

				// byte v1[] = version;
				// byte v2[] = ((Entry)anObject).version;
				// return name.equals( ((Entry)anObject).name ) &&
				// (v1 == null ||
				// v2 == null ||
				// MessageDigest.isEqual(v1, v2));
			}
			return false;
		}

		/* synchronized */
		void fillInData() throws IOException {
			if (data != null) {
				return;
			}

			debug("<Reading cache from disk...'" + name
					+ "'");
			final File fFile = new File(filename);

			try {
				data = (byte[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
					@Override
					public Object run() throws IOException {
						final DataInputStream oi = new DataInputStream(new FileInputStream(fFile));

						// may skip those..
						oi.readUTF(); // name
						oi.readLong();

						byte[] dd = null;
						final int len = oi.readInt();

						if (len >= 0) {
							dd = new byte[len];
							oi.readFully(dd);
						}
						if ((dd != null) && (dd.length == 0)) {
							dd = null;
						}
						return dd;
					}
				});
			} catch (final PrivilegedActionException ex) {
				final Exception e = ex.getException();

				if (e instanceof IOException) {
					throw (IOException) e;
				} else {
					ex.printStackTrace();
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			debugln("done>");
		}

		int getSize() {
			return name.length() * 2 + 8 +

			// ((filename != null) ? filename.length() * 2 : 0) +
			4 + ((data != null) ? data.length : 0);
		}

		@Override
		public int hashCode() {
			return hash;
		}

		void remove() {
			if (filename == null) {
				return;
			}
			final java.io.File f = new File(filename);

			try {
				AccessController.doPrivileged(new PrivilegedAction() {
					@Override
					public Object run() {
						f.delete();
						return null;
					}
				});
			} catch (final Exception ex) {
				System.out.println("CacheManager: couldn't delete a file "
						+ filename);
			}
		}

		public void save() throws IOException {
			if (filename != null) {
				return;
			}

			_pool_index++;
			filename = _pool_dir + File.separator + "c@"
			+ _pool_index;

			debug("<Saving cache for '" + name + "'... ");
			try {
				final String fFileName = filename;
				final String fName = name;
				final long fVersion = version;
				final byte[] fData = data;

				AccessController.doPrivileged(new PrivilegedExceptionAction() {
					@Override
					public Object run() throws IOException {
						final DataOutputStream out = new DataOutputStream(new FileOutputStream(fFileName));

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
			} catch (final PrivilegedActionException ex) {
				final Exception e = ex.getException();

				if (e instanceof IOException) {
					throw (IOException) e;
				}
				ex.printStackTrace();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			debugln("done>");
		}

		@Override
		public String toString() {
			if (data != null) {
				return "Entry:[" + name + '.' + version + '.'
				+ data.length + ']';
			} else {
				return "Entry:[" + name + '.' + version + '.' + null
				+ ']';
			}
		}
	}

	private final class LinkedList {
		private final class ListElem {
			Entry entry;
			ListElem prev;
			ListElem next;

			ListElem(final Entry e) {
				entry = e;
				prev = null;
				next = null;
			}

			ListElem(final Entry e, final ListElem p, final ListElem n) {
				entry = e;
				prev = p;
				next = n;
			}

			@Override
			public boolean equals(final Object e) {
				if (e == null) {
					return false;
				}
				if (e instanceof Entry) {
					return entry.equals(e);
				} else if (e instanceof ListElem) {
					if (entry == null) {
						return ((((ListElem) e)).entry == null);
					} else {
						return entry.equals(((ListElem) e).entry);
					}
				} else {
					return false;
				}
			}

			@Override
			public int hashCode() {
				if (entry == null) {
					return 0;
				} else {
					return entry.hashCode();
				}
			}
		}
		Hashtable tab; // (key, val) = (entry, ListElem)
		ListElem first = null;
		long size; // size in bytes.

		long maxSize; // max size in bytes.

		LinkedList(final long max) {
			tab = new Hashtable();
			first = null;
			size = 0;
			maxSize = max;
		}

		synchronized Entry get(final Entry e) {
			final ListElem elem = (ListElem) tab.get(e);

			if (elem == null) {
				return null;
			} else {
				return elem.entry;
			}
		}

		synchronized Entry getFirst() {
			return (first == null) ? null : first.entry;
		}

		synchronized Entry[] putAtFirst(final Entry e) {
			if ((maxSize == 0) || (e == null)) {
				return null;
			}
			Entry purged[] = null;
			ListElem elem = (ListElem) tab.get(e);

			if (elem != null) {

				// It's already in the queue. Move it to first posision.
				if (elem != first) {

					// (first) ... (pre) -> (elm) -> (nxt)
					// :
					// (first) -> (elm) ... (pre) -> (nxt)
					final ListElem pre = elem.prev; // must be non-null.
					final ListElem nxt = elem.next; // may be null.

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
				final Vector pp = new Vector();

				while ((size > 0)
						&& (size + e.getSize() > maxSize)) {
					final ListElem last = first.prev;
					final Entry p = last.entry;

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

		synchronized Entry remove(final Entry e) {
			final ListElem elem = (ListElem) tab.remove(e);

			if (elem == null) {
				return null;
			}

			// (first) -> ... (pre) -> (elm) -> (nxt)
			final ListElem pre = elem.prev;
			final ListElem nxt = elem.next;

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
			final Entry entry = elem.entry;

			elem.entry = null;
			elem.prev = null;
			elem.next = null;
			return entry;
		}
	}

	private static CacheManager _singleton = null;
	public static CacheManager getCacheManager() {
		if (_singleton == null) {
			try {
				_singleton = new CacheManager();
			} catch (final IOException ex) {
				ex.printStackTrace();
				_singleton = null;
			}
		}
		return _singleton;
	}

	private long _cache_size = 0;
	String _pool_dir = "";

	// t static final int _hashcode(byte[] b) {
	// t int h = 0;
	// t for(int i=0; i<b.length; i++) {
	// t h += (h * 37) + (int)b[i];
	// t }
	// t return h;
	// t }

	int _pool_index = 0;

	private LinkedList _cache = null;

	private Hashtable _pool = null;

	/*
	 * ================================================== public static void
	 * main(String a[]) { CacheManager cache = new CacheManager(a[0],
	 * Long.parseLong(a[1]), Long.parseLong(a[2])); byte val_a[] = {'a'}; byte
	 * val_b[] = {'b'}; byte val_c[] = {'c'}; byte val_d[] = {'d'}; byte val_e[]
	 * = {'e'}; byte val_f[] = {'f'}; byte val_g[] = {'g'}; byte val[]; long v1
	 * = 1; long v2 = 2; long v3 = 3; long v4 = 4; long v5 = 5; // if (a.length
	 * == 2) { for (int i = 0; i < 5; i++) {
	 * System.out.println("---------------------"); cache.putData("a", v1,
	 * val_a); cache.putData("b", v1, val_b); cache.putData("c", v1, val_c);
	 * cache.putData("d", v2, val_d); cache.putData("e", v2, val_e);
	 * cache.putData("f", v2, val_f); cache.putData("g", v3, val_g); //
	 * cache.close(); // } else { val = cache.getData("a", v1); if (val == null)
	 * { cache.putData("a", v1, val_a); } val = cache.getData("a", v1); if (val
	 * != null) System.out.println("*************found 'a'"); val =
	 * cache.getData("b", v1); if (val != null)
	 * System.out.println("*************found 'b'"); val = cache.getData("c",
	 * v1); if (val != null) System.out.println("*************found 'c'"); val =
	 * cache.getData("d", v2); if (val != null)
	 * System.out.println("*************found 'd'"); val = cache.getData("e",
	 * v2); if (val != null) System.out.println("*************found 'e'"); val =
	 * cache.getData("f", v2); if (val != null)
	 * System.out.println("*************found 'f'"); val = cache.getData("g",
	 * v3); if (val != null) System.out.println("*************found 'g'"); val =
	 * cache.getData("d", v2); if (val != null)
	 * System.out.println("*************found 'd'"); val = cache.getData("e",
	 * v2); if (val != null) System.out.println("*************found 'e'"); // }
	 * } System.exit(0); } ==================================================
	 */
	public CacheManager() throws IOException {
		final Resource res = Resource.getResourceFor("aglets");

		try {
			final String server = AgletRuntime.getAgletRuntime().getServerAddress();
			final URL u = new URL(server);

			_pool_dir = res.getString("aglets.cache") + File.separator
			+ u.getHost() + '@' + u.getPort();
		} catch (final MalformedURLException ex) {
			throw new IOException("Could not get host info.");
		}
		debugln("pool_dir = " + _pool_dir);
		final File dir = new File(_pool_dir);
		boolean exists = false;
		boolean isDir = false;

		try {
			final Boolean[] bb = (Boolean[]) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					final Boolean[] result = new Boolean[2];

					result[0] = new Boolean(dir.exists());
					result[1] = new Boolean(dir.isDirectory());
					return result;
				}
			});

			exists = bb[0].booleanValue();
			isDir = bb[1].booleanValue();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		if (exists && (isDir == false)) {
			throw new IOException(_pool_dir + " is not a directory.");
		}
		if (exists == false) {
			debugln("Creating pool dir " + _pool_dir);
			if (FileUtils.ensureDirectory(_pool_dir + File.separator) == false) {
				throw new IOException("Failed to create new cache directory : "
						+ _pool_dir);
			}
		}
		_cache_size = res.getInteger("aglets.cache.size", 1024 * 512);
		_cache = new LinkedList(_cache_size);
		_pool = new Hashtable();
		try {
			readPool();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public CacheManager(final String dir, final long size) {
		_pool_dir = dir;
		_cache_size = size;
		_cache = new LinkedList(size);
		_pool = new Hashtable();
		readPool();
	}

	void debug(final String a) {

		// System.out.print(a);
	}

	void debugln(final String a) {

		// System.out.println(a);
	}

	private void decRefCount(final Entry e) {
		synchronized (e) {
			debugln("refCount--[" + (e.refCount - 1) + "]: "
					+ e.toString());
			if (--e.refCount <= 0) {
				_cache.remove(e);
				_pool.remove(e);
				e.remove();
				debugln("refCount--: removed " + e.toString());
			}
		}
	}

	synchronized public byte[] getData(final String name) {
		return this.getData(name, 0);
	}

	synchronized public byte[] getData(final String name, final long version) {
		final Entry key = new Entry(name, version, null);
		final Entry e = getFromCache(key);

		return ((e == null) ? null : e.data);
	}

	synchronized Entry getFromCache(final Entry key) {
		Entry e = _cache.get(key);

		if (e == null) {
			e = (Entry) _pool.get(key);
			if (e == null) {
				return null;
			}
		}
		putIntoCache(e);
		final Entry result = _cache.getFirst();

		return result;
	}

	private void incRefCount(final Entry e) {
		synchronized (e) {
			e.refCount++;
			debugln("refCount++[" + e.refCount + "]: " + e.toString());
		}
	}

	synchronized public void putData(final String name, final long version, final byte[] data) {
		final Entry e = new Entry(name, version, data);

		putIntoCache(e);
	}

	synchronized public void putData(
	                                 final String name,
	                                 final long version,
	                                 final byte[] data,
	                                 final boolean refCount) {
		final Entry e = new Entry(name, version, data);

		putIntoCache(e);
		if (refCount) {
			incRefCount(_cache.getFirst());
		}
	}

	synchronized void putIntoCache(final Entry e) {
		final Entry purged[] = _cache.putAtFirst(e);

		if (purged != null) {
			for (int i = purged.length - 1; i >= 0; i--) {
				debugln("memcache purge: " + purged[i].name);
				if (!_pool.contains(purged[i])) {
					try {
						purged[i].save();
						_pool.put(purged[i], purged[i]);
					} catch (final IOException ex) {
						System.out.println("Failed to write a bytecode cache entry to disk: "
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
			lists = (String[]) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return f.list();
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		if (lists != null) {
			debugln("<Reading Pool..." + lists.length);
		}
		for (int i = 0; (lists != null) && (i < lists.length); i++) {
			try {
				final int index = Integer.parseInt(lists[i].substring(2));

				if (_pool_index < index) {
					_pool_index = index;
				}
			} catch (final Exception ex) {

				// just skip the illegal file
				continue;
			}
			final String filename = _pool_dir + File.separator + lists[i];

			debugln("  Reading Cache file: " + filename);
			DataInputStream oi = null;

			try {
				oi = new DataInputStream(new FileInputStream(filename));
				final String name = oi.readUTF();
				final long version = oi.readLong();
				final int available = oi.readInt();

				if (oi.available() != available) {
					System.out.println("Cache size mismatch.!");
					try {

						// remove illegal cache.!
						final String fFileName = filename;

						AccessController.doPrivileged(new PrivilegedAction() {
							@Override
							public Object run() {
								new File(fFileName).delete();
								return null;
							}
						});
					} catch (final Exception ex) {
						ex.printStackTrace();
					}
					continue;
				}
				final Entry e = new Entry(name, version, null);

				e.filename = filename;
				e.fillInData();
				putIntoCache(e);
			} catch (final IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					oi.close();
				} catch (final Exception ex) {
				}
				;
			}
		}
		debugln("done>");
	}

	synchronized public void releaseData(final String name, final long version) {
		final Entry key = new Entry(name, version, null);
		final Entry e = getFromCache(key);

		decRefCount(e);
	}
}
