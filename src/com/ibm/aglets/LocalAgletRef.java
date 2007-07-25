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

import com.ibm.maf.AgentProfile;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.ClassName;
import com.ibm.maf.Name;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.DeserializationFailed;
import com.ibm.maf.ClassUnknown;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.AgletStub;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.FutureReply;
import com.ibm.aglet.Message;
import com.ibm.aglet.MessageManager;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletNotFoundException;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.MessageException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.RequestRefusedException;
import com.ibm.aglet.ServerNotFoundException;
import com.ibm.aglet.event.AgletEvent;
import com.ibm.aglet.event.CloneEvent;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.PersistencyEvent;
import com.ibm.aglet.system.ContextEvent;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.SocketException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.Debug;

import java.security.AccessController;
import java.security.ProtectionDomain;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import com.ibm.aglets.security.AgletPermission;
import com.ibm.aglets.security.MessagePermission;
import com.ibm.aglets.security.ContextPermission;
import com.ibm.aglets.security.Lifetime;
import com.ibm.aglets.security.PolicyImpl;

import com.ibm.aglet.security.Protections;
import com.ibm.aglet.security.Protection;
import com.ibm.aglet.security.AgletProtection;
import com.ibm.aglet.security.MessageProtection;

import com.ibm.awb.weakref.Ref;
import com.ibm.awb.weakref.VirtualRef;

import org.aglets.log.AgletsLogger;

/**
 * Class LocalAgletRef is the implementation of AgletStub. The purpose of
 * this class is to provide a mechanism to control the aglet.
 * 
 * @version    $Revision: 1.8 $ $Date: 2007/07/25 23:33:05 $ $Author: maxthomax $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */
final public class LocalAgletRef extends AgletStub implements AgletRef {

	static final int NOT_INITIALIZED = 0;
	static final int ACTIVE = Aglet.ACTIVE;
	static final int INACTIVE = Aglet.INACTIVE;
	static final int INVALID = 0x1 << 2;

	static final String CLASS_AGLET_PERMISSION = 
		"com.ibm.aglets.security.AgletPermission";
	static final String CLASS_MESSAGE_PERMISSION = 
		"com.ibm.aglets.security.MessagePermission";
	static final String CLASS_AGLET_PROTECTION = 
		"com.ibm.aglet.security.AgletProtection";
	static final String CLASS_MESSAGE_PROTECTION = 
		"com.ibm.aglet.security.MessageProtection";

	private static final String ACTION_CLONE = "clone";
	private static final String ACTION_DISPOSE = "dispose";
	private static final String ACTION_DISPATCH = "dispatch";
	private static final String ACTION_DEACTIVATE = "deactivate";
	private static final String ACTION_ACTIVATE = "activate";
	private static final String ACTION_RETRACT = "retract";
    
	private static AgletsLogger logger = new AgletsLogger(LocalAgletRef.class.getName());

	/* package */
	private static AgentProfile _agent_profile = null;

	static {
		_agent_profile = new AgentProfile((short)1,		/* java */
		(short)1,										/* Aglets */
		"Aglets", (short)0,								/* Major */
		(short)2,										/* minor */
		(short)1,										/* serialization */
		null);
	} 

	/* package */
	Aglet aglet = null;
	AgletInfo info = null;
	ResourceManager resourceManager = null;
	MessageManagerImpl messageManager = null;
	AgletProxyImpl proxy = null;

	// #     /**
	// #      * The allowance: availability of the aglet's resources; cloning and hops
	// #      */
	// #     Allowance allowance = null;
	/**
	 * The protections: permission collection about who can send what kind of
	 * messages to the aglet
	 */
	Protections protections = null;

	/* private */
	private Name _name = null;				// MAF name
	private int _state = NOT_INITIALIZED;
	private boolean _hasSnapshot = false;
	private AgletContextImpl _context = null;
	private String _text = null;			// legacy
	private boolean _secure = true;
	private Certificate _owner = null;
	private int _mode = -1;

	private Object lock = new Object();		// locker to synchronized

	private int num_of_trial_to_dispose = 0;

	/*
	 * Reference Table.
	 */
	static Hashtable local_ref_table = new Hashtable();

	static class RefKey {
		Name name;
		int hash = 0;

		RefKey(Name n) {
			name = n;
			for (int i = 0; i < n.identity.length; i++) {
				hash += (hash * 37) + (int)n.identity[i];
			} 
		}
		public int hashCode() {
			return hash;
		} 

		public boolean equals(Object obj) {
			if (obj instanceof RefKey) {
				return equals(((RefKey)obj).name, name);
			} 
			return false;
		} 

		static public boolean equals(Name n1, Name n2) {
			if (	/* n1.authority.length == n2.authority.length && */
			n1.identity.length == n2.identity.length 
			&& n1.agent_system_type == n2.agent_system_type) {

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
		_context = cxt;
		_secure = secure;
	}
	/* package */
	Object _clone() throws CloneNotSupportedException {
		try {
			_context.startCreation();
		} catch (ShuttingDownException ex) {
			throw new CloneNotSupportedException("Shutting down");
		} 
		synchronized (lock) {
			boolean success = false;

			try {
				checkValidation();
				checkActive();
				try {
					dispatchEvent(new CloneEvent(CloneEvent.CLONING, proxy));
				} catch (SecurityException ex) {
					throw ex;
				} catch (Exception ex) {
					ex.printStackTrace();
				} 

				// # 		// consume a room of cloning.
				// # 		// When the aglet has no available rooms of cloning,
				// # 		// raise an ExhaustedException.
				// # 		allowance.consumeRoomCloning();

				suspendMessageManager();
				LocalAgletRef clone_ref = new LocalAgletRef(_context, 
															this._secure);

				// Set the owner
				//Certificate owner = AgletRuntime.getCurrentCertificate();
				Certificate owner = _owner;
				Name new_name = AgletRuntime.newName(owner);

				clone_ref.setName(new_name);

				// Set AgletInfo
				clone_ref.info = 
					new AgletInfo(MAFUtil.toAgletID(new_name), 
								  info.getAgletClassName(), 
								  info.getCodeBase(), 
								  _context.getHostingURL().toString(), 
								  System.currentTimeMillis(), 
								  info.getAPIMajorVersion(), 
								  info.getAPIMinorVersion(), owner);
				AgletWriter writer = new AgletWriter();

				writer.writeAglet(this);
				clone_ref.createResourceManager(writer.getClassNames());
				AgletReader reader = new AgletReader(writer.getBytes());

				reader.readAglet(clone_ref);
				Aglet clone = clone_ref.aglet;

				// # 		Allowance clone_allowance = null;
				// # 		final int roomHops = allowance.getRoomHops();
				// # 		final Lifetime lifetime = allowance.getLifeTime();
				// # 		if(allowance.isFiniteCloning()) {
				// # 		    // transfer half of rooms
				// # 		    final int roomCloning = allowance.getRoomCloning()/2;
				// # 		    clone_allowance = new Allowance(0, roomHops, lifetime);
				// # 		    allowance.transferRoomCloning(clone_allowance, roomCloning);
				// # 		} else {
				// # 		    clone_allowance = new Allowance(Room.INFINITE, roomHops, lifetime);
				// # 		}
				// #
				// # 		clone_ref.allowance = clone_allowance;

				// because Protections is not Cloneable
				clone_ref.protections = cloneProtections(protections);

				// start
				clone.setStub(clone_ref);
				clone_ref.proxy = new AgletProxyImpl(clone_ref);
				clone_ref.startClonedAglet(_context, proxy);
				success = true;
				return clone_ref.proxy;
			} catch (ClassNotFoundException ex) {
				throw new CloneNotSupportedException("Class Not Found :" 
													 + ex.getMessage());

				// } catch (ExhaustedException ex) {
				// throw new CloneNotSupportedException("Available room of cloning was exhausted : " + ex.getMessage());
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new CloneNotSupportedException("IO Exception :" 
													 + ex.getMessage());
			} catch (AgletException ex) {
				throw new CloneNotSupportedException("Aglet Exception :" 
													 + ex.getMessage());
			} catch (RuntimeException ex) {
				throw ex;
			} 
			finally {
				resumeMessageManager();
				if (success) {
					_context.log("Clone", info.getAgletClassName());
				} else {
					_context.log("Clone", 
								 "Failed to clone the aglet [" 
								 + info.getAgletClassName() + "]");
				} 
				dispatchEvent(new CloneEvent(CloneEvent.CLONED, proxy));
				_context.endCreation();
			} 
		} 
	}
	/**
	 * 
	 * 
	 */
	public void activate() 
			throws IOException, InvalidAgletException, 
				   AgletNotFoundException, ShuttingDownException {

		_context.startCreation();

		synchronized (lock) {
			checkValidation();

			if (isActive()) {
				_context.endCreation();
				return;
			} 

			// if (_mode == DeactivationInfo.SUSPENDED) {
			// throw new AgletNotFoundException("Cannot activate the suspended aglet");
			// }

			String key = getPersistenceKey();

			ObjectInputStream oin = null;
			Persistence persistence = _context.getPersistence();

			try {
				if (_mode == DeactivationInfo.SUSPENDED) {
					messageManager.state = MessageManagerImpl.UNINITIALIZED;
				} else {

					// - 		    checkAgletPermissionAndProtection(ACTION_ACTIVATE);

					PersistentEntry entry = persistence.getEntry(key);

					oin = new ObjectInputStream(entry.getInputStream());

					DeactivationInfo dinfo = 
						(DeactivationInfo)oin.readObject();

					setMessageManager((MessageManagerImpl)oin.readObject());

					_hasSnapshot = dinfo.isSnapshot();

					ClassName[] classnames = (ClassName[])oin.readObject();

					byte[] agent = new byte[oin.readInt()];

					oin.readFully(agent);

					AgletReader reader = new AgletReader(agent);

					reader.readInfo(this);

					createResourceManager(classnames);

					reader.readAglet(this);

					aglet.setStub(this);

					// this should already have proxy.
				} 

				if (_mode == DeactivationInfo.SUSPENDED) {
					startResumedAglet();
				} else {
					startActivatedAglet();
				} 

				_context.log("Activated", info.getAgletClassName());
				return;

			} catch (IOException ex) {
				throw new AgletNotFoundException(key);

			} catch (ClassNotFoundException ex) {

				// REMIND
				throw new AgletNotFoundException(key);

			} catch (InvalidAgletException ex) {
				ex.printStackTrace();
				throw new AgletNotFoundException(key);
			} 
			finally {
				try {
					try {
						if (oin != null) {
							oin.close();
						} 
					} catch (IOException e) {}

					_context._timer.removeInfo(key);
					if (_hasSnapshot == false) {
						persistence.removeEntry(key);
					} 
				} 
				finally {
					_context.endCreation();
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
	 * @param newprotections (@link PermissionCollection} with protections listing who can send
	 * what kind of messages to the aglet
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
		if (isActive() == false) {
			throw new AgletsSecurityException("");
		} 
	}
	private void checkAgletPermission(String actions) {

		// System.out.println("checkPermission(new AgletPermission("+_identity.getName()+", "+actions+"))");
		checkPermission(new AgletPermission(AgletRuntime
			.getCertificateAlias(_owner), actions));
	}
	private void checkAgletPermissionAndProtection(String actions) {
		checkAgletPermission(actions);
		checkAgletProtection(actions);
	}
	private void checkAgletProtection(String actions) {
		Certificate cert = AgletRuntime.getCurrentCertificate();

		// ?????????? What if cert is null????? (HT)
		if (cert != null) {

			// System.out.println("checkProtection(new AgletProtection("+id.getName()+", "+actions+"))");
			checkProtection(new AgletProtection(AgletRuntime
				.getCertificateAlias(cert), actions));
		} 
	}
	private void checkMessagePermission(MessageImpl msg) {

		// System.out.println("checkPermission(msg.getPermission("+_identity.getName()+"))");
		Permission p = 
			msg.getPermission(AgletRuntime.getCertificateAlias(_owner));

		// System.out.println("checkPermission("+p.toString()+")");
		checkPermission(p);
	}
	private void checkMessagePermission(String actions) {

		// System.out.println("checkPermission(new MessagePermission("+_identity.getName()+", "+actions+"))");
		checkPermission(new MessagePermission(AgletRuntime
			.getCertificateAlias(_owner), actions));
	}
	private void checkMessagePermissionAndProtection(MessageImpl msg) {
		checkMessagePermission(msg);
		checkMessageProtection(msg);
	}
	private void checkMessagePermissionAndProtection(String actions) {
		checkMessagePermission(actions);
		checkMessageProtection(actions);
	}
	private void checkMessageProtection(MessageImpl msg) {
		Certificate cert = AgletRuntime.getCurrentCertificate();

		// ??????What if cert is null????(HT)
		if (cert != null) {

			// System.out.println("checkProtection(msg.getProtection("+id.getName()+"))");
			Permission p = 
				msg.getProtection(AgletRuntime.getCertificateAlias(cert));

			// System.out.println("checkProtection("+p.toString()+")");
			checkProtection(p);
		} 
	}
	private void checkMessageProtection(String actions) {
		Certificate cert = AgletRuntime.getCurrentCertificate();

		// ??????What if cert is null????(HT)
		if (cert != null) {

			// System.out.println("checkProtection(new MessageProtection("+id.getName()+", "+actions+"))");
			checkProtection(new MessageProtection(AgletRuntime
				.getCertificateAlias(cert), actions));
		} 
	}
	/*
	 * check permission/protection
	 */
	private void checkPermission(Permission p) {
		if (_context != null) {
			_context.checkPermission(p);
		} 
	}
	private void checkProtection(Permission p) {
		if (!_secure) {
			return;
		} 

		logger.debug("protections="+String.valueOf(protections));
		logger.debug("permission="+String.valueOf(p));
		if (protections != null && protections.implies(p) == false) {
			SecurityException ex = new SecurityException(p.toString());

			ex.printStackTrace();
			throw ex;
		} 
	}
	/*
	 * Checks if the aglet ref is valid.
	 */
	public void checkValidation() throws InvalidAgletException {
		if (!isValid()) {
			throw new InvalidAgletException("Aglet is not valid");
		} 
	}
	// #     /*
	// #      *
	// #      */
	// #     public void resetAllowance() {
	// # 	CodeSource cs = new CodeSource(info.getCodeBase(), null);
	// # 	Policy pol = null;
	// # 	try {
	// # 	    AccessController.beginPrivileged();
	// # 	    pol = Policy.getPolicy();
	// # 	} finally {
	// # 	    AccessController.endPrivileged();
	// # 	}
	// # 	pol = (Policy)AccessController.doPrivileged(new PrivilegedAction() {
	// # 	    public Object run() {
	// # 		return Policy.getPolicy();
	// # 	    }
	// # 	});
	// # 	Permissions permissions = null;
	// # 	if(pol instanceof PolicyImpl) {
	// # 	    PolicyImpl policy = (PolicyImpl)pol;
	// # 	    permissions = policy.getPermissions(cs, null);
	// # 	}
	// # 	if(permissions!=null) {
	// # 	    Enumeration perms = permissions.elements();
	// # 	    while(allowance==null && perms.hasMoreElements()) {
	// # 		Object obj = perms.nextElement();
	// # 		if(obj instanceof ActivityPermission) {
	// # 		    ActivityPermission perm = (ActivityPermission)obj;
	// # 		    if(perm.isMatched("name")) {
	// # 			if(allowance==null) {
	// # 			    allowance = perm.getAllowance();
	// # //+			} else {
	// # //+			    allow.limit(perm.getAllowance());
	// # 			}
	// # 		    }
	// # 		}
	// # 	    }
	// # 	}
	// #
	// # 	if(allowance==null) {
	// # 	    allowance = new Allowance(Room.INFINITE, Room.INFINITE, Lifetime.UNLIMITED);
	// # 	}
	// #     }

	/**
	 * Clones the aglet ref. Note that the cloned aglet will get activated.
	 * If you like to get cloned aglet which is not activated, throw
	 * ThreadDeath exception in the onClone method.
	 * 
	 * @return  the new aglet ref what holds cloned aglet.
	 * @exception CloneNotSupportedException if the cloning fails.
	 * @exception InvalidAgletException if the aglet is invalid.
	 */
	protected Object clone() throws CloneNotSupportedException {

		/*
		 * TO AVOID SELF CKECKING : M.O
		 * checkPermission(new AgletPermission("this", ACTION_CLONE));
		 * checkProtection(new AgletProtection("this", ACTION_CLONE));
		 */
		checkAgletPermissionAndProtection(ACTION_CLONE);
		return _clone();
	}
	private MessageImpl cloneMessageAndCheck(Message msg, int type) {
		MessageImpl clone;

		if (msg instanceof SystemMessage) {
			clone = (MessageImpl)msg;
		} else {	// normal or delegate
			clone = new MessageImpl(msg, null, type, 
									System.currentTimeMillis());
		} 
		checkMessagePermissionAndProtection(clone);
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
				AgletProtection ap = (AgletProtection)obj;
				String name = ap.getName();
				String actions = ap.getActions();
				AgletProtection nap = new AgletProtection(name, actions);

				ps.add(nap);
			} else if (obj instanceof MessageProtection) {
				MessageProtection mp = (MessageProtection)obj;
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
		resourceManager = _context.createResourceManager(info.getCodeBase(), 
				_owner, table);
		if (resourceManager == null) {
			logger.error("invalid codebase:" + info.getCodeBase());
		} 
		return resourceManager;
	}
	/**
	 * Deactivate aglet till the specified date. The deactivated aglet are
	 * stored in the aglet spool.
	 * @param duration the duration to sleep in milliseconds.
	 * @exception AgletEception if can not deactivate the aglet.
	 */
	protected void deactivate(long duration) throws IOException {
		try {
			checkActive();

			/*
			 * TO AVOID SELF CKECKING : M.O
			 * checkPermission(new AgletPermission("this", ACTION_DEACTIVATE));
			 * checkProtection(new AgletProtection("this", ACTION_DEACTIVATE));
			 */
			checkAgletPermissionAndProtection(ACTION_DEACTIVATE);
			deactivate(AgletThread.getCurrentMessage(), duration);
		} catch (InvalidAgletException excpt) {
			throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " 
											  + excpt);
		} catch (RequestRefusedException excpt) {
			throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " 
											  + excpt);
		} 
	}
	/*
	 * 
	 */
	void deactivate(MessageImpl msg, long duaration) 
			throws IOException, InvalidAgletException, 
				   RequestRefusedException {

		synchronized (lock) {
			checkValidation();

			if (duaration < 0) {
				throw new IllegalArgumentException("minutes must be positive");
			} 

			Persistence persistence = _context.getPersistence();

			if (persistence == null) {
				_context
					.log("Deactivation", 
						 "Deactivation not implemneted in this environment");
				return;
			} 

			try {
				dispatchEvent(new PersistencyEvent(PersistencyEvent
					.DEACTIVATING, proxy, duaration));
			} catch (SecurityException ex) {
				throw ex;
			} catch (Exception ex) {
				ex.printStackTrace();
			} 

			ObjectOutputStream out = null;

			// 
			// Suspend all threads except for the current thread
			// 
			suspendMessageManager();

			// start
			boolean success = false;

			String key = getPersistenceKey();

			try {
				long wakeupTime = duaration == 0 ? 0 
								  : System.currentTimeMillis() + duaration;
				PersistentEntry entry = persistence.createEntryWith(key);

				out = new ObjectOutputStream(entry.getOutputStream());

				DeactivationInfo dinfo = new DeactivationInfo(_name, 
						wakeupTime, key, DeactivationInfo.DEACTIVATED);

				writeDeactivatedAglet(out, dinfo);

				_context._timer.add(dinfo);
				success = true;
			} 
			finally {
				if (success == false) {
					try {
						persistence.removeEntry(key);
					} catch (ThreadDeath t) {
						throw t;
					} catch (Throwable ee) {

						// ignore
					} 
					resumeMessageManager();
					_context.log("Deactivate", 
								 "Fail to save aglet [" + key + "]");
				} 
				if (out != null) {
					try {
						out.flush();
						out.close();
					} catch (IOException ex) {}
				} 
			} 

			_state = INACTIVE;
			_mode = DeactivationInfo.DEACTIVATED;

			if (msg != null && msg.future != null) {
				msg.future.sendReplyIfNeeded(null);
			} 

			// message manager is persistent and will be restored later
			messageManager.deactivate();

			terminateThreads();

			_hasSnapshot = false;
			aglet = null;
			try {
				_context.log("Deactivate", key);
				_context
					.postEvent(new ContextEvent(ContextEvent
						.DEACTIVATED, _context, proxy), true);
			} 
			finally {
				resourceManager.disposeAllResources();
				resourceManager.stopThreadGroup();
			} 
		} 
	}
	/**
	 * Delegates a message to the ref.
	 * @param msg a message to delegate
	 * @exception InvalidAgletException if the aglet is not valid any longer.
	 */
	public void delegateMessage(Message msg) throws InvalidAgletException {
        logger.debug("delegateMessage()++");
		synchronized (msg) {
			if (msg instanceof MessageImpl == false 
					|| ((MessageImpl)msg).isDelegatable() == false) {

				throw new IllegalArgumentException("The message cannot be delegated " 
												   + msg);
			} 

			MessageManagerImpl mng = messageManager;

			checkValidation();

			MessageImpl origin = (MessageImpl)msg;
			MessageImpl clone = (MessageImpl)origin.clone();

			checkMessagePermissionAndProtection(clone);

			if (mng != null) {
				origin.disable();		// disable the message
				mng.postMessage(clone);
			} else {
				origin.cancel("Message Manager not found " 
							  + (_state == INACTIVE ? "[inactive]" : ""));
			} 
		} 
	}
	void destroyMessageManager() {

		// Debug.check();
		messageManager.destroy();

		// Debug.check();
	}
	// trip with Ticket
	protected void dispatch(Ticket ticket) 
			throws IOException, RequestRefusedException {
		try {
			checkActive();

			/*
			 * TO AVOID SELF CKECKING : M.O
			 * checkPermission(new AgletPermission("this", ACTION_DISPATCH));
			 * checkProtection(new AgletProtection("this", ACTION_DISPATCH));
			 */
			checkAgletPermissionAndProtection(ACTION_DISPATCH);
			dispatch(AgletThread.getCurrentMessage(), ticket);
		} catch (InvalidAgletException ex) {
			throw new AgletsSecurityException(ACTION_DISPATCH + " : " + ex);
		} 
	}
	// trip with Ticket
	void dispatch(MessageImpl msg, Ticket ticket) 
			throws IOException, RequestRefusedException, 
				   InvalidAgletException {
		URL dest = ticket.getDestination();

		synchronized (lock) {
			checkValidation();

			// 
			// Converts URL to the destination ticket.
			// 
			if (dest.getRef() != null &&!"".equals(dest.getRef())) {
				throw new MalformedURLException("MalformedURL in dispatchAglet:" 
												+ ticket);
			} 

			try {
				dispatchEvent(new MobilityEvent(MobilityEvent.DISPATCHING, 
												proxy, ticket));
			} catch (SecurityException ex) {
				throw ex;
			} catch (Exception ex) {
				ex.printStackTrace();
			} 

			suspendMessageManager();

			boolean success = false;

			try {
				MAFAgentSystem _maf = 
					MAFAgentSystem.getMAFAgentSystem(ticket);

				if (_maf == null) {
					throw new ServerNotFoundException(ticket.toString());
				} 

				// # 		// consume a room of hops.
				// # 		// When the aglet has no available rooms of hops,
				// # 		// raise an ExhaustedException.
				// # 		allowance.consumeRoomHops();

				// 
				// clean up the reference. REMIND: needs to be improved.
				// 
				removeAgletRef(_name, this);

				AgletWriter writer = new AgletWriter();

				writer.writeInfo(this);
				writer.writeAglet(this);

				// Name        name       = getName(); // ??
				byte[] agent = writer.getBytes();
				String place = dest.getFile();

				if (place.startsWith("/")) {
					place = place.substring(1);
				} 

				ClassName[] classnames = writer.getClassNames();
				String codebase = info.getCodeBase().toString();

				MAFAgentSystem local = 
					MAFAgentSystem.getLocalMAFAgentSystem();

				_maf.receive_agent(_name, _agent_profile, agent, place, 
								   classnames, codebase, local);

				success = true;

				// # 	    } catch (ExhaustedException ex) {
				// # 		ex.printStackTrace();
				// # 		throw new RequestRefusedException("Available room of hops was exhausted : " + ex.getMessage());
			} catch (ClassUnknown ex) {
				ex.printStackTrace();
				throw new RequestRefusedException(ticket + " " 
												  + info.getAgletClassName());

			} catch (DeserializationFailed ex) {
				throw new RequestRefusedException(ticket + " " 
												  + info.getAgletClassName());

				/*
				 * } catch (RequestRefused ex) {
				 * throw new RequestRefusedException(ticket + " " +
				 * info.getAgletClassName());
				 */
			} catch (MAFExtendedException ex) {

				ex.printStackTrace();
				throw new RequestRefusedException(ticket + " " 
												  + info.getAgletClassName());
			} 
			finally {
				if (success == false) {
					resumeMessageManager();
					addAgletRef(_name, this);
					_context.log("Dispatch", 
								 "Fail to dispatch " 
								 + info.getAgletClassName() + " to " 
								 + ticket);

				} 
			} 

			invalidateReference();

			RemoteAgletRef r_ref = RemoteAgletRef.getAgletRef(ticket, _name);

			r_ref.setAgletInfo(info);

			AgletProxy new_proxy = new AgletProxyImpl(r_ref);

			if (msg != null && msg.future != null) {
				msg.future.sendReplyIfNeeded(new_proxy);
			} 

			removeSnapshot();

			terminateThreads();

			destroyMessageManager();

			try {
				_context.log("Dispatch", 
							 info.getAgletClassName() + " to " 
							 + ticket.getDestination());
				_context
					.postEvent(new ContextEvent(ContextEvent
						.DISPATCHED, _context, new_proxy, ticket
							.getDestination()), true);
			} 
			finally {
				releaseResource();
			} 
		} 
	}
	/*
	 * dispatches
	 */
	protected void dispatch(URL url) 
			throws IOException, RequestRefusedException {
		dispatch(new Ticket(url));
	}
	/*
	 * Event
	 */
	public void dispatchEvent(AgletEvent ev) {
		aglet.dispatchEvent(ev);
	}
	/**
	 * Disposes the aglet.
	 * @exception InvalidAgletException if the aglet is invalid.
	 */
	protected void dispose() {
		try {
			checkActive();

			/*
			 * TO AVOID SELF CKECKING : M.O
			 * checkPermission(new AgletPermission("this", ACTION_DISPOSE));
			 * checkProtection(new AgletProtection("this", ACTION_DISPOSE));
			 */
			checkAgletPermissionAndProtection(ACTION_DISPOSE);
			dispose(AgletThread.getCurrentMessage());
		} catch (InvalidAgletException excpt) {
			throw new AgletsSecurityException(ACTION_DISPOSE + " : " + excpt);
		} catch (RequestRefusedException excpt) {
			throw new AgletsSecurityException(ACTION_DISPOSE + " : " + excpt);
		} 
	}
	void dispose(MessageImpl msg) 
			throws InvalidAgletException, RequestRefusedException {

		// 
		// this is ad hoc
		// 
		if (num_of_trial_to_dispose > 2 && isValid()) {
			disposeAnyway(msg);
			return;
		} 
		num_of_trial_to_dispose++;

		synchronized (lock) {
			checkValidation();
			try {
				aglet.onDisposing();
			} 
			finally {
				disposeAnyway(msg);
			} 
		} 
	}
	private void disposeAnyway(MessageImpl msg) 
			throws RequestRefusedException {
		suspendMessageManager();

		invalidateReference();

		if (msg != null && msg.future != null) {
			msg.future.sendReplyIfNeeded(null);
		} 

		removeSnapshot();

		terminateThreads();

		destroyMessageManager();

		try {
			_context.log("Dispose", info.getAgletClassName());
			_context
				.postEvent(new ContextEvent(ContextEvent
					.DISPOSED, _context, proxy), true);
		} 
		finally {

			// Debug.check();
			releaseResource();
		} 
	}
	// #     /**
	// #      * Gets the allowance: availability of the aglet's resources.
	// #      * @return an Allowance object
	// #      */
	// #     public Allowance getAllowance() {
	// # //	checkActive();
	// # 	return allowance;
	// #     }

	/**
	 * Gets the address.
	 * 
	 * @return the current context address
	 */
	public String getAddress() throws InvalidAgletException {
		AgletContext c = _context;

		checkValidation();
		return c.getHostingURL().toString();
	}
	/**
	 * Gets the aglet. If the aglet is access protected it will require
	 * the right key to get access.
	 * @return the aglet
	 * @exception SecurityException if the current execution is not allowed.
	 */
	public Aglet getAglet() throws InvalidAgletException {
		checkValidation();
		checkMessagePermissionAndProtection("access");
		return aglet;
	}
	/*
	 * Gets the context
	 */
	protected AgletContext getAgletContext() {
		checkActive();
		return _context;
	}
	/**
	 * Gets the information of the aglet
	 * @return an AgletInfo object
	 */
	public AgletInfo getAgletInfo() {
		return info;
	}
	/* pakcage protected */
	static LocalAgletRef getAgletRef(Name name) {
		return (LocalAgletRef)local_ref_table.get(new RefKey(name));
	}
	/**
	 * Gets the Certificate of the aglet's class.
	 * @return a Certificate
	 */
	public Certificate getCertificate() throws InvalidAgletException {
		checkValidation();
		return _owner;
	}
	/*
	 * 
	 */
	protected MessageManager getMessageManager() {
		checkActive();
		return messageManager;
	}
	/**
	 * 
	 */

	/* package */
	public Name getName() {
		return _name;
	}
	/*
	 * 
	 */
	private String getPersistenceKey() {
		return info.getAgletID().toString();
	}
	/**
	 * Gets the protections: permission collection about
	 * who can send what kind of messages to the aglet
	 * @return collection of protections about who can send
	 * what kind of messages to the aglet
	 */
	protected PermissionCollection getProtections() {
		return protections;
	}
	public Ref getRef(VirtualRef vref) {

		// if (forwarding..)
		return this;
	}
	public String getRefClassName() {
		return "com.ibm.aglets.RemoteAgletRef";
	}
	boolean getSecurity() {
		return _secure;
	}
	/*
	 * Helpers
	 */
	String getStateAsString() {
		switch (_state) {
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
	 * @return the message line.
	 */
	public String getText() {
		checkActive();
		return _text == null ? "" : _text;
	}
	void invalidateReference() {
		unsubscribeAllMessages();
		_state = INVALID;
		_context.removeAgletProxy(info.getAgletID(), proxy);
		removeAgletRef(_name, this);
	}
	/**
	 * Checks if it's valid or not.
	 */
	public boolean isActive() {
		return _state == ACTIVE;
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
		return (_state & s) != 0;
	}
	/**
	 * Checks if it's valid or not.
	 */
	public boolean isValid() {
		return _state == ACTIVE || _state == INACTIVE;
	}
	protected void kill() {
		suspendMessageManager();
		switch (_state) {
		case ACTIVE:
			aglet = null;
			break;
		case INACTIVE:
			String key = getPersistenceKey();

			_context._timer.removeInfo(key);
			try {
				_context.getPersistence().removeEntry(key);
			} catch (Exception ex) {}
			break;
		default:
		}

		invalidateReference();
		removeSnapshot();

		terminateThreads();

		destroyMessageManager();

		try {
			_context.log("Dispose", info.getAgletClassName());
			_context
				.postEvent(new ContextEvent(ContextEvent
					.DISPOSED, _context, proxy), true);
		} 
		finally {
			releaseResource();
		} 
	}
	/*
	 * ===============================
	 * 
	 * See com.ibm.awb.weakref.Ref
	 * 
	 */
	public void referenced() {}
	/*
	 * This method is supposed to be called only from synchronized block
	 */
	void releaseResource() {

		_context = null;
		aglet = null;
		messageManager = null;

		resourceManager.disposeAllResources();
		resourceManager.stopThreadGroup();
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
		if (_hasSnapshot) {
			_hasSnapshot = false;
			try {
				_context.getPersistence().removeEntry(getPersistenceKey());
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 
	}
	public void resume() 
			throws AgletNotFoundException, InvalidAgletException, 
				   ShuttingDownException {
		_context.startCreation();

		synchronized (lock) {
			checkValidation();

			if (isActive()) {
				_context.endCreation();
				return;
			} 

			if (_mode != DeactivationInfo.SUSPENDED) {
				throw new AgletNotFoundException("Cannot resume the deactivated aglet");
			} 

			String key = getPersistenceKey();

			try {
				messageManager.state = MessageManagerImpl.UNINITIALIZED;

				// this should already have proxy.
				startResumedAglet();

				_context.log("Activated", info.getAgletClassName());
				return;

				/*
				 * } catch (IOException ex) {
				 * throw new AgletNotFoundException(key);
				 * 
				 * } catch (ClassNotFoundException ex) {
				 * // REMIND
				 * throw new AgletNotFoundException(key);
				 */
			} catch (InvalidAgletException ex) {
				ex.printStackTrace();
				throw new AgletNotFoundException(key);
			} 
			finally {
				try {
					_context._timer.removeInfo(key);
				} 
				finally {
					_context.endCreation();
				} 
			} 
		} 
	}
	/*
	 * Resumes all threads and resume accepting incoming messages.
	 */
	void resumeMessageManager() {
		messageManager.resume();
	}
	/* package */
	byte[] retract() throws MAFExtendedException {

		boolean success = false;
		String classname = info.getAgletClassName();

		try {
			checkAgletPermissionAndProtection(ACTION_RETRACT);

			Message m = new SystemMessage(Message.REVERT, null, 
										  SystemMessage.RETRACT_REQUEST);

			FutureReply f = sendFutureMessage(m);

			f.waitForReply(50000);

			if (f.isAvailable()) {
				try {
					f.getReply();
				} catch (MessageException ex) {
					if (ex.getException() instanceof SecurityException) {
						throw (SecurityException)ex.getException();
					} else {

						// why not successfull ? should not happen
						ex.printStackTrace();
					} 
				} catch (NotHandledException ex) {

					// invalid..? check with proxy.checkValidation()...
				} 
			} 
			checkValidation();

			AgletWriter writer = new AgletWriter();

			writer.writeInfo(this);
			writer.writeAglet(this);

			byte[] agent = writer.getBytes();

			invalidateReference();
			removeSnapshot();

			terminateThreads();

			destroyMessageManager();

			success = true;

			_context
				.postEvent(new ContextEvent(ContextEvent
					.REVERTED, this, proxy, null), true);
			return agent;

		} catch (SecurityException ex) {
			throw new MAFExtendedException(toMessage(ex));

		} catch (IOException ex) {
			throw new MAFExtendedException(toMessage(ex));

		} catch (InvalidAgletException ex) {
			throw new MAFExtendedException(toMessage(ex));

		} 
		finally {
			if (success) {
				_context.log("Reverted", classname);

				releaseResource();
			} else {
				_context.log("Reverted", "Failed to revert " + classname);
				resumeMessageManager();
			} 
		} 
	}
	/*
	 * Sends a message. This posts a message into the message manager.
	 * MessageManager controls concurrency and no synchronized
	 * modifier is needed.
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

		sendFutureMessage(msg, future);
		return future;
	}
	/* protected */
	void sendFutureMessage(Message msg, FutureReplyImpl future) 
			throws InvalidAgletException {

		// 
		// Just for thread safety to avoid a message being posted
		// into destroyed messageManager. this must be improved.
		// 
		MessageManagerImpl mng = messageManager;

		checkValidation();

		MessageImpl clone = cloneMessageAndCheck(msg, Message.FUTURE);

		clone.future = future;
		mng.postMessage(clone);
	}
	/*
	 * Sends a message in synchronous way.
	 */
	public Object sendMessage(Message msg) 
			throws MessageException, InvalidAgletException, 
				   NotHandledException {

		// 
		// Just for thread safety to avoid a message being posted
		// into destroyed messageManager. this must be improved.
		// 
		MessageManagerImpl mng = messageManager;

		checkValidation();

		FutureReplyImpl future = new FutureReplyImpl();
		MessageImpl clone = cloneMessageAndCheck(msg, Message.SYNCHRONOUS);

		clone.future = future;
		mng.postMessage(clone);
		return future.getReply();
	}
	/*
	 * Sends an oneway message.
	 * REMIND: IMPLEMENT!
	 */
	public void sendOnewayMessage(Message msg) throws InvalidAgletException {

		// 
		// Just for thread safety to avoid a message being posted
		// into destroyed messageManager. this must be improved.
		// 
		MessageManagerImpl mng = messageManager;

		checkValidation();

		FutureReplyImpl future = new FutureReplyImpl();
		MessageImpl clone = cloneMessageAndCheck(msg, Message.ONEWAY);

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
		aglet = a;
		final Class cls = aglet.getClass();
		ProtectionDomain domain = (ProtectionDomain)
			AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return cls.getProtectionDomain();
					}
				}
			);

		// =	ProtectionDomain domain = ProtectionDomain.getDomain(cls);

		// System.out.println("Class="+aglet.getClass());
		// System.out.println("ClaasLoader="+cls.getClassLoader());
		// System.out.println("ProtectionDomain="+String.valueOf(domain));

		if (domain != null && protections == null) {
			PermissionCollection ps = domain.getPermissions();

			if (ps != null) {
				Enumeration perms = ps.elements();

				while (perms.hasMoreElements()) {
					Permission perm = (Permission)perms.nextElement();

					if (perm instanceof Protection) {
						if (protections == null) {
							protections = new Protections();
						} 
						protections.add(perm);
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
		messageManager = impl;
		messageManager.setAgletRef(this);
	}
	/* package */
	void setName(Name n) {
		_name = n;
		_owner = AgletRuntime.getCertificate(_name.authority);

		// String authorityName = new String(_name.authority);
		// _identity = AgletRuntime.getIdentity(authorityName);
		// String name = _identity.getName();
		// if (!authorityName.equals(name)) {
		// System.err.println("Unknown authority '" + authorityName + "'. Regard as '" + name + "'");
		// }
	}
	/**
	 * Sets the protections: permission collection about
	 * who can send what kind of messages to the aglet
	 * @param newprotections (@link PermissionCollection} with protections listing who can send
	 * what kind of messages to the aglet
	 */
	protected void setProtections(PermissionCollection newprotections) {
		if (canSetProtections(newprotections)) {

			// only restriction can be done
			Protections ps = new Protections();
			Enumeration prots = newprotections.elements();

			while (prots.hasMoreElements()) {
				Permission protection = (Permission)prots.nextElement();

				ps.add(protection);
			} 
			protections = ps;
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
		_secure = secure;
	}
	/**
	 * Sets/Shows a text.
	 * @param text
	 */
	protected void setText(String text) {
		checkActive();
		_text = text;
		_context
			.postEvent(new ContextEvent(ContextEvent
				.STATE_CHANGED, _context, proxy, text), true);
	}
	/**
	 * Checkpointing the snapshot of the aglet.
	 * @exception IOException
	 */
	protected void snapshot() throws IOException {
		synchronized (lock) {
			checkActive();

			ObjectOutputStream out = null;
			Persistence persistence = _context.getPersistence();

			suspendMessageManager();

			String key = getPersistenceKey();
			boolean success = false;

			try {
				PersistentEntry entry = persistence.createEntryWith(key);

				out = new ObjectOutputStream(entry.getOutputStream());

				writeDeactivatedAglet(out, 
									  new DeactivationInfo(_name, -1, key, 
														   DeactivationInfo
														   .DEACTIVATED));

				_hasSnapshot = true;
				success = true;
			} catch (IOException ex) {
				try {
					persistence.removeEntry(key);
				} catch (Exception ee) {}
				throw ex;
			} catch (RuntimeException ex) {
				try {
					persistence.removeEntry(key);
				} catch (Exception ee) {}
				throw ex;
			} 
			finally {
				resumeMessageManager();
				if (success) {
					_context.log("Snapshot", key);
				} else {
					_context.log("Snapshot", 
								 "Fail to save snapshot for aglet [" + key 
								 + "]");
				} 
				if (out != null) {
					try {
						out.close();
					} catch (IOException ex) {}
				} 
			} 
		} 
	}
	/**
	 * Send events to the activated aglet.
	 * @exception AgletException if the activation fails.
	 * @see com.ibm.aglet.event.PersistencyListener#onActivation(PersistencyEvent)
	 */
	void startActivatedAglet() throws InvalidAgletException {
		_state = ACTIVE;

		messageManager
			.postMessage(new EventMessage(new PersistencyEvent(PersistencyEvent
				.ACTIVATION, proxy, 0)));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, 
				null));

		_context
			.postEvent(new ContextEvent(ContextEvent
				.ACTIVATED, _context, proxy), true);

		resumeMessageManager();
	}
	/**
	 * Activates the arrived aglet.
	 * @param cxt the aglet context in which the aglet activated
	 * @param sender url of the departure
	 * @exception AgletException if the activation fails.
	 * @see com.ibm.aglet.event.MobilityListener#onArrival(MobilityEvent)
	 */
	void startArrivedAglet(AgletContextImpl cxt, 
						   String sender) throws InvalidAgletException {
		validate(cxt, ACTIVE);

		messageManager
			.postMessage(new EventMessage(new MobilityEvent(MobilityEvent
				.ARRIVAL, proxy, _context.getHostingURL())));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, 
				null));

		_context
			.postEvent(new ContextEvent(ContextEvent
				.ARRIVED, cxt, proxy, sender), true);

		resumeMessageManager();
	}
	/**
	 * Activates the cloned aglet.
	 * @param cxt the aglet context in which the aglet activated
	 * @param parent proxy to the original aglet
	 * @exception AgletException if the activation fails.
	 * @see com.ibm.aglet.event.CloneListener#onCloned(CloneEvent)
	 */
	void startClonedAglet(AgletContextImpl cxt, AgletProxyImpl parent) 
			throws InvalidAgletException {
		validate(cxt, ACTIVE);

		messageManager
			.postMessage(new EventMessage(new CloneEvent(CloneEvent.CLONE, 
				proxy)));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, 
				null));

		_context
			.postEvent(new ContextEvent(ContextEvent
				.CLONED, cxt, proxy, parent), true);

		resumeMessageManager();
	}
	/**
	 * Initializes the aglet.
	 * @param cxt the aglet context in which the aglet activated
	 * @param init argumetns to be used in onCreation method.
	 * @exception InvalidAgletException if the aglet is invalid.
	 * @see Aglet#onCreation
	 */
	void startCreatedAglet(AgletContextImpl cxt, 
						   Object init) throws InvalidAgletException {
		validate(cxt, ACTIVE);

		messageManager = new MessageManagerImpl(this);

		messageManager.postMessage(new SystemMessage(SystemMessage.CREATE, 
				init));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, 
				null));
		_context.postEvent(new ContextEvent(ContextEvent.CREATED, cxt, proxy), 
						   true);

		startMessageManager();
	}
	/*
	 * Start the aglet threads
	 */
	void startMessageManager() {
		messageManager.start();
	}
	/**
	 * Send events to the resumed aglet.
	 * @exception AgletException if the activation fails.
	 * @see com.ibm.aglet.event.PersistencyListener#onActivation(PersistencyEvent)
	 */
	void startResumedAglet() throws InvalidAgletException {
		_state = ACTIVE;

		// //messageManager.postMessage(new EventMessage(new PersistencyEvent(PersistencyEvent.RESUME, proxy, 0)));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, 
				null));

		_context
			.postEvent(new ContextEvent(ContextEvent
				.RESUMED, _context, proxy), true);

		resumeMessageManager();
	}
	// -- subscribe to a specific whiteboard.
	// 
	protected void subscribeMessage(String kind) {
		synchronized (lock) {
			checkActive();
			checkPermission(new ContextPermission(kind, "subscribe"));
			_context._subscriberManager.subscribe(this, kind);
		} 
	}
	/**
	 * Suspend aglet for the specified amount of time. The suspended aglet will
	 * remain in the memory.
	 * @param duration the duration to sleep in milliseconds.
	 * @exception InvalidAgletEception if can not suspend the aglet.
	 */
	protected void suspend(long duration) throws InvalidAgletException {
		try {
			checkActive();

			/*
			 * TO AVOID SELF CKECKING : M.O
			 * checkPermission(new AgletPermission("this", ACTION_DEACTIVATE));
			 * checkProtection(new AgletProtection("this", ACTION_DEACTIVATE));
			 */
			checkAgletPermissionAndProtection(ACTION_DEACTIVATE);
			suspend(AgletThread.getCurrentMessage(), duration);
		} catch (InvalidAgletException excpt) {
			throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " 
											  + excpt);
		} catch (RequestRefusedException excpt) {
			throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " 
											  + excpt);
		} 
	}
	void suspend(MessageImpl msg, long duaration) 
			throws InvalidAgletException, RequestRefusedException {

		synchronized (lock) {
			checkValidation();

			if (duaration < 0) {
				throw new IllegalArgumentException("minutes must be positive");
			} 

			// 
			// Suspend all threads except for the current thread
			// 
			suspendMessageManager();

			// start
			String key = getPersistenceKey();

			long wakeupTime = duaration == 0 ? 0 
							  : System.currentTimeMillis() + duaration;
			DeactivationInfo dinfo = new DeactivationInfo(_name, wakeupTime, 
					key, DeactivationInfo.SUSPENDED);

			_context._timer.add(dinfo);

			_state = INACTIVE;
			_mode = DeactivationInfo.SUSPENDED;

			if (msg != null && msg.future != null) {
				msg.future.sendReplyIfNeeded(null);
			} 

			// message manager is persistent and will be restored later
			messageManager.deactivate();

			terminateThreads();

			try {
				_context.log("Suspend", key);
				_context
					.postEvent(new ContextEvent(ContextEvent
						.SUSPENDED, _context, proxy), true);
			} 
			finally {

				// resourceManager.disposeAllResources();
				// resourceManager.stopThreadGroup();
			} 
		} 
	}
	/*
	 * Retraction
	 */
	void suspendForRetraction(Ticket ticket) throws InvalidAgletException {

		synchronized (lock) {
			checkValidation();
			try {
				dispatchEvent(new MobilityEvent(MobilityEvent.REVERTING, 
												proxy, ticket));
			} catch (SecurityException ex) {
				throw ex;
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
			suspendMessageManager();
		} 
	}
	/*
	 * Suspends all threads and accepting new message
	 */
	void suspendMessageManager() {
		messageManager.suspend();
	}
	void terminateThreads() {

		// Debug.check();
		resourceManager.stopAllThreads();
	}
	static String toMessage(Exception ex) {
		return ex.getClass().getName() + ':' + ex.getMessage();
	}
	public String toString() {
		if (!isValid()) {
			return "Aglet [ invalid ]";
		} 
		StringBuffer buff = new StringBuffer();

		if (_state == ACTIVE) {
			buff.append("Aglet [active]\n");
		} else if (_state == INACTIVE) {
			buff.append("Aglet [inactive]\n");
		} 

		String ownerName;
		if(_owner == null)
		    ownerName = "ANONYMOUS";
		else
		    ownerName = ((X509Certificate)_owner).getSubjectDN().getName();

		buff.append("      ClassName [" + info.getAgletClassName() + "]\n");
		buff.append("      Identifier[" + info.getAgletID() + "]\n");
		buff.append("      Owner[" + ownerName + "]\n");
		buff.append("      CodeBase[" + info.getCodeBase() + "]\n");
		buff.append(resourceManager.toString());

		/*
		 * if (threadGroup == null) {
		 * return buff.toString();
		 * } else {
		 * Thread t[] = new Thread[threadGroup.activeCount()];
		 * threadGroup.enumerate(t);
		 * String head = "      Threads  ";
		 * for(int i=0; i<t.length; i++) {
		 * buff.append(head + t[i].toString() + "\n");
		 * head = "               ";
		 * }
		 * //	    buff.append("      MessageManager\n");
		 * //	    buff.append(messageManager.toString());
		 * return buff.toString();
		 * }
		 */
		return buff.toString();
	}
	public void unreferenced() {}
	// -- unsubscribe from all whiteboards.
	// 
	protected void unsubscribeAllMessages() {
		synchronized (lock) {
			checkActive();
			_context._subscriberManager.unsubscribeAll(this);
		} 
	}
	// -- unsubscribe from a specific whiteboard.
	// 
	protected boolean unsubscribeMessage(String kind) {
		synchronized (lock) {
			checkActive();
			return _context._subscriberManager.unsubscribe(this, kind);
		} 
	}
	/*
	 * This method is supposed to be called only once.
	 * 
	 */
	/* synchronized */
	void validate(AgletContextImpl context, 
				  int state) throws InvalidAgletException {
		if (isValid()) {
			throw new IllegalAccessError("Aglet is already validated");
		} 
		_state = state;
		_context = context;
		_context.addAgletProxy(info.getAgletID(), proxy);

		// see AgletReader
		addAgletRef(_name, this);
	}
	private void writeDeactivatedAglet(ObjectOutputStream out, 
									   DeactivationInfo dinfo) throws IOException {
		out.writeObject(dinfo);
		out.writeObject(messageManager);

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
		s.writeObject(_name);
		s.writeObject(_context.getHostingURL().toString());
	}
}
