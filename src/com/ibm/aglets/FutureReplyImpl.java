package com.ibm.aglets;

/*
 * @(#)FutureReplyImpl.java
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

import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.FutureReply;
import com.ibm.aglet.ReplySet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.MessageException;

import java.util.Vector;
import java.util.Enumeration;

/**
 * The <tt>FutureReplyImpl</tt> class is an implementation of
 * com.ibm.aglet.FutureReply abstract class.
 * 
 * @version     1.5    $Date: 2009/07/27 10:31:41 $
 * @author	Mitsuru Oshima
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 */
class FutureReplyImpl extends FutureReply {
	volatile boolean available = false;
	Object result;
	AgletException exception = null;
	ReplySet set[] = null;

	
	/**
	 * Keeps a crossreference to the replyset this future reply has been added to.
	 */
	final synchronized protected void addedTo(ReplySet replySet) {
		// check params
		if(replySet == null)
			return;
		
		// if the reply is available, then I set as done this reply in the reply set
		if (available) {
			replySet.done(this);
			return;
		} 
		
		// keep a crosscut reference to the replyset of this future reply. The set variable, of kind
		if (set == null || set.length == 0) {
			set    = new ReplySet[1];
			set[0] = replySet;
		} else{
			// add the reply set at the end of the array
			ReplySet tmp[] = new ReplySet[ set.length + 1 ];
			System.arraycopy(set,0,tmp,0,set.length);
			tmp[ tmp.length - 1 ] = replySet;
			set = tmp;
		}
			
	}
	
	
	/**
	 * Cancels from this reply (i.e., the caller is no more interested in this future reply???).
	 * Since version 1.5 this method does no raise an exception, it simply unlocks the waiting threads and
	 * reply sets.
	 * @param msg ????
	 * 
	 */
	synchronized void cancel(String msg) {
		if (!available) {
			available = true;
			//exception = new NotHandledException(msg);
			notifyAll();
			notifySet();
		} 
	}
	
	
	/**
	 * Returns the object of this reply. If the reply has raised an exception, than the exception is thrown.
	 */
	final synchronized public Object getReply() 
			throws MessageException, NotHandledException {
		waitForReply();
		if (exception != null) {
			// throw the exception
			if( exception instanceof MessageException )
				throw (MessageException)this.exception;
			else
			if( exception instanceof NotHandledException )
				throw (NotHandledException)this.exception;
		} 
		return result;
	}
	
	
	
	/**
	 * Is the future reply available?
	 */
	final public boolean isAvailable() {
		return available;
	}
	
	/**
	 * Notifies each replyset stored for this future reply about the 
	 * completation of the future reply. Then the reply set is set to null.
	 *
	 */
	final private void notifySet() {
		for(int i=0; this.set!=null && i<this.set.length; i++)
			this.set[i].done(this);
		
		this.set = null;
	}
	
	
	final synchronized void sendExceptionIfNeeded(Throwable ex) {
		if (!available) {
			setExceptionAndNotify(ex);
		} 
	}
	
	
	final synchronized void sendReplyIfNeeded(Object obj) {
		if (!available) {
			setReplyAndNotify(obj);
		} 
	}
	synchronized void setExceptionAndNotify(Throwable ex) {
		if (available) {
			throw new IllegalAccessError("Reply has been already set");
		} 
		available = true;
		exception = new MessageException(ex);
		notifyAll();

		notifySet();
	}
	
	synchronized void setReplyAndNotify(Object result) {
		if (available) {
			throw new IllegalAccessError("Reply has been already set");
		} 
		this.result = result;
		available = true;
		notifyAll();

		notifySet();
	}
	
	/**
	 * Suspends the caller on this method until a reply (or an exception) has been posted.
	 */
	final synchronized public void waitForReply() {
		while (available == false) {
			try {
				wait();
			} catch (InterruptedException ex) {
				System.err.println("Exception in FutureReplyImpl.waitForReply()");
				ex.printStackTrace();
			} 
		} 
	}
	
	/**
	 * Suspends the caller for the specified timeout or until a future reply comes. 
	 * @param timeout the number of msec the caller must be suspended max. If <=0 the caller
	 * is suspended until a reply comes.
	 */
	final synchronized public void waitForReply(long timeout) {
		if (timeout <= 0) {
			waitForReply();
		} else {
			long until = System.currentTimeMillis() + timeout;
			long reft;

			while (available == false 
				   && (reft = (until - System.currentTimeMillis())) > 0) {
				try {
					wait(reft);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} 
			} 
		} 
	}
}
