package com.ibm.aglets;

/*
 * @(#)MessageManagerImpl.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.Message;
import com.ibm.aglet.MessageManager;
import com.ibm.aglet.FutureReply;
import com.ibm.aglet.ReplySet;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Stack;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.aglets.log.*;

// import com.ibm.awb.misc.Debug;

/*
 * Priority
 * DONT_QUEUE       -1
 * REENTRANT_MESSAGE 12
 * SYSTEM_MESSAGE    11 (onCreation, etc)
 * REQUEST_MESSAGE   10
 */

/**
 * The <tt>MessageManagerReplyImpl</tt> class is an implementation of
 * com.ibm.aglet.MessageManager interface.
 * 
 * @version     1.30    $Date: 2007/07/16 12:03:31 $
 * @author	Mitsuru Oshima
 */
final class MessageManagerImpl implements MessageManager, 
										  java.io.Serializable {
    	static AgletsLogger logger = new AgletsLogger("com.ibm.aglet.system.AgletRuntime");
    
	public static final int REENTRANT_PRIORITY = 12;
	public static final int SYSTEM_PRIORITY = 11;
	public static final int REQUEST_PRIORITY = 10;
    
	/*
	 * Status
	 */
	static final int UNINITIALIZED = 0;
	static final int RUNNING = 1;
	static final int SUSPENDED = 2;
	static final int DEACTIVATED = 3;
	static final int DESTROYED = 4;

	/*
	 * Status String
	 */
	static private String[] state_string = {
		"UNINITIALIZED", "RUNNING", "SUSPENDED", "DEACTIVATED", "DESTRYOED"
	};

	transient private MessageQueue message_queue = new MessageQueue();

	transient private MessageQueue waiting_queue = new MessageQueue();


	static private Hashtable defaultPriorityTable = null;

	static {
		defaultPriorityTable = new Hashtable();
		defaultPriorityTable.put(Message.CLONE, 
								 new Integer(REQUEST_PRIORITY));
		defaultPriorityTable.put(Message.DISPOSE, 
								 new Integer(REQUEST_PRIORITY));
		defaultPriorityTable.put(Message.DISPATCH, 
								 new Integer(REQUEST_PRIORITY));
		defaultPriorityTable.put(Message.DEACTIVATE, 
								 new Integer(REQUEST_PRIORITY));
		defaultPriorityTable.put(Message.REVERT, 
								 new Integer(REQUEST_PRIORITY));
	} 

	transient private MessageImpl owner = null;

	transient private Stack threadSpool = new Stack();

	transient private LocalAgletRef ref;

	private Hashtable priorityTable = null;
	private Vector activationTable = null;

	int state = UNINITIALIZED;

	/*
	 * public void close() {
	 * synchronized(message_queue) {
	 * closed = true;
	 * }
	 * }
	 * 
	 * public boolean isEmpty() {
	 * return message_queue.peek() == null;
	 * }
	 */
	MessageManagerImpl(LocalAgletRef ref) {
		priorityTable = (Hashtable)defaultPriorityTable.clone();
		this.ref = ref;
	}
	/*
	 * Cancel all messages in the message queue
	 */
	void cancelMessagesInMessageQueue() {
		for (MessageImpl msg = message_queue.peek(); msg != null; 
				msg = msg.next) {

			if (owner != msg) {
				msg.cancel("handler destroyed : message = " + msg.toString());
				msg.destroy();
			} 
		} 
		message_queue.removeAll();
	}
	/*
	 * Cancel all messages in the waiting queue
	 * void cancelMiscMessages() {
	 * int size = misc.size();
	 * MessageImpl msg;
	 * for(int i=0; i<size; i++) {
	 * msg = ((MessageImpl)misc.elementAt(i));
	 * msg.cancel("handler destroyed : message = " + msg.toString());
	 * msg.destroy();
	 * }
	 * }
	 */

	/*
	 * Cancel all messages in the waiting queue
	 */
	void cancelMessagesInWaitingQueue() {
		for (MessageImpl msg = waiting_queue.peek(); msg != null; 
				msg = msg.next) {

			msg.cancel("handler destroyed : message = " + msg.toString());
			msg.destroy();
		} 
		waiting_queue.removeAll();
	}
	/*
	 * Cancel the owner message
	 */
	void cancelOwnerMessage() {
		if (owner != null && isOwner() == false) {
			owner.cancel("handler destroyed : message = " + owner.toString());
			owner.destroy();
			owner = null;
		} 
	}
	void deactivate() {
		synchronized (message_queue) {
			if (isSuspended() == false) {
				throw new IllegalArgumentException("Cannot deactivate");
			} 
			setState(DEACTIVATED);

			cancelOwnerMessage();
			cancelMessagesInWaitingQueue();
		} 

		cancelMessagesInMessageQueue();
		invalidateSpooledThreads();
	}
	// 
	// This have to be improved.
	// 
	public void destroy() {
		MessageImpl msg;

		// Debug.check();
		synchronized (message_queue) {
			if (isDestroyed()) {
				return;
			} 
			setState(DESTROYED);
			ref = null;

			// Debug.check();
			cancelOwnerMessage();

			// invalidate all waiting messages
			// This must be in the synchronized(message_queue) block
			// @see notifyMessage
			// @see notifyAllMessages
			// Debug.check();
			cancelMessagesInWaitingQueue();
		} 

		// invalidate all messages in the queue
		// This doesn't have to be in synchronized block because
		// no one can activate queued message
		// Debug.check();
		cancelMessagesInMessageQueue();

		// invalidate all threads
		// Debug.check();

		invalidateSpooledThreads();

		// Debug.check();
	}
	public void exitMonitor() {
		synchronized (message_queue) {
			if (isOwner() == false) {
				throw new IllegalMonitorStateException("Current thread is not owner " 
													   + Thread.currentThread() 
													   + " != " + owner);
			} 
			processNextMessage();
		} 
	}
	void exitMonitorIfOwner() {
		synchronized (message_queue) {
			if (isOwner()) {
				processNextMessage();
			} 
		} 
	}
	LocalAgletRef getAgletRef() {
		return ref;
	}
	private void invalidateSpooledThreads() {
		while (threadSpool.empty() == false) {
			((AgletThread)threadSpool.pop()).invalidate();
		} 
	}
	public boolean isDeactivated() {
		return state == DEACTIVATED;
	}
	public boolean isDestroyed() {
		return state == DESTROYED;
	}
	boolean isOwner() {

		// REMIND: owner may become null after the check
		if (owner != null && owner.thread == Thread.currentThread()) {
			return true;
		} else {
			return false;
		} 
	}
	public boolean isRunning() {
		return state == RUNNING;
	}
	public boolean isSuspended() {
		return state == SUSPENDED;
	}
	public boolean isUninitialized() {
		return state == UNINITIALIZED;
	}
	/* package protected */
	void kill() {
		setState(DESTROYED);
		ref = null;

		// Debug.check();
		while (threadSpool.empty() == false) {
			((AgletThread)threadSpool.pop()).stop();
		} 

		// Debug.check();
	}
	public void notifyAllMessages() {
		MessageImpl notifier = null;

		synchronized (message_queue) {
			if (isOwner() == false) {
				throw new IllegalMonitorStateException("Current thread is not owner");
			} 
			notifier = owner;
			if (waiting_queue.peek() != null) {
				message_queue.insertAtTop(notifier);
				message_queue.insertAtTop(waiting_queue);
				waiting_queue.removeAll();

				notifier.setWaiting();
				processNextMessage();
			} 
		} 
		notifier.doWait();
	}
	public void notifyMessage() {
		MessageImpl notifier = null;
		MessageImpl waiting = null;

		synchronized (message_queue) {
			if (isOwner() == false) {
				throw new IllegalMonitorStateException("Current thread is not owner");
			} 
			notifier = owner;

			// remove waiting message from queue.
			waiting = waiting_queue.pop();
			if (waiting != null) {

				// set the waiting message to the top of the queue
				// and put the notifier on the next of the queue
				message_queue.insertAtTop(notifier);
				message_queue.insertAtTop(waiting);

				notifier.setWaiting();
				processNextMessage();
			} 
		} 

		notifier.doWait();
	}
	/*
	 * Thread Management
	 */
	AgletThread popThread() {
		if (isDestroyed()) {
			System.out.println("should not happen");
			return null;
		} 

		synchronized (threadSpool) {
			if (threadSpool.empty()) {
				return ref.resourceManager.newAgletThread(this);

				// due to Fiji
				// return tm.newAgletThread(ref.threadGroup, this);
			} 
			return (AgletThread)threadSpool.pop();
		} 
	}
	void postMessage(MessageImpl msg) {
		postMessage(msg, false);
	}
	/*
	 * Post a message
	 */
	private void postMessage(MessageImpl msg, boolean oneway) {
        logger.debug("postMessage()++");
		int priority = NORM_PRIORITY;
		MessageImpl reentrantOwner = null;

		synchronized (message_queue) {
			if (isDestroyed()) {
				msg.cancel("MessageManager destroyed : message = " 
						   + msg.toString());
				return;
			} else if (isDeactivated()) {
				if (activationTable != null 
						&& activationTable.contains(msg.getKind())) {
					try {
						ref.activate();
						msg.enableDelegation();
						ref.delegateMessage(msg);
					} catch (Exception ex) {
						ex.printStackTrace();
						msg.cancel("MessageManager cannot activate aglet : message = " 
								   + msg.toString());
						return;
					} 
				} else {
					msg.cancel("MessageManager deactivated : message = " 
							   + msg.toString());
				} 
				return;
			} 

			if (msg.getKind() != null) {
				Object o = priorityTable.get(msg.getKind());

				if (o instanceof Integer) {
					priority = ((Integer)o).intValue();
				} 
			} else {
				priority = msg.priority;
			} 

			/*
			 * Not Queued
			 */
			if (priority < 0) {
				msg.activate(this);
				return;
			} 

			if (isOwner() && oneway == false) {
				reentrantOwner = owner;

				// keep the original priority
				// and set the top priority
				priority = reentrantOwner.priority;
				reentrantOwner.priority = REENTRANT_PRIORITY;
				message_queue.insertAtTop(reentrantOwner);

				// 
				// Reentrant message has top priority and
				// must be put on the top of queue.
				// 
				msg.priority = REENTRANT_PRIORITY;
				message_queue.insertAtTop(msg);

				reentrantOwner.setWaiting();

				processNextMessage();

			} else {

				// 
				// Normal message is put in the queue in accordance with
				// its priority
				// 
				msg.priority = priority;
				message_queue.insert(msg);
				processNextMessageIfEmpty();
			} 
		} 

		// 
		// @see notifyMessage
		// 
		if (reentrantOwner != null) {
			reentrantOwner.doWait();

			// restore original priority
			reentrantOwner.priority = priority;
		} 
	}
	/*
	 * This have to be called from synchronized(message_queue) block
	 */
	private void processNextMessage() {

		// don't process any more if destroed
		// don't process if suspended
		// but can process next messages even if closed
		if (isRunning() == false) {
			return;
		} 

		if (message_queue.peek() != null) {
			owner = message_queue.pop();
			owner.activate(this);
		} else {
			owner = null;
		} 
	}
	private void processNextMessageIfEmpty() {
		if (owner == null) {
			processNextMessage();
		} 
	}
	void pushMessage(MessageImpl msg) {
		postMessage(msg, true);
	}
	/*
	 * void pushThreadAndExitMonitorIfOwner(AgletThread thread) {
	 * synchronized(message_queue) {
	 * synchronized(threadSpool) {
	 * pushThread(thread);
	 * if (isOwner()) {
	 * processNextMessage();
	 * }
	 * }
	 * }
	 * }
	 */

	void pushThread(AgletThread thread) {
		synchronized (threadSpool) {
			if (isDestroyed()) {
				thread.invalidate();
				return;
			} 
			threadSpool.push(thread);
		} 
	}
	void pushThreadAndExitMonitorIfOwner(AgletThread thread) {
		synchronized (message_queue) {
			pushThread(thread);
			if (isOwner()) {
				processNextMessage();
			} 
		} 
	}
	/*
	 * MessageManagerImpl(java.io.ObjectInput in, LocalAgletRef ref) throws IOException, ClassNotFoundException {
	 * priorityTable = (Hashtable)in.readObject();
	 * activationTable = (Vector)in.readObject();
	 * state = in.readInt();
	 * this.ref = ref;
	 * }
	 */

	private void readObject(java.io.ObjectInputStream s) 
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();

		// state = UNINITIALIZED;
		threadSpool = new Stack();
		message_queue = new MessageQueue();
		waiting_queue = new MessageQueue();
	}
	void removeThread(AgletThread thread) {
		threadSpool.removeElement(thread);
	}
	public void resume() {		// ThreadGroup group) {
		synchronized (message_queue) {
			if (isRunning() || isDestroyed()) {
				return;
			} 
			setState(RUNNING);
			ref.resourceManager.resumeAllThreads();
			processNextMessage();

			// processNextMessageIfEmpty();
		} 
	}
	void setAgletRef(LocalAgletRef r) {
		ref = r;
	}
	public void setPriority(String kind, int priority) {
		if ((priority & ACTIVATE_AGLET) == ACTIVATE_AGLET) {
			if (activationTable == null) {
				activationTable = new Vector();
			} 
			if (activationTable.contains(kind) == false) {
				activationTable.addElement(kind);
			} 
			priority = priority & 0xF;
			if (priority == 0) {

				// priority = NORM_PRIORITY;
				return;
			} 
		} 

		if (priority != NOT_QUEUED 
				&& (priority < MIN_PRIORITY || priority > MAX_PRIORITY)) {
			throw new IllegalArgumentException("illegal priority");
		} 

		priorityTable.put(kind, new Integer(priority));

		// REMIND: re-sort the messages in the queue
	}
	/* synchronized (message_queue) */
	void setState(int next) {
		switch (state) {
		case UNINITIALIZED:
			if (next == RUNNING || next == DEACTIVATED) {
				state = next;
				return;
			} 
			break;
		case RUNNING:
			if (next == SUSPENDED) {
				state = next;
				return;
			} 

			break;
		case SUSPENDED:
			if (next == DESTROYED || next == RUNNING || next == DEACTIVATED) {
				state = next;
				return;
			} 
			break;
		case DEACTIVATED:
			if (next == RUNNING) {
				state = next;
				return;
			} 
			break;
		case DESTROYED:
		default:

			// cannot move to any state!
			break;
		}
		throw new IllegalArgumentException("Cannot proceed from " 
										   + state_string[state] + " to " 
										   + state_string[next]);
	}
	/*
	 * Start
	 */
	void start() {
		if (isUninitialized() == false) {
			throw new IllegalMonitorStateException("MessageManager not valid");
		} 
		synchronized (message_queue) {
			setState(RUNNING);
			processNextMessageIfEmpty();
		} 
	}
	public void suspend() {		// ThreadGroup group) {
		synchronized (message_queue) {
			if (isSuspended() || isDestroyed()) {
				return;
			} 
			setState(SUSPENDED);

			// to make sure that no other thread has entered in the
			// synchronous block.
			ref.resourceManager.suspendAllThreads();
		} 
	}
	/**
	 * 
	 */
	public String toString() {
		int i = 0;
		StringBuffer buff = new StringBuffer("Active queue\n");
		MessageImpl tmp = message_queue.peek();

		synchronized (message_queue) {
			i = 1;
			while (tmp != null) {
				buff.append(String.valueOf(i++));
				buff.append(":");
				buff.append(tmp.toString());
				buff.append("\n");
				tmp = tmp.next;
			} 
			tmp = waiting_queue.peek();
			buff.append("Waiting queue\n");
			i = 1;
			while (tmp != null) {
				buff.append(String.valueOf(i++));
				buff.append(":");
				buff.append(tmp.toString());
				buff.append("\n");
				tmp = tmp.next;
			} 
		} 
		return buff.toString();
	}
	public void waitMessage() {
		waitMessage(0);
	}
	public void waitMessage(long timeout) {

		MessageImpl wait = null;

		synchronized (message_queue) {
			if (isOwner() == false) {
				throw new IllegalMonitorStateException("Current thread is not owner");
			} 
			wait = owner;

			// put the owner message to the waiting queue
			waiting_queue.append(wait);
			wait.setWaiting();
			processNextMessage();
		} 

		// wait outside of synchronized block to avoid dead lock
		if (timeout == 0) {

			// short cut
			wait.doWait();
		} else {
			wait.doWait(timeout);
			synchronized (message_queue) {
				if (wait.isWaiting()) {

					// nobody didn't wake me up!
					waiting_queue.remove(wait);

					// REMIND: this must be improved to
					// consider priority
					message_queue.insertAtTop(wait);

					// kick
					processNextMessageIfEmpty();

					// message_queue.notify();
				} 
			} 

			// if still waiting,
			// wait again until the handler loop activates this message
			wait.doWait();
		} 
	}
}
