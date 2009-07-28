package com.ibm.aglets.thread;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;

/**
 * This class is used as a thread for the delivery of a single message.
 * Don't use this class to deliver specific messages to the agent, when you can use the standard pool and
 * proxy mechanism. Please note that this class does not represent the final thread that will handle the delivery
 * of the message, but just a glue thread among the sender and the receiver threads.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0
 * 6/10/2005
 *
 */
public class DeliveryMessageThread extends Thread {
	
	
	/**
	 * A counter of the number of delivery threads.
	 */
	protected static int counter = 1;
	
	/**
	 * The proxy to which send a message.
	 */
	private AgletProxy proxy = null;
	
	/**
	 * The message to deliver.
	 */
	private Message message = null;
	
	/**
	 * Indicates if this thread has been started or not.
	 */
	private boolean started = false;
	
	
	/**
	 * Constructs the thread. It does not start the thread! 
	 * @param p the proxy of the agent to use for this thread.
	 * @param message the message to delivery thru the proxy.
	 */
	public DeliveryMessageThread(AgletProxy p, Message msg){
		super("Delivery Message Thread "+DeliveryMessageThread.counter );
		DeliveryMessageThread.counter++;
		this.proxy = p;
		this.message = msg;
	}
	
	
	public void run(){
		// check if the thread has started, to avoid calling this method directly
		if( ! started )
			return;
		
		
		// deliver the current message to the proxy
		while( started ){
			try{
				// deliver the message if any
				if( this.proxy != null && this.message != null )
					this.proxy.sendMessage( this.message );
				
				// reset the proxy and the message thus to use this thread in the future
				this.proxy   = null;
				this.message = null;
				
				// now reinsert myself in the pool and wait
				synchronized(this){
					AgletThreadPool.getInstance().pushDeliveryMessageThread(this);
					this.wait();
				}
				
			}catch(Exception e){
				System.err.println("Exception in DeliveryMessageThread!");
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Deliver a specific message to the specific agent.
	 * @param p the proxy of the agent to deliver to
	 * @param m the message to deliver.
	 */
	public final synchronized void deliverMessage(AgletProxy p, Message m){
		this.message = m;
		this.proxy   = p;
		this.deliverMessage();
	}
	
	/**
	 * Deliver the current message.
	 */
	public final synchronized void deliverMessage(){
		if( ! started ){
			this.started = true;
			this.start();
		}
		notifyAll();
	}
}
