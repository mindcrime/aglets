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

import org.aglets.log.AgletsLogger;

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

    /*
     * The main window
     */
    private MainWindow _window = null;

    boolean reboot = false;

    private AgletsLogger logger = AgletsLogger.getLogger(this.getClass().getName());

    /*
     * Constructs Tahiti.
     */
    public Tahiti() {
    }

    @Override
    public void agletActivated(ContextEvent event) {
	this._window.updateProxyInList(event.getAgletProxy());
    }

    @Override
    public void agletArrived(ContextEvent event) {
	this.logger.debug("{EVENT} AgletArrived event " + event);
	this._window.insertProxyToList(event.getAgletProxy());
    }

    @Override
    public void agletCloned(ContextEvent event) {
	this.logger.debug("{EVENT} Aglet cloned event " + event);
	this._window.insertProxyToList(event.getAgletProxy());
    }

    /*
     * ContextEvent callbacks
     */
    @Override
    public void agletCreated(ContextEvent event) {
	this.logger.debug("{EVENT} Aglet created event " + event);
	this._window.insertProxyToList(event.getAgletProxy());
    }

    @Override
    public void agletDeactivated(ContextEvent event) {
	this._window.updateProxyInList(event.getAgletProxy());

	// _window.removeProxyList(event.getAgletProxy());
    }

    @Override
    public void agletDispatched(ContextEvent event) {
	this._window.removeProxyFromList(event.getAgletProxy());
    }

    @Override
    public void agletDisposed(ContextEvent event) {
	this._window.removeProxyFromList(event.getAgletProxy());
    }

    @Override
    public void agletResumed(ContextEvent event) {
	this._window.updateProxyInList(event.getAgletProxy());
    }

    @Override
    public void agletReverted(ContextEvent event) {
	this._window.removeProxyFromList(event.getAgletProxy());
    }

    @Override
    public void agletStateChanged(ContextEvent event) {
	AgletProxy proxy = event.getAgletProxy();
	String text = event.getText();

	this._window.text.put(proxy, text);
	this._window.updateProxyInList(proxy);
    }

    @Override
    public void agletSuspended(ContextEvent event) {
	this._window.updateProxyInList(event.getAgletProxy());

	// _window.removeProxyList(event.getAgletProxy());
    }

    /**
     * shutdowned
     */
    @Override
    public void contextShutdown(ContextEvent event) {
    }

    @Override
    public void contextStarted(ContextEvent event) {
	CONTEXT = event.getAgletContext();
	final Tahiti tahiti = this;
	final Resource tahiti_res = Resource.getResourceFor("tahiti");

	this._window = (MainWindow) AccessController.doPrivileged(new PrivilegedAction() {
	    @Override
	    public Object run() {

		// Create Tahiti's main window.
		MainWindow w = new MainWindow(tahiti);

		w.pack();
		java.awt.Dimension d = w.getToolkit().getScreenSize();
		java.awt.Dimension wsize = w.getSize();
		int intx = tahiti_res.getInteger("tahiti.window.x", (d.width - wsize.width) / 2);
		int inty = tahiti_res.getInteger("tahiti.window.y", (d.height - wsize.height) / 2);

		w.setLocation(intx, inty);
		w.show();
		w.toFront();
		return w;
	    }
	});
	Enumeration e = CONTEXT.getAgletProxies();

	while (e.hasMoreElements()) {
	    this._window.insertProxyToList((AgletProxy) e.nextElement());
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

	Resource res = Resource.getResourceFor("tahiti");

	res.save("Tahiti");
	this.logger.debug("Tahiti exiting (reboot = " + this.reboot + ")");
	System.exit(this.reboot == true ? 0 : 1);
    }

    public final static UserManager getUserManager() {
	return _userManager;
    }

    public static void init() {
	com.ibm.aglet.system.AgletRuntime runtime = com.ibm.aglet.system.AgletRuntime.getAgletRuntime();

	if (runtime == null) {
	    return;
	}
	String username = runtime.getOwnerName();

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
	    String propfile = FileUtils.getPropertyFilenameForUser(username, "tahiti");

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
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	try {
	    Class.forName("sun.awt.PlatformFont"); // for 1.1
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
	} catch (Throwable t) {
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
	    SecurityManager sm = new AgletsSecurityManager();

	    System.setSecurityManager(sm);
	} catch (AgletsSecurityException ex) {
	    throw ex;
	} catch (SecurityException ex) {
	}
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
	this.reboot = true;
	this.exit();
    }

    @Override
    public void showDocument(ContextEvent event) {
	this._window.showURL(event.getDocumentURL().toString());
    }

    @Override
    public void showMessage(ContextEvent event) {
	String l = event.getMessage();

	if ((this._window != null) && (this._window.logWindow != null)) {
	    this._window.logWindow.appendText(l);
	    this._window.setMessage(l);
	}
    }
}
