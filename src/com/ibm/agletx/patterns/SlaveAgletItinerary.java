package com.ibm.agletx.patterns;

/*
 * @(#)Slave.java
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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.*;
import java.util.*;
import java.net.*;
import java.io.IOException;

final class SlaveAgletItinerary extends com.ibm.agletx.util.SeqPlanItinerary {

	private Message message = null;

	private boolean inOrigin = false;

	public SlaveAgletItinerary(Aglet aglet, URL url) {
		super(aglet);
		addPlan(url.toString(), "doJob");
	}
	public SlaveAgletItinerary(Aglet aglet, Vector urls) {
		super(aglet);
		for (Enumeration e = urls.elements(); e.hasMoreElements(); ) {
			addPlan(((URL)e.nextElement()).toString(), "doJob");
		} 
	}
	private AgletProxy getProxy() {
		return aglet.getAgletContext().getAgletProxy(aglet.getAgletID());

	}
	public void goOrigin(Message msg) {
		try {
			message = msg;
			goOrigin1();
		} catch (Exception e) {
			e.printStackTrace();
			message = null;
		} 
	}
	private void goOrigin1() throws Exception {
		String origin = getOrigin();

		if (origin == null) {
			throw new AgletException("no origin exists");
		} else {
			inOrigin = true;
			while (true) {		// -- try until you succeed.
				try {
					URL orig = new URL(origin);

					aglet.dispatch(orig);
				} catch (ServerNotFoundException e) {

					// -- do nothing
				} catch (RequestRefusedException e) {

					// -- do nothing
				} catch (Exception e) {
					e.printStackTrace();
					inOrigin = false;
					break;
				} 
			} 
		} 
	}
	public void handleException(Throwable ex) {
		URL host = aglet.getAgletContext().getHostingURL();

		goOrigin(new Message("onError", new SlaveError(host, ex)));
	}
	public void handleTripException(Throwable ex) {

		// modified by Y. Mima 9/10/98
		// if the exception is ServerNotFoundException,
		// Slave does not come home and continue its work
		if (ex instanceof ServerNotFoundException) {
			URL host = aglet.getAgletContext().getHostingURL();

			try {
				aglet.getProxy().sendMessage(new Message("onError", 
														 new SlaveError(host, 
														 ex)));
			} catch (Exception e) {
				handleException(e);
			} 
		} else {
			handleException(ex);
		} 
	}
	public void onArrival(MobilityEvent ev) {
		if (inOrigin == true) {
			try {
				if (message != null) {
					getProxy().sendMessage(message);
					aglet.dispose();
				} 
			} catch (MessageException ex) {
				handleException(ex.getException());
			} catch (NotHandledException ex) {}
			catch (InvalidAgletException ex) {}
		} else {
			super.onArrival(ev);
		} 
	}
	protected void onTermination() {
		goOrigin(new Message("onReturn", null));
	}
	private void print(String text) {
		System.out.println(text);
	}
	public void startTrip() {
		Message msg = new Message("initializeJob", null);

		try {
			getProxy().sendMessage(msg);
		} catch (MessageException ex) {
			handleException(ex.getException());
		} catch (NotHandledException ex) {
			handleException(ex);
		} catch (InvalidAgletException ex) {
			handleException(ex);
		} 
		super.startTrip();

	}
}
