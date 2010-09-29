package net.sourceforge.aglets.examples.events;

/*
 * @(#)MobilityEvents.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.message.Message;

/**
 * MobilityEvents
 * 
 * Sample program for the use of Mobility Listener. Counts the number of hops
 * and keeps the history of hopping.
 * 
 * @version 1.01
 * @author Yoshiaki Mima
 */
public class MobilityEvents extends Aglet implements MobilityListener {
    /**
     * 
     */
    private static final long serialVersionUID = 4766693893154763601L;
    String history = null; // history
    int count = 0; // hopping counter

    @Override
    public boolean handleMessage(Message msg) {

	if (msg.sameKind("dialog")) { // show history
	    System.out.println(this.history);
	    return true;
	} else if (msg.sameKind("clear")) { // reset history and count
	    this.history = "";
	    this.count = 0;
	    return true;
	}
	return false;
    }

    @Override
    public void onArrival(MobilityEvent ev) {
	this.count += 1;
	this.history += "onArrival: "
	    + this.getAgletContext().getHostingURL().toString() + "\n";
    }

    @Override
    public void onCreation(Object arg) {
	this.history = "[History of MobilityEvents]\n";
	this.count = 0;
	this.addMobilityListener(this);
    }

    @Override
    public void onDispatching(MobilityEvent ev) {
	this.history += "onDispatching: "
	    + this.getAgletContext().getHostingURL().toString() + "\n";
    }

    @Override
    public void onReverting(MobilityEvent ev) {
	this.history += "onReverting: "
	    + this.getAgletContext().getHostingURL().toString() + "\n";
    }

    @Override
    public void run() {
	this.setText("hopping count: " + this.count);
    }
}
