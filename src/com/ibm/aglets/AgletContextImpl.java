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

import com.ibm.maf.*;

import com.ibm.aglet.*;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglet.util.ImageData;

// import com.ibm.awb.misc.DigestTable;
import com.ibm.awb.misc.Archive;
import com.ibm.awb.misc.Resource;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Permission;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.net.SocketPermission;
import java.io.FilePermission;
import com.ibm.aglets.security.AgletPermission;
import com.ibm.aglets.security.MessagePermission;
import com.ibm.aglets.security.ContextPermission;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;

import java.security.Identity;

import java.net.URL;
import java.net.URLConnection;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.ObjectOutput;
import java.io.ByteArrayOutputStream;

import java.lang.ClassNotFoundException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.SocketException;

import java.rmi.RemoteException;
import com.ibm.maf.NameInvalid;
import org.aglets.log.*;

/*
 * MM
 */
import java.awt.Image;
import java.awt.Toolkit;
import sun.audio.*;
import java.applet.AudioClip;

/**
 * The <tt>AgletContextImpl</tt> class is the execution context for running aglets.
 * It provides means for maintaining and managing running aglets in an
 * environment where the aglets are protected from each other and the host
 * system is secured against malicious aglets.
 * 
 * @version     1.20	$Date: 2009/07/27 10:31:41 $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */

final public class AgletContextImpl implements AgletContext {
    private static LogCategory logCategory = LogInitializer.getCategory("com.ibm.aglets.AgletContextImpl");
    
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
	 * Checks to see if it is possible to retrieve the aglet.
	 * public void revertAglet(AgletID aid, URL remoteURL, OutputStream out) throws IOException, InvalidAgletException {
	 * //
	 * // REMIND: here, we have to lock the proxy
	 * //
	 * return;
	 * }
	 */

	AgletID context_aid = new AgletID("00");

	/*
	 * 
	 */
	ContextListener listeners = null;

	EventRunner erunner = null;

	/* protected */

	class EventRunner extends Thread {
		Vector events = new Vector();
		boolean sync = false;

		EventRunner() {
			setPriority(6);
		}

		public void postEvent(ContextEvent event) {
			events.addElement(event);
		} 

		public void sync() {
			sync = true;
			synchronized (this) {
				ContextEvent event;

				while (events.size() > 0) {
					event = (ContextEvent)events.firstElement();
					events.removeElementAt(0);
					postEvent0(event);
				} 
				sync = false;
			} 
		} 

		public void run() {
			ContextEvent event;

			while (true) {
				synchronized (this) {
					while (sync || events.size() == 0) {
						try {
							wait(1500);
						} catch (Exception ex) {
							return;
						} 
					} 
					event = (ContextEvent)events.firstElement();
					events.removeElementAt(0);
				} 
				try {
					postEvent0(event);
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
	 * @param prop property list.
	 */

	/* package protected */
	AgletContextImpl(String name) {
		this(name, AgletRuntime.getAgletRuntime().isSecure());
	}
	AgletContextImpl(String name, boolean secure) {
		_name = name;
		_timer = new AgletTimer(this);
		setSecurity(secure);
	}
	/*
	 * Adds an aglet to the current context.
	 */
	void addAgletProxy(AgletID aid, 
					   AgletProxyImpl proxy) throws InvalidAgletException {

		// proxy.checkValidation();
		// REMIND: critical session
		_agletProxies.put(aid, proxy);
	}
	synchronized public void addContextListener(ContextListener o) {
		if (ADD_LISTENER_PERMISSION == null) {
			ADD_LISTENER_PERMISSION = new ContextPermission("listener", 
					"add");
		} 
		checkPermission(ADD_LISTENER_PERMISSION);
		if (listeners == null) {
			listeners = o;
			return;
		} 
		synchronized (listeners) {
			if (listeners instanceof ListenerList) {
				ListenerList tmp = (ListenerList)listeners;

				if (!tmp.contains(o)) {
					tmp.addElement(o);
				} 
			} else {
				ListenerList tmp = new ListenerList();

				tmp.addElement(listeners);
				tmp.addElement(o);
				listeners = tmp;
			} 
		} 
	}
	/*
	 * check permission
	 */

	// accessable in package
	void checkPermission(Permission p) {
		if (!_secure) {
			return;
		} 

		// ---- I don't make sense why doPricileged()
		// - 	final Permission perm = p;
		// - 	AccessController.doPrivileged(new PrivilegedAction() {
		// - 	    public Object run() {
		// - 		AccessController.checkPermission(perm);
		// - 		return null;
		// - 	    }
		// - 	});

		// System.out.println("permission="+String.valueOf(p));
		AccessController.checkPermission(p);
	}
	/**
	 * Clear the cache
	 */
	public void clearCache(URL codebase) {
		_rm_factory.clearCache(codebase, 
							   AgletRuntime.getCurrentCertificate());
	}
	/**
	 * Creates an instance of the specified aglet located at the specified URL.
	 * @param url the URL to load the aglet class from.
	 * @param name the aglet's class name.
	 * @return a newly instantiated and initialized Aglet.
	 * @exception ClassNotFoundException if the class was not found
	 * @exception InstantiationException if failed to instantiate the Aglet.
	 */
	public AgletProxy createAglet(URL url, String classname, Object init) 
			throws IOException, AgletException, ClassNotFoundException, 
				   InstantiationException {
		Certificate owner = AgletRuntime.getCurrentCertificate();

		return createAglet(url, classname, owner, init);
	}
	/**
	 * Creates an instance of the specified aglet located at the specified URL.
	 * @param url the URL to load the aglet class from.
	 * @param name the aglet's class name.
	 * @return a newly instantiated and initialized Aglet.
	 * @exception ClassNotFoundException if the class was not found
	 * @exception InstantiationException if failed to instantiate the Aglet.
	 */
	private AgletProxy createAglet(URL url, String classname, 
								   Certificate owner, 
								   Object init) throws IOException, 
								   AgletException, ClassNotFoundException, 
								   InstantiationException {

		// System.out.println("createAglet("+url+","+classname+","+owner.getName()+","+init+")");
		startCreation();
		try {

			// 
			// Converts URL to the destination url.
			// 
			if (url != null && url.getRef() != null) {
				log("Create", 
					"Fail to create an aglet \"" + classname + "\" from " 
					+ (url == null ? "Local" : url.toString()));
				throw new MalformedURLException("MalformedURL in createAglet:" 
												+ url);
			} 
			if (url == null) {
				url = _rm_factory.lookupCodeBaseFor(classname);

				// System.out.println("lookupCodeBaseFor("+classname+")="+String.valueOf(url));
				if (url == null) {
					throw new ClassNotFoundException(classname);
				} 
			} 
			String agletLocation = String.valueOf(url) + "@" + classname;

			checkPermission(new ContextPermission(agletLocation, "create"));
			Aglet aglet = null;
			LocalAgletRef ref = new LocalAgletRef(this, _secure);

			ref.setName(AgletRuntime.newName(owner));
			ref.info = new AgletInfo(MAFUtil.toAgletID(ref.getName()), 
									 classname, url, 
									 getHostingURL().toString(), 
									 System.currentTimeMillis(), 
									 Aglet.MAJOR_VERSION, 
									 Aglet.MINOR_VERSION, owner);
			ResourceManager rm = ref.createResourceManager(null);

			rm.setResourceManagerContext();
			try {
				aglet = (Aglet)rm.loadClass(classname).newInstance();
			} catch (ClassCastException ex) {
				log("Create", 
					"Fail to create an aglet \"" + classname + "\" from " 
					+ url);
				throw new InstantiationException("ClassCastException:" 
												 + classname + ":" 
												 + ex.getMessage());
			} catch (ClassNotFoundException ex) {
				log("Create", 
					"Fail to create an aglet \"" + classname + "\" from " 
					+ url);
				throw ex;
			} catch (InstantiationException ex) {
				log("Create", 
					"Fail to create an aglet \"" + classname + "\" from " 
					+ url);
				throw ex;
			} catch (IllegalAccessException ex) {
				log("Create", 
					"Fail to create an aglet \"" + classname + "\" from " 
					+ url);
				throw new InstantiationException("IllegalAccessException:" 
												 + classname + ":" 
												 + ex.getMessage());
			} 
			finally {
				rm.unsetResourceManagerContext();
			} 

			// # 	    // get an allowance for the codebase at the url from policy DB file
			// # 	    ref.resetAllowance();

			// start
			aglet.setStub(ref);
			ref.proxy = new AgletProxyImpl(ref);
			ref.startCreatedAglet(this, init);
			log("Create", classname + " from " + url);

			// /	    if (_finder != null) {
			// /		try {
			// /		    _finder.register_agent(ref.getName(),
			// /					   _hostingURL.toString(),
			// /					   MAF.toAgentProfile(ref.info));
			// /		} catch (NameInvalid ex) {
			// /		    ex.printStackTrace();
			// /		}
			// /	    }

			return ref.proxy;
		} 
		finally {
			endCreation();
		} 
	}
	/**
	 * 
	 */
	synchronized ResourceManager createResourceManager(URL codebase, 
			Certificate owner, ClassName[] table) {
		return _rm_factory.createResourceManager(codebase, owner, table);
	}
	void endCreation() {
		synchronized (creationLock) {
			creating--;
			if (shutting_down) {
				creationLock.notify();
			} 
		} 
	}
	/**
	 * Gets the aglet proxies in the current execution context.
	 * @return an enumeration of aglet proxies.
	 */
	public Enumeration getAgletProxies() {
		return _agletProxies.elements();
	}
	/**
	 * Gets the aglet proxies in the current execution context.
	 * @return an enumeration of aglet proxies.
	 */
	public Enumeration getAgletProxies(int type) {
		synchronized (_agletProxies) {
			Vector v = new Vector();
			Enumeration e = _agletProxies.elements();

			while (e.hasMoreElements()) {
				AgletProxy p = (AgletProxy)e.nextElement();

				if (p.isState(type)) {
					v.addElement(p);
				} 
			} 
			return v.elements();
		} 
	}
	/**
	 * Gets the proxy for an aglet specified by its identity.
	 * @param aid the identity of the aglet.
	 * @return the aglet proxy.
	 */
	public AgletProxy getAgletProxy(AgletID aid) {
		AgletProxy p = (AgletProxy)_agletProxies.get(aid);

		if (p != null) {
			return p;
		} 
		try {
			MAFFinder finder = 
				MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();

			if (finder != null) {
				String[] locations = 
					finder.lookup_agent(MAFUtil.toName(aid, null), null);

				p = getAgletProxy(new URL(locations[0]), aid);
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
	 * @param aid the identity of the aglet.
	 * @return the aglet proxy
	 * @deprecated
	 */
	public AgletProxy getAgletProxy(URL host, AgletID aid) {
		try {

			// REMIND: toString is not acculate
			// 
			Ticket ticket = new Ticket(host);
			AgletRef ref = RemoteAgletRef.getAgletRef(ticket, 
													  MAFUtil.toName(aid, 
													  null));

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
			checkPermission(new FilePermission(url.getFile(), "read"));
		} else {
			String hostport = url.getHost() + ':' + url.getPort();

			checkPermission(new SocketPermission(hostport, "connect"));
		} 

		AudioClip c = (AudioClip)clips.get(url);

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
			} 
			finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ex) {}
				} 
			} 
			clips.put(url, c);
		} 
		return c;
	}
	/**
	 * Returns the URL of the daemon serving all current execution contexts.
	 * @exception AgletException if the hosting URL cannot be determined.
	 */
	public URL getHostingURL() {
		return _hostingURL;
	}
	/**
	 * 
	 */
	public Image getImage(ImageData d) {
		ImageData data = d;
		Image img = (Image)images.get(data);

		if (img == null) {

			img = 
				Toolkit.getDefaultToolkit()
					.createImage(data.getImageProducer());
			images.put(data, img);
		} 
		return img;
	}
	/**
	 * 
	 */
	public Image getImage(URL url) {
		Image img = (Image)images.get(url);

		if (img == null) {
			img = Toolkit.getDefaultToolkit().getImage(url);
			images.put(url, img);
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
	 * @return the name of the context
	 */
	public String getName() {
		return _name;
	}
	public Persistence getPersistence() throws IOException {
		if (_persistence == null) {
			new IOException("Persistency Service is not supported.");
		} 
		return _persistence;
	}
	/**
	 * Gets the context property indicated by the key.
	 * @param key the name of the context property.
	 * @return the value of the specified key.
	 */
	public Object getProperty(String key) {
		return getProperty(key, null);
	}
	/**
	 * Gets the context property indicated by the key and default value.
	 * @param key the name of the context property.
	 * @param def the value to use if this property is not set.
	 * @return the value of the specified key.
	 */
	public Object getProperty(String key, Object def) {

		checkPermission(new ContextPermission("property." + key, "read"));

		Object r = _contextProperties.get(key);

		return r == null ? def : r;
	}
	public ResourceManagerFactory getResourceManagerFactory() {
		return _rm_factory;
	}
	boolean getSecurity() {
		return _secure;
	}
	Object handleMessage(Message msg) 
			throws NotHandledException, MessageException {
		if (msg.sameKind("createAglet")) {
			Object codebase = msg.getArg("codebase");
			Object classname = msg.getArg("classname");

			// remote creator is anonymous user
			Certificate owner = AgletRuntime.getAnonymousUserCertificate();
			Object init = msg.getArg("init");

			if ((codebase == null || codebase instanceof String) 
					&& classname instanceof String) {
				try {
					return createAglet(codebase == null ? null 
									   : new URL((String)codebase), 
									   (String)classname, owner, init);
				} catch (Exception ex) {
					throw new MessageException(ex, 
											   "createAglet failed due to: ");
				} 
			} 
			throw new MessageException(new IllegalArgumentException("createAglet"), 
									   "createAglet failed due to: ");
		} else if (msg.sameKind("getAgletProxies")) {
			synchronized (_agletProxies) {
				Vector tmp = new Vector();
				Enumeration e = _agletProxies.elements();

				while (e.hasMoreElements()) {
					AgletProxy p = (AgletProxy)e.nextElement();

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
		postEvent(new ContextEvent(ContextEvent.MESSAGE, this, null, kind 
								   + " : " + msg), false);
	}
	/**
	 * 
	 */
	public ReplySet multicastMessage(Message msg) {
		checkPermission(new ContextPermission(msg.getKind(), "multicast"));
		return _subscriberManager.multicastMessage(msg);
	}
	/*
	 * To handle the aglet which doesn't respond to any request by the
	 * system. Viewer may pops up the dialog to confirm that the system
	 * can dispose by force.
	 */
	boolean noResponseAglet(AgletProxy proxy) {
		postEvent(new ContextEvent(ContextEvent.NO_RESPONSE, this, proxy), 
				  true);
		return true;
	}
	public void postEvent(ContextEvent event, boolean sync) {
		if (sync) {
			if (erunner != null) {
				erunner.sync();
			} 
			postEvent0(event);
		} else {
			if (erunner == null) {
				synchronized (this) {
					if (erunner == null) {
						erunner = new EventRunner();
						erunner.start();
					} 
				} 
			} 
			erunner.postEvent(event);
		} 
	}
	public void postEvent0(ContextEvent event) {

		// Update registration to Finder
		try {
			MAFFinder finder = 
				MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();

			if (finder != null) {
				AgletProxyImpl p = (AgletProxyImpl)event.getAgletProxy();
				AgletRef ref0 = p.getAgletRef();

				if (ref0 instanceof LocalAgletRef) {
					LocalAgletRef ref = (LocalAgletRef)p.getAgletRef();

					switch (event.getID()) {
					case ContextEvent.CREATED:
					case ContextEvent.CLONED:
					case ContextEvent.ARRIVED:
						try {
							finder
								.register_agent(ref.getName(), 
												_hostingURL.toString(), 
												MAF.toAgentProfile(ref.info));
							break;
						} catch (Exception ex) {
							ex.printStackTrace();
						} 
					case ContextEvent.DISPOSED:
					case ContextEvent.REVERTED:
						try {
							finder.unregister_agent(ref.getName());
						} catch (Exception ex) {
							ex.printStackTrace();
						} 
						break;
					case ContextEvent.DISPATCHED:
						try {
							finder
								.register_agent(ref.getName(), 
												event.arg.toString(), 
												MAF.toAgentProfile(ref.info));
						} catch (Exception ex) {
							ex.printStackTrace();
						} 
						break;
					case ContextEvent.DEACTIVATED:
					case ContextEvent.ACTIVATED:
					case ContextEvent.SHUTDOWN:
					}
				} 
			} 
		} catch (NullPointerException ex) {}
		catch (FinderNotFound ex) {

			// ex.printStackTrace();
		} 

		if (listeners == null) {
			return;
		} 

		// synchronized(listeners) {
		switch (event.getID()) {
		case ContextEvent.STARTED:
			listeners.contextStarted(event);
			break;
		case ContextEvent.SHUTDOWN:
			listeners.contextShutdown(event);
			break;
		case ContextEvent.CREATED:
			listeners.agletCreated(event);
			break;
		case ContextEvent.CLONED:
			listeners.agletCloned(event);
			break;
		case ContextEvent.DISPOSED:
			listeners.agletDisposed(event);
			break;

		case ContextEvent.DISPATCHED:
			listeners.agletDispatched(event);
			break;

		case ContextEvent.REVERTED:
			listeners.agletReverted(event);
			break;
		case ContextEvent.ARRIVED:
			listeners.agletArrived(event);
			break;

		case ContextEvent.DEACTIVATED:
			listeners.agletDeactivated(event);
			break;
		case ContextEvent.ACTIVATED:
			listeners.agletActivated(event);
			break;

		case ContextEvent.STATE_CHANGED:
			listeners.agletStateChanged(event);
			break;

		case ContextEvent.SHOW_DOCUMENT:
			listeners.showDocument(event);
			break;

		case ContextEvent.MESSAGE:
			listeners.showMessage(event);
		}

		// }
	}
	/**
	 * Receives an aglet. Will start the aglet and return its proxy.
	 * @param aglet the aglet to be received by the context.
	 * @exception AgletException if it is not received.
	 */
	public void receiveAglet(Name agent_name, ClassName[] classnames, 
							 String codebase, byte[] agent, 
							 String sender) throws AgletException, 
							 ClassNotFoundException {

		startCreation();
		try {
			String authorityName = new String(agent_name.authority);

			// this permission should be checked with context's privileges
			checkPermission(new ContextPermission(authorityName, "receive"));

			LocalAgletRef ref = new LocalAgletRef(this, _secure);

			ref.setName(agent_name);

			AgletReader reader = new AgletReader(agent);

			reader.readInfo(ref);

			ref.createResourceManager(classnames);

			reader.readAglet(ref);

			ref.aglet.setStub(ref);
			ref.proxy = new AgletProxyImpl(ref);
			ref.startArrivedAglet(this, sender);

			// com.ibm.awb.misc.Debug.check();

			// /	    if (_finder != null) {
			// /		_finder.register_agent(ref.getName(),
			// /				       _hostingURL.toString(),
			// /				       MAF.toAgentProfile(ref.info));
			// /	    }

			String msg = "Receive : " + ref.info.getAgletClassName() 
						 + " from " + sender;

			postEvent(new ContextEvent(ContextEvent.MESSAGE, this, null, msg), 
					  false);

		} catch (java.io.NotSerializableException ex) {
			ex.printStackTrace();
			throw new AgletException("Incoming aglet is not serializable in this system " 
									 + ex.getMessage());
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AgletException("Failed to receive.. " 
									 + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new AgletException("Failed to receive.. " 
									 + ex.getMessage());
		} 
		finally {

			// com.ibm.awb.misc.Debug.check();
			endCreation();

			// com.ibm.awb.misc.Debug.check();
		} 
	}
	/*
	 * Removes an aglet from the current context.
	 */
	void removeAgletProxy(AgletID aid, AgletProxyImpl proxy) {

		// CRITICAL SESSION
		synchronized (_agletProxies) {

			// 
			// This is a hack to enable dispatching from one machine
			// to itself. This must be improved.
			// 
			if (proxy == _agletProxies.get(aid)) {
				_agletProxies.remove(aid);
			} 
		} 
	}
	synchronized public void removeContextListener(ContextListener o) {
		if (REMOVE_LISTENER_PERMISSION == null) {
			REMOVE_LISTENER_PERMISSION = new ContextPermission("listener", 
					"remove");
		} 
		checkPermission(REMOVE_LISTENER_PERMISSION);
		if (listeners == null) {
			return;
		} 

		synchronized (listeners) {
			if (listeners == o) {
				listeners = null;
			} else if (listeners != null 
					   && listeners instanceof ListenerList) {
				((ListenerList)listeners).removeElement(o);
			} 
		} 
	}
	public AgletProxy retractAglet(Ticket ticket, AgletID aid) 
			throws IOException, AgletException {

		String destination = ticket.getDestination().toString();

		// - 	checkPermission(new ContextPermission("", "retract"));
		checkPermission(new ContextPermission(destination, "retract"));

		boolean success = false;

		try {
			MAFAgentSystem _maf = MAFAgentSystem.getMAFAgentSystem(ticket);

			if (_maf == null) {
				throw new ServerNotFoundException(ticket.toString());
			} 
			Name name = MAFUtil.toName(aid, null);

			byte[] agent = _maf.retract_agent(name);

			AgletReader reader = new AgletReader(agent);

			LocalAgletRef ref = new LocalAgletRef(this, _secure);

			// ref.setName(name);
			// reader.readInfo(ref);
			// ref.createResourceManager(null);// for the time being.
			reader.readInfo(ref);
			ref.setName(MAFUtil.toName(ref.info.getAgletID(), 
									   ref.info.getAuthorityCertificate()));
			ref.createResourceManager(null);

			reader.readAglet(ref);

			ref.aglet.setStub(ref);
			ref.proxy = new AgletProxyImpl(ref);
			ref.startArrivedAglet(this, destination);

			// /	    if (_finder != null) {
			// /		try {
			// /		    _finder.register_agent(ref.getName(),
			// /					   _hostingURL.toString(),
			// /					   MAF.toAgentProfile(ref.info));
			// /		} catch (NameInvalid ex) {
			// /		    ex.printStackTrace();
			// /		}
			// /	    }

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

		} 
		finally {
			if (success) {
				log("Retract", aid + " from " + ticket);
			} else {
				log("Retract", "Fail to retract " + ticket);
			} 
		} 
	}
	/**
	 * Retracts the Aglet specified by its url:
	 * scheme://host-domain-name/[user-name]#aglet-identity.
	 * @param url the location and aglet identity of the aglet to be retracted.
	 * @return the aglet proxy for the retracted aglet.
	 * @exception AgletException when the method failed to retract the aglet.
	 * @deprecated
	 */
	public AgletProxy retractAglet(URL url) 
			throws IOException, AgletException {
		return retractAglet(new Ticket(url), new AgletID(url.getRef()));
	}
	/**
	 * Retracts the Aglet specified by its url:
	 * scheme://host-domain-name/[user-name]#aglet-identity.
	 * @param url the location and aglet identity of the aglet to be retracted.
	 * @param aid the aglet identity of the aglet to be retracted.
	 * @return the aglet proxy for the retracted aglet.
	 * @exception AgletException when the method failed to retract the aglet.
	 */
	public AgletProxy retractAglet(URL url, AgletID aid) 
			throws IOException, AgletException {
		return retractAglet(new Ticket(url), aid);
	}
	public void setPersistence(Persistence p) throws AgletException {
		if (_persistence != null) {
			throw new AgletsSecurityException("Persistence already set");
		} 
		_persistence = p;
	}
	/**
	 * Sets the context property
	 */
	public void setProperty(String key, Object value) {
		checkPermission(new ContextPermission("property." + key, "write"));

		if (value == null) {
			_contextProperties.remove(key);
		} else {
			_contextProperties.put(key, value);
		} 
	}
	/**
	 * 
	 */
	public void setResourceManagerFactory(ResourceManagerFactory rmf) {
		if (_rm_factory != null) {
			throw new AgletsSecurityException("Factory already set");
		} 
		_rm_factory = rmf;
	}
	void setSecurity(boolean secure) {
		_secure = secure;
	}
	/**
	 * Shows a new document. This may be ignored by the aglet context.
	 * ContextPermission("showDocument", url) is required.
	 * @param url an url to be shown
	 */
	public void showDocument(URL url) {
		String urlstr = null;

		if (url != null) {
			urlstr = url.toString();
		} 
		checkPermission(new ContextPermission("showDocument", urlstr));
		postEvent(new ContextEvent(ContextEvent
			.SHOW_DOCUMENT, this, null, url), false);
	}
	public void shutdown() {
		shutdown(new Message("shutdown"));
	}
	/*
	 * 
	 */
	public void shutdown(Message msg) {
		if (SHUTDOWN_PERMISSION == null) {
			SHUTDOWN_PERMISSION = new ContextPermission("context", 
														"shutdown");
		} 
		checkPermission(SHUTDOWN_PERMISSION);

		shutting_down = true;

		_timer.destroy();

		logCategory.info("shutting down.");
		synchronized (creationLock) {
			while (creating > 0) {
				try {
					creationLock.wait();
				} catch (InterruptedException ex) {}
			} 
		} 

		Enumeration e = _agletProxies.elements();
		ReplySet set = new ReplySet();

		while (e.hasMoreElements()) {
			AgletProxy proxy = (AgletProxy)e.nextElement();

			try {
				FutureReply f = proxy.sendAsyncMessage(msg);

				set.addFutureReply(f);
			} catch (InvalidAgletException ex) {}
		} 

		logCategory.debug("[waiting for response..]");

		while (set.hasMoreFutureReplies()) {
			set.waitForNextFutureReply(5000);
			if (set.isAnyAvailable()) {
				set.getNextFutureReply();
			} else {
				System.err.println("[some of the aglets didn't respond...]");
				break;
			} 
		} 

		logCategory.info("[terminating aglets.]");

		MAFFinder finder = null;

		try {
			finder = MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();
		} catch (FinderNotFound ex) {
			finder = null;
		} 

		if (finder != null) {
			try {
				finder.unregister_place(_hostingURL.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 

		e = _agletProxies.elements();
		while (e.hasMoreElements()) {
			AgletProxyImpl ref = (AgletProxyImpl)e.nextElement();

			try {
				if (ref.isActive()) {
					ref.dispose();
					if (finder != null) {
						LocalAgletRef r = (LocalAgletRef)ref.getAgletRef();

						if (finder != null) {
							finder.unregister_agent(r.getName());
						} 
					} 
				} 
			} catch (InvalidAgletException ex) {}
			catch (EntryNotFound ex) {}
			catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 

		Resource aglets_res = Resource.getResourceFor("aglets");

		aglets_res.save("Aglets");

		// save context property
		if (_persistence != null) {
			try {
				Properties p = (Properties)_contextProperties.clone();

				e = p.keys();
				while (e.hasMoreElements()) {
					String k = (String)e.nextElement();

					if ((_contextProperties.get(k) instanceof String) 
							== false) {
						AgletRuntime.verboseOut("removing property :" + k);
						_contextProperties.remove(k);
					} 
				} 
				PersistentEntry entry = _persistence.getEntry("properties-" 
						+ _name);

				if (entry == null) {
					entry = _persistence.createEntryWith("properties-" 
														 + _name);
				} 
				OutputStream out = entry.getOutputStream();

				_contextProperties.store(out, "ContextProperty/" + _name);
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		} 

		postEvent(new ContextEvent(ContextEvent.SHUTDOWN, this, null), true);
	}
	/**
	 * Starts
	 */
	synchronized public void start() {
		start(true);
	}
	synchronized public void start(boolean reactivate) {
		if (START_PERMISSION == null) {
			START_PERMISSION = new ContextPermission("context", "start");
		} 
		checkPermission(START_PERMISSION);
		if (shutting_down == false) {
			return;
		} 

		shutting_down = false;

		String addr = MAFAgentSystem.getLocalMAFAgentSystem().getAddress();

		// URL url = AgletRuntime.getAgletRuntime().getServerURL();
		try {
			URL url = new URL(addr);

			_hostingURL = new URL(url.getProtocol(), url.getHost(), 
								  url.getPort(), '/' + _name);
		} catch (MalformedURLException ex) {
			logCategory.error(ex);
		} 

		// 
		// ResourceManagerFacotry is a mandatory and set the default object
		// if its empty
		// 
		if (_rm_factory == null) {
			_rm_factory = AgletRuntime.getDefaultResourceManagerFactory();
		} 

		if (_persistence == null) {
			_persistence = AgletRuntime.createPersistenceFor(this);
		} 

		// 
		// Persistence is not a mandatory
		// 
		if (_persistence != null) {
			PersistentEntry entry = _persistence.getEntry("properties-" 
					+ _name);

			if (reactivate) {
				if (entry != null) {
					try {
						InputStream in = entry.getInputStream();

						try {
							_contextProperties.load(in);
						} 
						finally {
							in.close();
						} 
					} catch (IOException ex) {
						ex.printStackTrace();
					} 
				} 
				try {
					_timer.recoverTimer(_persistence);
				} catch (AgletException ex) {
					ex.printStackTrace();
				} 
			} else {
				logCategory.info("removing deactivated aglets in the context(" 
							 + _name + ")");
				for (Enumeration e = _persistence.entryKeys(); 
						e.hasMoreElements(); ) {
					String key = (String)e.nextElement();

					if (!key.equals("properties-" + _name)) {
						logCategory.debug("\t" + key);
						_persistence.removeEntry(key);
					} 
				} 
			} 
		} 

		// 
		// REMIND: here, context can start receiving aglets...
		// currently not....
		// 
		postEvent(new ContextEvent(ContextEvent.STARTED, this, null), true);

		// 
		// Timer
		// 
		_timer.start();

		// 
		// Register this context to MAFFinder server
		// 
		try {
			MAFAgentSystem local = MAFAgentSystem.getLocalMAFAgentSystem();
			MAFFinder finder = local.get_MAFFinder();

			if (finder != null) {
				try {
					String place_name = _hostingURL.toString();

					// place_name should be a canonical form? H.T.
					finder.register_place(place_name, _hostingURL.toString());
				} catch (Exception ex) {
					ex.printStackTrace();
				} 
			} 
		} catch (FinderNotFound ex) {}
	}
	void startCreation() throws ShuttingDownException {
		synchronized (creationLock) {
			if (shutting_down) {
				throw new ShuttingDownException();
			} 
			creating++;
		} 
	}
}
