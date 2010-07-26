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

import org.aglets.log.AgletsLogger;

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
	int max = Thread.currentThread().getPriority();

	AGLET_GROUPS.setMaxPriority(max - 1);
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
    public ResourceManagerImpl(AgletClassLoader l, String name) {
	logger.debug("Ctor: [" + name + "]");
	this._loader = l;
	this._name = name;
    }

    /*
     * ======================================================== General (Window)
     * ========================================================
     */
    public void addResource(Object o) {
	logger.debug("addResource");
	synchronized (this._resources) {
	    if (this._resources.contains(o) == false) {
		this._resources.addElement(o);
	    }
	}
    }

    /**
     * return false if not found.
     */
    public boolean contains(Class cls) {
	logger.debug("contains()");
	return this._loader.contains(cls);
    }

    public void disposeAllResources() {
	synchronized (this._resources) {
	    java.util.Enumeration e = this._resources.elements();

	    while (e.hasMoreElements()) {
		Object o = e.nextElement();

		if (o instanceof java.awt.Window) {
		    ((java.awt.Window) o).dispose();
		} else {

		    // what's else?
		}
	    }
	    this._resources = null;
	}
	// <RAB> 01092002 Do not kill loader so agent can recieve message to
	// reativate
	// _loader = null;
	// </RAB>
    }

    /**
     * Archives that this resource manager is managing.
     */
    public Archive getArchive(ClassName[] table) {
	return this._loader.getArchive(table);
    }

    /**
	 * 
	 */
    public ClassName[] getClassNames(Class[] classes) {
	return this._loader.getClassNames(classes);
    }

    String getName() {
	return this._name;
    }

    /* package */
    static ResourceManagerImpl getResourceManagerContext() {
	logger.debug("getResourceManagerContext()++");
	ResourceManagerImpl rm = (ResourceManagerImpl) rm_contexts.get(Thread.currentThread());

	if (rm == null) {
	    logger.debug("No context found for thread getting group.");
	    ThreadGroup tg = Thread.currentThread().getThreadGroup();

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

    synchronized public ThreadGroup getThreadGroup() {
	if (this._group == null) {
	    try {
		final ResourceManagerImpl fResMan = this;

		this._group = (AgletThreadGroup) AccessController.doPrivileged(new PrivilegedAction() {
		    public Object run() {
			return new AgletThreadGroup(AGLET_GROUPS, fResMan);
		    }
		});
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
	return this._group;
    }

    /*
	 * 
	 */
    public void importArchive(Archive a) {
	this._loader.importArchive(a);
    }

    /*
     * ======================================================== ByteCode and
     * Class Management ========================================================
     */
    public Class loadClass(String name) throws ClassNotFoundException {
	return this._loader.loadClass(name);
    }

    /*
     * ====================================================== Thread Management
     * ======================================================
     */
    public AgletThread newAgletThread(MessageManager mm) {
	logger.debug("newAgletThread");
	try {
	    final ThreadGroup fThreadGroup = this.getThreadGroup();
	    final MessageManager fMsgMan = mm;

	    return (AgletThread) AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return new AgletThread(fThreadGroup, fMsgMan);
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    public void resumeAllThreads() {
	try {
	    final ThreadGroup fThreadGroup = this.getThreadGroup();

	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    fThreadGroup.resume();
		    return null;
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void setResourceManagerContext() {
	logger.debug("setResourceManagerContext() : " + this.getName());
	rm_contexts.put(Thread.currentThread(), this);
    }

    public void stopAllThreads() {
	final AgletThreadGroup g = (AgletThreadGroup) this.getThreadGroup();

	//
	// Needs to imporove.
	//
	try {
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    boolean suicide = false;

		    synchronized (g) {
			g.invalidate();
			g.setDaemon(true);
			ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();

			if (g.parentOf(currentGroup)) {

			    // suicide
			    suicide = true;
			    Thread t[] = new Thread[g.activeCount() + 1];
			    int num = g.enumerate(t, true);
			    Thread current = Thread.currentThread();

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
	} catch (IllegalThreadStateException ex) {
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    synchronized public void stopThreadGroup() {
	try {
	    final ThreadGroup fThreadGroup = this._group;

	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    try {
			fThreadGroup.stop();
		    } catch (Exception ex) {
		    } finally {
			try {
			    fThreadGroup.destroy();
			} catch (IllegalThreadStateException ex) {
			}
		    }
		    return null;
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	this._group = null;
    }

    public void suspendAllThreads() {
	final ThreadGroup fThreadGroup = this.getThreadGroup();
	synchronized (fThreadGroup) {
	    try {
		AccessController.doPrivileged(new PrivilegedAction() {
		    public Object run() {
			Thread t[] = new Thread[fThreadGroup.activeCount()];
			Thread current = Thread.currentThread();
			int num = fThreadGroup.enumerate(t, true);

			for (int i = 0; i < num; i++) {
			    if (current != t[i]) {
				t[i].suspend();
			    }
			}
			return null;
		    }
		});
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    public void unsetResourceManagerContext() {
	logger.debug("unsetResourceManagerContext()");
	rm_contexts.remove(Thread.currentThread());
    }

    @Override
    public String toString() {
	return this._name;
    }

}
