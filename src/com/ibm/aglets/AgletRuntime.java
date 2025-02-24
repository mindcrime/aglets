package com.ibm.aglets;

/*
 * @(#)AgletRuntime.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Permission;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListResourceBundle;
import java.util.PropertyPermission;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Vector;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.aglets.tahiti.AgletsSecurityManager;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.Opt;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.Version;
import com.ibm.maf.AgentSystemInfo;
import com.ibm.maf.FinderNotFound;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFFinder;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.Name;

final public class AgletRuntime extends com.ibm.aglet.system.AgletRuntime {

	/**
	 * Magic number of Aglet
	 */
	public final static int AGLET_MAGIC = 0xa9010;

	/**
	 * Version of Aglet Trasfer Format
	 */
	public final static byte AGLET_STREAM_VERSION = 9;

	/**
	 * THe version of this program.
	 */
	private static Version VERSION;

	private static ResourceBundle bundle = null;
	private static AgletsLogger logger = AgletsLogger.getLogger(AgletRuntime.class.getName());
	/*
	 * This must be outside of this source code.
	 */
	private static Object[][] contents;

	static {
		// initializes the version of this run-time.
		VERSION = new Version("Aglets Mobile Agent Platform", 2, 5, 0, false);

		contents = new Object[][] {
				{ "aglets.version", "[" + VERSION + "]" },
				{
					"aglets.license",
					"---------------------------------------------\n"
					+ " Licensed Materials - Property of IBM\n"
					+ "         (c) Copyright IBM Corp.\n"
					+ " 1996, 1998 All rights reserved.\n"
					+ " US Government Users Restricted Rights - \n"
					+ " Use, duplication or disclosure restricted\n"
					+ " by GSA ADP Schedule Contract with IBM Corp.\n"
					+ "---------------------------------------------" },
					{
						"aglets.error.export_morethan_one",
						"The obsolete AGLET_EXPORT_PATH env var contains more than one "
						+ "directories." }, };

		bundle = new ListResourceBundle() {
			@Override
			public Object[][] getContents() {
				return contents;
			}
		};

		// - bundle = (ResourceBundle)AccessController.doPrivileged(new
		// PrivilegedAction() {
		// - public Object run() {
		// - return ResourceBundle.getBundle("tahiti");
		// - }
		// - });
	}

	private static final String DEFAULT_VIEWER = "com.ibm.aglets.tahiti.Tahiti";

	final static Opt option_defs[] = {
		Opt.Entry("-nosecurity", "aglets.secure", "false", "    -nosecurity          disable security manager"),
		Opt.Entry("-enablebox", "aglets.enableBox", "true", null),
		Opt.Entry("-nojit"), };

	static AgletRuntime currentRuntime = null;

	static Hashtable contexts = new Hashtable();

	private static Certificate ANONYMOUS_USER = null;

	private static boolean verbose = false;
	private static boolean initialized = false;

	/**
	 * Gets User Administrator. private static UserAdministrator _administrator
	 * = null;
	 */

	/**
	 * Gets User Authenticator. private static UserAuthenticator _authenticator
	 * = null;
	 */

	/*
	 * Default ResourceManagerFactory object
	 */
	private static ResourceManagerFactory defaultResourceManagerFactory = null;

	/*
	 * PersistenceFactory
	 */
	private static PersistenceFactory persistenceFactory = null;

	/*
	 * Returns a new id.
	 */
	private static Random _randomGenerator = new Random();

	//
	// check whether initialized or not
	//
	static void check() {
		if (initialized == false) {
			throw new Error("AgletRuntime not initialized");
		}
	}
	/**
	 * Clear cache
	 */
	public static void clearCache() {
		final AgletContext contexts[] = getAgletRuntime().getAgletContexts();

		for (final AgletContext context : contexts) {
			final AgletContextImpl cxt = (AgletContextImpl) context;
			final ResourceManagerFactory factory = cxt.getResourceManagerFactory();

			if (factory != null) {
				factory.clearCache();
			}
		}
	}
	static public Persistence createPersistenceFor(final AgletContext cxt) {
		check();
		if (persistenceFactory != null) {
			return persistenceFactory.createPersistenceFor(cxt);
		}
		return null;
	}

	/*
	 * Exported objects static Hashtable exported_contexts = new Hashtable();
	 */

	static Certificate getAnonymousUserCertificate() {

		// if (ANONYMOUS_USER == null) {
		// UserAdministrator userAdmin = getUserAdministrator();
		// ANONYMOUS_USER = userAdmin.getAnonymousUser().getIdentity();
		// }
		return ANONYMOUS_USER;
	}

	/**
	 * Returns certificate of a user. If the user's certificate is not found,
	 * returns the certificate of anonymous user whose alias is indicated by
	 * "aglets.keystore.anonymousAlias" property. Certificate is read from the
	 * keystore located by "aglets.keystore.file" property.
	 * 
	 * @param encoded
	 *            an encoded byte array of a certificate.
	 * @return certificate which has the same byte array as the parameter.
	 */
	public static Certificate getCertificate(final byte[] encoded) {
		Certificate cert = null;

		try {
			for (final Enumeration e = _keyStore.aliases(); e.hasMoreElements();) {
				final String alias = (String) e.nextElement();
				final Certificate c = _keyStore.getCertificate(alias);
				final byte[] ce = c.getEncoded();

				if (encoded.length == ce.length) {
					boolean match = true;

					for (int i = 0; i < encoded.length; i++) {
						if (encoded[i] != ce[i]) {
							match = false;
							break;
						}
					}
					if (match) {
						cert = c;
						break;
					}
				}
			}
			if (cert != null) {
				return cert;
			} else {
				System.out.println("AgletRuntime is requested to get unknown user's certificate");
				return getAnonymousUserCertificate();
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns certificate of a user. If the user's certificate is not found,
	 * returns the certificate of anonymous user whose alias is indicated by
	 * "aglets.keystore.anonymousAlias" property. Certificate is read from the
	 * keystore located by "aglets.keystore.file" property.
	 * 
	 * @param username
	 *            an certificate alias in the keystore.
	 * @return certificate of the user
	 */
	public static Certificate getCertificate(final String username) {
		Certificate cert = null;

		try {
			cert = _keyStore.getCertificate(username);
			if (cert != null) {
				return cert;
			} else {
				System.out.println("AgletRuntime is requested to get unknown user's certificate: "
						+ username);
				return getAnonymousUserCertificate();
			}
		} catch (final KeyStoreException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * @return java.lang.String
	 * @param cert
	 *            java.security.cert.Certificate
	 */
	public static String getCertificateAlias(final Certificate cert) {
		try {
			return _keyStore.getCertificateAlias(cert);
		} catch (final KeyStoreException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/*
	 * check permission
	 */

	/**
	 * This was getCurrentIdentity
	 */
	static Certificate getCurrentCertificate() {
		final SecurityManager sm = System.getSecurityManager();

		if ((sm != null) && (sm instanceof AgletsSecurityManager)) {
			final AgletsSecurityManager asm = (AgletsSecurityManager) sm;
			Certificate cert = asm.getCurrentCertificate();

			if (cert == null) {
				final com.ibm.aglet.system.AgletRuntime runtime = getAgletRuntime();

				if (runtime != null) {
					cert = runtime.getOwnerCertificate();
				}
			}
			return cert;
		}

		// ??? Should return anonymous user's cert? (HT)
		return getAnonymousUserCertificate();
	}

	/*
	 * Returns a default ResourceManagerFactory
	 */
	static public ResourceManagerFactory getDefaultResourceManagerFactory() {
		check();

		return defaultResourceManagerFactory;
	}

	/*
	 * public Enumeration getAgletProxies(String contextName, String
	 * authorityName) { try { MessageBroker b =
	 * MessageBroker.getMessageBroker(contextName); Message m = new
	 * Message("getAgletProxies"); m.setArg("authorityName", authorityName);
	 * FutureReply f = b.sendAsyncMessage(new AgletID("00"), m); Vector v =
	 * (Vector)f.getReply(); return v.elements(); } catch (MalformedURLException
	 * ex) { ex.printStackTrace(); return null; } catch (NotHandledException ex)
	 * { ex.printStackTrace(); return null; } catch (MessageException ex) {
	 * ex.printStackTrace(); return null; } }
	 */

	/**
	 * @return java.security.PrivateKey
	 * @param cert
	 *            java.security.cert.Certificate
	 * @param passwd
	 *            byte[]
	 */
	public static java.security.PrivateKey getPrivateKey(
	                                                     final Certificate cert,
	                                                     final char[] passwd) {
		try {
			return (java.security.PrivateKey) _keyStore.getKey(getCertificateAlias(cert), passwd);
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns certificate of a registered user. If the user's certificate is
	 * not found, returns null. Certificate is read from the keystore located by
	 * "aglets.keystore.file" property.
	 * 
	 * @param username
	 *            an certificate alias in the keystore.
	 * @return certificate of the user, null if the user was not found.
	 */
	public static Certificate getRegisteredCertificate(final String username) {
		Certificate cert = null;

		try {
			cert = _keyStore.getCertificate(username);
			if (cert != null) {
				return cert;
			} else {
				return null;
			}
		} catch (final KeyStoreException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Provides back the version of this AgletRuntime.
	 * 
	 * @return the version of this running system
	 */
	public static Version getVersion() {
		return VERSION;
	}

	/**
	 * Verbose message
	 */
	public static final boolean isVerbose() {
		return verbose;
	}

	static Name newName(final Certificate authority) {
		final byte[] b = new byte[8];

		_randomGenerator.nextBytes(b);
		try {
			return new Name(authority.getEncoded(), b, MAFUtil.AGENT_SYSTEM_TYPE_AGLETS);
		} catch (final java.security.cert.CertificateEncodingException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/*
	 * Set a default ResourceManagerFactory
	 */
	static public void setDefaultResourceManagerFactory(
	                                                    final ResourceManagerFactory factory) {
		check();

		defaultResourceManagerFactory = factory;
	}

	/**
	 * Exporting objects public void exportAgletContext(AgletContext cxt) {
	 * exported_contexts.put(cxt.getName(), cxt); }
	 */

	/*
	 * public AgletContext getExportedAgletContext(String name) { return
	 * (AgletContext)exported_contexts.get(name); }
	 */

	/*
	 * public AgletStatus getAgletStatus(AgletID id) { return
	 * getLocalAgletRef(id).getStatus(); }
	 */

	/*
	 * Set a default ResourceManagerFactory
	 */
	static public void setPersistenceFactory(final PersistenceFactory p_factory) {
		check();

		persistenceFactory = p_factory;
	}

	public static final void verboseOut(final String msg) {
		if (verbose) {
			logger.debug(msg);
		}
	}

	private java.lang.String _ownerAlias = null;

	private java.security.cert.Certificate _ownerCertificate = null;

	private static java.security.KeyStore _keyStore = null;

	/**
	 * Authenticate an user with password. When the password is correct, returns
	 * the Certificate.
	 * 
	 * @param username
	 *            username of the user
	 * @param password
	 *            password of the user
	 * @return Certificate of the user, null if the authentication was in fail.
	 * 
	 *         ????NEED IMPL (HT)
	 */
	public static Certificate authenticateUser(final String username, final String password) {
		try {
			char[] pwd = null;

			if ((password != null) && (password.length() > 0)) {
				pwd = password.toCharArray();
			}
			_keyStore.getKey(username, pwd);
			return _keyStore.getCertificate(username);
		} catch (final Exception ex) {
			return null;
		}

		/*
		 * ----------- final String uname = username; final String passwd =
		 * password; final UserAuthenticator authenticator =
		 * getUserAuthenticator(); Certificate cert = null; cert =
		 * (Certificate)AccessController.doPrivileged(new PrivilegedAction() {
		 * public Object run() { try { char[] pwd = null; if (passwd != null &&
		 * passwd.length() > 0) { pwd = passwd.toCharArray(); } Key k =
		 * _keyStore.getKey(uname, pwd); return _keyStore.getCertificate(uname);
		 * } catch (Exception ex) { ex.printStackTrace(); return null; } } });
		 * return cert; ----------------
		 */
	}

	/*
	 * Constructs an instance of AgletRuntime
	 */
	public AgletRuntime() {
		synchronized (AgletRuntime.class) {
			if (currentRuntime != null) {
				throw new SecurityException("Runtime cannot be created twice");
			}
			currentRuntime = this;
		}
	}

	/**
	 * Authenticate an user with password. When the password is correct, the
	 * user owns the runtime and returns the owner's certificate.
	 * 
	 * @param username
	 *            username of the user who will own the runtime
	 * @param password
	 *            password of the user
	 * @return the owner's certificate when authentication of the user succeeds
	 */
	@Override
	public Certificate authenticateOwner(final String username, final String password) {
		_ownerCertificate = authenticateUser(username, password);
		if (_ownerCertificate == null) {
			_ownerAlias = null;
		} else {
			_ownerAlias = username;
			startup();
		}
		return _ownerCertificate;
	}

	// accessable in package
	void checkPermission(final Permission p) {
		if (!isSecure()) {
			return;
		}

		// System.out.println("permission="+String.valueOf(p));
		// = SecurityManager sm = System.getSecurityManager();
		// = if (sm != null) {
		// = sm.checkPermission(p);
		// = }
		AccessController.checkPermission(p);
	}

	@Override
	public AgletProxy createAglet(
	                              final String contextName,
	                              final URL codebase,
	                              final String classname,
	                              final Object init) throws IOException {
		try {
			final Ticket ticket = new Ticket(contextName);

			final Message msg = new Message("createAglet");

			if (codebase != null) {
				msg.setArg("codebase", codebase.toString());
			}
			msg.setArg("classname", classname);
			if (init != null) {
				msg.setArg("init", init);
			}
			return (AgletProxy) MessageBroker.sendMessage(ticket, null, msg);
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * AgletContext managements in this runtime
	 */
	@Override
	public AgletContext createAgletContext(final String name) {
		synchronized (contexts) {
			if (contexts.get(name) == null) {
				final AgletContext cxt = new AgletContextImpl(name);

				contexts.put(name, cxt);
				return cxt;
			} else {
				throw new IllegalArgumentException("Context already exists");
			}
		}
	}

	@Override
	public AgletContext getAgletContext(final String name) {
		return (AgletContext) contexts.get(name);
	}

	@Override
	public AgletContext[] getAgletContexts() {
		synchronized (contexts) {
			final AgletContext[] c = new AgletContext[contexts.size()];
			final Enumeration e = contexts.elements();
			int i = 0;

			while (e.hasMoreElements()) {
				c[i++] = (AgletContext) e.nextElement();
			}
			return c;
		}
	}

	/**
	 * Gets an enumeration of aglet proxies of all aglets residing in the
	 * context specified by contextName.
	 * 
	 * @param contextName
	 *            name of the context whose proxies to list
	 */
	@Override
	public AgletProxy[] getAgletProxies(final String contextName) throws IOException {
		try {
			final Vector v = (Vector) MessageBroker.sendMessage(new Ticket(contextName), null, new Message("getAgletProxies"));
			final AgletProxy[] ret = new AgletProxy[v.size()];

			v.copyInto(ret);
			return ret;
		} catch (final NotHandledException ex) {
			ex.printStackTrace();
			return null;
		} catch (final MessageException ex) {
			ex.printStackTrace();
			return null;
		} catch (final InvalidAgletException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Obtains a proxy reference the remote aglet.
	 */
	@Override
	public AgletProxy getAgletProxy(final String contextName, final AgletID aid)
	throws IOException {
		final Ticket ticket = new Ticket(contextName);

		return new AgletProxyImpl(RemoteAgletRef.getAgletRef(ticket, MAFUtil.toName(aid, null)));
	}

	/**
	 * Returns aglets property of the user who owns the runtime. It needs
	 * PropertyPermission for the key of aglets property.
	 * 
	 * @param key
	 *            key of aglets property
	 * @return aglets property of the user who owns the runtime. If the property
	 *         for the key does not exist, return null.
	 * @exception SecurityException
	 *                if PropertyPermission for the key is not give.
	 */
	@Override
	public String getAgletsProperty(final String key) {
		return this.getAgletsProperty(key, null);
	}

	/**
	 * Returns aglets property of the user who owns the runtime. It needs
	 * PropertyPermission for the key of aglets property.
	 * 
	 * @param key
	 *            key of aglets property
	 * @param def
	 *            default value of aglets property
	 * @return aglets property of the user who owns the runtime. If the property
	 *         for the key does not exist, return def.
	 * @exception SecurityException
	 *                if PropertyPermission for the key is not given.
	 */
	@Override
	public String getAgletsProperty(final String key, final String def) {
		return this.getProperty("aglets", key, def);
	}

	/**
	 * Returns certificate of the user who owns the runtime.
	 * 
	 * @return Certificate of the user who owns the runtime
	 */
	@Override
	public Certificate getOwnerCertificate() {
		return _ownerCertificate;
	}

	/**
	 * Returns name of the user who owns the runtime.
	 * 
	 * @return name of the user who owns the runtime
	 */
	@Override
	public String getOwnerName() {
		return _ownerAlias;
	}

	/**
	 * Returns property of the user who owns the runtime. It needs
	 * PropertyPermission for the key of specified property, and FilePermission
	 * for the property file.
	 * 
	 * @param prop
	 *            name of properties
	 * @param key
	 *            key of property
	 * @return property of the user who owns the runtime. If the property for
	 *         the key does not exist, return null.
	 * @exception SecurityException
	 *                if PropertyPermission for the key is not given.
	 */
	@Override
	public String getProperty(final String prop, final String key) {
		return this.getProperty(prop, key, null);
	}

	/**
	 * Returns property of the user who owns the runtime. It needs
	 * PropertyPermission for the key of specified property, and FilePermission
	 * for the property file.
	 * 
	 * @param prop
	 *            name of properties
	 * @param key
	 *            key of property
	 * @param def
	 *            default value of property
	 * @return property of the user who owns the runtime. If the property for
	 *         the key does not exist, return def.
	 * @exception SecurityException
	 *                if PropertyPermission for the key is not given.
	 */
	@Override
	public String getProperty(final String prop, final String key, final String def) {
		try {
			final SecurityManager security = System.getSecurityManager();

			if (security != null) {
				security.checkPropertyAccess(key);
			}
		} catch (final SecurityException ex) {
			return def;
		}

		Resource res = Resource.getResourceFor(prop);

		if (res == null) {

			// needed ?
			final String username = getOwnerName();

			if (username == null) {
				logger.error("No user.");
				return def;
			}
			String propfile = null;
			final String uname = username;
			final String prp = prop;

			propfile = (String) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return FileUtils.getPropertyFilenameForUser(uname, prp);
				}
			});
			try {
				res = Resource.createResource(prop, propfile, null);
			} catch (final MalformedURLException ex) {
				System.err.println(ex.toString());
			}
			logger.debug("getProperty: reading " + prop + " property from "
					+ propfile);
		}
		String ret = null;

		if (res != null) {
			ret = res.getString(key);
		}
		return ret == null ? def : ret;
	}

	@Override
	public String getServerAddress() {
		return MAFAgentSystem.getLocalMAFAgentSystem().getAddress();
	}

	@Override
	synchronized protected void initialize(final String args[]) {
		if (initialized) {
			throw new IllegalAccessError("AgletRuntime already initialized");
		}

		// Interprets the command line option.
		Opt.setopt(MAFAgentSystem.option_defs);
		Opt.setopt(option_defs);
		Opt.getopt(args);
		if (Opt.checkopt(args) == false) {
			System.exit(1);
		}

		// Check if it's verbose.
		final Resource res = Resource.getResourceFor("system");

		if (res != null) {
			verbose = res.getBoolean("verbose", false);
		}

		// Load the keystore.
		String keyStoreFile = System.getProperty("aglets.keystore.file", null);

		if (keyStoreFile == null) {
			String userHome = System.getProperty("user.home");

			if (!userHome.endsWith(File.separator)) {
				userHome += File.separator;
			}
			keyStoreFile = userHome + ".keystore";
		}
		final String keyStorePwd = System.getProperty("aglets.keystore.password", null);

		try {
			final FileInputStream in = new FileInputStream(keyStoreFile);

			if (_keyStore == null) {
				_keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			}
			char[] pwd = null;

			if (keyStorePwd != null) {
				pwd = keyStorePwd.toCharArray();
			}
			_keyStore.load(in, pwd);

			// Read the certificate of anonymous user
			final String anonName = System.getProperty("aglets.keystore.anonymousAlias", "anonymous");

			ANONYMOUS_USER = _keyStore.getCertificate(anonName);
		} catch (final Exception ex) {
			ex.printStackTrace();

			// ILog.emerg();
			System.exit(-1);
		}

		// Read policy file
		final com.ibm.aglets.security.PolicyImpl policyImpl = new com.ibm.aglets.security.PolicyImpl();

		Policy.setPolicy(policyImpl);

		SharedSecrets.getSharedSecrets();

		// Mark that initialization is done.
		initialized = true;

		// For users information.
		// System.out.println(bundle.getString("aglets.license"));
		// System.out.println(bundle.getString("aglets.version"));
	}

	/**
	 * Kill the specified aglet.
	 */
	@Override
	public void killAglet(final AgletProxy proxy) throws InvalidAgletException {
		final LocalAgletRef ref = LocalAgletRef.getAgletRef(MAFUtil.toName(proxy.getAgletID(), null));

		if (ref != null) {
			ref.kill();
		} else {
			throw new InvalidAgletException("kill: local aglet not found");
		}
	}

	@Override
	public void removeAgletContext(final AgletContext cxt) {
		synchronized (contexts) {
			final String name = cxt.getName();

			contexts.remove(name);

			// exported_contexts.remove(name);
		}
	}

	/**
	 * Sets aglets property of the user who owns the runtime. It needs
	 * PropertyPermission for the key of aglets property, and FilePermission for
	 * the aglets property file.
	 * 
	 * @param key
	 *            key of aglets property
	 * @param value
	 *            value of specified aglets property
	 * @exception SecurityException
	 *                if permissions for the key are not given.
	 */
	@Override
	public void setAgletsProperty(final String key, final String value) {
		setProperty("aglets", key, value);
	}

	/**
	 * Sets property of the user who owns the runtime. It needs
	 * PropertyPermission for the key of property, and FilePermission for the
	 * property file.
	 * 
	 * @param prop
	 *            name of properties
	 * @param key
	 *            key of property
	 * @param value
	 *            value of specified property
	 * @exception SecurityException
	 *                if permissions for the key are not given.
	 */
	@Override
	public void setProperty(final String prop, final String key, final String value) {
		checkPermission(new PropertyPermission(key, "write"));

		Resource res = Resource.getResourceFor(prop);

		if (res == null) {
			final String username = getOwnerName();

			if (username == null) {
				System.err.println("No user.");
				return;
			}
			String propfile = null;
			final String uname = username;
			final String prp = prop;

			propfile = (String) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return FileUtils.getPropertyFilenameForUser(uname, prp);
				}
			});
			try {
				res = Resource.createResource(prop, propfile, null);
			} catch (final MalformedURLException ex) {
				System.err.println(ex.toString());
			}
			logger.debug("setProperty: reading " + prop + " property from "
					+ propfile);
		}
		if (res == null) {
			logger.error("No resource.");
			return;
		}
		res.setResource(key, value);
		res.save(prop);
		return;
	}

	/**
	 * Shutdown
	 */
	@Override
	public void shutdown() {
		this.shutdown(new Message("shutdown"));
	}

	@Override
	public void shutdown(final Message msg) {
		synchronized (contexts) {
			final Enumeration e = contexts.elements();

			while (e.hasMoreElements()) {
				((AgletContextImpl) e.nextElement()).shutdown(msg);
			}
		}
		try {
			final MAFAgentSystem as = MAFAgentSystem.getLocalMAFAgentSystem();
			final MAFFinder finder = as.get_MAFFinder();

			if (finder != null) {
				try {
					final AgentSystemInfo asi = as.get_agent_system_info();

					finder.unregister_agent_system(asi.agent_system_name);
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (final FinderNotFound ex) {
		}
	}

	private void startup() {
		final String username = getOwnerName();

		if (username == null) {
			System.err.println("No user.");
			return;
		}

		Resource res = Resource.getResourceFor("aglets");

		if (res == null) {
			try {
				final String propfile = FileUtils.getPropertyFilenameForUser(username, "aglets");

				res = Resource.createResource("aglets", propfile, null);
				logger.debug("startup: reading aglets property from "
						+ propfile);
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		res.importOptionProperties("aglets");

		//
		// This should be moved to "resource bundle".
		//
		res.setResource("aglets.version", VERSION.toString());
		res.setResource("aglets.stream.version", String.valueOf(AgletRuntime.AGLET_STREAM_VERSION));
		res.setOptionResource("aglets.copyright", "Licenced Materials - Property of IBM \n"
				+ "(c) Copyright IBM Corp. 1996,1997 All Rights Reserved");

		//
		//
		//
		final String export_path[] = res.getStringArray("aglets.export.path", File.pathSeparator
				+ ", ");

		if ((export_path.length > 0) && (export_path[0] != null)
				&& (export_path[0].length() > 0)) {
			res.setOptionResource("aglets.public.root", export_path[0]);
		}
		if (export_path.length > 1) {
			System.out.println(bundle.getString("aglets.error.export_morethan_one"));
		}

		final String aglets_home = res.getString("aglets.home", null);
		final String aglets_public = aglets_home + File.separator + "public";

		// Pre-loaded list of local example aglets 
		final StringBuilder aglets_list = new StringBuilder();

		// File system path to the example aglets
		final File examples_dir = new File(aglets_public);
		if (examples_dir.exists()) {
			final URI ed_uri = examples_dir.toURI();
			// Create a list of URIs of example aglets
			final String examples_package = "net.sourceforge.aglets.examples";
			final String[] examples_class_suffixes = {
					"simple.DisplayAglet",
					"simple.TimeoutAglet",
					"simple.VanillaAglet",
					"hello.HelloAglet",
					"itinerary.CirculateAglet",
					"itinerary.VisitingAglet",
					"itinerary.StationaryAglet",
					"itinerary.MessengerAglet",
					"logger.LoggingAgent",
					"mdispatcher.HelloAglet",
					"http.WebServerAglet",
					"talk.TalkMaster",
					"talk.TalkSlave",
					"events.MobilityEvents",
					"finder.Finder",
					"finder.HostCollector",
					"finder.HostList",
					"finder.HostTravellor",
					"finder.Test",
					"finder.Traveller",
					"openurl.OpenURL",
					"patterns.Finger",
					"patterns.FingerSlave",
					"patterns.Watcher",
					"patterns.WatcherNotifier",
					"patterns.Writer",
					"patterns.WriterSlave",
					"protection.ProtectionAglet",
					"protection.TargetAglet",
					"simplemasterslave.SimpleMaster",
					"simplemasterslave.SimpleSlave",
					"start.FirstAglet",
					"thread.AgletSleeping",
					"thread.AgletThread",
					"thread.ThreadAgent",
					"thread.ReentrantThreadAgent",
					"thread.SleepingAgent",
					"watcher.ProxyWatcher",
					"watcher.WatcherSlave"
			};
			// string representation of the URI of the aglet's class
			String aglet_uri = null;
			// delimiter between URLs, initially empty for the front of the result string
			String separator = "";
			// actual value of the separator, to be assigned to 'separator'
			// when the program is sure that the first element has been appended
			final String actual_separator = " ";
			for (final String s: examples_class_suffixes) {
				try {
					// create the URI from elements
					aglet_uri = new URI(
							ed_uri.getScheme(),
							ed_uri.getUserInfo(),
							ed_uri.getHost(),
							ed_uri.getPort(),
							ed_uri.getPath(),
							examples_package + "." + s,
							ed_uri.getFragment()
					).normalize().toURL().toString();
					// append separator
					aglets_list.append(separator);
					// append next list item
					aglets_list.append(aglet_uri);
					// switch separator to actual value from the second item onwards
					separator = actual_separator;
				} catch (final URISyntaxException ex) {
					// assuming that the manipulations here in code have not ruined
					// the URL, blame the public directory
					logger.warn("'Public' aglets directory leads to a URI syntax error: "
							+ ed_uri.toString());
				} catch (final MalformedURLException e) {
					// assuming that the manipulations here in code have not ruined
					// the URL, blame the public directory
					logger.warn("'Public' aglets directory leads to a malformed URL: "
							+ ed_uri.toString());
				}
			}
			// append to the list of aglets a local one that comes in a JAR file
			URI jar_uri = null;
			try {
				final File jar = new File(aglets_public + File.separator + "translator.jar");
				jar_uri = jar.toURI();
				final String jar_aglet = new URI (
						jar_uri.getScheme(),
						jar_uri.getUserInfo(),
						jar_uri.getHost(),
						jar_uri.getPort(),
						jar_uri.getPath(),
						"net.sourceforge.aglets.examples.translator.TranslatingAglet",
						jar_uri.getFragment()
				).normalize().toURL().toString();
				// append separator
				aglets_list.append(separator);
				// append next list item
				aglets_list.append(jar_aglet);
				// switch separator to actual value in case it hasn't been set yet
				separator = actual_separator;
			} catch (final URISyntaxException ex) {
				// assuming that the manipulations here in code have not ruined
				// the URL, blame the jar file
				logger.warn("Location of 'public/translator.jar' leads to a URI syntax error: "
						+ jar_uri.toString());
			} catch (final MalformedURLException e) {
				// assuming that the manipulations here in code have not ruined
				// the URL, blame the jar file
				logger.warn("Location of 'public/translator.jar' leads to a malformed URL: "
						+ jar_uri.toString());
			}
			// append to the list of aglets a few stored on a web server
			// append separator
			aglets_list.append(separator);
			// append next list item
			aglets_list.append("http://aglets.sourceforge.net/aglet_examples"
					+ "?net.sourceforge.aglets.examples.start.FirstAglet");
			// append actual separator (regardless of whether 'separator' contains or not
			// the actual_separator value, here we are certain that the previous value has been appended)
			aglets_list.append(actual_separator);
			// append next list item
			aglets_list.append("http://aglets.sourceforge.net/aglet_examples/translator.jar"
					+ "?net.sourceforge.aglets.examples.translator.TranslatingAglet");
		} else {
			logger.warn("'Public' directory missing:" + aglets_public);
		}

		//
		// Default Resources
		//
		final String default_resources[][] = {
				{ "aglets.public.root", aglets_public },
				{ "aglets.viewer", DEFAULT_VIEWER },
				{ "aglets.addressbook", "" },

				// {"aglets.box.userid", mailaddress},
				{ "aglets.agletsList", aglets_list.toString() } };

		res.setDefaultResources(default_resources);

		setSecure(res.getBoolean("aglets.secure", true));

		try {
			String spool_dir = FileUtils.getSpoolDirectory();

			res.setDefaultResource("aglets.spool", spool_dir);
			spool_dir = res.getString("aglets.spool");

			String cache_dir = FileUtils.getCacheDirectory();

			res.setDefaultResource("aglets.cache", cache_dir);
			cache_dir = res.getString("aglets.cache");

			FileUtils.ensureDirectory(spool_dir + File.separator);
			FileUtils.ensureDirectory(cache_dir + File.separator);

		} catch (final SecurityException ex) {
			ex.printStackTrace();

			//
		}
	}
}
