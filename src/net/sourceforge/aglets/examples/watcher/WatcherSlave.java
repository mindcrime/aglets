package net.sourceforge.aglets.examples.watcher;

/*
 * @(#)WatcherSlave.java
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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.event.MobilityAdapter;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.PersistencyAdapter;
import com.ibm.aglet.event.PersistencyEvent;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageManager;

/**
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class WatcherSlave extends Aglet {
    /**
     * 
     */
    private static final long serialVersionUID = 2892337996219553685L;
    AgletProxy master;
    boolean started = false;

    public String getInfo() {
	AgletContext ac = this.getAgletContext();
	StringBuffer b = new StringBuffer();

	Enumeration e = ac.getAgletProxies(ACTIVE | INACTIVE);

	while (e.hasMoreElements()) {
	    try {
		AgletProxy p = (AgletProxy) e.nextElement();
		AgletInfo info = p.getAgletInfo();

		b.append(info.toString());
		b.append("\n---------\n");
	    } catch (InvalidAgletException ex) {
		b.append("[InvalidAglet]\n");
		continue;
	    }
	}
	return b.toString();
    }

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("start")) {

	    // to work around a bug in framework..... sorry.
	    msg.sendReply();

	    this.start();
	} else if (msg.sameKind("stop")) {
	    this.stop();

	} else if (msg.sameKind("gonext")) {
	    try {
		this.dispatch((URL) msg.getArg());
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }

	} else if (msg.sameKind("sleep")) {

	    // sleep at most 10 seconds.
	    try {
		this.deactivate(10 * 1000);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	} else if (msg.sameKind("getInfo")) {
	    msg.sendReply(this.getInfo());

	} else {
	    return false;
	}
	return true;
    }

    @Override
    public void onCreation(Object o) {
	if (o instanceof AgletID) {
	    this.master = this.getAgletContext().getAgletProxy((AgletID) o);
	} else if (o instanceof AgletProxy) {
	    this.master = (AgletProxy) o;
	} else {
	    this.master = null;
	}

	//
	// Activate if these messages arrives.
	//
	this.getMessageManager().setPriority("start", MessageManager.ACTIVATE_AGLET);
	this.getMessageManager().setPriority(Message.DISPOSE, MessageManager.ACTIVATE_AGLET);

	//
	// event listener
	//
	this.addPersistencyListener(new PersistencyAdapter() {
	    /**
	     * 
	     */
	    private static final long serialVersionUID = -1788859594009985012L;

	    @Override
	    public void onActivation(PersistencyEvent ev) {
		WatcherSlave.this.setText("wakeup");

		//
		// Start monitoring if it was monitoring
		//
		if (WatcherSlave.this.started) {
		    WatcherSlave.this.started = false;
		    WatcherSlave.this.start();
		}
	    }
	});
	this.addMobilityListener(new MobilityAdapter() {
	    /**
	     * 
	     */
	    private static final long serialVersionUID = -1615892152139033000L;

	    @Override
	    public void onArrival(MobilityEvent ev) {
		WatcherSlave.this.setText("arrived");

		//
		// Start monitoring if it was monitoring
		//
		if (WatcherSlave.this.started) {
		    WatcherSlave.this.started = false;
		    WatcherSlave.this.start();
		}
	    }
	});
    }

    /**
     * Start monitoring
     */
    public void start() {
	if (this.started || (this.master == null)) {
	    return;
	}
	this.setText("started");
	this.started = true;

	while (this.started) {
	    try {
		this.master.sendAsyncMessage(new Message("update", this.getInfo()));
	    } catch (InvalidAgletException ex) {
		ex.printStackTrace();
		this.dispose();
	    }
	    this.waitMessage(2 * 1000); // wait two seconds.
	}
	this.setText("stopped");
    }

    public void stop() {
	this.started = false;
	this.notifyMessage();
    }
}
