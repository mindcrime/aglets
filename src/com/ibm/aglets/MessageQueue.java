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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.Message;
import com.ibm.aglet.MessageManager;
import java.util.Hashtable;
import java.util.Stack;

/*
 * You have to explicitly synchronize
 * REMIND: should be replaced by priority-queue implementation.
 */
final class MessageQueue {
	private MessageImpl head = new MessageImpl();
	private MessageImpl tail = head;

	void append(MessageImpl append) {
		append.next = null;
		tail.next = append;
		tail = append;
	}
	// problem!
	void insert(MessageImpl msg) {
		MessageImpl tmp = head;

		while (tmp.next != null && tmp.next.priority >= msg.priority) {
			tmp = tmp.next;
		} 
		msg.next = tmp.next;
		tmp.next = msg;
	}
	void insertAtTop(MessageImpl top) {
		top.next = head.next;
		head.next = top;
	}
	void insertAtTop(MessageQueue queue) {
		if (queue.head.next != null) {
			queue.tail.next = head.next;	// isEmpty?

			// queue.head.next.next = head.next;
			head.next = queue.head.next;
		} 
	}
	MessageImpl peek() {
		return head.next;
	}
	MessageImpl pop() {
		if (head.next != null) {
			MessageImpl pop = head.next;

			// remove waiting message from queue.
			head.next = pop.next;
			pop.next = null;
			if (tail == pop) {
				tail = head;
			} 
			return pop;
		} else {
			return null;
		} 
	}
	void remove(MessageImpl msg) {
		MessageImpl tmp;

		for (tmp = head; tmp.next != null && tmp.next != msg; tmp = tmp.next);

		if (tmp.next == msg) {
			tmp.next = msg.next;
		} 
		msg.next = null;
	}
	void removeAll() {
		head.next = null;
		tail = head;
	}
	public String toString() {
		MessageImpl tmp = head.next;
		StringBuffer buff = new StringBuffer();
		int i = 1;

		while (tmp != null) {
			buff.append(String.valueOf(i++));
			buff.append(":");
			buff.append(tmp.toString());
			buff.append("\n");
			tmp = tmp.next;
		} 
		return buff.toString();
	}
}
