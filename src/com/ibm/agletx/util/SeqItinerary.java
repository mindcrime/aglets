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
    public SeqItinerary(Aglet aglet) {
	this.aglet = aglet;
	aglet.addMobilityListener(this);
    }

    /**
     * Add a new destination
     * 
     * @param the
     *            address of the host where the task is to be executed.
     */
    public void addAddress(String address) {
	this.addTask(address, (Task) null);
    }

    /**
     * Return an enumeration of all the addresses
     */
    public Enumeration addresses() {
	return this.hosts.elements();
    }

    /**
     * Add a new task
     * 
     * @param task
     *            the task to be added
     * @param the
     *            address of the host where the task is to be executed.
     */
    public void addTask(String address, Task task) {
	this.hosts.addElement(address);
	this.tasks.addElement(task);
    }

    /**
     * Check if at the last destination
     */
    synchronized public boolean atLastDestination() {
	return this.index >= this.hosts.size();
    }

    /**
     * Empty the itineray
     */
    public void clear() {
	this.hosts = new Vector();
	this.tasks = new Vector();
	this.index = 0;
    }

    /**
     * Return the address at the specified index.
     */
    public String getAddressAt(int index) {
	return (String) this.hosts.elementAt(index);
    }

    /**
     * Return the address of the current destination
     */
    public String getCurrentAddress() {
	return this.getAddressAt(this.index);
    }

    /**
     * Return the task to be performed at the current destination
     */
    protected Task getCurrentTask() {
	return this.getTaskAt(this.index);
    }

    /**
     * Return the address of the origin
     */
    public String getOrigin() {
	return this.origin;
    }

    /**
     * Return the Proxy of the owner aglet
     */
    public AgletProxy getOwnerAglet() {
	return this.aglet.getAgletContext().getAgletProxy(this.aglet.getAgletID());
    }

    /**
     * Return the task at a specific index
     */
    public Task getTaskAt(int index) {
	return (Task) this.tasks.elementAt(index);
    }

    /**
     * Go to the next address and perform the next task
     */
    public void goToNext() {
	try {
	    if (this.atLastDestination()) {
		this.onTermination();
		return;
	    }
	    URL address = new URL((String) this.hosts.elementAt(this.index));

	    this.aglet.dispatch(address);
	} catch (ServerNotFoundException ex) {
	    this.handleTripException(ex);
	    this.tryNext();
	} catch (Exception ex) {
	    this.handleTripException(ex);

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
    public void handleException(Throwable ex) {
	ex.printStackTrace();
    }

    /**
     * Handle exception during the travelling of the owner aglet (try to
     * dispatch to the next destination).
     * 
     * @param ex
     *            the exception
     */
    public void handleTripException(Throwable ex) {
	this.tryNext();
    }

    void incIndex() {
	this.index++;
    }

    /**
     * Return the index of a specific task
     */
    public int indexOf(Task task) {
	return this.tasks.indexOf(task);
    }

    /**
     * Return the index of a specific address
     */
    public int indexOf(String address) {
	return this.hosts.indexOf(address);
    }

    /**
     * Check if the itinerary is a cyclic one.
     */
    public boolean isRepeat() {
	return this.repeat;
    }

    /**
     * This is not normally used by aglets programmers.
     */
    @Override
    public void onArrival(MobilityEvent ev) {
	this.currentTarget = ev.getAgletProxy();
	try {
	    Task task = this.getCurrentTask();

	    if (task != null) {
		task.execute(this);
	    }
	} catch (Throwable ex) {
	    this.handleException(ex);
	} finally {
	    this.index++;
	    if (this.atLastDestination() && this.repeat) {
		this.index = 0;
	    }
	}
	this.goToNext();
    }

    protected void onTermination() {
    }

    /**
     * Remove a task at a specific index
     */
    public void removeTaskAt(int index) {
	this.tasks.removeElementAt(index);
	this.hosts.removeElementAt(index);
    }

    /**
     * Define whether the itinerary is to be repeated (cyclic)
     */
    public void setRepeat(boolean b) {
	this.repeat = b;
    }

    /**
     * Return the size of the aglet's itinerary.
     */
    public int size() {
	return this.hosts.size();
    }

    /**
     * Start the trip defined in this itinerary
     */
    public void startTrip() {
	this.origin = this.aglet.getAgletContext().getHostingURL().toString();
	this.index = 0;
	this.goToNext();
    }

    private void tryNext() {

	// try next one
	this.index++;
	this.goToNext();
    }
}
