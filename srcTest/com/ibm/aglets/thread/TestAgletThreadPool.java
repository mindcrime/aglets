/**
 * 
 */
package com.ibm.aglets.thread;

import junit.framework.TestCase;

import com.ibm.aglet.AgletException;
import com.ibm.aglets.MessageManagerImpl;

/**
 * A test case for the aglet thread pool.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         24/ago/07
 */
public class TestAgletThreadPool extends TestCase {

    private AgletThreadPool pool = null;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
	super.setUp();
	// get a new instance of the thread pool
	this.pool = AgletThreadPool.getInstance();
    }

    /**
     * Test method for
     * {@link com.ibm.aglets.thread.AgletThreadPool#pop(com.ibm.aglet.message.MessageManager)}
     * .
     */
    public void testPop() {
	try {
	    // check if I can get at least maxPoolSize threads!
	    int i = 0;
	    for (i = 0; i < this.pool.getMaxPoolSize(); i++) {
		Thread t1 = this.pool.pop(new MessageManagerImpl(null));
		assertNotNull(t1); // never be null
		assertTrue((t1 instanceof AgletThread)); // should be an aglet
		// thread
	    }

	} catch (AgletException e) {
	    throw new RuntimeException(e);
	}

    }

    /**
     * Test method for
     * {@link com.ibm.aglets.thread.AgletThreadPool#push(com.ibm.aglets.thread.AgletThread)}
     * .
     */
    public void testPush() {
	try {
	    AgletThread t = new AgletThread(this.pool.getThreadGroup());
	    this.pool.push(t);
	    assertTrue(this.pool.contains(t)); // the pool should contain the
	    // thread now
	} catch (AgletException e) {
	    throw new RuntimeException(e);
	}

	try {
	    AgletThread t2 = new AgletThread(new ThreadGroup("Test_thread_group"));
	    this.pool.push(t2); // should now work
	} catch (AgletException e) {
	}
    }

}
