package com.ibm.aglets.tahiti;

/*
 * @(#)ResourceManagerFactory.java
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

import java.net.URL;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.security.Identity;
import java.security.CodeSource;
import java.security.cert.Certificate;
import com.ibm.aglet.MessageManager;
import com.ibm.aglets.ResourceManager;
import com.ibm.aglets.AgletRuntime;
import com.ibm.aglets.AgletThread;
import com.ibm.awb.misc.*;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.ClassName;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import java.security.Policy;
import com.ibm.aglets.security.PolicyImpl;

/**
 * @version     1.10	$Date: 2001/07/28 06:32:50 $
 * @author      Mitsuru Oshima
 */
final class ResourceManagerFactory 
	implements com.ibm.aglets.ResourceManagerFactory {

	/*
	 * Just to load classes in the app domain.
	 */
	final class AppResourceManager implements ResourceManager {

		// private ClassLoader _loader;

		AppResourceManager() {

			// _loader = loader;
		} 

		public Class loadClass(String name) throws ClassNotFoundException {
			return Class.forName(name);
		} 

		public boolean contains(Class cls) {
			return cls != null && cls.getClassLoader() == null;
		} 

		public Archive getArchive(ClassName[] table) {
			return null;
		} 
		public void importArchive(Archive a) {}
		public ClassName[] getClassNames(Class[] classes) {
			return null;
		} 

		public void addResource(Object obj) {}
		public void disposeAllResources() {}
		public AgletThread newAgletThread(MessageManager mm) {
			return null;
		} 
		public void stopAllThreads() {}
		public void stopThreadGroup() {}
		public void suspendAllThreads() {}
		public void resumeAllThreads() {}
		public void setResourceManagerContext() {}
		public void unsetResourceManagerContext() {}
	}

	static private String _agletsClassPath[];
	static private Hashtable _manifests;
	static private String _publicRoot;
	static private String _localAddr;

	static Class exportedClass[];

	static String[] exportedClassName = {
		"com.ibm.aglets.AgletProxyImpl", "com.ibm.aglets.DeactivationInfo", 
		"com.ibm.aglets.MessageImpl", "com.ibm.aglets.MessageManagerImpl", 
		"com.ibm.aglets.SystemMessage", "com.ibm.aglets.AgletImageData", 
		"com.ibm.aglets.AgletAudioClip", 
		"com.ibm.aglets.ByteArrayImageSource", "com.ibm.awb.misc.Resource", 
		"com.ibm.awb.weakref.VirtualRef", 
	};

	static {
		Resource res = Resource.getResourceFor("aglets");

		_agletsClassPath = res.getStringArray("aglets.class.path", 
											  File.pathSeparator);

		_publicRoot = res.getString("aglets.public.root", null);

		if (_publicRoot != null) {
			try {
				_publicRoot = getCanonicalDirectory(_publicRoot);
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		} 

		_localAddr = MAFAgentSystem.getLocalMAFAgentSystem().getAddress();
		Policy pol = Policy.getPolicy();

		if (pol instanceof PolicyImpl) {
			PolicyImpl policy = (PolicyImpl)pol;

			policy.setSystemCodeBase(_localAddr);
			policy.setPublicRoot(_publicRoot);
		} 

		// 
		// Search jar/zip files.
		// 
		_manifests = new Hashtable();

		verboseOut("AgletsClassPath: ==========");

		for (int i = 0; i < _agletsClassPath.length; i++) {
			lookupJarFiles(_agletsClassPath[i], true);
			verboseOut(" " + _agletsClassPath[i]);
		} 

		verboseOut("                 ==========");

		ClassLoader loader = ResourceManagerFactory.class.getClassLoader();

		exportedClass = new Class[exportedClassName.length];
		for (int i = 0; i < exportedClassName.length; i++) {
			try {
				if (loader == null) {
					exportedClass[i] = Class.forName(exportedClassName[i]);
				} else {
					exportedClass[i] = loader.loadClass(exportedClassName[i]);
				} 
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} 
		} 
	} 

	private ResourceManager _appResourceManager;
	private Hashtable _map = new Hashtable();

	public ResourceManagerFactory() {

		// - 	ClassLoader loader = this.getClass().getClassLoader();
		// tentative
		// ClassLoader loader = SecureClassLoader.getSecureClassLoader();
		_appResourceManager = new AppResourceManager();
	}
	/**
	 * 
	 */
	synchronized public void clearCache() {
		_map = new Hashtable();
	}
	/**
	 * 
	 */
	synchronized public void clearCache(URL codebase, Certificate owner) {
		if (codebase == null) {
			clearCache();
		} else {
			CodeSource cs = new CodeSource(codebase, new Certificate[] {
				owner
			});

			_map.remove(cs);
		} 
	}
	private AgletClassLoader createClassLoader(URL codeBase, 
											   Certificate owner) {
		String oo = null;

		if (owner != null) {
			oo = 
				((java.security.cert.X509Certificate)owner).getSubjectDN()
					.getName();
		} 
		verboseOut("creating AgletClassLoader: for " + codeBase + " : " + oo);

		// do not forget the scope!
		Certificate[] owners;

		if (owner != null) {
			owners = new Certificate[] {
				owner
			};
		} else {
			owners = new Certificate[0];
		} 
		CodeSource cs = new CodeSource(codeBase, owners);
		Vector v = (Vector)_map.get(cs);

		if (v == null) {
			v = new Vector();
			_map.put(cs, v);
		} 
		AgletClassLoader loader = null;

		try {
			if (JarAgletClassLoader.isJarFile(codeBase)) {
				loader = new JarAgletClassLoader(codeBase, owner);
			} else {
				String f = codeBase.getFile();

				if (f != null && f.endsWith("/") == false) {
					codeBase = new URL(codeBase, codeBase.getFile() + "/");
				} 
				loader = new AgletClassLoader(codeBase, owner);
				for (int i = 0; i < exportedClass.length; i++) {
					loader.cacheResolvedClass(exportedClass[i]);
				} 
			} 
			v.insertElementAt(loader, 0);
		} catch (IOException ex) {
			ex.printStackTrace();
			loader = null;
		} 
		return loader;
	}
	/*
	 * 
	 */
	synchronized public ResourceManager createResourceManager(URL codebase, 
			Certificate owner, ClassName[] t) {
		AgletClassLoader loader = getClassLoaderInCache(codebase, owner, t);

		if (loader == null) {
			System.out.println("creating loader");
			loader = createClassLoader(codebase, owner);
		} 

		// /????? new ResourceManagerImpl(loader);
		String ownerName = null;

		if (owner != null) {
			ownerName = 
				((java.security.cert.X509Certificate)owner).getSubjectDN()
					.getName();
		} 
		return new ResourceManagerImpl(loader, ownerName);
	}
	static private String getCanonicalDirectory(String path) 
			throws IOException {
		final File file = new File(path);

		try {
			return (String)AccessController
				.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws IOException {
					String cp = file.getCanonicalPath();

					if (!(file.isAbsolute())) {

						// due to the URL formatting bugs
						cp = File.separatorChar + cp;
					} 
					if (cp.charAt(cp.length() - 1) != File.separatorChar) {
						cp = cp + File.separatorChar;
					} 
					return cp;
				} 
			});
		} catch (PrivilegedActionException ex) {
			throw (IOException)ex.getException();
		} 
	}
	private AgletClassLoader getClassLoaderInCache(URL codebase, 
			Certificate owner, ClassName[] table) {

		//System.out.println(codebase);
		//System.out.println(owner.toString());

		Certificate[] owners;

		if (owner != null) {
			owners = new Certificate[] {
				owner
			};
		} else {
			owners = new Certificate[0];
		} 
		CodeSource cs = new CodeSource(codebase, owners);
		Vector v = (Vector)_map.get(cs);

		if (table == null & JarAgletClassLoader.isJarFile(codebase)) {
			try {
				final URL fCodebase = codebase;
				ClassName[] tmpTab = (ClassName[])AccessController.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws IOException {
							java.io.InputStream in = fCodebase.openStream();
							JarArchive jar = new JarArchive(in);

							// table = jar.getClassNames();
							Archive.Entry ae[] = jar.entries();

							ClassName[] tab = new ClassName[ae.length];
							for (int i = 0; i < ae.length; i++) {
								tab[i] = new ClassName(ae[i].name(), 
									  DigestTable.toByteArray(ae[i].digest()));
							} 

							// tab = jar.getDigestTable();
							in.close();
							return tab; // Nothing to retrun.
						}
					}
				);
				table = new ClassName[tmpTab.length];
				System.arraycopy(tmpTab, 0, table, 0, table.length);
			} catch (PrivilegedActionException ex) {
				ex.printStackTrace();
			} 
		} 

		// REMIND: synchronization. MO
		if (v != null) {
			Enumeration e = v.elements();

			while (e.hasMoreElements()) {
				AgletClassLoader loader = (AgletClassLoader)e.nextElement();

				if (table == null || loader.matchAndImport(table)) {
					return loader;
				} 
			} 
		} 
		return null;
	}
	// 
	public ResourceManager getCurrentResourceManager() {
		ResourceManager rm = ResourceManagerImpl.getResourceManagerContext();

		if (rm == null) {
			SecurityManager sm = System.getSecurityManager();

			if (sm != null && sm instanceof AgletsSecurityManager) {
				AgletsSecurityManager asm = (AgletsSecurityManager)sm;
				ClassLoader loader = asm.getCurrentNonSecureClassLoader();

				if (loader instanceof AgletClassLoader) {
					rm = ((AgletClassLoader)loader);
				} else {

					// then, that's secure class loader.
					rm = _appResourceManager;
				} 
			} 
		} 
		return rm;
	}
	private static boolean isJarFile(String p) {
		return p.endsWith(".jar");
	}
	/**
	 * 
	 */
	public URL lookupCodeBaseFor(String name) {
		String codebase = null;

		try {
			codebase = lookupCodeBaseInManifest(name);

			// System.out.println("lookupCodeBaseInManifest("+name+")="+codebase);
			if (codebase == null) {
				codebase = lookupCodeBaseFrom(name, _agletsClassPath);

				// System.out.println("lookupCodeBaseFrom("+name+", _agletsClassPath)="+codebase);
			} 
			if (codebase == null) {
				return null;
			} 
		} catch (IOException ex) {
			ex.printStackTrace();
		} 

		// System.out.println("export root="+_exportRoot);
		if (codebase.startsWith(_publicRoot)) {
			codebase = _localAddr + File.separatorChar 
					   + codebase.substring(_publicRoot.length());
		} else {
			if (codebase.startsWith("/")) {
				codebase = "file:" + codebase;
			} else {
				codebase = "file:/" + codebase;
			} 
		} 

		try {
			URL u = new URL(codebase.replace(File.separatorChar, '/'));

			return u;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} 
	}
	static private String lookupCodeBaseFrom(String name, String[] pathList) 
			throws IOException {
		final String[] pl = pathList;
		final String classFileName = name.replace('.', File.separatorChar) 
									 + ".class";

		try {
			return (String)AccessController
				.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws IOException {
					for (int i = 0; i < pl.length; i++) {
						final File f = new File(pl[i] + File.separator 
												+ classFileName);

						if (f.exists()) {
							return getCanonicalDirectory(pl[i]);

							// REMIND:
							// file URL automatically adds local address to URL.
							// return new URL(new URL("file", "", absolute).toExternalForm());
						} 
					} 
					return null;
				} 
			});
		} catch (PrivilegedActionException ex) {
			throw (IOException)ex.getException();
		} 
	}
	static private String lookupCodeBaseInManifest(String name) 
			throws IOException {
		Enumeration e = _manifests.keys();

		while (e.hasMoreElements()) {
			Manifest m = (Manifest)e.nextElement();

			if (m.contains(name.replace('.', '/') + ".class")) {
				return (String)_manifests.get(m);
			} 
		} 
		return null;
	}
	private static void lookupJarFiles(String path, boolean recursive) {
		final File f = new File(path);
		Boolean b = 
			(Boolean)AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return new Boolean(f.isDirectory());
			} 
		});
		boolean isDir = b.booleanValue();

		if (isDir && recursive) {
			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					String[] list = f.list();
					String front = f.getPath() + File.separator;

					for (int i = 0; i < list.length; i++) {
						lookupJarFiles(front + list[i], false);
					} 
					return null;
				} 
			});
		} else if (isJarFile(path)) {
			readManifest(path);
		} 
	}
	private static void readManifest(String path) {
		try {
			path = new File(path).getCanonicalPath();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
		verboseOut("Reading manifest .. " + path);
		try {
			JarArchive j = new JarArchive(path);
			Manifest m = j.getManifest();

			_manifests.put(m, path);
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}
	static private void verboseOut(String msg) {
		AgletRuntime.verboseOut(msg);
	}
}
