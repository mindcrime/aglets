package com.ibm.aglet;

/*
 * @(#)AgletProxy.java
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

// # import com.ibm.aglets.security.Allowance;

import java.net.URL;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

/**
 * AgletProxy interface is a placeholder for aglets. The purpose of this
 * interface is to provide a mechanism to control and limit direct access to
 * aglets.
 * 
 * @version     1.50    $Date: 2001/07/28 06:33:57 $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */
public interface AgletProxy {

	/**
	 * Activates the aglet.
	 * This is a forced activation of a deactivated/resumed aglet.
	 * @exception IOException if the activation failed.
	 * @exception AgletException if the aglet is not valid.
	 */
	abstract public void activate() throws IOException, AgletException;
	// #     /**
	// #      * Gets the allowance: availability of the aglet's resources.
	// #      *
	// #      * @return the Allowance object
	// #      * @exception InvalidAgletException if the aglet is not valid.
	// #      * @see com.ibm.aglet.Allowance
	// #      */
	// #     abstract public Allowance getAllowance() throws InvalidAgletException;

	/*
	 * Aglet Control APIs.
	 */

	/**
	 * Clones the aglet and its proxy.
	 * Note that the cloned aglet will get activated.
	 * If you like to get cloned aglet which is not activated, throw
	 * ThreadDeath exception in the onClone method.
	 * 
	 * @return  the new aglet proxy what holds cloned aglet.
	 * @exception CloneNotSupportedException if the cloning fails.
	 * @exception InvalidAgletException if the aglet is invalid.
	 */
	abstract public Object clone() throws CloneNotSupportedException;
	/**
	 * Deactivates the aglet. The system may store the aglet in the spool
	 * (disk or memory depending on the server). The aglet will be re-activated
	 * later (at the given time or manually).
	 * @param duration duration of the aglet deactivating in milliseconds.
	 * If this is 0, it will be activeted at the next startup time.
	 * @exception InvalidAgletException if the aglet is not valid.
	 * @exception IllegalArgumentException if the minutes parameter is negative.
	 */
	abstract public void deactivate(long duration) 
			throws IOException, InvalidAgletException;
	/**
	 * Delegates a message to the aglet. The message needs to be
	 * a message passed as an argument to <tt>Aglet#handleMessage</tt>.
	 * The delegated message is sent to the receiver and
	 * will be handled in the <tt>Aglet#handleMessage(Message)</tt> method.
	 * 
	 * @param msg a message to delegate
	 * @exception InvalidAgletException if the aglet proxy is not valid.
	 * @see Aglet#handleMessage
	 */
	abstract public void delegateMessage(Message msg) 
			throws InvalidAgletException;
	/**
	 * Dispatches the aglet to the location specified by the ticket
	 * as the argument.
	 * @param address the address of the destination context.
	 * @return the AgletProxy of the dispatched (remote) aglet.
	 * @exception ServerNotFoundException if the server
	 * @exception UnknownHostException if the host given in the URL doesn't
	 * exist.
	 * @exception MalformedURLException if the given url dosn't specify the
	 * host.
	 * @exception RequestRefusedException if the remote server refused the
	 * dispatch request.
	 * @exception InvalidAgletException if the aglet is not valid.
	 */
	abstract public AgletProxy dispatch(Ticket ticket) 
			throws IOException, AgletException;
	/**
	 * Dispatches the aglet to the location specified by the argument address.
	 * @param address the address of the destination context.
	 * @return the AgletProxy of the dispatched (remote) aglet.
	 * @exception ServerNotFoundException if the server
	 * @exception UnknownHostException if the host given in the URL doesn't
	 * exist.
	 * @exception MalformedURLException if the given url dosn't specify the
	 * host.
	 * @exception RequestRefusedException if the remote server refused the
	 * dispatch request.
	 * @exception InvalidAgletException if the aglet is not valid.
	 */
	abstract public AgletProxy dispatch(URL address) 
			throws IOException, AgletException;
	/**
	 * Dispatches the aglet to the location specified by the argument address.
	 * @param address the address of the destination context.
	 * @return the AgletProxy of the dispatched (remote) aglet.
	 * @exception ServerNotFoundException if the server
	 * @exception UnknownHostException if the host given in the URL doesn't
	 * exist.
	 * @exception MalformedURLException if the given url dosn't specify the
	 * host.
	 * @exception RequestRefusedException if the remote server refused the
	 * dispatch request.
	 * @exception InvalidAgletException if the aglet is not valid.
	 * abstract public AgletProxy dispatch(String address) throws IOException, AgletException;
	 */

	/**
	 * Disposes the aglet.
	 * @exception InvalidAgletException if the aglet is invalid.
	 */
	abstract public void dispose() throws InvalidAgletException;
	/**
	 * Gets the current address of the target aglet.
	 * 
	 * @reutrn the address
	 * @return InvalidAgletException if the aglt is not valid.
	 */
	abstract public String getAddress() throws InvalidAgletException;
	/**
	 * Gets the aglet that the proxy manages.
	 * @return the aglet
	 * @exception InvalidAgletException if the aglet is not valid.
	 * @exception SecurityException if you are not allowed to access the aglet.
	 */
	abstract public Aglet getAglet() throws InvalidAgletException;
	/**
	 * Gets the aglet's class name.
	 * @return the class name.
	 * @exception InvalidAgletException if the aglet is not valid.
	 */
	abstract public String getAgletClassName() throws InvalidAgletException;
	/**
	 * Gets the aglet's id.
	 * @return the aglet's id
	 * @exception InvalidAgletException if the aglet is not valid.
	 */
	abstract public AgletID getAgletID() throws InvalidAgletException;
	/**
	 * Gets the AgletInfo object of the aglet.
	 * 
	 * @return the AgletInfo object
	 * @exception InvalidAgletException if the aglet is not valid.
	 * @see com.ibm.aglet.AgletInfo
	 */
	abstract public AgletInfo getAgletInfo() throws InvalidAgletException;
	/**
	 * Checks if the aglet is active or deactivated.
	 * @return true if the aglet is active.
	 * @exception InvalidAgletException if the aglet is not valid.
	 */
	abstract public boolean isActive() throws InvalidAgletException;
	/**
	 * Checks if the aglet proxy is referencing a remote aglet.
	 * @return true if the aglet resides at the remote site.
	 */
	abstract public boolean isRemote();
	/**
	 * Checks if the aglet is in the state give by type.
	 * @param type an integer value specifying the aglet's state
	 * @return true if the aglet is in the same state as the type
	 * give as an argument.
	 * @see Aglet#ACTIVE
	 * @see Aglet#INACTIVE
	 */
	abstract public boolean isState(int type);
	/**
	 * Checks if the aglet proxy is invalid or not.
	 * The aglet proxy become invalid in the following way.
	 * <ol>
	 * <li>The aglet is disposed.
	 * <li>The aglet is dispatched.
	 * </ol>
	 * If the aglet is deactivated, it become <tt>INACTIVE<tt>.
	 * @return true if the aglet proxy is valid. false if not.
	 */
	abstract public boolean isValid();
	/**
	 * Sends a message in asynchronous way.
	 * @param msg a message to send.
	 * @return a future object that will give you the reply of the message.
	 * @exception InvalidAgletException if the aglet is not valid any longer.
	 */
	abstract public FutureReply sendAsyncMessage(Message msg) 
			throws InvalidAgletException;
	/**
	 * Sends a future message to the aglet. The invocation will
	 * 
	 * @param msg a message to send.
	 * @return a future object that will give you the reply of the message.
	 * @exception InvalidAgletException if the aglet is not valid any longer.
	 */
	abstract public FutureReply sendFutureMessage(Message msg) 
			throws InvalidAgletException;
	/**
	 * Sends a message in synchronous way. This waits for finishing the message
	 * handing.
	 * @param msg a message to send.
	 * @return the result object if any. null if not.
	 * @exception InvalidAgletException if the aglet is not valid any longer.
	 * @exception NotHandledException if the aglet didn't handle the
	 * the message.
	 * @exception MessageException a exception
	 * which the handleMessage method raised.
	 */
	abstract public Object sendMessage(Message msg) 
			throws InvalidAgletException, NotHandledException, 
				   MessageException;
	/**
	 * Sends a oneway message to the aglet. No acknowledgement will be
	 * sent back to the sender.
	 * @param msg a message to send.
	 * @exception InvalidAgletException if the aglet is not valid any longer.
	 */
	abstract public void sendOnewayMessage(Message msg) 
			throws InvalidAgletException;
	/**
	 * <b>This is an experimental feature.</b>
	 * <p>This is almost like <tt>deactivate(long duration)</tt>,
	 * but there are some differences.
	 * <ol>
	 * <li>The object of the aglet remains at the memory.
	 * <li>No event is notified, thus ContextListener and PersistencyListener
	 * cannot know the suspend/activation.
	 * </ol>
	 * The aglet is re-activated by <tt>activate()</tt>.
	 * The caller is required to have "deactivate" permissoin.
	 * 
	 * @param duration duration of the aglet deactivating in milliseconds.
	 * If this is 0, it will be activeted at the next startup time.
	 * @exception AgletException if the aglet cannot be suspended.
	 * @exception IllegalArgumentException if the minutes parameter is negative.
	 */
	abstract public void suspend(long duration) throws InvalidAgletException;
}
