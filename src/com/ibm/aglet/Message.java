package com.ibm.aglet;

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
import com.ibm.aglet.util.Arguments;

/**
 * The <tt>Message</tt> class is a object that holds its kind
 * and arguments passed to the receiver. In handleMessage() method
 * on Aglet class, the reply to the request can be set if any.
 * 
 * @version     1.70    $Date: 2007/07/25 23:33:04 $
 * @author	Mitsuru Oshima
 */
public class Message implements java.io.Serializable {

	static final long serialVersionUID = 5467548823007286376L;

	/**
	 * The types of message that indecates how the message was sent.
	 * @see #getMessageType()
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
	 * @param value a double value for this argument.
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
}
