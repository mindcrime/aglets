package com.ibm.aglets.tahiti;

/*
 * @(#)AgletClassLoader.java
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

// import com.ibm.atci.AtciConstants;
import com.ibm.maf.*;

import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;

import com.ibm.aglet.Ticket;

import com.ibm.aglet.MessageManager;
import com.ibm.aglets.ResourceManager;
import com.ibm.aglets.AgletRuntime;
import com.ibm.aglets.AgletThread;

import com.ibm.awb.misc.Archive;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.Hexadecimal;

import com.ibm.maf.ClassName;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.CodeSource;
import java.security.Policy;
import java.security.Permissions;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * Class <tt>AgletClassLoader<tt> is responsible for loading classes for the aglets.
 * This class has a class loader cache as a static member and store all class
 * loader objects with keys which is the URL of the origin of classes managed
 * by the loader. In aglets bytecodes of classes are transfered with objects.
 * Therefore, there may be many version of classes whose names are same in
 * an aglet server. In aglets classes are managed based on their origin.
 * Classes which sources of their bytecodes are same are managed by a same
 * aglet loader in an aglet server. Therefore, an object can access objects
 * if sources of their class bytecodes are same, otherwise ClassCastException
 * will occur.<p>
 * 
 * An aglet loader caches classes and their bytecodes.
 * 
 * @version     1.20	$Date: 2001/07/28 06:32:36 $
 * @author      Danny B. Lange
 * @author      Gaku Yamamoto
 * @author      Mitsuru Oshima
 */
class AgletClassLoader extends ClassLoader implements ResourceManager {

	/**
	 * Cache to store bytecodes of classes.
	 */
	static protected CacheManager _cache = null;

	static AgentProfile _agent_profile = null;

	static {
		verboseOut("creating cache manager");

		// "aglets.cache" (pool_dir)
		// "aglets.cache.memsize"
		// "aglets.cache.disksize"

		_cache = CacheManager.getCacheManager();

		_agent_profile = new AgentProfile((short)1,		/* java */
		(short)1,										/* Aglets */
		"Aglets", (short)0,								/* Major */
		(short)2,										/* minor */
		(short)1,										/* serialization */
		null);
	} 

	/*
	 * CodeBase
	 */
	private URL _codeBase = null;

	/*
	 * Certificate of the owner
	 */
	private Certificate _ownerCert = null;

	/*
	 * Digest table for classes managed by this classloader.
	 */
	protected DigestTable _digest_table = new DigestTable();

	/**
	 * Cache to store resolved classes.
	 */
	private Hashtable _resolvedClassCache = new Hashtable();

	private java.util.Vector _resources = new java.util.Vector();

	/*
	 * Constructs a new AgletClassLoader with codebase.
	 * @param codebase the codebase in which the all classes are originated.
	 * @param owner Certificate of the owner
	 */
	protected AgletClassLoader(URL codebase, Certificate owner) {
		_codeBase = codebase;
		_ownerCert = owner;
	}
	public void addResource(Object o) {
		synchronized (_resources) {
			if (_resources.contains(o) == false) {
				_resources.addElement(o);
			} 
		} 
	}
	public void cacheResolvedClass(Class cls) {
		_resolvedClassCache.put(cls.getName(), cls);
	}
	/**
	 * Checks if the given class is managed by this manager.
	 */
	public boolean contains(Class cls) {
		return _resolvedClassCache.contains(cls);
	}
	public void disposeAllResources() {
		synchronized (_resources) {
			java.util.Enumeration e = _resources.elements();

			while (e.hasMoreElements()) {
				Object o = e.nextElement();

				if (o instanceof java.awt.Window) {
					((java.awt.Window)o).dispose();
				} else {

					// what's else?
				} 
			} 
			_resources = null;
		} 
	}
	private static void dumpBytes(byte[] bytes) {
		if (bytes != null) {
			for (int i = 0; i < bytes.length; i++) {
				System.out.print(Hexadecimal.valueOf(bytes[i]));
				if (i % 16 == 15) {
					System.out.println();
				} else {
					System.out.print(" ");
				} 
			} 
			if (bytes.length % 16 != 0) {
				System.out.println();
			} 
		} 
	}
	/**
	 * Shout when an AgletClassLoader object is caught by GC. This method is
	 * for verifing whether a class loader becomes a target of GC or not.
	 */
	public void finalize() {
		verboseOut("Class Loader: Garbage Collected");
		disposeAllResources();
		releaseCacheEntries();
	}
	private byte[] findByteCode(String classname) {
		return getResourceAsByteArray(classname.replace('.', '/') + ".class");
	}
	/*
	 * Digest is only good for the classes other than system classes.
	 * private byte[] digest(Class c) {
	 * return digest(c.getName());
	 * }
	 */

	/*
	 * Digest is only good for the classes other than system classes.
	 * synchronized private byte[] digest(String filename) {
	 * 
	 * byte[] digest = _digest_table.getDigest(filename);
	 * byte[] digest = _digest_table.getDigest(filename);
	 * if (digest == null) {
	 * byte[] d = findByteCodeInCache(filename);
	 * if (d == null) {
	 * return null;
	 * }
	 * digest = _digest_table.setData(filename, d);
	 * }
	 * return digest;
	 * }
	 */

	protected byte[] findByteCodeInCache(String filename) {

		// byte[] b = _digest_table.getDigest(filename);
		long d = _digest_table.getDigest(filename);

		return _cache.getData(filename, d);
	}
	/**
	 * Loads a class
	 * @see AgletClassLoader#loadClass
	 * @see AgletClassLoader#instantiageAglet
	 */
	private Class findClassInternal(String name) 
			throws ClassNotFoundException {
		Class clazz = null;

		try {
			clazz = findSystemClass(name);
			if (clazz != null) {
				verboseOut("Loading " + name + " from System");
				return clazz;
			} 
		} catch (ClassNotFoundException ex) {}

		clazz = findLoadedClass(name);
		if (clazz != null) {
			verboseOut("Using class " + name + " in cache");
			return clazz;
		} 

		clazz = loadClassFromCodeBase(name);
		if (clazz != null) {
			verboseOut("Loading class " + name + " from CodeBase");
		} 
		return clazz;
	}
	/**
	 * Gets the class specified by the name from resolved cache.
	 * 
	 * @param name the class name.
	 * @return the class of the name, null if the class is not in the cache.
	 */
	private Class findResolvedClass(String classname) {
		return (Class)_resolvedClassCache.get(classname);
	}
	public Archive getArchive(ClassName[] t) {
		int size = t.length;

		if (match(t)) {
			Archive a = new Archive();

			for (int i = 0; i < size; i++) {
				String name = t[i].name;
				byte b[] = findByteCodeInCache(name);

				if (b != null) {
					long d = _digest_table.getDigest(name);

					verboseOut("putResource(" + name + "," + d + "," 
							   + b.length + ") into archive");
					a.putResource(name, d, b);
				} 
			} 
			return a;
		} else {
			System.err.println("getArchive: doesn't match");
			return null;
		} 
	}
	/**
	 * Computes Digest
	 */
	synchronized public ClassName[] getClassNames(Class[] classes) {
		return _digest_table.getClassNames(classes);
	}
	/**
	 * Tells where the class was loaded from.
	 */
	public URL getCodeBase() {
		return _codeBase;
	}
	/**
	 * Gets certificate of the owner.
	 * (replacement of getIdentity)
	 * @return Certificate of the owner
	 */
	public Certificate getOwnerCertificate() {
		return _ownerCert;
	}
	synchronized protected byte[] getResourceAsByteArray(String filename) {
		long digest = _digest_table.getDigest(filename);
		byte data[] = null;

		if (digest != 0) {
			data = _cache.getData(filename, digest);
			verboseOut("get '" + filename + "' from cache by getData(" 
					   + filename + "," + digest + ")");
		} 
		if (data == null) {
			try {
				final String fn = filename;

				data = 
					(byte[])AccessController
						.doPrivileged(new PrivilegedAction() {
					public Object run() {
						byte[] res = loadResourceFromCodeBase(fn);

						verboseOut("get '" + fn + "' from codebase");
						return res;
					} 
				});
			} catch (Throwable t) {
				t.printStackTrace();
			} 
		} 

		// -No needed? (HT, MO)
		// -	if (data == null) {
		// -	    // get any data that matches the filename
		// -	    // code base info can be used here to specify which.
		// -	    data = _cache.getData(filename);
		// -	}

		if (data != null) {
			putResource(filename, data);
		} 
		return data;
	}
	public InputStream getResourceAsStream(String filename) {
		byte b[] = getResourceAsByteArray(filename);

		if (b != null) {
			return new java.io.ByteArrayInputStream(b);
		} 
		return null;
	}
	/**
	 * 
	 */
	public void importArchive(Archive a) {
		Archive.Entry ae[] = a.entries();

		verboseOut("importArchive()");
		for (int i = 0; i < ae.length; i++) {

			// do we need check? M.O.
			verboseOut("archive[" + i + "].name()=" + ae[i].name());
			verboseOut("archive[" + i + "].digest()=" + ae[i].digest());
			verboseOut("archive[" + i + "].data().length=" 
					   + ae[i].data().length);
			putResource(ae[i].name(), a.getResourceAsByteArray(ae[i].name()));

			/*
			 * _digest_table.setDigest(ae.name(), ae.digest());
			 * _cache.putData(ae.name(), ae.digest(), ae.data());
			 */
		} 
	}
	/**
	 * Loads a class specified by the param name. If a bytecode of the class
	 * has not been loaded, an AgletClassLoader object will load the bytecode
	 * form the codebase and define the class. The loaded bytecode and class	    * will be stored into the class data cache and the class cache
	 * respectively.
	 * 
	 * @param name the name of the desired class.
	 * @param resolve true if the class must be resolved.
	 * @return a loaded class
	 * @exception ClassNotFoundException if the class is not found.
	 */
	synchronized protected Class loadClass(String name, boolean resolve) 
			throws ClassNotFoundException {

		try {
			Class cl = findResolvedClass(name);

			if (cl != null) {
				verboseOut("Using class " + name + " in resolved cache");
				return cl;
			} 

			// REMIND: THIS IS NOT CORRECT (M.Oshima)
			// Needs to check in the context of loading aglet if
			// necessary.
			// AccessController.checkPermission(new RuntimePermission("package.access." + name));
			cl = findClassInternal(name);
			if (cl == null) {
				throw new ClassNotFoundException(name);
			} 

			String realName = cl.getName();

			if (!realName.equals(name)) {
				throw new ClassNotFoundException(name);
			} 

			if (resolve) {
				if (_resolvedClassCache.contains(cl)) {
					verboseOut(name + " was resolved before.");
					return cl;
				} else {
					verboseOut("resolving.. " + name);
				} 
				boolean success = false;

				try {
					resolveClass(cl);
					success = true;
				} catch (Exception ex) {

					// _archive.removeResource(name);
					throw new ClassNotFoundException("Resolve class: " 
													 + ex.toString());
				} 
				finally {
					if (success) {
						cacheResolvedClass(cl);
					} else {

						// _archive.removeResource(name);
					} 
				} 
			} 
			return cl;
		} catch (SecurityException e) {
			e.printStackTrace();
			Thread.dumpStack();
			throw e;
		} 
	}
	private Class loadClassFromCodeBase(String classname) 
			throws ClassNotFoundException {
		verboseOut("loadClassFromCodeBase(" + classname + ")");
		byte[] bytecode = findByteCode(classname);

		if (bytecode == null) {
			throw new ClassNotFoundException(classname);
		} 
		verboseOut("findByteCode(" + classname + ") returns bytecode (" 
				   + bytecode.length + "bytes)");

		// if(AgletRuntime.isVerbose()) {
		// dumpBytes(bytecode);
		// }

		try {
			verboseOut("define class " + classname);
			Object[] signers = null;
			Certificate[] certs = null;

			if (_ownerCert != null) {
				certs = new Certificate[1];
				certs[0] = _ownerCert;
			} 
			final CodeSource cs = new CodeSource(_codeBase, certs);

			// Permissions perms = new Permissions();
			// perms.add(new AllPermission()); // ???????(To be implemented)
			ProtectionDomain pd = 
				(ProtectionDomain)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					Policy policy = Policy.getPolicy();
					PermissionCollection perms = policy.getPermissions(cs);

					return new ProtectionDomain(cs, perms);
				} 
			});
			Class clazz = defineClass(classname, bytecode, 0, 
									  bytecode.length, pd);

			if (clazz.getName().equals(classname) == false) {

				// if the name is not same as the name of the loaded class

				/*
				 * _archive.removeResource(name);
				 */
				throw new ClassNotFoundException(classname);
			} 
			return clazz;
		} catch (ClassFormatError e) {
			e.printStackTrace();
			System.err.println("When loading " + classname + " from " 
							   + _codeBase + " : " + e.getClass().getName() 
							   + e.getMessage());
			throw new ClassNotFoundException("When loading " + classname 
											 + " from " + _codeBase + " : " 
											 + e.getClass().getName() 
											 + e.getMessage());
		} 
	}
	/**
	 * The method for loading class data. This loads the bytecode from
	 * codebase of this loader.
	 * 
	 * @param name the class name.
	 * @return the bytecode for the class.
	 * @exception IOException if loading the class data fails.
	 */
	private byte[] loadResourceFromCodeBase(String name) {

		/*
		 * try {
		 * Ticket t = new Ticket(_codeBase);
		 * MAFAgentSystem maf = MAFAgentSystem.getMAFAgentSystem(t);
		 * 
		 * ClassName[] list = new ClassName[1];
		 * list[0] = new ClassName(name, null);
		 * 
		 * byte[][] classes = maf.fetch_class(list, _codeBase.toString(),
		 * _agent_profile);
		 * return classes[0];
		 * } catch (UnknownHostException ex) {
		 * ex.printStackTrace();
		 * return null;
		 * 
		 * } catch (ClassUnknown ex) {
		 * ex.printStackTrace();
		 * return null;
		 * 
		 * } catch (MAFExtendedException ex) {
		 * ex.printStackTrace();
		 * return null;
		 * 
		 * }
		 */
		byte[] bytecode;
		InputStream is = null;

		try {
			URL url = new URL(_codeBase, name);
			int content_length = -1;

			// fetch
			URLConnection connection = url.openConnection();

			connection.setRequestProperty("user-agent", "Aglets/1.1");
			connection.setRequestProperty("agent-system", "aglets");
			connection.setRequestProperty("agent-language", "java");
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.connect();

			// connection.sendRequest();
			is = connection.getInputStream();
			content_length = connection.getContentLength();

			if (content_length < 0) {
				content_length = is.available();
			} 
			if (content_length == 0) {
				return null;
			} 
			bytecode = new byte[content_length];

			int offset = 0;

			while (content_length > 0) {
				int read = is.read(bytecode, offset, content_length);

				offset += read;
				content_length -= read;
			} 
			is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} 
		finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ex) {}
			} 
		} 
		return bytecode;
	}
	/*
	 * synchronized public DigestTable getDigestTable(Class[] classes) {
	 * DigestTable r = new DigestTable(classes.length);
	 * for(int i=0; i<classes.length; i++) {
	 * String filename = classes[i].getName().replace('.','/') + ".class";
	 * //	    byte[] v = _digest_table.getDigest(filename);
	 * //	    if ( v != null ) {
	 * long v = _digest_table.getDigest(filename);
	 * if ( v != 0 ) {
	 * r.setDigest(filename, v);
	 * }
	 * }
	 * return r;
	 * }
	 */

	synchronized public boolean match(ClassName[] table) {
		return _digest_table.match(table, false);
	}
	synchronized public boolean matchAndImport(ClassName[] table) {
		return _digest_table.match(table, true);
	}
	public AgletThread newAgletThread(MessageManager mm) {
		return null;
	}
	private void putResource(String name, byte[] data) {

		// byte[] digest = _digest_table.setData(name, value);
		long digest = _digest_table.getDigest(name);

		if (digest == 0) {
			digest = _digest_table.setData(name, data);
			verboseOut("digest of " + name + " = " + digest);
			_cache.putData(name, digest, data, true);
		} else {
			_cache.putData(name, digest, data, false);
		} 
	}
	private void releaseCacheEntries() {
		synchronized (_digest_table) {
			for (int i = 0; i < _digest_table.size(); i++) {
				String name = _digest_table.getName(i);
				long digest = _digest_table.getDigest(i);

				_cache.releaseData(name, digest);
			} 
		} 
	}
	public void resumeAllThreads() {}
	public void setResourceManagerContext() {}
	public void stopAllThreads() {}
	public void stopThreadGroup() {}
	public void suspendAllThreads() {}
	public String toString() {
		String cb = null;

		if (_codeBase == null) {
			cb = "NOWHERE";
		} else {
			cb = _codeBase.toString();
		} 
		String owner = null;

		if (_ownerCert == null) {
			owner = "NOBODY";
		} else {
			owner = _ownerCert.toString();
		} 
		return "[AgletClassLoader codebase = " + cb + " owner = " + owner 
			   + "]";
	}
	public void unsetResourceManagerContext() {}
	private static void verboseOut(String msg) {
		AgletRuntime.verboseOut(msg);
	}
}
