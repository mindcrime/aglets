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
 * @version     1.00    $Date: 2001/07/28 06:34:10 $
 * @author      Mitsuru Oshima
 */
public class CloneEvent extends AgletEvent {

	/**
	 * Marks the first integer id for the range of clone event ids.
	 */
	public static final int AGLET_CLONE_FIRST = 1100;

	/**
	 * Marks the last integer id for the range of clone event ids.
	 */
	public static final int AGLET_CLONE_LAST = 1102;

	/**
	 * The CLONING event type is delivered when the aglet is attempted to
	 * clone.
	 */
	public static final int CLONING = AGLET_CLONE_FIRST;

	/**
	 * The CLONE event type is delivered when the clone of the aglet
	 * is created. Note that this event is delivered only to
	 * the cloned object but not to the original.
	 */
	public static final int CLONE = AGLET_CLONE_FIRST + 1;

	/**
	 * The CLONED event type is delivered after the cloning of the aglet
	 * finished. Note that this event is delivered only to
	 * the original aglet.
	 */
	public static final int CLONED = AGLET_CLONE_FIRST + 2;

	private static String name[] = {
		"CLONEING", "CLONE", "CLONED", 
	};

	/**
	 * Constructs the clone event object with the specified id and aglet
	 */
	public CloneEvent(int id, AgletProxy aglet) {
		super(aglet, id);
	}
	/**
	 * Returns the aglet proxy which is the source of the event.
	 */
	public AgletProxy getAgletProxy() {
		return (AgletProxy)source;
	}
	public String toString() {
		return "CloneEvent[" + name[id - AGLET_CLONE_FIRST] + "]";
	}
}
