package com.ibm.aglets.thread;


import com.ibm.aglets.*;

import java.util.*;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.MessageManager;
import com.ibm.aglet.message.*;

/**
 * This class represents a pool for a set of aglet threads. Agents
 * and message managers can pop and push threads from the pool
 * in order to handle execution threads. Using a singleton pool
 * like this avoid the creation of a lot of threads within the
 * platform.
 * @author Luca Ferrari cat4hire@users.sourceforge.net
 * 28-mag-2005
 * @version 1.0
 */
public class AgletThreadPool {

	/**
	 * A self refernce to myself, used to create
	 * a singleton.
	 */
	private static AgletThreadPool myself = null;
		
	/**
	 * The real pool that will contain the threads.
	 */
	private Stack pool = null;
	
	
	/**
	 * A pool of special threads, used to deliver a single message to an agent.
	 */
	private Stack deliveryPool = null;
	
	/**
	 * The max number of thread this pool must handle.
	 */
	private int size;
	
	/**
	 * The number of thread handled currenlty.
	 */
	private int handled;
	
	
	/**
	 * Base constructor: it creates the pool-stack and initializes
	 * the size of the pool.
	 * @param size the max number of threads this pool can create
	 */
	protected AgletThreadPool(int size){
		super();
		this.size = size;
		AgletThreadPool.myself = this;
		this.pool = new Stack();
		this.deliveryPool = new Stack();
		this.handled = 0;
	}
	
	
	
	/**
	 * Obtains the current instance of the pool. If no instance have been
	 * created yet, creates an instance for 10 threads.
	 * @return the reference to the pool
	 */
	public static AgletThreadPool getInstance(){
		if( AgletThreadPool.myself != null ){
			return AgletThreadPool.myself;
		}
		else{
			synchronized (AgletThreadPool.class){
				AgletThreadPool.myself = new AgletThreadPool(10);
				return AgletThreadPool.myself;
				
			}
		}
			
	}
	
	
	
	/**
	 * A service to get an AgletThread. If possible, a thread is exctracted from the
	 * queue, otherwise a new thread is created. If the maximum count of the pool is reached,
	 * the process is suspended (wait) until a new thread becomes available
	 * in the pool.
	 * <B>Important</B>: in a shared thread environment like this, it is important
	 * to set the right message manager for the thread, otherwise messages
	 * will be delivered to the wrong aglet.
	 * @param group the group of the (in case) new thread
	 * @param manager the message manager that must be associated to the thread
	 * @return
	 */
	protected synchronized final AgletThread getThread(ThreadGroup group, MessageManager manager){
		// as first I need to check if there's an available
		// thread in the pool
		
		AgletThread t = null;
		
		
		if( this.pool.size() >= 1 ){
			// I've got at least one ready thread, get it
			t = (AgletThread) this.getWaitingThread();
		}
		else{
			// if here there's no thread in the pool, I should
			// create a new thread but it depends on the
			// number of threads I've already created.
			
			// If the number of created thread has reached the
			// maximun count, then stop the creation and wait
			// until a new thread becomes available
			if( this.handled == this.size ){
				try{
					while( this.pool.size() == 0){
						this.wait();
					}
					
					// if here at least one thread is in the pool
					t = (AgletThread)this.getWaitingThread();
															
				}catch(InterruptedException ex){
					System.err.println("Cannot wait for another thread in the pool");
					ex.printStackTrace();
					return null;
				}
			}
			else{
				// i can create a new thread
				t = new AgletThread(group, manager);
				t.setMessageManager(manager);
				// should I change the group of the thread????
				this.handled++;
				
			}
		}
		
		
		// if here I've got the thread
		t.setMessageManager(manager);
		return t;
	}
	
	
	/**
	 * A service to extract a waiting thread from the pool. It is important
	 * to note that once placed in the pool the thread could be not yet sleeping,
	 * that means it is going to call the wait method, but it hasn't done yet.
	 * For this reason this method searches in the pool for a thread that
	 * is effectively waiting (i.e., it has done the wait call). The loop is
	 * performed twice on the pool, to avoid infinite loop.
	 * @return the first waiting thread in the pool
	 */
	protected final AgletThread getWaitingThread(){
		AgletThread t = null;
		boolean find = false;
		
		if( this.pool != null && this.pool.size() >= 1){
			int looper = this.pool.size()*2;
			
			while( find == false && looper >= 0){
				t = (AgletThread) this.pool.pop();
				looper--;
				
				if( t.isWaiting() == false){
					// re-insert the thread in the pool
					this.pool.push(t);
				}
				else
					find = true;
			}
		}
		
		return t;
	}
	
	
	/**
	 * A method to place a thread in the pool, that means releasing
	 * a thread thus other agents/message managers can use it. The thread
	 * is inserted in the pool only if it does not already is in the pool
	 * and if the max number of thread managed from this pool has not been reached
	 * yet (to avoid continue pushing of new threads). In the case
	 * a process is waiting for a thread (wiat), it is notified.
	 * @param thread the thread to release
	 * @return true if the thread is placed in the pool, false otherwise.
	 */
	public synchronized boolean push(AgletThread thread){
		// check the argument
		if( thread == null ){
			return false;
		}
		
		// check if the thread is already contained in the pool
		// or if the maximum size of the pool has been reached
		// (i.e., avoid continue pushing of new threads!)
		if( this.pool.contains(thread) || this.pool.size() == this.size ){
			return false;
		}
		
		// now insert the thread in the pool
		this.pool.push(thread);
		
		// notify sleeping threads
		this.notifyAll();
		
		
		return true;
	}
	
	
	/**
	 * A service to get a thread from the pool.
	 * @param manager the message manager to use with the thread
	 * @return the thread obtained from the pool (could be a new thread).
	 */
	public AgletThread pop(MessageManager manager){
		AgletThread t = this.getThread(Thread.currentThread().getThreadGroup(), manager);
		//t.notifyAll();
		return t;
	}

	
	protected void dumpPool(){
		for(int i=0; i<this.pool.size(); i++){
			System.out.println("Thread in pool: "+this.pool.elementAt(i));
		}
	}
	

	/**
	 * Returns a delivery message thread. Please note that this code does not keep into account of the size
	 * of the thread pool, that means, each time a delivery thread is required and no one is available, it
	 * will be created. This is due to the fact that the number of delivery thread created should be less
	 * than the number of thread used to execute agents.
	 * @param proxy the proxy for the delivery thread.
	 * @param message the message to deliver.
	 * @return
	 */
	public DeliveryMessageThread getDeliveryMessageThread(AgletProxy proxy, Message message){
		// check parameters
		if( proxy == null || message == null )
			return null;
		
		// synchornize
		synchronized(this){
			if( this.deliveryPool.size() == 0 ){
				// no delivery thread yet created, create a new one				
				DeliveryMessageThread dmt = new DeliveryMessageThread(proxy, message);
				return dmt;
			}
			else{
				// we have a thread in the pool, get it
				DeliveryMessageThread dmt = (DeliveryMessageThread) this.deliveryPool.pop();
				return dmt;
			}
		}
	}
	
	
	/**
	 * Re-insert a thread in the pool of the delivery thread.
	 * @param thread the thread to re-insert.
	 */
	public void pushDeliveryMessageThread(DeliveryMessageThread thread){
		if( thread != null && this.deliveryPool != null && this.deliveryPool.contains(thread) == false )
			synchronized(this){
				this.deliveryPool.push(thread);
			}
		else
			return;
	}
	
	
}
