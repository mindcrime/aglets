package net.sourceforge.aglets.examples.openurl;

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

import java.awt.Frame;
import java.net.URL;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.util.SimpleItinerary;

/**
 * <tt> OpenURL </tt> is an mobile aglet that goes to a remote host to open the
 * docuemnt specified by URL.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:54 $
 * @author Mitsuru Oshima
 */
public class OpenURL extends Aglet {

    /*
     * UI to interact with a User this will be automatically disposed when the
     * aglet is disposed
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

    public void go(Message msg) {
	try {
	    this.itinerary = new SimpleItinerary(this);
	    this.itinerary.go((String) msg.getArg("destination"), new Message("openURL", msg.getArg("url")));
	} catch (Exception ex) {
	    ex.printStackTrace();
	    this.dispose();
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
    public synchronized void goDestination(String dest) {
	try {
	    AgletProxy p = (AgletProxy) this.clone();
	    Message m = new Message("go");

	    m.setArg("url", this.url);
	    m.setArg("destination", dest);
	    p.sendMessage(m);
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
	if (msg.sameKind("go")) {
	    this.go(msg);
	}
	if (msg.sameKind("atHome")) {
	    this.atHome(msg);
	} else if (msg.sameKind("openURL")) {
	    this.openURL(msg);
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
	this.dialog(null);
	this.home = this.getAgletContext().getHostingURL().toString();
    }

    /*
     * open URL
     */
    public void openURL(Message msg) {
	try {
	    this.getAgletContext().showDocument(new URL(this.url));
	    this.itinerary.go(this.home, "atHome");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
