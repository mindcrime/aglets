package com.ibm.agletx.util;

/*
 * @(#)MetaItinerary.java
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

import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.event.MobilityAdapter;
import com.ibm.aglet.event.MobilityEvent;

/**
 * an itineray class to log visited and unvisited destinations during an aglet's
 * tour.
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 */
public class MetaItinerary extends MobilityAdapter implements
java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3170189459194900971L;
	private final Vector visitedHosts = new Vector();
	private final Vector hosts = new Vector();

	public MetaItinerary(final Aglet aglet) {
		aglet.addMobilityListener(this);
	}

	/**
	 * Return unvisited destinations
	 * 
	 * @return enumaration of addresses of unvisited destinations.
	 */
	public Enumeration getNonVisitedHosts() {
		final Vector v = new Vector();
		boolean found = false;

		for (final Enumeration e = hosts.elements(); e.hasMoreElements();) {
			URL url = null;

			try {
				url = new URL((String) e.nextElement());
			} catch (final Exception ex) {
				continue;
			}

			found = false;
			for (final Enumeration e1 = visitedHosts.elements(); e1.hasMoreElements();) {
				try {
					found = url.sameFile(new URL((String) (e1.nextElement())));
				} catch (final Exception ex) {
					continue;
				}
				if (found) {
					break;
				}
			}

			if (!found) {
				v.addElement(url);
			}
		}
		return v.elements();
	}

	/**
	 * Return visited destinations
	 * 
	 * @return enumaration of addresses of visited destinations.
	 */
	public Enumeration getVisitedHosts() {
		return visitedHosts.elements();
	}

	@Override
	public void onArrival(final MobilityEvent ev) {
		visitedHosts.addElement(ev.getLocation().toString());
	}

	@Override
	public void onDispatching(final MobilityEvent ev) {
		hosts.addElement(ev.getLocation().toString());
	}
}
