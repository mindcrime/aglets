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

import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.RequestRefusedException;
import com.ibm.aglet.ServerNotFoundException;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;

final class SlaveAgletItinerary extends com.ibm.agletx.util.SeqPlanItinerary {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5753313996227566152L;

	private Message message = null;

	private boolean inOrigin = false;

	public SlaveAgletItinerary(final Aglet aglet, final URL url) {
		super(aglet);
		this.addPlan(url.toString(), "doJob");
	}

	public SlaveAgletItinerary(final Aglet aglet, final Vector urls) {
		super(aglet);
		for (final Enumeration e = urls.elements(); e.hasMoreElements();) {
			this.addPlan(((URL) e.nextElement()).toString(), "doJob");
		}
	}

	private AgletProxy getProxy() {
		return aglet.getAgletContext().getAgletProxy(aglet.getAgletID());

	}

	public void goOrigin(final Message msg) {
		try {
			message = msg;
			goOrigin1();
		} catch (final Exception e) {
			e.printStackTrace();
			message = null;
		}
	}

	private void goOrigin1() throws Exception {
		final String origin = getOrigin();

		if (origin == null) {
			throw new AgletException("no origin exists");
		} else {
			inOrigin = true;
			while (true) { // -- try until you succeed.
				try {
					final URL orig = new URL(origin);

					aglet.dispatch(orig);
				} catch (final ServerNotFoundException e) {

					// -- do nothing
				} catch (final RequestRefusedException e) {

					// -- do nothing
				} catch (final Exception e) {
					e.printStackTrace();
					inOrigin = false;
					break;
				}
			}
		}
	}

	@Override
	public void handleException(final Throwable ex) {
		final URL host = aglet.getAgletContext().getHostingURL();

		goOrigin(new Message("onError", new SlaveError(host, ex)));
	}

	@Override
	public void handleTripException(final Throwable ex) {

		// modified by Y. Mima 9/10/98
		// if the exception is ServerNotFoundException,
		// Slave does not come home and continue its work
		if (ex instanceof ServerNotFoundException) {
			final URL host = aglet.getAgletContext().getHostingURL();

			try {
				aglet.getProxy().sendMessage(new Message("onError", new SlaveError(host, ex)));
			} catch (final Exception e) {
				handleException(e);
			}
		} else {
			handleException(ex);
		}
	}

	@Override
	public void onArrival(final MobilityEvent ev) {
		if (inOrigin == true) {
			try {
				if (message != null) {
					getProxy().sendMessage(message);
					aglet.dispose();
				}
			} catch (final MessageException ex) {
				handleException(ex.getException());
			} catch (final NotHandledException ex) {
			} catch (final InvalidAgletException ex) {
			}
		} else {
			super.onArrival(ev);
		}
	}

	@Override
	protected void onTermination() {
		goOrigin(new Message("onReturn", null));
	}

	@Override
	public void startTrip() {
		final Message msg = new Message("initializeJob", null);

		try {
			getProxy().sendMessage(msg);
		} catch (final MessageException ex) {
			handleException(ex.getException());
		} catch (final NotHandledException ex) {
			handleException(ex);
		} catch (final InvalidAgletException ex) {
			handleException(ex);
		}
		super.startTrip();

	}
}
