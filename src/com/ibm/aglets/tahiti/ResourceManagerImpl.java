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

import java.util.Vector;
import java.util.Hashtable;

// import com.ibm.awb.misc.DigestTable;
import com.ibm.maf.ClassName;
import com.ibm.awb.misc.Archive;
import com.ibm.aglet.MessageManager;
import com.ibm.aglets.AgletThread;
import com.ibm.aglets.log.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.log4j.Logger;

/**
 * ResourceManagerImpl is a implementation of ResourceManager
 * in the Aglets framework.
 * 
 * @version     $Revision: 1.7 $	$Date: 2009/07/27 10:31:40 $ $Author: cat4hire $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

final class ResourceManagerImpl implements com.ibm.aglets.ResourceManager {

	static private ThreadGroup AGLET_GROUPS = new ThreadGroup("AGLET_GROUPS");
	static private Logger logCategory = LoggerFactory.getLogger(com.ibm.aglets.ResourceManager.class);
    

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
	 * ======================================================
	 * ResourceManager Context
	 * ======================================================
	 */
	static private Hashtable rm_contexts = new Hashtable();

	/**
	 * 
	 */
	public ResourceManagerImpl(AgletClassLoader l, String name) {
        logCategory.debug("Ctor: ["+name+"]");
		_loader = l;
		_name = name;
	}
	/*
	 * ========================================================
	 * General (Window)
	 * ========================================================
	 */
	public void addResource(Object o) {
        logCategory.debug("addResource");
		synchronized (_resources) {
			if (_resources.contains(o) == false) {
				_resources.addElement(o);
			} 
		} 
	}
	/**
	 * return false if not found.
	 */
	public boolean contains(Class cls) {
        logCategory.debug("contains()");
		return _loader.contains(cls);
	}
	public void disposeAllResources() {
		synchronized (_resources) {
			java.util.Enumeration e = _resources.elements();

			while (e.hasMoreElements()) {
				Object o = e.nextElement();

				if (o instanceof java.awt.Window) {
					((java.awt.Window)o).dispose();
				} else {

					// what's else?
				} 
			} 
			_resources = null;
		} 
        // <RAB> 01092002 Do not kill loader so agent can recieve message to reativate
		//_loader = null;
        // </RAB>
	}
	/**
	 * Archives that this resource manager is managing.
	 */
	public Archive getArchive(ClassName[] table) {
		return _loader.getArchive(table);
	}
	/**
	 * 
	 */
	public ClassName[] getClassNames(Class[] classes) {
		return _loader.getClassNames(classes);
	}
	String getName() {
		return _name;
	}
	/* package */
	static ResourceManagerImpl getResourceManagerContext() {
        logCategory.debug("getResourceManagerContext()++");
		ResourceManagerImpl rm = 
			(ResourceManagerImpl)rm_contexts.get(Thread.currentThread());
        
		if (rm == null) {
            logCategory.debug("No context found for thread getting group.");
			ThreadGroup tg = Thread.currentThread().getThreadGroup();

			if (tg instanceof AgletThreadGroup) {
				rm = ((AgletThreadGroup)tg)._rm;
			} 
		}
        
        if( logCategory.isDebugEnabled() ) {
            if( rm != null ) {
                logCategory.debug("Using RM: "+rm.getName());
            } else {
                logCategory.debug("No manager found");
            }
        }
        logCategory.debug("getResourceManagerContext()--");
		return rm;
	}
	synchronized public ThreadGroup getThreadGroup() {
		if (_group == null) {
			try {
				final ResourceManagerImpl fResMan = this;

				_group = 
					(AgletThreadGroup)AccessController
						.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return new AgletThreadGroup(AGLET_GROUPS, fResMan);
					} 
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 
		return _group;
	}
	/*
	 * 
	 */
	public void importArchive(Archive a) {
		_loader.importArchive(a);
	}
	/*
	 * ========================================================
	 * ByteCode and Class Management
	 * ========================================================
	 */
	public Class loadClass(String name) throws ClassNotFoundException {
		return _loader.loadClass(name);
	}
	/*
	 * ======================================================
	 * Thread Management
	 * ======================================================
	 */
	public AgletThread newAgletThread(MessageManager mm) {
        logCategory.debug("newAgletThread");
		try {
			final ThreadGroup fThreadGroup = getThreadGroup();
			final MessageManager fMsgMan = mm;

			return (AgletThread)AccessController
				.doPrivileged(new PrivilegedAction() {
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
			final ThreadGroup fThreadGroup = getThreadGroup();

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
        logCategory.debug("setResourceManagerContext() : "+getName());
		rm_contexts.put(Thread.currentThread(), this);
	}
	public void stopAllThreads() {
		final AgletThreadGroup g = (AgletThreadGroup)getThreadGroup();

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
						ThreadGroup currentGroup = 
							Thread.currentThread().getThreadGroup();

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
		} catch (IllegalThreadStateException ex) {}
		catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	synchronized public void stopThreadGroup() {
		try {
			final ThreadGroup fThreadGroup = _group;

			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					try {
						fThreadGroup.stop();
					} catch (Exception ex) {}
					finally {
						try {
							fThreadGroup.destroy();
						} catch (IllegalThreadStateException ex) {}
					} 
					return null;
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		_group = null;
	}
	public void suspendAllThreads() {
		final ThreadGroup fThreadGroup = getThreadGroup();
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
        logCategory.debug("unsetResourceManagerContext()");
		rm_contexts.remove(Thread.currentThread());
	}
    
    public String toString() {
        return _name;
    }
    
}
