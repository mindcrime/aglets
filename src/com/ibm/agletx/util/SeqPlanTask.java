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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;

import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;

class SeqPlanTask extends Task implements Runnable {

	private Message msg = null;
	private AgletProxy p = null;

	private SeqItinerary itin = null;

	public SeqPlanTask(Message msg) {
		this.msg = msg;
	}
	public void execute(SeqItinerary itin) throws Exception {
		this.itin = itin;
		new Thread(this).start();
	}
	public Message getMessage() {
		return msg;
	}
	public void run() {
		AgletProxy p = itin.getOwnerAglet();

		try {
			p.sendMessage(msg);
		} catch (Exception ex) {
			itin.handleException(ex);
		} 
		if (itin.atLastDestination() & itin.isRepeat()) {
			itin.startTrip();
		} else {
			itin.goToNext();
		} 
	}
}
