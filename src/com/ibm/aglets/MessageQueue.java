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


import com.ibm.aglet.message.Message;

import java.util.LinkedList;
import java.util.*;

/*
 * You have to explicitly synchronize
 * REMIND: should be replaced by priority-queue implementation.
 */


/**
 * The message queue class represents a queue for messages. In this implementationm
 * it stores messages by mean of a linked list.
 * @author Luca Ferrari (refactoring) cat4hire@users.sourceforge.net
 *
 */
public class MessageQueue {

	/**
	 * The linked listr used to keep all messages.
	 */
	private LinkedList messages = null;


	/**
	 * Indicates if the queue is active or not. A deactiated queue
	 * does not return any message thru pop.
	 */
	private boolean enabled = true;


	/**
	 * Default constructor. Initializes the linked list to be used.
	 *
	 */
	public MessageQueue(){
		// create a new linked list
		this.messages = new LinkedList();
	}



	/**
	 * Insert the specified message to the tail of the list.
	 * @param msg the message to be appended
	 */
	public synchronized void append(Message msg){
		// insert the message in the list only if it's not null
		if( msg != null)
			this.messages.addLast( msg );
	}

	public synchronized void insert(Message msg){
		// check if the message is null
		if( msg == null )
			return;

		// I need to insert the message at the right place
		// depending by its priority, thus I need to find the
		// first index free.
		if( this.messages.size() != 0)
			this.insertAndSortByPriority( msg );
		else
			this.messages.add(msg);

		// MODIFICA: qui se il messaggio e' rolex ed e' "bloccante"
		// la coda va disabilitata!
		// if(msg instanceof RolexMessage && bloccante) this.setEnabled(false);

	}


	/**
	 * A method that performs the insertion of a message depending
	 * on its priority. Override this method in subclasses to
	 * change the behaviour of the insertion mechanism.
	 * @param msg the message to insert (suppoosed not null)
	 * @return the index of the insertion.
	 */
	protected int insertAndSortByPriority(Message msg){
		ListIterator iterator = this.messages.listIterator();
		Message tmp = null;
		int index = 0;
		boolean inserted = false;
		List tail = null;
		LinkedList tempList = new LinkedList();


		for(int i=0; i< this.messages.size(); i++){
			Message cur = (Message)this.messages.get(i);

			// check the priority
			if( msg.getPriority() > cur.getPriority() && inserted == false){
				tempList.add(tempList.size(),msg);
				inserted=true;
			}

			// insert the current element of the list
			tempList.add(tempList.size(),cur);
		}



		// when here I'm not sure the message has been added to the
		// list, check it
		if( ! tempList.contains(msg)){
			index = tempList.size();
			tempList.addLast(msg);
		}

		// change the list
		this.messages = tempList;


		return index;

	}


	/**
	 * Insert the message at the head of the queue, without regard
	 * to the priority of other messages.
	 * @param msg the message to be appended
	 */
	public synchronized void insertAtTop(Message msg){
		// check params
		if( msg == null)
			return;

		this.messages.addFirst( msg );
	}

	/**
	 * Appends a whole queue at the top of the current one.
	 * @param queue the queue to append
	 */
	public synchronized void insertAtTop(MessageQueue queue){
		// check params
		if( queue == null )
			return;

		// insert the elements of the queue starting from the last
		// to the first
		for(int i=queue.messages.size()-1; i>0; i--){
			Message tmp = (Message) queue.messages.get(i);
			this.insertAtTop( tmp );
		}
	}


	/**
	 * Get the first message in the queue. This method does not
	 * remove the first element from the queue.
	 * @return the first message of the queue
	 */
	public Message peek(){
		if (enabled == false) {
			return null;
		} else
			return (Message) this.messages.getFirst();
	}

	/**
	 * Returns and remove from the queue the first message, if the queue is
	 * enebled. Otherwise returns null.
	 * 
	 * @return the first message in the queue
	 */
	public synchronized Message pop(){
		if (enabled == false) {
			return null;
		} else
			return (Message) this.messages.removeFirst();
	}

	/**
	 * Removes a message from the queue.
	 * 
	 * @param msg
	 *            the message to remove
	 * @return true if the message has been removed (i.e., it was in the queue),
	 *         false otherwise
	 */
	public synchronized boolean remove(Message msg){
		return  this.messages.remove( msg );
	}


	/**
	 * Removes all messages from the queue.
	 *
	 */
	public synchronized void removeAll(){
		this.messages = null;
		this.messages = new LinkedList();
	}


	/**
	 * Indicates if the queue contains or not messages.
	 * @return true if the queue is empty
	 */
	public boolean isEmpty(){
		return this.messages.isEmpty();
	}

	/**
	 * Returns an iterator for the current queue.
	 * @return the iterator for the queue
	 */
	public Iterator iterator(){
		if( this.messages != null){
			return this.messages.iterator();
		}
		else
			return null;
	}



	public String toString(){
		// suppose 10 chars (at least) for each message in the queue
		StringBuffer buffer = new StringBuffer( this.messages.size() * 10);

		for(int i=0; i< this.messages.size(); i++){
			Message tmp = (Message) this.messages.get(i);
			buffer.append(i+") "+tmp.getKind()+" - "+tmp.getPriority()+"\n");
		}

		return new String(buffer);
	}





	/**
	 * Activates/deactivates the queue.
	 * @param enabled true if the queue must return something from the pop
	 * method
	 */
	protected  void setEnabled ( boolean enabled ){
		this.enabled = enabled;
	}

        /**
         * Returns TRUE if the queue is enabled.
         */
        public boolean isEnabled () {
            return enabled;
        }
}
