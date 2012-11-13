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

	/**
	 * 
	 */
	private static final long serialVersionUID = 4167545259197191876L;
	public final static int NOTIFICATION = 0;
	public final static int EXPIRY = 1;
	public final static int EXCEPTION = 2;

	// -- minimum time interval (in hours) above which the notifier
	// -- is deactivated between two successive checks.
	private static final double MIN_TIME_OF_DEACTIVATION = 1.0 / 120.0;

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
	                                final URL url,
	                                final String source,
	                                final AgletContext context,
	                                final Aglet master,
	                                final URL destination,
	                                final double interval,
	                                final double duration,
	                                final boolean stay,
	                                final Object argument)
	throws IOException,
	AgletException {
		final Arguments args = new Arguments();

		args.setArg("destination", destination.toString());
		args.setArg("interval", interval);
		args.setArg("duration", duration);
		args.setArg("stay", stay);
		args.setArg("agrument", argument);
		args.setArg("receiver", master.getAgletID());

		try {
			return context.createAglet(url, source, args);
		} catch (final InstantiationException ex) {
			throw new AgletException(ex.getClass().getName() + ':'
					+ ex.getMessage());
		} catch (final ClassNotFoundException ex) {
			throw new AgletException(ex.getClass().getName() + ':'
					+ ex.getMessage());
		}
	}

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
		return (_startingTime + (long) (_duration * 3600000)) > (new Date()).getTime();
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

	private void gotoSleep(final double hours) throws AgletException {
		if (hours > MIN_TIME_OF_DEACTIVATION) {
			try {
				deactivate((int) (hours * 60));
			} catch (final IOException ae) {
				throw new AgletException("deactivation has been failed!!");
			}
		} else {
			waitInHours(hours); // -- busy wait
		}
	}

	@Override
	public boolean handleMessage(final Message msg) {
		try {
			if (msg.sameKind("start")) {
				start();
			} else {
				return false;
			}
		} catch (final Exception ex) {
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

	private String makeExceptionMessage(final String phase, final Throwable ex) {
		final String host = getAgletContext().getHostingURL().toString();

		return new String(host + ":" + "<" + ex.getClass().getName() + "::"
				+ ex.getMessage() + ">;"
				+ ((phase != null) ? "DURING " + phase : "Internal"));
	}

	// -- defines initialization before the checks begin.
	//
	private void observeInit() throws Exception {
		try {
			initializeCheck();
		} catch (final Throwable ex) {
			throw new Exception(makeExceptionMessage("initializeCheck", ex));
		}
		startTimer();
	}

	// -- Performs repeated checks.
	//
	private void observeRun() throws Exception {

		try {
			while (checkTimeout()) {
				boolean b = false;

				try {
					b = doCheck();
				} catch (final Throwable ex) {
					throw new Exception(makeExceptionMessage("doCheck", ex));
				}
				if (b) {
					sendRemoteMessage(NOTIFICATION, MESSAGE);
					if (!_stay) {
						break;
					}
				}
				gotoSleep(_interval);
			}
			sendRemoteMessage(EXPIRY, new String("Stay/Duration-time has expired"));
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
			throw new Exception(makeExceptionMessage(null, ex));
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
	 * @param object
	 *            the initialization argument.
	 * @exception AgletException
	 *                if the initialization fails.
	 */
	@Override
	public synchronized void onCreation(final Object object) {
		final Arguments obj = (Arguments) object;

		_master = (AgletID) (obj.getArg("receiver"));
		_destination = (String) (obj.getArg("destination"));
		_interval = ((Double) (obj.getArg("interval"))).doubleValue();
		_duration = ((Double) (obj.getArg("duration"))).doubleValue();
		_stay = ((Boolean) (obj.getArg("stay"))).booleanValue();
		ARGUMENT = obj.getArg("agrument");

		origin = getAgletContext().getHostingURL().toString();

		try {
			final SimpleItinerary itin = new SimpleItinerary(this);

			itin.go(_destination, new Message("start", null));
		} catch (final AgletException ex) {
			ex.printStackTrace();
			dispose();
		} catch (final IOException ex) {
			ex.printStackTrace();
			dispose();
		}
	}

	// -- Notifies the master.
	//
	private void sendRemoteMessage(final int type, final Object data)
	throws IOException,
	AgletException {
		final Arguments args = new Arguments();

		args.setArg("message", data);
		args.setArg("date", new Date());
		args.setArg("type", type);

		// Messenger.create(getAgletContext(), new URL(origin), _master, new
		// Message("notification", args));
		// AgletProxy ap = getAgletContext().getAgletProxy(new URL(origin),
		// _master);
		// ap.sendMessage(new Message("notification", args));
		final URL org = new URL(origin);

		Messenger.create(getAgletContext(), org, org, _master, new Message("notification", args));
	}

	private void start() {
		try {
			observeInit();
			observeRun();
		} catch (final Exception ex) {
			ex.printStackTrace();
			try {
				sendRemoteMessage(EXCEPTION, ex.getMessage());
			} catch (final Exception ex1) {
				ex1.printStackTrace();
			}
		}
		dispose();
	}

	// -- Defines the start time of checks.
	//
	private void startTimer() {
		_startingTime = (new Date()).getTime();
	}

	private synchronized void waitInHours(final double h) {
		try {
			this.wait((int) (h * 3600));
		} catch (final InterruptedException ex) {
		}
	}
}
