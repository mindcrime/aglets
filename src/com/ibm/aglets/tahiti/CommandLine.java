package com.ibm.aglets.tahiti;

/*
 * @(#)CommandLine.java
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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.awb.misc.Resource;

/**
 * <tt>CommandLine</tt> is a simple command line interface to an aglet server.
 * 
 * @see com.ibm.aglet.system.ContextListener
 * @see com.ibm.aglet.system.ContextEvent
 * @version 1.10 $Date: 2009/07/28 07:04:52 $
 * @author Mitsuru Oshima
 */
public final class CommandLine implements ContextListener, Runnable {

	private static UserManager _userManager = new CommandLineUserManager();
	private static String helpMsg = "help                    Display this message. \n"
		+ "shutdown                Shutdown the server. \n"
		+ "reboot                  Reboot the server. \n"
		+ "list                    List all aglets in the server. \n"
		+ "prompt                  Display or changes the prompt. \n"
		+ "msg on|off              Message printing on/off. \n"
		+ "create [codeBase] name  Create new aglet. \n"
		+ "<aglet> dispatch URL    Dispatch the aglet to the URL. \n"
		+ "<aglet> clone           Clone the aglet. \n"
		+ "<aglet> dispose         Dispose the aglet. \n"
		+ "<aglet> dialog          Request a dialog to interact with.\n"
		+ "<aglet> property        Display properties of the aglet.\n"
		+ "Note: <aglet> is a left most string listed in the result of list command. ";

	/*
	 * for autonumbering aglets
	 */
	private static int serial = 0;

	public final static UserManager getUserManager() {
		return _userManager;
	}

	/*
	 * Aglets table
	 */
	private final Hashtable aglets = new Hashtable();

	/*
	 * Prompt char.
	 */
	private String prompt = ">";

	/*
	 * AgletContext
	 */
	private AgletContext context = null;

	/*
	 * Null print stream to turn off messages. private
	 * com.ibm.awb.misc.LogPrintStream null_log;
	 */
	private boolean message = true;

	synchronized private void added(final AgletProxy proxy) {
		aglets.put("aglet" + (serial++), new Item(proxy));
	}

	/**
	 * Called when an aglet has been activated
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletActivated(final ContextEvent event) {
		if (message) {
			System.out.println("Activated " + event.getAgletProxy());
		}

		// added(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has arived
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletArrived(final ContextEvent event) {
		added(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been cloned
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletCloned(final ContextEvent event) {
		added(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been created
	 * 
	 * @param event
	 *            an AgletEvent
	 */
	@Override
	public void agletCreated(final ContextEvent event) {
		added(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been deactivated
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletDeactivated(final ContextEvent event) {
		if (message) {
			System.out.println("Deactivated " + event.getAgletProxy());
		}

		// removed(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been dispatched
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletDispatched(final ContextEvent event) {

		// event.getAgletProxy() is the AgletProxy AFTER dispatching,
		// not BEFORE dispatching.
		// So it cannot be removed because aglets hashtable doesn't contain it.
		// removed(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been disposed
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletDisposed(final ContextEvent event) {
		this.removed(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been resumed
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletResumed(final ContextEvent event) {
		if (message) {
			System.out.println("Resumed " + event.getAgletProxy());
		}

		// added(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been reverted
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletReverted(final ContextEvent event) {
		this.removed(event.getAgletProxy());
	}

	/**
	 * Called when an aglet has been updated
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletStateChanged(final ContextEvent event) {
		synchronized (aglets) {
			final Enumeration e = aglets.keys();

			while (e.hasMoreElements()) {
				final Object k = e.nextElement();
				final Item i = (Item) aglets.get(k);

				if (i.equals(event.getAgletProxy())) {
					i.setText((String) event.arg);
					if (message) {
						System.out.println(k.toString() + " : " + event.arg);
					}
				}
			}
		}
	}

	/**
	 * Called when an aglet has been suspended
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void agletSuspended(final ContextEvent event) {
		if (message) {
			System.out.println("Suspended " + event.getAgletProxy());
		}

		// removed(event.getAgletProxy());
	}

	/**
	 * Interprets inputs from command line.
	 * 
	 * @param line
	 *            typed string
	 */
	void command(final String line) throws Exception {
		Item item = null;
		final StringTokenizer st = new StringTokenizer(line, " \t");

		if (st.hasMoreTokens()) {
			final String cmd = st.nextToken();

			if ("shutdown".equalsIgnoreCase(cmd)) {
				AgletRuntime.getAgletRuntime().shutdown();
				System.exit(1);
			} else if ("reboot".equalsIgnoreCase(cmd)) {
				AgletRuntime.getAgletRuntime().shutdown();
				System.exit(0);
			} else if ("list".equalsIgnoreCase(cmd)) {
				list(System.out);
			} else if ("threads".equalsIgnoreCase(cmd)) {
				MainWindow.dumpThreads(System.out);
			} else if ("debug".equalsIgnoreCase(cmd)) {
				if (st.hasMoreTokens()) {
					if ("on".equalsIgnoreCase(st.nextToken())) {
						com.ibm.awb.misc.Debug.debug(true);
					} else {
						com.ibm.awb.misc.Debug.debug(false);
					}
				} else {
					com.ibm.awb.misc.Debug.list(System.err);
				}
			} else if ("msg".equalsIgnoreCase(cmd)) {
				if (st.hasMoreTokens()) {
					if ("on".equalsIgnoreCase(st.nextToken())) {
						message = true;
					} else {
						message = false;
					}
				} else {
					System.out.println(message ? "message on"
							: "message off");
				}
			} else if ("prompt".equalsIgnoreCase(cmd)) {
				if (st.hasMoreTokens()) {
					prompt = st.nextToken();
				} else {
					System.out.println("prompt : " + prompt);
				}
			} else if ("help".equalsIgnoreCase(cmd)) {
				System.out.println(helpMsg);
			} else if ("create".equalsIgnoreCase(cmd)) {
				URL url = null;
				String name = "";

				if (st.countTokens() == 2) {
					url = new URL(st.nextToken());
					name = st.nextToken();
				} else if (st.countTokens() == 1) {
					name = st.nextToken();
				} else {
					System.out.println("create [URL] name");
					return;
				}
				context.createAglet(url, name, null);
			} else if ((item = (Item) aglets.get(cmd)) != null) {
				if (st.hasMoreTokens()) {
					item.command(st.nextToken(), st);
					if (!item.isValid()) {
						this.removed(item);
						System.out.println("Removed : " + cmd);
					}
				} else {
					System.out.println(item);
				}
			} else {
				System.out.println("unknown command : " + cmd);
			}
		}
	}

	@Override
	public void contextShutdown(final ContextEvent ev) {

		//
	}

	/**
	 * Initializes the Viewer with the context.
	 * 
	 * @param ev
	 *           event containing the AgletContext.
	 */
	@Override
	public void contextStarted(final ContextEvent ev) {
		context = ev.getAgletContext();
		final Resource res = Resource.getResourceFor("aglets");

		//
		// Check to see if this is a registered user.
		//
		if (res.getBoolean("aglets.registered", false) == false) {
		}

		new Thread(this).start();
	}

	/**
	 * Prints out the list of aglets
	 */
	void list(final PrintStream p) throws Exception {
		final Enumeration e = aglets.keys();

		while (e.hasMoreElements()) {
			final String k = (String) e.nextElement();
			final Item item = (Item) aglets.get(k);

			// if (item.isValid()) {
			p.println(k + " [" + item.proxy.getAgletClassName() + "] "
					+ item.text);

			// }
		}
	}

	synchronized private void removed(final AgletProxy proxy) {
		final Enumeration e = aglets.keys();

		while (e.hasMoreElements()) {
			final Object k = e.nextElement();

			if (aglets.get(k).equals(proxy)) {
				aglets.remove(k);
			}
		}
	}

	synchronized private void removed(final Item item) {
		this.removed(item.proxy);
	}

	/**
	 * Infinite loop to process inputs
	 */
	@Override
	public void run() {
		Thread.currentThread().setPriority(1);
		final DataInput in = new DataInputStream(System.in);

		while (true) {
			try {
				System.out.print(prompt + " ");
				System.out.flush();
				final String line = in.readLine();

				command(line);
			} catch (final Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Called when an aglet request to show the document given as URL
	 * 
	 * @see com.ibm.aglets.tahiti.Tahiti#showDocument
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void showDocument(final ContextEvent event) {
		if (message) {
			System.out.println("hyper link required :" + event.arg);
		}
	}

	/**
	 * Called to show the message
	 * 
	 * @param event
	 *            an ContextEvent
	 */
	@Override
	public void showMessage(final ContextEvent event) {
		if (message) {
			System.out.println((String) event.arg);
		}
	}
}
