package com.ibm.aglet.event;

/*
 * @(#)AgletEventListener.java
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

import java.util.Enumeration;
import java.util.Vector;

/**
 * The aglet event listener class is a container class for all aglet related
 * listener. It is not normally used by the aglet programmers.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class AgletEventListener implements CloneListener, MobilityListener,
	PersistencyListener {
    Vector vector = new Vector();

    public AgletEventListener() {
    }

    /**
     * Constructs an AgletEventlistener object with specified two clone listener
     * objects.
     */
    public AgletEventListener(CloneListener l1, CloneListener l2) {
	this.vector.addElement(l1);
	this.vector.addElement(l2);
    }

    /**
     * Constructs an AgletEventlistener object with specified two mobility
     * listener objects.
     */
    public AgletEventListener(MobilityListener l1, MobilityListener l2) {
	this.vector.addElement(l1);
	this.vector.addElement(l2);
    }

    /**
     * Constructs an AgletEventlistener object with specified two persistency
     * listener objects.
     */
    public AgletEventListener(PersistencyListener l1, PersistencyListener l2) {
	this.vector.addElement(l1);
	this.vector.addElement(l2);
    }

    /**
     * Adds the specified clone listener object
     */
    public void addCloneListener(CloneListener listener) {
	if (this.vector.contains(listener)) {
	    return;
	}
	this.vector.addElement(listener);
    }

    /**
     * Adds the specified mobility listener object
     */
    public void addMobilityListener(MobilityListener listener) {
	if (this.vector.contains(listener)) {
	    return;
	}
	this.vector.addElement(listener);
    }

    /**
     * Adds the specified persistency listener object
     */
    public void addPersistencyListener(PersistencyListener listener) {
	if (this.vector.contains(listener)) {
	    return;
	}
	this.vector.addElement(listener);
    }

    /**
     * Calls the onActivation methods on the listers with the specified
     * persistency event.
     */
    public void onActivation(PersistencyEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((PersistencyListener) e.nextElement()).onActivation(ev);
	}
    }

    /**
     * Calls the onArrival methods on the listers with the specified mobility
     * event.
     */
    public void onArrival(MobilityEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((MobilityListener) e.nextElement()).onArrival(ev);
	}
    }

    /**
     * Calls the onClone methods on the listers with the specified Clone event.
     */
    public void onClone(CloneEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((CloneListener) e.nextElement()).onClone(ev);
	}
    }

    /**
     * Calls the onCloned methods on the listers with the specified Clone event.
     */
    public void onCloned(CloneEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((CloneListener) e.nextElement()).onCloned(ev);
	}
    }

    /**
     * Calls the onCloning methods on the listers with the specified Clone
     * event.
     */
    public void onCloning(CloneEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((CloneListener) e.nextElement()).onCloning(ev);
	}
    }

    /**
     * Calls the onDeactivating methods on the listers with the specified
     * persistency event.
     */
    public void onDeactivating(PersistencyEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((PersistencyListener) e.nextElement()).onDeactivating(ev);
	}
    }

    /**
     * Calls the onDispatching methods on the listers with the specified
     * mobility event.
     */
    public void onDispatching(MobilityEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((MobilityListener) e.nextElement()).onDispatching(ev);
	}
    }

    /**
     * Calls the onReverting methods on the listers with the specified mobility
     * event.
     */
    public void onReverting(MobilityEvent ev) {
	Enumeration e = this.vector.elements();

	while (e.hasMoreElements()) {
	    ((MobilityListener) e.nextElement()).onReverting(ev);
	}
    }

    /**
     * Removes the specified clone listener object
     */
    public void removeCloneListener(CloneListener listener) {
	this.vector.removeElement(listener);
    }

    /**
     * Removes the specified mobility listener object
     */
    public void removeMobilityListener(MobilityListener listener) {
	this.vector.removeElement(listener);
    }

    /**
     * Removes the specified persistency listener object
     */
    public void removePersistencyListener(PersistencyListener listener) {
	this.vector.removeElement(listener);
    }

    /**
     * Returns the number of listeners
     */
    public int size() {
	return this.vector.size();
    }
}
