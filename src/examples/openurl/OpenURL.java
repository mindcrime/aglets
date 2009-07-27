package examples.openurl;

/*
 * @(#)OpenURL.java
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
import com.ibm.agletx.util.*;

import java.lang.InterruptedException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.net.*;
import java.awt.*;

import java.util.Enumeration;

/**
 * <tt> OpenURL </tt> is an mobile aglet that goes to a remote host
 * to open the docuemnt specified by URL.
 * 
 * @version     1.00	$Date: 2009/07/27 10:31:42 $
 * @author	Mitsuru Oshima
 */
public class OpenURL extends Aglet {

	/*
	 * UI to interact with a User
	 * this will be automatically disposed when the aglet is disposed
	 */
	transient Frame my_dialog = null;

	/*
	 * 
	 */
	String url = "http://w3.trl.ibm.com";

	/*
	 * 
	 */
	String home = null;

	/*
	 * Itinerary
	 */
	SimpleItinerary itinerary = null;

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
	public void go(Message msg) {
		try {
			itinerary = new SimpleItinerary(this);
			itinerary.go((String)msg.getArg("destination"), 
						 new Message("openURL", msg.getArg("url")));
		} catch (Exception ex) {
			ex.printStackTrace();
			dispose();
		} 
	}
	/**
	 * Dispatch the aglet to the destination.
	 * @param destination a url which specifies the destination
	 * @exception when the aglet is in the invalid state
	 */
	public synchronized void goDestination(String dest) {
		try {
			AgletProxy p = (AgletProxy)clone();
			Message m = new Message("go");

			m.setArg("url", url);
			m.setArg("destination", dest);
			p.sendMessage(m);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	/*
	 * Handles the message
	 * @param msg the message sent
	 */
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("go")) {
			go(msg);
		} 
		if (msg.sameKind("atHome")) {
			atHome(msg);
		} else if (msg.sameKind("openURL")) {
			openURL(msg);
		} else if (msg.sameKind("dialog")) {
			dialog(msg);
		} else {
			return false;
		} 
		return true;
	}
	/*
	 * Initializes the aglet. Only called the very first time this
	 * aglet is created.
	 */
	public void onCreation(Object init) {
		dialog(null);
		home = getAgletContext().getHostingURL().toString();
	}
	/*
	 * open URL
	 */
	public void openURL(Message msg) {
		try {
			getAgletContext().showDocument(new URL(url));
			itinerary.go(home, "atHome");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
