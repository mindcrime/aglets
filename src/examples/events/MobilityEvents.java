package examples.events;

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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.message.Message;

/**
 * MobilityEvents
 * 
 * Sample program for the use of Mobility Listener.
 * Counts the number of hops and keeps the history of hopping.
 * @version     1.01
 * @author	Yoshiaki Mima
 */
public class MobilityEvents extends Aglet implements MobilityListener {
	String history = null;		// history
	int count = 0;				// hopping counter

	public boolean handleMessage(Message msg) {

		if (msg.sameKind("dialog")) {			// show history
			System.out.println(history);
			return true;
		} else if (msg.sameKind("clear")) {		// reset history and count
			history = "";
			count = 0;
			return true;
		} 
		return false;
	}
	public void onArrival(MobilityEvent ev) {
		count += 1;
		history += "onArrival: " 
				   + getAgletContext().getHostingURL().toString() + "\n";
	}
	public void onCreation(Object arg) {
		history = "[History of MobilityEvents]\n";
		count = 0;
		addMobilityListener(this);
	}
	public void onDispatching(MobilityEvent ev) {
		history += "onDispatching: " 
				   + getAgletContext().getHostingURL().toString() + "\n";
	}
	public void onReverting(MobilityEvent ev) {
		history += "onReverting: " 
				   + getAgletContext().getHostingURL().toString() + "\n";
	}
	public void run() {
		setText("hopping count: " + count);
	}
}
