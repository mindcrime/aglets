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

import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;

import com.ibm.aglets.AgletRuntime;
import com.ibm.aglets.AgletsSecurityException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.FileUtils;

// import com.ibm.aglets.agletbox.Polling;

import java.security.Identity;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Hashtable;

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;

import java.net.URL;

/**
 * Tahiti is the viewer for aglets
 * 
 * @version     1.10    $Date: 2009/07/27 10:31:40 $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */

public final class Tahiti implements ContextListener {

	// tentative
	static boolean enableBox = false;

	private static UserManager _userManager = new TahitiUserManager();
	static String default_resources[][] = {
		 {
			"tahiti.background", 
			String.valueOf(java.awt.Color.lightGray.getRGB())
		}, {
			"tahiti.titleFont", "Helvetica-bold-16"
		}, {
			"tahiti.font", "Helvetica-plain-12"
		}, {
			"tahiti.fixedFont", "Courier-plain-12"
		}, 
	};

	/*
	 * The aglet context
	 */
	static AgletContext CONTEXT = null;

	/*
	 * A thread to poll aglets from an agletbox.
	 */

	// static Polling POLLING = null;

	/*
	 * The main window
	 */
	private MainWindow _window = null;

	boolean reboot = false;

	/*
	 * Constructs Tahiti.
	 */
	public Tahiti() {}
	public void agletActivated(ContextEvent event) {
		//_window.updateProxyInList(event.getAgletProxy());
	}
	public void agletArrived(ContextEvent event) {
		_window.insertProxyToList(event.getAgletProxy());
	}
	public void agletCloned(ContextEvent event) {
		_window.insertProxyToList(event.getAgletProxy());
	}
	/*
	 * ContextEvent callbacks
	 */
	public void agletCreated(ContextEvent event) {
		_window.insertProxyToList(event.getAgletProxy());
	}
	public void agletDeactivated(ContextEvent event) {
		//_window.updateProxyInList(event.getAgletProxy());

		// _window.removeProxyList(event.getAgletProxy());
	}
	public void agletDispatched(ContextEvent event) {
		_window.removeProxyFromList(event.getAgletProxy());
	}
	public void agletDisposed(ContextEvent event) {
		_window.removeProxyFromList(event.getAgletProxy());
	}
	public void agletResumed(ContextEvent event) {
		//_window.updateProxyInList(event.getAgletProxy());
	}
	public void agletReverted(ContextEvent event) {
		_window.removeProxyFromList(event.getAgletProxy());
	}
	public void agletStateChanged(ContextEvent event) {
		AgletProxy proxy = event.getAgletProxy();
		String text = event.getText();

		_window.text.put(proxy, text);
		//_window.updateProxyInList(proxy);
	}
	public void agletSuspended(ContextEvent event) {
		//_window.updateProxyInList(event.getAgletProxy());

		// _window.removeProxyList(event.getAgletProxy());
	}
	/**
	 * shutdowned
	 */
	public void contextShutdown(ContextEvent event) {}
	public void contextStarted(ContextEvent event) {
		CONTEXT = event.getAgletContext();
		final Tahiti tahiti = this;
		final Resource tahiti_res = Resource.getResourceFor("tahiti");

		_window = 
			(MainWindow)AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {

				// Create Tahiti's main window.
				MainWindow w = new MainWindow(tahiti);

				w.pack();
				java.awt.Dimension d = w.getToolkit().getScreenSize();
				java.awt.Dimension wsize = w.getSize();
				int intx = tahiti_res.getInteger("tahiti.window.x", 
												 (d.width - wsize.width) / 2);
				int inty = tahiti_res.getInteger("tahiti.window.y", 
												 (d.height - wsize.height) 
												 / 2);

				w.setLocation(intx, inty);
				w.show();
				w.toFront();
				return w;
			} 
		});
		Enumeration e = CONTEXT.getAgletProxies();

		while (e.hasMoreElements()) {
			_window.insertProxyToList((AgletProxy)e.nextElement());
		} 
		if (enableBox) {
			int update = 
				Resource.getResourceFor("aglets")
					.getInteger("aglets.box.update", 0);

			// POLLING = new Polling(CONTEXT,update);
			// POLLING.start();
		} 
	}
	void exit() {

		AgletRuntime.getAgletRuntime().shutdown();

		// keep the size of window.
		_window.saveSize();

		Resource res = Resource.getResourceFor("tahiti");

		res.save("Tahiti");

		System.exit(reboot == true ? 0 : 1);
	}
	public final static UserManager getUserManager() {
		return _userManager;
	}
	public static void init() {
		com.ibm.aglet.system.AgletRuntime runtime = 
			AgletRuntime.getAgletRuntime();

		if (runtime == null) {
			return;
		} 
		String username = runtime.getOwnerName();

		if (username == null) {
			return;
		} 
		Properties system_props = 
			(Properties)AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return System.getProperties();
			} 
		});

		try {
			String propfile = FileUtils.getPropertyFilenameForUser(username, 
					"tahiti");

			Resource.createResource("tahiti", propfile, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		Resource res = Resource.getResourceFor("tahiti");

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

		if (aglet_home.charAt(aglet_home.length() - 1) 
				!= File.separatorChar) {
			aglet_home += File.separator;
		} 
		String cmd = "openurl";

		if (File.separator.equals("\\")) {
			cmd = "openurl.bat";
		} 
		res.setDefaultResource("tahiti.browser_command", 
							   aglet_home + "bin" + File.separator + cmd);
        
        res.importOptionProperties("tahiti.window");
	}
	public static void initializeGUI() {
		try {
			Class.forName("sun.awt.image.JPEGImageDecoder");
			Class.forName("sun.audio.AudioPlayer");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		try {
			Class.forName("sun.awt.PlatformFont");		// for 1.1
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		try {
			new java.awt.Frame().addNotify();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 

		try {
			Class.forName("sun.awt.ScreenUpdater");
		} catch (Throwable t) {}
	}
	public static void installFactories() {
		AgletRuntime
			.setDefaultResourceManagerFactory(new ResourceManagerFactory());
		AgletRuntime.setPersistenceFactory(new PersistenceFactory());
	}
	public static void installSecurity() {
		try {

			// IntrospectorImpl impl = new IntrospectorImpl();
			// AccessController.setIntrospector(impl);
			// System.setSecurityManager(impl);
			SecurityManager sm = new AgletsSecurityManager();

			System.setSecurityManager(sm);
		} catch (AgletsSecurityException ex) {
			throw ex;
		} catch (SecurityException ex) {}
	}
	static public void main(String a[]) {
		MainWindow main = new MainWindow(new Tahiti());

		main.pack();
		main.show();
	}
	/*
	 * The exit method.
	 */
	void reboot() {
		reboot = true;
		exit();
	}
	public void showDocument(ContextEvent event) {
		_window.showURL(event.getDocumentURL().toString());
	}
	public void showMessage(ContextEvent event) {
		String l = event.getMessage();

		_window.logWindow.appendText(l);
		_window.setMessage(l);
	}
}
