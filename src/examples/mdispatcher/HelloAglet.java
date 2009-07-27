package examples.mdispatcher;

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
import java.util.Enumeration;
import java.awt.event.*;

/**
 * <tt> HelloAglet </tt> is a revised version of examples.hello.HelloAglet,
 * which uses MethodDispatcher class to handle incoming messages.
 * 
 * @version     1.00	$Date: 2009/07/27 10:31:41 $
 * @author	Danny B. Lange
 * @author	Mitsuru Oshima
 * @see examples.hello.HelloAglet
 * @see examples.examples.MethodDispatcher
 */
public class HelloAglet extends Aglet {

	/*
	 * UI to interact with a User
	 * this will be automatically disposed when the aglet is disposed
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
		setText("I'm back.");
		waitMessage(2 * 1000);
		dispose();
	}
	/**
	 * Creates the dialog window. This has the reference to the instance
	 * of the Dialog to avoid opening multiple dialog windows.
	 */
	public void dialog(Message msg) {
		if (my_dialog == null) {
			my_dialog = new MyDialog(this);
			my_dialog.pack();
			my_dialog.setSize(my_dialog.getPreferredSize());
		} 
		my_dialog.show();
	}
	/*
	 * Go to the destination and say hello!
	 */
	public void go(Message msg) {
		URL dest = (URL)msg.getArg();

		try {
			itinerary.go(dest.toString(), "sayHello");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	/**
	 * Dispatch the aglet to the destination.
	 * @param destination a url which specifies the destination
	 * @exception when the aglet is in the invalid state
	 */
	public synchronized void goDestination(String destination) {
		try {
			itinerary.go(destination, "sayHello");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	/*
	 * Handles the message
	 * @param msg the message sent
	 */
	public boolean handleMessage(Message msg) {
		return mdispatcher.handleMessage(msg);
	}
	/*
	 * Initializes the aglet. Only called the very first time this
	 * aglet is created.
	 */
	public void onCreation(Object init) {
		itinerary = new SimpleItinerary(this);
		mdispatcher = new MethodDispatcher(this);

		// Remember the URL as a string.
		home = getAgletContext().getHostingURL().toString();
	}
	/*
	 * Say hello!
	 */
	public void sayHello(Message msg) {

		// greetings
		setText(message);

		waitMessage(5 * 1000);

		try {
			setText("I'll go back to.. " + home);
			waitMessage(1000);
			itinerary.go(home, "atHome");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
