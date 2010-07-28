package com.ibm.aglets.thread;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;

/**
 * The aim of this class is to provide a thread specialized for sending
 * messages, so that the main thread of an agent can order to this thread to
 * send messages while continuining doing something else. The thread created
 * waits until a new message (and an addressee proxy) is provided, and then
 * delivers the message. Please note that until a deliver message method is
 * invoked, this thread will stay waiting. Once the message has been delivered,
 * the thread stays waiting again for a new message. In this way you can reuse
 * this thread for several messages.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 6/10/2005
 * 
 */
public class DeliveryMessageThread extends Thread {

    /**
     * A counter of the number of delivery threads.
     */
    protected static int counter = 1;

    /**
     * The proxy to which send a message.
     */
    private AgletProxy proxy = null;

    /**
     * The message to deliver.
     */
    private Message message = null;

    /**
     * Indicates if this thread has been started or not.
     */
    private boolean started = false;

    /**
     * Constructs the thread. It does not start the thread! This means you have
     * to manually deliver the message thru the deliverMessage method.
     * 
     * @param p
     *            the proxy of the agent to use for this thread.
     * @param message
     *            the message to delivery thru the proxy.
     */
    public DeliveryMessageThread(AgletProxy p, Message msg) {
	super("Delivery Message Thread " + DeliveryMessageThread.counter);

	this.setDaemon(true); // keep this thread as a daemon in order to not
			      // interfere with the virtual machine threads
	DeliveryMessageThread.counter++;
	this.proxy = p;
	this.message = msg;
    }

    /**
     * Builds the delivery message and delivers immediatly the message if
     * specified.
     * 
     * @param p
     *            the proxy to which the message has to be sent
     * @param msg
     *            the message to send
     * @param deliveryImmediatly
     *            true if the message must be delivered immediatly
     */
    public DeliveryMessageThread(AgletProxy p, Message msg,
	    boolean deliveryImmediatly) {
	this(p, msg);
	if (deliveryImmediatly)
	    this.deliverMessage();
    }

    @Override
    public void run() {
	// check if the thread has started, to avoid calling this method
	// directly
	if (!this.started)
	    return;

	// deliver the current message to the proxy
	while (this.started) {
	    try {
		// deliver the message if any
		if ((this.proxy != null) && (this.message != null))
		    this.proxy.sendMessage(this.message);

		// reset the proxy and the message thus to use this thread in
		// the future
		this.proxy = null;
		this.message = null;

		// now reinsert myself in the pool and wait
		synchronized (this) {
		    AgletThreadPool.getInstance().pushDeliveryMessageThread(this);
		    this.wait();
		}

	    } catch (Exception e) {
		System.err.println("Exception in DeliveryMessageThread!");
		e.printStackTrace();
	    }
	}
    }

    /**
     * Deliver a specific message to the specific agent.
     * 
     * @param p
     *            the proxy of the agent to deliver to
     * @param m
     *            the message to deliver.
     * @param deliverNow
     *            true if the message must be delivered immediatly, false if the
     *            message must wait until deliverMessage is called on this
     *            thread.
     */
    public final synchronized void deliverMessage(
						  AgletProxy p,
						  Message m,
						  boolean deliverNow) {
	this.message = m;
	this.proxy = p;
	if (deliverNow)
	    this.deliverMessage();
    }

    /**
     * Delivers the current message.
     */
    public final synchronized void deliverMessage(AgletProxy p, Message m) {
	this.message = m;
	this.proxy = p;
	this.deliverMessage();
    }

    /**
     * Deliver the current message.
     */
    public final synchronized void deliverMessage() {
	if (!this.started) {
	    this.started = true;
	    this.start();
	}
	this.notifyAll();
    }

    /**
     * Stops the current thread, so that it can be destroyed.
     */
    public final synchronized void stopDelivery() {
	this.started = false;
	this.notifyAll();
    }

}
