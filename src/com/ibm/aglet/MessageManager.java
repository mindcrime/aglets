package com.ibm.aglet;

/*
 * @(#)MessageManager.java
 * 
 * (c) Copyright IBM Corp. 1996, 1997, 1998
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

/**
 * The <tt>MessageManager</tt> controls concurrency of incomming message.
 * Each kind of message can have a priority and will be placed in the
 * message queue in accordance with its priority.
 * 
 * @see FutureReply
 * @see ReplySet
 * 
 * @version     1.30    $Date: 2001/07/28 06:33:58 $
 * @author	Mitsuru Oshima
 */
public interface MessageManager {

	/**
	 * Used as a argugment to the setPriority. It indicates that
	 * messages which have the same kind will not be queued in the
	 * message queue and processed immediately.
	 * 
	 * @see MessageManager#setPriority
	 */
	public static final int NOT_QUEUED = 11;

	/**
	 * Used as a logical OR in the priority. It indicates that
	 * receiving this kind of messages will automatically activate
	 * the aglet if it had been deactivated.
	 * 
	 * <pre>
	 * getMessageManager().setPriority("wakeMeUp",
	 * NORM_PRIORITY | ACTIVATE_AGLET);
	 * </pre>
	 * 
	 * @see MessageManager#setPriority
	 */
	public static final int ACTIVATE_AGLET = 0x10;

	/**
	 * The minimal priority that the message can have.
	 * @see MessageManager#setPriority
	 */
	public static final int MIN_PRIORITY = 1;

	/**
	 * The default priority that is assigned to a message.
	 * @see MessageManager#setPriority
	 */
	public static final int NORM_PRIORITY = 5;

	/**
	 * The maximum priority that the message can have.
	 * @see MessageManager#setPriority
	 */
	public static final int MAX_PRIORITY = 10;

	/**
	 * Stop accepting the messages
	 * public void close();
	 */

	/**
	 * Checks if the message queue is empty.
	 * @return true if the message queue is empty.
	 * public boolean isEmpty();
	 */

	/**
	 * Destorys the manager. After this calling, the message manager
	 * is no longer valid and all queued and incoming message will
	 * be denied.
	 */
	public void destroy();
	/**
	 * Exits the current monitor.
	 * @see Aglet#exitMonitor
	 * @see waitMessage
	 * @see notifyMessage
	 * @see notifyAllMessages
	 */
	public void exitMonitor();
	/**
	 * Notifies all of waiting threads.
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see Aglet#notifyAllMessages
	 * @see waitMessage
	 * @see notifyMessage
	 */
	public void notifyAllMessages();
	/**
	 * Notifies a single waiting thread.
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see Aglet#notifyMessage
	 * @see waitMessage
	 * @see notifyAllMessages
	 */
	public void notifyMessage();
	/**
	 * Sets the message's priority.
	 * 
	 * @param kind      the kind to set a priority
	 * @param priority  the priority
	 */
	public void setPriority(String kind, int priority);
	/**
	 * Waits until it is notified.
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see notifyMessage
	 * @see notifyAllMessages
	 */
	public void waitMessage();
	/**
	 * Waits until it is notified or the timeout expires.
	 * @param timeout the maximum time to wait in milliseconds.
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see Aglet#waitMessage
	 * @see notifyMessage
	 * @see notifyAllMessages
	 */
	public void waitMessage(long timeout);
}
