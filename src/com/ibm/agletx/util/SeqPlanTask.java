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

	public SeqPlanTask(final Message msg) {
		this.msg = msg;
	}

	@Override
	public void execute(final SeqItinerary itin) throws Exception {
		this.itin = itin;
		new Thread(this).start();
	}

	public Message getMessage() {
		return msg;
	}

	@Override
	public void run() {
		final AgletProxy p = itin.getOwnerAglet();

		try {
			p.sendMessage(msg);
		} catch (final Exception ex) {
			itin.handleException(ex);
		}
		if (itin.atLastDestination() & itin.isRepeat()) {
			itin.startTrip();
		} else {
			itin.goToNext();
		}
	}
}
