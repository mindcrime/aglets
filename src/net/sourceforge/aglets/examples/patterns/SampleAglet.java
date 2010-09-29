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
    /**
     * The main interaction window.
     */
    protected SampleWindow _msw = null;

    abstract void createSlave(Vector destinations, Object obj);

    abstract void createWindow();

    // -- Shows or brings to top the main window.
    //
    protected synchronized void Dialog() {
	if (this._msw != null) {
	    this._msw.show();
	}
    }

    // -- Return the proxy of a specific aglet
    //
    static public AgletProxy getAgletProxyInContext(Aglet m, AgletID id)
    throws AgletException {
	return m.getAgletContext().getAgletProxy(id);
    }

    protected void go(URL url) {
	this.setupSlave(url);

	Vector destinations = new Vector();

	destinations.addElement(url);
	this.createSlave(destinations, new Object());
    }

    protected void go(Vector destinations, Object obj) {
	this.setupSlave((URL) (destinations.firstElement()));
	this.createSlave(destinations, obj);
    }

    // -- Handler for messages
    //
    @Override
    public boolean handleMessage(Message msg) {
	try {
	    if (msg.sameKind("updateWindow")) {
		this.updateWindow();
	    } else if (msg.sameKind("error")) {
		this.inError((msg.getArg()));
	    }
	} catch (Exception e) {
	    System.out.println(e); // -- not yet handled
	}
	return false;
    }

    protected synchronized void inError(Object message) {
	this.setTheMessage((String) message);
    }

    static public URL makeAgletURL(URL host, AgletID id) throws IOException {
	return new URL(host.toString() + "#" + id.toString());
    }

    @Override
    public void onActivation(PersistencyEvent ev) {
	this._msw.show();
    }

    /**
     * Makes this aglet immobile
     */
    @Override
    public void onArrival(MobilityEvent ev) {
	throw new SecurityException("should not arrive here");
    }

    @Override
    public void onClone(CloneEvent ev) {
    }

    @Override
    public void onCloned(CloneEvent ev) {
    }

    @Override
    public void onCloning(CloneEvent ev) {
	throw new SecurityException("not allowed");
    }

    // -- Callback methods for messages
    //
    @Override
    public void onCreation(Object o) {
	this.addMobilityListener(this);
	this.addCloneListener(this);
	this.addPersistencyListener(this);

	try {
	    this.subscribeMessage("updateWindow");
	} catch (Exception e) {
	    System.out.println(e); // -- not yet handled
	}

	this.createWindow(); // --create the GUI Window
    }

    @Override
    public void onDeactivating(PersistencyEvent ev) {
	this._msw.setVisible(false);
    }

    /**
     * Makes this aglet immobile
     */
    @Override
    public synchronized void onDispatching(MobilityEvent ev) {

	// I will shout if you try to move me!
	throw new SecurityException("Don't ever try to move me!");
    }

    // -- Disposes the interaction window.
    //
    @Override
    public synchronized void onDisposing() {

	// Removes any windows if disposed.
	if (this._msw != null) {
	    this._msw.dispose();
	}
    }

    /**
     * Makes this aglet immobile
     */
    @Override
    public void onReverting(MobilityEvent ev) {
	throw new SecurityException();
    }

    // -- Clears the output areas in the main window.
    //
    protected void resetTheWindow() {
	if (this._msw != null) {

	    // _msw.clearMessage();
	    this._msw.clearResult();
	}
    }

    // -- Entry point for the aglet's own thread.
    //
    @Override
    public void run() {
	this.setText("Starting...");
    }

    // -- Sets the message line in the interaction window.
    //
    protected synchronized void setTheMessage(String text) {
	super.setText(text);
	if (this._msw != null) {
	    this._msw.setMessage(text);
	}
    }

    // -- callback function for the "go" bottom.
    //
    protected void setupSlave(URL url) {
	String _target = url.toString();

	if (url.getHost().equals("")) { // in case the URL address
	    _target = "localhost"; // does not start with "atp://"
	    System.exit(1);
	}
	this.resetTheWindow();
	this.setTheMessage("going to: " + _target);

	this.getAgletContext().setProperty("position", this._msw.getPosition());
	this.getAgletContext().setProperty("filename", this._msw.getFilename());
	this.getAgletContext().multicastMessage(new Message("updateWindow"));
    }

    protected synchronized void updateWindow() throws Exception {
	AgletContext ctx = this.getAgletContext();

	this._msw.setPosition((String) ctx.getProperty("position", this._msw.getPosition()));
	this._msw.setFilename((String) ctx.getProperty("filename", this._msw.getFilename()));
    }
}
