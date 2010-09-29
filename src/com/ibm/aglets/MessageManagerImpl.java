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

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageManager;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.aglets.thread.AgletThreadPool;

// import com.ibm.awb.misc.Debug;

/**
 * The <tt>MessageManagerReplyImpl</tt> class is an implementation of
 * com.ibm.aglet.MessageManager interface.
 * 
 * The message manager is the decoupling point between a sender thread and a
 * receiver one. In fact, when an agent sends a message to another agent, it
 * comes up to the message manager of the addressee agent and enters the
 * postMessage method. Such method is quite complex, but briefly stores the
 * message into the addressee message queue and then notifies the message
 * manager itself that are at least one new pending message. The addressee
 * message manager then pops a thread from the thread pool and then processes
 * the message at the top of the queue (and all the following ones) until the
 * queue is empty. After that the message managers waits for other messages to
 * come.
 * 
 * Note by Luca Ferrari: I've tried to clean up the code related to the post and
 * process message. In particular now the code uses the new global thread pool
 * and keeps the same thread for re-entrant messages. Please consider that now,
 * due to the new message queue, the cancelMessages method simply remove all the
 * messages from the message queue. This should not be an error since the old
 * code simply cancel and destroy all the messages in the message queue, but
 * they should not be processed then.
 * 
 * @version 2.0 9-8-2007
 * @author Mitsuru Oshima
 * @author Luca Ferrari
 */
public final class MessageManagerImpl implements MessageManager,
java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 5056206648262340360L;

    private static AgletsLogger logger = AgletsLogger.getLogger(MessageManagerImpl.class.getName());

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
    static private String[] state_string = { "UNINITIALIZED", "RUNNING",
	"SUSPENDED", "DEACTIVATED", "DESTRYOED" };

    transient private MessageQueue<MessageImpl> message_queue = new MessageQueue<MessageImpl>();

    transient private MessageQueue<MessageImpl> waiting_queue = new MessageQueue<MessageImpl>();

    /**
     * A priority table. If a message does not specifies a priority, the message
     * manager can check this table to see if the message kind is contained, and
     * in such case can use the priority assigned in the table for the above
     * message. Priorities are expressed as integers, and the keys are strings
     * that represent the message kinds.
     */
    private HashMap<String, Integer> priorityTable = null;

    /**
     * A default priority table, used when a priority cannot be found both in
     * the message and the priority table.
     */
    static private HashMap<String, Integer> defaultPriorityTable = new HashMap<String, Integer>(5);

    static {
	// assign default priorities for the well known message kinds
	defaultPriorityTable.put(Message.CLONE, new Integer(Message.REQUEST_PRIORITY));
	defaultPriorityTable.put(Message.DISPOSE, new Integer(Message.REQUEST_PRIORITY));
	defaultPriorityTable.put(Message.DISPATCH, new Integer(Message.REQUEST_PRIORITY));
	defaultPriorityTable.put(Message.DEACTIVATE, new Integer(Message.REQUEST_PRIORITY));
	defaultPriorityTable.put(Message.REVERT, new Integer(Message.REQUEST_PRIORITY));
    }

    /**
     * The message owner of this message manager. The owner is a special message
     * that has been peecked from the message queue and is going to be
     * processed. In other words is the closest message to be processed, being
     * processed or that has just been processed. Several critical methods of
     * the message manager should check if the current thread is the owner, that
     * is if it is the thread that has been chosen to process the owner message.
     */
    transient private MessageImpl owner = null;

    /**
     * The thread pool. Each thread used to process a message must be extracted
     * from this pool. Please note that the poll is global across the platform
     * thus this is only a reference to the pool, not a real private instance.
     */
    transient private AgletThreadPool threadPool = AgletThreadPool.getInstance();

    transient private LocalAgletRef ref;

    private Vector activationTable = null;

    int state = UNINITIALIZED;

    /**
     * This flag indicates if the aglet is sleeping, and so does its message
     * manager. In other words, each time the aglet starts sleeping, this flag
     * is set and it stays while the sleep period has expired.
     */
    private boolean sleeping = false;

    /*
     * public void close() { synchronized(message_queue) { closed = true; } }
     * 
     * public boolean isEmpty() { return message_queue.peek() == null; }
     */
    /**
     * The constructor has been set up as public for testing, and since I don't
     * like to enforce protection based on package access (Luca).
     */
    public MessageManagerImpl(LocalAgletRef ref) {
	HashMap<String, Integer> clone = (HashMap<String, Integer>) defaultPriorityTable.clone();
	this.priorityTable = clone;
	this.ref = ref;
    }

    /*
     * Cancel all messages in the message queue
     */
    void cancelMessagesInMessageQueue() {
	this.message_queue.removeAll();
    }

    /*
     * Cancel all messages in the waiting queue void cancelMiscMessages() { int
     * size = misc.size(); MessageImpl msg; for(int i=0; i<size; i++) { msg =
     * ((MessageImpl)misc.elementAt(i));
     * msg.cancel("handler destroyed : message = " + msg.toString());
     * msg.destroy(); } }
     */

    /*
     * Cancel all messages in the waiting queue
     */
    void cancelMessagesInWaitingQueue() {
	this.waiting_queue.removeAll();
    }

    /*
     * Cancel the owner message
     */
    void cancelOwnerMessage() {
	if ((this.owner != null) && (this.isOwner() == false)) {
	    this.owner.cancel("handler destroyed : message = "
		    + this.owner.toString());
	    this.owner.destroy();
	    this.owner = null;
	}
    }

    void deactivate() {
	synchronized (this.message_queue) {
	    if (this.isSuspended() == false) {
		throw new IllegalArgumentException("Cannot deactivate");
	    }
	    this.setState(DEACTIVATED);

	    this.cancelOwnerMessage();
	    this.cancelMessagesInWaitingQueue();
	}

	this.cancelMessagesInMessageQueue();
    }

    //
    // This have to be improved.
    //
    @Override
    public void destroy() {
	// Debug.check();
	synchronized (this.message_queue) {
	    if (this.isDestroyed()) {
		return;
	    }
	    this.setState(DESTROYED);
	    this.ref = null;

	    // Debug.check();
	    this.cancelOwnerMessage();

	    // invalidate all waiting messages
	    // This must be in the synchronized(message_queue) block
	    // @see notifyMessage
	    // @see notifyAllMessages
	    // Debug.check();
	    this.cancelMessagesInWaitingQueue();
	}

	// invalidate all messages in the queue
	// This doesn't have to be in synchronized block because
	// no one can activate queued message
	// Debug.check();
	this.cancelMessagesInMessageQueue();

	// invalidate all threads
	// Debug.check();

	// Debug.check();
    }

    @Override
    public void exitMonitor() {
	synchronized (this.message_queue) {
	    if (this.isOwner() == false) {
		throw new IllegalMonitorStateException("Current thread is not owner "
			+ Thread.currentThread() + " != " + this.owner);
	    }
	    this.processNextMessage();
	}
    }

    public void exitMonitorIfOwner() {
	synchronized (this.message_queue) {
	    if (this.isOwner()) {
		this.processNextMessage();
	    }
	}
    }

    public LocalAgletRef getAgletRef() {
	return this.ref;
    }

    public boolean isDeactivated() {
	return this.state == DEACTIVATED;
    }

    public boolean isDestroyed() {
	return this.state == DESTROYED;
    }

    /**
     * This method is used to check if the current thread is the one used to
     * process last message, or better if the message that is currently stored
     * as owner of this message manager has been attached to the same thread of
     * the one that calls this method. The aim of this method is to provide a
     * way to check who is calling critical methods of the message manager. For
     * instance, it should not be possible to call the processNextMessage()
     * method (to process another message) if not owning the message manager,
     * that is if the thread is different from a message processing thread.
     * 
     * @return true if the current thread is the same attached to the owner
     *         message
     */
    boolean isOwner() {
	if ((this.owner != null)
		&& this.owner.thread.equals(Thread.currentThread())) {
	    return true;
	} else {
	    return false;
	}
    }

    public boolean isRunning() {
	return this.state == RUNNING;
    }

    public boolean isSuspended() {
	return this.state == SUSPENDED;
    }

    public boolean isUninitialized() {
	return this.state == UNINITIALIZED;
    }

    /* package protected */
    /**
     * Kills the message manager and thus the agent. Please note that no more
     * actions on the thread are required, since the threads are shared.
     */
    void kill() {
	logger.debug("The message manager is being killed...");
	this.setState(DESTROYED);
	this.ref = null;
    }

    @Override
    public void notifyAllMessages() {
	MessageImpl notifier = null;

	synchronized (this.message_queue) {
	    if (this.isOwner() == false) {
		throw new IllegalMonitorStateException("Current thread is not owner");
	    }
	    notifier = this.owner;
	    if (this.waiting_queue.peek() != null) {
		this.message_queue.insertAtTop(notifier);
		this.message_queue.insertAtTop(this.waiting_queue);
		this.waiting_queue.removeAll();

		notifier.setWaiting();
		this.processNextMessage();
	    }
	}
	notifier.doWait();
    }

    @Override
    public void notifyMessage() {
	MessageImpl notifier = null;
	MessageImpl waiting = null;

	synchronized (this.message_queue) {
	    if (this.isOwner() == false) {
		throw new IllegalMonitorStateException("Current thread is not owner");
	    }
	    notifier = this.owner;

	    // remove waiting message from queue.
	    waiting = this.waiting_queue.pop();
	    if (waiting != null) {

		// set the waiting message to the top of the queue
		// and put the notifier on the next of the queue
		this.message_queue.insertAtTop(notifier);
		this.message_queue.insertAtTop(waiting);

		notifier.setWaiting();
		this.processNextMessage();
	    }
	}

	notifier.doWait();
    }

    /*
     * Thread Management
     */
    /**
     * Pops a thread from the pool. All the details about the thread management
     * and creation are now within the aglet thread pool, thus this method is
     * only a proxy for the pool popThread method.ù
     * 
     * @return the thread to use associated to the current message manager
     * @throws AgletException
     *             if the message manager has been destroyed or there is a
     *             problem getting the thread from the pool
     */
    AgletThread popThread() throws AgletException {
	// check if the message manager has been destroyed.
	if (this.isDestroyed()) {
	    logger.info("The message manager has been destroyed but a thread is being popped...not possible!");
	    throw new AgletException("Cannot pop a thread if the message manager has been destroyed!");
	}

	// check to have a valid thread pool
	if (this.threadPool == null)
	    this.threadPool = AgletThreadPool.getInstance();

	// get a new thread from the pool and return it
	return this.threadPool.pop(this);

    }

    void postMessage(MessageImpl msg) {
	this.postMessage(msg, false);
    }

    /**
     * Post a message to the Aglet behind this message manager. This method is
     * called from the sendXXXMessage methods of an aglet reference (i.e., from
     * the aglet proxy). Briefly this method has to enqueue the incoming message
     * and to notify a thread of process such message. Things are little more
     * complicated due to the different scenarios that can happen.
     * 
     * The simplest case is if a message is sent by another agent, that is by
     * another thread. In this case the message is simply enqueued and a call to
     * processNextIfEmpty is issued. Such method is used to wake up this message
     * manager threads to process the message(s) in the message queue. If the
     * message manager is not processing a message (i.e., the owner is null),
     * then a thread is required and the processing starts. In other words in
     * the above case a message is enqueued and then forced to be processed if
     * the message manager has not yet took a thread to process the message
     * queue.
     * 
     * If the message manager is not active (i.e., not processing messages) then
     * the message is discarded. As an exception, if the message is contained in
     * the activation table, then the message manager is woken up. More in
     * detail, if the message is one of those that can be processed (i.e., it is
     * in the activation table) it is pushed back to the agent reference
     * (LocalAgletRef) that will then pass it back thru the postMessage method.
     * This time the reference and the message manager are active and the
     * message will be processed normally.
     * 
     * If the message is posted by the same thread that is processing the
     * message itself, then we have a re-entrant message. This situation can
     * happen if the agent itself post a message to itself as a reply for
     * another message, such as the example below: <code>
     *  public boolean handleMessage(Message msg){
     * 	try{
     * 	// change the post message flag
     * 	this.postMessage = ! this.postMessage;
     * 
     * 
     * 	if( this.postMessage ){
     * 	    System.out.println("\n\t POSTING A MESSAGE TO MYSELF\n");
     * 	    AgletProxy myself = this.getProxy();
     * 	    Message mess = new Message("REENTRANT_MESSAGE_TO_MYSELF");
     * 	    //myself.sendOnewayMessage(mess);
     * 	    myself.sendMessage(mess);
     * 	}
     *     ....
     * }
     * </code> or if the agent, when receiving a message, decides to do
     * something that requires posting another message, maybe a system message
     * (e.g., suspend, clone, move, etc.). To better understand the situation
     * consider that we are in the postMessage with thread T1 and message M1,
     * and that the agent wants to send to itself the message M2. We understand
     * this when we are in the postMessage with T1 and M1 as owner (i.e.,
     * currently processed message) of the message manager. The old management
     * of such cases worked as follows: 1) put M2 as first message in the
     * message queue; 2) put M1 as second message in the message queue; 3a) call
     * processNextMessage(), that will claim a new thread T2 and will start it
     * to process M2; 3b) at the same time suspend T1 waiting for T2 for finish.
     * 4) once T2 has processed M2 (that is now the new owner of the message
     * manager), it will continue processing M1 (since T2 is the thread owner)
     * and thus M1 must not be processed no more by T1. 5) T1 is waked up and
     * finishes without doing something more with M1.
     * 
     * With the new threading system things go differently: the idea is that
     * since T1 is putting M2 on the message "stack", then T1 should process
     * both M1 and M2. This avoids resource wasting (we have already a thread,
     * we don't need to get another one) and provides coherency. More in detail:
     * 1) T1 puts M2 on the top of the message queue; 2) T1 puts M1 as second
     * message in the queue; 3) T1 marks itself as re-entrant, thus the
     * AgletThread knows that before suspending it will called to process
     * another message; 4) a call to processNextMessage() is issued. This
     * changes the message of T1, but this does not matter since T1 has already
     * processed M1 and can re-loop to process M2. 4.1) the thread T1 instead of
     * suspending itself in the pool re-loop and process M2, but before this
     * marks itself as no-reentrant, thus to not loop forever. If another
     * re-entrant message comes, than steps from 1-4 are repeated and the thread
     * will re-loop.
     * 
     * @param msg
     *            the message to send to the agent
     * @param oneway
     */
    private void postMessage(MessageImpl msg, boolean oneway) {
	// check the argument
	if (msg == null)
	    return;

	logger.debug("MessageManagerImpl is receiving a new message, within the postMessage method...");

	// use the norm priority for the message
	int priority = NORM_PRIORITY;
	MessageImpl reentrantOwner = null;

	synchronized (this.message_queue) {
	    if (this.isDestroyed()) {
		// the message manager has been destroyed, therefore this
		// message
		// cannot be processed and so it is canceled
		logger.debug("Message manager has been destroyed and so it cannot process the message <"
			+ msg + ">");
		msg.cancel("Message manager has been destroyed and cannot process the message");
		return;
	    } else if (this.isDeactivated()) {
		// the message manager has been deactivated, this means it is
		// not receiving
		// messages but it can be resumed if a special message is
		// incoming. Such special
		// message must be listed in the activation table. This means
		// that if the current
		// message is in the activation table the message manager can be
		// woke up, otherwise
		// the message must be deleted.
		if ((this.activationTable != null)
			&& this.activationTable.contains(msg.getKind())) {
		    try {
			// reactivate the message manager. Reactivating means:
			// 1) activate the localagletref so it can receive again
			// messages
			// 2) enable the message to be delegatable, that is to
			// be passed thru another component
			// 3) delegate the message to the localagletref, that
			// will re-post the message to this
			// message manager. Since the message manager is now
			// activated it will process the message.
			logger.debug("Re-activating the message manager and delegating the message");
			this.ref.activate(); // activate the aglet ref and the
			// message manager
			msg.enableDelegation(); // set the message to accept
			// delegation
			this.ref.delegateMessage(msg); // delegate the message
			// to the aglet ref and
			// thus to this message
			// manager again
			return; // nothing more to do (the delegation will
			// re-enter this method to process the message)
		    } catch (Exception ex) {
			// cannot re-activate
			logger.error("Exception caught while re-activating the message manager", ex);
			msg.cancel("Exception caught while re-activating the message manager");
			return;
		    }
		} else {
		    // the message cannot wake up the message manager, and thus
		    // cannot be processed
		    msg.cancel("The message manager is deactivated and this message cannot re-activate it (it is not in the activation table)");
		    return;
		}
	    }

	    // if here the message manager is (now) active (it can has been
	    // activated thru
	    // the activation table), and thus it must process the message

	    // first of all it is important to establish the message priority.
	    // Each message has a desidered priority, but it is the message
	    // manager
	    // that must decide at which priority to process this kind of
	    // message. So the deal
	    // is to check first in the priority table for this kind of message,
	    // and only if a priority
	    // is not found to use the message one.
	    String msgKind = msg.getKind();
	    if (msgKind != null) {
		if (this.priorityTable.containsKey(msgKind))
		    priority = this.priorityTable.get(msgKind);
		else {
		    priority = msg.getPriority();
		    // store the priority for this kind of message, thus it can
		    // be re-processed later at the same priority
		    this.priorityTable.put(msgKind, priority);
		}
	    } else
		priority = msg.getPriority(); // the message has no kind, run it
	    // at its own priority

	    // depending on the message priority we can have two cases:
	    // 1) the message has a very high priority and must be processed
	    // immediatly
	    // (i.e., without being queued)
	    // 2) the message has a normal priority and must be processed after
	    // being queued.
	    if (priority <= Message.UNQUEUED_PRIORITY) {
		// process the message immedately
		msg.activate(this);
		return;
	    } else {
		// queue the message and process when it is time

		// here it can happen two kind of situations:
		// 1) the message has been posted by the current owner, that is
		// from the thread
		// that is already processing a message (owner). This could
		// happen, for instance,
		// if a message requires the execution of an action that
		// requires another system action
		// (for instance receiving a message - thus processing such
		// message as owner - the agent
		// requires an action that posts a message - for example to
		// suspend itself).
		// See examples.thread.ReentrantThreadAgent for an example.
		//
		// To process such condition we must place at the top of the
		// queue the current message (i.e., the message
		// that is being posted) and immediatly after the message owner
		// (i.e., the one being processed).
		// In this way the message re-entrant will be processed as
		// first, and the old owner immediatly after.
		//
		//
		// 2) the message comes from another thread and thus can be
		// inserted into the message queue
		//

		if (this.isOwner() && (oneway == false)) {
		    // place the current owner into the message queue with the
		    // re-entrant priority
		    reentrantOwner = this.owner;
		    reentrantOwner.setPriority(Message.REENTRANT_PRIORITY);
		    this.message_queue.insertAtTop(reentrantOwner);

		    // now place on top the current message
		    msg.setPriority(Message.REENTRANT_PRIORITY);
		    msg.setThread(this.owner.getThread()); // copy the thread,
		    // thus if a thread
		    // has already been
		    // assigned we don't
		    // need to get one
		    // new thread from
		    // the thread pool
		    msg.setReplyAvailable(); // needed if we want to work with
		    // the sendMessage methods, since
		    // they will
		    // block waiting for a reply, that
		    // will never come since the agent
		    // will not
		    // respond to itself!
		    AgletThread aThread = this.owner.getThread();
		    if (aThread != null)
			aThread.setReentrant(true); // with this the aglet
		    // thread that is processing
		    // the owner message
		    // knows that it is
		    // re-entrant, and thus that
		    // it will process this
		    // message too

		    this.message_queue.insertAtTop(msg);

		    // process a new message. This will cause the message queue
		    // to place the top message
		    // (i.e., msg) as owner and to activate the message (i.e.,
		    // to take a thread and process it).
		    // Once the thread has finished processing the message, it
		    // will call pushThreadAndExitIfOwner
		    // and thus it will notice that it is the owner of the
		    // message manager, therefore will process another
		    // message. The next message this time will be the old
		    // owner, that will now be processed.
		    this.processNextMessage();

		} else {
		    // normal message, queue it
		    msg.setPriority(priority);
		    this.message_queue.insert(msg);

		    // now process a message only if there is not a thread
		    // processing
		    // already a message
		    this.processNextMessageIfEmpty();
		}

	    }
	} // end of the synchronized block

	logger.debug("message queue\n" + this.message_queue.toString());

    }

    /**
     * This method starts the real processing of a message, and it is usually
     * called within the postMessage methods. The basic idea is to peek the
     * first message on top of the message queue and to activate it. Activating
     * a message will require the message manager to pop a thread from the
     * thread pool and the message to call handleMessage on such thread, and
     * thus the thread will process the message.
     * 
     * Messages will not be processed if the message manager is destroyed, it is
     * not running or it is sleeping (since they must be enqueued waiting for
     * the message manager to wake upo again). Please note that messages can be
     * processed if the message manager is closed, since it will consume all the
     * messages in the queue.
     * 
     */
    private void processNextMessage() {

	// don't process a message if the message manager is not running,
	// if it has been destroyed or if it is sleeping
	if ((!this.isRunning()) || this.isSleeping() || this.isDestroyed()) {
	    logger.debug("Message manager will not process the next message since it is not running, sleeping or even destroyed!!");
	    return;
	}

	// now peek the first message from the message queue and activate it!
	if (this.message_queue.peek() != null) {
	    // process the message: set the message as current owner of the
	    // message manager and activate it. Making the message as owner is
	    // useful
	    // to know which message (and thread) is currently processing the
	    // message
	    // manager
	    this.owner = this.message_queue.pop();
	    this.owner.activate(this);
	} else {
	    // nothing to process
	    this.owner = null;
	}
    }

    /**
     * Processes the next message only if the message manager has not already
     * started a processing cycle, that is only if the message manager owner is
     * null. Since this method wraps the processNextMessage() one, all the rules
     * of the latter applies to the former.
     * 
     */
    private void processNextMessageIfEmpty() {
	if (this.owner == null) {
	    this.processNextMessage();
	}
    }

    void pushMessage(MessageImpl msg) {
	this.postMessage(msg, true);
    }

    /*
     * void pushThreadAndExitMonitorIfOwner(AgletThread thread) {
     * synchronized(message_queue) { synchronized(threadSpool) {
     * pushThread(thread); if (isOwner()) { processNextMessage(); } } } }
     */

    /**
     * Places a thread back in the pool. Before the thread is placed in the pool
     * it's references are deleted (that is the message manager and the message
     * stored in the thread must be erased to avoid processing a message for
     * error).
     */
    void pushThread(AgletThread thread) {
	try {
	    // check the argument
	    if (thread == null)
		return;

	    // place the thread in the pool
	    this.threadPool.push(thread);
	} catch (AgletException ex) {
	    logger.error("Exception caught while pushing the thread into the thread pool", ex);
	}
    }

    public void pushThreadAndExitMonitorIfOwner(AgletThread thread) {
	synchronized (this.message_queue) {
	    // process another message
	    if (this.isOwner()) {
		this.processNextMessage();
	    }

	    // push the thread
	    this.pushThread(thread);
	}
    }

    /*
     * MessageManagerImpl(java.io.ObjectInput in, LocalAgletRef ref) throws
     * IOException, ClassNotFoundException { priorityTable =
     * (Hashtable)in.readObject(); activationTable = (Vector)in.readObject();
     * state = in.readInt(); this.ref = ref; }
     */

    private void readObject(java.io.ObjectInputStream s)
    throws IOException,
    ClassNotFoundException {
	s.defaultReadObject();

	this.message_queue = new MessageQueue();
	this.waiting_queue = new MessageQueue();
    }

    public void resume() { // ThreadGroup group) {
	synchronized (this.message_queue) {
	    if (this.isRunning() || this.isDestroyed()) {
		return;
	    }
	    this.setState(RUNNING);
	    this.ref.resourceManager.resumeAllThreads();
	    this.processNextMessage();

	    // processNextMessageIfEmpty();
	}
    }

    void setAgletRef(LocalAgletRef r) {
	this.ref = r;
    }

    @Override
    public void setPriority(String kind, int priority) {
	if ((priority & ACTIVATE_AGLET) == ACTIVATE_AGLET) {
	    if (this.activationTable == null) {
		this.activationTable = new Vector();
	    }
	    if (this.activationTable.contains(kind) == false) {
		this.activationTable.addElement(kind);
	    }
	    priority = priority & 0xF;
	    if (priority == 0) {

		// priority = NORM_PRIORITY;
		return;
	    }
	}

	if ((priority != NOT_QUEUED)
		&& ((priority < MIN_PRIORITY) || (priority > MAX_PRIORITY))) {
	    throw new IllegalArgumentException("illegal priority");
	}

	this.priorityTable.put(kind, new Integer(priority));

	// REMIND: re-sort the messages in the queue
    }

    /* synchronized (message_queue) */
    void setState(int next) {
	switch (this.state) {
	case UNINITIALIZED:
	    if ((next == RUNNING) || (next == DEACTIVATED)) {
		this.state = next;
		return;
	    }
	    break;
	case RUNNING:
	    if (next == SUSPENDED) {
		this.state = next;
		return;
	    }

	    break;
	case SUSPENDED:
	    if ((next == DESTROYED) || (next == RUNNING)
		    || (next == DEACTIVATED)) {
		this.state = next;
		return;
	    }
	    break;
	case DEACTIVATED:
	    if (next == RUNNING) {
		this.state = next;
		return;
	    }
	    break;
	case DESTROYED:
	default:

	    // cannot move to any state!
	    break;
	}
	throw new IllegalArgumentException("Cannot proceed from "
		+ state_string[this.state] + " to " + state_string[next]);
    }

    /*
     * Start
     */
    void start() {
	if (this.isUninitialized() == false) {
	    throw new IllegalMonitorStateException("MessageManager not valid");
	}
	synchronized (this.message_queue) {
	    this.setState(RUNNING);
	    this.processNextMessageIfEmpty();
	}
    }

    public void suspend() { // ThreadGroup group) {
	synchronized (this.message_queue) {
	    if (this.isSuspended() || this.isDestroyed()) {
		return;
	    }
	    this.setState(SUSPENDED);

	    // to make sure that no other thread has entered in the
	    // synchronous block.
	    this.ref.resourceManager.suspendAllThreads();
	}
    }

    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer(1000);

	buffer.append("MessageManagerImpl queues: ");
	buffer.append("\n");
	buffer.append(this.message_queue.toString());
	buffer.append(this.waiting_queue.toString());

	return buffer.toString();
    }

    @Override
    public void waitMessage() {
	this.waitMessage(0);
    }

    @Override
    public void waitMessage(long timeout) {

	MessageImpl wait = null;

	synchronized (this.message_queue) {
	    if (this.isOwner() == false) {
		throw new IllegalMonitorStateException("Current thread is not owner");
	    }
	    wait = this.owner;

	    // put the owner message to the waiting queue
	    this.waiting_queue.append(wait);
	    wait.setWaiting();
	    this.processNextMessage();
	}

	// wait outside of synchronized block to avoid dead lock
	if (timeout == 0) {

	    // short cut
	    wait.doWait();
	} else {
	    wait.doWait(timeout);
	    synchronized (this.message_queue) {
		if (wait.isWaiting()) {

		    // nobody didn't wake me up!
		    this.waiting_queue.remove(wait);

		    // REMIND: this must be improved to
		    // consider priority
		    this.message_queue.insertAtTop(wait);

		    // kick
		    this.processNextMessageIfEmpty();

		    // message_queue.notify();
		}
	    }

	    // if still waiting,
	    // wait again until the handler loop activates this message
	    wait.doWait();
	}
    }

    /**
     * Gets back the sleeping flag value. While the message manager (and the
     * agent) is sleeping messages can be enqueued, but the message manager
     * should not process them.
     * 
     * @return the sleeping value, true if the message manager is sleeping,
     *         false if it is not.
     */
    protected synchronized boolean isSleeping() {
	return this.sleeping;
    }

    /**
     * Sets the sleeping value.
     * 
     * @param sleeping
     *            the sleeping to set
     */
    protected synchronized void setSleeping(boolean sleeping) {
	logger.debug("The message manager is going to sleep " + sleeping);
	this.sleeping = sleeping;
    }
}
