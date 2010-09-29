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
    private static CacheManager _singleton = null;

    private long _cache_size = 0;

    String _pool_dir = "";
    int _pool_index = 0;

    private LinkedList _cache = null;
    private Hashtable _pool = null;

    // t static final int _hashcode(byte[] b) {
    // t int h = 0;
    // t for(int i=0; i<b.length; i++) {
    // t h += (h * 37) + (int)b[i];
    // t }
    // t return h;
    // t }

    private final class LinkedList {
	Hashtable tab; // (key, val) = (entry, ListElem)
	ListElem first = null;
	long size; // size in bytes.
	long maxSize; // max size in bytes.

	private final class ListElem {
	    Entry entry;
	    ListElem prev;
	    ListElem next;

	    ListElem(Entry e) {
		this.entry = e;
		this.prev = null;
		this.next = null;
	    }

	    ListElem(Entry e, ListElem p, ListElem n) {
		this.entry = e;
		this.prev = p;
		this.next = n;
	    }

	    @Override
	    public boolean equals(Object e) {
		if (e == null) {
		    return false;
		}
		if (e instanceof Entry) {
		    return this.entry.equals(e);
		} else if (e instanceof ListElem) {
		    if (this.entry == null) {
			return ((((ListElem) e)).entry == null);
		    } else {
			return this.entry.equals(((ListElem) e).entry);
		    }
		} else {
		    return false;
		}
	    }

	    @Override
	    public int hashCode() {
		if (this.entry == null) {
		    return 0;
		} else {
		    return this.entry.hashCode();
		}
	    }
	}

	LinkedList(long max) {
	    this.tab = new Hashtable();
	    this.first = null;
	    this.size = 0;
	    this.maxSize = max;
	}

	synchronized Entry getFirst() {
	    return (this.first == null) ? null : this.first.entry;
	}

	synchronized Entry get(Entry e) {
	    ListElem elem = (ListElem) this.tab.get(e);

	    if (elem == null) {
		return null;
	    } else {
		return elem.entry;
	    }
	}

	synchronized Entry[] putAtFirst(Entry e) {
	    if ((this.maxSize == 0) || (e == null)) {
		return null;
	    }
	    Entry purged[] = null;
	    ListElem elem = (ListElem) this.tab.get(e);

	    if (elem != null) {

		// It's already in the queue. Move it to first posision.
		if (elem != this.first) {

		    // (first) ... (pre) -> (elm) -> (nxt)
		    // :
		    // (first) -> (elm) ... (pre) -> (nxt)
		    ListElem pre = elem.prev; // must be non-null.
		    ListElem nxt = elem.next; // may be null.

		    nxt.prev = pre;
		    pre.next = nxt;
		    elem.next = this.first;
		    elem.prev = this.first.prev;
		    this.first.prev.next = elem;
		    this.first.prev = elem;
		    this.first = elem;
		}
	    } else {

		// It must be new entry. Insert an element.
		if (this.first == null) {
		    this.first = new ListElem(e);
		    this.first.prev = this.first;
		    this.first.next = this.first;
		    this.tab.put(e, this.first);
		    this.size += e.getSize();
		    return purged;
		}
		Vector pp = new Vector();

		while ((this.size > 0)
			&& (this.size + e.getSize() > this.maxSize)) {
		    ListElem last = this.first.prev;
		    Entry p = last.entry;

		    pp.addElement(p);
		    this.tab.remove(p);
		    this.size -= p.getSize();

		    if (last == this.first) {
			this.first = null;
		    } else {
			this.first.prev = last.prev;
			last.prev.next = this.first;
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
		if (this.first != null) {
		    elem = new ListElem(e, this.first.prev, this.first);
		    this.first.prev.next = elem;
		    this.first.prev = elem;
		} else {
		    elem = new ListElem(e);
		    elem.prev = elem;
		    elem.next = elem;
		}
		this.first = elem;
		this.tab.put(e, elem);
		this.size += e.getSize();
	    }
	    return purged;
	}

	synchronized Entry remove(Entry e) {
	    ListElem elem = (ListElem) this.tab.remove(e);

	    if (elem == null) {
		return null;
	    }

	    // (first) -> ... (pre) -> (elm) -> (nxt)
	    ListElem pre = elem.prev;
	    ListElem nxt = elem.next;

	    nxt.prev = pre;
	    pre.next = nxt;
	    if (elem == this.first) {
		if (elem.next == elem) {
		    this.first = null;
		} else {
		    this.first = this.first.next;
		}
	    }

	    this.size -= elem.entry.getSize();
	    Entry entry = elem.entry;

	    elem.entry = null;
	    elem.prev = null;
	    elem.next = null;
	    return entry;
	}
    }

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

	Entry(String n, long v, byte[] d) {
	    this.name = n;
	    this.version = v;
	    this.data = d;
	    this.refCount = 0;
	    this.hash = this.name.hashCode(); // t + _hashcode(version);
	}

	int getSize() {
	    return this.name.length() * 2 + 8 +

	    // ((filename != null) ? filename.length() * 2 : 0) +
	    4 + ((this.data != null) ? this.data.length : 0);
	}

	/* synchronized */
	void fillInData() throws IOException {
	    if (this.data != null) {
		return;
	    }

	    CacheManager.this.debug("<Reading cache from disk...'" + this.name
		    + "'");
	    final File fFile = new File(this.filename);

	    try {
		this.data = (byte[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    @Override
		    public Object run() throws IOException {
			DataInputStream oi = new DataInputStream(new FileInputStream(fFile));

			// may skip those..
			oi.readUTF(); // name
			oi.readLong();

			byte[] dd = null;
			int len = oi.readInt();

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
	    } catch (PrivilegedActionException ex) {
		Exception e = ex.getException();

		if (e instanceof IOException) {
		    throw (IOException) e;
		} else {
		    ex.printStackTrace();
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    CacheManager.this.debugln("done>");
	}

	public void save() throws IOException {
	    if (this.filename != null) {
		return;
	    }

	    CacheManager.this._pool_index++;
	    this.filename = CacheManager.this._pool_dir + File.separator + "c@"
	    + CacheManager.this._pool_index;

	    CacheManager.this.debug("<Saving cache for '" + this.name + "'... ");
	    try {
		final String fFileName = this.filename;
		final String fName = this.name;
		final long fVersion = this.version;
		final byte[] fData = this.data;

		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    @Override
		    public Object run() throws IOException {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(fFileName));

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
		    throw (IOException) e;
		}
		ex.printStackTrace();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    CacheManager.this.debugln("done>");
	}

	@Override
	public boolean equals(Object anObject) {
	    if (this == anObject) {
		return true;
	    }
	    if (anObject instanceof Entry) {

		// hash == anObject.hashCode()) {
		// byte v1[] = version;
		// byte v2[] = ((Entry)anObject).version;
		long v2 = ((Entry) anObject).version;

		return this.name.equals(((Entry) anObject).name)
		&& ((this.version == 0) || (v2 == 0) || (this.version == v2));

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
	    if (this.filename == null) {
		return;
	    }
	    final java.io.File f = new File(this.filename);

	    try {
		AccessController.doPrivileged(new PrivilegedAction() {
		    @Override
		    public Object run() {
			f.delete();
			return null;
		    }
		});
	    } catch (Exception ex) {
		System.out.println("CacheManager: couldn't delete a file "
			+ this.filename);
	    }
	}

	int hash;

	@Override
	public int hashCode() {
	    return this.hash;
	}

	@Override
	public String toString() {
	    if (this.data != null) {
		return "Entry:[" + this.name + '.' + this.version + '.'
		+ this.data.length + ']';
	    } else {
		return "Entry:[" + this.name + '.' + this.version + '.' + null
		+ ']';
	    }
	}
    }

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
	Resource res = Resource.getResourceFor("aglets");

	try {
	    String server = AgletRuntime.getAgletRuntime().getServerAddress();
	    URL u = new URL(server);

	    this._pool_dir = res.getString("aglets.cache") + File.separator
	    + u.getHost() + '@' + u.getPort();
	} catch (MalformedURLException ex) {
	    throw new IOException("Could not get host info.");
	}
	this.debugln("pool_dir = " + this._pool_dir);
	final File dir = new File(this._pool_dir);
	boolean exists = false;
	boolean isDir = false;

	try {
	    Boolean[] bb = (Boolean[]) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
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
	    throw new IOException(this._pool_dir + " is not a directory.");
	}
	if (exists == false) {
	    this.debugln("Creating pool dir " + this._pool_dir);
	    if (FileUtils.ensureDirectory(this._pool_dir + File.separator) == false) {
		throw new IOException("Failed to create new cache directory : "
			+ this._pool_dir);
	    }
	}
	this._cache_size = res.getInteger("aglets.cache.size", 1024 * 512);
	this._cache = new LinkedList(this._cache_size);
	this._pool = new Hashtable();
	try {
	    this.readPool();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public CacheManager(String dir, long size) {
	this._pool_dir = dir;
	this._cache_size = size;
	this._cache = new LinkedList(size);
	this._pool = new Hashtable();
	this.readPool();
    }

    void debug(String a) {

	// System.out.print(a);
    }

    void debugln(String a) {

	// System.out.println(a);
    }

    private void decRefCount(Entry e) {
	synchronized (e) {
	    this.debugln("refCount--[" + (e.refCount - 1) + "]: "
		    + e.toString());
	    if (--e.refCount <= 0) {
		this._cache.remove(e);
		this._pool.remove(e);
		e.remove();
		this.debugln("refCount--: removed " + e.toString());
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
	return this.getData(name, 0);
    }

    synchronized public byte[] getData(String name, long version) {
	Entry key = new Entry(name, version, null);
	Entry e = this.getFromCache(key);

	return ((e == null) ? null : e.data);
    }

    synchronized Entry getFromCache(Entry key) {
	Entry e = this._cache.get(key);

	if (e == null) {
	    e = (Entry) this._pool.get(key);
	    if (e == null) {
		return null;
	    }
	}
	this.putIntoCache(e);
	Entry result = this._cache.getFirst();

	return result;
    }

    private void incRefCount(Entry e) {
	synchronized (e) {
	    e.refCount++;
	    this.debugln("refCount++[" + e.refCount + "]: " + e.toString());
	}
    }

    synchronized public void putData(String name, long version, byte[] data) {
	Entry e = new Entry(name, version, data);

	this.putIntoCache(e);
    }

    synchronized public void putData(
                                     String name,
                                     long version,
                                     byte[] data,
                                     boolean refCount) {
	Entry e = new Entry(name, version, data);

	this.putIntoCache(e);
	if (refCount) {
	    this.incRefCount(this._cache.getFirst());
	}
    }

    synchronized void putIntoCache(Entry e) {
	Entry purged[] = this._cache.putAtFirst(e);

	if (purged != null) {
	    for (int i = purged.length - 1; i >= 0; i--) {
		this.debugln("memcache purge: " + purged[i].name);
		if (!this._pool.contains(purged[i])) {
		    try {
			purged[i].save();
			this._pool.put(purged[i], purged[i]);
		    } catch (IOException ex) {
			System.out.println("Failed to write a bytecode cache entry to disk: "
				+ purged[i].toString());
		    }
		}
	    }
	}
    }

    private void readPool() {
	this.debugln("<readPool: " + this._pool_dir);
	final File f = new File(this._pool_dir);
	String[] lists = null;

	try {
	    lists = (String[]) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    return f.list();
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	if (lists != null) {
	    this.debugln("<Reading Pool..." + lists.length);
	}
	for (int i = 0; (lists != null) && (i < lists.length); i++) {
	    try {
		int index = Integer.parseInt(lists[i].substring(2));

		if (this._pool_index < index) {
		    this._pool_index = index;
		}
	    } catch (Exception ex) {

		// just skip the illegal file
		continue;
	    }
	    String filename = this._pool_dir + File.separator + lists[i];

	    this.debugln("  Reading Cache file: " + filename);
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
			    @Override
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
		this.putIntoCache(e);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    } finally {
		try {
		    oi.close();
		} catch (Exception ex) {
		}
		;
	    }
	}
	this.debugln("done>");
    }

    synchronized public void releaseData(String name, long version) {
	Entry key = new Entry(name, version, null);
	Entry e = this.getFromCache(key);

	this.decRefCount(e);
    }
}
