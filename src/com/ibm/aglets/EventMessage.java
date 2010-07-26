package com.ibm.aglets;

/*
 * @(#)EventMessage.java
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

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.event.AgletEvent;

final class EventMessage extends MessageImpl {
    AgletEvent event;

    EventMessage(AgletEvent ev) {
	super();
	this.future = new FutureReplyImpl();
	this.event = ev;
    }

    @Override
    public final void handle(LocalAgletRef ref) throws InvalidAgletException {
	ref.dispatchEvent(this.event);
    }

    @Override
    boolean isDelegatable() {
	return false;
    }

    @Override
    public String toString() {
	return "[EventMessage evet = " + this.event + ']';
    }
}
