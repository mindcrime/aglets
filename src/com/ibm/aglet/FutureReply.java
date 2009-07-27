package com.ibm.aglet;

import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;

/*
 * @(#)FutureReply.java
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
 * The <tt>FutureReply</tt> class is a abstract class for the
 * result of a future message.
 * 
 * @see AgletProxy#sendAsyncMessage
 * 
 * @version     1.50    $Date: 2009/07/27 10:31:41 $
 * @author	Mitsuru Oshima
 */

public abstract class FutureReply {

	/*
	 * Informs that this result is added to ReplySet object.
	 * This is not normally used by the aglet programmers.
	 */
	abstract protected void addedTo(ReplySet set);
	/**
	 * Gets the reply as a primitive boolean.
	 */
	public boolean getBooleanReply() 
			throws MessageException, NotHandledException {
		return ((Boolean)getReply()).booleanValue();
	}
	/**
	 * Gets the reply as a primitive char.
	 */
	public char getCharReply() throws MessageException, NotHandledException {
		return ((Character)getReply()).charValue();
	}
	/**
	 * Gets the reply as a primitive double.
	 */
	public double getDoubleReply() 
			throws MessageException, NotHandledException {
		return ((Number)getReply()).doubleValue();
	}
	/**
	 * Gets the reply as a primitive float.
	 */
	public float getFloatReply() 
			throws MessageException, NotHandledException {
		return ((Number)getReply()).floatValue();
	}
	/**
	 * Gets the reply as a primitive integer.
	 */
	public int getIntReply() throws MessageException, NotHandledException {
		return ((Number)getReply()).intValue();
	}
	/**
	 * Gets the reply as a primitive long.
	 */
	public long getLongReply() throws MessageException, NotHandledException {
		return ((Number)getReply()).longValue();
	}
	/**
	 * If the message was not handled the receiver, MessageNotHandled
	 * exception is raised.
	 * 
	 * @see Message#sendReply
	 * @return reply object.
	 * @exception NotHandledException if the message was not handled.
	 */
	abstract public Object getReply() 
			throws MessageException, NotHandledException;
	/**
	 * Checks if the reply is available or not.
	 * @return boolean true if the reply has been set.
	 */
	abstract public boolean isAvailable();
	/**
	 * Waits for a reply until the reply is available.
	 */
	abstract public void waitForReply();
	/**
	 * Waits for a reply with specific timeout value.
	 * @param timeout	the maximum time to wait in milliseconds
	 */
	abstract public void waitForReply(long timeout);
}
