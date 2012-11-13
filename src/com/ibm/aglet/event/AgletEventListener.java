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
	/**
	 * 
	 */
	private static final long serialVersionUID = 6181788401517660403L;
	Vector vector = new Vector();

	public AgletEventListener() {
	}

	/**
	 * Constructs an AgletEventlistener object with specified two clone listener
	 * objects.
	 */
	public AgletEventListener(final CloneListener l1, final CloneListener l2) {
		vector.addElement(l1);
		vector.addElement(l2);
	}

	/**
	 * Constructs an AgletEventlistener object with specified two mobility
	 * listener objects.
	 */
	public AgletEventListener(final MobilityListener l1, final MobilityListener l2) {
		vector.addElement(l1);
		vector.addElement(l2);
	}

	/**
	 * Constructs an AgletEventlistener object with specified two persistency
	 * listener objects.
	 */
	public AgletEventListener(final PersistencyListener l1, final PersistencyListener l2) {
		vector.addElement(l1);
		vector.addElement(l2);
	}

	/**
	 * Adds the specified clone listener object
	 */
	public void addCloneListener(final CloneListener listener) {
		if (vector.contains(listener)) {
			return;
		}
		vector.addElement(listener);
	}

	/**
	 * Adds the specified mobility listener object
	 */
	public void addMobilityListener(final MobilityListener listener) {
		if (vector.contains(listener)) {
			return;
		}
		vector.addElement(listener);
	}

	/**
	 * Adds the specified persistency listener object
	 */
	public void addPersistencyListener(final PersistencyListener listener) {
		if (vector.contains(listener)) {
			return;
		}
		vector.addElement(listener);
	}

	/**
	 * Calls the onActivation methods on the listers with the specified
	 * persistency event.
	 */
	@Override
	public void onActivation(final PersistencyEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((PersistencyListener) e.nextElement()).onActivation(ev);
		}
	}

	/**
	 * Calls the onArrival methods on the listers with the specified mobility
	 * event.
	 */
	@Override
	public void onArrival(final MobilityEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((MobilityListener) e.nextElement()).onArrival(ev);
		}
	}

	/**
	 * Calls the onClone methods on the listers with the specified Clone event.
	 */
	@Override
	public void onClone(final CloneEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((CloneListener) e.nextElement()).onClone(ev);
		}
	}

	/**
	 * Calls the onCloned methods on the listers with the specified Clone event.
	 */
	@Override
	public void onCloned(final CloneEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((CloneListener) e.nextElement()).onCloned(ev);
		}
	}

	/**
	 * Calls the onCloning methods on the listers with the specified Clone
	 * event.
	 */
	@Override
	public void onCloning(final CloneEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((CloneListener) e.nextElement()).onCloning(ev);
		}
	}

	/**
	 * Calls the onDeactivating methods on the listers with the specified
	 * persistency event.
	 */
	@Override
	public void onDeactivating(final PersistencyEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((PersistencyListener) e.nextElement()).onDeactivating(ev);
		}
	}

	/**
	 * Calls the onDispatching methods on the listers with the specified
	 * mobility event.
	 */
	@Override
	public void onDispatching(final MobilityEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((MobilityListener) e.nextElement()).onDispatching(ev);
		}
	}

	/**
	 * Calls the onReverting methods on the listers with the specified mobility
	 * event.
	 */
	@Override
	public void onReverting(final MobilityEvent ev) {
		final Enumeration e = vector.elements();

		while (e.hasMoreElements()) {
			((MobilityListener) e.nextElement()).onReverting(ev);
		}
	}

	/**
	 * Removes the specified clone listener object
	 */
	public void removeCloneListener(final CloneListener listener) {
		vector.removeElement(listener);
	}

	/**
	 * Removes the specified mobility listener object
	 */
	public void removeMobilityListener(final MobilityListener listener) {
		vector.removeElement(listener);
	}

	/**
	 * Removes the specified persistency listener object
	 */
	public void removePersistencyListener(final PersistencyListener listener) {
		vector.removeElement(listener);
	}

	/**
	 * Returns the number of listeners
	 */
	public int size() {
		return vector.size();
	}
}
