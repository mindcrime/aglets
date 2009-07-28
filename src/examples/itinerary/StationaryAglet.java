package examples.itinerary;

/*
 * @(#)StationaryAglet.java
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
import com.ibm.agletx.patterns.Meeting;

public final class StationaryAglet extends Aglet implements MobilityListener {

	private Meeting meeting = null;
	boolean dispatched = false;

	public StationaryAglet() {}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("dispose")) {
			try {
				dispose();
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} else if ((meeting != null) && (msg.sameKind(meeting.getID()))) {
			meet((AgletID)(msg.getArg()));
			msg.sendReply(getAgletID());
			dispose();
			return true;
		} 
		return false;
	}
	private void meet(AgletID id) {
		print("[" + getAgletID() + "] I met with VisitingAglet [id=" + id 
			  + "]");
	}
	public void onArrival(MobilityEvent ev) {
		print("on Arrival");
		dispatched = true;
		try {
			meeting.ready(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	public void onCreation(Object ini) {
		print("created!");
		addMobilityListener(this);
		meeting = (Meeting)ini;
	}
	public void onDispatching(MobilityEvent ev) {
		if (dispatched) {
			throw new SecurityException("Don't try to move me!!");
		} 
	}
	public void onDisposing() {
		print("disposed!!");
	}
	public void onReverting(MobilityEvent ev) {
		throw new SecurityException();
	}
	private void print(String txt) {
		System.out.println(">>>StationaryAglet:" + txt);
	}
}
