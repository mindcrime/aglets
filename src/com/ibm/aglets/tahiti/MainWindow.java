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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.Message;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;

// import com.ibm.aglets.agletbox.AgletBox;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.net.URL;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * The <tt>MainWindow</tt> represents the main window for the Tahiti aglet
 * viewer.
 * 
 * @version     1.08    $Date: 2001/07/28 06:33:02 $
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 * @author      Yoshiaki Mima
 */

/*
 * Aglets List
 * 
 * Ordered by Keys "Creation time", "Class name"
 * 
 */
final class MainWindow extends Frame implements ItemListener, ActionListener {
	static ResourceBundle bundle = null;
	static {
		bundle = 
			(ResourceBundle)AccessController
				.doPrivileged(new PrivilegedAction() {
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
	boolean shrink = false;

	/* display options */
	static final int ORDER_CREATIONTIME = 0;
	static final int ORDER_CLASSNAME = 1;

	// 
	private int viewOrder = ORDER_CREATIONTIME;
	private boolean isAscent = true;
	private boolean isCompact = false;

	/* package */
	LogWindow logWindow = new LogWindow();

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

	// 
	class URLOpener implements ActionListener {
		private String url;

		URLOpener(String u) {
			url = u;
		}
		public void actionPerformed(ActionEvent ev) {
			showURL(url);
		} 
	}
	;
	class EventIssuer implements ActionListener {
		private int _type = 0;

		EventIssuer(int t) {
			_type = t;
		}
		public void actionPerformed(ActionEvent ev) {
			switch (_type) {
			case SHRINK:
				Button b = (Button)ev.getSource();

				if (shrink) {
					shrink = false;
					b.setLabel("><");
					showButtons();
					updateProxyList();
					_agletList.setVisible(true);
					restoreSize();
					doLayout();
					_buttonPanel.doLayout();
				} else {
					b.setLabel("<>");
					saveSize();
					hideButtons();
					_agletList.setVisible(false);
					setSize(350, 
							getBounds().height 
							- _agletList.getBounds().height);
					doLayout();
					_buttonPanel.doLayout();
					shrink = true;
				} 
				break;
			case GC:
				System.gc();
				break;
			case DIALOG:
				dialog(getSelectedProxy());
				break;
			case SHOW_THREADS:
				showThreads();
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
				AgletProxy p = getSelectedProxy();

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
			_type = type;
		}
		public void actionPerformed(ActionEvent ev) {
			final AgletProxy p = getSelectedProxy();

			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					TahitiDialog d = null;

					switch (_type) {
					case CREATE:
						d = CreateAgletDialog.getInstance(MainWindow.this);
						d.popupAtCenterOfParent();
						break;
					case RETRACT:
						d = RetractAgletDialog.getInstance(MainWindow.this);
						d.popupAtCenterOfParent();
						break;
					case DISPOSE:
						AgletProxy pp[] = new AgletProxy[1];

						pp[0] = p;
						d = new DisposeAgletDialog(MainWindow.this, pp);
						d.popupAtCenterOfParent();
						break;
					case CLONE:
						d = new CloneAgletDialog(MainWindow.this, p);
						d.popupAtCenterOfParent();
						break;
					case DISPATCH:
						d = new DispatchAgletDialog(MainWindow.this, p);
						d.popupAtCenterOfParent();
						break;
					case DEACTIVATE:
						d = new DeactivateAgletDialog(MainWindow.this, p);
						d.popupAtCenterOfParent();
						break;
					case AGLET_INFO:
						d = new PropertiesDialog(MainWindow.this, 
												 getSelectedProxy());
						d.popupAtCenterOfParent();
						break;
					case MEMORY_USAGE:
						d = MemoryUsageDialog.getInstance(MainWindow.this);
						d.popupAtCenterOfParent();
						break;
					case SHOW_LOG:
						logWindow.pack();
						if (logWindow.getLocation().x == 0 
								&& logWindow.getLocation().y == 0) {
							Dimension dim = getToolkit().getScreenSize();
							Dimension size = logWindow.getSize();

							logWindow
								.setLocation((dim.width - size.width) / 2, 
											 (dim.height - size.height) / 2);
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
						d = 
							TahitiDialog
								.message(MainWindow.this, 
										 bundle.getString("title.about_tahiti"), 
										 res.getString("tahiti.version") 
										 + "\n" 
										 + bundle
											 .getString("message.copyright"));
						d.popupAtCenterOfParent();
						break;
					case ABOUT_AGLETS:
						res = Resource.getResourceFor("aglets");
						String msg = "Aglets Version : " 
									 + res.getString("aglets.version") + "\n" 
									 + "Aglets API : " + Aglet.MAJOR_VERSION 
									 + "." + Aglet.MINOR_VERSION + "\n" 
									 + "Aglet Transfer Format : " 
									 + res.getString("aglets.stream.version") 
									 + "\n" + "\n" 
									 + res.getString("aglets.copyright");

						d = TahitiDialog.message(MainWindow.this, 
												 "About Aglets", msg);
						d.popupAtCenterOfParent();
						break;
					case EXIT:
						d = ShutdownDialog.getInstance(MainWindow.this);
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
	private TextField _messageLine = new TextField(_messageLineSize);

	public class TahitiEventHandler implements Runnable {
		int _type;
		AgletProxy _proxy;
		long _time;
		String _remoteURL;
		String _codebase;
		String _name;
		boolean _reload;

		TahitiEventHandler(int type, AgletProxy p) {
			_type = type;
			_proxy = p;
		}
		TahitiEventHandler(AgletProxy p, long time) {
			_type = DEACTIVATE;
			_proxy = p;
			_time = time;
		}
		TahitiEventHandler(AgletProxy p, String dest) {
			_type = DISPATCH;
			_proxy = p;
			_remoteURL = dest;
		}
		TahitiEventHandler(String codebase, String name, boolean reload) {
			_type = CREATE;
			_codebase = codebase;
			_name = name;
			_reload = reload;
		}
		public void run() {
			com.ibm.awb.misc.Debug.start();
			com.ibm.awb.misc.Debug.check();
			try {
				perform();
			} catch (Error ex) {
				ex.printStackTrace();
				TahitiDialog
					.message(MainWindow.this, "Error", ex.getClass()
						.getName() + "\n" + ex.getMessage())
							.popupAtCenterOfParent();
			} catch (Exception ex) {
				ex.printStackTrace();
				TahitiDialog
					.message(MainWindow.this, "Exception", ex.getClass()
						.getName() + "\n" + ex.getMessage())
							.popupAtCenterOfParent();
			} 
			finally {
				com.ibm.awb.misc.Debug.end();
			} 
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
						_codebase = _codebase.substring(0, 
														_codebase.length() 
														- 1);
					} 
				} 
				URL url = !_codebase.equals("") ? new URL(_codebase) : null;

				if (_reload) {
					Tahiti.CONTEXT.clearCache(null);
				} 
				Tahiti.CONTEXT.createAglet(url, _name, null);

				// itrptWin.waitForDisplay();
				// itrptWin.dispose();
				// update list
				break;
			case DISPATCH:

				// String key = _remoteURL + " " +
				// _proxy.getAgletClassName() + " " +
				// (new Date()).toString().substring(0, 20).trim();
				// String aid_str = _proxy.getAgletID().toString();
				_proxy.dispatch(new URL(_remoteURL));

				// itrptWin.waitForDisplay();
				// itrptWin.dispose();
				break;
			case RETRACT:
				AgletInfo info = _proxy.getAgletInfo();

				// System.out.println(info);
				Tahiti.CONTEXT.retractAglet(new URL(_proxy.getAddress()), 
											info.getAgletID());

				// itrptWin.waitForDisplay();
				// itrptWin.dispose();
				break;
			case DEACTIVATE:
				_proxy.deactivate(_time * 1000);
				break;
			case ACTIVATE:
				if (_proxy != null && _proxy.isValid() 
						&& _proxy.isActive() == false) {
					_proxy.activate();
				} 
				break;
			default:
			}
		} 
	}

	/*
	 * Constructs the instance of the main window for the
	 * Tahiti Aglet viewer.
	 */
	MainWindow(Tahiti tahiti) {
		_tahiti = tahiti;

		Util.setFont(this);
		Util.setBackground(this);
		Util.setFixedFont(_agletList);

		TahitiItem.init();

		_agletList.setBackground(Color.white);

		// Makes the menu bar.
		setMenuBar(makeMenuBar());

		// Creates the panels.
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints cns = new GridBagConstraints();

		setLayout(grid);
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.NONE;
		cns.anchor = GridBagConstraints.WEST;
		cns.weightx = 1.0;
		cns.weighty = 0.0;

		Panel p = makeButtonPanel();

		grid.setConstraints(p, cns);
		add(p);

		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;
		cns.anchor = GridBagConstraints.CENTER;

		grid.setConstraints(_agletList, cns);

		addListeners();

		add(_agletList);

		Resource tahiti_res = Resource.getResourceFor("tahiti");

		shrink = tahiti_res.getBoolean("tahiti.window.shrinked", false);
		if (shrink) {
			_agletList.setVisible(false);
		} 

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		grid.setConstraints(_messageLine, cns);
		add(_messageLine);

		_messageLine.setText("Tahiti - The Aglet Viewer is Running...");
		_messageLine.setBackground(Color.lightGray);
		_messageLine.setEditable(false);

		updateGUIState();

		AgletRuntime runtime = AgletRuntime.getAgletRuntime();
		String hosting = runtime.getServerAddress();
		String ownerName = runtime.getOwnerName();

		if (ownerName == null) {
			ownerName = "NO USER";
		} 

		Resource aglets_res = Resource.getResourceFor("aglets");
		boolean bsecure = aglets_res.getBoolean("aglets.secure", true);
		Resource atp_res = Resource.getResourceFor("atp");
		boolean brunning = true;

		if (atp_res != null) {
			brunning = atp_res.getBoolean("atp.server", false);
		} 

		setTitle("Tahiti: The Aglet Viewer [" + hosting + " (" + ownerName 
				 + ")" + (brunning ? "" : " : NOT RUNNING") + " ]" 
				 + (bsecure ? "" : " - << UNSECURE MODE >>"));
	}
	public void actionPerformed(ActionEvent ev) {
		int selected[] = _agletList.getSelectedIndexes();

		for (int i = 0; i < selected.length; i++) {
			AgletProxy p = 
				((TahitiItem)_itemList.elementAt(selected[i]))
					.getAgletProxy();

			if (p.isState(Aglet.INACTIVE)) {
				activateAglet(p);
			} else if (p.isState(Aglet.ACTIVE)) {
				dialog(p);
			} 
		} 
	}
	public void activateAglet(AgletProxy p) {
		new Thread(new TahitiEventHandler(ACTIVATE, p)).start();
	}
	private void addListeners() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				TahitiDialog dialog = 
					ShutdownDialog.getInstance(MainWindow.this);

				dialog.popupAtCenterOfParent();
			} 
		});

		_agletList.addItemListener(this);
		_agletList.addActionListener(this);
	}
	// Clears the message line.
	// 
	private void clearMessage() {
		_messageLine.setText("");
	}
	public void cloneAglet(AgletProxy p) {
		new Thread(new TahitiEventHandler(CLONE, p)).start();
	}
	public void createAglet(String codebase, String name, boolean reload) {
		new Thread(new TahitiEventHandler(codebase, name, reload)).start();
	}
	public void deactivateAglet(AgletProxy p, long time) {
		new Thread(new TahitiEventHandler(p, time)).start();
	}
	void dialog(AgletProxy proxy) {
		try {
			proxy.sendAsyncMessage(new Message("dialog"));
		} catch (InvalidAgletException ex) {
			setMessage(ex.getMessage());
		} 
	}
	public void dispatchAglet(AgletProxy p, String dest) {
		new Thread(new TahitiEventHandler(p, dest)).start();
	}
	public void disposeAglet(AgletProxy p) {
		new Thread(new TahitiEventHandler(DISPOSE, p)).start();
	}
	/*
	 * Retract aglets from an Aglet box
	 */
	void getAglets() {

		/*
		 * try {
		 * AgletBox.update(Tahiti.CONTEXT);
		 * } catch (Exception e) {
		 * setMessage("AgletBox: " + e.getMessage());
		 * }
		 */
	}
	// Returns an Aglet's item text line for the Aglet list.
	// 

	/*
	 * private String getItemText(AgletProxy proxy) {
	 * StringBuffer buffer = new StringBuffer();
	 * try {
	 * AgletInfo info = proxy.getAgletInfo();
	 * if (proxy.isValid() == false) {
	 * return "InvalidAglet";
	 * }
	 * if (proxy.isActive() == false) {
	 * buffer.append("[deactivated]");
	 * }
	 * buffer.append(new Date(info.getCreationTime()));
	 * buffer.append(" " + info.getAgletClassName() + ' ');
	 * String s = (String) text.get(proxy);
	 * buffer.append(" " + (s == null ? " " : s));
	 * } catch (InvalidAgletException ex) {
	 * return "InvalidAglet";
	 * } catch (RuntimeException ex) {
	 * ex.printStackTrace();
	 * } finally {
	 * }
	 * return buffer.toString();
	 * }
	 */

	private String getItemText(TahitiItem tahitiItem) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(tahitiItem.getText());
		AgletProxy proxy = tahitiItem.getAgletProxy();
		String s = (String)text.get(proxy);

		buffer.append(" " + (s == null ? " " : s));

		return buffer.toString();
	}
	public Dimension getPreferredSize() {
		Resource res = Resource.getResourceFor("tahiti");

		if (!shrink) {
			return new Dimension(res.getInteger("tahiti.window.width", 545), 
								 res.getInteger("tahiti.window.height", 350));
		} else {
			return new Dimension(res.getInteger("tahiti.window.s_width", 545), 
								 res.getInteger("tahiti.window.s_height", 
												350));
		} 
	}
	AgletProxy[] getSelectedProxies() {
		int selected[] = _agletList.getSelectedIndexes();
		AgletProxy p[] = new AgletProxy[selected.length];

		for (int i = 0; i < p.length; i++) {
			p[i] = 
				((TahitiItem)_itemList.elementAt(selected[i]))
					.getAgletProxy();
		} 
		return p;

		/*
		 * if (selected != -1)
		 * return (AgletProxy)_proxyList.elementAt(selected);
		 * else
		 * return null;
		 */
	}
	AgletProxy getSelectedProxy() {
		int selected = _agletList.getSelectedIndex();

		if (selected != -1) {
			return ((TahitiItem)_itemList.elementAt(selected))
				.getAgletProxy();
		} else {
			return null;
		} 
	}
	void hideButtons() {
		_dialogButton.setVisible(false);
		_infoButton.setVisible(false);
		_disposeButton.setVisible(false);
		_cloneButton.setVisible(false);
		_dispatchButton.setVisible(false);
	}
	/*
	 * synchronized void updateViewItems() {
	 * for (int i = 0; i < _itemList.size(); i++) {
	 * TahitiItem tahitiItem = (TahitiItem)_itemList.elementAt(i);
	 * _agletList.replaceItem(getItemText(tahitiItem), i);
	 * }
	 * }
	 */

	synchronized void insertProxyToList(AgletProxy proxy) {

		if (shrink) {
			return;
		} 

		TahitiItem tahitiItem = new TahitiItem(proxy);

		int index = -1;

		for (int i = 0; i < _itemList.size(); i++) {
			if (tahitiItem.compareTo((TahitiItem)_itemList.elementAt(i)) 
					<= 0) {
				index = i;
				break;
			} 
		} 

		if (index >= 0) {
			_itemList.insertElementAt(tahitiItem, index);
			_agletList.add(getItemText(tahitiItem), index);
		} else {
			_itemList.addElement(tahitiItem);
			_agletList.add(getItemText(tahitiItem));
		} 

		// updateGUIState();
	}
	public void itemStateChanged(ItemEvent ev) {
		updateGUIState();
		if (TahitiItem.isNeedUpdate()) {
			updateProxyList();
		} 
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
	// -- Menu bar and panel methods

	// Builds the menu bar.
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

		_javaConsoleMenuItem = 
			new MenuItem(bundle.getString("menuitem.javaconsole"));
		_javaConsoleMenuItem
			.addActionListener(new DialogOpener(SHOW_JAVACON));
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
		 * if (Tahiti.enableBox) {
		 * menu.addSeparator();
		 * 
		 * item = new MenuItem(bundle.getString("menuitem.get"));
		 * item.addActionListener(new EventIssuer(GET_AGLETS));
		 * menu.add(item);
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
		item
			.addActionListener(new URLOpener(bundle
				.getString("http.release_notes")));

		// "http://www.trl.ibm.co.jp/aglets/awb_1.0b1.html"));
		menu.add(item);

		item = new MenuItem(bundle.getString("menuitem.aglets_home_page"));
		item
			.addActionListener(new URLOpener(bundle
				.getString("http.aglets_home")));

		// "http://www.trl.ibm.co.jp/aglets/index.html"
		menu.add(item);


		item = new MenuItem(bundle.getString("menuitem.feedback"));
		item
			.addActionListener(new URLOpener(bundle
				.getString("http.feedback")));

		// "http://aglets.trl.ibm.co.jp/report.html"
		menu.add(item);

		item = new MenuItem(bundle.getString("menuitem.bug_report"));
		item
			.addActionListener(new URLOpener(bundle
				.getString("http.bug_report")));

		// "http://aglets.trl.ibm.co.jp/report.html"
		menu.add(item);

		item = new MenuItem(bundle.getString("menuitem.faq"));
		item.addActionListener(new URLOpener(bundle.getString("http.faq")));

		// "http://www.trl.ibm.co.jp/aglets/faq.html"
		menu.add(item);

		menubar.setHelpMenu(menu);

		return menubar;
	}
	public void reboot() {
		_tahiti.reboot();
	}
	synchronized void removeProxyFromList(AgletProxy proxy) {
		AgletProxy p = null;

		try {
			text.remove(proxy);
			if (shrink) {
				return;
			} 

			for (int i = _itemList.size() - 1; i >= 0; i--) {
				p = ((TahitiItem)_itemList.elementAt(i)).getAgletProxy();
				if (p.isValid() == false) {
					_itemList.removeElementAt(i);
					_agletList.remove(i);
				} 
			} 

			updateGUIState();

			/*
			 * } else {
			 * System.out.println("Not Found!: " + proxy.getAgletInfo());
			 * }
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		} 
	}
	public void restoreSize() {
		Resource res = Resource.getResourceFor("tahiti");

		setSize(res.getInteger("tahiti.window.width", 100), 
				res.getInteger("tahiti.window.height", 100));
	}
	public void retractAglet(AgletProxy p) {
		new Thread(new TahitiEventHandler(RETRACT, p)).start();
	}
	/*
	 * Handles the event
	 * public boolean handleEvent(Event event) {
	 * Thread handler;
	 * AgletProxy proxy;
	 * String remote_host;
	 * InterruptWindow itrptWin;
	 * 
	 * case CREATE_AGLET:
	 * remote_host = (String)event.target;
	 * String aglet_class = ((String)event.arg).trim();
	 * 
	 * itrptWin = new InterruptWindow(this,
	 * "Aglet Creation",
	 * "Creating Aglet",
	 * (remote_host.toLowerCase().startsWith("http://") ?
	 * remote_host + "/" : remote_host) + aglet_class);
	 * 
	 * handler = new TahitiEventThread(this, itrptWin, event);
	 * itrptWin.setHandler(handler);
	 * handler.start();
	 * itrptWin.popup(this);
	 * 
	 * break;
	 * 
	 * case DISPATCH_AGLET:
	 * proxy = (AgletProxy)event.target;
	 * remote_host = ((String)event.arg).trim();
	 * 
	 * itrptWin = new InterruptWindow(this,
	 * "Aglet Dispatch",
	 * "Dispatching Aglet",
	 * remote_host);
	 * handler = new TahitiEventThread(this, itrptWin, event);
	 * itrptWin.setHandler(handler);
	 * handler.start();
	 * itrptWin.popup(this);
	 * break;
	 * case RETRACT_AGLET:
	 * URL agletURL = (URL)event.target;
	 * 
	 * itrptWin = new InterruptWindow(this,
	 * "Aglet Retract",
	 * "Retracting Aglet",
	 * agletURL.getHost());
	 * handler = new TahitiEventThread(this, itrptWin, event);
	 * itrptWin.setHandler(handler);
	 * handler.start();
	 * itrptWin.popup(this);
	 * break;
	 * default:
	 * return false;
	 * }
	 * return true;
	 * } else {
	 * return super.handleEvent(event);
	 * }
	 * return false;
	 * }
	 */

	void saveSize() {
		java.awt.Rectangle bounds = getBounds();
		Resource res = Resource.getResourceFor("tahiti");

		res.setResource("tahiti.window.x", String.valueOf(bounds.x));
		res.setResource("tahiti.window.y", String.valueOf(bounds.y));
		if (!shrink) {
			res.setResource("tahiti.window.width", 
							String.valueOf(bounds.width));
			res.setResource("tahiti.window.height", 
							String.valueOf(bounds.height));
			res.setResource("tahiti.window.shrinked", "false");
		} else {
			res.setResource("tahiti.window.shrinked", "true");
			res.setResource("tahiti.window.s_width", 
							String.valueOf(bounds.width));
			res.setResource("tahiti.window.s_height", 
							String.valueOf(bounds.height));
		} 
	}
	public void setFont(Font f) {
		MenuBar menubar = getMenuBar();

		if (menubar != null) {
			menubar.setFont(f);

			int c = menubar.getMenuCount();

			for (int i = 0; i < c; i++) {
				Menu m = menubar.getMenu(i);

				m.setFont(f);
			} 
		} 
		super.setFont(f);
		doLayout();
	}
	// Updates the message line.
	// @param message the new message.
	// 
	void setMessage(String message) {
		_messageLine.setText(message);
	}
	void showButtons() {
		_dialogButton.setVisible(true);
		_infoButton.setVisible(true);
		_disposeButton.setVisible(true);
		_cloneButton.setVisible(true);
		_dispatchButton.setVisible(true);
	}
	static private void showThreadGroup(ThreadGroup g, int level) {
		int i;
		String indent = "                                 ".substring(0, 
				level);

		System.out.println(indent + "{" + g.toString() + "}");

		int n = g.activeCount();

		if (n > 0) {
			System.out.println(indent + " + Threads");

			Thread t[] = new Thread[g.activeCount()];

			g.enumerate(t);
			for (i = 0; i < t.length; i++) {
				if (g == t[i].getThreadGroup()) {
					System.out.println(indent + "  - " + t[i].toString() 
									   + (t[i].isAlive() ? " alive" 
										  : " dead"));
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
	static void showThreads() {
		ThreadGroup g = null;

		for (g = Thread.currentThread().getThreadGroup(); 
				g.getParent() != null; g = g.getParent()) {}

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
				AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
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
	private void showWindow(Window window) {}
	public void shutdown() {
		_tahiti.exit();
	}
	void updateGUIState() {
		int indexes[] = _agletList.getSelectedIndexes();

		boolean single = indexes.length == 1;
		boolean multiple = indexes.length >= 1;

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
	synchronized void updateProxyInList(AgletProxy proxy) {
		TahitiItem tahitiItem = null;

		if (shrink) {
			return;
		} 

		int selected = _agletList.getSelectedIndex();

		int index = -1;

		for (int i = 0; i < _itemList.size(); i++) {
			tahitiItem = (TahitiItem)_itemList.elementAt(i);
			if (tahitiItem.checkProxy(proxy)) {
				_agletList.replaceItem(getItemText(tahitiItem), i);
				index = i;
				if (index == selected) {
					_agletList.select(index);
				} 
			} 
		} 

		updateGUIState();
	}
	public synchronized void updateProxyList() {

		// return all
		_agletList.removeAll();
		_itemList.setSize(0);

		// System.out.println("updateProxyList()");

		Enumeration e = Tahiti.CONTEXT.getAgletProxies();

		while (e.hasMoreElements()) {
			insertProxyToList((AgletProxy)e.nextElement());
		} 
	}
}
