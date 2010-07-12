package com.ibm.aglets;

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

import org.apache.log4j.Logger;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.MessageManager;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglets.*;
import com.ibm.aglets.log.LoggerFactory;
import com.ibm.aglets.tahiti.MainWindow;

import examples.hello.HelloAglet;
import net.sourceforge.aglets.rolex.*;

// import com.ibm.awb.misc.Debug;

public		// TEMPORARY (because of Fiji)
final class AgletThread extends Thread {
	private boolean valid = true;
	private boolean start = false;
	private boolean loop_started = false;

	private MessageManagerImpl manager = null;
	private MessageImpl message = null;
	
	/**
	 * A logger for this class.
	 */
	Logger logger = LoggerFactory.getLogger(AgletThread.class);

	/**
	 * This flag indicates whenever the thread is waiting (should be
	 * in the thread pool then).
	 */
	private boolean waiting = false;

	static int count = 1;

	public AgletThread(ThreadGroup group, MessageManager m) {
		super(group, "No." + (count++) + ']');
		setManager(m);
		setPriority(group.getMaxPriority());
	}
	static MessageImpl getCurrentMessage() {
		Thread t = Thread.currentThread();

		if (t instanceof AgletThread) {
			return ((AgletThread)t).message;
		}
		return null;
	}
	
	
	
	/**
	 * Handle a message for a specified agent. Please note that this method is called from the sender thread,
	 * thus this method starts the current thread object in order to manage the message.
	 * @param msg the message to handle.
	 */
	void handleMessage(MessageImpl msg) {
	
		// store the message to handle and enable the message handling thru the 
		// start flag
		message = msg;
		start   = true;
		
		
		// if this thread is still alive (i.e., not destroyed), try to resume the thread itself, since
		// from the run method the thread should be waiting on this object itself.
		if (isAlive()) {
			// this branch is executed only after the thread has been pushed into the thread pool
			
			// synchronized block is needed only when the thread
			// is already running.
			synchronized (this) {
				notifyAll();
			}
		} else {
			// start this thread, since it seems to be sleeping
			this.start();
		}
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
	
	
	
	public void run() {


		if (loop_started) {

			// to assure that aglet cannot call run on this thread.
			return;
		}

		loop_started = true;
		start = false;

		//LocalAgletRef ref = manager.getAgletRef();

		// Debug.start();

		try {
			while (valid) {
				try {
					LocalAgletRef ref = getManager().getAgletRef();
					logger.debug("Delivering a message to the ref "+ref);
					if( message != null && !(message instanceof RolexMessage) ){
						logger.debug("The message is a normal message");
						message.handle(ref);
						if( message instanceof SystemMessage ){
							logger.debug("The message is a SystemMessage, set the start flag appropriately");
							start = ((SystemMessage)message).type == SystemMessage.CREATE;
						}
						else
							start = false;
					}
					else{
                        if (message instanceof RolexMessage && 
                        	((RolexMessage) message).isExclusive()) {
							logger.debug("The message is a RoleX message:"+message);
							//message.handle(ref);
							
							try{
								Aglet  a = ref.prepareForAgletSubstitution();
								logger.debug("Preparation for the agent substitution completed");
								ref.completeAgletSubstitution(a);
								logger.debug("Substitution completed");
							}catch(AgletException e){
								System.err.println("Rolex Exception "+e);
								e.printStackTrace();
							}
						}
                        
                        
                        
						start = false;
					}
					message = null;
				} catch (RuntimeException ex) {
					valid = false;
					throw ex;
				} catch (Error ex) {
					valid = false;
					throw ex;
				} catch (InvalidAgletException ex) {
					ex.printStackTrace();
					valid = false;
					start = true;
				}
				finally {

					// IMPORTANT: since the threads are now shared
					// I need to synchronize for the thread releasing and
					// waiting

					synchronized(this){

						if (valid) {
							getManager().pushThreadAndExitMonitorIfOwner(this);
						} else {
							getManager().exitMonitorIfOwner();
						}

						try{
							while( start == false && valid){
								this.setWaiting(true);
								this.wait();
								this.setWaiting(false);
							}
						}catch(InterruptedException ex){
							System.err.println("AgletThread - cannot suspend myself ");
							ex.printStackTrace();
							this.setWaiting(false);
						}

						start = false;


					}	// end of the synchronized block

				}

			}
		}
		finally {
			getManager().removeThread(this);
			message = null;

			// Debug.end();
		}
	}
	public String toString() {
		MessageImpl m = message;

		if (m == null) {
			return "AgletThread[" + getName() + ", priority = "
				   + getPriority() + ", valid = " + valid + ", start = "
				   + start;
		} else {
			return "AgletThread[" + getName() + "," + m.toString()
				   + ", priority = " + getPriority() + ", valid = " + valid
				   + ", start = " + start;
		}
	}


	/**
	 * A method to set the message manager for this thread. Since the thread can be
	 * shared from a pool. it is important to set the message manager
	 * before the message is processed.
	 * @param manager the message manager to handle the message
	 */
	public void setMessageManager(MessageManager manager){
		if(manager instanceof MessageManagerImpl){
			this.setManager((MessageManagerImpl) manager);
		}
	}

	/**
	 * Indicates if this thread is waiting, that means has done a wait call.
	 * <B>Should be invoked by a synchronized block!</B>
	 * @return true if the thread is waiting
	 */
	public synchronized boolean isWaiting(){
		return waiting;
	}

	/**
	 * Sets the waiting flag for this thread.
	 * @param waiting the waiting status.
	 */
	protected synchronized void setWaiting(boolean waiting){
		this.waiting = waiting;
	}
	/**
	 * Sets the message manager implementation.
	 * @param manager The manager to set.
	 */
	protected void setManager(MessageManager manager) {
		if( manager instanceof MessageManagerImpl)
			this.manager = (MessageManagerImpl) manager;
	}
	/**
	 * Returns the message manager of this thread.
	 * @return Returns the manager.
	 */
	protected MessageManagerImpl getManager() {
		return manager;
	}

}
