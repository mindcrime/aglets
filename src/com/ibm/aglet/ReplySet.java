package com.ibm.aglet;

/*
 * @(#)ReplySet.java
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

import java.util.Vector;

/**
 * ReplySet is a container of the FutureReply objects by which the
 * each of FutureReply object can be retrieved as its reply become available.
 * 
 * <pre>
 * ReplySet set = context.multicastMessage(new Message("multicast"));
 * set.addFutureReply( aglet.sendAsyncMessage(new Message("additional"));
 * int i = 0;
 * whlie(set.hasMoreFutureReplies()) {
 * FutureReply future = set.getNextFutureReply();
 * Object reply = future.getReply();
 * logCategory.debug("No[" + i + "] = " + reply);
 * }
 * </pre>
 * 
 * @see AgletContext#multicastMessage
 * @see Message
 * @see FutureReply
 * 
 * @version     1.20    $Date: 2002/01/19 21:57:19 $
 * @author	Mitsuru Oshima
 */

final public class ReplySet {

	private Vector done = new Vector();
	private Vector unavailable = new Vector();

	/**
	 * Constructs a ReplySet object.
	 */
	public ReplySet() {}
	/**
	 * Adds the FutureReplyd object to this ReplySet.
	 * @param reply the FutureReply to add.
	 */
	synchronized public void addFutureReply(FutureReply reply) {
		unavailable.addElement(reply);
		reply.addedTo(this);
	}
	/**
	 * Checks if all FutureReply objects in this ReplySet have received
	 * replies.
	 * @return true if all replies of FutureReply objects are available
	 */
	public boolean areAllAvailable() {
		return unavailable.size() == 0;
	}
	/**
	 * Counts the number of available replies in this ReplySet.
	 * @return the number of available replise
	 */
	public int countAvailable() {
		return done.size();
	}
	/**
	 * Counts the number of FutureReply objects which have no reply
	 * available.
	 * @return the number of FutureReply which have no reply available.
	 */
	public int countUnavailable() {
		return unavailable.size();
	}
	/**
	 * Is is not normally used by the aglet programmers.
	 */
	synchronized public void done(FutureReply reply) {

		// 
		// REMIND: This will be removed...
		// 
		if (unavailable.contains(reply) == false 
				|| done.contains(reply) == true 
				|| reply.isAvailable() == false) {
			throw new RuntimeException("ReplySet: invalid reply");
		} 
		unavailable.removeElement(reply);
		done.addElement(reply);
		notifyAll();
	}
	/**
	 * Gets the next FutureReply whose reply is available.
	 * @return a FutureReply object whose reply is available.
	 */
	synchronized public FutureReply getNextFutureReply() {
		waitForNextFutureReply();
		FutureReply r = (FutureReply)done.firstElement();

		done.removeElementAt(0);
		return r;
	}
	/**
	 * Checks if there are more FutureReply objects in this ReplySet object.
	 * @return true if there are FutureReply objects
	 */
	synchronized public boolean hasMoreFutureReplies() {
		return unavailable.size() != 0 || done.size() != 0;
	}
	/**
	 * Checks if there is any FutureReply object whose reply is
	 * available in this ReplySet object.
	 * @return true if there are FutureReply objects whose reply is available.
	 */
	public boolean isAnyAvailable() {
		return done.size() != 0;
	}
	/**
	 * Waits until the all replies are available.
	 */
	synchronized public void waitForAllReplies() {
		while (unavailable.size() != 0) {
			try {
				wait();
			} catch (InterruptedException ex) {}
		} 
	}
	/**
	 * Waits until the all replies are available
	 * @param timeout   the maximum time to wait in milliseconds.
	 */
	synchronized public void waitForAllReplies(long timeout) {
		if (timeout == 0) {
			waitForAllReplies();
		} else {
			long until = System.currentTimeMillis() + timeout;
			long reft;

			while (unavailable.size() != 0 
				   && (reft = (until - System.currentTimeMillis())) > 0) {
				try {
					wait(reft);
				} catch (InterruptedException ex) {}
			} 
		} 
	}
	/**
	 * Waits until the next reply is available.
	 */
	synchronized public void waitForNextFutureReply() {
		while (done.size() == 0) {
			try {
				wait();
			} catch (InterruptedException ex) {}
		} 
	}
	/**
	 * Waits until the next reply is available.
	 * @param timeout   the maximum time to wait in milliseconds.
	 */
	synchronized public void waitForNextFutureReply(long timeout) {
		if (timeout == 0) {
			waitForNextFutureReply();
		} else {
			long until = System.currentTimeMillis() + timeout;
			long reft;

			while (done.size() == 0 
				   && (reft = (until - System.currentTimeMillis())) > 0) {
				try {
					wait(reft);
				} catch (InterruptedException ex) {}
			} 
		} 
	}
}
