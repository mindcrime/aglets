package com.ibm.aglets.thread;

import java.util.Stack;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.MessageManager;
import com.ibm.aglets.MessageManagerImpl;

/**
 * A thread pool that provides pooling base mechanism for aglet threads. The
 * idea behind this pool is quite simple: once a new thread is required the pool
 * must be able to provide it. Provide a thread means either: a) provide an
 * already created and idle thread; b) create a new thread if no one idle thread
 * is available. Once a thread has finished its work it must be inserted again
 * in the pool, thus it can be used in further thread needed conditions.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 */
public class AgletThreadPool {
	/**
	 * Creates a new instance of the pool and returns it to the caller.
	 * 
	 * @return the instance of the thread pool for the running platform.
	 */
	public synchronized static AgletThreadPool getInstance() {
		// check if the pool is already ready
		if (mySelf != null)
			return mySelf;
		else {
			mySelf = new AgletThreadPool();
			return mySelf;
		}
	}

	// the logger for this pool
	private final AgletsLogger logger = AgletsLogger.getLogger(AgletThreadPool.class.getName());

	/**
	 * The minimum size of the pool, that is the minimum number thread this pool
	 * must create on startup.
	 */
	private int minPoolSize = 10;

	/**
	 * The max size of the pool, that is the max number of thread this pool can
	 * contain. After such number has been reached, the pool must suspend each
	 * request of a new thread until one becomes idle (and thus available)
	 * again.
	 */
	private int maxPoolSize = 100;

	/**
	 * A self reference to this object. This class is supposed to be a
	 * singleton, thus only one pool can exists in the whole system.
	 */
	private static AgletThreadPool mySelf = null;

	/**
	 * The pool will keep the threads into a stack container.
	 */
	private Stack<AgletThread> threads = null;

	/**
	 * A thread group that contains all the threads this pool will create.
	 */
	private final ThreadGroup threadGroup = new ThreadGroup("AgletThreadGroup");

	/**
	 * A stack for a specific type of threads: the delivery message threads.
	 */
	private final Stack<DeliveryMessageThread> deliveryMessageThreads = null;

	/**
	 * A counter that indicates how many threads this pool has created until
	 * now. It is useful for checking that the pool has not gone over the
	 * maxPoolSize value.
	 */
	private int createdThread = 0;

	/**
	 * A counter that provides an information about the number of busy threads
	 * in the pool.
	 */
	private int busyThreads = 0;

	/**
	 * Initializes the structures of the pool and creates the first threads (the
	 * minimum available threads) that will be used.
	 * 
	 */
	private AgletThreadPool() {
		super();
		threads = new Stack();

		// create the min threads
		logger.info("AgletThreadPool starting with " + minPoolSize
				+ " min threads");
		for (int i = 0; i < minPoolSize; i++)
			createNewThread();

		logger.info("AgletThreadPool ready");
	}

	/**
	 * Checks if a specific thread is currently owned by the thread pool.
	 * 
	 * @param thread
	 *            the thread to check
	 * @return true if the thread is currently handled by this pool
	 */
	public synchronized final boolean contains(final AgletThread thread) {
		if ((thread == null) || (threads.size() == 0))
			return false;
		else
			return threads.contains(thread);
	}

	/**
	 * An utility method that creates a new thread, sets the group of the thread
	 * and pushes it into the stack, thus the new thread is available to the
	 * pool. Moreover, this method increases the createdThread value, thus to
	 * store the number of threads created by this pool.
	 * 
	 */
	protected void createNewThread() {
		logger.debug("Creating a new thread (thread number "
				+ createdThread + ")");
		final AgletThread thread = new AgletThread(threadGroup);
		thread.setName("PooledAgletThread num." + createdThread);
		thread.setDaemon(true); // all the pooled thread are daemons, they are
		// utility threads
		threads.push(thread);
		createdThread++;
	}

	/**
	 * Provides the information about how many threads are busy at the moment in
	 * the pool.
	 * 
	 * @return the number of busy threads
	 */
	public synchronized final int getBusyThreadsNumber() {
		return busyThreads;
	}

	/**
	 * Returns the number of created threads of this pool.
	 * 
	 * @return the number of threads that this pool has created up to now
	 */
	public synchronized int getCreatedThread() {
		return createdThread;
	}

	/**
	 * Gets back the maxPoolSize.
	 * 
	 * @return the maxPoolSize
	 */
	public synchronized int getMaxPoolSize() {
		return maxPoolSize;
	}

	/**
	 * Gets back the minPoolSize.
	 * 
	 * @return the minPoolSize
	 */
	public synchronized int getMinPoolSize() {
		return minPoolSize;
	}

	/**
	 * Gets back the threadGroup.
	 * 
	 * @return the threadGroup
	 */
	public synchronized final ThreadGroup getThreadGroup() {
		return threadGroup;
	}

	/**
	 * Provides a new AgletThread to the caller. The AgletThread will be used
	 * combined with the specified message manager (that must be not-null). This
	 * method checks the pool to see if a good thread is contained in it, and
	 * thus such thread is returned. Otherwise, if possible (i.e., the pool has
	 * not yet reached the max size) a new thread is created. In the case there
	 * are no available threads and no more thread can be created (due to the
	 * reach of the max pool size), the caller thread is suspended waiting for a
	 * new thread to be available.
	 * 
	 * @param messageManager
	 *            the message manager that will be used for the new thread
	 * @return the AgletThread to use to manage messages.
	 * @throws AgletException
	 *             if the specified message manager is null, or a problem occurs
	 *             while waiting for a new thread to be available on the pool.
	 */
	public synchronized AgletThread pop(final MessageManager messageManager)
	throws AgletException {
		// first of all check if the message manager is valid
		if ((messageManager == null)
				|| (!(messageManager instanceof MessageManagerImpl)))
			throw new AgletException("Cannot get a thread for a null message manager");

		// the thread to return...
		AgletThread thread = null;

		// check the size of the stack/pool. If it is zero there are not threads
		// available
		// thus a new thread should be created, but this only if the number of
		// created
		// threads does not exceed the maxPoolSize, otherwise it is required to
		// wait...
		if ((threads.size() == 0)
				&& (createdThread < maxPoolSize)) {
			// create a new thread
			createNewThread();
		} else
			while ((threads.size() == 0)
					&& (createdThread > maxPoolSize)) {
				// I cannot create no more threads, the max size of the pool has
				// already been
				// reached, thus the caller must wait until a new thread is
				// available
				logger.debug("Waiting for a thread to re-enter the pool and be available...");
				try {
					this.wait();
				} catch (final InterruptedException ex) {
					logger.error("Exception caught while waiting for a thread to be available in the pool!", ex);
					throw new AgletException();
				}

			} // end of while

		// now if I'm here there must be at least an available thread in the
		// pool, pop
		// it and return it to the caller.
		logger.debug("Popping a thread from the pool");
		thread = threads.pop();
		busyThreads++;
		logger.debug("Returning the thread " + thread);

		// sets the message manager for this thread
		thread.setMessageManager((MessageManagerImpl) messageManager);

		// all done!
		return thread;

	}

	/**
	 * Inserts (or re-inserts) a thread into the pool, making it available for
	 * other requests. Please note that if the pool already contains a reference
	 * to such thread or a number of threads greater than the maxPoolSize have
	 * been pushed, the pushing is aborted. An exception is thrown if it is
	 * forced to push a thread that does not belong to the thread group of the
	 * pooled threads.
	 * 
	 * @param thread
	 *            the thread to insert into the pool.
	 * @exception if
	 *                the thread group is different from the one the pool has
	 *                set at the thread creation.
	 */
	public synchronized void push(final AgletThread thread) throws AgletException {
		// check if the thread is valid, or if it is already contained in the
		// pool
		// or if too much threads have been already be pushed in the pool!
		if ((thread == null) || contains(thread)
				|| (threads.size() > maxPoolSize))
			return;

		// check if the thread group is strictly the same of the pool thread
		// group
		if (threadGroup.equals(thread.getThreadGroup()) == false) {
			logger.error("Trying to push a thread with a group different from the pool thread group!");
			throw new AgletException("Trying to push a thread with a group different from the pool thread group!");
		}

		// simply push the thread to the stack
		logger.debug("Pushing back the thread in the pool");
		thread.setMessageManager(null);
		threads.push(thread);
		busyThreads--;
	}

	/**
	 * Reinsert the thread in the stack of the delivery threads.
	 * 
	 * @param deliveryMessageThread
	 *            the thread to insert
	 */
	public synchronized void pushDeliveryMessageThread(
	                                                   final DeliveryMessageThread deliveryMessageThread) {
		// check arguments
		if ((deliveryMessageThread == null)
				|| deliveryMessageThreads.contains(deliveryMessageThread))
			return;

		deliveryMessageThreads.push(deliveryMessageThread);

	}

	/**
	 * Sets the maxPoolSize value.
	 * 
	 * @param maxPoolSize
	 *            the maxPoolSize to set
	 */
	public synchronized void setMaxPoolSize(final int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	/**
	 * Sets the minPoolSize value.
	 * 
	 * @param minPoolSize
	 *            the minPoolSize to set
	 */
	public synchronized void setMinPoolSize(final int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

}
