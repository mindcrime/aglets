package com.ibm.aglets.tahiti;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Hashtable;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.message.MessageManager;
import com.ibm.aglets.ResourceManager;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.awb.misc.Archive;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.ClassName;

/**
 * Class <tt>AgletClassLoader<tt> is responsible for loading classes for the
 *  aglets. This class has a class loader cache as a static member and store all
 *  class loader objects with keys which is the URL of the origin of classes
 *  managed by the loader. In aglets bytecodes of classes are transfered with
 *  objects. Therefore, there may be many version of classes whose names are
 *  same in an aglet server. In aglets classes are managed based on their
 *  origin. Classes which sources of their bytecodes are same are managed by a
 *  same aglet loader in an aglet server. Therefore, an object can access
 *  objects if sources of their class bytecodes are same, otherwise
 *  ClassCastException will occur.<p>
 * 
 *  An aglet loader caches classes and their bytecodes.
 * 
 * @author Danny B. Lange
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 */
class AgletClassLoader extends ClassLoader implements ResourceManager {

	/**
	 * Cache to store bytecodes of classes.
	 * 
	 * @since
	 */
	protected static CacheManager _cache = null;

	static AgentProfile _agent_profile = null;

	private static AgletsLogger logger = AgletsLogger.getLogger(AgletClassLoader.class.getName());

	/**
	 * Digest table for classes managed by this classloader.
	 */
	protected DigestTable _digest_table = new DigestTable();

	/*
	 * CodeBase
	 */
	private URL _codeBase = null;

	/*
	 * Certificate of the owner
	 */
	private Certificate _ownerCert = null;

	/**
	 * Cache to store resolved classes.
	 */
	private final Hashtable _resolvedClassCache = new Hashtable();

	private java.util.Vector _resources = new java.util.Vector();

	static {
		_cache = CacheManager.getCacheManager();

		_agent_profile = new AgentProfile((short) 1,
				/*
				 * java
				 */
				(short) 1,
				/*
				 * Aglets
				 */
				"Aglets", (short) 0,
				/*
				 * Major
				 */
				(short) 2,
				/*
				 * minor
				 */
				(short) 1,
				/*
				 * serialization
				 */
				null);
	}

	/**
	 * Constructs a new AgletClassLoader with codebase.
	 * 
	 * @param codebase
	 *            the codebase in which the all classes are originated.
	 * @param owner
	 *            Certificate of the owner
	 */
	protected AgletClassLoader(final URL codebase, final Certificate owner) {
		logger.debug("Ctor: [" + codebase + "]");
		_codeBase = codebase;
		_ownerCert = owner;
	}

	/**
	 * Adds a feature to the Resource attribute of the AgletClassLoader object
	 * 
	 * @param o
	 *            The feature to be added to the Resource attribute
	 * @since
	 */
	@Override
	public void addResource(final Object o) {
		logger.debug("Adding resource.");
		synchronized (_resources) {
			if (_resources.contains(o) == false) {
				_resources.addElement(o);
			}
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param cls
	 *            Description of Parameter
	 * @since
	 */
	public void cacheResolvedClass(final Class cls) {
		_resolvedClassCache.put(cls.getName(), cls);
	}

	/**
	 * Checks if the given class is managed by this manager.
	 * 
	 * @param cls
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since
	 */
	@Override
	public boolean contains(final Class cls) {
		return _resolvedClassCache.contains(cls);
	}

	/**
	 * Description of the Method
	 * 
	 * @since
	 */
	@Override
	public void disposeAllResources() {
		synchronized (_resources) {
			final java.util.Enumeration e = _resources.elements();

			while (e.hasMoreElements()) {
				final Object o = e.nextElement();

				if (o instanceof java.awt.Window) {
					((java.awt.Window) o).dispose();
				} else {

					// what's else?
				}
			}
			_resources = null;
		}
	}

	/**
	 * Shout when an AgletClassLoader object is caught by GC. This method is for
	 * verifing whether a class loader becomes a target of GC or not.
	 * 
	 * @since
	 */
	@Override
	public void finalize() {
		logger.debug("Class Loader: Garbage Collected");
		disposeAllResources();
		releaseCacheEntries();
	}

	/**
	 * Description of the Method
	 * 
	 * @param classname
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since
	 */
	private byte[] findByteCode(final String classname) {
		return getResourceAsByteArray(classname.replace('.', '/')
				+ ".class");
	}

	/*
	 * Digest is only good for the classes other than system classes. private
	 * byte[] digest(Class c) { return digest(c.getName()); }
	 */
	/*
	 * Digest is only good for the classes other than system classes.
	 * synchronized private byte[] digest(String filename) {
	 * 
	 * byte[] digest = _digest_table.getDigest(filename); byte[] digest =
	 * _digest_table.getDigest(filename); if (digest == null) { byte[] d =
	 * findByteCodeInCache(filename); if (d == null) { return null; } digest =
	 * _digest_table.setData(filename, d); } return digest; }
	 */
	/**
	 * Description of the Method
	 * 
	 * @param filename
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since
	 */
	protected byte[] findByteCodeInCache(final String filename) {

		// byte[] b = _digest_table.getDigest(filename);
		final long d = _digest_table.getDigest(filename);

		return _cache.getData(filename, d);
	}

	/**
	 * Loads a class
	 * 
	 * @param name
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @exception ClassNotFoundException
	 *                Description of Exception
	 * @since
	 * @see AgletClassLoader#loadClass
	 */
	private Class findClassInternal(final String name) throws ClassNotFoundException {
		Class clazz = null;

		try {
			clazz = findSystemClass(name);
			if (clazz != null) {
				logger.debug("Loading " + name + " from System");
				return clazz;
			}
		} catch (final ClassNotFoundException ex) {
		}

		clazz = findLoadedClass(name);
		if (clazz != null) {
			logger.debug("Using class " + name + " in cache");
			return clazz;
		}

		clazz = loadClassFromCodeBase(name);
		if (clazz != null) {
			logger.debug("Loading class " + name + " from CodeBase");
		}
		return clazz;
	}

	/**
	 * Gets the class specified by the name from resolved cache.
	 * 
	 * @param classname
	 *            Description of Parameter
	 * @return the class of the name, null if the class is not in the cache.
	 * @since
	 */
	private Class findResolvedClass(final String classname) {
		return (Class) _resolvedClassCache.get(classname);
	}

	/**
	 * Gets the archive attribute of the AgletClassLoader object
	 * 
	 * @param t
	 *            Description of Parameter
	 * @return The archive value
	 * @since
	 */
	@Override
	public Archive getArchive(final ClassName[] t) {
		final int size = t.length;

		if (match(t)) {
			final Archive a = new Archive();

			for (int i = 0; i < size; i++) {
				final String name = t[i].name;
				final byte b[] = findByteCodeInCache(name);

				if (b != null) {
					final long d = _digest_table.getDigest(name);

					logger.debug("putResource(" + name + "," + d + ","
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
	 * 
	 * @param classes
	 *            Description of Parameter
	 * @return The classNames value
	 * @since
	 */
	@Override
	public synchronized ClassName[] getClassNames(final Class[] classes) {
		return _digest_table.getClassNames(classes);
	}

	/**
	 * Tells where the class was loaded from.
	 * 
	 * @return The codeBase value
	 * @since
	 */
	public URL getCodeBase() {
		return _codeBase;
	}

	/**
	 * Gets certificate of the owner. (replacement of getIdentity)
	 * 
	 * @return Certificate of the owner
	 * @since
	 */
	public Certificate getOwnerCertificate() {
		return _ownerCert;
	}

	/**
	 * Gets the resourceAsByteArray attribute of the AgletClassLoader object
	 * 
	 * @param filename
	 *            Description of Parameter
	 * @return The resourceAsByteArray value
	 * @since
	 */
	protected synchronized byte[] getResourceAsByteArray(final String filename) {
		final long digest = _digest_table.getDigest(filename);
		byte data[] = null;

		if (digest != 0) {
			data = _cache.getData(filename, digest);
			logger.debug("get '" + filename + "' from cache by getData("
					+ filename + "," + digest + ")");
		}
		if (data == null) {
			try {
				final String fn = filename;

				data = (byte[]) AccessController.doPrivileged(new PrivilegedAction() {
					@Override
					public Object run() {
						logger.debug("get '" + fn + "' from codebase");
						final byte[] res = AgletClassLoader.this.loadResourceFromCodeBase(fn);
						return res;
					}
				});
			} catch (final Throwable t) {
				logger.error("Error getting resource: " + t.getMessage());
			}
		}

		// -No needed? (HT, MO)
		// - if (data == null) {
		// - // get any data that matches the filename
		// - // code base info can be used here to specify which.
		// - data = _cache.getData(filename);
		// - }

		if (data != null) {
			putResource(filename, data);
		}
		return data;
	}

	/**
	 * Gets the resourceAsStream attribute of the AgletClassLoader object
	 * 
	 * @param filename
	 *            Description of Parameter
	 * @return The resourceAsStream value
	 * @since
	 */
	@Override
	public InputStream getResourceAsStream(final String filename) {
		final byte b[] = getResourceAsByteArray(filename);

		if (b != null) {
			return new java.io.ByteArrayInputStream(b);
		}
		return null;
	}

	/**
	 * Description of method.
	 * 
	 * @param a
	 *            Description of Parameter
	 * @since
	 */
	@Override
	public void importArchive(final Archive a) {
		final Archive.Entry ae[] = a.entries();

		logger.debug("importArchive()");
		for (int i = 0; i < ae.length; i++) {

			// do we need check? M.O.
			logger.debug("archive[" + i + "].name()=" + ae[i].name());
			logger.debug("archive[" + i + "].digest()=" + ae[i].digest());
			logger.debug("archive[" + i + "].data().length="
					+ ae[i].data().length);
			putResource(ae[i].name(), a.getResourceAsByteArray(ae[i].name()));

			/*
			 * _digest_table.setDigest(ae.name(), ae.digest());
			 * _cache.putData(ae.name(), ae.digest(), ae.data());
			 */
		}
	}

	/**
	 * Loads a class specified by the param name. If a bytecode of the class has
	 * not been loaded, an AgletClassLoader object will load the bytecode form
	 * the codebase and define the class. The loaded bytecode and class will be
	 * stored into the class data cache and the class cache respectively.
	 * 
	 * @param name
	 *            the name of the desired class.
	 * @param resolve
	 *            true if the class must be resolved.
	 * @return a loaded class
	 * @exception ClassNotFoundException
	 *                if the class is not found.
	 * @since
	 */
	@Override
	protected synchronized Class loadClass(final String name, final boolean resolve)
	throws ClassNotFoundException {
		logger.debug("loadClass()++ [" + resolve + "]");
		try {
			Class cl = findResolvedClass(name);

			if (cl != null) {
				logger.debug("Using class " + name + " in resolved cache");
				return cl;
			}

			// REMIND: THIS IS NOT CORRECT (M.Oshima)
			// Needs to check in the context of loading aglet if
			// necessary.
			// AccessController.checkPermission(new
			// RuntimePermission("package.access." + name));
			cl = findClassInternal(name);
			if (cl == null) {
				throw new ClassNotFoundException(name);
			}

			final String realName = cl.getName();

			if (!realName.equals(name)) {
				throw new ClassNotFoundException(name);
			}

			if (resolve) {
				if (_resolvedClassCache.contains(cl)) {
					logger.debug(name + " was resolved before.");
					return cl;
				} else {
					logger.debug("resolving.. " + name);
				}
				boolean success = false;

				try {
					resolveClass(cl);
					success = true;
				} catch (final Exception ex) {

					// _archive.removeResource(name);
					throw new ClassNotFoundException("Resolve class: "
							+ ex.toString());
				} finally {
					if (success) {
						cacheResolvedClass(cl);
					} else {

						// _archive.removeResource(name);
					}
				}
			}
			return cl;
		} catch (final SecurityException e) {
			e.printStackTrace();
			Thread.dumpStack();
			throw e;
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param classname
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @exception ClassNotFoundException
	 *                Description of Exception
	 * @since
	 */
	private Class loadClassFromCodeBase(final String classname)
	throws ClassNotFoundException {
		logger.debug("loadClassFromCodeBase(" + classname + ")");
		final byte[] bytecode = findByteCode(classname);

		if (bytecode == null) {
			throw new ClassNotFoundException(classname);
		}
		logger.debug("findByteCode(" + classname + ") returns bytecode ("
				+ bytecode.length + "bytes)");

		// if(AgletRuntime.isVerbose()) {
		// dumpBytes(bytecode);
		// }

		try {
			logger.debug("define class " + classname);
			Certificate[] certs = null;

			if (_ownerCert != null) {
				certs = new Certificate[1];
				certs[0] = _ownerCert;
			}
			final CodeSource cs = new CodeSource(_codeBase, certs);

			// Permissions perms = new Permissions();
			// perms.add(new AllPermission()); // ???????(To be implemented)
			final ProtectionDomain pd = (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					final Policy policy = Policy.getPolicy();
					final PermissionCollection perms = policy.getPermissions(cs);

					return new ProtectionDomain(cs, perms);
				}
			});
			final Class clazz = this.defineClass(classname, bytecode, 0, bytecode.length, pd);

			if (clazz.getName().equals(classname) == false) {

				// if the name is not same as the name of the loaded class

				/*
				 * _archive.removeResource(name);
				 */
				throw new ClassNotFoundException(classname);
			}

			// <RAB> 02142002
			cacheResolvedClass(clazz);
			// </rab>

			return clazz;
		} catch (final ClassFormatError e) {
			e.printStackTrace();
			System.err.println("When loading " + classname + " from "
					+ _codeBase + " : " + e.getClass().getName()
					+ e.getMessage());
			throw new ClassNotFoundException("When loading " + classname
					+ " from " + _codeBase + " : "
					+ e.getClass().getName() + e.getMessage());
		}
	}

	/**
	 * The method for loading class data. This loads the bytecode from codebase
	 * of this loader.
	 * 
	 * @param name
	 *            the class name.
	 * @return the bytecode for the class.
	 * @since
	 */
	private byte[] loadResourceFromCodeBase(final String name) {

		/*
		 * try { Ticket t = new Ticket(_codeBase); MAFAgentSystem maf =
		 * MAFAgentSystem.getMAFAgentSystem(t);
		 * 
		 * ClassName[] list = new ClassName[1]; list[0] = new ClassName(name,
		 * null);
		 * 
		 * byte[][] classes = maf.fetch_class(list, _codeBase.toString(),
		 * _agent_profile); return classes[0]; } catch (UnknownHostException ex)
		 * { ex.printStackTrace(); return null;
		 * 
		 * } catch (ClassUnknown ex) { ex.printStackTrace(); return null;
		 * 
		 * } catch (MAFExtendedException ex) { ex.printStackTrace(); return
		 * null;
		 * 
		 * }
		 */
		byte[] bytecode;
		InputStream is = null;

		logger.debug("LoadResourceFromCodeBase()++");
		try {
			final URL url = new URL(_codeBase, name);
			int content_length = -1;

			// fetch
			final URLConnection connection = url.openConnection();

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
				final int read = is.read(bytecode, offset, content_length);

				offset += read;
				content_length -= read;
			}
			is.close();
		} catch (final IOException ex) {
			logger.debug("Could not load resource [" + name + "] from ["
					+ _codeBase + "]");
			bytecode = null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final Exception ex) {
					logger.error("Error closing.", ex);
				}
			}
		}
		logger.debug("LoadResourceFromCodeBase()--");
		return bytecode;
	}

	/*
	 * synchronized public DigestTable getDigestTable(Class[] classes) {
	 * DigestTable r = new DigestTable(classes.length); for(int i=0;
	 * i<classes.length; i++) { String filename =
	 * classes[i].getName().replace('.','/') + ".class"; // byte[] v =
	 * _digest_table.getDigest(filename); // if ( v != null ) { long v =
	 * _digest_table.getDigest(filename); if ( v != 0 ) { r.setDigest(filename,
	 * v); } } return r; }
	 */
	/**
	 * Description of the Method
	 * 
	 * @param table
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since
	 */
	public synchronized boolean match(final ClassName[] table) {
		return _digest_table.match(table, false);
	}

	/**
	 * Description of the Method
	 * 
	 * @param table
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since
	 */
	public synchronized boolean matchAndImport(final ClassName[] table) {
		return _digest_table.match(table, true);
	}

	/**
	 * Description of the Method
	 * 
	 * @param mm
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since
	 */
	@Override
	public AgletThread newAgletThread(final MessageManager mm) {
		return null;
	}

	/**
	 * Description of the Method
	 * 
	 * @param name
	 *            Description of Parameter
	 * @param data
	 *            Description of Parameter
	 * @since
	 */
	private void putResource(final String name, final byte[] data) {

		// byte[] digest = _digest_table.setData(name, value);
		long digest = _digest_table.getDigest(name);

		if (digest == 0) {
			digest = _digest_table.setData(name, data);
			logger.debug("digest of " + name + " = " + digest);
			_cache.putData(name, digest, data, true);
		} else {
			_cache.putData(name, digest, data, false);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @since
	 */
	private void releaseCacheEntries() {
		synchronized (_digest_table) {
			for (int i = 0; i < _digest_table.size(); i++) {
				final String name = _digest_table.getName(i);
				final long digest = _digest_table.getDigest(i);

				_cache.releaseData(name, digest);
			}
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @since
	 */
	@Override
	public void resumeAllThreads() {
	}

	/**
	 * Sets the resourceManagerContext attribute of the AgletClassLoader object
	 * 
	 * @since
	 */
	@Override
	public void setResourceManagerContext() {
	}

	/**
	 * Description of the Method
	 * 
	 * @since
	 */
	@Override
	public void stopAllThreads() {
	}

	/**
	 * Description of the Method
	 * 
	 * @since
	 */
	@Override
	public void stopThreadGroup() {
	}

	/**
	 * Description of the Method
	 * 
	 * @since
	 */
	@Override
	public void suspendAllThreads() {
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Returned Value
	 * @since
	 */
	@Override
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
		return "[AgletClassLoader codebase = " + cb + " owner = " + owner + "]";
	}

	/**
	 * Description of the Method
	 * 
	 * @since
	 */
	@Override
	public void unsetResourceManagerContext() {
	}
}
