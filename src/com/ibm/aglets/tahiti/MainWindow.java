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

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;
import net.sourceforge.aglets.util.gui.WindowManager;

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
	class DialogOpener implements ActionListener {
		private int _type = 0;

		DialogOpener(final int type) {
			_type = type;
		}

		public void actionPerformed(final ActionEvent ev) {
			final AgletProxy p = getSelectedProxy();

			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					TahitiDialog d = null;

					switch (_type) {
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
							logWindow.pack();
							if ((logWindow.getLocation().x == 0)
									&& (logWindow.getLocation().y == 0)) {
								final Dimension dim = getToolkit().getScreenSize();
								final Dimension size = logWindow.getSize();

								logWindow.setLocation((dim.width - size.width) / 2, (dim.height - size.height) / 2);
							}
							logWindow.show();
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
							final String msg = "Aglets Version : "
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
	class EventIssuer implements ActionListener {
		private int _type = 0;

		EventIssuer(final int t) {
			_type = t;
		}

		public void actionPerformed(final ActionEvent ev) {
			switch (_type) {
				case SHRINK:
					final Button b = (Button) ev.getSource();

					break;
				case GC:
					System.gc();
					break;
				case DIALOG:
					dialog(getSelectedProxy());
					break;
				case SHOW_DEBUG:
					com.ibm.awb.misc.Debug.list(System.err);
				case SHOW_REFTABLE:
					com.ibm.aglets.RemoteAgletRef.showRefTable(System.err);
					break;
				case GET_AGLETS:
					getAglets();
					break;
				case ACTIVATE:
					activateAglet(getSelectedProxy());
					break;
				case KILL:
					final AgletProxy p = getSelectedProxy();

					if (p != null) {
						try {
							AgletRuntime.getAgletRuntime().killAglet(p);
						} catch (final Exception ex) {
							ex.printStackTrace();
						}
					}
					break;
			}
		}
	}

	public class TahitiEventHandler implements Runnable {
		int _type;
		AgletProxy _proxy;
		long _time;
		URL _remoteURL;
		String _codebase;
		String _name;
		boolean _reload;

		TahitiEventHandler(final AgletProxy p, final long time) {
			_type = DEACTIVATE;
			_proxy = p;
			_time = time;
		}

		TahitiEventHandler(final AgletProxy p, final URL dest) {
			_type = DISPATCH;
			_proxy = p;
			_remoteURL = dest;
		}

		TahitiEventHandler(final int type, final AgletProxy p) {
			_type = type;
			_proxy = p;
		}

		TahitiEventHandler(final String codebase, final String name, final boolean reload) {
			_type = CREATE;
			_codebase = codebase;
			_name = name;
			_reload = reload;
		}

		private void perform() throws Exception {
			switch (_type) {
				case CLONE:
					_proxy.clone();
					break;
				case DISPOSE:
					_proxy.dispose();
					break;
				case CREATE:
					if (!"".equals(_codebase)) {
						while (_codebase.toLowerCase().startsWith("http://")
								&& _codebase.endsWith("/")) {
							_codebase = _codebase.substring(0, _codebase.length() - 1);
						}
					}
					final URL url = !_codebase.equals("") ? new URL(_codebase)
					: null;

					if (_reload) {
						Tahiti.CONTEXT.clearCache(null);
					}
					final AgletProxy proxy = Tahiti.CONTEXT.createAglet(url, _name, null);
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
					_proxy.dispatch(_remoteURL);
					// agletList.removeItem(_proxy);

					// itrptWin.waitForDisplay();
					// itrptWin.dispose();
					break;
				case RETRACT:
					MainWindow.this.logger.debug("Getting the remote agent back "
							+ _proxy);
					final AgletInfo info = _proxy.getAgletInfo();

					// System.out.println(info);
					Tahiti.CONTEXT.retractAglet(new URL(_proxy.getAddress()), info.getAgletID());

					// itrptWin.waitForDisplay();
					// itrptWin.dispose();
					break;
				case DEACTIVATE:
					_proxy.deactivate(_time * 1000);
					break;
				case ACTIVATE:
					if ((_proxy != null) && _proxy.isValid()
							&& (_proxy.isActive() == false)) {
						_proxy.activate();
					}
					break;
				default:
			}
		}

		public void run() {
			com.ibm.awb.misc.Debug.start();
			com.ibm.awb.misc.Debug.check();
			try {
				perform();
			} catch (final Error ex) {
				ex.printStackTrace();
				TahitiDialog.message(MainWindow.this, "Error", ex.getClass().getName()
						+ "\n" + ex.getMessage()).popupAtCenterOfParent();
			} catch (final Exception ex) {
				ex.printStackTrace();
				TahitiDialog.message(MainWindow.this, "Exception", ex.getClass().getName()
						+ "\n" + ex.getMessage()).popupAtCenterOfParent();
			} finally {
				com.ibm.awb.misc.Debug.end();
			}
		}
	}

	//
	class URLOpener implements ActionListener {
		private final String url;

		URLOpener(final String u) {
			url = u;
		}

		public void actionPerformed(final ActionEvent ev) {
			showURL(url);
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 7553640699339847522L;
	static ResourceBundle bundle = null;
	static {
		bundle = (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				return ResourceBundle.getBundle("tahiti");
			}
		});
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
	private static void dumpThreadGroup(
	                                    final ThreadGroup currentGroup,
	                                    final int level,
	                                    PrintStream out) {
		final StringBuffer buffer = new StringBuffer(500);

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

			final Thread threads[] = new Thread[numThreads];
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
			final ThreadGroup groups[] = new ThreadGroup[numGroups];
			currentGroup.enumerate(groups);

			for (int i = groups.length - 1; i >= 0; i--)
				dumpThreadGroup(groups[i], level + 1, out);
		}

	}

	/**
	 * Dumps all the thread groups. Invokes the dumpThreadGroup method for
	 * dumping recursively each thread group.
	 * 
	 */
	public static void dumpThreads(final PrintStream stream) {
		for (ThreadGroup currentGroup = Thread.currentThread().getThreadGroup(); currentGroup != null; currentGroup = currentGroup.getParent())
			dumpThreadGroup(currentGroup, 0, stream);
	}
	static private void showThreadGroup(final ThreadGroup g, final int level) {
		int i;
		final String indent = "                                 ".substring(0, level);

		System.out.println(indent + "{" + g.toString() + "}");

		int n = g.activeCount();

		if (n > 0) {
			System.out.println(indent + " + Threads");

			final Thread t[] = new Thread[g.activeCount()];

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
			final ThreadGroup tg[] = new ThreadGroup[n];

			g.enumerate(tg);
			for (i = 0; i < tg.length; i++) {
				if (g == tg[i].getParent()) {
					showThreadGroup(tg[i], level + 4);
				}
			}
		}
	}
	static void showThreads() {
		ThreadGroup g = null;

		for (g = Thread.currentThread().getThreadGroup(); g.getParent() != null; g = g.getParent()) {
		}

		showThreadGroup(g, 0);
	}
	/*
	 * Aglet menu - menu items and shortcut buttons.
	 */
	private final MenuItem _dialogMenuItem = new MenuItem();

	private final MenuItem _disposeMenuItem = new MenuItem();
	private final MenuItem _cloneMenuItem = new MenuItem();

	private final MenuItem _infoMenuItem = new MenuItem();
	private final MenuItem _killMenuItem = new MenuItem();

	//
	private final Button _disposeButton = new Button();

	private final Button _dialogButton = new Button();

	private final Button _cloneButton = new Button();
	private final Button _infoButton = new Button();

	/*
	 * Mobility menu - menu items and shortcut buttons.
	 */
	private final MenuItem _dispatchMenuItem = new MenuItem();
	private final Button _dispatchButton = new Button();

	//
	private final MenuItem _deactivateMenuItem = new MenuItem();
	private final MenuItem _activateMenuItem = new MenuItem();

	//
	private MenuItem _javaConsoleMenuItem = new MenuItem();

	//
	private Tahiti _tahiti = null;
	//
	private final Vector _itemList = new Vector();
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

	// TODO gestione eventi qui sotto

	/**
	 * The base key for this class.
	 */
	private final String baseKey = this.getClass().getName();

	/**
	 * The window manager for this window.
	 */
	private WindowManager windowManager = null;

	// Size of message line.--
	//
	private final int _messageLineSize = 60;;

	// The message line.
	//
	private final TextField _messageLine = new TextField(_messageLineSize);

	/*
	 * Constructs the instance of the main window for the Tahiti Aglet viewer.
	 */
	MainWindow(final Tahiti tahiti) {
		super(false);
		_tahiti = tahiti;

		// set the title for this window
		setTitle(JComponentBuilder.getTitle(baseKey));

		// init the components
		memoryPanel = new MemoryPanel(200, 200, true, true);
		agletList = new AgletListPanel<AgletProxy>();
		agletList.setRenderer(new AgletListRenderer(agletList));
		agletList.addListSelectionListener(this);
		menuBar = new TahitiMenuBar(this);
		toolBar = new TahitiToolBar(this);

		// set the layout
		setLayout(new BorderLayout());

		// add the components
		this.add(agletList, BorderLayout.CENTER);
		this.add(memoryPanel, BorderLayout.SOUTH);
		this.add(toolBar, BorderLayout.NORTH);
		setJMenuBar(menuBar);

		// add the window manager to this window
		windowManager = new WindowManager(this);
		addWindowListener(windowManager);
		shouldExitOnClosing = true;

		// pack the window and show it
		pack();
		setVisible(true);

		try {
			// get the aglet runtime and prepare it
			final AgletRuntime runtime = AgletRuntime.getAgletRuntime();
			final String hosting = runtime.getServerAddress();
			String ownerName = runtime.getOwnerName();

			if (ownerName == null)
				ownerName = "[NO USER]";

			// get a few resources (i.e., wrappers around the
			// system properties) for the aglets
			final String titleAddition = " " + ownerName + "@" + hosting + " - "
			+ getInformativeTitle();
			setTitle(getTitle() + titleAddition);
		} catch (final Exception e) {
			logger.error("Cannot get an Aglet Runtime instance! Cannot continue!", e);
			setTitle(getTitle() + " "
					+ translator.translate(baseKey + ".title.error"));
			return;
		}

	}

	// -------------------------------------------------------------------
	// -- Message line

	/**
	 * Manages events from the GUI within the Tahiti server. Commands issued are
	 * divided into two groups: those that require a selection in the aglet list
	 * and those that do not require it. The former kind of commands accept
	 * multiple selection, and the right action will be processed for each
	 * selected aglet. Please note that this method has been designed to reduce
	 * the code, thus the dialog for each action is associated to a global
	 * TahitiDialog instance, and then it is made modal and visible...
	 */
	public void actionPerformed(final ActionEvent event) {
		// check params
		if (event == null)
			return;

		// get the action command
		final String command = event.getActionCommand();

		// get all the selected proxies
		final LinkedList<AgletProxy> selectedProxies = getSelectedProxies();

		if (commandRequiresAgletSelected(command)) {
			// the command requires an aglet to be selected
			// check that at least one aglet has been selected
			if ((selectedProxies == null) || (selectedProxies.size() == 0)) {
				JOptionPane.showMessageDialog(this, baseKey
						+ ".errorMessage.selectionEmpty", baseKey
						+ ".errorMessage.selectionEmpty.title", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// iterate on each aglet proxy
			final Iterator iter = selectedProxies.iterator();
			while ((iter != null) && iter.hasNext()) {
				final AgletProxy currentProxy = (AgletProxy) iter.next();
				BaseAgletsDialog dialog = null;

				// now do the right action
				if (GUICommandStrings.CLONE_AGLET_COMMAND.equals(command))
					dialog = new CloneAgletDialog(this, currentProxy);
				else if (GUICommandStrings.ACTIVATE_AGLET_COMMAND.equals(command))
					activateAglet(currentProxy);
				else if (GUICommandStrings.DEACTIVATE_AGLET_COMMAND.equals(command)) {
					// ask the user how many time it will deactivate the agent
					final String deactivationTime = JOptionPane.showInputDialog(this, translator.translate(baseKey
							+ ".deactivationTime"), translator.translate(baseKey
									+ ".deactivationTime.title"), JOptionPane.YES_NO_OPTION);
					try {
						final long millis = Long.parseLong(deactivationTime);
						deactivateAglet(currentProxy, millis);
					} catch (final NumberFormatException e) {
						logger.debug("Specified time for deactivation cannot be converted into a number of millis:  "
								+ deactivationTime);
					}
				} else if (GUICommandStrings.MESSAGE_AGLET_COMMAND.equals(command))
					message(currentProxy, new Message("dialog"));
				else if (GUICommandStrings.DISPOSE_AGLET_COMMAND.equals(command))
					// dispose all the aglets
					dialog = new DisposeAgletDialog(this, currentProxy);
				else if (GUICommandStrings.SLEEP_AGLET_COMMAND.equals(command)) {
					// the user wants to make the agent sleeping
					try {
						final AgletProxyImpl proxyImpl = (AgletProxyImpl) currentProxy;
						final Aglet aglet = proxyImpl.getAglet();

						final String deactivationTime = JOptionPane.showInputDialog(this, translator.translate(baseKey
								+ ".sleepTime"), translator.translate(baseKey
										+ ".sleepTime.title"), JOptionPane.YES_NO_OPTION);
						final long millis = Long.parseLong(deactivationTime);

						aglet.sleep(millis);

					} catch (final Exception e) {
						logger.error("Exception caught while trying to call a sleep within the aglet", e);
						JOptionPane.showMessageDialog(this, translator.translate(baseKey
								+ ".error.sleepMessage"), translator.translate(baseKey
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
				logger.info("Forcing the garbage collector");
				System.gc();
			} else if (GUICommandStrings.REBOOT_COMMAND.equals(command)) {
				// ask confirmation for rebooting
				if (JOptionPane.showConfirmDialog(getParent(), translator.translate(baseKey
						+ ".rebootMessage"), translator.translate(baseKey
								+ ".rebootMessage.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					reboot();
			} else if (GUICommandStrings.EXIT_COMMAND.equals(command)) {
				// ask confirmation for rebooting
				if (JOptionPane.showConfirmDialog(getParent(), translator.translate(baseKey
						+ ".shutdownMessage"), translator.translate(baseKey
								+ ".shutdownMessage.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)

					shutdown();
			} else if (GUICommandStrings.RETRACT_AGLET_COMMAND.equals(command))
				dialog = new RetractAgletDialog(this);
			else if (GUICommandStrings.REDUCE_COMMAND.equals(command)) {
				// the user wants to reduce the size of the window, thus
				// remove the aglet list
				this.remove(agletList);
				pack();
			} else if (GUICommandStrings.ENLARGE_COMMAND.equals(command)) {
				// enlarge the window, thus re-add the
				// aglet list panel
				this.add(agletList, BorderLayout.CENTER);
				pack();
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
				final String docRoot = translator.translate(baseKey
						+ ".documentationFile");
				showDocumentation(docRoot);
				return;
			} else if (GUICommandStrings.WEB_COMMAND.equals(command)) {
				// get the URL of the site
				final String webPage = translator.translate(baseKey
						+ ".webSite");
				try {
					final URL url = new URL(webPage);
					showWebSite(url);
				} catch (final MalformedURLException e) {
					logger.error("Exception caught while trying to open the web page", e);
				}

				return;
			} else if (GUICommandStrings.MEMORY_COMMAND.equals(command))
				// show the memory dialog
				dialog = new MemoryDialog(this);
			else if (GUICommandStrings.GARBAGECOLLECTOR_COMMAND.equals(command)) {
				// invoke the garbage collector
				logger.info("Invoking the garbage collector...");
				System.gc();
				logger.info("Garbage collector invoked.");
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".garbageCollector"), translator.translate(baseKey
								+ ".garbageCollector.title"), JOptionPane.INFORMATION_MESSAGE, JComponentBuilder.getIcon(baseKey
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
	 * Activates the aglet specified by the proxy.
	 * 
	 * @param p
	 *            the proxy of the agent to activate
	 */
	public void activateAglet(final AgletProxy p) {
		SwingUtilities.invokeLater(new TahitiEventHandler(ACTIVATE, p));
	}

	private void addListeners() {
		_agletList.addItemListener(this);
		_agletList.addActionListener(this);
	}

	// Clears the message line.
	//
	private void clearMessage() {
		_messageLine.setText("");
	}

	/**
	 * Clones the specified agent.
	 * 
	 * @param p
	 *            the proxy of the agent to clone
	 */
	public void cloneAglet(final AgletProxy p) {
		SwingUtilities.invokeLater(new TahitiEventHandler(CLONE, p));
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
	protected final boolean commandRequiresAgletSelected(final String command) {
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
	public void createAglet(final String codebase, final String name, final boolean reload) {
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
	public void deactivateAglet(final AgletProxy p, final long time) {
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
	protected void dialog(final AgletProxy proxy) {
		try {
			proxy.sendAsyncMessage(new Message("dialog"));
		} catch (final InvalidAgletException ex) {
			setMessage(ex.getMessage());
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
	public void dispatchAglet(final AgletProxy p, final URL dest) {
		SwingUtilities.invokeLater(new TahitiEventHandler(p, dest));
		// new Thread(new TahitiEventHandler(p, dest)).start();

	}

	/**
	 * Disposes the agent.
	 * 
	 * @param p
	 *            the proxy of the agent to dispose
	 */
	public void disposeAglet(final AgletProxy p) {
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

	/**
	 * Checks some run-time parameters to and returns a descriptive string to
	 * place in the window title.
	 */
	private String getInformativeTitle() {
		final Resource aglets_res = Resource.getResourceFor("aglets");
		final boolean bsecure = aglets_res.getBoolean("aglets.secure", true);
		final Resource atp_res = Resource.getResourceFor("atp");
		boolean brunning = true;

		if (atp_res != null)
			brunning = atp_res.getBoolean("atp.server", false);

		String titleAddition = "";
		if (brunning)
			titleAddition = translator.translate(baseKey
					+ ".title.running");
		else
			titleAddition = translator.translate(baseKey
					+ ".title.notrunning");

		if (bsecure)
			titleAddition += translator.translate(baseKey
					+ ".title.secure");
		else
			titleAddition += translator.translate(baseKey
					+ ".title.unsecure");

		return titleAddition;
	}

	private String getItemText(final TahitiItem tahitiItem) {
		final StringBuffer buffer = new StringBuffer();

		buffer.append(tahitiItem.getText());
		final AgletProxy proxy = tahitiItem.getAgletProxy();
		final String s = (String) text.get(proxy);

		buffer.append(" " + (s == null ? " " : s));

		return buffer.toString();
	}

	/**
	 * Provides a list of selected proxies in the aglet list.
	 */
	protected LinkedList<AgletProxy> getSelectedProxies() {
		return agletList.getSelectedItems();
	}

	/**
	 * Returns the first (and maybe the only) selected item in the aglet list
	 * panel.
	 * 
	 * @return the aglet proxy of the selected element
	 */
	protected AgletProxy getSelectedProxy() {
		return agletList.getSelectedItem();
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
		_dialogButton.setVisible(false);
		_infoButton.setVisible(false);
		_disposeButton.setVisible(false);
		_cloneButton.setVisible(false);
		_dispatchButton.setVisible(false);
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

	/**
	 * Adds a proxy to the proxy list.
	 * 
	 * @param proxy
	 *            the proxy to add
	 */
	protected synchronized void insertProxyToList(final AgletProxy proxy) {
		if (proxy == null)
			return;
		else
			agletList.addItem(proxy);

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
	public void itemStateChanged(final ItemEvent event) {

	}

	// Builds the button panel.
	// @return the button panel.
	//
	private Panel makeButtonPanel() {
		_buttonPanel = new Panel();
		_buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// ADD LISTENER
		Button b = new Button("><");

		b.addActionListener(new EventIssuer(SHRINK));
		_buttonPanel.add(b);

		b = new Button(bundle.getString("button.create"));
		b.addActionListener(new DialogOpener(CREATE));
		_buttonPanel.add(b);

		_dialogButton.setLabel(bundle.getString("button.dialog"));
		_dialogButton.addActionListener(new EventIssuer(DIALOG));
		_buttonPanel.add(_dialogButton);

		_infoButton.setLabel(bundle.getString("button.info"));
		_infoButton.addActionListener(new DialogOpener(AGLET_INFO));
		_buttonPanel.add(_infoButton);

		_disposeButton.setLabel(bundle.getString("button.dispose"));
		_disposeButton.addActionListener(new DialogOpener(DISPOSE));
		_buttonPanel.add(_disposeButton);

		_cloneButton.setLabel(bundle.getString("button.clone"));
		_cloneButton.addActionListener(new DialogOpener(CLONE));
		_buttonPanel.add(_cloneButton);

		_dispatchButton.setLabel(bundle.getString("button.dispatch"));
		_dispatchButton.addActionListener(new DialogOpener(DISPATCH));
		_buttonPanel.add(_dispatchButton);

		b = new Button(bundle.getString("button.retract"));
		b.addActionListener(new DialogOpener(RETRACT));
		_buttonPanel.add(b);

		return _buttonPanel;
	}

	// -------------------------------------------------------------------
	// -- Menu memoryBar and panel methods

	// Builds the menu memoryBar.
	//
	private MenuBar makeMenuBar() {
		final MenuBar menubar = new MenuBar();

		//
		// Aglet Menu
		//
		Menu menu = new Menu(bundle.getString("menu.aglet"));

		MenuItem item = new MenuItem(bundle.getString("menuitem.create"));

		item.addActionListener(new DialogOpener(CREATE));
		menu.add(item);

		_dialogMenuItem.setLabel(bundle.getString("menuitem.dialog"));
		_dialogMenuItem.addActionListener(new EventIssuer(DIALOG));
		menu.add(_dialogMenuItem);

		_disposeMenuItem.setLabel(bundle.getString("menuitem.dispose"));
		_disposeMenuItem.addActionListener(new DialogOpener(DISPOSE));
		menu.add(_disposeMenuItem);

		_cloneMenuItem.setLabel(bundle.getString("menuitem.clone"));
		_cloneMenuItem.addActionListener(new DialogOpener(CLONE));
		menu.add(_cloneMenuItem);

		_infoMenuItem.setLabel(bundle.getString("menuitem.info"));
		_infoMenuItem.addActionListener(new DialogOpener(AGLET_INFO));
		menu.add(_infoMenuItem);

		menu.addSeparator();

		_killMenuItem.setLabel(bundle.getString("menuitem.kill"));
		_killMenuItem.addActionListener(new EventIssuer(KILL));
		menu.add(_killMenuItem);

		menu.addSeparator();

		item = new MenuItem(bundle.getString("menuitem.exit"));
		item.addActionListener(new DialogOpener(EXIT));
		menu.add(item);

		menubar.add(menu);

		//
		// Mobility menu.
		//
		menu = new Menu(bundle.getString("menu.mobility"));

		_dispatchMenuItem.setLabel(bundle.getString("menuitem.dispatch"));
		_dispatchMenuItem.addActionListener(new DialogOpener(DISPATCH));
		menu.add(_dispatchMenuItem);

		item = new MenuItem(bundle.getString("menuitem.retract"));
		item.addActionListener(new DialogOpener(RETRACT));
		menu.add(item);

		_deactivateMenuItem.setLabel(bundle.getString("menuitem.deactivate"));
		_deactivateMenuItem.addActionListener(new DialogOpener(DEACTIVATE));
		menu.add(_deactivateMenuItem);

		_activateMenuItem.setLabel(bundle.getString("menuitem.activate"));
		_activateMenuItem.addActionListener(new EventIssuer(ACTIVATE));
		menu.add(_activateMenuItem);

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

		_javaConsoleMenuItem = new MenuItem(bundle.getString("menuitem.javaconsole"));
		_javaConsoleMenuItem.addActionListener(new DialogOpener(SHOW_JAVACON));
		if (com.ibm.awb.launcher.Agletsd.console != null) {
			_javaConsoleMenuItem.setEnabled(true);
		} else {
			_javaConsoleMenuItem.setEnabled(false);
		}
		menu.add(_javaConsoleMenuItem);

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

		final Resource res = Resource.getResourceFor("aglets");

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
	 * Sends the specified message to the specified agent.
	 * 
	 * @param proxy
	 *            the proxy of the agent that must receive the message
	 * @param message
	 *            the message to send
	 */
	protected void message(final AgletProxy proxy, final Message message) {
		try {
			if ((proxy == null) || (message == null))
				return;

			// send the message
			proxy.sendAsyncMessage(message);

		} catch (final AgletException e) {
			logger.error("Exception caught while sending a message to an agent", e);

		}
	}

	/**
	 * Reboots the tahiti server.
	 * 
	 */
	public void reboot() {
		logger.info("Tahiti is rebooting....");
		_tahiti.reboot = true;
		_tahiti.reboot();
	}

	/**
	 * Removes an element from the proxy list.
	 * 
	 * @param proxy
	 *            the proxy to remove
	 */
	protected synchronized void removeProxyFromList(final AgletProxy proxy) {
		// check params
		if (proxy == null)
			return;
		else
			agletList.removeItem(proxy);
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

	public void restoreSize() {
		final Resource res = Resource.getResourceFor("tahiti");

		this.setSize(res.getInteger("tahiti.window.width", 100), res.getInteger("tahiti.window.height", 100));
	}

	public void retractAglet(final AgletProxy p) {
		SwingUtilities.invokeLater(new TahitiEventHandler(RETRACT, p));
	}

	/**
	 * Saves the size of the window so that it can be restored once the window
	 * is restarted.
	 */
	private void saveSize() {
		final java.awt.Rectangle bounds = this.getBounds();
		final Resource res = Resource.getResourceFor("tahiti");

		res.setResource("tahiti.window.x", String.valueOf(bounds.x));
		res.setResource("tahiti.window.y", String.valueOf(bounds.y));
		res.setResource("tahiti.window.width", String.valueOf(bounds.width));
		res.setResource("tahiti.window.height", String.valueOf(bounds.height));
		res.setResource("tahiti.window.shrinked", "false");

	}

	public void setFont(final Font f) {
		final MenuBar menubar = getMenuBar();

		if (menubar != null) {
			menubar.setFont(f);

			final int c = menubar.getMenuCount();

			for (int i = 0; i < c; i++) {
				final Menu m = menubar.getMenu(i);

				m.setFont(f);
			}
		}
		super.setFont(f);
		doLayout();
	}

	// Updates the message line.
	// @param message the new message.
	//
	void setMessage(final String message) {
		_messageLine.setText(message);
	}

	void showButtons() {
		_dialogButton.setVisible(true);
		_infoButton.setVisible(true);
		_disposeButton.setVisible(true);
		_cloneButton.setVisible(true);
		_dispatchButton.setVisible(true);
	}

	/**
	 * Show the documentation of the platform.
	 * 
	 * @param documentRoot
	 *            the documentation file to show
	 */
	protected void showDocumentation(final String documentRoot) {
		final TahitiBrowser browser = new TahitiBrowser(this, documentRoot);
		browser.run();
	}

	/**
	 * Display a dialog to notify the user of an exception.
	 * 
	 * @param ex
	 */
	public final void showException(final Exception ex) {
		if (ex == null)
			return;

		// display an error dialog
		JOptionPane.showMessageDialog(this, ex.getStackTrace(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);

	}

	// -------------------------------------------------------------------
	// -- Window updating

	void showURL(final String url) {
		final Resource res = Resource.getResourceFor("tahiti");
		final String command = res.getString("tahiti.browser_command", null);

		if (command != null) {
			try {
				final StringTokenizer st = new StringTokenizer(command);
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
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Show the web site.
	 * 
	 * @param webSite
	 *            the url of the web site to show.
	 */
	protected void showWebSite(final URL webSite) {
		final TahitiBrowser browser = new TahitiBrowser(this, webSite);
		browser.run();
	}

	/*
	 * Shows the given dialog at the center
	 */
	private void showWindow(final Window window) {
	}

	/**
	 * Shuts down the Tahiti server. The tahiti server has the information about
	 * the rebooting!
	 * 
	 */
	public void shutdown() {
		logger.info("Shutting down the Tahiti server");
		_tahiti.reboot = false;
		_tahiti.exit();
	}

	void updateGUIState() {
		final int indexes[] = _agletList.getSelectedIndexes();

		final boolean single = indexes.length == 1;
		final boolean multiple = indexes.length >= 1;

		_dialogMenuItem.setEnabled(single);
		_dialogButton.setEnabled(single);

		_disposeMenuItem.setEnabled(multiple);
		_disposeButton.setEnabled(multiple);

		_killMenuItem.setEnabled(single);

		_cloneMenuItem.setEnabled(single);
		_cloneButton.setEnabled(single);

		_infoMenuItem.setEnabled(single);
		_infoButton.setEnabled(single);

		_dispatchMenuItem.setEnabled(single);
		_dispatchButton.setEnabled(single);

		// _retractMenuItem.setEnabled(); _retractButton.setEnabled();

		_deactivateMenuItem.setEnabled(single);
		_activateMenuItem.setEnabled(single);
	}

	/**
	 * Does nothing, no more required.
	 * 
	 * @param proxy
	 */
	@Deprecated
	synchronized void updateProxyInList(final AgletProxy proxy) {
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
		logger.info("The updateProxyInList method call is no longer required, since the AgletList class now stores proxies");
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
		logger.info("The updateProxyInList method call is no longer required, since the AgletList class now stores proxies");
	}

	/**
	 * Manages events from the agent list panel. Each time the selection on the
	 * panel changes, this method enables/disables GUI components such as menues
	 * and buttons to better guide the user thru the action it can take in a
	 * specific moment.
	 */
	public void valueChanged(final ListSelectionEvent event) {
		// check params
		if (event == null)
			return;

		// change the status of the buttons and of the menues
		// depending on the selection on the list
		boolean enable = false;

		if (agletList.getItemCount() > 0)
			enable = true;
		else
			enable = false;

		// enable/disable conditional elements
		menuBar.enableConditionalItems(enable);
		toolBar.enableConditionalButtons(enable);

	}
}
