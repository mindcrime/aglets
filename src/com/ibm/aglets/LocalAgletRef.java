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

import net.sourceforge.aglets.log.AgletsLogger;

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
import com.ibm.aglet.event.EventType;
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

	static class RefKey {
		static public boolean equals(final Name n1, final Name n2) {
			if ( /* n1.authority.length == n2.authority.length && */
					(n1.identity.length == n2.identity.length)
					&& (n1.agent_system_type == n2.agent_system_type)) {

				// int l = n1.authority.length;
				// for(int i=0; i<l; i++) {
				// if (n1.authority[i] != n2.authority[i]) {
				// return false;
				// }
				// }
				final int l = n1.identity.length;

				for (int i = 0; i < l; i++) {
					if (n1.identity[i] != n2.identity[i]) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		Name name;

		int hash = 0;

		RefKey(final Name n) {
			name = n;
			for (final byte element : n.identity) {
				hash += (hash * 37) + (int) element;
			}
		}

		public boolean equals(final Object obj) {
			if (obj instanceof RefKey) {
				return equals(((RefKey) obj).name, name);
			}
			return false;
		}

		public int hashCode() {
			return hash;
		}
	}
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
	/* pakcage protected */
	static LocalAgletRef getAgletRef(final Name name) {
		return (LocalAgletRef) local_ref_table.get(new RefKey(name));
	}
	/* package protected */
	private static void removeAgletRef(final Name name, final LocalAgletRef ref) {
		if (local_ref_table.contains(ref)) {
			local_ref_table.remove(new RefKey(name));

			// exported_aglets.remove(id);
		}
	}
	static String toMessage(final Exception ex) {
		return ex.getClass().getName() + ':' + ex.getMessage();
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

	private final Object lock = new Object(); // locker to synchronized

	private int num_of_trial_to_dispose = 0;

	/*
	 * Reference Table.
	 */
	static Hashtable local_ref_table = new Hashtable();

	/* package protected */
	private static void addAgletRef(final Name name, final LocalAgletRef ref) {
		local_ref_table.put(new RefKey(name), ref);
	}

	/**
	 * 
	 */
	private static Protections cloneProtections(final Protections protections) {

		// because java.security.Permissions and java.security.Permission
		// are not Cloneable.
		if (protections == null) {
			return null;
		}
		final Enumeration prots = protections.elements();
		final Protections ps = new Protections();

		while (prots.hasMoreElements()) {
			final Object obj = prots.nextElement();

			if (obj instanceof AgletProtection) {
				final AgletProtection ap = (AgletProtection) obj;
				final String name = ap.getName();
				final String actions = ap.getActions();
				final AgletProtection nap = new AgletProtection(name, actions);

				ps.add(nap);
			} else if (obj instanceof MessageProtection) {
				final MessageProtection mp = (MessageProtection) obj;
				final String name = mp.getName();
				final String actions = mp.getActions();
				final MessageProtection nmp = new MessageProtection(name, actions);

				ps.add(mp);
			}
		}
		return ps;
	}

	/**
	 * Creates an aglet reference
	 */

	/* package */
	LocalAgletRef(final AgletContextImpl cxt) {
		this(cxt, cxt.getSecurity());
	}

	LocalAgletRef(final AgletContextImpl cxt, final boolean secure) {
		_context = cxt;
		_secure = secure;
	}

	/* package */
	Object _clone() throws CloneNotSupportedException {
		try {
			_context.startCreation();
		} catch (final ShuttingDownException ex) {
			throw new CloneNotSupportedException("Shutting down");
		}
		synchronized (lock) {
			boolean success = false;

			try {
				checkValidation();
				checkActive();
				try {
					dispatchEvent(new CloneEvent(AgletEvent.nextID(), proxy, EventType.CLONING));
				} catch (final SecurityException ex) {
					throw ex;
				} catch (final Exception ex) {
					ex.printStackTrace();
				}

				// # // consume a room of cloning.
				// # // When the aglet has no available rooms of cloning,
				// # // raise an ExhaustedException.
				// # allowance.consumeRoomCloning();

				suspendMessageManager();
				final LocalAgletRef clone_ref = new LocalAgletRef(_context, _secure);

				// Set the owner
				// Certificate owner = AgletRuntime.getCurrentCertificate();
				final Certificate owner = _owner;
				final Name new_name = AgletRuntime.newName(owner);

				clone_ref.setName(new_name);

				// Set AgletInfo
				clone_ref.info = new AgletInfo(MAFUtil.toAgletID(new_name), info.getAgletClassName(), info.getCodeBase(), _context.getHostingURL().toString(), System.currentTimeMillis(), info.getAPIMajorVersion(), info.getAPIMinorVersion(), owner);
				final AgletWriter writer = new AgletWriter();

				writer.writeAglet(this);
				clone_ref.createResourceManager(writer.getClassNames());
				final AgletReader reader = new AgletReader(writer.getBytes());

				reader.readAglet(clone_ref);
				final Aglet clone = clone_ref.aglet;

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
				clone_ref.protections = cloneProtections(protections);

				// start
				clone.setStub(clone_ref);
				clone_ref.proxy = new AgletProxyImpl(clone_ref);
				clone_ref.startClonedAglet(_context, proxy);
				success = true;
				return clone_ref.proxy;
			} catch (final ClassNotFoundException ex) {
				throw new CloneNotSupportedException("Class Not Found :"
						+ ex.getMessage());

				// } catch (ExhaustedException ex) {
				// throw new
				// CloneNotSupportedException("Available room of cloning was exhausted : "
				// + ex.getMessage());
			} catch (final IOException ex) {
				ex.printStackTrace();
				throw new CloneNotSupportedException("IO Exception :"
						+ ex.getMessage());
			} catch (final AgletException ex) {
				throw new CloneNotSupportedException("Aglet Exception :"
						+ ex.getMessage());
			} catch (final RuntimeException ex) {
				logger.error("Exception caught while processing a message", ex);
				throw ex;
			} finally {
				resumeMessageManager();
				if (success) {
					_context.log("Clone", info.getAgletClassName());
				} else {
					_context.log("Clone", "Failed to clone the aglet ["
							+ info.getAgletClassName() + "]");
				}
				dispatchEvent(new CloneEvent(AgletEvent.nextID(), proxy, EventType.CLONED));
				_context.endCreation();
			}
		}
	}

	/**
	 * 
	 * 
	 */
	public void activate()
	throws IOException,
	InvalidAgletException,
	AgletNotFoundException,
	ShuttingDownException {

		_context.startCreation();

		synchronized (lock) {
			checkValidation();

			if (isActive()) {
				_context.endCreation();
				return;
			}

			// if (_mode == DeactivationInfo.SUSPENDED) {
			// throw new
			// AgletNotFoundException("Cannot activate the suspended aglet");
			// }

			final String key = getPersistenceKey();

			ObjectInputStream oin = null;
			final Persistence persistence = _context.getPersistence();

			try {
				if (_mode == DeactivationInfo.SUSPENDED) {
					messageManager.state = MessageManagerImpl.UNINITIALIZED;
				} else {

					// - checkAgletPermissionAndProtection(ACTION_ACTIVATE);

					final PersistentEntry entry = persistence.getEntry(key);

					oin = new ObjectInputStream(entry.getInputStream());

					final DeactivationInfo dinfo = (DeactivationInfo) oin.readObject();

					setMessageManager((MessageManagerImpl) oin.readObject());

					_hasSnapshot = dinfo.isSnapshot();

					final ClassName[] classnames = (ClassName[]) oin.readObject();

					final byte[] agent = new byte[oin.readInt()];

					oin.readFully(agent);

					final AgletReader reader = new AgletReader(agent);

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

			} catch (final IOException ex) {
				throw new AgletNotFoundException(key);

			} catch (final ClassNotFoundException ex) {

				// REMIND
				throw new AgletNotFoundException(key);

			} catch (final InvalidAgletException ex) {
				ex.printStackTrace();
				throw new AgletNotFoundException(key);
			} finally {
				try {
					try {
						if (oin != null) {
							oin.close();
						}
					} catch (final IOException e) {
					}

					_context._timer.removeInfo(key);
					if (_hasSnapshot == false) {
						persistence.removeEntry(key);
					}
				} finally {
					_context.endCreation();
				}
			}
		}
	}

	/**
	 * Returns that the protections can be set or not
	 * 
	 * @param newprotections
	 *            collection of protections about who can send what kind of
	 *            messages to the aglet
	 */
	private boolean canSetProtections(final PermissionCollection newprotections) {
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

	private void checkAgletPermission(final String actions) {

		// System.out.println("checkPermission(new AgletPermission("+_identity.getName()+", "+actions+"))");
		checkPermission(new AgletPermission(AgletRuntime.getCertificateAlias(_owner), actions));
	}

	private void checkAgletPermissionAndProtection(final String actions) {
		checkAgletPermission(actions);
		checkAgletProtection(actions);
	}

	private void checkAgletProtection(final String actions) {
		final Certificate cert = AgletRuntime.getCurrentCertificate();

		// ?????????? What if cert is null????? (HT)
		if (cert != null) {

			// System.out.println("checkProtection(new AgletProtection("+id.getName()+", "+actions+"))");
			checkProtection(new AgletProtection(AgletRuntime.getCertificateAlias(cert), actions));
		}
	}

	private void checkMessagePermission(final MessageImpl msg) {

		// System.out.println("checkPermission(msg.getPermission("+_identity.getName()+"))");
		final Permission p = msg.getPermission(AgletRuntime.getCertificateAlias(_owner));

		// System.out.println("checkPermission("+p.toString()+")");
		checkPermission(p);
	}

	private void checkMessagePermission(final String actions) {

		// System.out.println("checkPermission(new MessagePermission("+_identity.getName()+", "+actions+"))");
		checkPermission(new MessagePermission(AgletRuntime.getCertificateAlias(_owner), actions));
	}

	private void checkMessagePermissionAndProtection(final MessageImpl msg) {
		this.checkMessagePermission(msg);
		this.checkMessageProtection(msg);
	}

	private void checkMessagePermissionAndProtection(final String actions) {
		this.checkMessagePermission(actions);
		this.checkMessageProtection(actions);
	}

	private void checkMessageProtection(final MessageImpl msg) {
		final Certificate cert = AgletRuntime.getCurrentCertificate();

		// ??????What if cert is null????(HT)
		if (cert != null) {

			// System.out.println("checkProtection(msg.getProtection("+id.getName()+"))");
			final Permission p = msg.getProtection(AgletRuntime.getCertificateAlias(cert));

			// System.out.println("checkProtection("+p.toString()+")");
			checkProtection(p);
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

	private void checkMessageProtection(final String actions) {
		final Certificate cert = AgletRuntime.getCurrentCertificate();

		// ??????What if cert is null????(HT)
		if (cert != null) {

			// System.out.println("checkProtection(new MessageProtection("+id.getName()+", "+actions+"))");
			checkProtection(new MessageProtection(AgletRuntime.getCertificateAlias(cert), actions));
		}
	}

	/*
	 * check permission/protection
	 */
	private void checkPermission(final Permission p) {
		if (_context != null) {
			_context.checkPermission(p);
		}
	}

	private void checkProtection(final Permission p) {
		if (!_secure) {
			return;
		}

		logger.debug("protections=" + String.valueOf(protections));
		logger.debug("permission=" + String.valueOf(p));
		if ((protections != null)
				&& (protections.implies(p) == false)) {
			final SecurityException ex = new SecurityException(p.toString());

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
		checkAgletPermissionAndProtection(ACTION_CLONE);
		return _clone();
	}

	private MessageImpl cloneMessageAndCheck(final Message msg, final int type) {
		MessageImpl clone;

		if (msg instanceof SystemMessage) {
			clone = (MessageImpl) msg;
		} else { // normal or delegate
			clone = new MessageImpl(msg, null, type, System.currentTimeMillis());
		}
		this.checkMessagePermissionAndProtection(clone);
		return clone;
	}

	/*
	 * 
	 */
	ResourceManager createResourceManager(final ClassName[] table) {
		resourceManager = _context.createResourceManager(info.getCodeBase(), _owner, table);
		if (resourceManager == null) {
			logger.error("invalid codebase:" + info.getCodeBase());
		}
		return resourceManager;
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
	protected void deactivate(final long duration) throws IOException {
		try {
			checkActive();

			/*
			 * TO AVOID SELF CKECKING : M.O checkPermission(new
			 * AgletPermission("this", ACTION_DEACTIVATE)); checkProtection(new
			 * AgletProtection("this", ACTION_DEACTIVATE));
			 */
			checkAgletPermissionAndProtection(ACTION_DEACTIVATE);
			this.deactivate(AgletThread.getCurrentMessage(), duration);
		} catch (final InvalidAgletException excpt) {
			throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " + excpt);
		} catch (final RequestRefusedException excpt) {
			throw new AgletsSecurityException(ACTION_DEACTIVATE + " : " + excpt);
		}
	}

	/*
	 * 
	 */
	void deactivate(final MessageImpl msg, final long duaration)
	throws IOException,
	InvalidAgletException,
	RequestRefusedException {

		synchronized (lock) {
			checkValidation();

			if (duaration < 0) {
				throw new IllegalArgumentException("minutes must be positive");
			}

			final Persistence persistence = _context.getPersistence();

			if (persistence == null) {
				_context.log("Deactivation", "Deactivation not implemneted in this environment");
				return;
			}

			try {
				dispatchEvent(new PersistencyEvent(proxy, duaration, EventType.DEACTIVATING));
			} catch (final SecurityException ex) {
				throw ex;
			} catch (final Exception ex) {
				ex.printStackTrace();
			}

			ObjectOutputStream out = null;

			//
			// Suspend all threads except for the current thread
			//
			suspendMessageManager();

			// start
			boolean success = false;

			final String key = getPersistenceKey();

			try {
				final long wakeupTime = duaration == 0 ? 0
						: System.currentTimeMillis() + duaration;
				final PersistentEntry entry = persistence.createEntryWith(key);

				out = new ObjectOutputStream(entry.getOutputStream());

				final DeactivationInfo dinfo = new DeactivationInfo(_name, wakeupTime, key, DeactivationInfo.DEACTIVATED);

				writeDeactivatedAglet(out, dinfo);

				_context._timer.add(dinfo);
				success = true;
			} finally {
				if (success == false) {
					try {
						persistence.removeEntry(key);
					} catch (final ThreadDeath t) {
						throw t;
					} catch (final Throwable ee) {

						// ignore
					}
					resumeMessageManager();
					_context.log("Deactivate", "Fail to save aglet ["
							+ key + "]");
				}
				if (out != null) {
					try {
						out.flush();
						out.close();
					} catch (final IOException ex) {
					}
				}
			}

			_state = INACTIVE;
			_mode = DeactivationInfo.DEACTIVATED;

			if ((msg != null) && (msg.future != null)) {
				msg.future.sendReplyIfNeeded(null);
			}

			// message manager is persistent and will be restored later
			messageManager.deactivate();

			terminateThreads();

			_hasSnapshot = false;
			aglet = null;
			try {
				_context.log("Deactivate", key);
				_context.postEvent(new ContextEvent(_context, proxy, EventType.AGLET_DEACTIVATED), true);
			} finally {
				resourceManager.disposeAllResources();
				resourceManager.stopThreadGroup();
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
	public void delegateMessage(final Message msg) throws InvalidAgletException {
		logger.debug("delegateMessage()++");
		synchronized (msg) {
			if (((msg instanceof MessageImpl) == false)
					|| (((MessageImpl) msg).isDelegatable() == false)) {

				throw new IllegalArgumentException("The message cannot be delegated "
						+ msg);
			}

			final MessageManagerImpl mng = messageManager;

			checkValidation();

			final MessageImpl origin = (MessageImpl) msg;
			final MessageImpl clone = (MessageImpl) origin.clone();

			this.checkMessagePermissionAndProtection(clone);

			if (mng != null) {
				origin.disable(); // disable the message
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
	void dispatch(final MessageImpl msg, final Ticket ticket)
	throws IOException,
	RequestRefusedException,
	InvalidAgletException {
		final URL dest = ticket.getDestination();

		synchronized (lock) {
			checkValidation();

			//
			// Converts URL to the destination ticket.
			//
			if ((dest.getRef() != null) && !"".equals(dest.getRef())) {
				throw new MalformedURLException("MalformedURL in dispatchAglet:"
						+ ticket);
			}

			try {
				dispatchEvent(new MobilityEvent(proxy, ticket, EventType.DISPATCHING));
			} catch (final SecurityException ex) {
				throw ex;
			} catch (final Exception ex) {
				ex.printStackTrace();
			}

			suspendMessageManager();

			boolean success = false;

			try {
				final MAFAgentSystem _maf = MAFAgentSystem.getMAFAgentSystem(ticket);

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
				removeAgletRef(_name, this);

				final AgletWriter writer = new AgletWriter();

				writer.writeInfo(this);
				writer.writeAglet(this);

				// Name name = getName(); // ??
				final byte[] agent = writer.getBytes();
				String place = dest.getFile();

				if (place.startsWith("/")) {
					place = place.substring(1);
				}

				final ClassName[] classnames = writer.getClassNames();
				final String codebase = info.getCodeBase().toString();

				final MAFAgentSystem local = MAFAgentSystem.getLocalMAFAgentSystem();

				_maf.receive_agent(_name, _agent_profile, agent, place, classnames, codebase, local);

				success = true;

				// # } catch (ExhaustedException ex) {
				// # ex.printStackTrace();
				// # throw new
				// RequestRefusedException("Available room of hops was exhausted : "
				// + ex.getMessage());
			} catch (final ClassUnknown ex) {
				ex.printStackTrace();
				throw new RequestRefusedException(ticket + " "
						+ info.getAgletClassName());

			} catch (final DeserializationFailed ex) {
				throw new RequestRefusedException(ticket + " "
						+ info.getAgletClassName());

				/*
				 * } catch (RequestRefused ex) { throw new
				 * RequestRefusedException(ticket + " " +
				 * info.getAgletClassName());
				 */
			} catch (final MAFExtendedException ex) {

				ex.printStackTrace();
				throw new RequestRefusedException(ticket + " "
						+ info.getAgletClassName());
			} finally {
				if (success == false) {
					resumeMessageManager();
					addAgletRef(_name, this);
					_context.log("Dispatch", "Fail to dispatch "
							+ info.getAgletClassName() + " to " + ticket);

				}
			}

			invalidateReference();

			final RemoteAgletRef r_ref = RemoteAgletRef.getAgletRef(ticket, _name);

			r_ref.setAgletInfo(info);

			final AgletProxy new_proxy = new AgletProxyImpl(r_ref);

			if ((msg != null) && (msg.future != null)) {
				msg.future.sendReplyIfNeeded(new_proxy);
			}

			removeSnapshot();

			terminateThreads();

			destroyMessageManager();

			try {
				_context.log("Dispatch", info.getAgletClassName()
						+ " to " + ticket.getDestination());
				_context.postEvent(new ContextEvent(_context, new_proxy, ticket.getDestination(), EventType.AGLET_DISPATCHED), true);
			} finally {
				releaseResource();
			}
		}
	}

	// trip with Ticket
	protected void dispatch(final Ticket ticket)
	throws IOException,
	RequestRefusedException {
		try {
			checkActive();

			/*
			 * TO AVOID SELF CKECKING : M.O checkPermission(new
			 * AgletPermission("this", ACTION_DISPATCH)); checkProtection(new
			 * AgletProtection("this", ACTION_DISPATCH));
			 */
			checkAgletPermissionAndProtection(ACTION_DISPATCH);
			this.dispatch(AgletThread.getCurrentMessage(), ticket);
		} catch (final InvalidAgletException ex) {
			throw new AgletsSecurityException(ACTION_DISPATCH + " : " + ex);
		}
	}

	/*
	 * dispatches
	 */
	protected void dispatch(final URL url)
	throws IOException,
	RequestRefusedException {
		this.dispatch(new Ticket(url));
	}

	/*
	 * Event
	 */
	public void dispatchEvent(final AgletEvent ev) {
		aglet.dispatchEvent(ev);
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
	 * Disposes the aglet.
	 * 
	 * @exception InvalidAgletException
	 *                if the aglet is invalid.
	 */
	protected void dispose() {
		try {
			checkActive();

			/*
			 * TO AVOID SELF CKECKING : M.O checkPermission(new
			 * AgletPermission("this", ACTION_DISPOSE)); checkProtection(new
			 * AgletProtection("this", ACTION_DISPOSE));
			 */
			checkAgletPermissionAndProtection(ACTION_DISPOSE);
			this.dispose(AgletThread.getCurrentMessage());
		} catch (final InvalidAgletException excpt) {
			throw new AgletsSecurityException(ACTION_DISPOSE + " : " + excpt);
		} catch (final RequestRefusedException excpt) {
			throw new AgletsSecurityException(ACTION_DISPOSE + " : " + excpt);
		}
	}

	void dispose(final MessageImpl msg)
	throws InvalidAgletException,
	RequestRefusedException {

		//
		// this is ad hoc
		//
		if ((num_of_trial_to_dispose > 2) && isValid()) {
			disposeAnyway(msg);
			return;
		}
		num_of_trial_to_dispose++;

		synchronized (lock) {
			checkValidation();
			try {
				aglet.onDisposing();
			} finally {
				disposeAnyway(msg);
			}
		}
	}

	private void disposeAnyway(final MessageImpl msg) throws RequestRefusedException {
		suspendMessageManager();

		invalidateReference();

		if ((msg != null) && (msg.future != null)) {
			msg.future.sendReplyIfNeeded(null);
		}

		removeSnapshot();

		terminateThreads();

		destroyMessageManager();

		try {
			_context.log("Dispose", info.getAgletClassName());
			_context.postEvent(new ContextEvent(_context, proxy, EventType.AGLET_DISPOSED), true);
		} finally {

			// Debug.check();
			releaseResource();
		}
	}

	/**
	 * Gets the address.
	 * 
	 * @return the current context address
	 */
	public String getAddress() throws InvalidAgletException {
		final AgletContext c = _context;

		checkValidation();
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
		checkValidation();
		this.checkMessagePermissionAndProtection("access");
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
	 * 
	 * @return an AgletInfo object
	 */
	public AgletInfo getAgletInfo() {
		return info;
	}

	/**
	 * Gets the Certificate of the aglet's class.
	 * 
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
	 * Gets the protections: permission collection about who can send what kind
	 * of messages to the aglet
	 * 
	 * @return collection of protections about who can send what kind of
	 *         messages to the aglet
	 */
	protected PermissionCollection getProtections() {
		return protections;
	}

	public Ref getRef(final VirtualRef vref) {

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
	 * 
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
	public boolean isState(final int s) {
		return (_state & s) != 0;
	}

	/**
	 * Checks if it's valid or not.
	 */
	public boolean isValid() {
		return (_state == ACTIVE) || (_state == INACTIVE);
	}

	protected void kill() {
		suspendMessageManager();
		switch (_state) {
			case ACTIVE:
				aglet = null;
				break;
			case INACTIVE:
				final String key = getPersistenceKey();

				_context._timer.removeInfo(key);
				try {
					_context.getPersistence().removeEntry(key);
				} catch (final Exception ex) {
				}
				break;
			default:
		}

		invalidateReference();
		removeSnapshot();

		terminateThreads();

		destroyMessageManager();

		try {
			_context.log("Dispose", info.getAgletClassName());
			_context.postEvent(new ContextEvent(_context, proxy, EventType.AGLET_DISPOSED), true);
		} finally {
			releaseResource();
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

		_context = null;
		aglet = null;
		messageManager = null;

		resourceManager.disposeAllResources();
		resourceManager.stopThreadGroup();
	}

	/*
	 */
	void removeSnapshot() {
		if (_hasSnapshot) {
			_hasSnapshot = false;
			try {
				_context.getPersistence().removeEntry(getPersistenceKey());
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void resume()
	throws AgletNotFoundException,
	InvalidAgletException,
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

			final String key = getPersistenceKey();

			try {
				messageManager.state = MessageManagerImpl.UNINITIALIZED;

				// this should already have proxy.
				startResumedAglet();

				_context.log("Activated", info.getAgletClassName());
				return;

				/*
				 * } catch (IOException ex) { throw new
				 * AgletNotFoundException(key);
				 * 
				 * } catch (ClassNotFoundException ex) { // REMIND throw new
				 * AgletNotFoundException(key);
				 */
			} catch (final InvalidAgletException ex) {
				ex.printStackTrace();
				throw new AgletNotFoundException(key);
			} finally {
				try {
					_context._timer.removeInfo(key);
				} finally {
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
		final String classname = info.getAgletClassName();

		try {
			checkAgletPermissionAndProtection(ACTION_RETRACT);

			final Message m = new SystemMessage(Message.REVERT, null, SystemMessage.RETRACT_REQUEST);

			final FutureReply f = this.sendFutureMessage(m);

			f.waitForReply(50000);

			if (f.isAvailable()) {
				try {
					f.getReply();
				} catch (final MessageException ex) {
					if (ex.getException() instanceof SecurityException) {
						throw (SecurityException) ex.getException();
					} else {

						// why not successfull ? should not happen
						ex.printStackTrace();
					}
				} catch (final NotHandledException ex) {

					// invalid..? check with proxy.checkValidation()...
				}
			}
			checkValidation();

			final AgletWriter writer = new AgletWriter();

			writer.writeInfo(this);
			writer.writeAglet(this);

			final byte[] agent = writer.getBytes();

			invalidateReference();
			removeSnapshot();

			terminateThreads();

			destroyMessageManager();

			success = true;

			_context.postEvent(new ContextEvent(this, proxy, null, EventType.AGLET_REVERTED), true);
			return agent;

		} catch (final SecurityException ex) {
			throw new MAFExtendedException(toMessage(ex));

		} catch (final IOException ex) {
			throw new MAFExtendedException(toMessage(ex));

		} catch (final InvalidAgletException ex) {
			throw new MAFExtendedException(toMessage(ex));

		} finally {
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
	 * MessageManager controls concurrency and no synchronized modifier is
	 * needed.
	 * 
	 * REMIND: The way to handle system messages have to be imporved.
	 */
	public FutureReply sendFutureMessage(final Message msg)
	throws InvalidAgletException {

		//
		// Just for thread safety to avoid a message being posted
		// into destroyed messageManager. this must be improved.
		//
		final FutureReplyImpl future = new FutureReplyImpl();

		this.sendFutureMessage(msg, future);
		return future;
	}

	/* protected */
	void sendFutureMessage(final Message msg, final FutureReplyImpl future)
	throws InvalidAgletException {

		//
		// Just for thread safety to avoid a message being posted
		// into destroyed messageManager. this must be improved.
		//
		final MessageManagerImpl mng = messageManager;

		checkValidation();

		final MessageImpl clone = cloneMessageAndCheck(msg, Message.FUTURE);

		clone.future = future;
		mng.postMessage(clone);
	}

	/*
	 * Sends a message in synchronous way.
	 */
	public Object sendMessage(final Message msg)
	throws MessageException,
	InvalidAgletException,
	NotHandledException {

		//
		// Just for thread safety to avoid a message being posted
		// into destroyed messageManager. this must be improved.
		//
		final MessageManagerImpl mng = messageManager;

		checkValidation();

		final FutureReplyImpl future = new FutureReplyImpl();
		final MessageImpl clone = cloneMessageAndCheck(msg, Message.SYNCHRONOUS);

		clone.future = future;
		mng.postMessage(clone);
		return future.getReply();
	}

	/*
	 * Sends an oneway message. REMIND: IMPLEMENT!
	 */
	public void sendOnewayMessage(final Message msg) throws InvalidAgletException {

		//
		// Just for thread safety to avoid a message being posted
		// into destroyed messageManager. this must be improved.
		//
		final MessageManagerImpl mng = messageManager;

		checkValidation();

		final FutureReplyImpl future = new FutureReplyImpl();
		final MessageImpl clone = cloneMessageAndCheck(msg, Message.ONEWAY);

		clone.future = future;
		mng.pushMessage(clone);
		return;
	}

	/*
	 * @see com.ibm.aglet.AgletProxy#setStub
	 */
	protected void setAglet(final Aglet a) {
		if (a != null) {
			new IllegalAccessError("Aglet canont be set twice");
		}
		aglet = a;
		final Class cls = aglet.getClass();
		final ProtectionDomain domain = (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return cls.getProtectionDomain();
			}
		});

		// = ProtectionDomain domain = ProtectionDomain.getDomain(cls);

		// System.out.println("Class="+aglet.getClass());
		// System.out.println("ClaasLoader="+cls.getClassLoader());
		// System.out.println("ProtectionDomain="+String.valueOf(domain));

		if ((domain != null) && (protections == null)) {
			final PermissionCollection ps = domain.getPermissions();

			if (ps != null) {
				final Enumeration perms = ps.elements();

				while (perms.hasMoreElements()) {
					final Permission perm = (Permission) perms.nextElement();

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
	void setMessageManager(final MessageManagerImpl impl) {
		messageManager = impl;
		messageManager.setAgletRef(this);
	}

	/* package */
	void setName(final Name n) {
		_name = n;
		_owner = AgletRuntime.getCertificate(_name.authority);

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
	 * @param newprotections
	 *            collection of protections about who can send what kind of
	 *            messages to the aglet
	 */
	protected void setProtections(final PermissionCollection newprotections) {
		if (canSetProtections(newprotections)) {

			// only restriction can be done
			final Protections ps = new Protections();
			final Enumeration prots = newprotections.elements();

			while (prots.hasMoreElements()) {
				final Permission protection = (Permission) prots.nextElement();

				ps.add(protection);
			}
			protections = ps;
		} else {

			// cannot moderate restriction
			throw new IllegalArgumentException("cannot moderate protection");
		}
	}

	public void setRef(final VirtualRef vref, final ObjectInputStream s)
	throws IOException,
	ClassNotFoundException {

		// never called.
		throw new RuntimeException("Should Not Called");
	}

	void setSecurity(final boolean secure) {
		_secure = secure;
	}

	/**
	 * Sets/Shows a text.
	 * 
	 * @param text
	 */
	protected void setText(final String text) {
		checkActive();
		_text = text;
		_context.postEvent(new ContextEvent(_context, proxy, text, EventType.AGLET_STATE_CHANGED), true);
	}

	/**
	 * Checkpointing the snapshot of the aglet.
	 * 
	 * @exception IOException
	 */
	protected void snapshot() throws IOException {
		synchronized (lock) {
			checkActive();

			ObjectOutputStream out = null;
			final Persistence persistence = _context.getPersistence();

			suspendMessageManager();

			final String key = getPersistenceKey();
			boolean success = false;

			try {
				final PersistentEntry entry = persistence.createEntryWith(key);

				out = new ObjectOutputStream(entry.getOutputStream());

				writeDeactivatedAglet(out, new DeactivationInfo(_name, -1, key, DeactivationInfo.DEACTIVATED));

				_hasSnapshot = true;
				success = true;
			} catch (final IOException ex) {
				try {
					persistence.removeEntry(key);
				} catch (final Exception ee) {
				}
				throw ex;
			} catch (final RuntimeException ex) {
				try {
					persistence.removeEntry(key);
				} catch (final Exception ee) {
				}
				throw ex;
			} finally {
				resumeMessageManager();
				if (success) {
					_context.log("Snapshot", key);
				} else {
					_context.log("Snapshot", "Fail to save snapshot for aglet ["
							+ key + "]");
				}
				if (out != null) {
					try {
						out.close();
					} catch (final IOException ex) {
					}
				}
			}
		}
	}

	/**
	 * Send events to the activated aglet.
	 * 
	 * @exception AgletException
	 *                if the activation fails.
	 * @see com.ibm.aglet.event.PersistencyListener#onActivation
	 */
	void startActivatedAglet() throws InvalidAgletException {
		_state = ACTIVE;

		messageManager.postMessage(new EventMessage(new PersistencyEvent(proxy, 0, EventType.ACTIVATION)));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

		_context.postEvent(new ContextEvent(_context, proxy, EventType.AGLET_ACTIVATED), true);

		resumeMessageManager();
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
	 * @see com.ibm.aglet.event.MobilityListener#onArrival
	 */
	void startArrivedAglet(final AgletContextImpl cxt, final String sender)
	throws InvalidAgletException {
		validate(cxt, ACTIVE);

		messageManager.postMessage(new EventMessage(new MobilityEvent(proxy, _context.getHostingURL(), EventType.ARRIVAL)));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

		_context.postEvent(new ContextEvent(cxt, proxy, sender, EventType.AGLET_ARRIVED), true);

		resumeMessageManager();
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
	 * @see com.ibm.aglet.event.CloneListener#onClone
	 */
	void startClonedAglet(final AgletContextImpl cxt, final AgletProxyImpl parent)
	throws InvalidAgletException {
		validate(cxt, ACTIVE);

		messageManager.postMessage(new EventMessage(new CloneEvent(AgletEvent.nextID(), proxy, EventType.CLONE)));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

		_context.postEvent(new ContextEvent(cxt, proxy, parent, EventType.AGLET_CLONED), true);

		resumeMessageManager();
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
	void startCreatedAglet(final AgletContextImpl cxt, final Object init)
	throws InvalidAgletException {
		validate(cxt, ACTIVE);

		messageManager = new MessageManagerImpl(this);

		messageManager.postMessage(new SystemMessage(SystemMessage.CREATE, init));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));
		_context.postEvent(new ContextEvent(cxt, proxy, EventType.AGLET_CREATED), true);

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
	 * 
	 * @exception AgletException
	 *                if the activation fails.
	 * @see com.ibm.aglet.event.PersistencyListener#onActivation
	 */
	void startResumedAglet() throws InvalidAgletException {
		_state = ACTIVE;

		// //messageManager.postMessage(new EventMessage(new
		// PersistencyEvent(PersistencyEvent.RESUME, proxy, 0)));
		messageManager.postMessage(new SystemMessage(SystemMessage.RUN, null));

		_context.postEvent(new ContextEvent(_context, proxy, EventType.AGLET_RESUMED), true);

		resumeMessageManager();
	}

	// -- subscribe to a specific whiteboard.
	//
	protected void subscribeMessage(final String kind) {
		synchronized (lock) {
			checkActive();
			checkPermission(new ContextPermission(kind, "subscribe"));
			_context._subscriberManager.subscribe(this, kind);
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
	protected void suspend(final long duration) throws InvalidAgletException {
		// check params
		if (messageManager == null)
			throw new InvalidAgletException("The message manager is null!");

		try {
			// first of all suspend the message manager, thus it will continue
			// to enqueue messages but will not process them
			messageManager.setSleeping(true);

			// now suspend the thread for the duration specified
			final long suspendTime = System.currentTimeMillis();
			while ((suspendTime + duration) > System.currentTimeMillis()) {
				Thread.currentThread().sleep(duration);
			}

			// now wake up the message manager
			messageManager.setSleeping(false);

		} catch (final InterruptedException e) {
			logger.error("Exception caught while suspending the agent", e);
			throw new InvalidAgletException(e);
		}

	}

	void suspend(final MessageImpl msg, final long duaration)
	throws InvalidAgletException,
	RequestRefusedException {

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
			final String key = getPersistenceKey();

			final long wakeupTime = duaration == 0 ? 0 : System.currentTimeMillis()
					+ duaration;
			final DeactivationInfo dinfo = new DeactivationInfo(_name, wakeupTime, key, DeactivationInfo.SUSPENDED);

			_context._timer.add(dinfo);

			_state = INACTIVE;
			_mode = DeactivationInfo.SUSPENDED;

			if ((msg != null) && (msg.future != null)) {
				msg.future.sendReplyIfNeeded(null);
			}

			// message manager is persistent and will be restored later
			messageManager.deactivate();

			terminateThreads();

			try {
				_context.log("Suspend", key);
				_context.postEvent(new ContextEvent(_context, proxy, EventType.AGLET_SUSPENDED), true);
			} finally {

				// resourceManager.disposeAllResources();
				// resourceManager.stopThreadGroup();
			}
		}
	}

	/*
	 * Retraction
	 */
	void suspendForRetraction(final Ticket ticket) throws InvalidAgletException {

		synchronized (lock) {
			checkValidation();
			try {
				dispatchEvent(new MobilityEvent(proxy, ticket, EventType.REVERTING));
			} catch (final SecurityException ex) {
				throw ex;
			} catch (final Exception ex) {
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

	/**
	 * A description of the proxy in HTML, useful for tooltip.
	 * 
	 * @return the html string.
	 */
	public String toHTMLString() {
		// check if the aglet is active
		if (!isValid())
			return "Aglet [ invalid ]";

		// build a buffer with all the information about this aglet proxy
		final StringBuffer buffer = new StringBuffer(500);

		// the classname of the aglet
		buffer.append("<HTML>");
		buffer.append(info.getAgletClassName());

		// the status of the agent
		buffer.append("      Status: ");
		buffer.append("<B>");

		if (_state == ACTIVE)
			buffer.append(" active ");
		else
			buffer.append(" inactive ");

		buffer.append("</B>");

		// the identifier of the agent
		buffer.append("<BR>");
		buffer.append("      AgletID: ");
		buffer.append("<B>");
		buffer.append(info.getAgletID());
		buffer.append("</B>");

		// the code base of the agent
		buffer.append("<BR>");
		buffer.append("      Codebase: ");
		buffer.append("<B>");
		buffer.append(info.getCodeBase());
		buffer.append("</B>");

		buffer.append("<BR>");
		buffer.append("      ResourceManager: ");
		buffer.append("<B>");
		buffer.append(resourceManager.toString());
		buffer.append("</B>");

		// the owner of the agent
		buffer.append("<BR>");
		buffer.append("      Owner: ");
		buffer.append("<B>");

		if (_owner == null)
			buffer.append(" anonymous ");
		else
			buffer.append(((X509Certificate) _owner).getSubjectDN().getName());

		buffer.append("</B>");
		buffer.append("</HTML>");
		return buffer.toString();
	}

	public String toString() {
		// check if the aglet is active
		if (!isValid())
			return "Aglet [ invalid ]";

		// build a buffer with all the information about this aglet proxy
		final StringBuffer buffer = new StringBuffer(500);

		// the classname of the aglet
		buffer.append(info.getAgletClassName());

		// the status of the agent
		buffer.append("      Status: ");
		if (_state == ACTIVE)
			buffer.append(" active ");
		else
			buffer.append(" inactive ");

		// the identifier of the agent
		buffer.append("      AgletID: ");
		buffer.append(info.getAgletID());

		// the code base of the agent
		buffer.append("      Codebase: ");
		buffer.append(info.getCodeBase());

		buffer.append("      ResourceManager: ");
		buffer.append(resourceManager.toString());

		// the owner of the agent
		buffer.append("      Owner: ");

		if (_owner == null)
			buffer.append(" anonymous ");
		else
			buffer.append(((X509Certificate) _owner).getSubjectDN().getName());

		return buffer.toString();
	}

	public void unreferenced() {
	}

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
	protected boolean unsubscribeMessage(final String kind) {
		synchronized (lock) {
			checkActive();
			return _context._subscriberManager.unsubscribe(this, kind);
		}
	}

	/*
	 * This method is supposed to be called only once.
	 */
	/* synchronized */
	void validate(final AgletContextImpl context, final int state)
	throws InvalidAgletException {
		if (isValid()) {
			throw new IllegalAccessError("Aglet is already validated");
		}
		_state = state;
		_context = context;
		_context.addAgletProxy(info.getAgletID(), proxy);

		// see AgletReader
		addAgletRef(_name, this);
	}

	private void writeDeactivatedAglet(
	                                   final ObjectOutputStream out,
	                                   final DeactivationInfo dinfo)
	throws IOException {
		out.writeObject(dinfo);
		out.writeObject(messageManager);

		final AgletWriter writer = new AgletWriter();

		writer.writeInfo(this);
		writer.writeAglet(this);

		// Class Table
		out.writeObject(writer.getClassNames());

		final byte[] b = writer.getBytes();

		// Aglet
		out.writeInt(b.length);
		out.write(b);
	}

	/*
	 * @see com.ibm.aglets.RemoteAgletRef#findRef
	 */
	public void writeInfo(final ObjectOutputStream s) throws IOException {
		s.writeObject(_name);
		s.writeObject(_context.getHostingURL().toString());
	}

}
