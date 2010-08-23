package net.sourceforge.aglets.examples.hello;

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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.util.SimpleItinerary;

/**
 * <tt> HelloAglet </tt> is a mobile aglet that goes to a remote host to say
 * "Hello World" and then returns home and dies.
 * <p>
 * 1. Shows a dialog box to specify the destination server. invoke
 * handleMessage() methods.
 * <p>
 * 2. (Execution of "startTrip" message) Moves to the destination server and
 * send "sayHello" message. to itself.
 * <p>
 * 3. (Execution of "sayHello" message) Display a string for 5 secs and go back
 * to the origin. Send "atHome" message to itself.
 * <p>
 * 4. (Execution of "atHome" message) Display a string "I'm back" for 2 secs and
 * destroy itself.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:54 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */
public class HelloAglet extends Aglet {

    /*
     * UI to interact with a user this will be automatically disposed when the
     * aglet is disposed
     */
    transient Frame my_dialog = null; // not serialized

    /*
     * message
     */
    String message = null;

    /*
     * home address represented as a string
     */
    String home = null;

    /*
     * Itinerary
     */
    SimpleItinerary itinerary = null;

    /*
     * Reports arrival home and disappears
     */
    public void atHome(Message msg) {
	this.setText("I'm back."); // greetings
	this.waitMessage(2 * 1000); // show message, 2 seconds
	this.dispose(); // dispose it self
    }

    protected void createGUI() {
	this.my_dialog = new MyDialog(this);

	this.my_dialog.pack();
	this.my_dialog.setSize(this.my_dialog.getPreferredSize());
	this.my_dialog.setVisible(true);
    }

    /**
     * Creates and shows the dialog window. This aglet keeps the reference to
     * the instance of the Dialog to avoid opening multiple dialog windows.
     */
    public void dialog(Message msg) {

	// check and create a dialog box
	if (this.my_dialog == null) {
	    this.my_dialog = new MyDialog(this);
	    this.my_dialog.pack();
	    this.my_dialog.setSize(this.my_dialog.getPreferredSize());
	}

	// show the dialog box
	this.my_dialog.setVisible(true);
    }

    /*
     * Handles the message
     * 
     * @param msg the message sent
     */
    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("atHome")) {
	    this.atHome(msg);
	} else if (msg.sameKind("startTrip")) {
	    this.startTrip(msg);
	} else if (msg.sameKind("sayHello")) {
	    this.sayHello(msg);
	} else if (msg.sameKind("dialog")) {
	    this.dialog(msg);
	} else {
	    return false;
	}
	return true;
    }

    /*
     * Initializes the aglet. Only called the very first time this aglet is
     * created.
     */
    @Override
    public void onCreation(Object init) {
	this.setMessage("Hello World!"); // default message

	// Create GUI to control this Aglet
	this.createGUI();
	this.itinerary = new SimpleItinerary(this);

	// Initialize the variable.
	this.home = this.getAgletContext().getHostingURL().toString();
    }

    /*
     * Say hello!
     */
    public void sayHello(Message msg) {
	this.setText(this.message); // greetings
	this.waitMessage(5 * 1000); // show message, 5 seconds

	// try back home
	try {
	    this.setText("I'll go back to.. " + this.home);
	    this.waitMessage(1000); // 1 second

	    // Go back home and Send "atHome" message to owner this
	    this.itinerary.go(this.home, "atHome");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /*
     * Set the message
     * 
     * @param message the message to send
     */
    public void setMessage(String message) {
	this.message = message;
    }

    /**
     * Strats the trip of this aglet to the destination.
     */
    public synchronized void startTrip(Message msg) {

	// get the address for trip
	String destination = (String) msg.getArg();

	// Go to the destination and Send "sayHello" message to owner(this)
	try {
	    this.itinerary.go(destination, "sayHello");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
