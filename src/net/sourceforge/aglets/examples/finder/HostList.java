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
    Hashtable hostList;

    public void appendList(Hashtable list) {
	for (Enumeration e = list.keys(); e.hasMoreElements();) {
	    Object key = e.nextElement();

	    if (list.get(key) instanceof String) {
		if (this.hostList.get(key) == null) {
		    System.out.println("new: " + key);
		    this.hostList.put(key, "new");
		} else {

		    // System.out.println("key: " + key + " value: " +
		    // hostList.get(key));
		}
	    }
	}
    }

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("dialog")) {
	    try {
		int i = 0;

		System.out.println("HostList -- begin");
		for (Enumeration e = this.hostList.keys(); e.hasMoreElements();) {
		    System.out.println(i++ + ": " + e.nextElement());
		}
		System.out.println("HostList -- end");
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    return true;
	} else if (msg.sameKind("register")) {
	    if (msg.getArg() instanceof String) {
		this.hostList.put(msg.getArg(), "running");
	    }
	    return true;
	} else if (msg.sameKind("append")) {
	    if (msg.getArg() instanceof Hashtable) {
		this.appendList((Hashtable) msg.getArg());
	    }
	    return true;
	} else if (msg.sameKind("getlist")) {
	    msg.sendReply(this.hostList);
	    return true;
	} else if (msg.sameKind("shutdown")) {
	    try {
		this.deactivate(0);
	    } catch (Exception e) {
	    }
	    return true;
	} else if (msg.sameKind("dispose")) {
	    this.dispose();
	    return true;
	} else {
	}
	return true;
    }

    @Override
    public void onActivation(PersistencyEvent event) {
	AgletContext ac = this.getAgletContext();
	AgletProxy proxy = this.getProxy();
	AgletProxy ap;

	// check if another HostList registration
	ap = (AgletProxy) ac.getProperty("hostlist");
	if ((ap != null) && (ap.isValid())) {
	    try {
		ap.sendMessage(new Message("append", this.hostList));
	    } catch (Exception e) {
		System.out.println(e);
	    }
	    this.dispose();
	} else {
	    ac.setProperty("hostlist", proxy);
	}
    }

    @Override
    public void onArrival(MobilityEvent event) {
	this.dispose();
    }

    @Override
    public void onCreation(Object init) {
	AgletContext ac = this.getAgletContext();
	AgletProxy proxy = this.getProxy();
	AgletProxy ap;

	this.addMobilityListener(this);
	this.addPersistencyListener(this);

	// check if another HostList registration
	ap = (AgletProxy) ac.getProperty("hostlist");
	if ((ap != null) && (ap.isValid())) {
	    if (init instanceof Hashtable) {
		try {
		    ap.sendMessage(new Message("append", init));
		} catch (Exception e) {
		    System.out.println(e);
		}
	    }
	    this.dispose();
	} else {
	    ac.setProperty("hostlist", proxy);

	    if (init instanceof Hashtable) {
		this.hostList = (Hashtable) ((Hashtable) init).clone();
	    } else {
		this.hostList = new Hashtable();
	    }
	    this.hostList.put(ac.getHostingURL().toString(), "running");
	}
    }

    @Override
    public void onDeactivating(PersistencyEvent event) {

	// unregistration
	this.getAgletContext().setProperty("hostlist", null);
    }

    @Override
    public void onDispatching(MobilityEvent event) {
    }

    public void onRetraction(MobilityEvent event) {
    }

    @Override
    public void onReverting(MobilityEvent event) {
    }
}
