package com.ibm.aglet.event;

/*
 * @(#)PersistencyEvent.java
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
 * The persistency event
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class PersistencyEvent extends AgletEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4023883775091603405L;
	/**
	 * The duration of the persistency.
	 */
	private final long duration;

	/**
	 * Constructs a PersistencyEvent with the specified id, aglet proxy and
	 * duration.
	 */
	public PersistencyEvent(final AgletProxy aglet, final long duration, final EventType type) {
		super(aglet, AgletEvent.nextID(), type);
		this.duration = duration;
	}

	/**
	 * Returns the aglet proxy which is the source of the event.
	 */
	public AgletProxy getAgletProxy() {
		return (AgletProxy) source;
	}

	/**
	 * Gets the duration
	 */
	public long getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return "PersistencyEvent[" + getEventType().toString() + "]";
	}
}
