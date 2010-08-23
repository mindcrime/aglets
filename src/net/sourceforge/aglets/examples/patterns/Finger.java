package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)Finger.java
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
import java.util.Vector;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.patterns.Slave;

/**
 * Class Finger is used to retrieve local user information from a remote aglet
 * server. This information includes the name, organization, email address, and
 * the local time at the remote server. Given an URL (Agler Resource Locator) it
 * will dispatch a slave (the FingerSlave class) to retrieve local user
 * information. The slave will return with the information to be displayed by
 * the master.
 * 
 * @see FingerSlave
 * @version 1.00 96/12/28
 * @author Danny B. Lange
 * @author Yariv Aridor
 * @version 1.01 98/11/18
 * @author Shintaro Kosugi
 */

public class Finger extends SampleAglet {

    // Aglet web source (the URL & classname of the slave class).
    private static final String SlaveClassName = "examples.patterns.FingerSlave";

    /**
     * Creates and sets up the slave with necessary information for it to
     * dispatch to a remote aglet server and hopefully return safely. This
     * method is a callback method for the interaction window.
     * 
     * @param url
     *            the url of the destination.
     */
    @Override
    protected void createSlave(Vector destinations, Object obj) {
	try {
	    Slave.create(null, SlaveClassName, this.getAgletContext(), this, destinations, new String());
	} catch (IOException ae) {
	    this.inError(ae.getMessage());
	} catch (AgletException ae) {
	    this.inError(ae.getMessage());
	}
    }

    @Override
    public void createWindow() {
	try {
	    this._msw = new FingerWindow(this);
	    this.updateWindow();
	} catch (Exception e) {
	    this.inError(e.getMessage());
	}
    }

    // -- Handler for messages
    //
    @Override
    public boolean handleMessage(Message msg) {
	try {
	    if (msg.sameKind("result")) {
		FingerInfo arg = null;

		if ((arg = (FingerInfo) (msg.getArg())) != null) {
		    this._msw.setResult(arg.toTextBlock());
		    this.setTheMessage("Finished");
		} else {
		    this.setTheMessage("Finished, but no argument!");
		}
	    } else {
		super.handleMessage(msg);
	    }
	} catch (Exception e) {

	    // -- not yet handled
	    System.out.println(e);
	}
	return false;
    }
}
