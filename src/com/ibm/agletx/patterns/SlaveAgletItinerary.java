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

    public SlaveAgletItinerary(Aglet aglet, URL url) {
	super(aglet);
	this.addPlan(url.toString(), "doJob");
    }

    public SlaveAgletItinerary(Aglet aglet, Vector urls) {
	super(aglet);
	for (Enumeration e = urls.elements(); e.hasMoreElements();) {
	    this.addPlan(((URL) e.nextElement()).toString(), "doJob");
	}
    }

    private AgletProxy getProxy() {
	return this.aglet.getAgletContext().getAgletProxy(this.aglet.getAgletID());

    }

    public void goOrigin(Message msg) {
	try {
	    this.message = msg;
	    this.goOrigin1();
	} catch (Exception e) {
	    e.printStackTrace();
	    this.message = null;
	}
    }

    private void goOrigin1() throws Exception {
	String origin = this.getOrigin();

	if (origin == null) {
	    throw new AgletException("no origin exists");
	} else {
	    this.inOrigin = true;
	    while (true) { // -- try until you succeed.
		try {
		    URL orig = new URL(origin);

		    this.aglet.dispatch(orig);
		} catch (ServerNotFoundException e) {

		    // -- do nothing
		} catch (RequestRefusedException e) {

		    // -- do nothing
		} catch (Exception e) {
		    e.printStackTrace();
		    this.inOrigin = false;
		    break;
		}
	    }
	}
    }

    @Override
    public void handleException(Throwable ex) {
	URL host = this.aglet.getAgletContext().getHostingURL();

	this.goOrigin(new Message("onError", new SlaveError(host, ex)));
    }

    @Override
    public void handleTripException(Throwable ex) {

	// modified by Y. Mima 9/10/98
	// if the exception is ServerNotFoundException,
	// Slave does not come home and continue its work
	if (ex instanceof ServerNotFoundException) {
	    URL host = this.aglet.getAgletContext().getHostingURL();

	    try {
		this.aglet.getProxy().sendMessage(new Message("onError", new SlaveError(host, ex)));
	    } catch (Exception e) {
		this.handleException(e);
	    }
	} else {
	    this.handleException(ex);
	}
    }

    @Override
    public void onArrival(MobilityEvent ev) {
	if (this.inOrigin == true) {
	    try {
		if (this.message != null) {
		    this.getProxy().sendMessage(this.message);
		    this.aglet.dispose();
		}
	    } catch (MessageException ex) {
		this.handleException(ex.getException());
	    } catch (NotHandledException ex) {
	    } catch (InvalidAgletException ex) {
	    }
	} else {
	    super.onArrival(ev);
	}
    }

    @Override
    protected void onTermination() {
	this.goOrigin(new Message("onReturn", null));
    }

    @Override
    public void startTrip() {
	Message msg = new Message("initializeJob", null);

	try {
	    this.getProxy().sendMessage(msg);
	} catch (MessageException ex) {
	    this.handleException(ex.getException());
	} catch (NotHandledException ex) {
	    this.handleException(ex);
	} catch (InvalidAgletException ex) {
	    this.handleException(ex);
	}
	super.startTrip();

    }
}
