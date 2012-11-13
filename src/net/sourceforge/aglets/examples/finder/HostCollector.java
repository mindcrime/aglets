package net.sourceforge.aglets.examples.finder;

/*
 * @(#)HostCollector.java
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
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.message.Message;

/**
 * The HostCollector carries a list of aglet server names. When it moves to
 * another server, it adds current server address to the list. If there is no
 * HostList aglet is running, it creates an instance of HostList on that server.
 * If a HostList aglet is running, it appends a list of server name to list in
 * the HostList and then get the list belongs to the HostList.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Yoshiaki Mima
 * @see net.sourceforge.aglets.examples.finder.HostList
 */

public class HostCollector extends Aglet implements MobilityListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7122217680888792625L;
	Hashtable hostList;

	public void appendList(final Hashtable list) {
		for (final Enumeration e = list.keys(); e.hasMoreElements();) {
			final Object key = e.nextElement();

			if (hostList.get(key) == null) {
				System.out.println("new: " + key);
				hostList.put(key, "new");
			} else {
				System.out.println("key: " + key + " value: "
						+ hostList.get(key));
			}
		}
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("dialog")) {
			try {
				System.out.println(hostList);
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			return true;
		} else if (msg.sameKind("shutdown")) {
			try {
				deactivate(0);
			} catch (final Exception e) {
			}
			return true;
		} else {
		}
		return true;
	}

	@Override
	public void onArrival(final MobilityEvent event) {
		AgletProxy ap = (AgletProxy) getAgletContext().getProperty("hostlist");

		hostList.put(getAgletContext().getHostingURL().toString(), "running");

		try {
			if ((ap == null) || !ap.isValid()) {
				ap = getAgletContext().createAglet(getCodeBase(), "examples.finder.HostList", hostList);
			}

			ap.sendMessage(new Message("append", hostList));
			final Hashtable list = (Hashtable) ap.sendMessage(new Message("getlist"));

			if (list != null) {
				appendList(list);
			}
		} catch (final Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void onCreation(final Object init) {
		hostList = new Hashtable();
		AgletProxy ap = (AgletProxy) getAgletContext().getProperty("hostlist");

		hostList.put(getAgletContext().getHostingURL().toString(), "running");

		try {
			if ((ap == null) || !ap.isValid()) {
				ap = getAgletContext().createAglet(getCodeBase(), "examples.finder.HostList", hostList);
			}
		} catch (final Exception e) {
			System.out.println(e);
		}

		addMobilityListener(this);
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
