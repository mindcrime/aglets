package net.sourceforge.aglets.examples.finder;

/*
 * @(#)HostList.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.util.Enumeration;
import java.util.Hashtable;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.event.PersistencyEvent;
import com.ibm.aglet.event.PersistencyListener;
import com.ibm.aglet.message.Message;

/**
 * The HostList keeps a list of aglet server names. When it moves to another
 * server, it automatically disappears.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Yoshiaki Mima
 * @see net.sourceforge.aglets.examples.finder.HostCollector
 */

public class HostList extends Aglet implements PersistencyListener,
MobilityListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7039028428575317796L;
	Hashtable hostList;

	public void appendList(final Hashtable list) {
		for (final Enumeration e = list.keys(); e.hasMoreElements();) {
			final Object key = e.nextElement();

			if (list.get(key) instanceof String) {
				if (hostList.get(key) == null) {
					System.out.println("new: " + key);
					hostList.put(key, "new");
				} else {

					// System.out.println("key: " + key + " value: " +
					// hostList.get(key));
				}
			}
		}
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("dialog")) {
			try {
				int i = 0;

				System.out.println("HostList -- begin");
				for (final Enumeration e = hostList.keys(); e.hasMoreElements();) {
					System.out.println(i++ + ": " + e.nextElement());
				}
				System.out.println("HostList -- end");
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			return true;
		} else if (msg.sameKind("register")) {
			if (msg.getArg() instanceof String) {
				hostList.put(msg.getArg(), "running");
			}
			return true;
		} else if (msg.sameKind("append")) {
			if (msg.getArg() instanceof Hashtable) {
				appendList((Hashtable) msg.getArg());
			}
			return true;
		} else if (msg.sameKind("getlist")) {
			msg.sendReply(hostList);
			return true;
		} else if (msg.sameKind("shutdown")) {
			try {
				deactivate(0);
			} catch (final Exception e) {
			}
			return true;
		} else if (msg.sameKind("dispose")) {
			dispose();
			return true;
		} else {
		}
		return true;
	}

	@Override
	public void onActivation(final PersistencyEvent event) {
		final AgletContext ac = getAgletContext();
		final AgletProxy proxy = getProxy();
		AgletProxy ap;

		// check if another HostList registration
		ap = (AgletProxy) ac.getProperty("hostlist");
		if ((ap != null) && (ap.isValid())) {
			try {
				ap.sendMessage(new Message("append", hostList));
			} catch (final Exception e) {
				System.out.println(e);
			}
			dispose();
		} else {
			ac.setProperty("hostlist", proxy);
		}
	}

	@Override
	public void onArrival(final MobilityEvent event) {
		dispose();
	}

	@Override
	public void onCreation(final Object init) {
		final AgletContext ac = getAgletContext();
		final AgletProxy proxy = getProxy();
		AgletProxy ap;

		addMobilityListener(this);
		addPersistencyListener(this);

		// check if another HostList registration
		ap = (AgletProxy) ac.getProperty("hostlist");
		if ((ap != null) && (ap.isValid())) {
			if (init instanceof Hashtable) {
				try {
					ap.sendMessage(new Message("append", init));
				} catch (final Exception e) {
					System.out.println(e);
				}
			}
			dispose();
		} else {
			ac.setProperty("hostlist", proxy);

			if (init instanceof Hashtable) {
				hostList = (Hashtable) ((Hashtable) init).clone();
			} else {
				hostList = new Hashtable();
			}
			hostList.put(ac.getHostingURL().toString(), "running");
		}
	}

	@Override
	public void onDeactivating(final PersistencyEvent event) {

		// unregistration
		getAgletContext().setProperty("hostlist", null);
	}

	@Override
	public void onDispatching(final MobilityEvent event) {
	}

	public void onRetraction(final MobilityEvent event) {
	}

	@Override
	public void onReverting(final MobilityEvent event) {
	}
}
