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
	/**
	 * 
	 */
	private static final long serialVersionUID = -3560452166103973047L;
	AgletEvent event;

	EventMessage(final AgletEvent ev) {
		super();
		future = new FutureReplyImpl();
		event = ev;
	}

	@Override
	public final void handle(final LocalAgletRef ref) throws InvalidAgletException {
		ref.dispatchEvent(event);
	}

	@Override
	boolean isDelegatable() {
		return false;
	}

	@Override
	public String toString() {
		return "[EventMessage evet = " + event + ']';
	}
}
