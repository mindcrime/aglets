package com.ibm.aglets.tahiti;

/*
 * @(#)MainWindow.java
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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aglets.util.gui.GUICommandStrings;
import org.aglets.util.gui.JComponentBuilder;
import org.aglets.util.gui.WindowManager;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglets.AgletProxyImpl;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.aglets.thread.AgletThreadPool;
import com.ibm.awb.misc.Resource;

/**
 * The <tt>MainWindow</tt> represents the main window for the Tahiti aglet
 * viewer.
 * 
 * @version 1.08 $Date: 2009/07/28 07:04:52 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 * @author Yoshiaki Mima
 */

/*
 * Aglets List
 * 
 * Ordered by Keys "Creation time", "Class name"
 */

public final class MainWindow extends TahitiWindow implements ItemListener,
	ActionListener, ListSelectionListener {
    static ResourceBundle bundle = null;

    static {
	bundle = (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		return ResourceBundle.getBundle("tahiti");
	    }
	});
    }

    /*
     * Aglet menu - menu items and shortcut buttons.
     */
    private MenuItem _dialogMenuItem = new MenuItem();
    private MenuItem _disposeMenuItem = new MenuItem();
    private MenuItem _cloneMenuItem = new MenuItem();
    private MenuItem _infoMenuItem = new MenuItem();
    private MenuItem _killMenuItem = new MenuItem();

    //
    private Button _disposeButton = new Button();
    private Button _dialogButton = new Button();
    private Button _cloneButton = new Button();
    private Button _infoButton = new Button();

    /*
     * Mobility menu - menu items and shortcut buttons.
     */
    private MenuItem _dispatchMenuItem = new MenuItem();
    private Button _dispatchButton = new Button();

    //
    private MenuItem _deactivateMenuItem = new MenuItem();
    private MenuItem _activateMenuItem = new MenuItem();

    //
    private MenuItem _javaConsoleMenuItem = new MenuItem();

    //
    private Tahiti _tahiti = null;

    //
    private Vector _itemList = new Vector();
    Hashtable text = new Hashtable();

    //
    Panel _buttonPanel = new Panel();
    List _agletList = new List(0, false);

    /* display options */
    static final int ORDER_CREATIONTIME = 0;
    static final int ORDER_CLASSNAME = 1;

    /* package */
    LogWindow logWindow = null;

    /*
     * Tahiti event constants
     */
    static final int TAHITI_EVENT = 10000;
    static final int CLONE = TAHITI_EVENT + 1;
    static final int CREATE = TAHITI_EVENT + 2;
    static final int DISPOSE = TAHITI_EVENT + 3;
    static final int DISPATCH = TAHITI_EVENT + 4;
    static final int RETRACT = TAHITI_EVENT + 5;
    static final int KILL = TAHITI_EVENT + 6;
    static final int DEACTIVATE = TAHITI_EVENT + 7;
    static final int ACTIVATE = TAHITI_EVENT + 8;
    static final int DIALOG = TAHITI_EVENT + 9;
    static final int GET_AGLETS = TAHITI_EVENT + 10;

    //
    static final int EXIT = TAHITI_EVENT + 15;
    static final int SHUTDOWN = TAHITI_EVENT + 16;
    static final int REBOOT = TAHITI_EVENT + 17;

    //
    static final int AGLET_INFO = TAHITI_EVENT + 20;
    static final int SHOW_LOG = TAHITI_EVENT + 21;
    static final int MEMORY_USAGE = TAHITI_EVENT + 22;
    static final int GC = TAHITI_EVENT + 23;
    static final int SHOW_THREADS = TAHITI_EVENT + 24;
    static final int SHOW_DEBUG = TAHITI_EVENT + 25;
    static final int SHOW_REFTABLE = TAHITI_EVENT + 26;
    static final int SHOW_JAVACON = TAHITI_EVENT + 27;

    //
    static final int PREFERENCE1 = TAHITI_EVENT + 30;
    static final int PREFERENCE2 = TAHITI_EVENT + 31;
    static final int PREFERENCE3 = TAHITI_EVENT + 32;
    static final int PREFERENCE4 = TAHITI_EVENT + 33;

    //
    static final int ABOUT_AGLETS = TAHITI_EVENT + 35;
    static final int ABOUT_TAHITI = TAHITI_EVENT + 36;

    //
    static final int SHRINK = TAHITI_EVENT + 40;

    //
    static final int TAHITI_LAST_EVENT = TAHITI_EVENT + 41;

    /**
     * The panel with the list of the agents.
     */
    protected AgletListPanel<AgletProxy> agletList = null;

    /**
     * The tahiti menu memoryBar
     */
    protected TahitiMenuBar menuBar = null;

    /**
     * The tahiti toolbar.
     */
    protected TahitiToolBar toolBar = null;

    /**
     * The memory panel.
     */
    protected MemoryPanel memoryPanel = null;

    /**
     * The base key for this class.
     */
    private String baseKey = this.getClass().getName();

    /**
     * The window manager for this window.
     */
    private WindowManager windowManager = null;

    /*
     * Constructs the instance of the main window for the Tahiti Aglet viewer.
     */
    MainWindow(Tahiti tahiti) {
	super(false);
	this._tahiti = tahiti;

	// set the title for this window
	this.setTitle(JComponentBuilder.getTitle(this.baseKey));

	// init the components
	this.memoryPanel = new MemoryPanel(200, 200, true, true);
	this.agletList = new AgletListPanel<AgletProxy>();
	this.agletList.setRenderer(new AgletListRenderer(this.agletList));
	this.agletList.addListSelectionListener(this);
	this.menuBar = new TahitiMenuBar(this);
	this.toolBar = new TahitiToolBar(this);

	// set the layout
	this.setLayout(new BorderLayout());

	// add the components
	this.add(this.agletList, BorderLayout.CENTER);
	this.add(this.memoryPanel, BorderLayout.SOUTH);
	this.add(this.toolBar, BorderLayout.NORTH);
	this.setJMenuBar(this.menuBar);

	// add the window manager to this window
	this.windowManager = new WindowManager(this);
	this.addWindowListener(this.windowManager);
	this.shouldExitOnClosing = true;

	// pack the window and show it
	this.pack();
	this.setVisible(true);

	try {
	    // get the aglet runtime and prepare it
	    AgletRuntime runtime = AgletRuntime.getAgletRuntime();
	    String hosting = runtime.getServerAddress();
	    String ownerName = runtime.getOwnerName();

	    if (ownerName == null)
		ownerName = "[NO USER]";

	    // get a few resources (i.e., wrappers around the
	    // system properties) for the aglets
	    String titleAddition = " " + ownerName + "@" + hosting + " - "
		    + this.getInformativeTitle();
	    this.setTitle(this.getTitle() + titleAddition);
	} catch (Exception e) {
	    this.logger.error("Cannot get an Aglet Runtime instance! Cannot continue!", e);
	    this.setTitle(this.getTitle() + " "
		    + this.translator.translate(this.baseKey + ".title.error"));
	    return;
	}

    }

    /**
     * Checks some run-time parameters to and returns a descriptive string to
     * place in the window title.
     */
    private String getInformativeTitle() {
	Resource aglets_res = Resource.getResourceFor("aglets");
	boolean bsecure = aglets_res.getBoolean("aglets.secure", true);
	Resource atp_res = Resource.getResourceFor("atp");
	boolean brunning = true;

	if (atp_res != null)
	    brunning = atp_res.getBoolean("atp.server", false);

	String titleAddition = "";
	if (brunning)
	    titleAddition = this.translator.translate(this.baseKey
		    + ".title.running");
	else
	    titleAddition = this.translator.translate(this.baseKey
		    + ".title.notrunning");

	if (bsecure)
	    titleAddition += this.translator.translate(this.baseKey
		    + ".title.secure");
	else
	    titleAddition += this.translator.translate(this.baseKey
		    + ".title.unsecure");

	return titleAddition;
    }

    /**
     * Provides a list of selected proxies in the aglet list.
     */
    protected LinkedList<AgletProxy> getSelectedProxies() {
	return this.agletList.getSelectedItems();
    }

    /**
     * Returns the first (and maybe the only) selected item in the aglet list
     * panel.
     * 
     * @return the aglet proxy of the selected element
     */
    protected AgletProxy getSelectedProxy() {
	return this.agletList.getSelectedItem();
    }

    /**
     * Removes an element from the proxy list.
     * 
     * @param proxy
     *            the proxy to remove
     */
    protected synchronized void removeProxyFromList(AgletProxy proxy) {
	// check params
	if (proxy == null)
	    return;
	else
	    this.agletList.removeItem(proxy);
    }

    /**
     * Adds a proxy to the proxy list.
     * 
     * @param proxy
     *            the proxy to add
     */
    protected synchronized void insertProxyToList(AgletProxy proxy) {
	if (proxy == null)
	    return;
	else
	    this.agletList.addItem(proxy);

    }

    // TODO gestione eventi qui sotto

    /**
     * Manages events from the GUI within the Tahiti server. Commands issued are
     * divided into two groups: those that require a selection in the aglet list
     * and those that do not require it. The former kind of commands accept
     * multiple selection, and the right action will be processed for each
     * selected aglet. Please note that this method has been designed to reduce
     * the code, thus the dialog for each action is associated to a global
     * TahitiDialog instance, and then it is made modal and visible...
     */
    public void actionPerformed(ActionEvent event) {
	// check params
	if (event == null)
	    return;

	// get the action command
	String command = event.getActionCommand();

	// get all the selected proxies
	LinkedList<AgletProxy> selectedProxies = this.getSelectedProxies();

	if (this.commandRequiresAgletSelected(command)) {
	    // the command requires an aglet to be selected
	    // check that at least one aglet has been selected
	    if ((selectedProxies == null) || (selectedProxies.size() == 0)) {
		JOptionPane.showMessageDialog(this, this.baseKey
			+ ".errorMessage.selectionEmpty", this.baseKey
			+ ".errorMessage.selectionEmpty.title", JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    // iterate on each aglet proxy
	    Iterator iter = selectedProxies.iterator();
	    while ((iter != null) && iter.hasNext()) {
		AgletProxy currentProxy = (AgletProxy) iter.next();
		BaseAgletsDialog dialog = null;

		// now do the right action
		if (GUICommandStrings.CLONE_AGLET_COMMAND.equals(command))
		    dialog = new CloneAgletDialog(this, currentProxy);
		else if (GUICommandStrings.ACTIVATE_AGLET_COMMAND.equals(command))
		    this.activateAglet(currentProxy);
		else if (GUICommandStrings.DEACTIVATE_AGLET_COMMAND.equals(command)) {
		    // ask the user how many time it will deactivate the agent
		    String deactivationTime = JOptionPane.showInputDialog(this, this.translator.translate(this.baseKey
			    + ".deactivationTime"), this.translator.translate(this.baseKey
			    + ".deactivationTime.title"), JOptionPane.YES_NO_OPTION);
		    try {
			long millis = Long.parseLong(deactivationTime);
			this.deactivateAglet(currentProxy, millis);
		    } catch (NumberFormatException e) {
			this.logger.debug("Specified time for deactivation cannot be converted into a number of millis:  "
				+ deactivationTime);
		    }
		} else if (GUICommandStrings.MESSAGE_AGLET_COMMAND.equals(command))
		    this.message(currentProxy, new Message("dialog"));
		else if (GUICommandStrings.DISPOSE_AGLET_COMMAND.equals(command))
		    // dispose all the aglets
		    dialog = new DisposeAgletDialog(this, currentProxy);
		else if (GUICommandStrings.SLEEP_AGLET_COMMAND.equals(command)) {
		    // the user wants to make the agent sleeping
		    try {
			AgletProxyImpl proxyImpl = (AgletProxyImpl) currentProxy;
			Aglet aglet = proxyImpl.getAglet();

			String deactivationTime = JOptionPane.showInputDialog(this, this.translator.translate(this.baseKey
				+ ".sleepTime"), this.translator.translate(this.baseKey
				+ ".sleepTime.title"), JOptionPane.YES_NO_OPTION);
			long millis = Long.parseLong(deactivationTime);

			aglet.sleep(millis);

		    } catch (Exception e) {
			this.logger.error("Exception caught while trying to call a sleep within the aglet", e);
			JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
				+ ".error.sleepMessage"), this.translator.translate(this.baseKey
				+ ".error.sleepTitle"), JOptionPane.ERROR_MESSAGE);
		    }

		} else if (GUICommandStrings.DISPATCH_AGLET_COMMAND.equals(command))
		    dialog = new DispatchAgletDialog(this, currentProxy);
		else if (GUICommandStrings.INFO_AGLET_COMMAND.equals(command))
		    dialog = TahitiDialog.info(this, currentProxy);

		// else.....

		// now show the appropriate dialog
		if (dialog != null) {
		    dialog.setModal(true);
		    dialog.setVisible(true);
		}

	    }
	} else {
	    // the command does not require a selection, thus
	    // do only the action
	    BaseAgletsDialog dialog = null;

	    if (GUICommandStrings.CREATE_AGLET_COMMAND.equals(command))
		dialog = new CreateAgletDialog(this);
	    else if (GUICommandStrings.GARBAGECOLLECTOR_COMMAND.equals(command)) {
		this.logger.info("Forcing the garbage collector");
		System.gc();
	    } else if (GUICommandStrings.REBOOT_COMMAND.equals(command)) {
		// ask confirmation for rebooting
		if (JOptionPane.showConfirmDialog(this.getParent(), this.translator.translate(this.baseKey
			+ ".rebootMessage"), this.translator.translate(this.baseKey
			+ ".rebootMessage.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		    this.reboot();
	    } else if (GUICommandStrings.EXIT_COMMAND.equals(command)) {
		// ask confirmation for rebooting
		if (JOptionPane.showConfirmDialog(this.getParent(), this.translator.translate(this.baseKey
			+ ".shutdownMessage"), this.translator.translate(this.baseKey
			+ ".shutdownMessage.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)

		    this.shutdown();
	    } else if (GUICommandStrings.RETRACT_AGLET_COMMAND.equals(command))
		dialog = new RetractAgletDialog(this);
	    else if (GUICommandStrings.REDUCE_COMMAND.equals(command)) {
		// the user wants to reduce the size of the window, thus
		// remove the aglet list
		this.remove(this.agletList);
		this.pack();
	    } else if (GUICommandStrings.ENLARGE_COMMAND.equals(command)) {
		// enlarge the window, thus re-add the
		// aglet list panel
		this.add(this.agletList, BorderLayout.CENTER);
		this.pack();
	    } else if (GUICommandStrings.GENPREFS_COMMAND.equals(command))
		// show the general preferences dialog
		dialog = GeneralConfigDialog.getInstance(this);
	    else if (GUICommandStrings.NETPREFS_COMMAND.equals(command))
		// show the network preferences dialog
		dialog = NetworkConfigDialog.getInstance(this);
	    else if (GUICommandStrings.SECPREFS_COMMAND.equals(command))
		// show the security dialog
		dialog = SecurityConfigDialog.getInstance(this);
	    else if (GUICommandStrings.ABOUT_COMMAND.equals(command))
		// info about the project
		dialog = AboutDialog.getInstance(this);
	    else if (GUICommandStrings.DOC_COMMAND.equals(command)) {
		// get the documentation root file
		String docRoot = this.translator.translate(this.baseKey
			+ "documentationFile");
		this.showDocumentation(docRoot);
		return;
	    } else if (GUICommandStrings.WEB_COMMAND.equals(command)) {
		// get the URL of the site
		String webPage = this.translator.translate(this.baseKey
			+ ".webSite");
		try {
		    URL url = new URL(webPage);
		    this.showWebSite(url);
		} catch (MalformedURLException e) {
		    this.logger.error("Exception caught while trying to open the web page", e);
		}

		return;
	    } else if (GUICommandStrings.MEMORY_COMMAND.equals(command))
		// show the memory dialog
		dialog = new MemoryDialog(this);
	    else if (GUICommandStrings.GARBAGECOLLECTOR_COMMAND.equals(command)) {
		// invoke the garbage collector
		this.logger.info("Invoking the garbage collector...");
		System.gc();
		this.logger.info("Garbage collector invoked.");
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".garbageCollector"), this.translator.translate(this.baseKey
			+ ".garbageCollector.title"), JOptionPane.INFORMATION_MESSAGE, JComponentBuilder.getIcon(this.baseKey
			+ ".garbageCollector.icon"));
	    } else if (GUICommandStrings.THREAD_COMMAND.equals(command))
		// dump the threads
		dumpThreads(System.out);
	    else
		// to be removed when all will be implemented
		JOptionPane.showMessageDialog(this, "Feature not yet implemented!", "Error", JOptionPane.ERROR_MESSAGE);

	    // now show the dialog
	    if (dialog != null) {
		dialog.setModal(true);
		dialog.setVisible(true);
	    }

	}

    }

    /**
     * This is an utility method that return true if the command requires an
     * aglet to be selected in the aglet list panel.
     * 
     * @param command
     *            the command to check
     * @return true if the command requires that at least one aglet has been
     *         selected
     */
    protected final boolean commandRequiresAgletSelected(String command) {
	if (command == null)
	    return false;
	else if (GUICommandStrings.CLONE_AGLET_COMMAND.equals(command)
		|| GUICommandStrings.DISPATCH_AGLET_COMMAND.equals(command)
		|| GUICommandStrings.DISPOSE_AGLET_COMMAND.equals(command)
		|| GUICommandStrings.KILL_AGLET_COMMAND.equals(command)
		|| GUICommandStrings.DEACTIVATE_AGLET_COMMAND.equals(command)
		|| GUICommandStrings.ACTIVATE_AGLET_COMMAND.equals(command)
		|| GUICommandStrings.SLEEP_AGLET_COMMAND.equals(command)
		|| GUICommandStrings.INFO_AGLET_COMMAND.equals(command))
	    return true;
	else
	    return false;
    }

    //
    class URLOpener implements ActionListener {
	private String url;

	URLOpener(String u) {
	    this.url = u;
	}

	public void actionPerformed(ActionEvent ev) {
	    MainWindow.this.showURL(this.url);
	}
    };

    class EventIssuer implements ActionListener {
	private int _type = 0;

	EventIssuer(int t) {
	    this._type = t;
	}

	public void actionPerformed(ActionEvent ev) {
	    switch (this._type) {
	    case SHRINK:
		Button b = (Button) ev.getSource();

		break;
	    case GC:
		System.gc();
		break;
	    case DIALOG:
		MainWindow.this.dialog(MainWindow.this.getSelectedProxy());
		break;
	    case SHOW_DEBUG:
		com.ibm.awb.misc.Debug.list(System.err);
	    case SHOW_REFTABLE:
		com.ibm.aglets.RemoteAgletRef.showRefTable(System.err);
		break;
	    case GET_AGLETS:
		MainWindow.this.getAglets();
		break;
	    case ACTIVATE:
		MainWindow.this.activateAglet(MainWindow.this.getSelectedProxy());
		break;
	    case KILL:
		AgletProxy p = MainWindow.this.getSelectedProxy();

		if (p != null) {
		    try {
			AgletRuntime.getAgletRuntime().killAglet(p);
		    } catch (Exception ex) {
			ex.printStackTrace();
		    }
		}
		break;
	    }
	}
    }

    class DialogOpener implements ActionListener {
	private int _type = 0;

	DialogOpener(int type) {
	    this._type = type;
	}

	public void actionPerformed(ActionEvent ev) {
	    final AgletProxy p = MainWindow.this.getSelectedProxy();

	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    TahitiDialog d = null;

		    switch (DialogOpener.this._type) {
		    case DISPATCH:
			d = new DispatchAgletDialog(MainWindow.this, p);
			d.popupAtCenterOfParent();
			break;
		    case AGLET_INFO:
			// d = new PropertiesDialog(MainWindow.this,
			// getSelectedProxy());
			// d.popupAtCenterOfParent();
			break;
		    case SHOW_LOG:
			MainWindow.this.logWindow.pack();
			if ((MainWindow.this.logWindow.getLocation().x == 0)
				&& (MainWindow.this.logWindow.getLocation().y == 0)) {
			    Dimension dim = MainWindow.this.getToolkit().getScreenSize();
			    Dimension size = MainWindow.this.logWindow.getSize();

			    MainWindow.this.logWindow.setLocation((dim.width - size.width) / 2, (dim.height - size.height) / 2);
			}
			MainWindow.this.logWindow.show();
			break;
		    case SHOW_JAVACON:
			if (com.ibm.awb.launcher.Agletsd.console != null) {
			    com.ibm.awb.launcher.Agletsd.console.show();
			}
			break;
		    case PREFERENCE1:
			d = GeneralConfigDialog.getInstance(MainWindow.this);
			d.popupAtCenterOfParent();
			break;
		    case PREFERENCE2:
			d = NetworkConfigDialog.getInstance(MainWindow.this);
			d.popupAtCenterOfParent();
			break;
		    case PREFERENCE3:
			d = SecurityConfigDialog.getInstance(MainWindow.this);
			d.popupAtCenterOfParent();
			break;
		    case PREFERENCE4:
			d = ServerPrefsDialog.getInstance(MainWindow.this);
			d.popupAtCenterOfParent();
			break;
		    case ABOUT_TAHITI:
			Resource res = Resource.getResourceFor("tahiti");
			ResourceBundle bundle = null;

			bundle = ResourceBundle.getBundle("tahiti");
			d = TahitiDialog.message(MainWindow.this, bundle.getString("title.about_tahiti"), res.getString("tahiti.version")
				+ "\n" + bundle.getString("message.copyright"));
			d.popupAtCenterOfParent();
			break;
		    case ABOUT_AGLETS:
			res = Resource.getResourceFor("aglets");
			String msg = "Aglets Version : "
				+ res.getString("aglets.version") + "\n"
				+ "Aglets API : " + Aglet.MAJOR_VERSION + "."
				+ Aglet.MINOR_VERSION + "\n"
				+ "Aglet Transfer Format : "
				+ res.getString("aglets.stream.version") + "\n"
				+ "\n" + res.getString("aglets.copyright");

			d = TahitiDialog.message(MainWindow.this, "About Aglets", msg);
			d.popupAtCenterOfParent();
			break;
		    }
		    return null;
		}
	    });
	}
    }

    // -------------------------------------------------------------------
    // -- Message line

    // Size of message line.--
    //
    private int _messageLineSize = 60;

    // The message line.
    //
    private TextField _messageLine = new TextField(this._messageLineSize);

    public class TahitiEventHandler implements Runnable {
	int _type;
	AgletProxy _proxy;
	long _time;
	URL _remoteURL;
	String _codebase;
	String _name;
	boolean _reload;

	TahitiEventHandler(int type, AgletProxy p) {
	    this._type = type;
	    this._proxy = p;
	}

	TahitiEventHandler(AgletProxy p, long time) {
	    this._type = DEACTIVATE;
	    this._proxy = p;
	    this._time = time;
	}

	TahitiEventHandler(AgletProxy p, URL dest) {
	    this._type = DISPATCH;
	    this._proxy = p;
	    this._remoteURL = dest;
	}

	TahitiEventHandler(String codebase, String name, boolean reload) {
	    this._type = CREATE;
	    this._codebase = codebase;
	    this._name = name;
	    this._reload = reload;
	}

	public void run() {
	    com.ibm.awb.misc.Debug.start();
	    com.ibm.awb.misc.Debug.check();
	    try {
		this.perform();
	    } catch (Error ex) {
		ex.printStackTrace();
		TahitiDialog.message(MainWindow.this, "Error", ex.getClass().getName()
			+ "\n" + ex.getMessage()).popupAtCenterOfParent();
	    } catch (Exception ex) {
		ex.printStackTrace();
		TahitiDialog.message(MainWindow.this, "Exception", ex.getClass().getName()
			+ "\n" + ex.getMessage()).popupAtCenterOfParent();
	    } finally {
		com.ibm.awb.misc.Debug.end();
	    }
	}

	private void perform() throws Exception {
	    switch (this._type) {
	    case CLONE:
		this._proxy.clone();
		break;
	    case DISPOSE:
		this._proxy.dispose();
		break;
	    case CREATE:
		if (!"".equals(this._codebase)) {
		    while (this._codebase.toLowerCase().startsWith("http://")
			    && this._codebase.endsWith("/")) {
			this._codebase = this._codebase.substring(0, this._codebase.length() - 1);
		    }
		}
		URL url = !this._codebase.equals("") ? new URL(this._codebase)
			: null;

		if (this._reload) {
		    Tahiti.CONTEXT.clearCache(null);
		}
		AgletProxy proxy = Tahiti.CONTEXT.createAglet(url, this._name, null);
		// insert the proxy in the list of created agents
		// agletList.addItem(proxy);

		// itrptWin.waitForDisplay();
		// itrptWin.dispose();
		// update list
		break;
	    case DISPATCH:

		// String key = _remoteURL + " " +
		// _proxy.getAgletClassName() + " " +
		// (new Date()).toString().substring(0, 20).trim();
		// String aid_str = _proxy.getAgletID().toString();
		this._proxy.dispatch(this._remoteURL);
		// agletList.removeItem(_proxy);

		// itrptWin.waitForDisplay();
		// itrptWin.dispose();
		break;
	    case RETRACT:
		MainWindow.this.logger.debug("Getting the remote agent back "
			+ this._proxy);
		AgletInfo info = this._proxy.getAgletInfo();

		// System.out.println(info);
		Tahiti.CONTEXT.retractAglet(new URL(this._proxy.getAddress()), info.getAgletID());

		// itrptWin.waitForDisplay();
		// itrptWin.dispose();
		break;
	    case DEACTIVATE:
		this._proxy.deactivate(this._time * 1000);
		break;
	    case ACTIVATE:
		if ((this._proxy != null) && this._proxy.isValid()
			&& (this._proxy.isActive() == false)) {
		    this._proxy.activate();
		}
		break;
	    default:
	    }
	}
    }

    /**
     * Activates the aglet specified by the proxy.
     * 
     * @param p
     *            the proxy of the agent to activate
     */
    public void activateAglet(AgletProxy p) {
	SwingUtilities.invokeLater(new TahitiEventHandler(ACTIVATE, p));
    }

    /**
     * Show the documentation of the platform.
     * 
     * @param documentRoot
     *            the documentation file to show
     */
    protected void showDocumentation(String documentRoot) {
	TahitiBrowser browser = new TahitiBrowser(this, documentRoot);
	browser.run();
    }

    /**
     * Show the web site.
     * 
     * @param webSite
     *            the url of the web site to show.
     */
    protected void showWebSite(URL webSite) {
	TahitiBrowser browser = new TahitiBrowser(this, webSite);
	browser.run();
    }

    private void addListeners() {
	this._agletList.addItemListener(this);
	this._agletList.addActionListener(this);
    }

    // Clears the message line.
    //
    private void clearMessage() {
	this._messageLine.setText("");
    }

    /**
     * Clones the specified agent.
     * 
     * @param p
     *            the proxy of the agent to clone
     */
    public void cloneAglet(AgletProxy p) {
	SwingUtilities.invokeLater(new TahitiEventHandler(CLONE, p));
    }

    /**
     * Creates a new agent.
     * 
     * @param codebase
     *            the codebase of the class to create
     * @param name
     *            the name of the agent
     * @param reload
     *            reload it or get from the cache
     */
    public void createAglet(String codebase, String name, boolean reload) {
	SwingUtilities.invokeLater(new TahitiEventHandler(codebase, name, reload));
    }

    /**
     * Deactivates the specfiied agent for the specified number of millisecs.
     * 
     * @param p
     *            the proxy of the agent to deactivate
     * @param time
     *            the millisecs for the deactivation
     */
    public void deactivateAglet(AgletProxy p, long time) {
	SwingUtilities.invokeLater(new TahitiEventHandler(p, time));
    }

    /**
     * Sends a dialog message to the specified agent
     * 
     * @param proxy
     *            the addressee agent
     * @deprecated
     */
    @Deprecated
    protected void dialog(AgletProxy proxy) {
	try {
	    proxy.sendAsyncMessage(new Message("dialog"));
	} catch (InvalidAgletException ex) {
	    this.setMessage(ex.getMessage());
	}
    }

    /**
     * Sends the specified message to the specified agent.
     * 
     * @param proxy
     *            the proxy of the agent that must receive the message
     * @param message
     *            the message to send
     */
    protected void message(AgletProxy proxy, Message message) {
	try {
	    if ((proxy == null) || (message == null))
		return;

	    // send the message
	    proxy.sendAsyncMessage(message);

	} catch (AgletException e) {
	    this.logger.error("Exception caught while sending a message to an agent", e);

	}
    }

    /**
     * Dispatches the specified agent to the specified destination.
     * 
     * @param p
     *            the proxy of the agent to dispatch
     * @param dest
     *            the destination to which dispatch to
     */
    public void dispatchAglet(AgletProxy p, URL dest) {
	SwingUtilities.invokeLater(new TahitiEventHandler(p, dest));
	// new Thread(new TahitiEventHandler(p, dest)).start();

    }

    /**
     * Disposes the agent.
     * 
     * @param p
     *            the proxy of the agent to dispose
     */
    public void disposeAglet(AgletProxy p) {
	SwingUtilities.invokeLater(new TahitiEventHandler(DISPOSE, p));
    }

    /*
     * Retract aglets from an Aglet box
     */
    void getAglets() {

	/*
	 * try { AgletBox.update(Tahiti.CONTEXT); } catch (Exception e) {
	 * setMessage("AgletBox: " + e.getMessage()); }
	 */
    }

    // Returns an Aglet's item text line for the Aglet list.
    //

    /*
     * private String getItemText(AgletProxy proxy) { StringBuffer buffer = new
     * StringBuffer(); try { AgletInfo info = proxy.getAgletInfo(); if
     * (proxy.isValid() == false) { return "InvalidAglet"; } if
     * (proxy.isActive() == false) { buffer.append("[deactivated]"); }
     * buffer.append(new Date(info.getCreationTime())); buffer.append(" " +
     * info.getAgletClassName() + ' '); String s = (String) text.get(proxy);
     * buffer.append(" " + (s == null ? " " : s)); } catch
     * (InvalidAgletException ex) { return "InvalidAglet"; } catch
     * (RuntimeException ex) { ex.printStackTrace(); } finally { } return
     * buffer.toString(); }
     */

    private String getItemText(TahitiItem tahitiItem) {
	StringBuffer buffer = new StringBuffer();

	buffer.append(tahitiItem.getText());
	AgletProxy proxy = tahitiItem.getAgletProxy();
	String s = (String) this.text.get(proxy);

	buffer.append(" " + (s == null ? " " : s));

	return buffer.toString();
    }

    /*
     * public Dimension getPreferredSize() { Resource res =
     * Resource.getResourceFor("tahiti");
     * 
     * if (!shrink) { return new Dimension(res.getInteger("tahiti.window.width",
     * 545), res.getInteger("tahiti.window.height", 350)); } else { return new
     * Dimension(res.getInteger("tahiti.window.s_width", 545),
     * res.getInteger("tahiti.window.s_height", 350)); } }
     */

    /*
     * AgletProxy[] getSelectedProxies() { int selected[] =
     * _agletList.getSelectedIndexes(); AgletProxy p[] = new
     * AgletProxy[selected.length];
     * 
     * for (int i = 0; i < p.length; i++) { p[i] =
     * ((TahitiItem)_itemList.elementAt(selected[i])) .getAgletProxy(); } return
     * p;
     * 
     * }
     */
    /*
     * AgletProxy getSelectedProxy() { int selected =
     * _agletList.getSelectedIndex();
     * 
     * if (selected != -1) { return ((TahitiItem)_itemList.elementAt(selected))
     * .getAgletProxy(); } else { return null; } }
     */
    void hideButtons() {
	this._dialogButton.setVisible(false);
	this._infoButton.setVisible(false);
	this._disposeButton.setVisible(false);
	this._cloneButton.setVisible(false);
	this._dispatchButton.setVisible(false);
    }

    /*
     * synchronized void updateViewItems() { for (int i = 0; i <
     * _itemList.size(); i++) { TahitiItem tahitiItem =
     * (TahitiItem)_itemList.elementAt(i);
     * _agletList.replaceItem(getItemText(tahitiItem), i); } }
     */

    /*
     * synchronized void insertProxyToList(AgletProxy proxy) {
     * 
     * if (shrink) { return; }
     * 
     * TahitiItem tahitiItem = new TahitiItem(proxy);
     * 
     * int index = -1;
     * 
     * for (int i = 0; i < _itemList.size(); i++) { if
     * (tahitiItem.compareTo((TahitiItem)_itemList.elementAt(i)) <= 0) { index =
     * i; break; } }
     * 
     * if (index >= 0) { _itemList.insertElementAt(tahitiItem, index);
     * _agletList.add(getItemText(tahitiItem), index); } else {
     * _itemList.addElement(tahitiItem);
     * _agletList.add(getItemText(tahitiItem)); }
     * 
     * // updateGUIState(); }
     */

    /**
     * Manages selections on the agent list. For now it only manages the
     * selection/deselction of agents in order to enable or disable certain
     * elements on the GUI (buttons and menu entries).
     */
    public void itemStateChanged(ItemEvent event) {

    }

    // Builds the button panel.
    // @return the button panel.
    //
    private Panel makeButtonPanel() {
	this._buttonPanel = new Panel();
	this._buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

	// ADD LISTENER
	Button b = new Button("><");

	b.addActionListener(new EventIssuer(SHRINK));
	this._buttonPanel.add(b);

	b = new Button(bundle.getString("button.create"));
	b.addActionListener(new DialogOpener(CREATE));
	this._buttonPanel.add(b);

	this._dialogButton.setLabel(bundle.getString("button.dialog"));
	this._dialogButton.addActionListener(new EventIssuer(DIALOG));
	this._buttonPanel.add(this._dialogButton);

	this._infoButton.setLabel(bundle.getString("button.info"));
	this._infoButton.addActionListener(new DialogOpener(AGLET_INFO));
	this._buttonPanel.add(this._infoButton);

	this._disposeButton.setLabel(bundle.getString("button.dispose"));
	this._disposeButton.addActionListener(new DialogOpener(DISPOSE));
	this._buttonPanel.add(this._disposeButton);

	this._cloneButton.setLabel(bundle.getString("button.clone"));
	this._cloneButton.addActionListener(new DialogOpener(CLONE));
	this._buttonPanel.add(this._cloneButton);

	this._dispatchButton.setLabel(bundle.getString("button.dispatch"));
	this._dispatchButton.addActionListener(new DialogOpener(DISPATCH));
	this._buttonPanel.add(this._dispatchButton);

	b = new Button(bundle.getString("button.retract"));
	b.addActionListener(new DialogOpener(RETRACT));
	this._buttonPanel.add(b);

	return this._buttonPanel;
    }

    // -------------------------------------------------------------------
    // -- Menu memoryBar and panel methods

    // Builds the menu memoryBar.
    //
    private MenuBar makeMenuBar() {
	MenuBar menubar = new MenuBar();

	//
	// Aglet Menu
	//
	Menu menu = new Menu(bundle.getString("menu.aglet"));

	MenuItem item = new MenuItem(bundle.getString("menuitem.create"));

	item.addActionListener(new DialogOpener(CREATE));
	menu.add(item);

	this._dialogMenuItem.setLabel(bundle.getString("menuitem.dialog"));
	this._dialogMenuItem.addActionListener(new EventIssuer(DIALOG));
	menu.add(this._dialogMenuItem);

	this._disposeMenuItem.setLabel(bundle.getString("menuitem.dispose"));
	this._disposeMenuItem.addActionListener(new DialogOpener(DISPOSE));
	menu.add(this._disposeMenuItem);

	this._cloneMenuItem.setLabel(bundle.getString("menuitem.clone"));
	this._cloneMenuItem.addActionListener(new DialogOpener(CLONE));
	menu.add(this._cloneMenuItem);

	this._infoMenuItem.setLabel(bundle.getString("menuitem.info"));
	this._infoMenuItem.addActionListener(new DialogOpener(AGLET_INFO));
	menu.add(this._infoMenuItem);

	menu.addSeparator();

	this._killMenuItem.setLabel(bundle.getString("menuitem.kill"));
	this._killMenuItem.addActionListener(new EventIssuer(KILL));
	menu.add(this._killMenuItem);

	menu.addSeparator();

	item = new MenuItem(bundle.getString("menuitem.exit"));
	item.addActionListener(new DialogOpener(EXIT));
	menu.add(item);

	menubar.add(menu);

	//
	// Mobility menu.
	//
	menu = new Menu(bundle.getString("menu.mobility"));

	this._dispatchMenuItem.setLabel(bundle.getString("menuitem.dispatch"));
	this._dispatchMenuItem.addActionListener(new DialogOpener(DISPATCH));
	menu.add(this._dispatchMenuItem);

	item = new MenuItem(bundle.getString("menuitem.retract"));
	item.addActionListener(new DialogOpener(RETRACT));
	menu.add(item);

	this._deactivateMenuItem.setLabel(bundle.getString("menuitem.deactivate"));
	this._deactivateMenuItem.addActionListener(new DialogOpener(DEACTIVATE));
	menu.add(this._deactivateMenuItem);

	this._activateMenuItem.setLabel(bundle.getString("menuitem.activate"));
	this._activateMenuItem.addActionListener(new EventIssuer(ACTIVATE));
	menu.add(this._activateMenuItem);

	menubar.add(menu);

	//
	// View menu.
	//
	menu = new Menu(bundle.getString("menu.view"));
	item = new MenuItem(bundle.getString("menuitem.memoryusage"));
	item.addActionListener(new DialogOpener(MEMORY_USAGE));
	menu.add(item);

	// item = new MenuItem(bundle.getString("menuitem.age"));
	// item.addActionListener();
	// menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.log"));
	item.addActionListener(new DialogOpener(SHOW_LOG));
	menu.add(item);

	this._javaConsoleMenuItem = new MenuItem(bundle.getString("menuitem.javaconsole"));
	this._javaConsoleMenuItem.addActionListener(new DialogOpener(SHOW_JAVACON));
	if (com.ibm.awb.launcher.Agletsd.console != null) {
	    this._javaConsoleMenuItem.setEnabled(true);
	} else {
	    this._javaConsoleMenuItem.setEnabled(false);
	}
	menu.add(this._javaConsoleMenuItem);

	menubar.add(menu);

	//
	// Options menu.
	//
	menu = new Menu(bundle.getString("menu.options"));
	item = new MenuItem(bundle.getString("menuitem.general"));
	item.addActionListener(new DialogOpener(PREFERENCE1));
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.network"));
	item.addActionListener(new DialogOpener(PREFERENCE2));
	menu.add(item);

	Resource res = Resource.getResourceFor("aglets");

	if (res.getBoolean("aglets.secure", true)) {
	    item = new MenuItem(bundle.getString("menuitem.security"));
	    item.addActionListener(new DialogOpener(PREFERENCE3));
	    menu.add(item);
	}

	item = new MenuItem(bundle.getString("menuitem.server"));
	item.addActionListener(new DialogOpener(PREFERENCE4));
	menu.add(item);

	menubar.add(menu);

	//
	// Tools menu
	//
	menu = new Menu(bundle.getString("menu.tools"));

	item = new MenuItem(bundle.getString("menuitem.gc"));
	item.addActionListener(new EventIssuer(GC));
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.threads"));
	item.addActionListener(new EventIssuer(SHOW_THREADS));
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.debug"));
	item.addActionListener(new EventIssuer(SHOW_DEBUG));
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.reftable"));
	item.addActionListener(new EventIssuer(SHOW_REFTABLE));
	menu.add(item);

	/*
	 * if (Tahiti.enableBox) { menu.addSeparator();
	 * 
	 * item = new MenuItem(bundle.getString("menuitem.get"));
	 * item.addActionListener(new EventIssuer(GET_AGLETS)); menu.add(item);
	 * }
	 */
	menubar.add(menu);

	//
	// Help menu
	//
	menu = new Menu(bundle.getString("menu.help"));

	item = new MenuItem(bundle.getString("menuitem.about_tahiti"));
	item.addActionListener(new DialogOpener(ABOUT_TAHITI));
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.about_aglets"));
	item.addActionListener(new DialogOpener(ABOUT_AGLETS));
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.release_notes"));
	item.addActionListener(new URLOpener(bundle.getString("http.release_notes")));

	// "http://www.trl.ibm.co.jp/aglets/awb_1.0b1.html"));
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.aglets_home_page"));
	item.addActionListener(new URLOpener(bundle.getString("http.aglets_home")));

	// "http://www.trl.ibm.co.jp/aglets/index.html"
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.feedback"));
	item.addActionListener(new URLOpener(bundle.getString("http.feedback")));

	// "http://aglets.trl.ibm.co.jp/report.html"
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.bug_report"));
	item.addActionListener(new URLOpener(bundle.getString("http.bug_report")));

	// "http://aglets.trl.ibm.co.jp/report.html"
	menu.add(item);

	item = new MenuItem(bundle.getString("menuitem.faq"));
	item.addActionListener(new URLOpener(bundle.getString("http.faq")));

	// "http://www.trl.ibm.co.jp/aglets/faq.html"
	menu.add(item);

	menubar.setHelpMenu(menu);

	return menubar;
    }

    /**
     * Reboots the tahiti server.
     * 
     */
    public void reboot() {
	this.logger.info("Tahiti is rebooting....");
	this._tahiti.reboot = true;
	this._tahiti.reboot();
    }

    public void restoreSize() {
	Resource res = Resource.getResourceFor("tahiti");

	this.setSize(res.getInteger("tahiti.window.width", 100), res.getInteger("tahiti.window.height", 100));
    }

    public void retractAglet(AgletProxy p) {
	SwingUtilities.invokeLater(new TahitiEventHandler(RETRACT, p));
    }

    /*
     * Handles the event public boolean handleEvent(Event event) { Thread
     * handler; AgletProxy proxy; String remote_host; InterruptWindow itrptWin;
     * 
     * case CREATE_AGLET: remote_host = (String)event.target; String aglet_class
     * = ((String)event.arg).trim();
     * 
     * itrptWin = new InterruptWindow(this, "Aglet Creation", "Creating Aglet",
     * (remote_host.toLowerCase().startsWith("http://") ? remote_host + "/" :
     * remote_host) + aglet_class);
     * 
     * handler = new TahitiEventThread(this, itrptWin, event);
     * itrptWin.setHandler(handler); handler.start(); itrptWin.popup(this);
     * 
     * break;
     * 
     * case DISPATCH_AGLET: proxy = (AgletProxy)event.target; remote_host =
     * ((String)event.arg).trim();
     * 
     * itrptWin = new InterruptWindow(this, "Aglet Dispatch",
     * "Dispatching Aglet", remote_host); handler = new TahitiEventThread(this,
     * itrptWin, event); itrptWin.setHandler(handler); handler.start();
     * itrptWin.popup(this); break; case RETRACT_AGLET: URL agletURL =
     * (URL)event.target;
     * 
     * itrptWin = new InterruptWindow(this, "Aglet Retract", "Retracting Aglet",
     * agletURL.getHost()); handler = new TahitiEventThread(this, itrptWin,
     * event); itrptWin.setHandler(handler); handler.start();
     * itrptWin.popup(this); break; default: return false; } return true; } else
     * { return super.handleEvent(event); } return false; }
     */

    /**
     * Saves the size of the window so that it can be restored once the window
     * is restarted.
     */
    private void saveSize() {
	java.awt.Rectangle bounds = this.getBounds();
	Resource res = Resource.getResourceFor("tahiti");

	res.setResource("tahiti.window.x", String.valueOf(bounds.x));
	res.setResource("tahiti.window.y", String.valueOf(bounds.y));
	res.setResource("tahiti.window.width", String.valueOf(bounds.width));
	res.setResource("tahiti.window.height", String.valueOf(bounds.height));
	res.setResource("tahiti.window.shrinked", "false");

    }

    public void setFont(Font f) {
	MenuBar menubar = this.getMenuBar();

	if (menubar != null) {
	    menubar.setFont(f);

	    int c = menubar.getMenuCount();

	    for (int i = 0; i < c; i++) {
		Menu m = menubar.getMenu(i);

		m.setFont(f);
	    }
	}
	super.setFont(f);
	this.doLayout();
    }

    // Updates the message line.
    // @param message the new message.
    //
    void setMessage(String message) {
	this._messageLine.setText(message);
    }

    void showButtons() {
	this._dialogButton.setVisible(true);
	this._infoButton.setVisible(true);
	this._disposeButton.setVisible(true);
	this._cloneButton.setVisible(true);
	this._dispatchButton.setVisible(true);
    }

    static private void showThreadGroup(ThreadGroup g, int level) {
	int i;
	String indent = "                                 ".substring(0, level);

	System.out.println(indent + "{" + g.toString() + "}");

	int n = g.activeCount();

	if (n > 0) {
	    System.out.println(indent + " + Threads");

	    Thread t[] = new Thread[g.activeCount()];

	    g.enumerate(t);
	    for (i = 0; i < t.length; i++) {
		if (g == t[i].getThreadGroup()) {
		    System.out.println(indent + "  - " + t[i].toString()
			    + (t[i].isAlive() ? " alive" : " dead"));
		}
	    }
	}

	n = g.activeGroupCount();
	if (n > 0) {
	    System.out.println(indent + " + ThreadGroups");
	    ThreadGroup tg[] = new ThreadGroup[n];

	    g.enumerate(tg);
	    for (i = 0; i < tg.length; i++) {
		if (g == tg[i].getParent()) {
		    showThreadGroup(tg[i], level + 4);
		}
	    }
	}
    }

    /**
     * Dumps all the thread groups. Invokes the dumpThreadGroup method for
     * dumping recursively each thread group.
     * 
     */
    public static void dumpThreads(PrintStream stream) {
	for (ThreadGroup currentGroup = Thread.currentThread().getThreadGroup(); currentGroup != null; currentGroup = currentGroup.getParent())
	    dumpThreadGroup(currentGroup, 0, stream);
    }

    /**
     * Dumps each thread group, showing them with a nesting that depends from
     * the group level.
     * 
     * @param currentGroup
     *            the group to dump
     * @param level
     *            the level for the nesting
     * @param out
     *            the stream to which write the thread group
     */
    private static void dumpThreadGroup(ThreadGroup currentGroup, int level,
	    PrintStream out) {
	StringBuffer buffer = new StringBuffer(500);

	// check params
	if (out == null)
	    out = System.out;

	// prepare for nesting
	for (int i = level; i > 0; i--)
	    buffer.append("\t");

	// the group name
	buffer.append("ThreadGroup: ");
	buffer.append(currentGroup.getName());
	buffer.append("\n");

	// does thi group have threads?
	int numThreads = 0;
	if ((numThreads = currentGroup.activeCount()) > 0) {

	    Thread threads[] = new Thread[numThreads];
	    currentGroup.enumerate(threads);

	    for (int i = threads.length - 1; i >= 0; i--) {
		// prepare for nesting
		for (int j = level; j > 0; j--)
		    buffer.append("\t");

		buffer.append("Thread n.");
		buffer.append(i);
		buffer.append(" Name:");
		buffer.append(threads[i].getName());
		buffer.append(" Status:");
		buffer.append(threads[i].getState().toString());

		// check if this is a poolable thread and if it is in the pool.
		if (threads[i] instanceof AgletThread) {
		    buffer.append(" In thread-pool:");
		    buffer.append(AgletThreadPool.getInstance().contains((AgletThread) threads[i]));
		}

		buffer.append("\n");

	    }

	}

	// output the result for this group
	out.println(buffer.toString());

	// now get the other groups
	int numGroups = 0;
	if ((numGroups = currentGroup.activeGroupCount()) > 0) {
	    // get all the other groups
	    ThreadGroup groups[] = new ThreadGroup[numGroups];
	    currentGroup.enumerate(groups);

	    for (int i = groups.length - 1; i >= 0; i--)
		dumpThreadGroup(groups[i], level + 1, out);
	}

    }

    static void showThreads() {
	ThreadGroup g = null;

	for (g = Thread.currentThread().getThreadGroup(); g.getParent() != null; g = g.getParent()) {
	}

	showThreadGroup(g, 0);
    }

    // -------------------------------------------------------------------
    // -- Window updating

    void showURL(String url) {
	Resource res = Resource.getResourceFor("tahiti");
	String command = res.getString("tahiti.browser_command", null);

	if (command != null) {
	    try {
		StringTokenizer st = new StringTokenizer(command);
		final String cmdarray[] = new String[st.countTokens() + 1];
		int count = 0;

		while (st.hasMoreTokens()) {
		    cmdarray[count++] = st.nextToken();
		}
		cmdarray[count] = url;
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
			Runtime.getRuntime().exec(cmdarray);
			return null;
		    }
		});
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    /*
     * Shows the given dialog at the center
     */
    private void showWindow(Window window) {
    }

    /**
     * Shuts down the Tahiti server.
     * The tahiti server has the information about the rebooting!
     * 
     */
    public void shutdown() {
	this.logger.info("Shutting down the Tahiti server");
	this._tahiti.reboot = false;
	this._tahiti.exit();
    }

    void updateGUIState() {
	int indexes[] = this._agletList.getSelectedIndexes();

	boolean single = indexes.length == 1;
	boolean multiple = indexes.length >= 1;

	this._dialogMenuItem.setEnabled(single);
	this._dialogButton.setEnabled(single);

	this._disposeMenuItem.setEnabled(multiple);
	this._disposeButton.setEnabled(multiple);

	this._killMenuItem.setEnabled(single);

	this._cloneMenuItem.setEnabled(single);
	this._cloneButton.setEnabled(single);

	this._infoMenuItem.setEnabled(single);
	this._infoButton.setEnabled(single);

	this._dispatchMenuItem.setEnabled(single);
	this._dispatchButton.setEnabled(single);

	// _retractMenuItem.setEnabled(); _retractButton.setEnabled();

	this._deactivateMenuItem.setEnabled(single);
	this._activateMenuItem.setEnabled(single);
    }

    /**
     * Does nothing, no more required.
     * 
     * @param proxy
     */
    @Deprecated
    synchronized void updateProxyInList(AgletProxy proxy) {
	/*
	 * TahitiItem tahitiItem = null;
	 * 
	 * if (shrink) { return; }
	 * 
	 * int selected = _agletList.getSelectedIndex();
	 * 
	 * int index = -1;
	 * 
	 * for (int i = 0; i < _itemList.size(); i++) { tahitiItem =
	 * (TahitiItem)_itemList.elementAt(i); if (tahitiItem.checkProxy(proxy))
	 * { _agletList.replaceItem(getItemText(tahitiItem), i); index = i; if
	 * (index == selected) { _agletList.select(index); } } }
	 * 
	 * updateGUIState();
	 */
	this.logger.info("The updateProxyInList method call is no longer required, since the AgletList class now stores proxies");
    }

    @Deprecated
    public synchronized void updateProxyList() {
	/*
	 * // return all _agletList.removeAll(); _itemList.setSize(0);
	 * 
	 * // System.out.println("updateProxyList()");
	 * 
	 * Enumeration e = Tahiti.CONTEXT.getAgletProxies();
	 * 
	 * while (e.hasMoreElements()) {
	 * insertProxyToList((AgletProxy)e.nextElement()); }
	 */
	this.logger.info("The updateProxyInList method call is no longer required, since the AgletList class now stores proxies");
    }

    /**
     * Manages events from the agent list panel. Each time the selection on the
     * panel changes, this method enables/disables GUI components such as menues
     * and buttons to better guide the user thru the action it can take in a
     * specific moment.
     */
    public void valueChanged(ListSelectionEvent event) {
	// check params
	if (event == null)
	    return;

	// change the status of the buttons and of the menues
	// depending on the selection on the list
	boolean enable = false;

	if (this.agletList.getItemCount() > 0)
	    enable = true;
	else
	    enable = false;

	// enable/disable conditional elements
	this.menuBar.enableConditionalItems(enable);
	this.toolBar.enableConditionalButtons(enable);

    }

    /**
     * Display a dialog to notify the user of an exception.
     * 
     * @param ex
     */
    public final void showException(Exception ex) {
	if (ex == null)
	    return;

	// display an error dialog
	JOptionPane.showMessageDialog(this, ex.getStackTrace(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);

    }
}
