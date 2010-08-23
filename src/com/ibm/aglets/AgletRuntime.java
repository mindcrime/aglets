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

    private java.lang.String _ownerAlias = null;
    private java.security.cert.Certificate _ownerCertificate = null;
    private static java.security.KeyStore _keyStore = null;

    /*
     * Exported objects static Hashtable exported_contexts = new Hashtable();
     */

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
    public Certificate authenticateOwner(String username, String password) {
	this._ownerCertificate = authenticateUser(username, password);
	if (this._ownerCertificate == null) {
	    this._ownerAlias = null;
	} else {
	    this._ownerAlias = username;
	    this.startup();
	}
	return this._ownerCertificate;
    }

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
    public static Certificate authenticateUser(String username, String password) {
	try {
	    char[] pwd = null;

	    if ((password != null) && (password.length() > 0)) {
		pwd = password.toCharArray();
	    }
	    _keyStore.getKey(username, pwd);
	    return _keyStore.getCertificate(username);
	} catch (Exception ex) {
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

    //
    // check whether initialized or not
    //
    static void check() {
	if (initialized == false) {
	    throw new Error("AgletRuntime not initialized");
	}
    }

    /*
     * check permission
     */

    // accessable in package
    void checkPermission(Permission p) {
	if (!this.isSecure()) {
	    return;
	}

	// System.out.println("permission="+String.valueOf(p));
	// = SecurityManager sm = System.getSecurityManager();
	// = if (sm != null) {
	// = sm.checkPermission(p);
	// = }
	AccessController.checkPermission(p);
    }

    /**
     * Clear cache
     */
    public static void clearCache() {
	AgletContext contexts[] = getAgletRuntime().getAgletContexts();

	for (AgletContext context : contexts) {
	    AgletContextImpl cxt = (AgletContextImpl) context;
	    ResourceManagerFactory factory = cxt.getResourceManagerFactory();

	    if (factory != null) {
		factory.clearCache();
	    }
	}
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

    @Override
    public AgletProxy createAglet(
                                  String contextName,
                                  URL codebase,
                                  String classname,
                                  Object init) throws IOException {
	try {
	    Ticket ticket = new Ticket(contextName);

	    Message msg = new Message("createAglet");

	    if (codebase != null) {
		msg.setArg("codebase", codebase.toString());
	    }
	    msg.setArg("classname", classname);
	    if (init != null) {
		msg.setArg("init", init);
	    }
	    return (AgletProxy) MessageBroker.sendMessage(ticket, null, msg);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /**
     * AgletContext managements in this runtime
     */
    @Override
    public AgletContext createAgletContext(String name) {
	synchronized (contexts) {
	    if (contexts.get(name) == null) {
		AgletContext cxt = new AgletContextImpl(name);

		contexts.put(name, cxt);
		return cxt;
	    } else {
		throw new IllegalArgumentException("Context already exists");
	    }
	}
    }

    static public Persistence createPersistenceFor(AgletContext cxt) {
	check();
	if (persistenceFactory != null) {
	    return persistenceFactory.createPersistenceFor(cxt);
	}
	return null;
    }

    @Override
    public AgletContext getAgletContext(String name) {
	return (AgletContext) contexts.get(name);
    }

    @Override
    public AgletContext[] getAgletContexts() {
	synchronized (contexts) {
	    AgletContext[] c = new AgletContext[contexts.size()];
	    Enumeration e = contexts.elements();
	    int i = 0;

	    while (e.hasMoreElements()) {
		c[i++] = (AgletContext) e.nextElement();
	    }
	    return c;
	}
    }

    /**
     * Gets an enumeration of aglet proxies of all aglets residing in the
     * context specified by contextAddress.
     * 
     * @param contextAddress
     */
    @Override
    public AgletProxy[] getAgletProxies(String contextName) throws IOException {
	try {
	    Vector v = (Vector) MessageBroker.sendMessage(new Ticket(contextName), null, new Message("getAgletProxies"));
	    AgletProxy[] ret = new AgletProxy[v.size()];

	    v.copyInto(ret);
	    return ret;
	} catch (NotHandledException ex) {
	    ex.printStackTrace();
	    return null;
	} catch (MessageException ex) {
	    ex.printStackTrace();
	    return null;
	} catch (InvalidAgletException ex) {
	    ex.printStackTrace();
	    return null;
	}
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

    /**
     * Obtains a proxy reference the remote aglet.
     */
    @Override
    public AgletProxy getAgletProxy(String contextName, AgletID aid)
    throws IOException {
	Ticket ticket = new Ticket(contextName);

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
    public String getAgletsProperty(String key) {
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
    public String getAgletsProperty(String key, String def) {
	return this.getProperty("aglets", key, def);
    }

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
    public static Certificate getCertificate(byte[] encoded) {
	Certificate cert = null;

	try {
	    for (Enumeration e = _keyStore.aliases(); e.hasMoreElements();) {
		String alias = (String) e.nextElement();
		Certificate c = _keyStore.getCertificate(alias);
		byte[] ce = c.getEncoded();

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
	} catch (Exception ex) {
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
    public static Certificate getCertificate(String username) {
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
	} catch (KeyStoreException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /**
     * @return java.lang.String
     * @param cert
     *            java.security.cert.Certificate
     */
    public static String getCertificateAlias(Certificate cert) {
	try {
	    return _keyStore.getCertificateAlias(cert);
	} catch (KeyStoreException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /**
     * This was getCurrentIdentity
     */
    static Certificate getCurrentCertificate() {
	SecurityManager sm = System.getSecurityManager();

	if ((sm != null) && (sm instanceof AgletsSecurityManager)) {
	    AgletsSecurityManager asm = (AgletsSecurityManager) sm;
	    Certificate cert = asm.getCurrentCertificate();

	    if (cert == null) {
		com.ibm.aglet.system.AgletRuntime runtime = getAgletRuntime();

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

    /**
     * Returns certificate of the user who owns the runtime.
     * 
     * @return Certificate of the user who owns the runtime
     */
    @Override
    public Certificate getOwnerCertificate() {
	return this._ownerCertificate;
    }

    /**
     * Returns name of the user who owns the runtime.
     * 
     * @return name of the user who owns the runtime
     */
    @Override
    public String getOwnerName() {
	return this._ownerAlias;
    }

    /**
     * @return java.security.PrivateKey
     * @param cert
     *            java.security.cert.Certificate
     * @param passwd
     *            byte[]
     */
    public static java.security.PrivateKey getPrivateKey(
                                                         Certificate cert,
                                                         char[] passwd) {
	try {
	    return (java.security.PrivateKey) _keyStore.getKey(getCertificateAlias(cert), passwd);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
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
    public String getProperty(String prop, String key) {
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
    public String getProperty(String prop, String key, String def) {
	try {
	    SecurityManager security = System.getSecurityManager();

	    if (security != null) {
		security.checkPropertyAccess(key);
	    }
	} catch (SecurityException ex) {
	    return def;
	}

	Resource res = Resource.getResourceFor(prop);

	if (res == null) {

	    // needed ?
	    String username = this.getOwnerName();

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
	    } catch (MalformedURLException ex) {
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

    /**
     * Returns certificate of a registered user. If the user's certificate is
     * not found, returns null. Certificate is read from the keystore located by
     * "aglets.keystore.file" property.
     * 
     * @param username
     *            an certificate alias in the keystore.
     * @return certificate of the user, null if the user was not found.
     */
    public static Certificate getRegisteredCertificate(String username) {
	Certificate cert = null;

	try {
	    cert = _keyStore.getCertificate(username);
	    if (cert != null) {
		return cert;
	    } else {
		return null;
	    }
	} catch (KeyStoreException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public String getServerAddress() {
	return MAFAgentSystem.getLocalMAFAgentSystem().getAddress();
    }

    @Override
    synchronized protected void initialize(String args[]) {
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
	Resource res = Resource.getResourceFor("system");

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
	String keyStorePwd = System.getProperty("aglets.keystore.password", null);

	try {
	    FileInputStream in = new FileInputStream(keyStoreFile);

	    if (_keyStore == null) {
		_keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	    }
	    char[] pwd = null;

	    if (keyStorePwd != null) {
		pwd = keyStorePwd.toCharArray();
	    }
	    _keyStore.load(in, pwd);

	    // Read the certificate of anonymous user
	    String anonName = System.getProperty("aglets.keystore.anonymousAlias", "anonymous");

	    ANONYMOUS_USER = _keyStore.getCertificate(anonName);
	} catch (Exception ex) {
	    ex.printStackTrace();

	    // ILog.emerg();
	    System.exit(-1);
	}

	// Read policy file
	com.ibm.aglets.security.PolicyImpl policyImpl = new com.ibm.aglets.security.PolicyImpl();

	Policy.setPolicy(policyImpl);

	SharedSecrets.getSharedSecrets();

	// Mark that initialization is done.
	initialized = true;

	// For users information.
	// System.out.println(bundle.getString("aglets.license"));
	// System.out.println(bundle.getString("aglets.version"));
    }

    /**
     * Verbose message
     */
    public static final boolean isVerbose() {
	return verbose;
    }

    /**
     * Kill the specified aglet.
     */
    @Override
    public void killAglet(AgletProxy proxy) throws InvalidAgletException {
	LocalAgletRef ref = LocalAgletRef.getAgletRef(MAFUtil.toName(proxy.getAgletID(), null));

	if (ref != null) {
	    ref.kill();
	} else {
	    throw new InvalidAgletException("kill: local aglet not found");
	}
    }

    static Name newName(Certificate authority) {
	byte[] b = new byte[8];

	_randomGenerator.nextBytes(b);
	try {
	    return new Name(authority.getEncoded(), b, MAFUtil.AGENT_SYSTEM_TYPE_AGLETS);
	} catch (java.security.cert.CertificateEncodingException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public void removeAgletContext(AgletContext cxt) {
	synchronized (contexts) {
	    String name = cxt.getName();

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
    public void setAgletsProperty(String key, String value) {
	this.setProperty("aglets", key, value);
    }

    /*
     * Set a default ResourceManagerFactory
     */
    static public void setDefaultResourceManagerFactory(
                                                        ResourceManagerFactory factory) {
	check();

	defaultResourceManagerFactory = factory;
    }

    /*
     * Set a default ResourceManagerFactory
     */
    static public void setPersistenceFactory(PersistenceFactory p_factory) {
	check();

	persistenceFactory = p_factory;
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
    public void setProperty(String prop, String key, String value) {
	this.checkPermission(new PropertyPermission(key, "write"));

	Resource res = Resource.getResourceFor(prop);

	if (res == null) {
	    String username = this.getOwnerName();

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
	    } catch (MalformedURLException ex) {
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
    public void shutdown(Message msg) {
	synchronized (contexts) {
	    Enumeration e = contexts.elements();

	    while (e.hasMoreElements()) {
		((AgletContextImpl) e.nextElement()).shutdown(msg);
	    }
	}
	try {
	    MAFAgentSystem as = MAFAgentSystem.getLocalMAFAgentSystem();
	    MAFFinder finder = as.get_MAFFinder();

	    if (finder != null) {
		try {
		    AgentSystemInfo asi = as.get_agent_system_info();

		    finder.unregister_agent_system(asi.agent_system_name);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	} catch (FinderNotFound ex) {
	}
    }

    private void startup() {
	String username = this.getOwnerName();

	if (username == null) {
	    System.err.println("No user.");
	    return;
	}

	Resource res = Resource.getResourceFor("aglets");

	if (res == null) {
	    try {
		String propfile = FileUtils.getPropertyFilenameForUser(username, "aglets");

		res = Resource.createResource("aglets", propfile, null);
		logger.debug("startup: reading aglets property from "
			+ propfile);
	    } catch (Exception ex) {
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
	String export_path[] = res.getStringArray("aglets.export.path", File.pathSeparator
		+ ", ");

	if ((export_path.length > 0) && (export_path[0] != null)
		&& (export_path[0].length() > 0)) {
	    res.setOptionResource("aglets.public.root", export_path[0]);
	}
	if (export_path.length > 1) {
	    System.out.println(bundle.getString("aglets.error.export_morethan_one"));
	}

	String aglets_home = res.getString("aglets.home", null);

	//
	// Default Resources
	//
	String default_resources[][] = {
		{ "aglets.public.root", aglets_home + File.separator + "public" },
		{ "aglets.viewer", DEFAULT_VIEWER },
		{ "aglets.addressbook", "" },

		// {"aglets.box.userid", mailaddress},
		{
		    "aglets.agletsList",
		    "examples.simple.DisplayAglet "
		    + "examples.hello.HelloAglet "
		    + "examples.itinerary.CirculateAglet "
		    + "examples.mdispatcher.HelloAglet "
		    + "examples.http.WebServerAglet "
		    + "examples.talk.TalkMaster" }, };

	res.setDefaultResources(default_resources);

	this.setSecure(res.getBoolean("aglets.secure", true));

	try {
	    String spool_dir = FileUtils.getSpoolDirectory();

	    res.setDefaultResource("aglets.spool", spool_dir);
	    spool_dir = res.getString("aglets.spool");

	    String cache_dir = FileUtils.getCacheDirectory();

	    res.setDefaultResource("aglets.cache", cache_dir);
	    cache_dir = res.getString("aglets.cache");

	    FileUtils.ensureDirectory(spool_dir + File.separator);
	    FileUtils.ensureDirectory(cache_dir + File.separator);

	} catch (SecurityException ex) {
	    ex.printStackTrace();

	    //
	}
    }

    public static final void verboseOut(String msg) {
	if (verbose) {
	    logger.debug(msg);
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
}
