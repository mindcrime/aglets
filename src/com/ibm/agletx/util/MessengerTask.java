package com.ibm.agletx.util;

/*
 * @(#)MessengerItinerary.java
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
import java.io.IOException;

class MessengerTask extends Task {

	private AgletID id = null;

	public MessengerTask(AgletID id) {
		this.id = id;
	}
	public void execute(SeqItinerary itin) throws Exception {
		AgletContext ctx = itin.getOwnerAglet().getAglet().getAgletContext();
		AgletProxy p = ctx.getAgletProxy(id);
		Message msg = ((MessengerItinerary)itin).getMessage();

		p.sendAsyncMessage(msg);
	}
	public AgletID getAgletID() {
		return id;
	}
}
