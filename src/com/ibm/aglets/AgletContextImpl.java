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

import net.sourceforge.aglets.log.AgletsLogger;
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
	class EventRunner extends Thread {
		Vector events = new Vector();
		boolean sync = false;

		EventRunner() {
			setPriority(6);
		}

		public void postEvent(final ContextEvent event) {
			events.addElement(event);
		}

		@Override
		public void run() {
			ContextEvent event;

			while (true) {
				synchronized (this) {
					while (sync || (events.size() == 0)) {
						try {
							this.wait(1500);
						} catch (final Exception ex) {
							return;
						}
					}
					event = (ContextEvent) events.firstElement();
					events.removeElementAt(0);
				}
				try {
					postEvent0(event);
				} catch (final Exception t) {
					t.printStackTrace();
				}
			}
		}

		public void sync() {
			sync = true;
			synchronized (this) {
				ContextEvent event;

				while (events.size() > 0) {
					event = (ContextEvent) events.firstElement();
					events.removeElementAt(0);
					postEvent0(event);
				}
				sync = false;
			}
		}
	}

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
	private final Hashtable _agletProxies = new Hashtable();

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
	private final Object creationLock = new Object();
	private int creating = 0;

	/*
	 * Checks to see if it is possible to retrieve the aglet. public void
	 * revertAglet(AgletID aid, URL remoteURL, OutputStream out) throws
	 * IOException, InvalidAgletException { // // REMIND: here, we have to lock
	 * the proxy // return; }
	 */

	private boolean shutting_down = true;

	AgletID context_aid = new AgletID("00");

	/**
	 * A list of context listeners.
	 */
	ListenerList listeners = null;

	/* protected */

	EventRunner erunner = null;

	private final Hashtable images = new Hashtable();

	private final Hashtable clips = new Hashtable();

	/**
	 * Creates an execution context for aglets.
	 * 
	 * @param name
	 *            name for the context.
	 */

	/* package protected */
	AgletContextImpl(final String name) {
		this(name, com.ibm.aglet.system.AgletRuntime.getAgletRuntime().isSecure());
	}

	AgletContextImpl(final String name, final boolean secure) {
		_name = name;
		_timer = new AgletTimer(this);
		setSecurity(secure);
	}

	/*
	 * Adds an aglet to the current context.
	 */
	void addAgletProxy(final AgletID aid, final AgletProxyImpl proxy)
	throws InvalidAgletException {

		// proxy.checkValidation();
		// REMIND: critical session
		_agletProxies.put(aid, proxy);
	}

	/**
	 * Adds the specified context listener to the listener lists, only if the
	 * listener is not already contained in the listener list and if the agent
	 * has the right permission.
	 */
	@Override
	public synchronized void addContextListener(final ContextListener listener) {
		// check params
		if (listener == null)
			return;

		// check if I've the permission to add a listener
		// First of all check if I've got a permission already built, otherwise
		// build the add listener permission type and store it.
		if (ADD_LISTENER_PERMISSION == null) {
			ADD_LISTENER_PERMISSION = new ContextPermission("listener", "add");
		}

		checkPermission(ADD_LISTENER_PERMISSION);

		// if the listener container is null create a new one
		if (listeners == null)
			listeners = new ListenerList();

		// now add the new element
		if (!listeners.contains(listener)) {
			logger.debug("Adding the context listener " + listener);
			listeners.add(listener);
		}
	}

	/**
	 * Checks the specified permission calling the access controller.
	 * 
	 * @param permission
	 *            the permission to check
	 */
	void checkPermission(final Permission permission) {
		// check arguments
		if ((!_secure) || (permission == null)) {
			return;
		}

		// check the permission
		AccessController.checkPermission(permission);
	}

	/**
	 * Clear the cache
	 */
	@Override
	public void clearCache(final URL codebase) {
		_rm_factory.clearCache(codebase, AgletRuntime.getCurrentCertificate());
	}

	/**
	 * Creates an instance of the specified aglet located at the specified URL.
	 * 
	 * @param url
	 *            the URL to load the aglet class from.
	 * @param classname
	 *            the aglet's class name.
	 * @param owner
	 *            the certificate of the aglet's owner
	 * @param init
	 *            an initialization object for the new aglet
	 * @return a newly instantiated and initialized Aglet.
	 * @exception ClassNotFoundException
	 *                if the class was not found
	 * @exception InstantiationException
	 *                if failed to instantiate the Aglet.
	 */
	private AgletProxy createAglet(
	                               URL url,
	                               final String classname,
	                               final Certificate owner,
	                               final Object init)
	throws IOException,
	AgletException,
	ClassNotFoundException,
	InstantiationException {

		// System.out.println("createAglet("+url+","+classname+","+owner.getName()+","+init+")");
		startCreation();
		try {

			//
			// Converts URL to the destination url.
			//
			if ((url != null) && (url.getRef() != null)) {
				log("Create", "Fail to create an aglet \"" + classname
						+ "\" from " + (url == null ? "Local" : url.toString()));
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
			final String agletLocation = String.valueOf(url) + "@" + classname;

			checkPermission(new ContextPermission(agletLocation, "create"));
			Aglet aglet = null;
			final LocalAgletRef ref = new LocalAgletRef(this, _secure);

			ref.setName(AgletRuntime.newName(owner));
			ref.info = new AgletInfo(MAFUtil.toAgletID(ref.getName()), classname, url, getHostingURL().toString(), System.currentTimeMillis(), Aglet.MAJOR_VERSION, Aglet.MINOR_VERSION, owner);
			final ResourceManager rm = ref.createResourceManager(null);

			rm.setResourceManagerContext();
			try {
				aglet = (Aglet) rm.loadClass(classname).newInstance();
			} catch (final ClassCastException ex) {
				log("Create", "Fail to create an aglet \"" + classname
						+ "\" from " + url);
				throw new InstantiationException("ClassCastException:"
						+ classname + ":" + ex.getMessage());
			} catch (final ClassNotFoundException ex) {
				log("Create", "Fail to create an aglet \"" + classname
						+ "\" from " + url);
				throw ex;
			} catch (final InstantiationException ex) {
				log("Create", "Fail to create an aglet \"" + classname
						+ "\" from " + url);
				throw ex;
			} catch (final IllegalAccessException ex) {
				log("Create", "Fail to create an aglet \"" + classname
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
			log("Create", classname + " from " + url);

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
			endCreation();
		}
	}

	/**
	 * Creates an instance of the specified aglet located at the specified URL.
	 * 
	 * @param url
	 *            the URL to load the aglet class from.
	 * @param classname
	 *            the aglet's class name.
	 * @param init
	 *            an initialization object for the new aglet
	 * @return a proxy to a newly instantiated and initialized Aglet.
	 * @exception ClassNotFoundException
	 *                if the class was not found
	 * @exception InstantiationException
	 *                if failed to instantiate the Aglet.
	 */
	@Override
	public AgletProxy createAglet(final URL url, final String classname, final Object init)
	throws IOException,
	AgletException,
	ClassNotFoundException,
	InstantiationException {
		final Certificate owner = AgletRuntime.getCurrentCertificate();

		return this.createAglet(url, classname, owner, init);
	}

	/**
	 * 
	 */
	synchronized ResourceManager createResourceManager(
	                                                   final URL codebase,
	                                                   final Certificate owner,
	                                                   final ClassName[] table) {
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
	 * 
	 * @return an enumeration of aglet proxies.
	 */
	@Override
	public Enumeration getAgletProxies() {
		return _agletProxies.elements();
	}

	/**
	 * Gets the aglet proxies in the current execution context.
	 * 
	 * @return an enumeration of aglet proxies.
	 */
	@Override
	public Enumeration getAgletProxies(final int type) {
		synchronized (_agletProxies) {
			final Vector v = new Vector();
			final Enumeration e = _agletProxies.elements();

			while (e.hasMoreElements()) {
				final AgletProxy p = (AgletProxy) e.nextElement();

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
	@Override
	public AgletProxy getAgletProxy(final AgletID aid) {
		AgletProxy p = (AgletProxy) _agletProxies.get(aid);

		if (p != null) {
			return p;
		}
		try {
			final MAFFinder finder = MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();

			if (finder != null) {
				final String[] locations = finder.lookup_agent(MAFUtil.toName(aid, null), null);

				p = this.getAgletProxy(new URL(locations[0]), aid);
			}
		} catch (final EntryNotFound ex) {
			p = null;
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
			p = null;
		} catch (final FinderNotFound ex) {
			ex.printStackTrace();
			p = null;
		} catch (final Exception ex) {
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
	@Override
	@Deprecated
	public AgletProxy getAgletProxy(final URL host, final AgletID aid) {
		try {

			// REMIND: toString is not acculate
			//
			final Ticket ticket = new Ticket(host);
			final AgletRef ref = RemoteAgletRef.getAgletRef(ticket, MAFUtil.toName(aid, null));

			return new AgletProxyImpl(ref);
		} catch (final Exception ex) {
			return null;
		}
	}

	/**
	 * 
	 */
	@Override
	public AudioClip getAudioClip(final URL url) {

		/* NEW SECURITY */
		if ("file".equalsIgnoreCase(url.getProtocol())) {
			checkPermission(new FilePermission(url.getFile(), "read"));
		} else {
			final String hostport = url.getHost() + ':' + url.getPort();

			checkPermission(new SocketPermission(hostport, "connect"));
		}

		AudioClip c = (AudioClip) clips.get(url);

		if (c == null) {
			InputStream in = null;

			try {

				/*
				 * Fetch the url.
				 */
				final URLConnection conn = url.openConnection();

				conn.setRequestProperty("user-agent", "Tahiti/Alpha5x");
				conn.setRequestProperty("agent-system", "aglets");
				conn.setAllowUserInteraction(true);
				conn.connect();
				in = conn.getInputStream();
				final AudioData data = new AudioStream(in).getData();

				c = new AgletAudioClip(url, data);
			} catch (final IOException ex) {
				ex.printStackTrace();
				return null;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (final IOException ex) {
					}
				}
			}
			clips.put(url, c);
		}
		return c;
	}

	/**
	 * Returns the URL of the daemon serving all current execution contexts.
	 * 
	 * @exception AgletException
	 *                if the hosting URL cannot be determined.
	 */
	@Override
	public URL getHostingURL() {
		return _hostingURL;
	}

	/**
	 * 
	 */
	@Override
	public Image getImage(final ImageData d) {
		final ImageData data = d;
		Image img = (Image) images.get(data);

		if (img == null) {

			img = Toolkit.getDefaultToolkit().createImage(data.getImageProducer());
			images.put(data, img);
		}
		return img;
	}

	/**
	 * 
	 */
	@Override
	public Image getImage(final URL url) {
		Image img = (Image) images.get(url);

		if (img == null) {
			img = Toolkit.getDefaultToolkit().getImage(url);
			images.put(url, img);
		}
		return img;
	}

	/*
	 * Multi Media support.
	 */
	@Override
	public ImageData getImageData(final URL url) {
		InputStream in = null;

		try {
			final URLConnection conn = url.openConnection();

			conn.setRequestProperty("user-agent", "Tahiti/Alpha5x");
			conn.setRequestProperty("agent-system", "aglets");
			conn.setAllowUserInteraction(true);
			conn.connect();
			in = conn.getInputStream();
			final String type = conn.getContentType();

			int len = conn.getContentLength();

			if (len < 0) {
				len = in.available();
			}
			final byte[] b = new byte[len];

			final int off = 0;
			int n = 0;

			while (n < len) {
				final int count = in.read(b, off + n, len - n);

				if (count < 0) {
					throw new java.io.EOFException();
				}
				n += count;
			}
			in.close();

			return new AgletImageData(url, b, type);
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the name of the context
	 * 
	 * @return the name of the context
	 */
	@Override
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
	 * 
	 * @param key
	 *            the name of the context property.
	 * @return the value of the specified key.
	 */
	@Override
	public Object getProperty(final String key) {
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
	@Override
	public Object getProperty(final String key, final Object def) {

		checkPermission(new ContextPermission("property." + key, "read"));

		final Object r = _contextProperties.get(key);

		return r == null ? def : r;
	}

	public ResourceManagerFactory getResourceManagerFactory() {
		return _rm_factory;
	}

	boolean getSecurity() {
		return _secure;
	}

	Object handleMessage(final Message msg)
	throws NotHandledException,
	MessageException {
		if (msg.sameKind("createAglet")) {
			final Object codebase = msg.getArg("codebase");
			final Object classname = msg.getArg("classname");

			// remote creator is anonymous user
			final Certificate owner = AgletRuntime.getAnonymousUserCertificate();
			final Object init = msg.getArg("init");

			if (((codebase == null) || (codebase instanceof String))
					&& (classname instanceof String)) {
				try {
					return this.createAglet(codebase == null ? null
							: new URL((String) codebase), (String) classname, owner, init);
				} catch (final Exception ex) {
					throw new MessageException(ex, "createAglet failed due to: ");
				}
			}
			throw new MessageException(new IllegalArgumentException("createAglet"), "createAglet failed due to: ");
		} else if (msg.sameKind("getAgletProxies")) {
			synchronized (_agletProxies) {
				final Vector tmp = new Vector();
				final Enumeration e = _agletProxies.elements();

				while (e.hasMoreElements()) {
					final AgletProxy p = (AgletProxy) e.nextElement();

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
	void log(final String kind, final String msg) {
		postEvent(new ContextEvent(this, null, kind + " : " + msg, EventType.AGLET_MESSAGE), false);
	}

	/**
	 * 
	 */
	@Override
	public ReplySet multicastMessage(final Message msg) {
		checkPermission(new ContextPermission(msg.getKind(), "multicast"));
		return _subscriberManager.multicastMessage(msg);
	}

	/*
	 * To handle the aglet which doesn't respond to any request by the system.
	 * Viewer may pops up the dialog to confirm that the system can dispose by
	 * force.
	 */
	boolean noResponseAglet(final AgletProxy proxy) {
		postEvent(new ContextEvent(this, proxy, EventType.NO_REPONSE), true);
		return true;
	}

	public void postEvent(final ContextEvent event, final boolean sync) {
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

	public void postEvent0(final ContextEvent event) {

		// Update registration to Finder
		try {
			final MAFFinder finder = MAFAgentSystem.getLocalMAFAgentSystem().get_MAFFinder();

			if (finder != null) {
				final AgletProxyImpl p = (AgletProxyImpl) event.getAgletProxy();
				final AgletRef ref0 = p.getAgletRef();

				if (ref0 instanceof LocalAgletRef) {
					final LocalAgletRef ref = (LocalAgletRef) p.getAgletRef();

					// get the event type
					final EventType type = event.getEventType();

					if (EventType.AGLET_CREATED.equals(type)
							|| EventType.AGLET_CLONED.equals(type)
							|| EventType.AGLET_ARRIVED.equals(type)) {
						try {
							finder.register_agent(ref.getName(), _hostingURL.toString(), MAF.toAgentProfile(ref.info));
						} catch (final Exception ex) {
							ex.printStackTrace();
						}
					} else if (EventType.AGLET_DISPOSED.equals(type)
							|| EventType.AGLET_REVERTED.equals(type)) {

						try {
							finder.unregister_agent(ref.getName());
						} catch (final Exception ex) {
							ex.printStackTrace();
						}
					} else if (EventType.AGLET_DISPATCHED.equals(type)) {
						try {
							finder.register_agent(ref.getName(), event.arg.toString(), MAF.toAgentProfile(ref.info));
						} catch (final Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		} catch (final NullPointerException ex) {
		} catch (final FinderNotFound ex) {

			// ex.printStackTrace();
		}

		if (listeners == null) {
			return;
		}

		// get the event type
		final EventType type = event.getEventType();

		if (EventType.CONTEXT_STARTED.equals(type))
			listeners.contextStarted(event);
		else if (EventType.CONTEXT_SHUTDOWN.equals(type))
			listeners.contextShutdown(event);
		else if (EventType.AGLET_CREATED.equals(type))
			listeners.agletCreated(event);
		else if (EventType.AGLET_CLONED.equals(type))
			listeners.agletCloned(event);
		else if (EventType.AGLET_DISPOSED.equals(type))
			listeners.agletDisposed(event);
		else if (EventType.AGLET_DISPATCHED.equals(type))
			listeners.agletDispatched(event);
		else if (EventType.AGLET_REVERTED.equals(type))
			listeners.agletReverted(event);
		else if (EventType.AGLET_ARRIVED.equals(type))
			listeners.agletArrived(event);
		else if (EventType.AGLET_DEACTIVATED.equals(type))
			listeners.agletDeactivated(event);
		else if (EventType.AGLET_ACTIVATED.equals(type))
			listeners.agletActivated(event);
		else if (EventType.AGLET_STATE_CHANGED.equals(type))
			listeners.agletStateChanged(event);
		else if (EventType.SHOW_DOCUMENT.equals(type))
			listeners.showDocument(event);
		else if (EventType.AGLET_MESSAGE.equals(type))
			listeners.showMessage(event);

	}

	/**
	 * Receives an aglet. Will start the aglet and return its proxy.
	 * 
	 * @param agent_name
	 *            the name of the aglet
	 * @param classnames
	 *            classes making up the received aglet
	 * @param codebase
	 *            identifier of the origin of the aglet
	 * @param agent
	 *            the serialized state of the received aglet
	 * @param sender
	 *            name of the user sending the aglet
	 * @exception AgletException
	 *                if it is not received.
	 */
	public void receiveAglet(
	                         final Name agent_name,
	                         final ClassName[] classnames,
	                         final String codebase,
	                         final byte[] agent,
	                         final String sender)
	throws AgletException,
	ClassNotFoundException {

		startCreation();
		try {
			final String authorityName = new String(agent_name.authority);

			// this permission should be checked with context's privileges
			checkPermission(new ContextPermission(authorityName, "receive"));

			final LocalAgletRef ref = new LocalAgletRef(this, _secure);

			ref.setName(agent_name);

			final AgletReader reader = new AgletReader(agent);

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

			final String msg = "Receive : " + ref.info.getAgletClassName() + " from "
			+ sender;

			postEvent(new ContextEvent(this, null, msg, EventType.AGLET_MESSAGE), false);

		} catch (final java.io.NotSerializableException ex) {
			ex.printStackTrace();
			throw new AgletException("Incoming aglet is not serializable in this system "
					+ ex.getMessage());
		} catch (final IOException ex) {
			ex.printStackTrace();
			throw new AgletException("Failed to receive.. " + ex.getMessage());
		} catch (final Exception ex) {
			ex.printStackTrace();
			throw new AgletException("Failed to receive.. " + ex.getMessage());
		} finally {

			// com.ibm.awb.misc.Debug.check();
			endCreation();

			// com.ibm.awb.misc.Debug.check();
		}
	}

	/*
	 * Removes an aglet from the current context.
	 */
	void removeAgletProxy(final AgletID aid, final AgletProxyImpl proxy) {

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

	/**
	 * Removes the specified context listener from the list.
	 */
	@Override
	public synchronized void removeContextListener(final ContextListener listener) {
		// check args
		if (listener == null)
			return;

		// check to see if the agent has the permission to remove the context
		// listener.
		if (REMOVE_LISTENER_PERMISSION == null) {
			REMOVE_LISTENER_PERMISSION = new ContextPermission("listener", "remove");
		}

		checkPermission(REMOVE_LISTENER_PERMISSION);

		// if the listener list is null create a new one
		if (listeners == null)
			listeners = new ListenerList();

		// now remove the specified listener
		if (listeners.contains(listener)) {
			logger.debug("Removing the context listener " + listener);
			listeners.remove(listener);
		}
	}

	public AgletProxy retractAglet(final Ticket ticket, final AgletID aid)
	throws IOException,
	AgletException {

		final String destination = ticket.getDestination().toString();

		// - checkPermission(new ContextPermission("", "retract"));
		checkPermission(new ContextPermission(destination, "retract"));

		boolean success = false;

		try {
			final MAFAgentSystem _maf = MAFAgentSystem.getMAFAgentSystem(ticket);

			if (_maf == null) {
				throw new ServerNotFoundException(ticket.toString());
			}
			final Name name = MAFUtil.toName(aid, null);

			final byte[] agent = _maf.retract_agent(name);

			final AgletReader reader = new AgletReader(agent);

			final LocalAgletRef ref = new LocalAgletRef(this, _secure);

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
		} catch (final ClassNotFoundException ex) {

			// REMIND:
			throw new AgletException("Fail to retract : " + ex.getMessage());

		} catch (final UnknownHostException ex) {
			throw new ServerNotFoundException(ticket.toString());

		} catch (final IOException ex) {
			throw new AgletException(ticket.toString());

			/*
			 * MAF Exceptions
			 */
		} catch (final AgentNotFound ex) {
			throw new InvalidAgletException(ex.getMessage());

		} catch (final MAFExtendedException ex) {
			throw new AgletException(ex.getMessage());

		} finally {
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
	 * 
	 * @param url
	 *            the location and aglet identity of the aglet to be retracted.
	 * @return the aglet proxy for the retracted aglet.
	 * @exception AgletException
	 *                when the method failed to retract the aglet.
	 * @deprecated
	 */
	@Override
	@Deprecated
	public AgletProxy retractAglet(final URL url) throws IOException, AgletException {
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
	@Override
	public AgletProxy retractAglet(final URL url, final AgletID aid)
	throws IOException,
	AgletException {
		return this.retractAglet(new Ticket(url), aid);
	}

	public void setPersistence(final Persistence p) throws AgletException {
		if (_persistence != null) {
			throw new AgletsSecurityException("Persistence already set");
		}
		_persistence = p;
	}

	/**
	 * Sets the context property
	 */
	@Override
	public void setProperty(final String key, final Object value) {
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
	public void setResourceManagerFactory(final ResourceManagerFactory rmf) {
		if (_rm_factory != null) {
			throw new AgletsSecurityException("Factory already set");
		}
		_rm_factory = rmf;
	}

	void setSecurity(final boolean secure) {
		_secure = secure;
	}

	/**
	 * Shows a new document. This may be ignored by the aglet context.
	 * ContextPermission("showDocument", url) is required.
	 * 
	 * @param url
	 *            an url to be shown
	 */
	public void showDocument(final URL url) {
		String urlstr = null;

		if (url != null) {
			urlstr = url.toString();
		}
		checkPermission(new ContextPermission("showDocument", urlstr));
		postEvent(new ContextEvent(this, null, url, EventType.SHOW_DOCUMENT), false);
	}

	public void shutdown() {
		this.shutdown(new Message("shutdown"));
	}

	/*
	 * 
	 */
	public void shutdown(final Message msg) {
		if (SHUTDOWN_PERMISSION == null) {
			SHUTDOWN_PERMISSION = new ContextPermission("context", "shutdown");
		}
		checkPermission(SHUTDOWN_PERMISSION);

		shutting_down = true;

		_timer.destroy();

		logger.info("shutting down.");
		synchronized (creationLock) {
			while (creating > 0) {
				try {
					creationLock.wait();
				} catch (final InterruptedException ex) {
				}
			}
		}

		Enumeration e = _agletProxies.elements();
		final ReplySet set = new ReplySet();

		while (e.hasMoreElements()) {
			final AgletProxy proxy = (AgletProxy) e.nextElement();

			try {
				final FutureReply f = proxy.sendAsyncMessage(msg);

				set.addFutureReply(f);
			} catch (final InvalidAgletException ex) {
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
		} catch (final FinderNotFound ex) {
			finder = null;
		}

		if (finder != null) {
			try {
				finder.unregister_place(_hostingURL.toString());
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		e = _agletProxies.elements();
		while (e.hasMoreElements()) {
			final AgletProxyImpl ref = (AgletProxyImpl) e.nextElement();

			try {
				if (ref.isActive()) {
					ref.dispose();
					if (finder != null) {
						final LocalAgletRef r = (LocalAgletRef) ref.getAgletRef();

						if (finder != null) {
							finder.unregister_agent(r.getName());
						}
					}
				}
			} catch (final InvalidAgletException ex) {
			} catch (final EntryNotFound ex) {
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		final Resource aglets_res = Resource.getResourceFor("aglets");

		aglets_res.save("Aglets");

		// save context property
		if (_persistence != null) {
			try {
				final Properties p = (Properties) _contextProperties.clone();

				e = p.keys();
				while (e.hasMoreElements()) {
					final String k = (String) e.nextElement();

					if ((_contextProperties.get(k) instanceof String) == false) {
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
				final OutputStream out = entry.getOutputStream();

				_contextProperties.store(out, "ContextProperty/"
						+ _name);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}

		postEvent(new ContextEvent(this, null, EventType.CONTEXT_SHUTDOWN), true);
	}

	/**
	 * Starts
	 */
	synchronized public void start() {
		this.start(true);
	}

	synchronized public void start(final boolean reactivate) {
		if (START_PERMISSION == null) {
			START_PERMISSION = new ContextPermission("context", "start");
		}
		checkPermission(START_PERMISSION);
		if (shutting_down == false) {
			return;
		}

		shutting_down = false;

		final String addr = MAFAgentSystem.getLocalMAFAgentSystem().getAddress();

		// URL url = AgletRuntime.getAgletRuntime().getServerURL();
		try {
			final URL url = new URL(addr);

			_hostingURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), '/' + _name);
		} catch (final MalformedURLException ex) {
			logger.error(ex);
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
			final PersistentEntry entry = _persistence.getEntry("properties-"
					+ _name);

			if (reactivate) {
				if (entry != null) {
					try {
						final InputStream in = entry.getInputStream();

						try {
							_contextProperties.load(in);
						} finally {
							in.close();
						}
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
				}
				try {
					_timer.recoverTimer(_persistence);
				} catch (final AgletException ex) {
					ex.printStackTrace();
				}
			} else {
				logger.info("removing deactivated aglets in the context("
						+ _name + ")");
				for (final Enumeration e = _persistence.entryKeys(); e.hasMoreElements();) {
					final String key = (String) e.nextElement();

					if (!key.equals("properties-" + _name)) {
						logger.debug("\t" + key);
						_persistence.removeEntry(key);
					}
				}
			}
		}

		//
		// REMIND: here, context can start receiving aglets...
		// currently not....
		//
		postEvent(new ContextEvent(this, null, EventType.CONTEXT_STARTED), true);

		//
		// Timer
		//
		_timer.start();

		//
		// Register this context to MAFFinder server
		//
		try {
			final MAFAgentSystem local = MAFAgentSystem.getLocalMAFAgentSystem();
			final MAFFinder finder = local.get_MAFFinder();

			if (finder != null) {
				try {
					final String place_name = _hostingURL.toString();

					// place_name should be a canonical form? H.T.
					finder.register_place(place_name, _hostingURL.toString());
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (final FinderNotFound ex) {
		}
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
