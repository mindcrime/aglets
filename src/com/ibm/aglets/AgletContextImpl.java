package com.ibm.aglets;

/*
 * @(#)AgletContextImpl.java
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

import java.applet.AudioClip;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.aglets.log.AgletsLogger;

import sun.audio.AudioData;
import sun.audio.AudioStream;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.ServerNotFoundException;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.event.EventType;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.aglet.message.ReplySet;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglet.util.ImageData;
import com.ibm.aglets.security.ContextPermission;
import com.ibm.awb.misc.Resource;
import com.ibm.maf.AgentNotFound;
import com.ibm.maf.ClassName;
import com.ibm.maf.EntryNotFound;
import com.ibm.maf.FinderNotFound;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFFinder;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.Name;

/**
 * The <tt>AgletContextImpl</tt> class is the execution context for running
 * aglets. It provides means for maintaining and managing running aglets in an
 * environment where the aglets are protected from each other and the host
 * system is secured against malicious aglets.
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */

final public class AgletContextImpl implements AgletContext {
    private static AgletsLogger logger = AgletsLogger.getLogger(AgletContextImpl.class.getName());

    /*
     * secure/unsecure
     */
    private boolean _secure = true;

    /*
     * permissions
     */
    private static ContextPermission START_PERMISSION = null;
    private static ContextPermission SHUTDOWN_PERMISSION = null;
    private static ContextPermission ADD_LISTENER_PERMISSION = null;
    private static ContextPermission REMOVE_LISTENER_PERMISSION = null;

    /*
     * Hosting information.
     */
    private URL _hostingURL = null;

    /*
     * Context name
     */
    private String _name = "";

    /*
     * Persistence
     */
    private Persistence _persistence;

    /*
     * Table of Agletproxies
     */
    private Hashtable _agletProxies = new Hashtable();

    /*
     * Context properties.
     */
    Properties _contextProperties = new Properties();

    /*
     * Subscriber Manager
     */
    SubscriberManager _subscriberManager = new SubscriberManager();

    /*
     * ResourceManagerFactroy for this context
     */
    private ResourceManagerFactory _rm_factory = null;

    /*
     * Timer Management
     */
    AgletTimer _timer = null;

    /*
     * The object to synchronise creational operation
     */
    private Object creationLock = new Object();
    private int creating = 0;
    private boolean shutting_down = true;

    /*
     * Checks to see if it is possible to retrieve the aglet. public void
     * revertAglet(AgletID aid, URL remoteURL, OutputStream out) throws
     * IOException, InvalidAgletException { // // REMIND: here, we have to lock
     * the proxy // return; }
     */

    AgletID context_aid = new AgletID("00");

    /**
     * A list of context listeners.
     */
    ListenerList listeners = null;

    EventRunner erunner = null;

    /* protected */

    class EventRunner extends Thread {
	Vector events = new Vector();
	boolean sync = false;

	EventRunner() {
	    this.setPriority(6);
	}

	public void postEvent(ContextEvent event) {
	    this.events.addElement(event);
	}

	public void sync() {
	    this.sync = true;
	    synchronized (this) {
		ContextEvent event;

		while (this.events.size() > 0) {
		    event = (ContextEvent) this.events.firstElement();
		    this.events.removeElementAt(0);
		    AgletContextImpl.this.postEvent0(event);
		}
		this.sync = false;
	    }
	}

	@Override
	public void run() {
	    ContextEvent event;

	    while (true) {
		synchronized (this) {
		    while (this.sync || (this.events.size() == 0)) {
			try {
			    this.wait(1500);
			} catch (Exception ex) {
			    return;
			}
		    }
		    event = (ContextEvent) this.events.firstElement();
		    this.events.removeElementAt(0);
		}
		try {
		    AgletContextImpl.this.postEvent0(event);
		} catch (Exception t) {
		    t.printStackTrace();
		}
	    }
	}
    }

    private Hashtable images = new Hashtable();

    private Hashtable clips = new Hashtable();

    /**
     * Creates an execution context for aglets.
     * 
     * @param prop
     *            property list.
     */

    /* package protected */
    AgletContextImpl(String name) {
	this(name, com.ibm.aglet.system.AgletRuntime.getAgletRuntime().isSecure());
    }

    AgletContextImpl(String name, boolean secure) {
	this._name = name;
	this._timer = new AgletTimer(this);
	this.setSecurity(secure);
    }

    /*
     * Adds an aglet to the current context.
     */
    void addAgletProxy(AgletID aid, AgletProxyImpl proxy)
							 throws InvalidAgletException {

	// proxy.checkValidation();
	// REMIND: critical session
	this._agletProxies.put(aid, proxy);
    }

    /**
     * Adds the specified context listener to the listener lists, only if the
     * listener is not already contained in the listener list and if the agent
     * has the right permission.
     */
    public synchronized void addContextListener(ContextListener listener) {
	// check params
	if (listener == null)
	    return;

	// check if I've the permission to add a listener
	// First of all check if I've got a permission already built, otherwise
	// build the add listener permission type and store it.
	if (ADD_LISTENER_PERMISSION == null) {
	    ADD_LISTENER_PERMISSION = new ContextPermission("listener", "add");
	}

	this.checkPermission(ADD_LISTENER_PERMISSION);

	// if the listener container is null create a new one
	if (this.listeners == null)
	    this.listeners = new ListenerList();

	// now add the new element
	if (!this.listeners.contains(listener)) {
	    logger.debug("Adding the context listener " + listener);
	    this.listeners.add(listener);
	}
    }

    /**
     * Checks the specified permission calling the access controller.
     * 
     * @param permission
     *            the permission to check
     */
    void checkPermission(Permission permission) {
	// check arguments
	if ((!this._secure) || (permission == null)) {
	    return;
	}

	// check the permission
	AccessController.checkPermission(permission);
    }

    /**
     * Clear the cache
     */
    public void clearCache(URL codebase) {
	this._rm_factory.clearCache(codebase, AgletRuntime.getCurrentCertificate());
    }

    /**
     * Creates an instance of the specified aglet located at the specified URL.
     * 
     * @param url
     *            the URL to load the aglet class from.
     * @param name
     *            the aglet's class name.
     * @return a newly instantiated and initialized Aglet.
     * @exception ClassNotFoundException
     *                if the class was not found
     * @exception InstantiationException
     *                if failed to instantiate the Aglet.
     */
    public AgletProxy createAglet(URL url, String classname, Object init)
									 throws IOException,
									 AgletException,
									 ClassNotFoundException,
									 InstantiationException {
	Certificate owner = AgletRuntime.getCurrentCertificate();

	return this.createAglet(url, classname, owner, init);
    }

    /**
     * Creates an instance of the specified aglet located at the specified URL.
     * 
     * @param url
     *            the URL to load the aglet class from.
     * @param name
     *            the aglet's class name.
     * @return a newly instantiated and initialized Aglet.
     * @exception ClassNotFoundException
     *                if the class was not found
     * @exception InstantiationException
     *                if failed to instantiate the Aglet.
     */
    private AgletProxy createAglet(
				   URL url,
				   String classname,
				   Certificate owner,
				   Object init)
					       throws IOException,
					       AgletException,
					       ClassNotFoundException,
					       InstantiationException {

	// System.out.println("createAglet("+url+","+classname+","+owner.getName()+","+init+")");
	this.startCreation();
	try {

	    //
	    // Converts URL to the destination url.
	    //
	    if ((url != null) && (url.getRef() != null)) {
		this.log("Create", "Fail to create an aglet \"" + classname
			+ "\" from " + (url == null ? "Local" : url.toString()));
		throw new MalformedURLException("MalformedURL in createAglet:"
			+ url);
	    }
	    if (url == null) {
		url = this._rm_factory.lookupCodeBaseFor(classname);

		// System.out.println("lookupCodeBaseFor("+classname+")="+String.valueOf(url));
		if (url == null) {
		    throw new ClassNotFoundException(classname);
		}
	    }
	    String agletLocation = String.valueOf(url) + "@" + classname;

	    this.checkPermission(new ContextPermission(agletLocation, "create"));
	    Aglet aglet = null;
	    LocalAgletRef ref = new LocalAgletRef(this, this._secure);

	    ref.setName(AgletRuntime.newName(owner));
	    ref.info = new AgletInfo(MAFUtil.toAgletID(ref.getName()), classname, url, this.getHostingURL().toString(), System.currentTimeMillis(), Aglet.MAJOR_VERSION, Aglet.MINOR_VERSION, owner);
	    ResourceManager rm = ref.createResourceManager(null);

	    rm.setResourceManagerContext();
	    try {
		aglet = (Aglet) rm.loadClass(classname).newInstance();
	    } catch (ClassCastException ex) {
		this.log("Create", "Fail to create an aglet \"" + classname
			+ "\" from " + url);
		throw new InstantiationException("ClassCastException:"
			+ classname + ":" + ex.getMessage());
	    } catch (ClassNotFoundException ex) {
		this.log("Create", "Fail to create an aglet \"" + classname
			+ "\" from " + url);
		throw ex;
	    } catch (InstantiationException ex) {
		this.log("Create", "Fail to create an aglet \"" + classname
			+ "\" from " + url);
		throw ex;
	    } catch (IllegalAccessException ex) {
		this.log("Create", "Fail to create an aglet \"" + classname
			+ "\" from " + url);
		throw new InstantiationException("IllegalAccessException:"
			+ classname + ":" + ex.getMessage());
	    } finally {
		rm.unsetResourceManagerContext();
	    }

	    // # // get an allowance for the codebase at the url from policy DB
	    // file
	    // # ref.resetAllowance();

	    // start
	    aglet.setStub(ref);
	    ref.proxy = new AgletProxyImpl(ref);
	    ref.startCreatedAglet(this, init);
	    this.log("Create", classname + " from " + url);

	    // / if (_finder != null) {
	    // / try {
	    // / _finder.register_agent(ref.getName(),
	    // / _hostingURL.toString(),
	    // / MAF.toAgentProfile(ref.info));
	    // / } catch (NameInvalid ex) {
	    // / ex.printStackTrace();
	    // / }
	    // / }

	    return ref.proxy;
	} finally {
	    this.endCreation();
	}
    }

    /**
	 * 
	 */
    synchronized ResourceManager createResourceManager(
						       URL codebase,
						       Certificate owner,
						       ClassName[] table) {
	return this._rm_factory.createResourceManager(codebase, owner, table);
    }

    void endCreation() {
	synchronized (this.creationLock) {
	    this.creating--;
	    if (this.shutting_down) {
		this.creationLock.notify();
	    }
	}
    }

    /**
     * Gets the aglet proxies in the current execution context.
     * 
     * @return an enumeration of aglet proxies.
     */
    public Enumeration getAgletProxies() {
	return this._agletProxies.elements();
    }

    /**
     * Gets the aglet proxies in the current execution context.
     * 
     * @return an enumeration of aglet proxies.
     */
    public Enumeration getAgletProxies(int type) {
	synchronized (this._agletProxies) {
	    Vector v = new Vector();
	    Enumeration e = this._agletProxies.elements();

	    while (e.hasMoreElements()) {
		AgletProxy p = (AgletProxy) e.nextElement();

		if (p.isState(type)) {
		    v.addElement(p);
		}
	    }
	    return v.elements();
	}
    }

    /**
     * Gets the proxy for an aglet specified by its identity.
     * 
     * @param aid
     *            the identity of the aglet.
     * @return the aglet proxy.
     */
    public AgletProxy getAgletProxy(AgletID aid) {
	AgletProxy p = (AgletProxy) this._agletProxies.get(aid);

	if (p != null) {
	    return p;
	}
	try {
	    MAFFinder finder = MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();

	    if (finder != null) {
		String[] locations = finder.lookup_agent(MAFUtil.toName(aid, null), null);

		p = this.getAgletProxy(new URL(locations[0]), aid);
	    }
	} catch (EntryNotFound ex) {
	    p = null;
	} catch (MalformedURLException ex) {
	    ex.printStackTrace();
	    p = null;
	} catch (FinderNotFound ex) {
	    ex.printStackTrace();
	    p = null;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    p = null;
	}
	return p;
    }

    /**
     * Gets the proxy for a remote aglet specified by url
     * 
     * @param aid
     *            the identity of the aglet.
     * @return the aglet proxy
     * @deprecated
     */
    @Deprecated
    public AgletProxy getAgletProxy(URL host, AgletID aid) {
	try {

	    // REMIND: toString is not acculate
	    //
	    Ticket ticket = new Ticket(host);
	    AgletRef ref = RemoteAgletRef.getAgletRef(ticket, MAFUtil.toName(aid, null));

	    return new AgletProxyImpl(ref);
	} catch (Exception ex) {
	    return null;
	}
    }

    /**
	 * 
	 */
    public AudioClip getAudioClip(URL url) {

	/* NEW SECURITY */
	if ("file".equalsIgnoreCase(url.getProtocol())) {
	    this.checkPermission(new FilePermission(url.getFile(), "read"));
	} else {
	    String hostport = url.getHost() + ':' + url.getPort();

	    this.checkPermission(new SocketPermission(hostport, "connect"));
	}

	AudioClip c = (AudioClip) this.clips.get(url);

	if (c == null) {
	    InputStream in = null;

	    try {

		/*
		 * Fetch the url.
		 */
		URLConnection conn = url.openConnection();

		conn.setRequestProperty("user-agent", "Tahiti/Alpha5x");
		conn.setRequestProperty("agent-system", "aglets");
		conn.setAllowUserInteraction(true);
		conn.connect();
		in = conn.getInputStream();
		AudioData data = new AudioStream(in).getData();

		c = new AgletAudioClip(url, data);
	    } catch (IOException ex) {
		ex.printStackTrace();
		return null;
	    } finally {
		if (in != null) {
		    try {
			in.close();
		    } catch (IOException ex) {
		    }
		}
	    }
	    this.clips.put(url, c);
	}
	return c;
    }

    /**
     * Returns the URL of the daemon serving all current execution contexts.
     * 
     * @exception AgletException
     *                if the hosting URL cannot be determined.
     */
    public URL getHostingURL() {
	return this._hostingURL;
    }

    /**
	 * 
	 */
    public Image getImage(ImageData d) {
	ImageData data = d;
	Image img = (Image) this.images.get(data);

	if (img == null) {

	    img = Toolkit.getDefaultToolkit().createImage(data.getImageProducer());
	    this.images.put(data, img);
	}
	return img;
    }

    /**
	 * 
	 */
    public Image getImage(URL url) {
	Image img = (Image) this.images.get(url);

	if (img == null) {
	    img = Toolkit.getDefaultToolkit().getImage(url);
	    this.images.put(url, img);
	}
	return img;
    }

    /*
     * Multi Media support.
     */
    public ImageData getImageData(URL url) {
	InputStream in = null;

	try {
	    URLConnection conn = url.openConnection();

	    conn.setRequestProperty("user-agent", "Tahiti/Alpha5x");
	    conn.setRequestProperty("agent-system", "aglets");
	    conn.setAllowUserInteraction(true);
	    conn.connect();
	    in = conn.getInputStream();
	    String type = conn.getContentType();

	    int len = conn.getContentLength();

	    if (len < 0) {
		len = in.available();
	    }
	    byte[] b = new byte[len];

	    int off = 0;
	    int n = 0;

	    while (n < len) {
		int count = in.read(b, off + n, len - n);

		if (count < 0) {
		    throw new java.io.EOFException();
		}
		n += count;
	    }
	    in.close();

	    return new AgletImageData(url, b, type);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /**
     * Gets the name of the context
     * 
     * @return the name of the context
     */
    public String getName() {
	return this._name;
    }

    public Persistence getPersistence() throws IOException {
	if (this._persistence == null) {
	    new IOException("Persistency Service is not supported.");
	}
	return this._persistence;
    }

    /**
     * Gets the context property indicated by the key.
     * 
     * @param key
     *            the name of the context property.
     * @return the value of the specified key.
     */
    public Object getProperty(String key) {
	return this.getProperty(key, null);
    }

    /**
     * Gets the context property indicated by the key and default value.
     * 
     * @param key
     *            the name of the context property.
     * @param def
     *            the value to use if this property is not set.
     * @return the value of the specified key.
     */
    public Object getProperty(String key, Object def) {

	this.checkPermission(new ContextPermission("property." + key, "read"));

	Object r = this._contextProperties.get(key);

	return r == null ? def : r;
    }

    public ResourceManagerFactory getResourceManagerFactory() {
	return this._rm_factory;
    }

    boolean getSecurity() {
	return this._secure;
    }

    Object handleMessage(Message msg)
				     throws NotHandledException,
				     MessageException {
	if (msg.sameKind("createAglet")) {
	    Object codebase = msg.getArg("codebase");
	    Object classname = msg.getArg("classname");

	    // remote creator is anonymous user
	    Certificate owner = AgletRuntime.getAnonymousUserCertificate();
	    Object init = msg.getArg("init");

	    if (((codebase == null) || (codebase instanceof String))
		    && (classname instanceof String)) {
		try {
		    return this.createAglet(codebase == null ? null
			    : new URL((String) codebase), (String) classname, owner, init);
		} catch (Exception ex) {
		    throw new MessageException(ex, "createAglet failed due to: ");
		}
	    }
	    throw new MessageException(new IllegalArgumentException("createAglet"), "createAglet failed due to: ");
	} else if (msg.sameKind("getAgletProxies")) {
	    synchronized (this._agletProxies) {
		Vector tmp = new Vector();
		Enumeration e = this._agletProxies.elements();

		while (e.hasMoreElements()) {
		    AgletProxy p = (AgletProxy) e.nextElement();

		    tmp.addElement(p);
		}
		return tmp;
	    }
	}
	throw new NotHandledException("Message not handled: " + msg);
    }

    /*
     * Prints a message
     */
    void log(String kind, String msg) {
	this.postEvent(new ContextEvent(this, null, kind + " : " + msg, EventType.AGLET_MESSAGE), false);
    }

    /**
	 * 
	 */
    public ReplySet multicastMessage(Message msg) {
	this.checkPermission(new ContextPermission(msg.getKind(), "multicast"));
	return this._subscriberManager.multicastMessage(msg);
    }

    /*
     * To handle the aglet which doesn't respond to any request by the system.
     * Viewer may pops up the dialog to confirm that the system can dispose by
     * force.
     */
    boolean noResponseAglet(AgletProxy proxy) {
	this.postEvent(new ContextEvent(this, proxy, EventType.NO_REPONSE), true);
	return true;
    }

    public void postEvent(ContextEvent event, boolean sync) {
	if (sync) {
	    if (this.erunner != null) {
		this.erunner.sync();
	    }
	    this.postEvent0(event);
	} else {
	    if (this.erunner == null) {
		synchronized (this) {
		    if (this.erunner == null) {
			this.erunner = new EventRunner();
			this.erunner.start();
		    }
		}
	    }
	    this.erunner.postEvent(event);
	}
    }

    public void postEvent0(ContextEvent event) {

	// Update registration to Finder
	try {
	    MAFFinder finder = MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();

	    if (finder != null) {
		AgletProxyImpl p = (AgletProxyImpl) event.getAgletProxy();
		AgletRef ref0 = p.getAgletRef();

		if (ref0 instanceof LocalAgletRef) {
		    LocalAgletRef ref = (LocalAgletRef) p.getAgletRef();

		    // get the event type
		    EventType type = event.getEventType();

		    if (EventType.AGLET_CREATED.equals(type)
			    || EventType.AGLET_CLONED.equals(type)
			    || EventType.AGLET_ARRIVED.equals(type)) {
			try {
			    finder.register_agent(ref.getName(), this._hostingURL.toString(), MAF.toAgentProfile(ref.info));
			} catch (Exception ex) {
			    ex.printStackTrace();
			}
		    } else if (EventType.AGLET_DISPOSED.equals(type)
			    || EventType.AGLET_REVERTED.equals(type)) {

			try {
			    finder.unregister_agent(ref.getName());
			} catch (Exception ex) {
			    ex.printStackTrace();
			}
		    } else if (EventType.AGLET_DISPATCHED.equals(type)) {
			try {
			    finder.register_agent(ref.getName(), event.arg.toString(), MAF.toAgentProfile(ref.info));
			} catch (Exception ex) {
			    ex.printStackTrace();
			}
		    }
		}
	    }
	} catch (NullPointerException ex) {
	} catch (FinderNotFound ex) {

	    // ex.printStackTrace();
	}

	if (this.listeners == null) {
	    return;
	}

	// get the event type
	EventType type = event.getEventType();

	if (EventType.CONTEXT_STARTED.equals(type))
	    this.listeners.contextStarted(event);
	else if (EventType.CONTEXT_SHUTDOWN.equals(type))
	    this.listeners.contextShutdown(event);
	else if (EventType.AGLET_CREATED.equals(type))
	    this.listeners.agletCreated(event);
	else if (EventType.AGLET_CLONED.equals(type))
	    this.listeners.agletCloned(event);
	else if (EventType.AGLET_DISPOSED.equals(type))
	    this.listeners.agletDisposed(event);
	else if (EventType.AGLET_DISPATCHED.equals(type))
	    this.listeners.agletDispatched(event);
	else if (EventType.AGLET_REVERTED.equals(type))
	    this.listeners.agletReverted(event);
	else if (EventType.AGLET_ARRIVED.equals(type))
	    this.listeners.agletArrived(event);
	else if (EventType.AGLET_DEACTIVATED.equals(type))
	    this.listeners.agletDeactivated(event);
	else if (EventType.AGLET_ACTIVATED.equals(type))
	    this.listeners.agletActivated(event);
	else if (EventType.AGLET_STATE_CHANGED.equals(type))
	    this.listeners.agletStateChanged(event);
	else if (EventType.SHOW_DOCUMENT.equals(type))
	    this.listeners.showDocument(event);
	else if (EventType.AGLET_MESSAGE.equals(type))
	    this.listeners.showMessage(event);

    }

    /**
     * Receives an aglet. Will start the aglet and return its proxy.
     * 
     * @param aglet
     *            the aglet to be received by the context.
     * @exception AgletException
     *                if it is not received.
     */
    public void receiveAglet(
			     Name agent_name,
			     ClassName[] classnames,
			     String codebase,
			     byte[] agent,
			     String sender)
					   throws AgletException,
					   ClassNotFoundException {

	this.startCreation();
	try {
	    String authorityName = new String(agent_name.authority);

	    // this permission should be checked with context's privileges
	    this.checkPermission(new ContextPermission(authorityName, "receive"));

	    LocalAgletRef ref = new LocalAgletRef(this, this._secure);

	    ref.setName(agent_name);

	    AgletReader reader = new AgletReader(agent);

	    reader.readInfo(ref);

	    ref.createResourceManager(classnames);

	    reader.readAglet(ref);

	    ref.aglet.setStub(ref);
	    ref.proxy = new AgletProxyImpl(ref);
	    ref.startArrivedAglet(this, sender);

	    // com.ibm.awb.misc.Debug.check();

	    // / if (_finder != null) {
	    // / _finder.register_agent(ref.getName(),
	    // / _hostingURL.toString(),
	    // / MAF.toAgentProfile(ref.info));
	    // / }

	    String msg = "Receive : " + ref.info.getAgletClassName() + " from "
		    + sender;

	    this.postEvent(new ContextEvent(this, null, msg, EventType.AGLET_MESSAGE), false);

	} catch (java.io.NotSerializableException ex) {
	    ex.printStackTrace();
	    throw new AgletException("Incoming aglet is not serializable in this system "
		    + ex.getMessage());
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new AgletException("Failed to receive.. " + ex.getMessage());
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new AgletException("Failed to receive.. " + ex.getMessage());
	} finally {

	    // com.ibm.awb.misc.Debug.check();
	    this.endCreation();

	    // com.ibm.awb.misc.Debug.check();
	}
    }

    /*
     * Removes an aglet from the current context.
     */
    void removeAgletProxy(AgletID aid, AgletProxyImpl proxy) {

	// CRITICAL SESSION
	synchronized (this._agletProxies) {

	    //
	    // This is a hack to enable dispatching from one machine
	    // to itself. This must be improved.
	    //
	    if (proxy == this._agletProxies.get(aid)) {
		this._agletProxies.remove(aid);
	    }
	}
    }

    /**
     * Removes the specified context listener from the list.
     */
    public synchronized void removeContextListener(ContextListener listener) {
	// check args
	if (listener == null)
	    return;

	// check to see if the agent has the permission to remove the context
	// listener.
	if (REMOVE_LISTENER_PERMISSION == null) {
	    REMOVE_LISTENER_PERMISSION = new ContextPermission("listener", "remove");
	}

	this.checkPermission(REMOVE_LISTENER_PERMISSION);

	// if the listener list is null create a new one
	if (this.listeners == null)
	    this.listeners = new ListenerList();

	// now remove the specified listener
	if (this.listeners.contains(listener)) {
	    logger.debug("Removing the context listener " + listener);
	    this.listeners.remove(listener);
	}
    }

    public AgletProxy retractAglet(Ticket ticket, AgletID aid)
							      throws IOException,
							      AgletException {

	String destination = ticket.getDestination().toString();

	// - checkPermission(new ContextPermission("", "retract"));
	this.checkPermission(new ContextPermission(destination, "retract"));

	boolean success = false;

	try {
	    MAFAgentSystem _maf = MAFAgentSystem.getMAFAgentSystem(ticket);

	    if (_maf == null) {
		throw new ServerNotFoundException(ticket.toString());
	    }
	    Name name = MAFUtil.toName(aid, null);

	    byte[] agent = _maf.retract_agent(name);

	    AgletReader reader = new AgletReader(agent);

	    LocalAgletRef ref = new LocalAgletRef(this, this._secure);

	    // ref.setName(name);
	    // reader.readInfo(ref);
	    // ref.createResourceManager(null);// for the time being.
	    reader.readInfo(ref);
	    ref.setName(MAFUtil.toName(ref.info.getAgletID(), ref.info.getAuthorityCertificate()));
	    ref.createResourceManager(null);

	    reader.readAglet(ref);

	    ref.aglet.setStub(ref);
	    ref.proxy = new AgletProxyImpl(ref);
	    ref.startArrivedAglet(this, destination);

	    // / if (_finder != null) {
	    // / try {
	    // / _finder.register_agent(ref.getName(),
	    // / _hostingURL.toString(),
	    // / MAF.toAgentProfile(ref.info));
	    // / } catch (NameInvalid ex) {
	    // / ex.printStackTrace();
	    // / }
	    // / }

	    success = true;
	    return ref.proxy;
	} catch (ClassNotFoundException ex) {

	    // REMIND:
	    throw new AgletException("Fail to retract : " + ex.getMessage());

	} catch (UnknownHostException ex) {
	    throw new ServerNotFoundException(ticket.toString());

	} catch (IOException ex) {
	    throw new AgletException(ticket.toString());

	    /*
	     * MAF Exceptions
	     */
	} catch (AgentNotFound ex) {
	    throw new InvalidAgletException(ex.getMessage());

	} catch (MAFExtendedException ex) {
	    throw new AgletException(ex.getMessage());

	} finally {
	    if (success) {
		this.log("Retract", aid + " from " + ticket);
	    } else {
		this.log("Retract", "Fail to retract " + ticket);
	    }
	}
    }

    /**
     * Retracts the Aglet specified by its url:
     * scheme://host-domain-name/[user-name]#aglet-identity.
     * 
     * @param url
     *            the location and aglet identity of the aglet to be retracted.
     * @return the aglet proxy for the retracted aglet.
     * @exception AgletException
     *                when the method failed to retract the aglet.
     * @deprecated
     */
    @Deprecated
    public AgletProxy retractAglet(URL url) throws IOException, AgletException {
	return this.retractAglet(new Ticket(url), new AgletID(url.getRef()));
    }

    /**
     * Retracts the Aglet specified by its url:
     * scheme://host-domain-name/[user-name]#aglet-identity.
     * 
     * @param url
     *            the location and aglet identity of the aglet to be retracted.
     * @param aid
     *            the aglet identity of the aglet to be retracted.
     * @return the aglet proxy for the retracted aglet.
     * @exception AgletException
     *                when the method failed to retract the aglet.
     */
    public AgletProxy retractAglet(URL url, AgletID aid)
							throws IOException,
							AgletException {
	return this.retractAglet(new Ticket(url), aid);
    }

    public void setPersistence(Persistence p) throws AgletException {
	if (this._persistence != null) {
	    throw new AgletsSecurityException("Persistence already set");
	}
	this._persistence = p;
    }

    /**
     * Sets the context property
     */
    public void setProperty(String key, Object value) {
	this.checkPermission(new ContextPermission("property." + key, "write"));

	if (value == null) {
	    this._contextProperties.remove(key);
	} else {
	    this._contextProperties.put(key, value);
	}
    }

    /**
	 * 
	 */
    public void setResourceManagerFactory(ResourceManagerFactory rmf) {
	if (this._rm_factory != null) {
	    throw new AgletsSecurityException("Factory already set");
	}
	this._rm_factory = rmf;
    }

    void setSecurity(boolean secure) {
	this._secure = secure;
    }

    /**
     * Shows a new document. This may be ignored by the aglet context.
     * ContextPermission("showDocument", url) is required.
     * 
     * @param url
     *            an url to be shown
     */
    public void showDocument(URL url) {
	String urlstr = null;

	if (url != null) {
	    urlstr = url.toString();
	}
	this.checkPermission(new ContextPermission("showDocument", urlstr));
	this.postEvent(new ContextEvent(this, null, url, EventType.SHOW_DOCUMENT), false);
    }

    public void shutdown() {
	this.shutdown(new Message("shutdown"));
    }

    /*
	 * 
	 */
    public void shutdown(Message msg) {
	if (SHUTDOWN_PERMISSION == null) {
	    SHUTDOWN_PERMISSION = new ContextPermission("context", "shutdown");
	}
	this.checkPermission(SHUTDOWN_PERMISSION);

	this.shutting_down = true;

	this._timer.destroy();

	logger.info("shutting down.");
	synchronized (this.creationLock) {
	    while (this.creating > 0) {
		try {
		    this.creationLock.wait();
		} catch (InterruptedException ex) {
		}
	    }
	}

	Enumeration e = this._agletProxies.elements();
	ReplySet set = new ReplySet();

	while (e.hasMoreElements()) {
	    AgletProxy proxy = (AgletProxy) e.nextElement();

	    try {
		FutureReply f = proxy.sendAsyncMessage(msg);

		set.addFutureReply(f);
	    } catch (InvalidAgletException ex) {
	    }
	}

	logger.debug("[waiting for response..]");

	while (set.hasMoreFutureReplies()) {
	    set.waitForNextFutureReply(5000);
	    if (set.isAnyAvailable()) {
		set.getNextFutureReply();
	    } else {
		System.err.println("[some of the aglets didn't respond...]");
		break;
	    }
	}

	logger.info("[terminating aglets.]");

	MAFFinder finder = null;

	try {
	    finder = MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();
	} catch (FinderNotFound ex) {
	    finder = null;
	}

	if (finder != null) {
	    try {
		finder.unregister_place(this._hostingURL.toString());
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	e = this._agletProxies.elements();
	while (e.hasMoreElements()) {
	    AgletProxyImpl ref = (AgletProxyImpl) e.nextElement();

	    try {
		if (ref.isActive()) {
		    ref.dispose();
		    if (finder != null) {
			LocalAgletRef r = (LocalAgletRef) ref.getAgletRef();

			if (finder != null) {
			    finder.unregister_agent(r.getName());
			}
		    }
		}
	    } catch (InvalidAgletException ex) {
	    } catch (EntryNotFound ex) {
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	Resource aglets_res = Resource.getResourceFor("aglets");

	aglets_res.save("Aglets");

	// save context property
	if (this._persistence != null) {
	    try {
		Properties p = (Properties) this._contextProperties.clone();

		e = p.keys();
		while (e.hasMoreElements()) {
		    String k = (String) e.nextElement();

		    if ((this._contextProperties.get(k) instanceof String) == false) {
			AgletRuntime.verboseOut("removing property :" + k);
			this._contextProperties.remove(k);
		    }
		}
		PersistentEntry entry = this._persistence.getEntry("properties-"
			+ this._name);

		if (entry == null) {
		    entry = this._persistence.createEntryWith("properties-"
			    + this._name);
		}
		OutputStream out = entry.getOutputStream();

		this._contextProperties.store(out, "ContextProperty/"
			+ this._name);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}

	this.postEvent(new ContextEvent(this, null, EventType.CONTEXT_SHUTDOWN), true);
    }

    /**
     * Starts
     */
    synchronized public void start() {
	this.start(true);
    }

    synchronized public void start(boolean reactivate) {
	if (START_PERMISSION == null) {
	    START_PERMISSION = new ContextPermission("context", "start");
	}
	this.checkPermission(START_PERMISSION);
	if (this.shutting_down == false) {
	    return;
	}

	this.shutting_down = false;

	String addr = MAFAgentSystem.getLocalMAFAgentSystem().getAddress();

	// URL url = AgletRuntime.getAgletRuntime().getServerURL();
	try {
	    URL url = new URL(addr);

	    this._hostingURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), '/' + this._name);
	} catch (MalformedURLException ex) {
	    logger.error(ex);
	}

	//
	// ResourceManagerFacotry is a mandatory and set the default object
	// if its empty
	//
	if (this._rm_factory == null) {
	    this._rm_factory = AgletRuntime.getDefaultResourceManagerFactory();
	}

	if (this._persistence == null) {
	    this._persistence = AgletRuntime.createPersistenceFor(this);
	}

	//
	// Persistence is not a mandatory
	//
	if (this._persistence != null) {
	    PersistentEntry entry = this._persistence.getEntry("properties-"
		    + this._name);

	    if (reactivate) {
		if (entry != null) {
		    try {
			InputStream in = entry.getInputStream();

			try {
			    this._contextProperties.load(in);
			} finally {
			    in.close();
			}
		    } catch (IOException ex) {
			ex.printStackTrace();
		    }
		}
		try {
		    this._timer.recoverTimer(this._persistence);
		} catch (AgletException ex) {
		    ex.printStackTrace();
		}
	    } else {
		logger.info("removing deactivated aglets in the context("
			+ this._name + ")");
		for (Enumeration e = this._persistence.entryKeys(); e.hasMoreElements();) {
		    String key = (String) e.nextElement();

		    if (!key.equals("properties-" + this._name)) {
			logger.debug("\t" + key);
			this._persistence.removeEntry(key);
		    }
		}
	    }
	}

	//
	// REMIND: here, context can start receiving aglets...
	// currently not....
	//
	this.postEvent(new ContextEvent(this, null, EventType.CONTEXT_STARTED), true);

	//
	// Timer
	//
	this._timer.start();

	//
	// Register this context to MAFFinder server
	//
	try {
	    MAFAgentSystem local = MAFAgentSystem.getLocalMAFAgentSystem();
	    MAFFinder finder = local.get_MAFFinder();

	    if (finder != null) {
		try {
		    String place_name = this._hostingURL.toString();

		    // place_name should be a canonical form? H.T.
		    finder.register_place(place_name, this._hostingURL.toString());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	} catch (FinderNotFound ex) {
	}
    }

    void startCreation() throws ShuttingDownException {
	synchronized (this.creationLock) {
	    if (this.shutting_down) {
		throw new ShuttingDownException();
	    }
	    this.creating++;
	}
    }
}
