package com.ibm.aglet.message;

/*
 * @(#)Message.java
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

import java.util.Hashtable;

import com.ibm.aglet.Aglet;

/**
 * The <tt>Message</tt> class is a object that holds its kind
 * and arguments passed to the receiver. In handleMessage() method
 * on Aglet class, the reply to the request can be set if any.
 * 
 * Note by Luca Ferrari:
 * the message now includes the concept of priority, that was hidden in the MessageImpl
 * subclass. Since the messages are enqueued and delivered with a priority, as well as they can
 * be processed depending on their priority, I've moved the priority mechanism here. Moreover
 * it is the message that defines the priority types and no more the MessageManager, thus the
 * priority are centralized.
 * 
 * Now the message also includes an empty constructor, that will build the message with a special
 * kind equal to the class name. This is useful if there is the needing to subclass the message, since
 * it will allow the kind mechanism to work having different message classes.
 * 
 * @version     2.0 9-8-2007
 * @author	Mitsuru Oshima
 * @author      Luca Ferrari  - cat4hire@users.sourceforge.net
 */
public class Message implements java.io.Serializable {

	static final long serialVersionUID = 5467548823007286376L;

	/**
	 * The types of message that indecates how the message was sent.
	 * @see Message#getMessageType();
	 */
	static public final int SYNCHRONOUS = 0;
	static public final int FUTURE = 1;
	static public final int ONEWAY = 2;

	/**
	 * These kinds are used to specify the priority of the system message.
	 * @see MessageManager#setPriority
	 */
	static final public String CLONE = "_clone";
	static final public String DISPATCH = "_dispatch";
	static final public String DISPOSE = "_dispose";
	static final public String DEACTIVATE = "_deactivate";
	static final public String REVERT = "_revert";

	/*
	 * An arbitrary argument.
	 */
	protected Object arg;

	/*
	 * The kind of the message.
	 */
	protected String kind;

	/*
	 * The time when the message was sent.
	 */
	protected long timestamp;
	

	/**
	 * The min priority a message can have.
	 */
	public static final int MIN_PRIORITY = 1;
	
	/**
	 * The normal priority of a message.
	 */
	public static final int NORMAL_PRIORITY = 5;
	
	/**
	 * The max priority a message can have.
	 */
	public static final int MAX_PRIORITY = 10;
	
	/**
	 * The re-entrant priority is a special priority value, higher than every other
	 * priority value (even of the MAX_PRIORITY) and it is used for messages
	 * that an agent sends to itself while processing another message.
	 */
	public static final int REENTRANT_PRIORITY = 0xffffffff;
	
	/**
	 * The system priority is the priority of a system message, that is
	 * a message used to notify special cases from the platform to the agent (e.g.,
	 * start execution, migrate, etc.).
	 */
	public static final int SYSTEM_PRIORITY = REENTRANT_PRIORITY - 1;
	public static final int REQUEST_PRIORITY = SYSTEM_PRIORITY - 1;
	
	/**
	 * All messages with a priority less or equal than this will be
	 * processed immediatly, without being placed in the message queue.
	 */
	public static final int UNQUEUED_PRIORITY = 0;

	/**
	 * The priority of this message (usually normal).
	 */
	protected int priority = NORMAL_PRIORITY;

	/**
	 * Gets back the priority.
	 * @return the priority
	 */
	public final int getPriority() {
	    return priority;
	}
	/**
	 * Sets the priority value.
	 * This method calls the normalizePriority() one in order to define the normalized priority for a message. 
	 * In the case the message implementation (i.e., the message handled internally by the server or by the
	 * message handling procedure) needs to set other special priorities, override the normalizePriority() method.
	 * @param priority the priority to set
	 */
	public final void setPriority(int priority) {
	    // if a system message priority already set, don't do anything
	    if( this.priority == SYSTEM_PRIORITY )
		return;

	    this.priority = this.normalizePriority(priority);
	}
	
	/**
	 * Normalizes a priority for a message. If the priority is not included in the bounds
	 * of min/max priority, then a normal priority is returned.
	 * The priority should be in a range between the MIN and MAX priority
	 * values. If the priority is outside the bounds it will be set at norm priority by default. Please note
	 * that if the message has been created with the SYSTEM_PRIORITY it will not be possible to change
	 * its priority (this is to avoid that a system message can be lowered).
	 * @param priority the priority of the message that the user wants to set
	 * @return the effective priority this message should use.
	 */
	protected int normalizePriority(int priority){
	    if( priority < MIN_PRIORITY || priority > MAX_PRIORITY )
		return NORMAL_PRIORITY;
	    else
		return priority;
	}
	
	/**
	 * Builds a message with a pre-defined kind that is the class type.
	 * This constructor is useful when sub-classing the message type.
	 *
	 */
	public Message(){
	    super();
	    this.kind = this.getClass().getName();
	    this.arg = new Arguments();
	}
	
	
	/**
	 * Constructs a message. The message object created by
	 * this constructor have a hashtable which can be used
	 * for argument-value pair.
	 * 
	 * <pre>
	 * Message msg = new Message("stock-price");
	 * msg.setArg("company", "ibm");
	 * msg.setArg("currency", "dollar");
	 * Double d = (Double) proxy.sendMessage(msg);
	 * </pre>
	 * 
	 * @param kind a kind of this message.
	 */
	public Message(String kind) {
		this(kind, new Arguments());
	}
	/**
	 * Constructs a message with an argument value.
	 * @param kind a kind of this message.
	 */
	public Message(String kind, char c) {
		this(kind, new Character(c));
	}
	/**
	 * Constructs a message with an argument value.
	 * @param kind a kind of this message.
	 */
	public Message(String kind, double d) {
		this(kind, new Double(d));
	}
	/**
	 * Constructs a message with an argument value.
	 * @param kind a kind of this message.
	 */
	public Message(String kind, float f) {
		this(kind, new Float(f));
	}
	/**
	 * Constructs a message with an argument value.
	 * @param kind a kind of this message.
	 */
	public Message(String kind, int i) {
		this(kind, new Integer(i));
	}
	/**
	 * Constructs a message with an argument value.
	 * @param kind a kind of this message.
	 */
	public Message(String kind, long l) {
		this(kind, new Long(l));
	}
	/**
	 * Constructs a message with an argument value.
	 * @param kind a kind of this message.
	 * @param arg an argument of this message.
	 */
	public Message(String kind, Object arg) {
		this.kind = kind;
		this.arg = arg;
	}
	/**
	 * Constructs a message with an argument value.
	 * @param kind a kind of this message.
	 */
	public Message(String kind, boolean b) {
		this(kind, new Boolean(b));
	}
	/**
	 * Enable a defered reply. If this feature is enabled, this message
	 * is assumed that it will be handled later. Neither a reply nor
	 * a exception does not sent to the callee unless you explicitly
	 * send a reply regardless of whatever has been returned in
	 * <tt>handleMessage</tt> method.
	 * 
	 * @param b true if the reply of this message should be defered.
	 */
	public void enableDeferedReply(boolean b) {
		throw new NoSuchMethodError();
	}
	/**
	 * Compares two Message objects. Use sameKind() method to compare
	 * a message with its string representation of the kind.
	 * @see Message#sameKind
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Message && ((Message)obj).sameKind(kind)) {
			Object arg2 = ((Message)obj).arg;

			if (arg2 == arg || (arg != null && arg.equals(arg2))) {
				return true;
			} 
		} 
		return false;
	}
	/**
	 * Gets the argument.
	 */
	public Object getArg() {
		return arg;
	}
	/**
	 * Gets the value to which specified key is mapped in this message.
	 * @param name a name of this argument.
	 * @return a value of this argument.
	 */
	public Object getArg(String name) {
		if (arg instanceof Hashtable) {
			return ((Hashtable)arg).get(name);
		} 
		return null;
	}
	/**
	 * Gets the kind of this message
	 */
	public String getKind() {
		return kind;
	}
	/**
	 * Returns a type indecating how the message has been sent.
	 * This works only for the message passed to the handleMessage method.
	 * @see Aglet#handleMessage
	 */
	public int getMessageType() {
		throw new NoSuchMethodError();
	}
	/**
	 * Gets the time in milliseconds when the message was sent.
	 */
	public long getTimeStamp() {
		return timestamp;
	}
	/**
	 * Checks if the message has same kind as the given message.
	 * @param m a message to compare
	 */
	public boolean sameKind(Message m) {
		return (m != null && kind.equals(m.kind));
	}
	/**
	 * Checks if the message has same kind as given string.
	 * @param k a string to compare
	 */
	public boolean sameKind(String k) {
		return kind.equals(k);
	}
	/**
	 * Sets a exception to this message.
	 * @exception IllegalAccessError if a reply has already been sent.
	 */
	public void sendException(Exception exp) {
		throw new NoSuchMethodError();
	}
	/**
	 * Send a reply without sepcific value.
	 * @exception IllegalAccessError if a reply has already been sent.
	 */
	public void sendReply() {
		throw new NoSuchMethodError();
	}
	/**
	 * Sends a character value as a reply.
	 */
	public void sendReply(char c) {
		sendReply(new Character(c));
	}
	/**
	 * Sends a double value as a reply.
	 */
	public void sendReply(double d) {
		sendReply(new Double(d));
	}
	/**
	 * Sends a float value as a reply.
	 */
	public void sendReply(float f) {
		sendReply(new Float(f));
	}
	/**
	 * Sends a integer value as a reply.
	 */
	public void sendReply(int i) {
		sendReply(new Integer(i));
	}
	/**
	 * Sends a long value as a reply.
	 */
	public void sendReply(long l) {
		sendReply(new Long(l));
	}
	/**
	 * Sets a reply to this message.
	 * @see FutureReply#getReply
	 * @exception IllegalAccessError if a reply has already been sent.
	 */
	public void sendReply(Object arg) {
		throw new NoSuchMethodError();
	}
	/**
	 * Sends a bolean value as a reply.
	 */
	public void sendReply(boolean b) {
		sendReply(new Boolean(b));
	}
	/**
	 * Set a byte value with an associated name.
	 * @param name a name of this argument.
	 * @param value a byte value of this argument.
	 */
	public void setArg(String name, byte value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Set a character value with an associated name.
	 * @param name a name of this argument.
	 * @param value a character value of this argument.
	 */
	public void setArg(String name, char value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Set a double value with an associated name.
	 * @param name a name of this argument.
	 * @param d a double value of this argument.
	 */
	public void setArg(String name, double value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Set a float value with an associated name.
	 * @param name a name of this argument.
	 * @param value a float value of this argument.
	 */
	public void setArg(String name, float value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Set a int value with an associated name.
	 * @param name a name of this argument.
	 * @param value an integer value of this argument.
	 */
	public void setArg(String name, int value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Sets a long value with an associated name.
	 * @param name a name of this argument.
	 * @param value a long value of this argument.
	 */
	public void setArg(String name, long value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Sets a value with an associated name.
	 * @param name a name of this argument.
	 * @param a a value of this argument.
	 */
	public void setArg(String name, Object a) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, a);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Set a byte value with an associated name.
	 * @param name a name of this argument.
	 * @param value a byte value of this argument.
	 */
	public void setArg(String name, short value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Set a boolean value with an associated name.
	 * @param name a name of this argument.
	 * @param value a boolean value of this argument.
	 */
	public void setArg(String name, boolean value) {
		if (arg instanceof Arguments) {
			((Arguments)arg).setArg(name, value);
		} else {
			throw new RuntimeException("Cannot set name-value pair");
		} 
	}
	/**
	 * Gets the string representation of the message.
	 */
	public String toString() {
		return "[kind = " + kind + ": arg = " + String.valueOf(arg) + ']';
	}
	
	/**
	 * Increases the message priority, catching a possible wrap-around over the MAX_PRIORITY.
	 * If the message priority is greater than MAX_PRIORITY, the method does nothing (i.e., the
	 * priority is keeped at MAX_PRIORITY), otherwise the message priority is incremented by one.
	 *
	 */
	public synchronized void increasePriority(){
	    if( (this.priority + 1) < MAX_PRIORITY )
		this.priority += 1;
	}
	
	/**
	 * Decreases the message priority by one until it reaches the MIN_PRIORITY.
	 *
	 */
	public synchronized void decreasePriority(){
	    if( (this.priority - 1) > MIN_PRIORITY )
		this.priority -= 1;
	}
}
