package com.ibm.agletx.patterns;

/*
 * @(#)Notifier.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Arguments;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.util.SimpleItinerary;

/**
 * Create a notifier by calling the static method <tt>create</tt>. The notifier
 * will get dispatched automatically. The notifier performs successive checks
 * (at its destination) within a specified time duration. Upon every successfull
 * check (one the encounters a change in a local state), it notifies its master.
 * The notifier can be defined (see
 * 
 * <pre>
 * create
 * </pre>
 * 
 * ) to complete its job after the first successive check (although its time
 * duration has not been reached yet). If a notifier cannot be dispatched or it
 * encounters an error during a check, it notifies its master and disposed
 * itself.
 * 
 * @version 1.01 97/10/1
 * @author Danny B. Lange
 * @author Yariv Aridor
 */

public abstract class Notifier extends Aglet {

    // Type of notification messages

    public final static int NOTIFICATION = 0;
    public final static int EXPIRY = 1;
    public final static int EXCEPTION = 2;

    // -- minimum time interval (in hours) above which the notifier
    // -- is deactivated between two successive checks.
    private static final double MIN_TIME_OF_DEACTIVATION = 1.0 / 120.0;

    private String origin = null;

    // -- the master identifier
    private AgletID _master = null;

    // -- URL of the remote host to visit
    private String _destination;

    // -- Interval between checks (in hours)
    private double _interval = 1.0 / 60.0;

    // -- Duration of stay (in hours)
    private double _duration = 1.0;

    // -- Starting time
    private long _startingTime = 0;

    // -- Should stay after successfull check? .
    private boolean _stay = false;

    /**
     * The protected variable that carries any messages that should go along
     * with the notification back to the subscriber.
     */
    protected Object MESSAGE = null;

    /**
     * The protected variable that carries any arguments for the checks that
     * this notifier performs.
     */
    protected Object ARGUMENT = null;

    // -- Checks for time limit.
    //
    private boolean checkTimeout() {
	return (this._startingTime + (long) (this._duration * 3600000)) > (new Date()).getTime();
    }

    /**
     * Creates a notifier.
     * 
     * @param url
     *            the URL of the aglet class.
     * @param source
     *            the name of the aglet class.
     * @param context
     *            the aglet context in which the notifier should be created.
     * @param master
     *            the master aglet.
     * @param destination
     *            the URL of the destination.
     * @param interval
     *            the time in hours between to checks.
     * @param duration
     *            the life time of the notifier.
     * @param stay
     *            whether the notifier should remain after a notification.
     * @param argument
     *            the
     * 
     *            <pre>
     * argument
     * </pre>
     * 
     *            object.
     * @return an aglet proxy for the notifier.
     * @exception AgletException
     *                if the creation fails.
     */
    static public AgletProxy create(
                                    URL url,
                                    String source,
                                    AgletContext context,
                                    Aglet master,
                                    URL destination,
                                    double interval,
                                    double duration,
                                    boolean stay,
                                    Object argument)
    throws IOException,
    AgletException {
	Arguments args = new Arguments();

	args.setArg("destination", destination.toString());
	args.setArg("interval", interval);
	args.setArg("duration", duration);
	args.setArg("stay", stay);
	args.setArg("agrument", argument);
	args.setArg("receiver", master.getAgletID());

	try {
	    return context.createAglet(url, source, args);
	} catch (InstantiationException ex) {
	    throw new AgletException(ex.getClass().getName() + ':'
		    + ex.getMessage());
	} catch (ClassNotFoundException ex) {
	    throw new AgletException(ex.getClass().getName() + ':'
		    + ex.getMessage());
	}
    }

    /**
     * This method should be overridden to specify the check method for this
     * notifier.
     * 
     * @return boolean result of the check.
     * @exception AgletException
     *                if fails to complete.
     */
    abstract protected boolean doCheck() throws Exception;

    private void gotoSleep(double hours) throws AgletException {
	if (hours > MIN_TIME_OF_DEACTIVATION) {
	    try {
		this.deactivate((int) (hours * 60));
	    } catch (IOException ae) {
		throw new AgletException("deactivation has been failed!!");
	    }
	} else {
	    this.waitInHours(hours); // -- busy wait
	}
    }

    @Override
    public boolean handleMessage(Message msg) {
	try {
	    if (msg.sameKind("start")) {
		this.start();
	    } else {
		return false;
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return false;
    }

    // -- Abstract methods
    //

    /**
     * This method should be overridden to specify any intialization before the
     * checks performed by this notifier.
     * 
     * @exception AgletException
     *                if fails to complete.
     */
    abstract protected void initializeCheck() throws Exception;

    private String makeExceptionMessage(String phase, Throwable ex) {
	String host = this.getAgletContext().getHostingURL().toString();

	return new String(host + ":" + "<" + ex.getClass().getName() + "::"
		+ ex.getMessage() + ">;"
		+ ((phase != null) ? "DURING " + phase : "Internal"));
    }

    // -- defines initialization before the checks begin.
    //
    private void observeInit() throws Exception {
	try {
	    this.initializeCheck();
	} catch (Throwable ex) {
	    throw new Exception(this.makeExceptionMessage("initializeCheck", ex));
	}
	this.startTimer();
    }

    // -- Performs repeated checks.
    //
    private void observeRun() throws Exception {

	try {
	    while (this.checkTimeout()) {
		boolean b = false;

		try {
		    b = this.doCheck();
		} catch (Throwable ex) {
		    throw new Exception(this.makeExceptionMessage("doCheck", ex));
		}
		if (b) {
		    this.sendRemoteMessage(NOTIFICATION, this.MESSAGE);
		    if (!this._stay) {
			break;
		    }
		}
		this.gotoSleep(this._interval);
	    }
	    this.sendRemoteMessage(EXPIRY, new String("Stay/Duration-time has expired"));
	} catch (Exception ex) {
	    System.out.println(ex.getMessage());
	    throw new Exception(this.makeExceptionMessage(null, ex));
	}
    }

    /**
     * Initializes the notifier. Called only the first time this notifier is
     * created. The initialization argument includes the needed parameters for
     * the checks as defined in
     * 
     * <pre>
     * create
     * </pre>
     * 
     * .
     * 
     * @param obj
     *            the initialization argument.
     * @exception AgletException
     *                if the initialization fails.
     */
    @Override
    public synchronized void onCreation(Object object) {
	Arguments obj = (Arguments) object;

	this._master = (AgletID) (obj.getArg("receiver"));
	this._destination = (String) (obj.getArg("destination"));
	this._interval = ((Double) (obj.getArg("interval"))).doubleValue();
	this._duration = ((Double) (obj.getArg("duration"))).doubleValue();
	this._stay = ((Boolean) (obj.getArg("stay"))).booleanValue();
	this.ARGUMENT = obj.getArg("agrument");

	this.origin = this.getAgletContext().getHostingURL().toString();

	try {
	    SimpleItinerary itin = new SimpleItinerary(this);

	    itin.go(this._destination, new Message("start", null));
	} catch (AgletException ex) {
	    ex.printStackTrace();
	    this.dispose();
	} catch (IOException ex) {
	    ex.printStackTrace();
	    this.dispose();
	}
    }

    // -- Notifies the master.
    //
    private void sendRemoteMessage(int type, Object data)
    throws IOException,
    AgletException {
	Arguments args = new Arguments();

	args.setArg("message", data);
	args.setArg("date", new Date());
	args.setArg("type", type);

	// Messenger.create(getAgletContext(), new URL(origin), _master, new
	// Message("notification", args));
	// AgletProxy ap = getAgletContext().getAgletProxy(new URL(origin),
	// _master);
	// ap.sendMessage(new Message("notification", args));
	URL org = new URL(this.origin);

	Messenger.create(this.getAgletContext(), org, org, this._master, new Message("notification", args));
    }

    private void start() {
	try {
	    this.observeInit();
	    this.observeRun();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    try {
		this.sendRemoteMessage(EXCEPTION, ex.getMessage());
	    } catch (Exception ex1) {
		ex1.printStackTrace();
	    }
	}
	this.dispose();
    }

    // -- Defines the start time of checks.
    //
    private void startTimer() {
	this._startingTime = (new Date()).getTime();
    }

    private synchronized void waitInHours(double h) {
	try {
	    this.wait((int) (h * 3600));
	} catch (InterruptedException ex) {
	}
    }
}
