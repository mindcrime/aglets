package com.ibm.aglets.tahiti;

/*
 * @(#)Tahiti.java
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

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglets.AgletRuntime;
import com.ibm.aglets.AgletsSecurityException;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.Resource;

/**
 * Tahiti is the viewer for aglets
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:52 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */

public final class Tahiti implements ContextListener {

	// tentative
	static boolean enableBox = false;

	private static UserManager _userManager = new TahitiUserManager();
	static String default_resources[][] = {
		{ "tahiti.background",
			String.valueOf(java.awt.Color.lightGray.getRGB()) },
			{ "tahiti.titleFont", "Helvetica-bold-16" },
			{ "tahiti.font", "Helvetica-plain-12" },
			{ "tahiti.fixedFont", "Courier-plain-12" }, };

	/*
	 * The aglet context
	 */
	static AgletContext CONTEXT = null;

	/*
	 * A thread to poll aglets from an agletbox.
	 */

	// static Polling POLLING = null;

	public final static UserManager getUserManager() {
		return _userManager;
	}

	public static void init() {
		final com.ibm.aglet.system.AgletRuntime runtime = com.ibm.aglet.system.AgletRuntime.getAgletRuntime();

		if (runtime == null) {
			return;
		}
		final String username = runtime.getOwnerName();

		if (username == null) {
			return;
		}
		AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				return System.getProperties();
			}
		});

		try {
			final String propfile = FileUtils.getPropertyFilenameForUser(username, "tahiti");

			Resource.createResource("tahiti", propfile, null);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		final Resource res = Resource.getResourceFor("tahiti");

		/*
		 * Initializes the system properties with default values.
		 */
		res.setResource("tahiti.version", "Tahiti Aglets Server: 1.0b5");

		/*
		 * Set default resource values
		 */
		res.setDefaultResources(default_resources);

		/* box */
		Resource aglets_res = Resource.getResourceFor("aglets");

		enableBox = aglets_res.getBoolean("aglets.enableBox", false);

		/* browser */
		aglets_res = Resource.getResourceFor("aglets");
		String aglet_home = aglets_res.getString("aglets.home", ".");

		if (aglet_home.charAt(aglet_home.length() - 1) != File.separatorChar) {
			aglet_home += File.separator;
		}
		String cmd = "openurl";

		if (File.separator.equals("\\")) {
			cmd = "openurl.bat";
		}
		res.setDefaultResource("tahiti.browser_command", aglet_home + "bin"
				+ File.separator + cmd);

		res.importOptionProperties("tahiti.window");
	}

	public static void initializeGUI() {
		try {
			Class.forName("sun.awt.image.JPEGImageDecoder");
			Class.forName("sun.audio.AudioPlayer");
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		try {
			Class.forName("sun.awt.PlatformFont"); // for 1.1
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		try {
			new java.awt.Frame().addNotify();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		try {
			Class.forName("sun.awt.ScreenUpdater");
		} catch (final Throwable t) {
		}
	}

	public static void installFactories() {
		AgletRuntime.setDefaultResourceManagerFactory(new ResourceManagerFactory());
		AgletRuntime.setPersistenceFactory(new PersistenceFactory());
	}

	public static void installSecurity() {
		try {

			// IntrospectorImpl impl = new IntrospectorImpl();
			// AccessController.setIntrospector(impl);
			// System.setSecurityManager(impl);
			final SecurityManager sm = new AgletsSecurityManager();

			System.setSecurityManager(sm);
		} catch (final AgletsSecurityException ex) {
			throw ex;
		} catch (final SecurityException ex) {
		}
	}

	static public void main(final String a[]) {
		final MainWindow main = new MainWindow(new Tahiti());

		main.pack();
		main.show();
	}

	/*
	 * The main window
	 */
	private MainWindow _window = null;

	boolean reboot = false;

	private final AgletsLogger logger = AgletsLogger.getLogger(this.getClass().getName());

	/*
	 * Constructs Tahiti.
	 */
	public Tahiti() {
	}

	@Override
	public void agletActivated(final ContextEvent event) {
		_window.updateProxyInList(event.getAgletProxy());
	}

	@Override
	public void agletArrived(final ContextEvent event) {
		logger.debug("{EVENT} AgletArrived event " + event);
		_window.insertProxyToList(event.getAgletProxy());
	}

	@Override
	public void agletCloned(final ContextEvent event) {
		logger.debug("{EVENT} Aglet cloned event " + event);
		_window.insertProxyToList(event.getAgletProxy());
	}

	/*
	 * ContextEvent callbacks
	 */
	@Override
	public void agletCreated(final ContextEvent event) {
		logger.debug("{EVENT} Aglet created event " + event);
		_window.insertProxyToList(event.getAgletProxy());
	}

	@Override
	public void agletDeactivated(final ContextEvent event) {
		_window.updateProxyInList(event.getAgletProxy());

		// _window.removeProxyList(event.getAgletProxy());
	}

	@Override
	public void agletDispatched(final ContextEvent event) {
		_window.removeProxyFromList(event.getAgletProxy());
	}

	@Override
	public void agletDisposed(final ContextEvent event) {
		_window.removeProxyFromList(event.getAgletProxy());
	}

	@Override
	public void agletResumed(final ContextEvent event) {
		_window.updateProxyInList(event.getAgletProxy());
	}

	@Override
	public void agletReverted(final ContextEvent event) {
		_window.removeProxyFromList(event.getAgletProxy());
	}

	@Override
	public void agletStateChanged(final ContextEvent event) {
		final AgletProxy proxy = event.getAgletProxy();
		final String text = event.getText();

		_window.text.put(proxy, text);
		_window.updateProxyInList(proxy);
	}

	@Override
	public void agletSuspended(final ContextEvent event) {
		_window.updateProxyInList(event.getAgletProxy());

		// _window.removeProxyList(event.getAgletProxy());
	}

	/**
	 * shutdowned
	 */
	@Override
	public void contextShutdown(final ContextEvent event) {
	}

	@Override
	public void contextStarted(final ContextEvent event) {
		CONTEXT = event.getAgletContext();
		final Tahiti tahiti = this;
		final Resource tahiti_res = Resource.getResourceFor("tahiti");

		_window = (MainWindow) AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {

				// Create Tahiti's main window.
				final MainWindow w = new MainWindow(tahiti);

				w.pack();
				final java.awt.Dimension d = w.getToolkit().getScreenSize();
				final java.awt.Dimension wsize = w.getSize();
				final int intx = tahiti_res.getInteger("tahiti.window.x", (d.width - wsize.width) / 2);
				final int inty = tahiti_res.getInteger("tahiti.window.y", (d.height - wsize.height) / 2);

				w.setLocation(intx, inty);
				w.show();
				w.toFront();
				return w;
			}
		});
		final Enumeration e = CONTEXT.getAgletProxies();

		while (e.hasMoreElements()) {
			_window.insertProxyToList((AgletProxy) e.nextElement());
		}
		if (enableBox) {
			Resource.getResourceFor("aglets").getInteger("aglets.box.update", 0);

			// POLLING = new Polling(CONTEXT,update);
			// POLLING.start();
		}
	}

	void exit() {

		com.ibm.aglet.system.AgletRuntime.getAgletRuntime().shutdown();

		// keep the size of window.
		// _window.saveSize();

		final Resource res = Resource.getResourceFor("tahiti");

		res.save("Tahiti");
		logger.debug("Tahiti exiting (reboot = " + reboot + ")");
		System.exit(reboot == true ? 0 : 1);
	}

	/*
	 * The exit method.
	 */
	void reboot() {
		reboot = true;
		exit();
	}

	@Override
	public void showDocument(final ContextEvent event) {
		_window.showURL(event.getDocumentURL().toString());
	}

	@Override
	public void showMessage(final ContextEvent event) {
		final String l = event.getMessage();

		if ((_window != null) && (_window.logWindow != null)) {
			_window.logWindow.appendText(l);
			_window.setMessage(l);
		}
	}
}
