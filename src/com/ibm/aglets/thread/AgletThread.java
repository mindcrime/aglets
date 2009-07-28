package com.ibm.aglets.thread;

/*
 * @(#)AgletThread.java
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

import org.aglets.log.AgletsLogger;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageManager;
import com.ibm.aglets.LocalAgletRef;
import com.ibm.aglets.MessageImpl;
import com.ibm.aglets.MessageManagerImpl;

/**
 * The thread for message handling.
 * 
 * 
 * Notes from Luca Ferrari - cat4hire@users.sourceforge.net:
 * 
 * This class is a specific thread used to manage the message delivery. Once an agent must receive/process
 * a message, a thread is activated and used to process such message. In other words, the handleMessage() method
 * of an aglet is run on top of one of AgletThread.
 * 
 * Please note that the thread management involves the message manager and the message itself, since
 * the steps are the following:
 * 1) a message is delivered from a sender to the destination agent. The addressee agent stores the message
 * into a message queue, hold from the message manager.
 * 2) the message manager, once one or more thread have been stored in the queue, pops a thread and start
 * processing such threads one by one. In particular:
 *    a) the message is passed to the thread;
 *    b) the thread invokes the handle() method on the message itself
 *    c) the message invokes the handleMessage() on the aglet, passing itself as argument
 *    d) the thread exits the monitor, that is informs the message manager that the above message
 *       has been delivered. The message manager processes another message or leaves the thread. In the first case
 *       there is a chain that produces that until the message queue is empty the thread is hold to process
 *       messages. In the second case the thread is free, and the message manager waits until a new message comes.
 *       
 * 
 * In the 2.0.2 schema each agent has its own threadSpool, that is a stack of threads used to manage
 * only messages related to the owner agent. Once the thread has delivered the message, it is pushed back into
 * such stack (that is contained in the message manager).
 * 
 * In the 2.1.0 schema there is a thread pool that, globally, provides threads for the whole messaging system. Thus
 * the message manager does not handle any more a private stack of threads but requires them to the pool. 
 * Once the thread has delivered the message and no more messages must be processed for this agent, the
 * message manager pushes it back in the thread pool This allows a thread
 * to be used for different agents at different times. Please note that this implies that a thread must know not only
 * the message it is going to process, but also the message manager that oredered that, for coherence.
 * The thread has also two ways of locking:
 *  _ processing = it is processing a message, thus it cannot receive changes about the message itself or the message manager;
 *  _ changing = it is changing either the message manager or the message to process and thus cannot process it.
 *  
 *  Please note that the message manager will push and pop the thread again when it process a next message, this can bring to
 *  situations where the next message is processed by a different thread and, in general, wastes a little resources. Maybe this will be
 *  fixed in the future.
 *
 * 07/ago/07
 */
public
final class AgletThread extends Thread {
	private boolean valid = true;
	private boolean start = false;
	private boolean loop_started = false;

	private MessageManagerImpl messageManager = null;
	private MessageImpl message = null;

	/**
	 * A counter about the number of created threads.
	 */
	private static int count = 1;	// counts the number of thread created.
	
	/**
	 * The logger of this class of thread(s).
	 */
	private static AgletsLogger logger = AgletsLogger.getLogger("com.ibm.aglets.thread.AgletThread");
	
	/**
	 * The number of messages this thread has handled, just for a little statistic
	 * count.
	 */
	private int messageHandled = 0;
	
	
	/**
	 * Indicates if the thread is processing or not the current message.
	 */
	private boolean processing = false;
	
	/**
	 * Indicates if the something external to this thread is going to change the message to handle.
	 */
	private boolean changing = false;
	
	/**
	 * This flag indicates if the thread is managing a reentrant message.
	 */
	private boolean reentrant = false;
	
	
	/**
	 * Builds a thread knowing only the group. Used from the thread factory.
	 * @param group the thread group.
	 */
	public AgletThread(ThreadGroup group){
	    super(group, "AgletThread num.:" + (count++) );
	    this.messageManager = null;
	    this.setPriority(group.getMaxPriority());
	}

	public AgletThread(ThreadGroup group, MessageManager m) {
		super(group, "No." + (count++) + ']');
		messageManager = (MessageManagerImpl)m;
		setPriority(group.getMaxPriority());
	}
	
	public static MessageImpl getCurrentMessage() {
		Thread t = Thread.currentThread();

		if (t instanceof AgletThread) {
			AgletThread at = (AgletThread) t;
			return at.getMessage();
		} 
		return null;
	}
	
	
	/**
	 * This method is called each time a message must be processed. The idea is that
	 * this method should be called thru the proxy chain, and from this method the thread
	 * must be started/restarted and should process the message itself. Please note that this
	 * method is called within the sender's thread stack, and the message must be processed
	 * within the receiver thread stack.
	 * @param msg the message to process.
	 */
	public void handleMessage(MessageImpl msg) {
	    // check if the message is valid
	    if( msg == null )
		return;
	    
	    logger.debug("Handling a message => " + msg);

	    // set the message to use
	    this.setMessage(msg);
	    
	}
	
	
	
	synchronized public void invalidate() {

		// Debug.check();
		if (valid) {
			valid = false;
			start = true;
			notifyAll();
		} 

		// Debug.check();
	}
	
	
	/**
	 * The execution cycle of this thread. The thread will continue to process
	 * messages until it gets no more messages, and then it will suspend itself
	 * waiting for a new message to come.
	 */
	public void run() {
	    // if the loop of handing messages is already started return, so thus 
	    // no more than one run call can be done.
	    if (loop_started) {
		// to assure that aglet cannot call run on this thread.
		return;
	    } 

	    // set this thread as "started to handle messages"
	    loop_started = true;
	    start = false;
	    
	    // get the reference of the agent behind the message manager
	    if( this.messageManager == null )
		return;		// the message manager is not valid!

	    try {
		while (valid) {
		    try {
			
			logger.debug("AgletThread is starting processing");
			this.setReentrant(false);	// if the process is here and is re-entrant now I'm processing
							// a re-entrant message, thus after this I have to suspend myself.
			this.setProcessing(true);
		    	// get the right reference to the aglet behind the current
		    	// message manager. This must be done each time in the cycle because
		    	// the thread could be suspended or the message manager could be changed
		    	// if the thread has passed thru the pool.
			MessageManagerImpl manager = this.getMessageManager();
			logger.debug("The message manager is " + manager + ", the message is " + message);
		    	LocalAgletRef ref = manager.getAgletRef();
			message.handle(ref);	// handle the message
			this.messageHandled++;	// increment the number of messages handled by this thread
			
			synchronized(this){
			    if( ! this.isReentrant() ){
				message = null;		// invalidate the message so to not repeat the handling
				logger.debug("AgletThread has invalidate the message just processed (no reentrant find!)");
			    }
			}
			
			this.setProcessing(false);
			logger.debug("AgletThread finished processing a message");
			
		    } catch (RuntimeException ex) {
			logger.error("Exception caught while processing a message", ex);
			valid = false;
			throw ex;
		    } catch (Error ex) {
			logger.error("Error caught while processing a message");
			valid = false;
			throw ex;
		    } catch (InvalidAgletException ex) {
			logger.error("Exception caught while processing a message", ex);
			valid = false;
			start = true;
		    } finally {
			
			// if the thread is valid, i.e., it has not been stopped
			// then invoke special methods on the message manager to process
			// another message (thus once the thread has been activated all messages are processed)
			// or to process another message (if present) and to push back the thread in the pool.			
			if (valid && (! this.isReentrant())) {
			    // push the thread back into the pool...
			    logger.debug("The thread is going to be pushed back in the pool...");
			    messageManager.pushThreadAndExitMonitorIfOwner(this);
			} else {
			    // process one more message...
			    messageManager.exitMonitorIfOwner();
			} 
		    } 
		    
		    // here the message has been processed, thus I can suspend myself
		    // waiting for a new message to process
		    synchronized (this) {
			
			while (valid && this.message == null && (! this.isReentrant())) {
			    try {
				logger.debug("Thread suspending waiting for a next message...");
				this.wait();
			    } catch (InterruptedException ex) {
				logger.error("Exception caught while waiting for an incoming message", ex);			
			    } 
			} 
			
		    } 

		} 
		} 
		finally {
//			messageManager.removeThread(this);
			message = null;

			// Debug.end();
		} 
	}
	
	/**
	 * Provides a printable version of this thread and its state.
	 */
	public String toString() {
	    StringBuffer buffer = new StringBuffer(100);
	    
	    buffer.append("[" + this.getClass().getName() + "] ");
	    buffer.append("Thread number ");
	    buffer.append(count);
	    
	    if( this.messageManager != null ){
		buffer.append("\n\tMessageManager: )");
		buffer.append(this.messageManager);
	    }
	    
	    if( this.message != null ){
		buffer.append("\n\tMessage: ");
		buffer.append(this.message);
	    }
	    
	    buffer.append("\n\t\tvalid: " + this.valid);
	    buffer.append("\n\t\tmessageHandled: " + this.messageHandled);
	    buffer.append("\n\t\tstart: " + this.start);
	    buffer.append("\n");
	    
	    return buffer.toString();
	    
	}

	/**
	 * Gets back the message.
	 * @return the message
	 */
	protected synchronized MessageImpl getMessage() {
	    return message;
	}

	/**
	 * Sets the message value.
	 * @param message the message to set
	 */
	protected synchronized void setMessage(MessageImpl message) {
	    try{
		// first of all check if the thread is alive and is already processing
		// a message, in such case wait until it has finished of processing
		// the message, then change it.
		
		while( this.isAlive() && this.isProcessing() && (! this.isReentrant() )){
		    logger.debug("Thread waiting to set the message to process...");
		    this.wait();		// suspend myself until the current message has been processed
		}
		
		// substitute the message
		this.setChanging(true);
		logger.debug("Setting the message " + message + " for the current thread");
		this.message = message;
		this.start = true;
		
		// no more changing the message to handle
		this.setChanging(false);
		
		// is the thread running?
		if( ! this.isAlive() )
		    this.start();
		
		// resume suspended threads
		this.notifyAll();
	    
	    }catch(InterruptedException e){
		logger.error("Exception caught while trying to set a new message to process.", e);
	    }
	}

	/**
	 * Gets back the messageManager.
	 * @return the messageManager
	 */
	protected synchronized MessageManagerImpl getMessageManager() {
	    return messageManager;
	}

	/**
	 * Sets the messageManager value.
	 * @param messageManager the messageManager to set
	 */
	protected synchronized void setMessageManager(MessageManagerImpl messageManager) {
	    try{
		// first of all check if the thread is alive and is already processing
		// a message, in such case wait until it has finished of processing
		// the message, then change it.
		
		while( this.isAlive() && this.isProcessing() ){
		    this.wait();		// suspend myself until the current message has been processed
		}
		
		// substitute the message
		this.setChanging(true);
		this.messageManager = messageManager;
		this.start = true;
		
		// no more changing the message to handle
		this.setChanging(false);
		
		// resume suspended threads
		this.notifyAll();
	    
	    }catch(InterruptedException e){
		logger.error("Exception caught while trying to set a new messageManager.", e);
	    }
	}

	/**	
	 * Used to understand if the current thread is processing a message or not, and thus if it is
	 * safe to change the message this thread is handling.
	 * @return the processing
	 */
	protected synchronized boolean isProcessing() {
	    return processing;
	}

	/**
	 * Sets the processing value.
	 * @param processing the processing to set
	 */
	protected synchronized void setProcessing(boolean processing) {
	    // check if the thread is already changing the current message,
	    // thus I need to suspend and to wait, otherwise proceed
	    try{
		while( this.isAlive() && this.isChanging() ){
		    logger.debug("Thread waiting to set the processing flag...");
		    this.wait();
		}
		
		this.processing = processing;
		
		// resume suspended threads
		this.notifyAll();

		
	    }catch(InterruptedException e){
		logger.error("Exception caught while set processing value", e);
	    }
	}

	/**
	 * Gets back the changing.
	 * @return the changing
	 */
	protected synchronized boolean isChanging() {
	    return changing;
	}

	/**
	 * Sets the changing value.
	 * @param changing the changing to set
	 */
	protected synchronized void setChanging(boolean changing) {
	    // if the thread is already processing a message I must wait
	    try{
		while( this.isAlive() && this.isProcessing() && (! this.isReentrant() ) ){
		    logger.debug("Thread waiting to set the changing flag...");
		    this.wait();
		}
		
		this.changing = changing;
		
		// resume suspended threads
		this.notifyAll();

		
	    }catch(InterruptedException e){
		logger.error("Exception caught while set the changing status", e);
	    }
	    
	    this.changing = changing;
	}

	/**
	 * Gets back the reentrant flag.
	 * @return the reentrant
	 */
	public synchronized boolean isReentrant() {
	    return reentrant;
	}

	/**
	 * Sets the reentrant value. A re-entrant thread is a thread that must process another message
	 * sent from the same thread itself to the same agent.
	 * @param reentrant the reentrant to set
	 */
	public synchronized void setReentrant(boolean reentrant) {
	    logger.debug("This aglet thread will process one more re-entrant message!");
	    this.reentrant = reentrant;
	}
	
	
	
	
	
}
