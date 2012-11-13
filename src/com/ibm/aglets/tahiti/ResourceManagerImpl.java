package com.ibm.aglets.tahiti;

/*
 * @(#)ResourceManagerImpl.java
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Vector;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.message.MessageManager;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.awb.misc.Archive;
import com.ibm.maf.ClassName;

/**
 * ResourceManagerImpl is a implementation of ResourceManager in the Aglets
 * framework.
 * 
 * @version $Revision: 1.8 $ $Date: 2009/07/28 07:04:52 $ $Author: cat4hire $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

final class ResourceManagerImpl implements com.ibm.aglets.ResourceManager {

	static private ThreadGroup AGLET_GROUPS = new ThreadGroup("AGLET_GROUPS");
	private static AgletsLogger logger = AgletsLogger.getLogger(ResourceManagerImpl.class.getName());

	static {
		final int max = Thread.currentThread().getPriority();

		AGLET_GROUPS.setMaxPriority(max - 1);
	}

	/* package */
	static ResourceManagerImpl getResourceManagerContext() {
		logger.debug("getResourceManagerContext()++");
		ResourceManagerImpl rm = (ResourceManagerImpl) rm_contexts.get(Thread.currentThread());

		if (rm == null) {
			logger.debug("No context found for thread getting group.");
			final ThreadGroup tg = Thread.currentThread().getThreadGroup();

			if (tg instanceof AgletThreadGroup) {
				rm = ((AgletThreadGroup) tg)._rm;
			}
		}

		if (logger.isDebugEnabled()) {
			if (rm != null) {
				logger.debug("Using RM: " + rm.getName());
			} else {
				logger.debug("No manager found");
			}
		}
		logger.debug("getResourceManagerContext()--");
		return rm;
	}
	/*
	 * 
	 */
	private AgletThreadGroup _group = null;
	private AgletClassLoader _loader = null;
	private Vector _resources = new Vector();

	private String _name = null;

	/*
	 * ====================================================== ResourceManager
	 * Context ======================================================
	 */
	static private Hashtable rm_contexts = new Hashtable();

	/**
	 * 
	 */
	public ResourceManagerImpl(final AgletClassLoader l, final String name) {
		logger.debug("Ctor: [" + name + "]");
		_loader = l;
		_name = name;
	}

	/*
	 * ======================================================== General (Window)
	 * ========================================================
	 */
	@Override
	public void addResource(final Object o) {
		logger.debug("addResource");
		synchronized (_resources) {
			if (_resources.contains(o) == false) {
				_resources.addElement(o);
			}
		}
	}

	/**
	 * return false if not found.
	 */
	@Override
	public boolean contains(final Class cls) {
		logger.debug("contains()");
		return _loader.contains(cls);
	}

	@Override
	public void disposeAllResources() {
		synchronized (_resources) {
			final java.util.Enumeration e = _resources.elements();

			while (e.hasMoreElements()) {
				final Object o = e.nextElement();

				if (o instanceof java.awt.Window) {
					((java.awt.Window) o).dispose();
				} else {

					// what's else?
				}
			}
			_resources = null;
		}
		// <RAB> 01092002 Do not kill loader so agent can recieve message to
		// reativate
		// _loader = null;
		// </RAB>
	}

	/**
	 * Archives that this resource manager is managing.
	 */
	@Override
	public Archive getArchive(final ClassName[] table) {
		return _loader.getArchive(table);
	}

	/**
	 * 
	 */
	@Override
	public ClassName[] getClassNames(final Class[] classes) {
		return _loader.getClassNames(classes);
	}

	String getName() {
		return _name;
	}

	synchronized public ThreadGroup getThreadGroup() {
		if (_group == null) {
			try {
				final ResourceManagerImpl fResMan = this;

				_group = (AgletThreadGroup) AccessController.doPrivileged(new PrivilegedAction() {
					@Override
					public Object run() {
						return new AgletThreadGroup(AGLET_GROUPS, fResMan);
					}
				});
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
		return _group;
	}

	/*
	 * 
	 */
	@Override
	public void importArchive(final Archive a) {
		_loader.importArchive(a);
	}

	/*
	 * ======================================================== ByteCode and
	 * Class Management ========================================================
	 */
	@Override
	public Class loadClass(final String name) throws ClassNotFoundException {
		return _loader.loadClass(name);
	}

	/*
	 * ====================================================== Thread Management
	 * ======================================================
	 */
	@Override
	public AgletThread newAgletThread(final MessageManager mm) {
		logger.debug("newAgletThread");
		try {
			final ThreadGroup fThreadGroup = getThreadGroup();
			final MessageManager fMsgMan = mm;

			return (AgletThread) AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					return new AgletThread(fThreadGroup, fMsgMan);
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public void resumeAllThreads() {
		try {
			final ThreadGroup fThreadGroup = getThreadGroup();

			AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					fThreadGroup.resume();
					return null;
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void setResourceManagerContext() {
		logger.debug("setResourceManagerContext() : " + getName());
		rm_contexts.put(Thread.currentThread(), this);
	}

	@Override
	public void stopAllThreads() {
		final AgletThreadGroup g = (AgletThreadGroup) getThreadGroup();

		//
		// Needs to imporove.
		//
		try {
			AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					boolean suicide = false;

					synchronized (g) {
						g.invalidate();
						g.setDaemon(true);
						final ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();

						if (g.parentOf(currentGroup)) {

							// suicide
							suicide = true;
							final Thread t[] = new Thread[g.activeCount() + 1];
							final int num = g.enumerate(t, true);
							final Thread current = Thread.currentThread();

							for (int i = 0; i < num; i++) {
								if (current != t[i]) {
									t[i].resume();
									t[i].stop();
									t[i].interrupt();
								}
							}
						} else {
							g.stop();
							g.resume();
						}
					}
					if (suicide == false) {
						g.destroy();
					}
					return null;
				}
			});
		} catch (final IllegalThreadStateException ex) {
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	synchronized public void stopThreadGroup() {
		try {
			final ThreadGroup fThreadGroup = _group;

			AccessController.doPrivileged(new PrivilegedAction() {
				@Override
				public Object run() {
					try {
						fThreadGroup.stop();
					} catch (final Exception ex) {
					} finally {
						try {
							fThreadGroup.destroy();
						} catch (final IllegalThreadStateException ex) {
						}
					}
					return null;
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		_group = null;
	}

	@Override
	public void suspendAllThreads() {
		final ThreadGroup fThreadGroup = getThreadGroup();
		synchronized (fThreadGroup) {
			try {
				AccessController.doPrivileged(new PrivilegedAction() {
					@Override
					public Object run() {
						final Thread t[] = new Thread[fThreadGroup.activeCount()];
						final Thread current = Thread.currentThread();
						final int num = fThreadGroup.enumerate(t, true);

						for (int i = 0; i < num; i++) {
							if (current != t[i]) {
								t[i].suspend();
							}
						}
						return null;
					}
				});
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return _name;
	}

	@Override
	public void unsetResourceManagerContext() {
		logger.debug("unsetResourceManagerContext()");
		rm_contexts.remove(Thread.currentThread());
	}

}
