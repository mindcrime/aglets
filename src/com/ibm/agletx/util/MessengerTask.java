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

    private AgletID id = null;

    public MessengerTask(AgletID id) {
	this.id = id;
    }

    @Override
    public void execute(SeqItinerary itin) throws Exception {
	AgletContext ctx = itin.getOwnerAglet().getAglet().getAgletContext();
	AgletProxy p = ctx.getAgletProxy(this.id);
	Message msg = ((MessengerItinerary) itin).getMessage();

	p.sendAsyncMessage(msg);
    }

    public AgletID getAgletID() {
	return this.id;
    }
}
