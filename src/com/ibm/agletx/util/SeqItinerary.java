package com.ibm.agletx.util;

/*
 * @(#)SeqItinerary.java
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
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.ServerNotFoundException;
import com.ibm.aglet.event.MobilityAdapter;
import com.ibm.aglet.event.MobilityEvent;

/**
 * Define an abstract interface for an aglet's itinerary. An Itinerary is a set
 * of pairs of the form [host, task] where the task should be performed upon
 * arrival of the aglet to the host.
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 * @see Task
 */
public abstract class SeqItinerary extends MobilityAdapter implements
java.io.Serializable {

	static final long serialVersionUID = 7979344708988677111L;

	protected Aglet aglet;
	private Vector tasks = new Vector();
	private Vector hosts = new Vector();
	private int index = 0; // should be improved to become private
	transient protected AgletProxy currentTarget = null;
	private boolean repeat = false;
	private String origin = null;

	/**
	 * Constructor.
	 * 
	 * @param aglet
	 *            the owner aglet
	 */
	public SeqItinerary(final Aglet aglet) {
		this.aglet = aglet;
		aglet.addMobilityListener(this);
	}

	/**
	 * Add a new destination
	 * 
	 * @param address
	 *            name of the location where the task is to be executed.
	 */
	public void addAddress(final String address) {
		addTask(address, (Task) null);
	}

	/**
	 * Return an enumeration of all the addresses
	 */
	public Enumeration addresses() {
		return hosts.elements();
	}

	/**
	 * Add a new task
	 * 
	 * @param task
	 *            the task to be added
	 * @param address
	 *            name of the location where the task is to be executed.
	 */
	public void addTask(final String address, final Task task) {
		hosts.addElement(address);
		tasks.addElement(task);
	}

	/**
	 * Check if at the last destination
	 */
	synchronized public boolean atLastDestination() {
		return index >= hosts.size();
	}

	/**
	 * Empty the itineray
	 */
	public void clear() {
		hosts = new Vector();
		tasks = new Vector();
		index = 0;
	}

	/**
	 * Return the address at the specified index.
	 */
	public String getAddressAt(final int index) {
		return (String) hosts.elementAt(index);
	}

	/**
	 * Return the address of the current destination
	 */
	public String getCurrentAddress() {
		return getAddressAt(index);
	}

	/**
	 * Return the task to be performed at the current destination
	 */
	protected Task getCurrentTask() {
		return getTaskAt(index);
	}

	/**
	 * Return the address of the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * Return the Proxy of the owner aglet
	 */
	public AgletProxy getOwnerAglet() {
		return aglet.getAgletContext().getAgletProxy(aglet.getAgletID());
	}

	/**
	 * Return the task at a specific index
	 */
	public Task getTaskAt(final int index) {
		return (Task) tasks.elementAt(index);
	}

	/**
	 * Go to the next address and perform the next task
	 */
	public void goToNext() {
		try {
			if (atLastDestination()) {
				onTermination();
				return;
			}
			final URL address = new URL((String) hosts.elementAt(index));

			aglet.dispatch(address);
		} catch (final ServerNotFoundException ex) {
			handleTripException(ex);
			tryNext();
		} catch (final Exception ex) {
			handleTripException(ex);

			// } catch (Error ex) {
			// if (ex instanceof ThreadDeath == false) {
			// handleTripException(ex);
			// }
		}
	}

	/**
	 * Handle exception during task execution
	 * 
	 * @param ex
	 *            the exception
	 */
	public void handleException(final Throwable ex) {
		ex.printStackTrace();
	}

	/**
	 * Handle exception during the travelling of the owner aglet (try to
	 * dispatch to the next destination).
	 * 
	 * @param ex
	 *            the exception
	 */
	public void handleTripException(final Throwable ex) {
		tryNext();
	}

	void incIndex() {
		index++;
	}

	/**
	 * Return the index of a specific address
	 */
	public int indexOf(final String address) {
		return hosts.indexOf(address);
	}

	/**
	 * Return the index of a specific task
	 */
	public int indexOf(final Task task) {
		return tasks.indexOf(task);
	}

	/**
	 * Check if the itinerary is a cyclic one.
	 */
	public boolean isRepeat() {
		return repeat;
	}

	/**
	 * This is not normally used by aglets programmers.
	 */
	@Override
	public void onArrival(final MobilityEvent ev) {
		currentTarget = ev.getAgletProxy();
		try {
			final Task task = getCurrentTask();

			if (task != null) {
				task.execute(this);
			}
		} catch (final Throwable ex) {
			handleException(ex);
		} finally {
			index++;
			if (atLastDestination() && repeat) {
				index = 0;
			}
		}
		goToNext();
	}

	protected void onTermination() {
	}

	/**
	 * Remove a task at a specific index
	 */
	public void removeTaskAt(final int index) {
		tasks.removeElementAt(index);
		hosts.removeElementAt(index);
	}

	/**
	 * Define whether the itinerary is to be repeated (cyclic)
	 */
	public void setRepeat(final boolean b) {
		repeat = b;
	}

	/**
	 * Return the size of the aglet's itinerary.
	 */
	public int size() {
		return hosts.size();
	}

	/**
	 * Start the trip defined in this itinerary
	 */
	public void startTrip() {
		origin = aglet.getAgletContext().getHostingURL().toString();
		index = 0;
		goToNext();
	}

	private void tryNext() {

		// try next one
		index++;
		goToNext();
	}
}
