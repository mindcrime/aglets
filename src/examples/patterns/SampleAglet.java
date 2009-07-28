package examples.patterns;

/*
 * @(#)SampleAglet.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.message.Message;

import java.util.Vector;
import java.net.*;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Date;
import java.io.IOException;

/**
 * This class abstracts a stationary master aglet for the verious
 * samples in this package.
 * 
 * @version     1.00    96/12/28
 * @author      Yariv Aridor
 */

public abstract class SampleAglet extends Aglet implements MobilityListener, 
		PersistencyListener, CloneListener {
	/**
	 * The main interaction window.
	 */
	protected SampleWindow _msw = null;

	abstract void createSlave(Vector destinations, Object obj);
	abstract void createWindow();
	// -- Shows or brings to top the main window.
	// 
	protected synchronized void Dialog() {
		if (_msw != null) {
			_msw.show();
		} 
	}
	// -- Return the proxy of a specific aglet
	// 
	static public AgletProxy getAgletProxyInContext(Aglet m, 
			AgletID id) throws AgletException {
		return m.getAgletContext().getAgletProxy(id);
	}
	protected void go(URL url) {
		this.setupSlave(url);

		Vector destinations = new Vector();

		destinations.addElement(url);
		createSlave(destinations, new Object());
	}
	protected void go(Vector destinations, Object obj) {
		this.setupSlave((URL)(destinations.firstElement()));
		createSlave(destinations, obj);
	}
	// -- Handler for messages
	// 
	public boolean handleMessage(Message msg) {
		try {
			if (msg.sameKind("updateWindow")) {
				updateWindow();
			} else if (msg.sameKind("error")) {
				inError((String)(msg.getArg()));
			} 
		} catch (Exception e) {
			System.out.println(e);		// -- not yet handled
		} 
		return false;
	}
	protected synchronized void inError(Object message) {
		setTheMessage((String)message);
	}
	static public URL makeAgletURL(URL host, AgletID id) throws IOException {
		return new URL(host.toString() + "#" + id.toString());
	}
	public void onActivation(PersistencyEvent ev) {
		_msw.show();
	}
	/**
	 * Makes this aglet immobile
	 */
	public void onArrival(MobilityEvent ev) {
		throw new SecurityException("should not arrive here");
	}
	public void onClone(CloneEvent ev) {}
	public void onCloned(CloneEvent ev) {}
	public void onCloning(CloneEvent ev) {
		throw new SecurityException("not allowed");
	}
	// -- Callback methods for messages
	// 
	public void onCreation(Object o) {
		addMobilityListener(this);
		addCloneListener(this);
		addPersistencyListener(this);

		try {
			subscribeMessage("updateWindow");
		} catch (Exception e) {
			System.out.println(e);		// -- not yet handled
		} 

		createWindow();		// --create the GUI Window
	}
	public void onDeactivating(PersistencyEvent ev) {
		_msw.setVisible(false);
	}
	/**
	 * Makes this aglet immobile
	 */
	public synchronized void onDispatching(MobilityEvent ev) {

		// I will shout if you try to move me!
		throw new SecurityException("Don't ever try to move me!");
	}
	// -- Disposes the interaction window.
	// 
	public synchronized void onDisposing() {

		// Removes any windows if disposed.
		if (_msw != null) {
			_msw.dispose();
		} 
	}
	/**
	 * Makes this aglet immobile
	 */
	public void onReverting(MobilityEvent ev) {
		throw new SecurityException();
	}
	// --  Clears the output areas in the main window.
	// 
	protected void resetTheWindow() {
		if (_msw != null) {

			// _msw.clearMessage();
			_msw.clearResult();
		} 
	}
	// --  Entry point for the aglet's own thread.
	// 
	public void run() {
		setText("Starting...");
	}
	// --  Sets the message line in the interaction window.
	// 
	protected synchronized void setTheMessage(String text) {
		super.setText(text);
		if (_msw != null) {
			_msw.setMessage(text);
		} 
	}
	// -- callback function for the "go" bottom.
	// 
	protected void setupSlave(URL url) {
		String _target = url.toString();

		if (url.getHost().equals("")) {		// in case the URL address
			_target = "localhost";			// does not start with "atp://"
			System.exit(1);
		} 
		resetTheWindow();
		setTheMessage("going to: " + _target);

		getAgletContext().setProperty("position", (String)_msw.getPosition());
		getAgletContext().setProperty("filename", (String)_msw.getFilename());
		getAgletContext().multicastMessage(new Message("updateWindow"));
	}
	protected synchronized void updateWindow() throws Exception {
		AgletContext ctx = getAgletContext();

		_msw.setPosition((String)ctx.getProperty("position", 
												 _msw.getPosition()));
		_msw.setFilename((String)ctx.getProperty("filename", 
												 _msw.getFilename()));
	}
}
