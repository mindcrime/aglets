package net.sourceforge.aglets.examples.itinerary;

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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.patterns.Meeting;

public final class StationaryAglet extends Aglet implements MobilityListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4884771624206640655L;
	private Meeting meeting = null;
	boolean dispatched = false;

	public StationaryAglet() {
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("dispose")) {
			try {
				dispose();
				return true;
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		} else if ((meeting != null)
				&& (msg.sameKind(meeting.getID()))) {
			meet((AgletID) (msg.getArg()));
			msg.sendReply(getAgletID());
			dispose();
			return true;
		}
		return false;
	}

	private void meet(final AgletID id) {
		print("[" + getAgletID() + "] I met with VisitingAglet [id="
				+ id + "]");
	}

	@Override
	public void onArrival(final MobilityEvent ev) {
		print("on Arrival");
		dispatched = true;
		try {
			meeting.ready(this);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onCreation(final Object ini) {
		print("created!");
		addMobilityListener(this);
		meeting = (Meeting) ini;
	}

	@Override
	public void onDispatching(final MobilityEvent ev) {
		if (dispatched) {
			throw new SecurityException("Don't try to move me!!");
		}
	}

	@Override
	public void onDisposing() {
		print("disposed!!");
	}

	@Override
	public void onReverting(final MobilityEvent ev) {
		throw new SecurityException();
	}

	private void print(final String txt) {
		System.out.println(">>>StationaryAglet:" + txt);
	}
}
