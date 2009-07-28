package com.ibm.aglet;

/*
 * @(#)Aglet.java
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

import java.security.PermissionCollection;

import com.ibm.aglet.event.*;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageManager;

import java.net.URL;
import java.io.IOException;
import java.io.NotSerializableException;
import java.awt.Image;
import java.applet.AudioClip;

import org.aglets.log.AgletsLogger;
import org.aglets.util.AgletsTranslator;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * The <tt>Aglet</tt> class is the abstract base class for aglets. Use this
 * class to create your own personalized aglets.
 * 
 * @version     2.00    $Date: 2009/07/28 07:04:53 $
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 * @author      Luca Ferrari
 */
public abstract class Aglet implements java.io.Serializable {

	/*
	 * Current API version of the Aglet class.
	 */
	public static final short MAJOR_VERSION = 2;
	public static final short MINOR_VERSION = 1;

	/**
	 * State of Aglet.
	 * @see AgletContext#getAgletProxies
	 */
	public static final int ACTIVE = 0x1;
	public static final int INACTIVE = 0x1 << 1;

	/*
	 * The aglet's stub. This instance variable is transient to avoid
	 * serializing the stub.
	 */
	transient AgletStub _stub = null;

	private CloneListener cloneListener = null;
	private MobilityListener mobilityListener = null;
	private PersistencyListener persistencyListener = null;
	
	/**
	 * The translator object for this agent. This should be reloaded on each platform, since
	 * the agent should be localized to the platform it is running on.
	 */
	private transient AgletsTranslator translator = AgletsTranslator.getInstance(this.getClass().getName(), Locale.getDefault());

	/**
	 * The logger associated with this agent.
	 */
	private transient AgletsLogger logger = AgletsLogger.getLogger(this.getClass().getName());

	/**
	 * Constructs an uninitialized aglet. This method is called only
	 * once in the life cycle of an aglet. As a rule, you should never
	 * override this constructor. Instead, you should override
	 * <tt>onCreation()</tt> to initialize the aglet upon creation.
	 * @see Aglet#onCreation
	 */
	protected Aglet() {}
	/**
	 * Adds the specified clone listener to receive clone events
	 * from this aglet.
	 * @param listener the mobility listener
	 */
	synchronized final public void addCloneListener(CloneListener listener) {
		if (cloneListener == null) {
			cloneListener = listener;
		} else if (cloneListener == listener) {
			return;
		} else if (cloneListener instanceof AgletEventListener) {
			((AgletEventListener)cloneListener).addCloneListener(listener);
		} else if (cloneListener instanceof CloneListener) {
			cloneListener = new AgletEventListener(cloneListener, listener);
		} 
	}
	/**
	 * Adds the specified mobility listener to receive mobility events
	 * from this aglet.
	 * @param listener the mobility listener
	 */
	synchronized final public void addMobilityListener(MobilityListener listener) {
		if (mobilityListener == null) {
			mobilityListener = listener;
		} else if (mobilityListener == listener) {
			return;
		} else if (mobilityListener instanceof AgletEventListener) {
			((AgletEventListener)mobilityListener)
				.addMobilityListener(listener);
		} else if (mobilityListener instanceof MobilityListener) {
			mobilityListener = new AgletEventListener(mobilityListener, 
													  listener);
		} 
	}
	/**
	 * Adds the specified persistency listener to receive persistency events
	 * from this aglet.
	 * @param listener the persistency listener
	 */
	synchronized final public void addPersistencyListener(PersistencyListener listener) {
		if (persistencyListener == null) {
			persistencyListener = listener;
		} else if (persistencyListener == listener) {
			return;
		} else if (persistencyListener instanceof AgletEventListener) {
			((AgletEventListener)persistencyListener)
				.addPersistencyListener(listener);
		} else if (persistencyListener instanceof PersistencyListener) {
			persistencyListener = new AgletEventListener(persistencyListener, 
					listener);
		} 
	}
	/**
	 * Clones the aglet and the proxy that holds the aglet. Notice that
	 * it is the cloned aglet proxy which is returned by this method.
	 * @return the cloned proxy.
	 * @exception CloneNotSupportedException when the cloning fails.
	 * @see CloneListener#onCloning
	 * @see CloneListener#onClone
	 */
	public final Object clone() throws CloneNotSupportedException {
		return _stub.clone();
	}
	/**
	 * Deactivates the aglet. The aglet will temporarily be stopped and
	 * removed from its current context. It will return to the context and
	 * resume execution after the specified period has elapsed.
	 * @param millisecounds duration of the aglet deactivating.
	 * If this is 0, it will be activeted at the next startup time.
	 * @exception NotSerializableException if the entire aglet is not
	 * serializable.
	 * @exception IOException if I/O failed
	 * @exception IllegalArgumentException if the argument is negative.
	 */
	public final void deactivate(long duration) throws IOException {
		_stub.deactivate(duration);
	}
	/**
	 * Dispatches the aglet to the location (host) specified by the
	 * ticket as argument.
	 * @param ticket ticket to dispatch destination.
	 * @exception ServerNotFoundException if the server could not be found.
	 * @exception java.net.UnknownHostException if the host given in the URL does not
	 * exist.
	 * @exception RequestRefusedException if the remote server refused the
	 * dispatch request.
	 * @exception ServerNotFoundException if the the destination is
	 * unavailable
	 * @exception NotSerializableException if the entire aglet is not
	 * serializable
	 * @see MobilityListener#onDispatching
	 * @see MobilityListener#onArrival
	 */
	public final void dispatch(Ticket ticket) 
			throws IOException, RequestRefusedException {
		_stub.dispatch(ticket);
	}
	/**
	 * Dispatches the aglet to the location (host) specified by the
	 * argument.
	 * @param destination dispatch destination.
	 * @exception ServerNotFoundException if the server could not be found.
	 * @exception java.net.UnknownHostException if the host given in the URL does not
	 * exist.
	 * @exception RequestRefusedException if the remote server refused the
	 * dispatch request.
	 * @exception ServerNotFoundException if the the destination is
	 * unavailable
	 * @exception NotSerializableException if the entire aglet is not
	 * serializable
	 * @see MobilityListener#onDispatching
	 * @see MobilityListener#onArrival
	 */
	public final void dispatch(URL destination) 
			throws IOException, RequestRefusedException {
		_stub.dispatch(destination);
	}
	/**
	 * Dispatches an event to this aglet
	 * @param ev the aglet event
	 */
	final public void dispatchEvent(AgletEvent ev) {
		switch (ev.getID()) {
		case CloneEvent.CLONING:
		case CloneEvent.CLONE:
		case CloneEvent.CLONED:
			processCloneEvent((CloneEvent)ev);
			break;
		case MobilityEvent.DISPATCHING:
		case MobilityEvent.REVERTING:
		case MobilityEvent.ARRIVAL:
			processMobilityEvent((MobilityEvent)ev);
			break;
		case PersistencyEvent.DEACTIVATING:
		case PersistencyEvent.ACTIVATION:
			processPersistencyEvent((PersistencyEvent)ev);
			break;
		}
	}
	/**
	 * Destroys and removes the aglet from its current aglet context.
	 * A successful invocation of this method will kill all threads
	 * created by the given aglet.
	 * @see Aglet#onDisposing
	 */
	public final void dispose() {
		_stub.dispose();
	}
	/**
	 * Exits the current monitor.
	 * @exception IllegalMonitorStateException if the current thread is not the
	 * owner of the monitor.
	 * @see Aglet#exitMonitor
	 * @see waitMessage
	 * @see notifyMessage
	 * @see notifyAllMessages
	 */
	public void exitMonitor() {
		getMessageManager().exitMonitor();
	}
	/**
	 * Gets the context in which the aglet is currently executing.
	 * @return the current execution context.
	 */
	public final AgletContext getAgletContext() {
		return _stub.getAgletContext();
	}
	/**
	 * Gets the id of this aglet.
	 * @return the <tt>AgletID<tt> object of this aglet
	 * @see aglet.AgletID
	 */
	public final AgletID getAgletID() {
		return getAgletInfo().getAgletID();
	}
	/**
	 * Gets the info object of this aglet
	 * @return the <tt>aglet.AgletInfo<tt> object of this aglet
	 * @see aglet.AgletID
	 */
	public final AgletInfo getAgletInfo() {
		return _stub.getAgletInfo();
	}
	/**
	 * Gets an audio data
	 */
	public final AudioClip getAudioData(URL url) throws IOException {
		return getAgletContext().getAudioClip(url);
	}
	/**
	 * Gets the code base URL of this aglet
	 * @return the <tt>java.net.URL<tt> object of this aglet
	 * @see aglet.AgletID
	 */
	public final URL getCodeBase() {
		return getAgletInfo().getCodeBase();
	}
	/**
	 * Gets an image
	 */
	public final Image getImage(URL url) throws IOException {
		return getAgletContext().getImage(url);
	}
	/**
	 * Gets an image
	 */
	public final Image getImage(URL url, String name) throws IOException {
		return getAgletContext().getImage(new URL(url, name));
	}
	/**
	 * Gets the message manager.
	 * @return the message manager.
	 */
	public final MessageManager getMessageManager() {
		return _stub.getMessageManager();
	}
	/**
	 * Gets the protections: permission collection about
	 * who can send what kind of messages to the aglet
	 * @return collection of protections about who can send
	 * what kind of messages to the aglet
	 */
	public PermissionCollection getProtections() {
		return _stub.getProtections();
	}
	/**
	 * Gets the proxy of aglet.
	 * @return the proxy of aglet
	 */
	public final AgletProxy getProxy() {
		return _stub.getAgletContext().getAgletProxy(getAgletID());
	}
	/**
	 * Gets the message line of this Aglet.
	 * @return the <tt>String<tt> representing a message the aglet shows.
	 */
	public final String getText() {
		return _stub.getText();
	}
	/**
	 * Handles the message form outside.
	 * @param msg the message sent to the aglet
	 * @return true if the message was handled. Returns false if the message
	 * was not handled. If false is returned, the <tt>NotHandledException</tt> exception
	 * is thrown in the <tt>FutureReply.getReply</tt> and <tt>AgletProxy.sendMessage</tt>
	 * methods.
	 * @see FutureReply#getReply
	 * @see Message#sendReply
	 * @see AgletProxy#sendMessage
	 */
	public boolean handleMessage(Message message) {
		return false;
	}
	/**
	 * Notifies all of waiting threads.
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see Aglet#notifyAllMessages
	 * @see waitMessage
	 * @see notifyMessage
	 */
	public void notifyAllMessages() {
		getMessageManager().notifyAllMessages();
	}
	/**
	 * Notifies a single waiting thread.
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see Aglet#notifyMessage
	 * @see waitMessage
	 * @see notifyAllMessages
	 */
	public void notifyMessage() {
		getMessageManager().notifyMessage();
	}
	/**
	 * Initializes the new aglet. This method is called only once
	 * in the life cycle of an aglet. Override this method for custom
	 * initialization of the aglet.
	 * @param init the argument with which the aglet is initialized.
	 * @see AgletContext#createAglet
	 */
	public void onCreation(Object init) {}
	/**
	 * Is called when an attempt is made to dispose of the aglet.
	 * Subclasses may override this method to implement actions that
	 * should be taken in response to a request for disposal.
	 * @exception SecurityException if the request for disposal is rejected.
	 * @see Aglet#dispose
	 * @see AgletProxy#dispose
	 */
	public void onDisposing() {}
	/**
	 * Processes clone events occurring on this aglet by dispatching them
	 * to any registered CloneListener objects.
	 * @param ev the clone event
	 */
	protected void processCloneEvent(CloneEvent ev) {
		if (cloneListener != null) {
			switch (ev.getID()) {
			case CloneEvent.CLONING:
				cloneListener.onCloning(ev);
				break;
			case CloneEvent.CLONE:
				cloneListener.onClone(ev);
				break;
			case CloneEvent.CLONED:
				cloneListener.onCloned(ev);
				break;
			}
		} 
	}
	/**
	 * Processes mobility events occurring on this aglet by dispatching them
	 * to any registered MobilityListener objects.
	 * @param ev the mobility event
	 */
	protected void processMobilityEvent(MobilityEvent ev) {
		if (mobilityListener != null) {
			switch (ev.getID()) {
			case MobilityEvent.DISPATCHING:
				mobilityListener.onDispatching(ev);
				break;
			case MobilityEvent.REVERTING:
				mobilityListener.onReverting(ev);
				break;
			case MobilityEvent.ARRIVAL:
				mobilityListener.onArrival(ev);
				break;
			}
		} 
	}
	/**
	 * Processes persistency events occurring on this aglet by dispatching them
	 * to any registered PersistencyListener objects.
	 * @param ev the persistency event
	 */
	protected void processPersistencyEvent(PersistencyEvent ev) {
		if (persistencyListener != null) {
			switch (ev.getID()) {
			case PersistencyEvent.DEACTIVATING:
				persistencyListener.onDeactivating(ev);
				break;
			case PersistencyEvent.ACTIVATION:
				persistencyListener.onActivation(ev);
				break;
			}
		} 
	}
	/**
	 * Removes the specified clone listener so it no longer receives clone
	 * events.
	 * @param l the clone listener
	 */
	synchronized final public void removeCloneListener(CloneListener l) {
		if (cloneListener == l) {
			cloneListener = null;
		} else if (cloneListener instanceof AgletEventListener) {
			((AgletEventListener)cloneListener).removeCloneListener(l);
			if (((AgletEventListener)cloneListener).size() == 0) {
				cloneListener = null;
			} 
		} 
	}
	/**
	 * Removes the specified mobility listener so it no longer receives
	 * mobility events.
	 * @param l the mobility listener
	 */
	synchronized final public void removeMobilityListener(MobilityListener l) {
		if (mobilityListener == l) {
			mobilityListener = null;
		} else if (mobilityListener instanceof AgletEventListener) {
			((AgletEventListener)mobilityListener).removeMobilityListener(l);
			if (((AgletEventListener)mobilityListener).size() == 0) {
				mobilityListener = null;
			} 
		} 
	}
	/**
	 * Removes the specified persistency listener so it no longer receives
	 * persistency events.
	 * @param l the persistency listener
	 */
	synchronized final public void removePersistencyListener(PersistencyListener l) {
		if (persistencyListener == l) {
			persistencyListener = null;
		} else if (persistencyListener instanceof AgletEventListener) {
			((AgletEventListener)persistencyListener)
				.removePersistencyListener(l);
			if (((AgletEventListener)persistencyListener).size() == 0) {
				persistencyListener = null;
			} 
		} 
	}
	/**
	 * Is the entry point for the aglet's own thread of execution.
	 * This method is invoked upon a successful creation, dispatch,
	 * retraction, or activation of the aglet.
	 * @see Aglet#onCreation
	 * @see CloneListener#onClone
	 * @see MobilityListener#onArrival
	 * @see PersistencyListener#onActivation
	 */
	public void run() {}
	/**
	 * Sets the protections: permission collection about
	 * who can send what kind of messages to the aglet
	 * @param protections collection of protections about who can send
	 * what kind of messages to the aglet
	 */
	public void setProtections(PermissionCollection protections) {
		_stub.setProtections(protections);
	}
	/**
	 * Sets the proxy for the aglet. This cannot be set twice.
	 * Called by the system.
	 * @param proxy the proxy to set
	 */
	public synchronized final void setStub(AgletStub stub) {
		if (_stub != null) {
			throw new SecurityException();
		} 
		stub.setAglet(this);
		_stub = stub;
	}
	/**
	 * Sets the text of this Aglet. A way for the aglet to display
	 * messages on the viewer window.
	 * @param message the message.
	 */
	public final void setText(String text) {
		_stub.setText(text);
	}
	/**
	 * [Preliminary]
	 * Save a snapshot of this aglet into a 2nd storage. The snapshot will
	 * be activated only if the aglet is accidentally killed.
	 * (because of the system clash for instance)
	 * If one of dispose, dispatch and deactivate are invoked, this
	 * snapshot will be removed from 2nd storate. This call doesn't fire
	 * the persistency event, hence no lister is invoked.
	 * @exception NotSerializableException if the entire aglet is not
	 * serializable
	 * @exception IOException if I/O failed
	 */
	public final void snapshot() throws IOException {
		_stub.snapshot();
	}
	/**
	 * Gets the aglet property indicated by the key.
	 * @param key the name of the aglet property.
	 * @return the value of the specified key.
	 * public final String getProperty(String key) {
	 * return _proxy._getProperty(key);
	 * }
	 */

	/**
	 * Gets the aglet property indicated by the key and default value.
	 * @param key the name of the aglet property.
	 * @param defValue the default value to use if this property is not set.
	 * @return the value of the specified key.
	 * public final String getProperty(String key, String defValue) {
	 * return _proxy._getProperty(key, defValue);
	 * }
	 */

	/**
	 * Sets the aglet property indicated by the key and the value.
	 * @param key the name of the aglet property.
	 * @param value the value to put
	 * public final void setProperty(String key, String value) {
	 * _proxy._setProperty(key, value);
	 * }
	 */

	/**
	 * Enumerates all the property keys.
	 * @return property key enumeration.
	 * public final Enumeration getPropertyKeys() {
	 * return _proxy._getPropertyKeys();
	 * }
	 */

	/**
	 * Subscribes to a named message.
	 * @param name the message kind.
	 */
	public final void subscribeMessage(String name) {
		_stub.subscribeMessage(name);
	}
	
	
	/**
	 * This method allows the suspension of a running agent for the specified
	 * number of millisecs. During the suspension the agent will "sleep", that means it will
	 * not do anything but it will still receive messages. Incoming messages will be
	 * enqueued and will be processed once the agent woke up.
	 * 
	 * During the sleeping phase the agent will stay in memory.
	 * 
	 * You should not interact directly with the agent thread calling Thread.sleep() method,
	 * you should use this that takes into account synchronization and messaging issues.
	 * 
	 * @param duration the number of millisecs the agent will sleep. If zero the agent will not sleep.
	 * @throws AgletException if something goes wrong with the agent/stub/message manager
	 * @throws IllegalArgumentException if the duration is less then zero
	 */
	public final void suspend(long duration) throws AgletException, IllegalArgumentException {
	    // check params
	    if( duration < 0 )
		throw new IllegalArgumentException("Sleeping time cannot be negative! Please specify a positive millisecs value!");
	    else
	    if( duration == 0 )
		return;
	    else
		_stub.suspend(duration);
	}
	
	/**
	 * A methods to make the agent sleep. The method is a wrapper for the suspend one, thus calling
	 * sleep or suspend produces the same effects. Please see the comments for the suspend method.
	 * @param duration the duration of the sleeping period, in millisecs
	 * @throws AgletException if something goes wrong with the agent/stub/message manager
	 * @throws IllegalArgumentException if the duration is less then zero
	 */
	public final void sleep(long duration) throws AgletException, IllegalArgumentException {
	    this.suspend(duration);
	}
	
	/**
	 * Unsubscribes from all message kinds.
	 */
	public final void unsubscribeAllMessages() {
		_stub.unsubscribeAllMessages();
	}
	/**
	 * Unsubscribes from a named message.
	 * @param name the message kind.
	 * @return true if the message kind was subscribed.
	 */
	public final boolean unsubscribeMessage(String name) {
		return _stub.unsubscribeMessage(name);
	}
	/**
	 * Waits until it is notified.
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see MessageManager#waitMessage
	 * @see notifyMessage
	 * @see notifyAllMessages
	 */
	public void waitMessage() {
		getMessageManager().waitMessage();
	}
	/**
	 * Waits until it is notified or the timeout expires
	 * @param timeout the maximum value to wait in milliseconds
	 * @exception IllegalMonitorStateException If the current thread
	 * is not the owner of the monitor.
	 * @see MessageManager#waitMessage
	 * @see notifyMessage
	 * @see notifyAllMessages
	 */
	public void waitMessage(long timeout) {
		getMessageManager().waitMessage(timeout);
	}
	
	/**
	 * Inits (or reinitis) the currnet AgletTranslator object with the specified
	 * base name and locale.
	 * @param baseName the identifier basename for the translator object (e.g., the name of a property file or a class name)
	 * @param locale the locale to use (or null).
	 */
	protected synchronized void initTranslator(String baseName, Locale locale){
	    this.translator = AgletsTranslator.getInstance(baseName, locale);
	}
	
	
	/**
	 * Gets back the translator.
	 * @param reinitialize true if the translator must be reinitialized. This should be
	 * used after a moving or cloning operation, to be sure the translator has been re-initialized.
	 * @return the translator
	 */
	protected final AgletsTranslator getTranslator(boolean reinitialize) {
	    if( this.translator == null && reinitialize )
		this.translator = AgletsTranslator.getInstance(this.getClass().getName(), Locale.getDefault());
		
	    return translator;
	}
	/**
	 * Sets the translator value.
	 * @param translator the translator to set
	 */
	protected final void setTranslator(AgletsTranslator translator) {
	    this.translator = translator;
	}
	
	
	/**
	 * Provides the logger associated to this agent.
	 * @param reinitialize true if the logger must be reinitialized
	 * @return the logger associated to this agent
	 */
	protected final AgletsLogger getLogger(boolean reinitialize){
	    if( this.logger == null && reinitialize)
		this.logger = AgletsLogger.getLogger(this.getClass().getName());
	    
	    return this.logger;
	}
	
	
}
