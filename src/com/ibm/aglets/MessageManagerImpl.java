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

import java.util.*;
import com.ibm.aglet.Aglet;
import com.ibm.aglet.MessageManager;
import com.ibm.aglet.FutureReply;
import com.ibm.aglet.ReplySet;
import com.ibm.aglet.message.Message;


import java.util.Vector;
import java.util.Hashtable;
import java.util.Stack;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.aglets.log.*;

import com.ibm.aglets.thread.*;


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
 * @version     2    
 * @author	Mitsuru Oshima
 * @author Luca Ferrari cat4hire@users.sourceforge.net (refactoring)
 * 
 */
public   class MessageManagerImpl implements MessageManager, 
										  java.io.Serializable {
    static private LogCategory logCategory = LogInitializer.getCategory("com.ibm.aglets.MessageManagerImpl");
    
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

	
	
	/**
	 * The owner of the message manager. The owner message is the message
	 * currently being processed, that is the one why the thread is
	 * running on this message manager.
	 */
	transient private Message owner = null;
	
	/**
	 * Indicates if the message manager has been suspended or not.
	 */
	private boolean sleeping = false;

	//transient private Stack threadSpool = new Stack();
	transient private AgletThreadPool threadPool = AgletThreadPool.getInstance();

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
	public MessageManagerImpl(LocalAgletRef ref) {
		priorityTable = (Hashtable)defaultPriorityTable.clone();
		this.ref = ref;
	}
	
	
	/**
	 * Removes a message from the message queue. This method calls
	 * the destroyMessage, that you should override in order to
	 * deal with your current implementatio of Message (e.g., MessageImpl). 
	 * @author Luca Ferrari
	 *
	 */
	void cancelMessagesInMessageQueue() {
		// before cancelling all the messages from the queue,
		// I need to advice the future replies
		Iterator iterator = this.message_queue.iterator();
		
		while( iterator != null && iterator.hasNext()){
			Message msg = (Message) iterator.next();
			this.destroyMessage(msg);
			
		}
		
		// remove all messages from the queue
		this.message_queue.removeAll();
		
	}
	
	
	
	/**
	 * Sets the status of the sleeping indicator. <B>Please note that this method sets only the status
	 * of the sleeping boolean value, it does not suspend the message manager!</B>
	 * @param value the value of the sleeping flag.
	 */
	protected final synchronized void setSleeping(boolean value){
		this.sleeping = value;
	}
	
	/**
	 * Provides the sleeping status.
	 * @return true if the message manager is sleeping, false otherwise.
	 */
	public final synchronized boolean isSleeping(){
		return this.sleeping;
	}
	
	/**
	 * Suspends the message manager. Messages will be queued but not processed.
	 *
	 */
	public final void sleep(){
		this.setSleeping(true);
	}
	
	/**
	 * Wakes up the message manager, messages will be processed.
	 *
	 */
	public final void wakeUp(){
		this.setSleeping(false);
		//this.processNextMessage();
	}
	
	
	/**
	 * A method to remove the future reply from a message. This method
	 * exploits the MessageImpl method. Override the method if you are 
	 * using a different implementation of the message.
	 * @param msg the message to convert into a MessageImpl and to 
	 * remove.
	 * @author Luca Ferrari
	 */
	protected void destroyMessage(Message msg){
		// check arguments
		if( msg == null || ! (msg instanceof MessageImpl) )
			return;
		
		// convert the message to a messageImpl and cancel the
		// future reply
		MessageImpl mimpl = (MessageImpl) msg;
		mimpl.cancel("Cancelled from the queue");
		mimpl.destroy();
		
	}
	
	
	/**
	 * Cancels all messages in the waiting queue. This method calls
	 * the destroyMessage one, that you should override in order
	 * to deal with your implementation of Message.
	 * @author Luca Ferrari
	 */
	void cancelMessagesInWaitingQueue() {
		//before cancelling all the messages from the queue,
		// I need to advice the future replies
		Iterator iterator = this.waiting_queue.iterator();
		
		while( iterator != null && iterator.hasNext()){
			Message msg = (Message) iterator.next();
			this.destroyMessage(msg);
			
		}
		
		// remove all messages from the queue
		this.message_queue.removeAll();
	}
	
	
	
	/**
	 * Cancels the owner message.
	 *
	 */
	void cancelOwnerMessage() {
		if (owner != null && isOwner() == false) {
			this.destroyMessage( this.owner );
			this.owner = null;
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
	
	
	/**
	 * Eperimental!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *
	 */
	private void invalidateSpooledThreads() {
	/*	while (threadSpool.empty() == false) {
			((AgletThread)threadSpool.pop()).invalidate();
		}
	*/ 
	}
	public boolean isDeactivated() {
		return state == DEACTIVATED;
	}
	public boolean isDestroyed() {
		return state == DESTROYED;
	}
	
	
	
	
	/**
	 * A method to know if the running thread is the owner of the
	 * message manager, that is the one encapsulated in the message
	 * owner of this message manager. This method strictly depends
	 * over the message implementation (i.e, MessageImpl), thus you should
	 * override it depending on the message you're using.
	 * @return true if the thread is the owner of the message manager
	 */
	protected boolean isOwner() {
		if ( owner != null && owner instanceof MessageImpl &&
			((MessageImpl)owner).thread == Thread.currentThread()) {
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
/*	void kill() {
		setState(DESTROYED);
		ref = null;

		// Debug.check();
		while (threadSpool.empty() == false) {
			((AgletThread)threadSpool.pop()).stop();
		} 

		// Debug.check();
	}
*/
	
	/**
	 * Experimental!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 */
	void kill(){
		// invalidate the message manager
		this.deactivate();
	}
	
	/**
	 * Notifies all messages in the queue. Similarly to the notifyMessage
	 * method, this method places all the waiting queue at the top of
	 * the message queue, and then calls the processNextMessage method,
	 * thus all the waiting messages are processed. After this the
	 * thread is suspended.
	 * This method relies on the doWait() one for the thread
	 * suspension.
	 */
	public void notifyAllMessages() {
		Message notifier = null;

		synchronized (message_queue) {
			if (isOwner() == false) {
				throw new IllegalMonitorStateException("Current thread is not owner");
			} 
			notifier = owner;
			if (waiting_queue.peek() != null) {
				message_queue.insertAtTop(notifier);
				message_queue.insertAtTop(waiting_queue);
				waiting_queue.removeAll();

				notifier.setWaiting(true);
				processNextMessage();
			} 
		} 
		
		this.doWait(notifier);
	}
	
	
	
	/**
	 * Notifies a single message. The message to notify is the
	 * owner of the message manager. The method checks if the thread
	 * running is the one attached to the message, and in this case
	 * checks if there are waiting messages. In this case both the
	 * owner and the first waiting message are placed at the top of
	 * message queue. After this, the processNextMessage method is call,
	 * and this causes the thread to notify the message to the agent (handle(...)). 
	 * Since the waiting message is inserted at the top of queue <b>after</b>
	 * the owner, the processed message is the waiting one. In other words,
	 * if there's a waiting message, this is always processed, otherwise the
	 * thread is suspended.
	 * In the case there's no one waiting message (or after the notification
	 * of the owner), the thread suspends over the owner message.
 	 * This method relies on the doWait() one for the thread
	 * suspension.
	 */
	public void notifyMessage() {
		Message notifier = null;
		Message waiting = null;

		synchronized (message_queue) {
			
			// check if the thread attached to the owner message
			// is the same that is running this method
			if (isOwner() == false) {
				throw new IllegalMonitorStateException("Current thread is not owner");
			} 
			
			// the notifier message is the owner of this message
			// manager
			notifier = owner;

			
			// Since the thread could have been suspended until
			// the notifier/owner message came, there could be
			// a set of messages in the waiting queue. Thus, I
			// need to extract the message from the waiting queue
			// and to notify it to the aglet.
			
			// remove waiting message from queue.
			waiting = waiting_queue.pop();
			
			if (waiting != null) {

				// set the waiting message to the top of the queue
				// and put the notifier on the next of the queue
				message_queue.insertAtTop(notifier);
				message_queue.insertAtTop(waiting);

				// Now I've placed the messages in the queue,
				// indicates that the notifier/owner is waiting
				// to be processed
				notifier.setWaiting(true);
				
				// process the message
				processNextMessage();
			} 
		} 

		this.doWait(notifier);
	}
	
	
	/**
	 * Suspends over the messageImpl. Substitute with the
	 * implementation for your kind of message implementation.
	 * @param msg the message (supposed MessageImpl) to suspend
	 * over
	 * @author Luca Ferrari
	 */
	protected void doWait(Message msg){
		if( msg != null && msg instanceof MessageImpl){
			((MessageImpl)msg).doWait();
		}
		
	}
	
	/**
	 * Similar to the doWait method, but with a timeout.
	 * @param msg the message to suspend onto.
	 * @param timeout the timeout to wait for.
	 */
	protected void doWait(Message msg, long timeout){
		if( msg != null && msg instanceof MessageImpl){
			((MessageImpl)msg).doWait(timeout);
		}
	}
	
	
	
	/*
	 * Thread Management
	 */
/*	AgletThread popThread() {
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
*/
	
	/**
	 * Get the agletthread from the <b>global</b> thread pool.
	 * @return the thread for the message manager
	 */
	AgletThread popThread(){
		if( this.isDestroyed() ){
			System.err.println("Trying to get a thread for a destroyed message manager!");
			return null;
		}
		
		// get the thread from the pool
		return this.threadPool.pop(this);
	}
	
	
	void postMessage(MessageImpl msg) {
		postMessage(msg, false);
	}
	
	/**
	 * A method to enable delegation of a message. This method depends
	 * on the MessageImpl class, override it depending on your message
	 * implementation.
	 * @param msg the message to delegate
	 */
	protected void enableDelegation(Message msg){
		if( msg != null && msg instanceof MessageImpl ){
			((MessageImpl)msg).enableDelegation();
		}
	}
	
	
	/**
	 * Post a new message. This method works as follows:
	 * 1) if the message manager is deactivated and the message is one
	 * of those that can reactivate it, the message is activated
	 * 2) if the message is sent from the thread to itself, that means
	 * the thread calling postMessage is the owner of the message manager,
	 * the current message is enqueued and the loop message is processed as first.
	 * 3) if the message is a normal message, it is enqueued and then 
	 * the processNextMessageIfFree is called.
	 * @param msg the message to process
	 * @param oneway
	 */
	protected final void postMessage(Message msg, boolean oneway) {
        logCategory.debug("[MessageManagerImpl.postMessage()] "+msg);
        
		int priority = NORM_PRIORITY;
		Message reentrantOwner = null;

		synchronized (message_queue) {
			if (isDestroyed()) {
				// cannot process messages if I'm destroyed
				this.destroyMessage(msg);
				return;
			} 
			else 
			if (isDeactivated()) {
				// I'm deactivated, the only thing I can do is to check if
				// my activation table contains this message, if so
				// I can try to reactivate the agent
				if (activationTable != null && 
					activationTable.contains(msg.getKind())) {
					try {
						ref.activate();
						this.enableDelegation( msg );
						ref.delegateMessage(msg);
					} catch (Exception ex) {
						ex.printStackTrace();
						this.destroyMessage(msg);
						return;
					} 
				} else {
					this.destroyMessage(msg);
				} 
				return;
			} 

			
			// get the priority of the message
			priority = msg.getPriority();
			
			
			// if the priority is less than 0, the message is not
			// queued and is immediatly processed. Thus a negative priority
			// is higher than a positive value????????????
			// Luca: it does not make sense! and it complicates the sleeping activity!
			//if (priority < 0) {
			//	this.deliverMessageWithoutQueueing(msg);
			//	return;
			//} 

			
			// if the running thread is the same associated to
			// the message manager and the message is not oneway
			// process it immediatly. In other words,
			// if the thread is trying to notify a message to itself
			// stop processing the last message and process this one.
			if (isOwner() && oneway == false) {
				// store the message I'm processing now
				reentrantOwner = owner;

				// keep the original priority and set the
				// priority of the original message at the max, thus
				// it will be processed immediatly after the other.
				// NOTE: insertAtTop should not use priority, thus
				// here it is not needed, but keep it for sureness.
				priority = reentrantOwner.getPriority();
				reentrantOwner.setPriority(REENTRANT_PRIORITY);
				message_queue.insertAtTop(reentrantOwner);

				// insert the new message at the top of the queue
				msg.setPriority(REENTRANT_PRIORITY);
				message_queue.insertAtTop(msg);

				// the original message is waiting
				reentrantOwner.setWaiting(true);

				// process the message currently at the top
				// of the queue
				processNextMessage();

			} else {

				// normal message, enqueue depending on its priority
				this.message_queue.insert(msg);
				
				// now call processNextMessage if there's not an owner,
				// this will lead to the processing of the message just enqueued
				processNextMessageIfEmpty();
			} 
		} 

		// if I have a previous owner, I need to reactivate it 
		if (reentrantOwner != null) {
			this.doWait(reentrantOwner);

			// restore original priority
			reentrantOwner.setPriority(priority);
		} 
	}
	
	
	
	/**
	 * A method to deliver the message. You should override this method
	 * since it depends on the implementation of the message (for example
	 * MessageImpl).
	 * @param msg the message to be delivered
	 */
	protected void deliverMessageWithoutQueueing(Message msg){
		if( msg != null && msg instanceof MessageImpl){
			((MessageImpl)msg).activate(this);
		}
	}
	
	
	
	/**
	 * Processes a message at the top of the message queue. This method relies on
	 * the processCurrentOwner one, that you should override to deal with your specific
	 * Message implementation.
	 * 
	 * This method pops the first message in the message queue and sets
	 * it as the owner of the MessageManager. After that, it calls the
	 * processCurrentOwner that activates the message.
	 */
	protected final void processNextMessage() {

		// don't process messages if not running or sleeping
		if (isRunning() == false  || this.isSleeping() ) {
			return;
		} 

		// extract a message from the queue and activate it
		if( this.message_queue != null && 
			this.message_queue.isEmpty() == false ){
			// get the first message in the queue
			this.owner = (Message) this.message_queue.pop();
			// process the message (only if it is not null)
			if( this.owner != null )
				this.processCurrentOwner();
		}
		else{
			this.owner = null;
		}
		
	}
	
	
	
	
	
	
	/**
	 * Process the current owner message. You should override this method
	 * depending on the implementation of your message. This method treats
	 * only MessageImpl objects.
	 *
	 */
	protected void processCurrentOwner(){
		if( this.owner != null && this.owner instanceof MessageImpl )
			((MessageImpl)this.owner).activate(this);
	} 
	
	
	/**
	 * Processes a message only if there's not an owner, that means
	 * only if there's not a currently processing message.
	 *
	 */
	private final void processNextMessageIfEmpty() {
		// process a message only if the current owner is empty and if not sleeping
		if( this.owner == null && ! this.isSleeping() ){
			this.processNextMessage();
		}
	}
	void pushMessage(MessageImpl msg) {
		postMessage(msg, true);
	}


/*	void pushThread(AgletThread thread) {
		synchronized (threadSpool) {
			if (isDestroyed()) {
				thread.invalidate();
				return;
			} 
			threadSpool.push(thread);
		} 
	}
*/
	/**
	 * Releases a thread and replace it into the <b>global</b> pool.
	 * @param thread the thread to release
	 */
	void pushThread(AgletThread thread){
		this.threadPool.push(thread);
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
		this.threadPool = AgletThreadPool.getInstance();
		message_queue = new MessageQueue();
		waiting_queue = new MessageQueue();
	}
	void removeThread(AgletThread thread) {
		this.threadPool.push(thread);
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
	
	
	
	
	public String toString() {
		int i = 0;
		StringBuffer buff = new StringBuffer("Active queue\n");
		buff.append( this.message_queue.toString() );
		buff.append("\nWaiting queue\n");
		buff.append( this.waiting_queue.toString() );
		return buff.toString();
	}
	
	
	
	public void waitMessage() {
		waitMessage(0);
	}
	
	
	/**
	 * Waits untill a message come.
	 * This method works as follows:
	 * the owner message (the one currently being processed) is placed
	 * in the waiting queue and the processNextMessage() method is called
	 * (i.e., the next message is processed). After that, if the timeout
	 * is zero the thread is suspended until a new message is activated (i.e, the notify
	 * into the activate method of MessageImpl is called). Otherwise, a wait(timoeut)
	 * is called. In the latter case, when the thread wakes up, it checks if the
	 * message placed in the waiting queue (the old owner, since now it should be
	 * another) is still waiting. If so, nobody has pulled it out of the waiting
	 * queue, thus it is removed and inserted in the message queue and 
	 * processNextMessageIfEmpty is called. This produces the message processing
	 * only if the current owner is null. When the notifyMessage is called, the waiting
	 * message is extracted from the waiting queue and reactivated.
	 */
	public void waitMessage(long timeout) {

		Message wait = null;

		synchronized (message_queue) {
			if (isOwner() == false) {
				throw new IllegalMonitorStateException("Current thread is not owner");
			} 
			wait = owner;

			// put the owner message to the waiting queue
			waiting_queue.append(wait);
			wait.setWaiting(true);
			processNextMessage();
		} 

		// wait outside of synchronized block to avoid dead lock
		if (timeout == 0) {
			// no timeout specified, wait until a new message
			// comes
			this.doWait( wait );
		} 
		else {
			// wait for the specified timeout
			this.doWait( wait, timeout);
			
			
			// if here the timeout expired, thus the wait
			// message should be processed, check it
			
			synchronized (message_queue) {
				if (wait.isWaiting()) {

					// nobody didn't wake me up!
					waiting_queue.remove(wait);

					// REMIND: this must be improved to
					// consider priority
					message_queue.insertAtTop(wait);

					// process the message only if there's no other
					// message being processed
					processNextMessageIfEmpty();

				} 
			} 

			// now that the timeout has expired, notify 
			// the next message
			// is it right???????????????????????????
			// Please note that if here the running thread is the
			// one that owns the message manager, and thus can notify
			// the message.
			this.notifyMessage();
		} 
	}
	
	
	protected void enableMessageQueue(boolean enable){
		if( this.message_queue != null ){
			this.message_queue.setEnabled(enable);
		}
	}
}
