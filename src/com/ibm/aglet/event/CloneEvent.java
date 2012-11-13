package com.ibm.aglet.event;

/*
 * @(#)CloneEvent.java
 * 
 * (c) Copyright IBM Corp. 1997, 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import com.ibm.aglet.AgletProxy;

/**
 * The clon event occurs when the cloning of an aglet is attempted.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class CloneEvent extends AgletEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5794430523051887230L;

	/**
	 * Constructs the clone event object with the specified id and aglet
	 */
	@Deprecated
	public CloneEvent(final int id, final AgletProxy aglet) {
		super(aglet, id);
	}

	/**
	 * Creates the event of the specified type.
	 * 
	 * @param id
	 * @param proxy
	 * @param type
	 */
	public CloneEvent(final int id, final AgletProxy proxy, final EventType type) {
		super(proxy, id, type);
	}

	/**
	 * Returns the aglet proxy which is the source of the event.
	 */
	public AgletProxy getAgletProxy() {
		return (AgletProxy) source;
	}

	@Override
	public String toString() {
		return "CloneEvent[" + getEventType().toString() + "]";
	}
}
