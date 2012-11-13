package com.ibm.aglets;

/*
 * @(#)MessageQueue.java
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.aglet.message.Message;

/**
 * This class represents a message queue. A message queue is a queue where
 * messages are placed before they can be processed. The idea is that a message
 * manager creates its own message queue and uses it to store message before it
 * has activate a thread to process it. The use of a message queue allows the
 * uncoupling of agent threads. In fact when an agent sends a message to another
 * agent, the sending thread thru several method calls inserts the message in
 * the message queue, and thus the sender thread returns. Thus the sender thread
 * is involved until the message has been inserted in the message queue. Once
 * this is done, it is the addressee thread in charge of processing the message
 * itself, and this is done by the message manager that gets a thread and pops a
 * message from the message queue.
 * 
 * Please note that this class can be used as a queue for every implementation
 * of a message, since it has been generalized. Thus it can be created to manage
 * Message or MessageImpl objects.
 * 
 * The old implementation of message queue was based on a manually set linked
 * list between message implementations, while this uses a standard linked list.
 * Moreover this version is synchronized and thus thread safe. Finally it takes
 * care of priority, since it inserts the messages depending on their priority,
 * thus the highest priority are always at the head of the list and will be the
 * first being popped.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         09/ago/07
 */
public class MessageQueue<MSG extends Message> {

	/**
	 * The messages will be stored into a linked list.
	 */
	private final List<MSG> messages = new LinkedList<MSG>();

	/**
	 * Appends a message, that is places the message at the end of the queue.
	 * 
	 * @param msg
	 *            the message to append
	 */
	public synchronized void append(final MSG msg) {
		// check params
		if (msg == null)
			return;

		// append the message at the tail
		this.messages.add(msg);
	}

	/**
	 * Inserts a new message in the queue. The alghoritm is the following: each
	 * message is extracted from the queue and analyzed; at the first message
	 * with a priority less than the one we want to insert, the message is
	 * inserted. If no one message is found, than the message is placed at the
	 * tail of the queue.
	 * 
	 * @param msg
	 *            the message to insert
	 */
	public synchronized void insert(final MSG msg) {
		// check params
		if (msg == null)
			return;

		final Iterator iter = this.messages.iterator();

		while ((iter != null) && iter.hasNext()) {
			final MSG currentMessage = (MSG) iter.next();
			if (currentMessage.getPriority() < msg.getPriority()) {
				final int index = this.messages.indexOf(currentMessage);
				this.messages.add(index, msg);
				return;
			}
		}

		// if here the message must be placed at the tail of the queue
		this.messages.add(msg);
	}

	public synchronized void insertAtTop(final MessageQueue<MSG> queue) {
		if ((queue == null) || queue.isEmpty())
			return;
		else {
			this.messages.addAll(0, queue.messages);
		}
	}

	/**
	 * Inserts the message at the top of the queue. Please note that this method
	 * will overtake the priority mechanism, thus it is possible to insert at
	 * the top a message with a low priority.
	 * 
	 * @param top
	 *            the message to place at the head of the list
	 */
	public synchronized void insertAtTop(final MSG top) {
		if (top == null)
			return;

		// place the message at the head of the queue
		this.messages.add(0, top);
	}

	public boolean isEmpty() {
		return this.messages.isEmpty();
	}

	/**
	 * Gets (but don't remove) the message at the top of the queue.
	 * 
	 * @return the message at the top.
	 */
	public synchronized MSG peek() {
		if (!this.messages.isEmpty())
			return this.messages.get(0);
		else
			return null;
	}

	/**
	 * Extract (i.e., remove) the message at the top of the queue.
	 * 
	 * @return the message at the top or null
	 */
	public synchronized MSG pop() {
		if (this.messages.isEmpty())
			return null;
		else
			return this.messages.remove(0);
	}

	/**
	 * Removes a message from the queue.
	 * 
	 * @param msg
	 *            the message to remove
	 * @return true if the message has been removed, false otherwise
	 */
	public synchronized boolean remove(final MSG msg) {
		if (this.messages.contains(msg)) {
			this.messages.remove(msg);
			return true;
		} else
			return false;
	}

	/**
	 * Removes all the messages from the queue, that is after this the queue
	 * will be empty.
	 * 
	 */
	public synchronized void removeAll() {
		if (!this.messages.isEmpty())
			this.messages.clear();
	}

	public int size() {
		return this.messages.size();
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer(50 * this.messages.size());

		buffer.append("The message queue contains " + this.messages.size()
				+ " messages");
		for (int i = 0; i < this.messages.size(); i++) {
			buffer.append("\n");
			for (int j = i; j > 0; j--)
				// make a few indentation spaces
				buffer.append(" ");

			buffer.append("message " + i + ")" + this.messages.get(i));
		}

		return buffer.toString();
	}
}
