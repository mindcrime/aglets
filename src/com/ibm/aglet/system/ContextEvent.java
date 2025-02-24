package com.ibm.aglet.system;

/*
 * @(#)ContextEvent.java
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

import java.net.URL;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.event.AgletEvent;
import com.ibm.aglet.event.EventType;

/**
 * Context level event
 * 
 * @version 1.50 $Date: 2009/07/28 07:04:54 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */
public class ContextEvent extends AgletEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7387680262581311719L;

	/**
	 * AgletProxy proxy
	 */
	protected AgletProxy agletproxy;

	/**
	 * Arbitary arguments
	 */
	public Object arg = null;

	/**
	 * Constructs an ContextEvent with the specified type.
	 */
	public ContextEvent(final Object context, final AgletProxy target, final EventType type) {
		super(context, AgletEvent.nextID(), type);
		agletproxy = target;
	}

	/**
	 * Constructs an ContextEvent with the specified type.
	 */
	public ContextEvent(final Object context, final AgletProxy target, final Object arg,
	                    final EventType type) {
		this(context, target, type);
		agletproxy = target;
		this.arg = arg;
	}

	/**
	 * Gets AgletContext object of this event
	 */
	public AgletContext getAgletContext() {
		return (AgletContext) source;
	}

	/**
	 * Gets AgletProxy object of this event null if the event is STARTED or
	 * STOPPED
	 */
	public AgletProxy getAgletProxy() {
		return agletproxy;
	}

	/**
	 * Gets the document URL.
	 */
	public final URL getDocumentURL() {
		if (EventType.SHOW_DOCUMENT.equals(getEventType())) {
			return (URL) arg;
		} else {
			throw new IllegalAccessError("Event is not SHOW_DOCUMENT");
		}
	}

	/**
	 * Gets the message to show
	 */
	public final String getMessage() {
		if (EventType.AGLET_MESSAGE.equals(getEventType())) {
			return (String) arg;
		} else {
			throw new IllegalAccessError("Event is not MESSAGE!");
		}
	}

	/**
	 * Provides the text for a state change.
	 * 
	 * @return the state change text
	 */
	public final String getText() {
		if (EventType.AGLET_STATE_CHANGED.equals(getEventType())) {
			return (String) arg;
		} else {
			throw new IllegalAccessError("Event is not STATE_CHANGED");
		}
	}

	@Override
	public String toString() {
		return "ContextEvent[" + getEventType() + "]";
	}
}
