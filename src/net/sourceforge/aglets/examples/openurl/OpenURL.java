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

	/**
	 * 
	 */
	private static final long serialVersionUID = -5234890830553847795L;

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
	public void atHome(final Message msg) {
		setText("I'm back.");
		this.waitMessage(2 * 1000);
		dispose();
	}

	/**
	 * Creates the dialog window. This has the reference to the instance of the
	 * Dialog to avoid opening multiple dialog windows.
	 */
	public void dialog(final Message msg) {
		if (my_dialog == null) {
			my_dialog = new MyDialog(this);
			my_dialog.pack();
			my_dialog.setSize(my_dialog.getPreferredSize());
		}
		my_dialog.show();
	}

	public void go(final Message msg) {
		try {
			itinerary = new SimpleItinerary(this);
			itinerary.go((String) msg.getArg("destination"), new Message("openURL", msg.getArg("url")));
		} catch (final Exception ex) {
			ex.printStackTrace();
			dispose();
		}
	}

	/**
	 * Dispatch the aglet to the destination.
	 * 
	 * @param dest
	 *            a url which specifies the destination
	 * @exception when
	 *                the aglet is in the invalid state
	 */
	public synchronized void goDestination(final String dest) {
		try {
			final AgletProxy p = (AgletProxy) clone();
			final Message m = new Message("go");

			m.setArg("url", url);
			m.setArg("destination", dest);
			p.sendMessage(m);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * Handles the message
	 * 
	 * @param msg the message sent
	 */
	@Override
	public boolean handleMessage(final Message msg) {
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
	 * Initializes the aglet. Only called the very first time this aglet is
	 * created.
	 */
	@Override
	public void onCreation(final Object init) {
		dialog(null);
		home = getAgletContext().getHostingURL().toString();
	}

	/*
	 * open URL
	 */
	public void openURL(final Message msg) {
		try {
			getAgletContext().showDocument(new URL(url));
			itinerary.go(home, "atHome");
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}
