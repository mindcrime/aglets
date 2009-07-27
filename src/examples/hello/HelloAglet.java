package examples.hello;

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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.util.*;

import com.ibm.agletx.util.SimpleItinerary;

import java.lang.InterruptedException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

/**
 * <tt> HelloAglet </tt> is a mobile aglet that goes to a remote host
 * to say "Hello World" and then returns home and dies.
 * <p>1. Shows a dialog box to specify the destination server.
 * invoke handleMessage() methods.
 * <p>2. (Execution of "startTrip" message) Moves to the destination server
 * and send "sayHello" message.
 * to itself.
 * <p>3. (Execution of "sayHello" message) Display a string for 5 secs
 * and go back to the origin. Send "atHome" message to itself.
 * <p>4. (Execution of "atHome" message) Display a string "I'm back"
 * for 2 secs and destroy itself.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:42 $
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 */
public class HelloAglet extends Aglet {

	/*
	 * UI to interact with a user
	 * this will be automatically disposed when the aglet is disposed
	 */
	transient Frame my_dialog = null;		// not serialized

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
		setText("I'm back.");		// greetings
		waitMessage(2 * 1000);		// show message, 2 seconds
		dispose();					// dispose it self
	}
	protected void createGUI() {
		my_dialog = new MyDialog(this);

		my_dialog.pack();
		my_dialog.setSize(my_dialog.getPreferredSize());
		my_dialog.setVisible(true);
	}
	/**
	 * Creates and shows the dialog window.
	 * This aglet keeps the reference to the instance
	 * of the Dialog to avoid opening multiple dialog windows.
	 */
	public void dialog(Message msg) {

		// check and create a dialog box
		if (my_dialog == null) {
			my_dialog = new MyDialog(this);
			my_dialog.pack();
			my_dialog.setSize(my_dialog.getPreferredSize());
		} 

		// show the dialog box
		my_dialog.setVisible(true);
	}
	/*
	 * Handles the message
	 * @param msg the message sent
	 */
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("atHome")) {
			atHome(msg);
		} else if (msg.sameKind("startTrip")) {
			startTrip(msg);
		} else if (msg.sameKind("sayHello")) {
			sayHello(msg);
		} else if (msg.sameKind("dialog")) {
			dialog(msg);
		} else {
			return false;
		} 
		return true;
	}
	/*
	 * Initializes the aglet.
	 * Only called the very first time this aglet is created.
	 */
	public void onCreation(Object init) {
		setMessage("Hello World!");		// default message

		// Create GUI to control this Aglet
		createGUI();
		itinerary = new SimpleItinerary(this);

		// Initialize the variable.
		home = getAgletContext().getHostingURL().toString();
	}
	/*
	 * Say hello!
	 */
	public void sayHello(Message msg) {
		setText(message);			// greetings
		waitMessage(5 * 1000);		// show message, 5 seconds

		// try back home
		try {
			setText("I'll go back to.. " + home);
			waitMessage(1000);		// 1 second

			// Go back home and Send "atHome" message to owner this
			itinerary.go(home, "atHome");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	/*
	 * Set the message
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
		String destination = (String)msg.getArg();

		// Go to the destination and Send "sayHello" message to owner(this)
		try {
			itinerary.go(destination, "sayHello");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
