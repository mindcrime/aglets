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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import java.util.*;
import java.net.*;
import java.io.IOException;

/**
 * an itineray class to log visited and unvisited destinations during
 * an aglet's tour.
 * 
 * @version     1.20    $Date: 2009/07/28 07:04:53 $
 * @author      Yariv Aridor
 */
public class MetaItinerary extends MobilityAdapter 
	implements java.io.Serializable {

	private Vector visitedHosts = new Vector();
	private Vector hosts = new Vector();


	public MetaItinerary(Aglet aglet) {
		aglet.addMobilityListener(this);
	}
	/**
	 * Return unvisited destinations
	 * @return enumaration of addresses of unvisited destinations.
	 */
	public Enumeration getNonVisitedHosts() {
		Vector v = new Vector();
		boolean found = false;

		for (Enumeration e = hosts.elements(); e.hasMoreElements(); ) {
			URL url = null;

			try {
				url = new URL((String)e.nextElement());
			} catch (Exception ex) {
				continue;
			} 

			found = false;
			for (Enumeration e1 = visitedHosts.elements(); 
					e1.hasMoreElements(); ) {
				try {
					found = url.sameFile(new URL((String)(e1.nextElement())));
				} catch (Exception ex) {
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
	 * @return enumaration of addresses of visited destinations.
	 */
	public Enumeration getVisitedHosts() {
		return visitedHosts.elements();
	}
	public void onArrival(MobilityEvent ev) {
		visitedHosts.addElement(ev.getLocation().toString());
	}
	public void onDispatching(MobilityEvent ev) {
		hosts.addElement(ev.getLocation().toString());
	}
}
