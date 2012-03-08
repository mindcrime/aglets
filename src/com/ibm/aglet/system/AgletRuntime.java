package com.ibm.aglet.system;

/*
 * @(#)AgletRuntime.java
 * 
 * (c) Copyright IBM Corp. 1997, 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;

/**
 * <tt>AgletRuntime</tt> class provides the way to access the information of the
 * local or remote context. Neither of an application nor aglet can create its
 * own instance of this runtime class.
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:54 $
 * @author Mitsuru Oshima
 * @see AgletRuntime#getAgletRuntime
 */
public abstract class AgletRuntime {

    final static String runtimePackagePrefix = "aglet.runtime.packagePrefix";
    private static AgletsLogger logger = AgletsLogger.getLogger(AgletRuntime.class.getName());

    private boolean _secure = true;

    /*
     * [Preliminary] Exports the specified aglet context. After this successful
     * invocation, the specified context is visible and accessible from a
     * remote host, which means that the aglet can be dispatched into and also
     * the remote operations like listing the aglet proxies residing in the
     * context are enabled.
     * 
     * @param cxt
     *            the context to export
     * @exception SecurityException
     *                if the current execution is not allowed to export an
     *                AgletContext. abstract public void
     *                exportAgletContext(AgletContext cxt);
     */

    /*
     * [Preliminary] Exports the specified aglet. After this successfull
     * invocation, the specified aglet is visiable and accessible from a remote
     * host, which means that the aglet can be receive remote messages from
     * remote hosts.
     * 
     * @param aglet
     *            the aglet to export
     * @exception SecurityException
     *                if the current execution is not allowed to export an Aglet
     *                abstract public void exportAglet(Aglet aglet);
     */

    /*
     * abstract public boolean isGuiAvailable();
     */

    private static AgletRuntime runtime = null;

    /**
     * Returns an enumeration of proxies in the context specified by the
     * contextAddress.
     * 
     * @param contextAddress
     *            abstract public Enumeration getAgletProxies(String
     *            contextAddress, String authorityName);
     */
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
    abstract public Certificate authenticateOwner(
                                                  String username,
                                                  String password);

    /**
     * Creates an aglet remotely within the specified context.
     * 
     * @param contextAddress
     *            an address of context.
     * @param codebase
     *            an codebase for the aglet.
     * @param name
     *            name of aglets' class.
     * @param init
     *            an object passed as an initialize argument.
     * @see Aglet#onCreation
     * @see AgletContext#createAglet
     */
    abstract protected AgletProxy createAglet(
                                              String contextAddress,
                                              URL codebase,
                                              String name,
                                              Object init) throws IOException;

    /**
     * Creates an DefaultAgletContext object given by the Framework
     * implementation with specified name. Hosting multiple contexts is not
     * supported in alpha5 release.
     * 
     * @exception SecurityException
     *                if the current execution is not allowed to create an
     *                AgletContext.
     */
    abstract public AgletContext createAgletContext(String name);

    /**
     * Returns the AgletContext which has the specified name.
     * 
     * @param name
     *            the name of the context
     * @exception SecurityException
     *                if the current execution is not allowed to acccess the
     *                AgletContext.
     */
    abstract public AgletContext getAgletContext(String name);

    /**
     * Gets the contexts in the environment.
     */
    abstract public AgletContext[] getAgletContexts();

    /**
     * Returns the proxies in the context specified by the contextAddress.
     * 
     * @param contextAddress
     *            specify context URL with a string.
     */
    abstract protected AgletProxy[] getAgletProxies(String contextAddress)
    throws IOException;

    /**
     * Obtains the remote proxy for the aglet specified by the context and id.
     * 
     * @param contextAddress
     *            specify context URL with a string.
     * @param id
     *            target aglet identifyer.
     */
    abstract protected AgletProxy getAgletProxy(
                                                String contextAddress,
                                                AgletID id) throws IOException;

    /**
     * Gets the AgletRuntime object associated with the current Java
     * application.
     */
    synchronized static public AgletRuntime getAgletRuntime() {
	if (runtime == null) {
	    init(null);
	}
	return runtime;
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
    abstract public String getAgletsProperty(String key);

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
    abstract public String getAgletsProperty(String key, String def);

    /**
     * Returns certificate of the user who owns the runtime.
     * 
     * @return Certificate of the user who owns the runtime
     */
    abstract public Certificate getOwnerCertificate();

    /**
     * Returns name of the user who owns the runtime.
     * 
     * @return name of the user who owns the runtime
     */
    abstract public String getOwnerName();

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
    abstract public String getProperty(String prop, String key);

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
    abstract public String getProperty(String prop, String key, String def);

    /**
     * Gets an address of the server
     * 
     * @return the address of the server
     */
    abstract public String getServerAddress();

    /**
     * Create and initialize a runtime environment with a string array
     * 
     * @param args
     *            string array which is typically given as a argument to
     *            <tt>main(String args[])</tt> function.
     */
    synchronized static public AgletRuntime init(String args[]) {
	return init(args, null);
    }

    /**
     * Create and initialize a runtime environment with a string array
     * 
     * @param args
     *            string array which is typically given as a argument to
     *            <tt>main(String args[])</tt> function.
     * @param loader
     *            a classloader used to load a class of the implementation.
     */
    synchronized static public AgletRuntime init(
                                                 String args[],
                                                 ClassLoader loader) {
	if (runtime != null) {
	    throw new IllegalAccessError("Already Initialized");
	}

	String packagePrefix = "com.ibm.aglets";

	try {
	    packagePrefix = System.getProperty(runtimePackagePrefix, "com.ibm.aglets");
	} catch (SecurityException e) {

	    // for Fiji
	}

	String classname = packagePrefix + ".AgletRuntime";

	try {
	    Class clazz = null;

	    if (loader != null) {
		clazz = loader.loadClass(classname);
	    } else {
		clazz = Class.forName(classname);
	    }

	    Object obj = clazz.newInstance();

	    if (obj instanceof AgletRuntime) {
		runtime = (AgletRuntime) obj;
	    } else {
		logger.error("[ \"" + classname + "\" is not Runtime]");
	    }
	} catch (ClassNotFoundException ex) {
	    logger.error("[ The class \"" + classname + "\" not found]");
	} catch (Exception ex) {
	    logger.error("[ An instance of \"" + classname
		    + "\" cannot be created]");
	}

	runtime.initialize(args);
	return runtime;
    }

    /**
     * Initializes an AgletRuntime object with the given array of string. This
     * is typically an argument of <tt>main(String args[])</tt> function.
     * 
     * @param args
     *            arguments used to initialize
     * @exception IllegalAccessException
     *                if the instance has been already initialized.
     */
    abstract protected void initialize(String args[]);

    /**
     * Returns security.
     * 
     * @return True if the runtime is working with security
     */
    public boolean isSecure() {
	return this._secure;
    }

    /**
     * Kill the specified aglet. This aglet have to be a local aglet in this
     * runtime.
     * 
     * @param proxy
     *            the aglet proxy object to kill.
     */
    abstract public void killAglet(AgletProxy proxy)
    throws InvalidAgletException;

    /**
     * Removes the specified aglet context from the runtime environment. It is
     * also removed from export list if it's exported.
     * 
     * @param cxt
     *            the context to remove
     */
    abstract public void removeAgletContext(AgletContext cxt);

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
    abstract public void setAgletsProperty(String key, String value);

    /**
     * Save property of the user who owns the runtime. It needs
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
    abstract public void setProperty(String prop, String key, String value);

    /**
     * Sets secure.
     * 
     * @param secure
     *            true if the runtime is working with security
     */
    protected void setSecure(boolean secure) {
	this._secure = secure;
    }

    /**
     * Shutdown all contexts in the runtime
     */
    abstract public void shutdown();

    /**
     * Shutdown all contexts in the current runtime with the specific message
     * object. This messag object is delivered to all aglets in all contexts
     * before all aglets are killed.
     */
    abstract public void shutdown(Message msg);
}
