package net.sourceforge.aglets.examples.patterns;

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

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.event.CloneEvent;
import com.ibm.aglet.event.CloneListener;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.event.PersistencyEvent;
import com.ibm.aglet.event.PersistencyListener;
import com.ibm.aglet.message.Message;

/**
 * This class abstracts a stationary master aglet for the verious samples in
 * this package.
 * 
 * @version 1.00 96/12/28
 * @author Yariv Aridor
 */

public abstract class SampleAglet extends Aglet implements MobilityListener,
PersistencyListener, CloneListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4541556138552175994L;
	// -- Return the proxy of a specific aglet
	//
	static public AgletProxy getAgletProxyInContext(final Aglet m, final AgletID id)
	throws AgletException {
		return m.getAgletContext().getAgletProxy(id);
	}

	static public URL makeAgletURL(final URL host, final AgletID id) throws IOException {
		return new URL(host.toString() + "#" + id.toString());
	}

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

	protected void go(final URL url) {
		setupSlave(url);

		final Vector destinations = new Vector();

		destinations.addElement(url);
		createSlave(destinations, new Object());
	}

	protected void go(final Vector destinations, final Object obj) {
		setupSlave((URL) (destinations.firstElement()));
		createSlave(destinations, obj);
	}

	// -- Handler for messages
	//
	@Override
	public boolean handleMessage(final Message msg) {
		try {
			if (msg.sameKind("updateWindow")) {
				updateWindow();
			} else if (msg.sameKind("error")) {
				inError((msg.getArg()));
			}
		} catch (final Exception e) {
			System.out.println(e); // -- not yet handled
		}
		return false;
	}

	protected synchronized void inError(final Object message) {
		setTheMessage((String) message);
	}

	@Override
	public void onActivation(final PersistencyEvent ev) {
		_msw.show();
	}

	/**
	 * Makes this aglet immobile
	 */
	@Override
	public void onArrival(final MobilityEvent ev) {
		throw new SecurityException("should not arrive here");
	}

	@Override
	public void onClone(final CloneEvent ev) {
	}

	@Override
	public void onCloned(final CloneEvent ev) {
	}

	@Override
	public void onCloning(final CloneEvent ev) {
		throw new SecurityException("not allowed");
	}

	// -- Callback methods for messages
	//
	@Override
	public void onCreation(final Object o) {
		addMobilityListener(this);
		addCloneListener(this);
		addPersistencyListener(this);

		try {
			subscribeMessage("updateWindow");
		} catch (final Exception e) {
			System.out.println(e); // -- not yet handled
		}

		createWindow(); // --create the GUI Window
	}

	@Override
	public void onDeactivating(final PersistencyEvent ev) {
		_msw.setVisible(false);
	}

	/**
	 * Makes this aglet immobile
	 */
	@Override
	public synchronized void onDispatching(final MobilityEvent ev) {

		// I will shout if you try to move me!
		throw new SecurityException("Don't ever try to move me!");
	}

	// -- Disposes the interaction window.
	//
	@Override
	public synchronized void onDisposing() {

		// Removes any windows if disposed.
		if (_msw != null) {
			_msw.dispose();
		}
	}

	/**
	 * Makes this aglet immobile
	 */
	@Override
	public void onReverting(final MobilityEvent ev) {
		throw new SecurityException();
	}

	// -- Clears the output areas in the main window.
	//
	protected void resetTheWindow() {
		if (_msw != null) {

			// _msw.clearMessage();
			_msw.clearResult();
		}
	}

	// -- Entry point for the aglet's own thread.
	//
	@Override
	public void run() {
		setText("Starting...");
	}

	// -- Sets the message line in the interaction window.
	//
	protected synchronized void setTheMessage(final String text) {
		super.setText(text);
		if (_msw != null) {
			_msw.setMessage(text);
		}
	}

	// -- callback function for the "go" bottom.
	//
	protected void setupSlave(final URL url) {
		String _target = url.toString();

		if (url.getHost().equals("")) { // in case the URL address
			_target = "localhost"; // does not start with "atp://"
			System.exit(1);
		}
		resetTheWindow();
		setTheMessage("going to: " + _target);

		getAgletContext().setProperty("position", _msw.getPosition());
		getAgletContext().setProperty("filename", _msw.getFilename());
		getAgletContext().multicastMessage(new Message("updateWindow"));
	}

	protected synchronized void updateWindow() throws Exception {
		final AgletContext ctx = getAgletContext();

		_msw.setPosition((String) ctx.getProperty("position", _msw.getPosition()));
		_msw.setFilename((String) ctx.getProperty("filename", _msw.getFilename()));
	}
}
