package net.sourceforge.aglets.examples.mdispatcher;

/*
 * @(#)HelloAglet.java
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

import java.awt.Frame;
import java.net.URL;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.util.SimpleItinerary;

/**
 * <tt> HelloAglet </tt> is a revised version of examples.hello.HelloAglet,
 * which uses MethodDispatcher class to handle incoming messages.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 * @see net.sourceforge.aglets.examples.hello.HelloAglet
 * @see examples.examples.MethodDispatcher
 */
public class HelloAglet extends Aglet {

    /**
     * 
     */
    private static final long serialVersionUID = -8658785846814939604L;

    /*
     * UI to interact with a User this will be automatically disposed when the
     * aglet is disposed
     */
    transient Frame my_dialog = null;

    /*
     * Default message
     */
    String message = "Hello World!";

    /*
     * 
     */
    String home = null;

    /*
     * Itinerary
     */
    SimpleItinerary itinerary = null;

    /*
     * MethodDispatcher
     */
    MethodDispatcher mdispatcher = null;

    /*
     * 
     */
    public void atHome(Message msg) {
	this.setText("I'm back.");
	this.waitMessage(2 * 1000);
	this.dispose();
    }

    /**
     * Creates the dialog window. This has the reference to the instance of the
     * Dialog to avoid opening multiple dialog windows.
     */
    public void dialog(Message msg) {
	if (this.my_dialog == null) {
	    this.my_dialog = new MyDialog(this);
	    this.my_dialog.pack();
	    this.my_dialog.setSize(this.my_dialog.getPreferredSize());
	}
	this.my_dialog.show();
    }

    /*
     * Go to the destination and say hello!
     */
    public void go(Message msg) {
	URL dest = (URL) msg.getArg();

	try {
	    this.itinerary.go(dest.toString(), "sayHello");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Dispatch the aglet to the destination.
     * 
     * @param destination
     *            a url which specifies the destination
     * @exception when
     *                the aglet is in the invalid state
     */
    public synchronized void goDestination(String destination) {
	try {
	    this.itinerary.go(destination, "sayHello");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /*
     * Handles the message
     * 
     * @param msg the message sent
     */
    @Override
    public boolean handleMessage(Message msg) {
	return this.mdispatcher.handleMessage(msg);
    }

    /*
     * Initializes the aglet. Only called the very first time this aglet is
     * created.
     */
    @Override
    public void onCreation(Object init) {
	this.itinerary = new SimpleItinerary(this);
	this.mdispatcher = new MethodDispatcher(this);

	// Remember the URL as a string.
	this.home = this.getAgletContext().getHostingURL().toString();
    }

    /*
     * Say hello!
     */
    public void sayHello(Message msg) {

	// greetings
	this.setText(this.message);

	this.waitMessage(5 * 1000);

	try {
	    this.setText("I'll go back to.. " + this.home);
	    this.waitMessage(1000);
	    this.itinerary.go(this.home, "atHome");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
