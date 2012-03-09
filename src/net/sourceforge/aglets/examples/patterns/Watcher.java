package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)Watcher.java
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

import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.Arguments;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.patterns.Notifier;

/**
 * Class Watcher is used to check the update of a file located in a remote aglet
 * server. Given an URL and a file pathname, it will dispatch a slave (the
 * WatcherNotifier instance) to 'watch' update of a file. The slave will not
 * return to the master aglet. While at the remote server, the slave may
 * dispatch a file update report to the master. This is a part of the Messenger
 * usage pattern. Watcher is acting as a receiver in this pattern.
 * 
 * @see Watcher
 * @see WatcherWindow
 * @version 1.00 96/12/28
 * @author Danny B. Lange
 * @author Yoshiaki Mima
 * @author Yariv Aridor
 */

public class Watcher extends SampleAglet {
    /**
     * 
     */
    private static final long serialVersionUID = -5034114008843261797L;
    private final static String NotifierClassName = "examples.patterns.WatcherNotifier";
    private double interval = 0.0;
    private double duration = 0.0;
    private boolean stay = false;
    private String path = "";

    @Override
    protected void createSlave(Vector destinations, Object obj) {
	try {
	    Notifier.create(null, NotifierClassName, this.getAgletContext(), this, (URL) (destinations.firstElement()), this.interval, this.duration, this.stay, this.path);
	} catch (IOException ae) {
	    this.setTheMessage("Notifier Aglet creation failed...");
	} catch (AgletException ae) {
	    this.setTheMessage("Notifier Aglet creation failed...");
	}
    }

    /*
     * public void onCreation (Object o) { super.onCreation(o); try { _msw = new
     * WatcherWindow(this); updateWindow(); } catch (Exception e) {
     * inError(e.getMessage()); } }
     */

    @Override
    public void createWindow() {
	try {
	    this._msw = new WatcherWindow(this);
	    this.updateWindow();
	} catch (Exception e) {
	    this.inError(e.getMessage());
	}
    }

    /**
     * Creates and sets up the WatcherNotifier with the necessary information to
     * dispatch to a remote aglet server and hopefully stay there successfully.
     * This method is a callback method for the interaction window.
     * 
     * @param destination
     *            contains the destination URL.
     * @param interval
     *            TODO
     * @param duration
     *            TODO
     * @param stay
     *            TODO
     * @param path
     *            TODO
     */
    protected void go(
                      URL destination,
                      double interval,
                      double duration,
                      boolean stay,
                      String path) {
	this.interval = interval;
	this.duration = duration;
	this.stay = stay;
	this.path = path;

	super.go(destination);
    }

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("notification")) {
	    this.message((Arguments) (msg.getArg()));
	} else {
	    super.handleMessage(msg);
	}
	return true;
    }

    /**
     * Implements the message interface of a receiver. This method is a part of
     * the Messenger usage pattern.
     * 
     * @param message Message returned.
     */
    private synchronized void message(Arguments message) {
	int type = ((Integer) message.getArg("type")).intValue();

	if (type == Notifier.NOTIFICATION) {
	    this._msw.appendResult("UPDATE: "
		    + (String) (message.getArg("message")) + ", AT: "
		    + message.getArg("date").toString());
	} else {
	    this.setTheMessage((String) (message.getArg("message")));
	}
    }
}
