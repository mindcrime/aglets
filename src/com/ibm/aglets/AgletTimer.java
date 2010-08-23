package com.ibm.aglets;

/*
 * @(#)AgletTimer.java
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
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.Date;
import java.util.Enumeration;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.maf.MAFUtil;

/**
 * The <tt>AgletTimer</tt> class is the time manager.
 * 
 * @version 1.00 96/07/08
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 */

final class AgletTimer implements Runnable {

    /*
     * aglet context
     */
    private AgletContextImpl _context = null;

    /*
     * Timer request chain.
     */
    private DeactivationInfo top = null;

    /*
     * Handler
     */
    Thread handler = null;

    // -- Create an aglet timer.
    AgletTimer(AgletContextImpl context) {
	this._context = context;
	this.top = new DeactivationInfo(null, 0, null);
	this.top.wakeup = 0;
	this.top.next = null;
    }

    synchronized void add(DeactivationInfo dinfo) {

	if (dinfo.wakeup == 0) {

	    // ignore
	    // this aglet will be activated after rebooting.
	    return;
	}

	DeactivationInfo tmp;

	for (tmp = this.top; (tmp.next != null)
	&& (tmp.next.wakeup < dinfo.wakeup); tmp = tmp.next) {
	}
	dinfo.next = tmp.next;
	tmp.next = dinfo;
	this.notifyAll();
    }

    synchronized void destroy() {
	if (this.handler != null) {
	    this.handler.stop();
	    this.handler = null;
	}
    }

    public void list(PrintStream out) {
	System.out.println("======= Deactivated Aglets ==========");
	for (DeactivationInfo t = this.top.next; t != null; t = t.next) {
	    System.out.println(t.key + " : " + new Date(t.wakeup));
	}
    }

    synchronized DeactivationInfo popInfo() {
	if (this.top.next != null) {
	    DeactivationInfo info = this.top.next;

	    this.top.next = this.top.next.next;
	    return info;
	}
	return null;
    }

    /*
     * recover timer state from
     */
    void recoverTimer(Persistence persistence) throws AgletException {
	Enumeration e = persistence.entryKeys();

	while (e.hasMoreElements()) {
	    String key = (String) e.nextElement();
	    PersistentEntry entry = persistence.getEntry(key);
	    ObjectInputStream in = null;

	    try {
		in = new ObjectInputStream(entry.getInputStream());

		// tentative
		DeactivationInfo dinfo = (DeactivationInfo) in.readObject();

		//
		// Construct message manager for deactivated aglet
		//

		MessageManagerImpl mm = (MessageManagerImpl) in.readObject();

		mm.setState(MessageManagerImpl.DEACTIVATED);

		LocalAgletRef ref = new LocalAgletRef(this._context);

		ref.setName(dinfo.agent_name);

		ref.setMessageManager(mm);

		// Class Names
		in.readObject();

		int len = in.readInt();
		byte[] agent = new byte[len];

		in.readFully(agent);

		AgletReader reader = new AgletReader(agent);

		reader.readInfo(ref);

		if (dinfo.wakeup == 0) {
		    dinfo.wakeup = System.currentTimeMillis();
		}

		/*
		 * if (key.equalsIgnoreCase(ref.info.getAgletID().toString() )
		 * == false) { System.out.println("wrong AgletID in reading:" +
		 * key + " != " + ref.info.getAgletID()); // in.close();
		 * continue; }
		 */
		ref.proxy = new AgletProxyImpl(ref);
		ref.validate(this._context, LocalAgletRef.INACTIVE);

		this.add(dinfo);

		// new Event(key, dinfo.wakeup, 0, 0, 0, 0, 0));
		// in.close();
	    } catch (StreamCorruptedException ex) {
		continue;
	    } catch (ClassNotFoundException ex) {
		ex.printStackTrace();
	    } catch (IOException ex) {
		ex.printStackTrace();
	    } finally {
		if (in != null) {
		    try {
			in.close();
		    } catch (Exception ex) {
		    }
		}
	    }
	}
    }

    synchronized void removeInfo(String target_key) {
	DeactivationInfo tmp;

	for (tmp = this.top; tmp.next != null; tmp = tmp.next) {

	    if (tmp.next.key.equals(target_key)) {
		tmp.next = tmp.next.next;
		return;
	    }
	}
    }

    @Override
    public void run() {
	long sleeptime = 0;

	while (true) {
	    DeactivationInfo info = this.topInfo();

	    sleeptime = info.wakeup - System.currentTimeMillis();
	    if (sleeptime > 0) {
		synchronized (this) {
		    try {
			this.wait(sleeptime);
		    } catch (InterruptedException ex) {
			ex.printStackTrace();
		    }
		}
	    } else {
		AgletID id = MAFUtil.toAgletID(info.agent_name);

		try {
		    com.ibm.aglet.AgletProxy proxy = this._context.getAgletProxy(id);

		    if (proxy != null) {
			proxy.activate();
		    } else {
			System.out.println("Proxy Not Found");
			this.removeInfo(info.key);
		    }
		} catch (IOException ex) {
		    ex.printStackTrace();
		} catch (AgletException ex) {
		    ex.printStackTrace();
		}
	    }
	}
    }

    synchronized void start() {
	if (this.handler == null) {
	    this.handler = new Thread(this);
	    this.handler.start();
	}
    }

    synchronized DeactivationInfo topInfo() {
	while (this.top.next == null) {
	    try {
		this.wait();
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }
	}
	return this.top.next;
    }
}
