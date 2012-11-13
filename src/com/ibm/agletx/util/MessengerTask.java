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

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;

class MessengerTask extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1963644597621257467L;
	private AgletID id = null;

	public MessengerTask(final AgletID id) {
		this.id = id;
	}

	@Override
	public void execute(final SeqItinerary itin) throws Exception {
		final AgletContext ctx = itin.getOwnerAglet().getAglet().getAgletContext();
		final AgletProxy p = ctx.getAgletProxy(id);
		final Message msg = ((MessengerItinerary) itin).getMessage();

		p.sendAsyncMessage(msg);
	}

	public AgletID getAgletID() {
		return id;
	}
}
