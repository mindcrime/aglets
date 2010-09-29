package com.ibm.agletx.util;

/*
 * @(#)SeqPlanItinerary.java
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

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;

class SeqPlanTask extends Task implements Runnable {

    /**
     * 
     */
    private static final long serialVersionUID = -3091321402129441206L;
    private Message msg = null;
    private SeqItinerary itin = null;

    public SeqPlanTask(Message msg) {
	this.msg = msg;
    }

    @Override
    public void execute(SeqItinerary itin) throws Exception {
	this.itin = itin;
	new Thread(this).start();
    }

    public Message getMessage() {
	return this.msg;
    }

    @Override
    public void run() {
	AgletProxy p = this.itin.getOwnerAglet();

	try {
	    p.sendMessage(this.msg);
	} catch (Exception ex) {
	    this.itin.handleException(ex);
	}
	if (this.itin.atLastDestination() & this.itin.isRepeat()) {
	    this.itin.startTrip();
	} else {
	    this.itin.goToNext();
	}
    }
}
