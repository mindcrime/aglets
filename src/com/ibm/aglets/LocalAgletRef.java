package com.ibm.aglets;

/*
 * @(#)LocalAgletRef.java
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Hashtable;

import org.aglets.log.AgletsLogger;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletNotFoundException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.AgletStub;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.RequestRefusedException;
import com.ibm.aglet.ServerNotFoundException;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.event.AgletEvent;
import com.ibm.aglet.event.CloneEvent;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.PersistencyEvent;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.aglet.message.MessageManager;
import com.ibm.aglet.security.AgletProtection;
import com.ibm.aglet.security.MessageProtection;
import com.ibm.aglet.security.Protection;
import com.ibm.aglet.security.Protections;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglets.security.AgletPermission;
import com.ibm.aglets.security.ContextPermission;
import com.ibm.aglets.security.MessagePermission;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.awb.weakref.Ref;
import com.ibm.awb.weakref.VirtualRef;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.ClassName;
import com.ibm.maf.ClassUnknown;
import com.ibm.maf.DeserializationFailed;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.Name;

/**
 * Class LocalAgletRef is the implementation of AgletStub. The purpose of this
 * class is to provide a mechanism to control the aglet.
 * 
 * @version $Revision: 1.10 $ $Date: 2009/07/28 07:04:53 $ $Author: cat4hire $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */
final public class LocalAgletRef extends AgletStub implements AgletRef {

    static final int NOT_INITIALIZED = 0;
    static final int ACTIVE = Aglet.ACTIVE;
    static final int INACTIVE = Aglet.INACTIVE;
    static final int INVALID = 0x1 << 2;

    static final String CLASS_AGLET_PERMISSION = "com.ibm.aglets.security.AgletPermission";
    static final String CLASS_MESSAGE_PERMISSION = "com.ibm.aglets.security.MessagePermission";
    static final String CLASS_AGLET_PROTECTION = "com.ibm.aglet.security.AgletProtection";
    static final String CLASS_MESSAGE_PROTECTION = "com.ibm.aglet.security.MessageProtection";

    private static final String ACTION_CLONE = "clone";
    private static final String ACTION_DISPOSE = "dispose";
    private static final String ACTION_DISPATCH = "dispatch";
    private static final String ACTION_DEACTIVATE = "deactivate";
    private static final String ACTION_RETRACT = "retract";

    private static AgletsLogger logger = AgletsLogger.getLogger(LocalAgletRef.class.getName());

    /* package */
    private static AgentProfile _agent_profile = null;

    static {
	_agent_profile = new AgentProfile((short) 1, /* java */
	(short) 1, /* Aglets */
	"Aglets", (short) 0, /* Major */
	(short) 2, /* minor */
	(short) 1, /* serialization */
	null);
    }

    /* package */
    Aglet aglet = null;
    AgletInfo info = null;
    ResourceManager resourceManager = null;
    MessageManagerImpl messageManager = null;
    AgletProxyImpl proxy = null;

    // # /**
    // # * The allowance: availability of the aglet's resources; cloning and
    // hops
    // # */
    // # Allowance allowance = null;
    /**
     * The protections: permission collection about who can send what kind of
     * messages to the aglet
     */
    Protections protections = null;

    /* private */
    private Name _name = null; // MAF name
    private int _state = NOT_INITIALIZED;
    private boolean _hasSnapshot = false;
    private AgletContextImpl _context = null;
    private String _text = null; // legacy
    private boolean _secure = true;
    private Certificate _owner = null;
    private int _mode = -1;

    private Object lock = new Object(); // locker to synchronized

    private int num_of_trial_to_dispose = 0;

    /*
     * Reference Table.
     */
    static Hashtable local_ref_table = new Hashtable();

    static class RefKey {
	Name name;
	int hash = 0;

	RefKey(Name n) {
	    this.name = n;
	    for (byte element : n.identity) {
		this.hash += (this.hash * 37) + (int) element;
	    }
	}

	public int hashCode() {
	    return this.hash;
	}

	public boolean equals(Object obj) {
	    if (obj instanceof RefKey) {
		return equals(((RefKey) obj).name, this.name);
	    }
	    return false;
	}

	static public boolean equals(Name n1, Name n2) {
	    if ( /* n1.authority.length == n2.authority.length && */
	    (n1.identity.length == n2.identity.length)
		    && (n1.agent_system_type == n2.agent_system_type)) {

		// int l = n1.authority.length;
		// for(int i=0; i<l; i++) {
		// if (n1.authority[i] != n2.authority[i]) {
		// return false;
		// }
		// }
		int l = n1.identity.length;

		for (int i = 0; i < l; i++) {
		    if (n1.identity[i] != n2.identity[i]) {
			return false;
		    }
		}
		return true;
	    }
	    return false;
	}
    }

    /**
     * Creates an aglet reference
     */

    /* package */
    LocalAgletRef(AgletContextImpl cxt) {
	this(cxt, cxt.getSecurity());
    }

    LocalAgletRef(AgletContextImpl cxt, boolean secure) {
	this._context = cxt;
	this._secure = secure;
    }

    /* package */
    Object _clone() throws CloneNotSupportedException {
	try {
	    this._context.startCreation();
	} catch (ShuttingDownException ex) {
	    throw new CloneNotSupportedException("Shutting down");
	}
	synchronized (this.lock) {
	    boolean success = false;

	    try {
		this.checkValidation();
		this.checkActive();
		try {
		    this.dispatchEvent(new CloneEvent(CloneEvent.CLONING, this.proxy));
		} catch (SecurityException ex) {
		    throw ex;
		} catch (Exception ex) {
		    ex.printStackTrace();
		}

		// # // consume a room of cloning.
		// # // When the aglet has no available rooms of cloning,
		// # // raise an ExhaustedException.
		// # allowance.consumeRoomCloning();

		this.suspendMessageManager();
		LocalAgletRef clone_ref = new LocalAgletRef(this._context, this._secure);

		// Set the owner
		// Certificate owner = AgletRuntime.getCurrentCertificate();
		Certificate owner = this._owner;
		Name new_name = AgletRuntime.newName(owner);

		clone_ref.setName(new_name);

		// Set AgletInfo
		clone_ref.info = new AgletInfo(MAFUtil.toAgletID(new_name), this.info.getAgletClassName(), this.info.getCodeBase(), this._context.getHostingURL().toString(), System.currentTimeMillis(), this.info.getAPIMajorVersion(), this.info.getAPIMinorVersion(), owner);
		AgletWriter writer = new AgletWriter();

		writer.writeAglet(this);
		clone_ref.createResourceManager(writer.getClassNames());
		AgletReader reader = new AgletReader(writer.getBytes());

		reader.readAglet(clone_ref);
		Aglet clone = clone_ref.aglet;

		// # Allowance clone_allowance = null;
		// # final int roomHops = allowance.getRoomHops();
		// # final Lifetime lifetime = allowance.getLifeTime();
		// # if(allowance.isFiniteCloning()) {
		// # // transfer half of rooms
		// # final int roomCloning = allowance.getRoomCloning()/2;
		// # clone_allowance = new Allowance(0, roomHops, lifetime);
		// # allowance.transferRoomCloning(clone_allowance,
		// roomCloning);
		// # } else {
		// # clone_allowance = new Allowance(Room.INFINITE, roomHops,
		// lifetime);
		// # }
		// #
		// # clone_ref.allowance = clone_allowance;

		// because Protections is not Cloneable
		clone_ref.protections = cloneProtections(this.protections);

		// start
		clone.setStub(clone_ref);
		clone_ref.proxy = new AgletProxyImpl(clone_ref);
		clone_ref.startClonedAglet(this._context, this.proxy);
		success = true;
		return clone_ref.proxy;
	    } catch (ClassNotFoundException ex) {
		throw new CloneNotSupportedException("Class Not Found :"
			+ ex.getMessage());

		// } catch (ExhaustedException ex) {
		// throw new
		// CloneNotSupportedException("Available room of cloning was exhausted : "
		// + ex.getMessage());
	    } catch (IOException ex) {
		ex.printStackTrace();
		throw new CloneNotSupportedException("IO Exception :"
			+ ex.getMessage());
	    } catch (AgletException ex) {
		throw new CloneNotSupportedException("Aglet Exception :"
			+ ex.getMessage());
	    } catch (RuntimeException ex) {
		this.logger.error("Exception caught while processing a message", ex);
		throw ex;
	    } finally {
		this.resumeMessageManager();
		if (success) {
		    this._context.log("Clone", this.info.getAgletClassName());
		} else {
		    this._context.log("Clone", "Failed to clone the aglet ["
			    + this.info.getAgletClassName() + "]");
		}
		this.dispatchEvent(new CloneEvent(CloneEvent.CLONED, this.proxy));
		this._context.endCreation();
	    }
	}
    }

    /**
	 * 
	 * 
	 */
    public void activate() throws IOException, InvalidAgletException,
	    AgletNotFoundException, ShuttingDownException {

	this._context.startCreation();

	synchronized (this.lock) {
	    this.checkValidation();

	    if (this.isActive()) {
		this._context.endCreation();
		return;
	    }

	    // if (_mode == DeactivationInfo.SUSPENDED) {
	    // throw new
	    // AgletNotFoundException("Cannot activate the suspended aglet");
	    // }

	    String key = this.getPersistenceKey();

	    ObjectInputStream oin = null;
	    Persistence persistence = this._context.getPersistence();

	    try {
		if (this._mode == DeactivationInfo.SUSPENDED) {
		    this.messageManager.state = MessageManagerImpl.UNINITIALIZED;
		} else {

		    // - checkAgletPermissionAndProtection(ACTION_ACTIVATE);

		    PersistentEntry entry = persistence.getEntry(key);

		    oin = new ObjectInputStream(entry.getInputStream());

		    DeactivationInfo dinfo = (DeactivationInfo) oin.readObject();

		    this.setMessageManager((MessageManagerImpl) oin.readObject());

		    this._hasSnapshot = dinfo.isSnapshot();

		    ClassName[] classnames = (ClassName[]) oin.readObject();

		    byte[] agent = new byte[oin.readInt()];

		    oin.readFully(agent);

		    AgletReader reader = new AgletReader(agent);

		    reader.readInfo(this);

		    this.createResourceManager(classnames);

		    reader.readAglet(this);

		    this.aglet.setStub(this);

		    // this should already have proxy.
		}

		if (this._mode == DeactivationInfo.SUSPENDED) {
		    this.startResumedAglet();
		} else {
		    this.startActivatedAglet();
		}

		this._context.log("Activated", this.info.getAgletClassName());
		return;

	    } catch (IOException ex) {
		throw new AgletNotFoundException(key);

	    } catch (ClassNotFoundException ex) {

		// REMIND
		throw new AgletNotFoundException(key);

	    } catch (InvalidAgletException ex) {
		ex.printStackTrace();
		throw new AgletNotFoundException(key);
	    } finally {
		try {
		    try {
			if (oin != null) {
			    oin.close();
			}
		    } catch (IOException e) {
		    }

		    this._context._timer.removeInfo(key);
		    if (this._hasSnapshot == false) {
			persistence.removeEntry(key);
		    }
		} finally {
		    this._context.endCreation();
		}
	    }
	}
    }

    /* package protected */
    private static void addAgletRef(Name name, LocalAgletRef ref) {
	local_ref_table.put(new RefKey(name), ref);
    }

    /**
     * Returns that the protections can be set or not
     * 
     * @param protections
     *            collection of protections about who can send what kind of
     *            messages to the aglet
     */
    private boolean canSetProtections(PermissionCollection newprotections) {
	if (newprotections == null) {
	    return false;
	}

	// if(_protection==null) {
	// return true;
	// }
	//
	// // needed ?
	// Enumeration prots = newprotections.elements();
	// while(prots.hasMoreElements()) {
	// Permission protection = (Permission)prots.nextElement();
	// if(!protections.implies(new_protection)) {
	// return false;
	// }
	// }

	return true;
    }

    void checkActive() {
	if (this.isActive() == false) {
	    throw new AgletsSecurityException("");
	}
    }

    private void checkAgletPermission(String actions) {

	// System.out.println("checkPermission(new AgletPermission("+_identity.getName()+", "+actions+"))");
	this.checkPermission(new AgletPermission(AgletRuntime.getCertificateAlias(this._owner), actions));
    }

    private void checkAgletPermissionAndProtection(String actions) {
	this.checkAgletPermission(actions);
	this.checkAgletProtection(actions);
    }

    private void checkAgletProtection(String actions) {
	Certificate cert = AgletRuntime.getCurrentCertificate();

	// ?????????? What if cert is null????? (HT)
	if (cert != null) {

	    // System.out.println("checkProtection(new AgletProtection("+id.getName()+", "+actions+"))");
	    this.checkProtection(new AgletProtection(AgletRuntime.getCertificateAlias(cert), actions));
	}
    }

    private void checkMessagePermission(MessageImpl msg) {

	// System.out.println("checkPermission(msg.getPermission("+_identity.getName()+"))");
	Permission p = msg.getPermission(AgletRuntime.getCertificateAlias(this._owner));

	// System.out.println("checkPermission("+p.toString()+")");
	this.checkPermission(p);
    }

    private void checkMessagePermission(String actions) {

	// System.out.println("checkPermission(new MessagePermission("+_identity.getName()+", "+actions+"))");
	this.checkPermission(new MessagePermission(AgletRuntime.getCertificateAlias(this._owner), actions));
    }

    private void checkMessagePermissionAndProtection(MessageImpl msg) {
	this.checkMessagePermission(msg);
	this.checkMessageProtection(msg);
    }

    private void checkMessagePermissionAndProtection(String actions) {
	this.checkMessagePermission(actions);
	this.checkMessageProtection(actions);
    }

    private void checkMessageProtection(MessageImpl msg) {
	Certificate cert = AgletRuntime.getCurrentCertificate();

	// ??????What if cert is null????(HT)
	if (cert != null) {

	    // System.out.println("checkProtection(msg.getProtection("+id.getName()+"))");
	    Permission p = msg.getProtection(AgletRuntime.getCertificateAlias(cert));

	    // System.out.println("checkProtection("+p.toString()+")");
	    this.checkProtection(p);
	}
    }

    private void checkMessageProtection(String actions) {
	Certificate cert = AgletRuntime.getCurrentCertificate();

	// ??????What if cert is null????(HT)
	if (cert != null) {

	    // System.out.println("checkProtection(new MessageProtection("+id.getName()+", "+actions+"))");
	    this.checkProtection(new MessageProtection(AgletRuntime.getCertificateAlias(cert), actions));
	}
    }

    /*
     * check permission/protection
     */
    private void checkPermission(Permission p) {
	if (this._context != null) {
	    this._context.checkPermission(p);
	}
    }

    private void checkProtection(Permission p) {
	if (!this._secure) {
	    return;
	}

	logger.debug("protections=" + String.valueOf(this.protections));
	logger.debug("permission=" + String.valueOf(p));
	if ((this.protections != null)
		&& (this.protections.implies(p) == false)) {
	    SecurityException ex = new SecurityException(p.toString());

	    ex.printStackTrace();
	    throw ex;
	}
    }

    /*
     * Checks if the aglet ref is valid.
     */
    public void checkValidation() throws InvalidAgletException {
	if (!this.isValid()) {
	    throw new InvalidAgletException("Aglet is not valid");
	}
    }

    // # /*
    // # *
    // # */
    // # public void resetAllowance() {
    // # CodeSource cs = new CodeSource(info.getCodeBase(), null);
    // # Policy pol = null;
    // # try {
    // # AccessController.beginPrivileged();
    // # pol = Policy.getPolicy();
    // # } finally {
    // # AccessController.endPrivileged();
    // # }
    // # pol = (Policy)AccessController.doPrivileged(new PrivilegedAction() {
    // # public Object run() {
    // # return Policy.getPolicy();
    // # }
    // # });
    // # Permissions permissions = null;
    // # if(pol instanceof PolicyImpl) {
    // # PolicyImpl policy = (PolicyImpl)pol;
    // # permissions = policy.getPermissions(cs, null);
    // # }
    // # if(permissions!=null) {
    // # Enumeration perms = permissions.elements();
    // # while(allowance==null && perms.hasMoreElements()) {
    // # Object obj = perms.nextElement();
    // # if(obj instanceof ActivityPermission) {
    // # ActivityPermission perm = (ActivityPermission)obj;
    // # if(perm.isMatched("name")) {
    // # if(allowance==null) {
    // # allowance = perm.getAllowance();
    // # //+ } else {
    // # //+ allow.limit(perm.getAllowance());
    // # }
    // # }
    // # }
    // # }
    // # }
    // #
    // # if(allowance==null) {
    // # allowance = new Allowance(Room.INFINITE, Room.INFINITE,
    // Lifetime.UNLIMITED);
    // # }
    // # }

    /**
     * Clones the aglet ref. Note that the cloned aglet will get activated. If
     * you like to get cloned aglet which is not activated, throw ThreadDeath
     * exception in the onClone method.
     * 
     * @return the new aglet ref what holds cloned aglet.
     * @exception CloneNotSupportedException
     *                if the cloning fails.
     * @exception InvalidAgletException
     *                if the aglet is invalid.
     */
    protected Object clone() throws CloneNotSupportedException {

	/*
	 * TO AVOID SELF CKECKING : M.O checkPermission(new
	 * AgletPermission("this", ACTION_CLONE)); checkProtection(new
	 * AgletProtection("this", ACTION_CLONE));
	 */
	this.checkAgletPermissionAndProtection(ACTION_CLONE);
	return this._clone();
    }

    private MessageImpl cloneMessageAndCheck(Message msg, int type) {
	MessageImpl clone;

	if (msg instanceof SystemMessage) {
	    clone = (MessageImpl) msg;
	} else { // normal or delegate
	    clone = new MessageImpl(msg, null, type, System.currentTimeMillis());
	}
	this.checkMessagePermissionAndProtection(clone);
	return clone;
    }

    /**
	 * 
	 */
    private static Protections cloneProtections(Protections protections) {

	// because java.security.Permissions and java.security.Permission
	// are not Cloneable.
	if (protections == null) {
	    return null;
	}
	Enumeration prots = protections.elements();
	Protections ps = new Protections();

	while (prots.hasMoreElements()) {
	    Object obj = prots.nextElement();

	    if (obj instanceof AgletProtection) {
		AgletProtection ap = (AgletProtection) obj;
		String name = ap.getName();
		String actions = ap.getActions();
		AgletProtection nap = new AgletProtection(name, actions);

		ps.add(nap);
	    } else if (obj instanceof MessageProtection) {
		MessageProtection mp = (MessageProtection) obj;
		String name = mp.getName();
		String actions = mp.getActions();
		MessageProtection nmp = new MessageProtection(name, actions);

		ps.add(mp);
	    }
	}
	return ps;
    }

    /*
	 * 
	 */
    ResourceManager createResourceManager(ClassName[] table) {
	this.resourceManager = this._context.createResourceManager(this.info.getCodeBase(), this._owner, table);
	if (this.resourceManager == null) {
	    logger.error("invalid codebase:" + this.info.getCodeBase());
	}
	return this.resourceManager;
    }

    /**
     * Deactivate aglet till the specified date. The deactivated aglet are
     * stored in the aglet spool.
     * 
     * @param duration
     *            the duration to sleep in milliseconds.
     * @exception AgletEception
     *                if can not deactivate the aglet.
     */
    protected void deactivate(long duration) throws IOException {
	try {
	    this.checkActive();

	    /*
	     * TO AVOID SELF CKECKING : M.O checkPermission(new
	     * AgletPermission("this", ACTION_DEACTIVATE)); checkProtection(new
	     * AgletProtection("this", ACTION_DEACTIVATE));
	     */
	    this.checkAgletPermissionAndProtection(ACTION_DEACTIVATE);
	    this.deactivate(AgletThread.getCurrentMessage(), duration);
	} catch (InvalidAgletException excpt) {
	    throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " + excpt);
	} catch (RequestRefusedException excpt) {
	    throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " + excpt);
	}
    }

    /*
	 * 
	 */
    void deactivate(MessageImpl msg, long duaration) throws IOException,
	    InvalidAgletException, RequestRefusedException {

	synchronized (this.lock) {
	    this.checkValidation();

	    if (duaration < 0) {
		throw new IllegalArgumentException("minutes must be positive");
	    }

	    Persistence persistence = this._context.getPersistence();

	    if (persistence == null) {
		this._context.log("Deactivation", "Deactivation not implemneted in this environment");
		return;
	    }

	    try {
		this.dispatchEvent(new PersistencyEvent(PersistencyEvent.DEACTIVATING, this.proxy, duaration));
	    } catch (SecurityException ex) {
		throw ex;
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }

	    ObjectOutputStream out = null;

	    //
	    // Suspend all threads except for the current thread
	    //
	    this.suspendMessageManager();

	    // start
	    boolean success = false;

	    String key = this.getPersistenceKey();

	    try {
		long wakeupTime = duaration == 0 ? 0
			: System.currentTimeMillis() + duaration;
		PersistentEntry entry = persistence.createEntryWith(key);

		out = new ObjectOutputStream(entry.getOutputStream());

		DeactivationInfo dinfo = new DeactivationInfo(this._name, wakeupTime, key, DeactivationInfo.DEACTIVATED);

		this.writeDeactivatedAglet(out, dinfo);

		this._context._timer.add(dinfo);
		success = true;
	    } finally {
		if (success == false) {
		    try {
			persistence.removeEntry(key);
		    } catch (ThreadDeath t) {
			throw t;
		    } catch (Throwable ee) {

			// ignore
		    }
		    this.resumeMessageManager();
		    this._context.log("Deactivate", "Fail to save aglet ["
			    + key + "]");
		}
		if (out != null) {
		    try {
			out.flush();
			out.close();
		    } catch (IOException ex) {
		    }
		}
	    }

	    this._state = INACTIVE;
	    this._mode = DeactivationInfo.DEACTIVATED;

	    if ((msg != null) && (msg.future != null)) {
		msg.future.sendReplyIfNeeded(null);
	    }

	    // message manager is persistent and will be restored later
	    this.messageManager.deactivate();

	    this.terminateThreads();

	    this._hasSnapshot = false;
	    this.aglet = null;
	    try {
		this._context.log("Deactivate", key);
		this._context.postEvent(new ContextEvent(ContextEvent.DEACTIVATED, this._context, this.proxy), true);
	    } finally {
		this.resourceManager.disposeAllResources();
		this.resourceManager.stopThreadGroup();
	    }
	}
    }

    /**
     * Delegates a message to the ref.
     * 
     * @param msg
     *            a message to delegate
     * @exception InvalidAgletException
     *                if the aglet is not valid any longer.
     */
    public void delegateMessage(Message msg) throws InvalidAgletException {
	logger.debug("delegateMessage()++");
	synchronized (msg) {
	    if (((msg instanceof MessageImpl) == false)
		    || (((MessageImpl) msg).isDelegatable() == false)) {

		throw new IllegalArgumentException("The message cannot be delegated "
			+ msg);
	    }

	    MessageManagerImpl mng = this.messageManager;

	    this.checkValidation();

	    MessageImpl origin = (MessageImpl) msg;
	    MessageImpl clone = (MessageImpl) origin.clone();

	    this.checkMessagePermissionAndProtection(clone);

	    if (mng != null) {
		origin.disable(); // disable the message
		mng.postMessage(clone);
	    } else {
		origin.cancel("Message Manager not found "
			+ (this._state == INACTIVE ? "[inactive]" : ""));
	    }
	}
    }

    void destroyMessageManager() {

	// Debug.check();
	this.messageManager.destroy();

	// Debug.check();
    }

    // trip with Ticket
    protected void dispatch(Ticket ticket) throws IOException,
	    RequestRefusedException {
	try {
	    this.checkActive();

	    /*
	     * TO AVOID SELF CKECKING : M.O checkPermission(new
	     * AgletPermission("this", ACTION_DISPATCH)); checkProtection(new
	     * AgletProtection("this", ACTION_DISPATCH));
	     */
	    this.checkAgletPermissionAndProtection(ACTION_DISPATCH);
	    this.dispatch(AgletThread.getCurrentMessage(), ticket);
	} catch (InvalidAgletException ex) {
	    throw new AgletsSecurityException(ACTION_DISPATCH + " : " + ex);
	}
    }

    // trip with Ticket
    void dispatch(MessageImpl msg, Ticket ticket) throws IOException,
	    RequestRefusedException, InvalidAgletException {
	URL dest = ticket.getDestination();

	synchronized (this.lock) {
	    this.checkValidation();

	    //
	    // Converts URL to the destination ticket.
	    //
	    if ((dest.getRef() != null) && !"".equals(dest.getRef())) {
		throw new MalformedURLException("MalformedURL in dispatchAglet:"
			+ ticket);
	    }

	    try {
		this.dispatchEvent(new MobilityEvent(MobilityEvent.DISPATCHING, this.proxy, ticket));
	    } catch (SecurityException ex) {
		throw ex;
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }

	    this.suspendMessageManager();

	    boolean success = false;

	    try {
		MAFAgentSystem _maf = MAFAgentSystem.getMAFAgentSystem(ticket);

		if (_maf == null) {
		    throw new ServerNotFoundException(ticket.toString());
		}

		// # // consume a room of hops.
		// # // When the aglet has no available rooms of hops,
		// # // raise an ExhaustedException.
		// # allowance.consumeRoomHops();

		//
		// clean up the reference. REMIND: needs to be improved.
		//
		removeAgletRef(this._name, this);

		AgletWriter writer = new AgletWriter();

		writer.writeInfo(this);
		writer.writeAglet(this);

		// Name name = getName(); // ??
		byte[] agent = writer.getBytes();
		String place = dest.getFile();

		if (place.startsWith("/")) {
		    place = place.substring(1);
		}

		ClassName[] classnames = writer.getClassNames();
		String codebase = this.info.getCodeBase().toString();

		MAFAgentSystem local = MAFAgentSystem.getLocalMAFAgentSystem();

		_maf.receive_agent(this._name, _agent_profile, agent, place, classnames, codebase, local);

		success = true;

		// # } catch (ExhaustedException ex) {
		// # ex.printStackTrace();
		// # throw new
		// RequestRefusedException("Available room of hops was exhausted : "
		// + ex.getMessage());
	    } catch (ClassUnknown ex) {
		ex.printStackTrace();
		throw new RequestRefusedException(ticket + " "
			+ this.info.getAgletClassName());

	    } catch (DeserializationFailed ex) {
		throw new RequestRefusedException(ticket + " "
			+ this.info.getAgletClassName());

		/*
		 * } catch (RequestRefused ex) { throw new
		 * RequestRefusedException(ticket + " " +
		 * info.getAgletClassName());
		 */
	    } catch (MAFExtendedException ex) {

		ex.printStackTrace();
		throw new RequestRefusedException(ticket + " "
			+ this.info.getAgletClassName());
	    } finally {
		if (success == false) {
		    this.resumeMessageManager();
		    addAgletRef(this._name, this);
		    this._context.log("Dispatch", "Fail to dispatch "
			    + this.info.getAgletClassName() + " to " + ticket);

		}
	    }

	    this.invalidateReference();

	    RemoteAgletRef r_ref = RemoteAgletRef.getAgletRef(ticket, this._name);

	    r_ref.setAgletInfo(this.info);

	    AgletProxy new_proxy = new AgletProxyImpl(r_ref);

	    if ((msg != null) && (msg.future != null)) {
		msg.future.sendReplyIfNeeded(new_proxy);
	    }

	    this.removeSnapshot();

	    this.terminateThreads();

	    this.destroyMessageManager();

	    try {
		this._context.log("Dispatch", this.info.getAgletClassName()
			+ " to " + ticket.getDestination());
		this._context.postEvent(new ContextEvent(ContextEvent.DISPATCHED, this._context, new_proxy, ticket.getDestination()), true);
	    } finally {
		this.releaseResource();
	    }
	}
    }

    /*
     * dispatches
     */
    protected void dispatch(URL url) throws IOException,
	    RequestRefusedException {
	this.dispatch(new Ticket(url));
    }

    /*
     * Event
     */
    public void dispatchEvent(AgletEvent ev) {
	this.aglet.dispatchEvent(ev);
    }

    /**
     * Disposes the aglet.
     * 
     * @exception InvalidAgletException
     *                if the aglet is invalid.
     */
    protected void dispose() {
	try {
	    this.checkActive();

	    /*
	     * TO AVOID SELF CKECKING : M.O checkPermission(new
	     * AgletPermission("this", ACTION_DISPOSE)); checkProtection(new
	     * AgletProtection("this", ACTION_DISPOSE));
	     */
	    this.checkAgletPermissionAndProtection(ACTION_DISPOSE);
	    this.dispose(AgletThread.getCurrentMessage());
	} catch (InvalidAgletException excpt) {
	    throw new AgletsSecurityException(ACTION_DISPOSE + " : " + excpt);
	} catch (RequestRefusedException excpt) {
	    throw new AgletsSecurityException(ACTION_DISPOSE + " : " + excpt);
	}
    }

    void dispose(MessageImpl msg) throws InvalidAgletException,
	    RequestRefusedException {

	//
	// this is ad hoc
	//
	if ((this.num_of_trial_to_dispose > 2) && this.isValid()) {
	    this.disposeAnyway(msg);
	    return;
	}
	this.num_of_trial_to_dispose++;

	synchronized (this.lock) {
	    this.checkValidation();
	    try {
		this.aglet.onDisposing();
	    } finally {
		this.disposeAnyway(msg);
	    }
	}
    }

    private void disposeAnyway(MessageImpl msg) throws RequestRefusedException {
	this.suspendMessageManager();

	this.invalidateReference();

	if ((msg != null) && (msg.future != null)) {
	    msg.future.sendReplyIfNeeded(null);
	}

	this.removeSnapshot();

	this.terminateThreads();

	this.destroyMessageManager();

	try {
	    this._context.log("Dispose", this.info.getAgletClassName());
	    this._context.postEvent(new ContextEvent(ContextEvent.DISPOSED, this._context, this.proxy), true);
	} finally {

	    // Debug.check();
	    this.releaseResource();
	}
    }

    // # /**
    // # * Gets the allowance: availability of the aglet's resources.
    // # * @return an Allowance object
    // # */
    // # public Allowance getAllowance() {
    // # // checkActive();
    // # return allowance;
    // # }

    /**
     * Gets the address.
     * 
     * @return the current context address
     */
    public String getAddress() throws InvalidAgletException {
	AgletContext c = this._context;

	this.checkValidation();
	return c.getHostingURL().toString();
    }

    /**
     * Gets the aglet. If the aglet is access protected it will require the
     * right key to get access.
     * 
     * @return the aglet
     * @exception SecurityException
     *                if the current execution is not allowed.
     */
    public Aglet getAglet() throws InvalidAgletException {
	this.checkValidation();
	this.checkMessagePermissionAndProtection("access");
	return this.aglet;
    }

    /*
     * Gets the context
     */
    protected AgletContext getAgletContext() {
	this.checkActive();
	return this._context;
    }

    /**
     * Gets the information of the aglet
     * 
     * @return an AgletInfo object
     */
    public AgletInfo getAgletInfo() {
	return this.info;
    }

    /* pakcage protected */
    static LocalAgletRef getAgletRef(Name name) {
	return (LocalAgletRef) local_ref_table.get(new RefKey(name));
    }

    /**
     * Gets the Certificate of the aglet's class.
     * 
     * @return a Certificate
     */
    public Certificate getCertificate() throws InvalidAgletException {
	this.checkValidation();
	return this._owner;
    }

    /*
	 * 
	 */
    protected MessageManager getMessageManager() {
	this.checkActive();
	return this.messageManager;
    }

    /**
	 * 
	 */

    /* package */
    public Name getName() {
	return this._name;
    }

    /*
	 * 
	 */
    private String getPersistenceKey() {
	return this.info.getAgletID().toString();
    }

    /**
     * Gets the protections: permission collection about who can send what kind
     * of messages to the aglet
     * 
     * @return collection of protections about who can send what kind of
     *         messages to the aglet
     */
    protected PermissionCollection getProtections() {
	return this.protections;
    }

    public Ref getRef(VirtualRef vref) {

	// if (forwarding..)
	return this;
    }

    public String getRefClassName() {
	return "com.ibm.aglets.RemoteAgletRef";
    }

    boolean getSecurity() {
	return this._secure;
    }

    /*
     * Helpers
     */
    String getStateAsString() {
	switch (this._state) {
	case INVALID:
	    return "INVALID";
	case ACTIVE:
	    return "ACTIVE";
	case INACTIVE:
	    return "INACTIVE";
	default:
	    return "DEFAULT";
	}
    }

    /**
     * Gets the current content of the Aglet's message line.
     * 
     * @return the message line.
     */
    public String getText() {
	this.checkActive();
	return this._text == null ? "" : this._text;
    }

    void invalidateReference() {
	this.unsubscribeAllMessages();
	this._state = INVALID;
	this._context.removeAgletProxy(this.info.getAgletID(), this.proxy);
	removeAgletRef(this._name, this);
    }

    /**
     * Checks if it's valid or not.
     */
    public boolean isActive() {
	return this._state == ACTIVE;
    }

    /**
     * Checks if it's remote or not.
     */
    public boolean isRemote() {
	return false;
    }

    /**
     * Check the state
     */
    public boolean isState(int s) {
	return (this._state & s) != 0;
    }

    /**
     * Checks if it's valid or not.
     */
    public boolean isValid() {
	return (this._state == ACTIVE) || (this._state == INACTIVE);
    }

    protected void kill() {
	this.suspendMessageManager();
	switch (this._state) {
	case ACTIVE:
	    this.aglet = null;
	    break;
	case INACTIVE:
	    String key = this.getPersistenceKey();

	    this._context._timer.removeInfo(key);
	    try {
		this._context.getPersistence().removeEntry(key);
	    } catch (Exception ex) {
	    }
	    break;
	default:
	}

	this.invalidateReference();
	this.removeSnapshot();

	this.terminateThreads();

	this.destroyMessageManager();

	try {
	    this._context.log("Dispose", this.info.getAgletClassName());
	    this._context.postEvent(new ContextEvent(ContextEvent.DISPOSED, this._context, this.proxy), true);
	} finally {
	    this.releaseResource();
	}
    }

    /*
     * ===============================
     * 
     * See com.ibm.awb.weakref.Ref
     */
    public void referenced() {
    }

    /*
     * This method is supposed to be called only from synchronized block
     */
    void releaseResource() {

	this._context = null;
	this.aglet = null;
	this.messageManager = null;

	this.resourceManager.disposeAllResources();
	this.resourceManager.stopThreadGroup();
    }

    /* package protected */
    private static void removeAgletRef(Name name, LocalAgletRef ref) {
	if (local_ref_table.contains(ref)) {
	    local_ref_table.remove(new RefKey(name));

	    // exported_aglets.remove(id);
	}
    }

    /*
	 */
    void removeSnapshot() {
	if (this._hasSnapshot) {
	    this._hasSnapshot = false;
	    try {
		this._context.getPersistence().removeEntry(this.getPersistenceKey());
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    public void resume() throws AgletNotFoundException, InvalidAgletException,
	    ShuttingDownException {
	this._context.startCreation();

	synchronized (this.lock) {
	    this.checkValidation();

	    if (this.isActive()) {
		this._context.endCreation();
		return;
	    }

	    if (this._mode != DeactivationInfo.SUSPENDED) {
		throw new AgletNotFoundException("Cannot resume the deactivated aglet");
	    }

	    String key = this.getPersistenceKey();

	    try {
		this.messageManager.state = MessageManagerImpl.UNINITIALIZED;

		// this should already have proxy.
		this.startResumedAglet();

		this._context.log("Activated", this.info.getAgletClassName());
		return;

		/*
		 * } catch (IOException ex) { throw new
		 * AgletNotFoundException(key);
		 * 
		 * } catch (ClassNotFoundException ex) { // REMIND throw new
		 * AgletNotFoundException(key);
		 */
	    } catch (InvalidAgletException ex) {
		ex.printStackTrace();
		throw new AgletNotFoundException(key);
	    } finally {
		try {
		    this._context._timer.removeInfo(key);
		} finally {
		    this._context.endCreation();
		}
	    }
	}
    }

    /*
     * Resumes all threads and resume accepting incoming messages.
     */
    void resumeMessageManager() {
	this.messageManager.resume();
    }

    /* package */
    byte[] retract() throws MAFExtendedException {

	boolean success = false;
	String classname = this.info.getAgletClassName();

	try {
	    this.checkAgletPermissionAndProtection(ACTION_RETRACT);

	    Message m = new SystemMessage(Message.REVERT, null, SystemMessage.RETRACT_REQUEST);

	    FutureReply f = this.sendFutureMessage(m);

	    f.waitForReply(50000);

	    if (f.isAvailable()) {
		try {
		    f.getReply();
		} catch (MessageException ex) {
		    if (ex.getException() instanceof SecurityException) {
			throw (SecurityException) ex.getException();
		    } else {

			// why not successfull ? should not happen
			ex.printStackTrace();
		    }
		} catch (NotHandledException ex) {

		    // invalid..? check with proxy.checkValidation()...
		}
	    }
	    this.checkValidation();

	    AgletWriter writer = new AgletWriter();

	    writer.writeInfo(this);
	    writer.writeAglet(this);

	    byte[] agent = writer.getBytes();

	    this.invalidateReference();
	    this.removeSnapshot();

	    this.terminateThreads();

	    this.destroyMessageManager();

	    success = true;

	    this._context.postEvent(new ContextEvent(ContextEvent.REVERTED, this, this.proxy, null), true);
	    return agent;

	} catch (SecurityException ex) {
	    throw new MAFExtendedException(toMessage(ex));

	} catch (IOException ex) {
	    throw new MAFExtendedException(toMessage(ex));

	} catch (InvalidAgletException ex) {
	    throw new MAFExtendedException(toMessage(ex));

	} finally {
	    if (success) {
		this._context.log("Reverted", classname);

		this.releaseResource();
	    } else {
		this._context.log("Reverted", "Failed to revert " + classname);
		this.resumeMessageManager();
	    }
	}
    }

    /*
     * Sends a message. This posts a message into the message manager.
     * MessageManager controls concurrency and no synchronized modifier is
     * needed.
     * 
     * REMIND: The way to handle system messages have to be imporved.
     */
    public FutureReply sendFutureMessage(Message msg)
	    throws InvalidAgletException {

	//
	// Just for thread safety to avoid a message being posted
	// into destroyed messageManager. this must be improved.
	//
	FutureReplyImpl future = new FutureReplyImpl();

	this.sendFutureMessage(msg, future);
	return future;
    }

    /* protected */
    void sendFutureMessage(Message msg, FutureReplyImpl future)
	    throws InvalidAgletException {

	//
	// Just for thread safety to avoid a message being posted
	// into destroyed messageManager. this must be improved.
	//
	MessageManagerImpl mng = this.messageManager;

	this.checkValidation();

	MessageImpl clone = this.cloneMessageAndCheck(msg, Message.FUTURE);

	clone.future = future;
	mng.postMessage(clone);
    }

    /*
     * Sends a message in synchronous way.
     */
    public Object sendMessage(Message msg) throws MessageException,
	    InvalidAgletException, NotHandledException {

	//
	// Just for thread safety to avoid a message being posted
	// into destroyed messageManager. this must be improved.
	//
	MessageManagerImpl mng = this.messageManager;

	this.checkValidation();

	FutureReplyImpl future = new FutureReplyImpl();
	MessageImpl clone = this.cloneMessageAndCheck(msg, Message.SYNCHRONOUS);

	clone.future = future;
	mng.postMessage(clone);
	return future.getReply();
    }

    /*
     * Sends an oneway message. REMIND: IMPLEMENT!
     */
    public void sendOnewayMessage(Message msg) throws InvalidAgletException {

	//
	// Just for thread safety to avoid a message being posted
	// into destroyed messageManager. this must be improved.
	//
	MessageManagerImpl mng = this.messageManager;

	this.checkValidation();

	FutureReplyImpl future = new FutureReplyImpl();
	MessageImpl clone = this.cloneMessageAndCheck(msg, Message.ONEWAY);

	clone.future = future;
	mng.pushMessage(clone);
	return;
    }

    /*
     * @see com.ibm.aglet.AgletProxy#setStub
     */
    protected void setAglet(Aglet a) {
	if (a != null) {
	    new IllegalAccessError("Aglet canont be set twice");
	}
	this.aglet = a;
	final Class cls = this.aglet.getClass();
	ProtectionDomain domain = (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		return cls.getProtectionDomain();
	    }
	});

	// = ProtectionDomain domain = ProtectionDomain.getDomain(cls);

	// System.out.println("Class="+aglet.getClass());
	// System.out.println("ClaasLoader="+cls.getClassLoader());
	// System.out.println("ProtectionDomain="+String.valueOf(domain));

	if ((domain != null) && (this.protections == null)) {
	    PermissionCollection ps = domain.getPermissions();

	    if (ps != null) {
		Enumeration perms = ps.elements();

		while (perms.hasMoreElements()) {
		    Permission perm = (Permission) perms.nextElement();

		    if (perm instanceof Protection) {
			if (this.protections == null) {
			    this.protections = new Protections();
			}
			this.protections.add(perm);
		    }
		}
	    }

	    // System.out.println("protections="+String.valueOf(protections));
	}
    }

    /*
	 * 
	 */
    /* package synchronized */
    void setMessageManager(MessageManagerImpl impl) {
	this.messageManager = impl;
	this.messageManager.setAgletRef(this);
    }

    /* package */
    void setName(Name n) {
	this._name = n;
	this._owner = AgletRuntime.getCertificate(this._name.authority);

	// String authorityName = new String(_name.authority);
	// _identity = AgletRuntime.getIdentity(authorityName);
	// String name = _identity.getName();
	// if (!authorityName.equals(name)) {
	// System.err.println("Unknown authority '" + authorityName +
	// "'. Regard as '" + name + "'");
	// }
    }

    /**
     * Sets the protections: permission collection about who can send what kind
     * of messages to the aglet
     * 
     * @param protections
     *            collection of protections about who can send what kind of
     *            messages to the aglet
     */
    protected void setProtections(PermissionCollection newprotections) {
	if (this.canSetProtections(newprotections)) {

	    // only restriction can be done
	    Protections ps = new Protections();
	    Enumeration prots = newprotections.elements();

	    while (prots.hasMoreElements()) {
		Permission protection = (Permission) prots.nextElement();

		ps.add(protection);
	    }
	    this.protections = ps;
	} else {

	    // cannot moderate restriction
	    throw new IllegalArgumentException("cannot moderate protection");
	}
    }

    public void setRef(VirtualRef vref, ObjectInputStream s)
	    throws IOException, ClassNotFoundException {

	// never called.
	throw new RuntimeException("Should Not Called");
    }

    void setSecurity(boolean secure) {
	this._secure = secure;
    }

    /**
     * Sets/Shows a text.
     * 
     * @param text
     */
    protected void setText(String text) {
	this.checkActive();
	this._text = text;
	this._context.postEvent(new ContextEvent(ContextEvent.STATE_CHANGED, this._context, this.proxy, text), true);
    }

    /**
     * Checkpointing the snapshot of the aglet.
     * 
     * @exception IOException
     */
    protected void snapshot() throws IOException {
	synchronized (this.lock) {
	    this.checkActive();

	    ObjectOutputStream out = null;
	    Persistence persistence = this._context.getPersistence();

	    this.suspendMessageManager();

	    String key = this.getPersistenceKey();
	    boolean success = false;

	    try {
		PersistentEntry entry = persistence.createEntryWith(key);

		out = new ObjectOutputStream(entry.getOutputStream());

		this.writeDeactivatedAglet(out, new DeactivationInfo(this._name, -1, key, DeactivationInfo.DEACTIVATED));

		this._hasSnapshot = true;
		success = true;
	    } catch (IOException ex) {
		try {
		    persistence.removeEntry(key);
		} catch (Exception ee) {
		}
		throw ex;
	    } catch (RuntimeException ex) {
		try {
		    persistence.removeEntry(key);
		} catch (Exception ee) {
		}
		throw ex;
	    } finally {
		this.resumeMessageManager();
		if (success) {
		    this._context.log("Snapshot", key);
		} else {
		    this._context.log("Snapshot", "Fail to save snapshot for aglet ["
			    + key + "]");
		}
		if (out != null) {
		    try {
			out.close();
		    } catch (IOException ex) {
		    }
		}
	    }
	}
    }

    /**
     * Send events to the activated aglet.
     * 
     * @param cxt
     *            the aglet context in which the aglet activated
     * @exception AgletException
     *                if the activation fails.
     * @see Aglet#onActivation
     */
    void startActivatedAglet() throws InvalidAgletException {
	this._state = ACTIVE;

	this.messageManager.postMessage(new EventMessage(new PersistencyEvent(PersistencyEvent.ACTIVATION, this.proxy, 0)));
	this.messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

	this._context.postEvent(new ContextEvent(ContextEvent.ACTIVATED, this._context, this.proxy), true);

	this.resumeMessageManager();
    }

    /**
     * Activates the arrived aglet.
     * 
     * @param cxt
     *            the aglet context in which the aglet activated
     * @param sender
     *            url of the departure
     * @exception AgletException
     *                if the activation fails.
     * @see Aglet#onArrival
     */
    void startArrivedAglet(AgletContextImpl cxt, String sender)
	    throws InvalidAgletException {
	this.validate(cxt, ACTIVE);

	this.messageManager.postMessage(new EventMessage(new MobilityEvent(MobilityEvent.ARRIVAL, this.proxy, this._context.getHostingURL())));
	this.messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

	this._context.postEvent(new ContextEvent(ContextEvent.ARRIVED, cxt, this.proxy, sender), true);

	this.resumeMessageManager();
    }

    /**
     * Activates the cloned aglet.
     * 
     * @param cxt
     *            the aglet context in which the aglet activated
     * @param parent
     *            proxy to the original aglet
     * @exception AgletException
     *                if the activation fails.
     * @see Aglet#onClone
     */
    void startClonedAglet(AgletContextImpl cxt, AgletProxyImpl parent)
	    throws InvalidAgletException {
	this.validate(cxt, ACTIVE);

	this.messageManager.postMessage(new EventMessage(new CloneEvent(CloneEvent.CLONE, this.proxy)));
	this.messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

	this._context.postEvent(new ContextEvent(ContextEvent.CLONED, cxt, this.proxy, parent), true);

	this.resumeMessageManager();
    }

    /**
     * Initializes the aglet.
     * 
     * @param cxt
     *            the aglet context in which the aglet activated
     * @param init
     *            argumetns to be used in onCreation method.
     * @exception InvalidAgletException
     *                if the aglet is invalid.
     * @see Aglet#onCreation
     */
    void startCreatedAglet(AgletContextImpl cxt, Object init)
	    throws InvalidAgletException {
	this.validate(cxt, ACTIVE);

	this.messageManager = new MessageManagerImpl(this);

	this.messageManager.postMessage(new SystemMessage(SystemMessage.CREATE, init));
	this.messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));
	this._context.postEvent(new ContextEvent(ContextEvent.CREATED, cxt, this.proxy), true);

	this.startMessageManager();
    }

    /*
     * Start the aglet threads
     */
    void startMessageManager() {
	this.messageManager.start();
    }

    /**
     * Send events to the resumed aglet.
     * 
     * @param cxt
     *            the aglet context in which the aglet activated
     * @exception AgletException
     *                if the activation fails.
     * @see Aglet#onActivation
     */
    void startResumedAglet() throws InvalidAgletException {
	this._state = ACTIVE;

	// //messageManager.postMessage(new EventMessage(new
	// PersistencyEvent(PersistencyEvent.RESUME, proxy, 0)));
	this.messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

	this._context.postEvent(new ContextEvent(ContextEvent.RESUMED, this._context, this.proxy), true);

	this.resumeMessageManager();
    }

    // -- subscribe to a specific whiteboard.
    //
    protected void subscribeMessage(String kind) {
	synchronized (this.lock) {
	    this.checkActive();
	    this.checkPermission(new ContextPermission(kind, "subscribe"));
	    this._context._subscriberManager.subscribe(this, kind);
	}
    }

    /**
     * Suspends the agent for the specified number of millisecs. The suspension
     * works as follows: 1) the message manager is set as sleeping, thus it will
     * not process any message as it arrives, but it will only enqueue it. 2)
     * the current thread is suspended 3) the message manager is woke up
     * 
     * Please note that it is not necessary to force the message manager to
     * process another message, since we are currently in the processing cycle
     * of the current message.
     * 
     * @param duration
     *            the number of millisecs to suspend the agent for
     * @throws InvalidAgletException
     *             if the message manager is null or any other problem occurs
     */
    protected void suspend(long duration) throws InvalidAgletException {
	// check params
	if (this.messageManager == null)
	    throw new InvalidAgletException("The message manager is null!");

	try {
	    // first of all suspend the message manager, thus it will continue
	    // to enqueue messages but will not process them
	    this.messageManager.setSleeping(true);

	    // now suspend the thread for the duration specified
	    long suspendTime = System.currentTimeMillis();
	    while ((suspendTime + duration) > System.currentTimeMillis()) {
		Thread.currentThread().sleep(duration);
	    }

	    // now wake up the message manager
	    this.messageManager.setSleeping(false);

	} catch (InterruptedException e) {
	    logger.error("Exception caught while suspending the agent", e);
	    throw new InvalidAgletException(e);
	}

    }

    void suspend(MessageImpl msg, long duaration) throws InvalidAgletException,
	    RequestRefusedException {

	synchronized (this.lock) {
	    this.checkValidation();

	    if (duaration < 0) {
		throw new IllegalArgumentException("minutes must be positive");
	    }

	    //
	    // Suspend all threads except for the current thread
	    //
	    this.suspendMessageManager();

	    // start
	    String key = this.getPersistenceKey();

	    long wakeupTime = duaration == 0 ? 0 : System.currentTimeMillis()
		    + duaration;
	    DeactivationInfo dinfo = new DeactivationInfo(this._name, wakeupTime, key, DeactivationInfo.SUSPENDED);

	    this._context._timer.add(dinfo);

	    this._state = INACTIVE;
	    this._mode = DeactivationInfo.SUSPENDED;

	    if ((msg != null) && (msg.future != null)) {
		msg.future.sendReplyIfNeeded(null);
	    }

	    // message manager is persistent and will be restored later
	    this.messageManager.deactivate();

	    this.terminateThreads();

	    try {
		this._context.log("Suspend", key);
		this._context.postEvent(new ContextEvent(ContextEvent.SUSPENDED, this._context, this.proxy), true);
	    } finally {

		// resourceManager.disposeAllResources();
		// resourceManager.stopThreadGroup();
	    }
	}
    }

    /*
     * Retraction
     */
    void suspendForRetraction(Ticket ticket) throws InvalidAgletException {

	synchronized (this.lock) {
	    this.checkValidation();
	    try {
		this.dispatchEvent(new MobilityEvent(MobilityEvent.REVERTING, this.proxy, ticket));
	    } catch (SecurityException ex) {
		throw ex;
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    this.suspendMessageManager();
	}
    }

    /*
     * Suspends all threads and accepting new message
     */
    void suspendMessageManager() {
	this.messageManager.suspend();
    }

    void terminateThreads() {

	// Debug.check();
	this.resourceManager.stopAllThreads();
    }

    static String toMessage(Exception ex) {
	return ex.getClass().getName() + ':' + ex.getMessage();
    }

    public String toString() {
	// check if the aglet is active
	if (!this.isValid())
	    return "Aglet [ invalid ]";

	// build a buffer with all the information about this aglet proxy
	StringBuffer buffer = new StringBuffer(500);

	// the classname of the aglet
	buffer.append(this.info.getAgletClassName());

	// the status of the agent
	buffer.append("      Status: ");
	if (this._state == ACTIVE)
	    buffer.append(" active ");
	else
	    buffer.append(" inactive ");

	// the identifier of the agent
	buffer.append("      AgletID: ");
	buffer.append(this.info.getAgletID());

	// the code base of the agent
	buffer.append("      Codebase: ");
	buffer.append(this.info.getCodeBase());

	buffer.append("      ResourceManager: ");
	buffer.append(this.resourceManager.toString());

	// the owner of the agent
	buffer.append("      Owner: ");

	if (this._owner == null)
	    buffer.append(" anonymous ");
	else
	    buffer.append(((X509Certificate) this._owner).getSubjectDN().getName());

	return buffer.toString();
    }

    /**
     * A description of the proxy in HTML, useful for tooltip.
     * 
     * @return the html string.
     */
    public String toHTMLString() {
	// check if the aglet is active
	if (!this.isValid())
	    return "Aglet [ invalid ]";

	// build a buffer with all the information about this aglet proxy
	StringBuffer buffer = new StringBuffer(500);

	// the classname of the aglet
	buffer.append("<HTML>");
	buffer.append(this.info.getAgletClassName());

	// the status of the agent
	buffer.append("      Status: ");
	buffer.append("<B>");

	if (this._state == ACTIVE)
	    buffer.append(" active ");
	else
	    buffer.append(" inactive ");

	buffer.append("</B>");

	// the identifier of the agent
	buffer.append("<BR>");
	buffer.append("      AgletID: ");
	buffer.append("<B>");
	buffer.append(this.info.getAgletID());
	buffer.append("</B>");

	// the code base of the agent
	buffer.append("<BR>");
	buffer.append("      Codebase: ");
	buffer.append("<B>");
	buffer.append(this.info.getCodeBase());
	buffer.append("</B>");

	buffer.append("<BR>");
	buffer.append("      ResourceManager: ");
	buffer.append("<B>");
	buffer.append(this.resourceManager.toString());
	buffer.append("</B>");

	// the owner of the agent
	buffer.append("<BR>");
	buffer.append("      Owner: ");
	buffer.append("<B>");

	if (this._owner == null)
	    buffer.append(" anonymous ");
	else
	    buffer.append(((X509Certificate) this._owner).getSubjectDN().getName());

	buffer.append("</B>");
	buffer.append("</HTML>");
	return buffer.toString();
    }

    public void unreferenced() {
    }

    // -- unsubscribe from all whiteboards.
    //
    protected void unsubscribeAllMessages() {
	synchronized (this.lock) {
	    this.checkActive();
	    this._context._subscriberManager.unsubscribeAll(this);
	}
    }

    // -- unsubscribe from a specific whiteboard.
    //
    protected boolean unsubscribeMessage(String kind) {
	synchronized (this.lock) {
	    this.checkActive();
	    return this._context._subscriberManager.unsubscribe(this, kind);
	}
    }

    /*
     * This method is supposed to be called only once.
     */
    /* synchronized */
    void validate(AgletContextImpl context, int state)
	    throws InvalidAgletException {
	if (this.isValid()) {
	    throw new IllegalAccessError("Aglet is already validated");
	}
	this._state = state;
	this._context = context;
	this._context.addAgletProxy(this.info.getAgletID(), this.proxy);

	// see AgletReader
	addAgletRef(this._name, this);
    }

    private void writeDeactivatedAglet(ObjectOutputStream out,
	    DeactivationInfo dinfo) throws IOException {
	out.writeObject(dinfo);
	out.writeObject(this.messageManager);

	AgletWriter writer = new AgletWriter();

	writer.writeInfo(this);
	writer.writeAglet(this);

	// Class Table
	out.writeObject(writer.getClassNames());

	byte[] b = writer.getBytes();

	// Aglet
	out.writeInt(b.length);
	out.write(b);
    }

    /*
     * @see com.ibm.aglets.RemoteAgletRef#findRef
     */
    public void writeInfo(ObjectOutputStream s) throws IOException {
	s.writeObject(this._name);
	s.writeObject(this._context.getHostingURL().toString());
    }

}
