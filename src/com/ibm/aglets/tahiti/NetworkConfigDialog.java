package com.ibm.aglets.tahiti;

/*
 * @(#)NetworkConfigDialog.java
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

import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

// import com.ibm.atp.auth.Challenge;
// import com.ibm.atp.auth.Randoms;

// import com.ibm.aglets.security.User;
// import com.ibm.aglets.security.UserAuthenticator;
// import com.ibm.aglets.security.UserAdministrator;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import java.security.cert.Certificate;

import java.util.Hashtable;
import java.util.Enumeration;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.FileUtils;

/**
 * Class NetworkConfigDialog represents the dialog for
 * 
 * @version     1.03    96/04/15
 * @author      Danny B. Lange
 * @modified  97/02/17 Yariv Aridor : add subsription support
 */

final class NetworkConfigDialog extends TahitiDialog 
	implements ActionListener, ItemListener {

	private static final String ACTION_RESTORE_DEFAULTS = "Restore Defaults";

	/*
	 * Proxy Configuration
	 */
	private Checkbox _useProxy = new Checkbox("Use HTTP Proxy");
	private TextField _proxyHost = new TextField(30);
	private TextField _proxyPort = new TextField(5);
	private TextField _noProxy = new TextField(35);

	/*
	 * 
	 */
	private Checkbox _httpTunneling = 
		new Checkbox("Accept HTTP Tunneling Request");
	private Checkbox _httpMessaging = 
		new Checkbox("Accept HTTP Request as a Message");

	/* subscription panel */
	private static final int UNDEFINED = 0;
	private static final int YES = 1;
	private static final int NO = 2;

	private static final String ACTION_OK = "OK";
	private static final String ACTION_SUBSCRIBE = "subscribe";
	private static final String ACTION_UNSUBSCRIBE = "unsubscribe";

	protected Button _subscribe = new Button("Subscribe");
	protected Button _unsubscribe = new Button("Unsubscribe");

	private TextField _boxHost = new TextField(35);
	private TextField _boxUserid = new TextField(35);
	private TextField _boxPasswd = new TextField(35);
	private Choice _updateChoice = new Choice();
	private int _boxSubscribe = UNDEFINED;

	/* authentication panel */
	private Checkbox _authenticationMode = 
		new Checkbox("Do Authentication on ATP Connection");
	private Checkbox _secureRandomSeed = 
		new Checkbox("Use Secure Random Seed");
	private static final String CREATE_SHARED_SECRET = 
		"Create a new shared secret";
	private Button _createSharedSecret = new Button(CREATE_SHARED_SECRET);
	private static final String REMOVE_SHARED_SECRET = 
		"Remove a shared secret";
	private Button _removeSharedSecret = new Button(REMOVE_SHARED_SECRET);
	private static final String IMPORT_SHARED_SECRET = 
		"Import a shared secret";
	private Button _importSharedSecret = new Button(IMPORT_SHARED_SECRET);
	private static final String EXPORT_SHARED_SECRET = 
		"Export a shared secret";
	private Button _exportSharedSecret = new Button(EXPORT_SHARED_SECRET);

	/*
	 * Singleton instance reference.
	 */
	private static NetworkConfigDialog _instance = null;

	private int boxUpdateValues[] = {
		0, 15, 30, 60, 60 * 5, 60 * 15, 60 * 60
	};

	/*
	 * Constructs a new Aglet creation dialog.
	 */
	private NetworkConfigDialog(MainWindow parent) {
		super(parent, "Network Preferences", true);

		makePanel();

		addButton(ACTION_OK, this);
		addCloseButton(null);
		addButton(ACTION_RESTORE_DEFAULTS, this);
	}
	/*
	 * The call back methods
	 */
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if (ACTION_SUBSCRIBE.equals(cmd)) {
			subscribe();
		} else if (ACTION_UNSUBSCRIBE.equals(cmd)) {
			unsubscribe();
		} else if (CREATE_SHARED_SECRET.equals(cmd)) {
			createSharedSecret();
		} else if (REMOVE_SHARED_SECRET.equals(cmd)) {
			removeSharedSecret();
		} else if (IMPORT_SHARED_SECRET.equals(cmd)) {
			importSharedSecret();
		} else if (EXPORT_SHARED_SECRET.equals(cmd)) {
			exportSharedSecret();
		} else if (ACTION_RESTORE_DEFAULTS.equals(cmd)) {
			restoreDefaults();
		} else if (ACTION_OK.equals(cmd)) {
			dispose();
			if (save()) {
				ShutdownDialog sd = 
					new ShutdownDialog((MainWindow)getParent(), 
									   "To be effective, you need reboot the server.");

				sd.popupAtCenterOfParent();
			} 
			inform();
		} 
	}
	private void createSharedSecret() {
		TahitiDialog d = new CreateSharedSecretDialog(getMainWindow());

		d.popupAtCenterOfParent();
	}
	private void exportSharedSecret() {
		class ExportSharedSecret extends TahitiDialog 
			implements ActionListener, ItemListener {
			List list;
			TextField filename;

			SharedSecrets secrets;

			ExportSharedSecret(Frame f, SharedSecrets secs) {
				super(f, EXPORT_SHARED_SECRET, true);
				secrets = secs;
				add("North", new Label(EXPORT_SHARED_SECRET, Label.CENTER));
				GridBagPanel p = new GridBagPanel();

				add(p);

				list = new List(5, false);
				filename = new TextField(20);

				GridBagConstraints cns = new GridBagConstraints();

				cns.fill = GridBagPanel.HORIZONTAL;
				cns.anchor = GridBagConstraints.WEST;
				cns.gridwidth = GridBagConstraints.REMAINDER;

				p.setConstraints(cns);
				p.addLabeled("Domain name list", list);
				p.addLabeled("Filename", filename);
				list.addItemListener(this);
				list.addActionListener(this);
				Enumeration domains = secrets.getDomainNames();

				if (domains != null) {
					while (domains.hasMoreElements()) {
						String domain = (String)domains.nextElement();

						list.add(domain);
					} 
				} 
				filename.addActionListener(this);
				addButton(ACTION_OK, this);
				addCloseButton("Cancel");
			}

			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					String domainName = list.getSelectedItem();

					if (domainName != null &&!domainName.equals("")) {
						String filen = 
							domainName.toLowerCase().replace(' ', '_') 
							+ ".sec";

						filename.setText(filen);
					} 
				} 
			} 

			public void actionPerformed(ActionEvent ev) {
				String domainName = list.getSelectedItem();

				if (domainName == null || domainName.equals("")) {
					TahitiDialog
						.alert(getMainWindow(), "Select a domain name in list")
							.popupAtCenterOfParent();
					return;
				} 
				String filen = filename.getText();

				if (filen == null || filen.equals("")) {
					TahitiDialog.alert(getMainWindow(), "Specify filename")
						.popupAtCenterOfParent();
					return;
				} 
				String owner = getOwnerName();
				String workDir = FileUtils.getWorkDirectoryForUser(owner);
				String secretFilename = workDir + File.separator + filen;
				SharedSecret secret = secrets.getSharedSecret(domainName);

				if (secret == null) {
					TahitiDialog
						.alert(getMainWindow(), "The shared secret does not exist")
							.popupAtCenterOfParent();
					return;
				} 
				try {
					secret.save(secretFilename);
				} catch (IOException excpt) {
					TahitiDialog.alert(getMainWindow(), 
									   "Cannot save").popupAtCenterOfParent();
					return;
				} 
				TahitiDialog.message(getMainWindow(), "Exported", 
									 "The shared secret of domain '" 
									 + domainName 
									 + "' is exported into a file '" 
									 + secretFilename 
									 + "'").popupAtCenterOfParent();
				dispose();
			} 
		}
		;

		SharedSecrets secrets = SharedSecrets.getSharedSecrets();

		if (secrets == null) {
			TahitiDialog.alert(getMainWindow(), 
							   "No shared secrets").popupAtCenterOfParent();
			return;
		} 
		TahitiDialog d = new ExportSharedSecret(getMainWindow(), secrets);

		d.popupAtCenterOfParent();
	}
	/*
	 * Singletion method to get the instnace
	 */
	static NetworkConfigDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new NetworkConfigDialog(parent);
		} else {
			_instance.updateValues();
		} 
		return _instance;
	}
	private static String getOwnerName() {
		com.ibm.aglet.system.AgletRuntime runtime = 
			com.ibm.aglet.system.AgletRuntime.getAgletRuntime();

		if (runtime == null) {
			return null;
		} 
		return runtime.getOwnerName();
	}
	/*
	 * Subscribe
	 */
	private int getStatusCode(Hashtable headers) {
		int defValue = -1;

		try {
			String ret = (String)(headers.get("status-code"));

			return ret == null ? defValue : Integer.parseInt(ret);
		} catch (Exception e) {}
		return defValue;
	}
	// TEMPRARY
	private int getSubscribeStatus(String s) {
		if (s.equalsIgnoreCase("yes")) {
			return YES;
		} else if (s.equalsIgnoreCase("no")) {
			return NO;
		} else if (s.equalsIgnoreCase("undefined")) {
			return UNDEFINED;
		} 
		return UNDEFINED;
	}
	private void importSharedSecret() {
		class ImportSharedSecret extends TahitiDialog 
			implements ActionListener {
			TextField filename;

			ImportSharedSecret(Frame f) {
				super(f, IMPORT_SHARED_SECRET, true);
				add("North", new Label(IMPORT_SHARED_SECRET, Label.CENTER));
				GridBagPanel p = new GridBagPanel();

				add(p);

				filename = new TextField(20);

				GridBagConstraints cns = new GridBagConstraints();

				cns.fill = GridBagPanel.HORIZONTAL;
				cns.anchor = GridBagConstraints.WEST;
				cns.gridwidth = GridBagConstraints.REMAINDER;

				p.setConstraints(cns);
				p.addLabeled("Filename", filename);
				filename.addActionListener(this);
				addButton(ACTION_OK, this);
				addCloseButton("Cancel");
			}

			public void actionPerformed(ActionEvent ev) {
				String filen = filename.getText();

				if (filen == null || filen.equals("")) {
					TahitiDialog.alert(getMainWindow(), "Specify filename")
						.popupAtCenterOfParent();
					return;
				} 
				String owner = getOwnerName();
				String workDir = FileUtils.getWorkDirectoryForUser(owner);
				String secretFilename = workDir + File.separator + filen;
				SharedSecret secret = null;

				try {
					secret = SharedSecret.load(secretFilename);
				} catch (FileNotFoundException excpt) {
					TahitiDialog.alert(getMainWindow(), "File not found: " + secretFilename)
						.popupAtCenterOfParent();
					return;
				} catch (IOException excpt) {
					TahitiDialog.alert(getMainWindow(), "Cannot read file: " + secretFilename)
						.popupAtCenterOfParent();
					return;
				} 
				if (secret == null) {
					TahitiDialog.alert(getMainWindow(), "No shared secret: " + secretFilename)
						.popupAtCenterOfParent();
					return;
				} 
				String domainName = secret.getDomainName();
				SharedSecrets secrets = SharedSecrets.getSharedSecrets();
				SharedSecret sec = secrets.getSharedSecret(domainName);

				if (sec != null) {
					TahitiDialog.alert(getMainWindow(), 
									   "The shared secret for domain '" 
									   + domainName 
									   + "' already exists").popupAtCenterOfParent();
					return;
				} 
				secrets.addSharedSecret(secret);
				secrets.save();
				TahitiDialog.message(getMainWindow(), "Imported", 
									 "The shared secret for domain '" 
									 + domainName 
									 + "' is imported").popupAtCenterOfParent();
				dispose();
			} 
		}
		;
		TahitiDialog d = new ImportSharedSecret(getMainWindow());

		d.popupAtCenterOfParent();
	}
	private void inform() {
		Resource aglets_res = Resource.getResourceFor("aglets");

		if (Tahiti.enableBox) {

			// Tahiti.POLLING.setFrequency( aglets_res.getInteger("aglets.box.update.sec", 0));
		} 
	}
	public void itemStateChanged(ItemEvent ev) {
		updateGUIState();
	}
	/*
	 * Layouts all components
	 */
	void makePanel() {
		GridBagPanel p = new GridBagPanel();

		add("Center", p);

		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.insets = new Insets(5, 5, 5, 5);

		p.setConstraints(cns);

		BorderPanel panel = new BorderPanel("Http Tunneling");

		p.add(panel, GridBagConstraints.REMAINDER);
		setupHttpTunnelingPanel(panel);

		// panel = new BorderPanel("Subscription");
		// p.add(panel, GridBagConstraints.REMAINDER);
		// setupSubscriptionPanel(panel);

		panel = new BorderPanel("Authentication");
		p.add(panel, GridBagConstraints.REMAINDER);
		setupAuthenticationPanel(panel);

		panel = new BorderPanel("Others");
		p.add(panel, GridBagConstraints.REMAINDER);
		setupOthersPanel(panel);

		_updateChoice.addItem("None");
		_updateChoice.addItem("At most every 15sec");
		_updateChoice.addItem("At most every 30sec");
		_updateChoice.addItem("At most every 1 min");
		_updateChoice.addItem("At most every 5 min");
		_updateChoice.addItem("At most every 15min");
		_updateChoice.addItem("At most once an hour");

		updateValues();
	}
	protected Panel makeSubscribeButtonPanel() {
		Panel p = new Panel();

		p.setLayout(new FlowLayout(FlowLayout.RIGHT));
		p.add(_subscribe);
		p.add(_unsubscribe);
		_subscribe.setActionCommand(ACTION_SUBSCRIBE);
		_subscribe.addActionListener(this);

		_unsubscribe.setActionCommand(ACTION_UNSUBSCRIBE);
		_unsubscribe.addActionListener(this);

		return p;
	}
	private void removeSharedSecret() {

		// ?????NEED TO BE IMPLEMENTED (HT)
		class RemoveSharedSecret extends TahitiDialog 
			implements ActionListener {
			List list;
			TextField password;
			SharedSecrets secrets;

			RemoveSharedSecret(Frame f, SharedSecrets secs) {
				super(f, REMOVE_SHARED_SECRET, true);
				secrets = secs;
				add("North", new Label(REMOVE_SHARED_SECRET, Label.CENTER));
				GridBagPanel p = new GridBagPanel();

				add(p);

				list = new List(5, false);
				password = new TextField(20);
				password.setEchoChar('*');

				GridBagConstraints cns = new GridBagConstraints();

				cns.fill = GridBagPanel.HORIZONTAL;
				cns.anchor = GridBagConstraints.WEST;
				cns.gridwidth = GridBagConstraints.REMAINDER;

				p.setConstraints(cns);
				p.addLabeled("Domain name list", list);
				p.addLabeled("Password", password);
				list.addActionListener(this);
				Enumeration domains = secrets.getDomainNames();

				if (domains != null) {
					while (domains.hasMoreElements()) {
						String domain = (String)domains.nextElement();

						list.add(domain);
					} 
				} 
				password.addActionListener(this);
				addButton(ACTION_OK, this);
				addCloseButton("Cancel");
			}

			public void actionPerformed(ActionEvent ev) {
				String domainName = list.getSelectedItem();

				if (domainName == null || domainName.equals("")) {
					TahitiDialog
						.alert(getMainWindow(), "Select a domain name in list")
							.popupAtCenterOfParent();
					return;
				} 
				SharedSecret secret = secrets.getSharedSecret(domainName);

				if (secret == null) {
					TahitiDialog
						.alert(getMainWindow(), "Shared secret does not exist")
							.popupAtCenterOfParent();
					return;
				} 
				AgletRuntime rt = AgletRuntime.getAgletRuntime();

				// Certificate ownerCert = rt.getOwnerCertificate();
				String ownerName = rt.getOwnerName();

				if (rt.authenticateOwner(ownerName, password.getText()) 
						== null) {
					TahitiDialog
						.alert(getMainWindow(), "Authentication failed")
							.popupAtCenterOfParent();
					password.setText("");
					return;
				} 

				/*
				 * ------------
				 * String ownerName = secret.getOwnerName();
				 * UserAuthenticator auth = UserAuthenticator.getUserAuthenticator();
				 * User owner = auth.getUser(ownerName);
				 * if(owner==null) {
				 * TahitiDialog.alert(getMainWindow(),
				 * "The owner does not exist").popupAtCenterOfParent();
				 * return;
				 * }
				 * if(!owner.isNoLogin() && !owner.verify(password.getText())) {
				 * TahitiDialog.alert(getMainWindow(),
				 * "Password is incorrect").popupAtCenterOfParent();
				 * password.setText("");
				 * return;
				 * }
				 * ----------------
				 */
				secrets.removeSharedSecret(domainName);
				secrets.save();
				TahitiDialog.message(getMainWindow(), "Removed", 
									 "The domain '" + domainName 
									 + "' is removed").popupAtCenterOfParent();
				dispose();
			} 
		}
		;

		SharedSecrets secrets = SharedSecrets.getSharedSecrets();

		if (secrets == null) {
			TahitiDialog.alert(getMainWindow(), 
							   "No shared secrets").popupAtCenterOfParent();
			return;
		} 
		TahitiDialog d = new RemoveSharedSecret(getMainWindow(), secrets);

		d.popupAtCenterOfParent();
	}
	void restoreDefaults() {
		_updateChoice.select(0);
		_httpTunneling.setState(false);
		_httpMessaging.setState(false);
	}
	/*
	 * private boolean check () {
	 * int min = -1;
	 * PopupMessageWindow mw = null;
	 * 
	 * try {
	 * min = Integer.parseInt(_boxUpdate.getText().trim());
	 * } catch(Exception e) {
	 * min = -1;
	 * }
	 * 
	 * if (min<0)  {
	 * (mw=new PopupMessageWindow(_parent,"ERROR!!","invalid update setting")).popup(_parent);
	 * return false;
	 * }
	 * return true;
	 * }
	 */

	private boolean save() {
		boolean changed = false;
		String value;
		Resource system_res = Resource.getResourceFor("system");
		Resource atp_res = Resource.getResourceFor("atp");

		boolean use = _useProxy.getState();

		if (use != atp_res.getBoolean("atp.useHttpProxy", false)) {
			changed = true;
		} 
		atp_res.setResource("atp.useHttpProxy", String.valueOf(use));

		value = _proxyHost.getText().trim();
		if (value.equals(atp_res.getString("atp.http.proxyHost")) == false) {
			changed = true;
		} 
		atp_res.setResource("atp.http.proxyHost", value);
		system_res.setResource("proxyHost", use ? value : "");
		system_res.setResource("http.proxyHost", use ? value : "");

		value = _proxyPort.getText().trim();
		if (value.equals(atp_res.getString("atp.http.proxyPort")) == false) {
			changed = true;
		} 
		atp_res.setResource("atp.http.proxyPort", value);
		system_res.setResource("proxyPort", use ? value : "");
		system_res.setResource("http.proxyPort", use ? value : "");

		value = _noProxy.getText().trim();
		if (value.equals(atp_res.getString("atp.noProxy")) == false) {
			changed = true;
		} 
		atp_res.setResource("atp.noProxy", value);
		system_res.setResource("http.nonProxyHosts", value);

		/*
		 * allow/disallow http tunneling/messaging
		 */
		atp_res.setResource("atp.http.tunneling", 
							String.valueOf(_httpTunneling.getState()));
		atp_res.setResource("atp.http.messaging", 
							String.valueOf(_httpMessaging.getState()));

		// com.ibm.atp.daemon.Daemon.update();

		/*
		 * authentication
		 */
		final boolean auth = _authenticationMode.getState();

		if (auth != atp_res.getBoolean("atp.authentication", false)) {
			changed = true;
		} 
		atp_res.setResource("atp.authentication", String.valueOf(auth));
		if (auth) {
			System.out.println("AUTHENTICATION MODE ON.");

			// SharedSecrets.getSharedSecrets();
		} else {
			System.out.println("AUTHENTICATION MODE OFF.");
		} 
		final boolean secureseed = _secureRandomSeed.getState();

		atp_res.setResource("atp.secureseed", String.valueOf(secureseed));
		if (secureseed) {
			System.out.println("USE SECURE RANDOM SEED.");
		} else {
			System.out.println("USE UNSECURE PSEUDO RANDOM SEED.");
		} 

		// Randoms.setUseSecureRandomSeed(secureseed);
		if (auth) {
			System.out
				.print("[Generating random seed ... wait for a while ... ");
			if (auth) {

				// Randoms.getRandomGenerator(Challenge.LENGTH);
			} 
			System.out.println("done.]");
		} 

		Resource aglets_res = Resource.getResourceFor("aglets");

		if (Tahiti.enableBox) {
			aglets_res
				.setResource("aglets.box.update.sec", 
							 String
								 .valueOf(updateValue(_updateChoice
									 .getSelectedIndex())));
			aglets_res
				.setResource("aglets.box.update", 
							 String
								 .valueOf(_updateChoice.getSelectedIndex()));

			aglets_res.setResource("aglets.box.host", 
								   _boxHost.getText().trim());
			aglets_res.setResource("aglets.box.passwd", 
								   _boxPasswd.getText().trim());
			aglets_res.setResource("aglets.box.userid", 
								   _boxUserid.getText().trim());
			aglets_res.setResource("aglets.box.subscribe", 
								   setSubscribeStatus(_boxSubscribe));
		} 
		aglets_res.save("Tahiti");
		atp_res.save("Tahiti");

		// REMIND: needs update
		// com.ibm.atp.protocol.http.HttpProxy.update();
		return changed;
	}
	private String setSubscribeStatus(int i) {
		switch (i) {
		case YES:
			return "yes";
		case NO:
			return "no";
		case UNDEFINED:
			return "undefined";
		default:
			return "undefined";
		}
	}
	/*
	 * Make the authentication panel
	 */
	void setupAuthenticationPanel(BorderPanel authPanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.NONE;
		cns.weighty = 1.0;
		cns.insets = authPanel.topInsets();
		cns.insets.bottom = authPanel.bottomInsets().bottom;
		authPanel.setConstraints(cns);

		cns.fill = GridBagConstraints.HORIZONTAL;
		authPanel.add(_authenticationMode, GridBagConstraints.REMAINDER, 1.0);
		authPanel.add(_secureRandomSeed, GridBagConstraints.REMAINDER, 1.0);

		cns.fill = GridBagConstraints.HORIZONTAL;

		cns.gridwidth = GridBagConstraints.RELATIVE;
		authPanel.add(_createSharedSecret);
		_createSharedSecret.setActionCommand(CREATE_SHARED_SECRET);
		_createSharedSecret.addActionListener(this);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		authPanel.add(_removeSharedSecret);
		_removeSharedSecret.setActionCommand(REMOVE_SHARED_SECRET);
		_removeSharedSecret.addActionListener(this);

		cns.gridwidth = GridBagConstraints.RELATIVE;
		authPanel.add(_importSharedSecret);
		_importSharedSecret.setActionCommand(IMPORT_SHARED_SECRET);
		_importSharedSecret.addActionListener(this);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		authPanel.add(_exportSharedSecret);
		_exportSharedSecret.setActionCommand(EXPORT_SHARED_SECRET);
		_exportSharedSecret.addActionListener(this);
	}
	/*
	 * Make the GUI for setting http proxy
	 */
	void setupHttpTunnelingPanel(BorderPanel proxyPanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.NONE;
		cns.weighty = 1.0;
		cns.insets = proxyPanel.topInsets();
		cns.insets.bottom = proxyPanel.bottomInsets().bottom;

		proxyPanel.setConstraints(cns);

		proxyPanel.add(_httpTunneling, GridBagConstraints.REMAINDER, 1.0);
		proxyPanel.add(_useProxy, GridBagConstraints.REMAINDER, 1.0);

		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.insets = proxyPanel.bottomInsets();

		proxyPanel.add(new Label("ProxyHost:"), 1, 0.1);
		proxyPanel.add(_proxyHost, 1, 1.0);
		proxyPanel.add(new Label("Port:"), 1, 0.1);
		proxyPanel.add(_proxyPort, GridBagConstraints.REMAINDER, 0.4);

		proxyPanel.add(new Label("Do not use the proxy server for domains:"), 
					   GridBagConstraints.REMAINDER, 1.0);
		proxyPanel.add(_noProxy, GridBagConstraints.REMAINDER, 1.0);

		_useProxy.addItemListener(this);
	}
	/*
	 * 
	 */
	void setupOthersPanel(BorderPanel othersPanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.NONE;
		cns.weighty = 1.0;

		othersPanel.setConstraints(cns);

		cns.insets = othersPanel.topInsets();
		cns.insets.bottom = othersPanel.bottomInsets().bottom;
		othersPanel.add(_httpMessaging, GridBagConstraints.REMAINDER, 1.0);
	}
	/*
	 * Make the subscription panel
	 */
	void setupSubscriptionPanel(BorderPanel subPanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.NONE;
		cns.weighty = 1.0;
		cns.insets = subPanel.topInsets();
		cns.insets.bottom = subPanel.bottomInsets().bottom;

		subPanel.setConstraints(cns);

		/*
		 * Util.addCmp(subPanel,grid, cns, _useBox,
		 * GridBagConstraints.REMAINDER, 1.0);
		 */
		cns.fill = GridBagConstraints.HORIZONTAL;
		subPanel.add(new Label("Server(URL):"), 1, 0.1);
		subPanel.add(_boxHost, GridBagConstraints.REMAINDER, 1.0);

		/*
		 * Util.addCmp(subPanel, grid, cns,makeSubscribeButtonPanel(),
		 * GridBagConstraints.REMAINDER, 1.0);
		 */
		cns.insets = subPanel.bottomInsets();
		_boxPasswd.setEchoChar('x');
		subPanel.add(new Label("Password:"), 1, 0.1);
		subPanel.add(_boxPasswd, GridBagConstraints.REMAINDER, 1.0);

		subPanel.add(new Label("UserId:"), 1, 0.1);
		subPanel.add(_boxUserid, GridBagConstraints.REMAINDER, 1.0);

		cns.fill = GridBagConstraints.NONE;
		subPanel.add(new Label("Frequency:"), 1, 0.1);
		subPanel.add(_updateChoice, 1, 0.1);

		cns.anchor = GridBagConstraints.EAST;
		subPanel.add(_subscribe, GridBagConstraints.REMAINDER, 1.0);
		subPanel.add(_unsubscribe, GridBagConstraints.REMAINDER, 1.0);
	}
	void subscribe() {

		/*
		 * Resource res = Resource.getResourceFor("aglets");
		 * try {
		 * res.setResource("aglets.box.host", _boxHost.getText().trim());
		 * res.setResource("aglets.box.passwd", _boxPasswd.getText().trim());
		 * res.setResource("aglets.box.userid",  _boxUserid.getText().trim());
		 * String name = res.getString("aglets.box.userid","");
		 * URL url = new URL("aglet", name, "");
		 * AgletConnection connection = new AgletConnection(url,"subscribe");
		 * connection.sendRequest();
		 * InputStream is = connection.getInputStream();
		 * DataInputStream ds = new DataInputStream(is);
		 * Hashtable headers = new Hashtable();
		 * AtpConnectionImpl.parseHeaders(ds,headers);
		 * if (getStatusCode(headers) != AtciConstants.OKAY) {
		 * String line =ds.readLine();
		 * throw new IOException(line.trim());
		 * }
		 * updateSubscribeGUIState(YES);
		 * res.setResource("aglets.box.subscribe", setSubscribeStatus(YES));
		 * res.save("Tahiti");
		 * } catch (IOException ex) {
		 * TahitiDialog.alert(getMainWindow(),
		 * "ERROR during SUBSCRIBE!!\n" +
		 * ex.getClass().getName() + ":" + ex.getMessage()).popupAtCenterOfParent();
		 * }
		 */
	}
	void unsubscribe() {

		/*
		 * Resource res = Resource.getResourceFor("aglets");
		 * try {
		 * String name = res.getString("aglets.box.userid","");
		 * URL url = new URL("aglet", name, "");
		 * AgletConnection connection = new AgletConnection(url,"unsubscribe");
		 * connection.sendRequest();
		 * InputStream is = connection.getInputStream();
		 * DataInputStream ds = new DataInputStream(is);
		 * Hashtable headers = new Hashtable();
		 * AtpConnectionImpl.parseHeaders(ds,headers);
		 * if (getStatusCode(headers) != AtciConstants.OKAY) {
		 * String line =ds.readLine();
		 * throw new IOException(line.trim());
		 * }
		 * updateSubscribeGUIState(NO);
		 * res.setResource("aglets.box.subscribe", setSubscribeStatus(NO));
		 * res.save("Tahiti");
		 * } catch (IOException ex) {
		 * TahitiDialog.alert(getMainWindow(),
		 * "ERROR during UNSUBSCRIBE!!\n" +
		 * ex.getClass().getName() + ":" + ex.getMessage()
		 * ).popupAtCenterOfParent();
		 * }
		 */
	}
	void updateGUIState() {
		_boxUserid.setEnabled(false);
		boolean b = _useProxy.getState();

		_proxyHost.setEnabled(b);
		_proxyPort.setEnabled(b);
		_noProxy.setEnabled(b);

		_boxUserid.setEnabled(false);

		/*
		 * //  Temporarely
		 * _useUpdate.setLabel("Update Off:");
		 * _boxUpdate.setEnabled(false);
		 * _useUpdate.setEnabled(false);
		 */

		updateSubscribeGUIState(_boxSubscribe);

		/*
		 * if (_useBox.getState()==false) {
		 * _useBox.setLabel("Box: Disabled");
		 * //	    _boxUserid.setEnabled(false);
		 * _boxPasswd.setEnabled(false);
		 * _useUpdate.setEnabled(false);
		 * _subscribe.setEnabled(false);
		 * _unsubscribe.setEnabled(false);
		 * _boxUpdate.setEnabled(false);
		 * _boxHost.setEnabled(false);
		 * } else {
		 * _useBox.setLabel("Box: Enabled");
		 * //	    _boxUserid.setEnabled(true);
		 * //	    _boxPasswd.setEnabled(true);
		 * _useUpdate.setEnabled(true);
		 * updateSubscribeGUIState(_boxSubscribe);
		 * //	    _boxHost.setEnabled(true);
		 * if (_useUpdate.getState() == false) {
		 * _useUpdate.setLabel("Update Off:");
		 * _boxUpdate.setEnabled(false);
		 * } else {
		 * _useUpdate.setLabel("Update On:");
		 * _boxUpdate.setEnabled(true);
		 * }
		 * }
		 */

	}
	private int updateIndex(int value) {
		int i = -1;

		for (int j = 0; j < boxUpdateValues.length; j++) {
			if (value == boxUpdateValues[j]) {
				i = j;
				break;
			} 
		} 
		return i;
	}
	private void updateSubscribeGUIState(int cond) {
		if (Tahiti.enableBox == false) {
			_subscribe.setEnabled(false);
			_unsubscribe.setEnabled(false);
			_boxHost.setText("Disabled");
			_boxUserid.setText("Disabled");
			_boxHost.setEnabled(false);
			_boxPasswd.setEnabled(false);
			_updateChoice.select(0);
			_updateChoice.setEnabled(false);

			// Temporary
			Resource res = Resource.getResourceFor("aglets");

			res.setResource("aglets.box.subscribe", setSubscribeStatus(NO));

			return;
		} 

		switch (cond) {
		case YES:
			_subscribe.setEnabled(false);
			_unsubscribe.setEnabled(true);
			_boxHost.setEnabled(false);
			_boxPasswd.setEnabled(false);

			// _boxUserid.setEnabled(false);
			break;
		case NO:
			_subscribe.setEnabled(true);
			_unsubscribe.setEnabled(false);
			_boxHost.setEnabled(true);
			_boxPasswd.setEnabled(true);

			// _boxUserid.setEnabled(true);
			break;
		case UNDEFINED:
			_subscribe.setEnabled(true);
			_unsubscribe.setEnabled(true);
			_boxHost.setEnabled(true);
			_boxPasswd.setEnabled(true);

			// _boxUserid.setEnabled(true);
			break;
		default:
		}
		_boxSubscribe = cond;
	}
	private int updateValue(int index) {
		return (index < boxUpdateValues.length) ? boxUpdateValues[index] : 0;
	}
	/*
	 * Setting values
	 */
	private void updateValues() {

		Resource atp_res = Resource.getResourceFor("atp");

		_proxyHost.setText(atp_res.getString("atp.http.proxyHost", ""));
		_proxyPort.setText(atp_res.getString("atp.http.proxyPort", ""));

		_noProxy.setText(atp_res.getString("atp.noProxy", ""));
		_useProxy.setState(atp_res.getBoolean("atp.useHttpProxy", false));

		Resource res = Resource.getResourceFor("aglets");

		/*
		 * allow/disallow http tunneling/messaging
		 */
		_httpTunneling.setState(atp_res.getBoolean("atp.http.tunneling", 
												   false));
		_httpMessaging.setState(atp_res.getBoolean("atp.http.messaging", 
												   false));

		/*
		 * Authentication
		 */
		_authenticationMode.setState(atp_res.getBoolean("atp.authentication", 
				false));
		_secureRandomSeed.setState(atp_res.getBoolean("atp.secureseed", 
				false));

		/*
		 * BOX
		 */
		String email = res.getString("aglets.box.userid", "");

		// _useBox.setState( res.getBoolean("aglets.box.enabled", false) );
		_boxUserid.setText(res.getString("aglets.box.userid", email));
		_boxPasswd.setText(res.getString("aglets.box.passwd"));

		// _useUpdate.setState( res.getBoolean("aglets.box.update.enabled", false));
		_updateChoice
			.select(updateIndex(res.getInteger("aglets.box.update.sec", 0)));
		_updateChoice.select(res.getInteger("aglets.box.update", 0));

		_boxHost.setText(res.getString("aglets.box.host"));
		String tmp = res.getString("aglets.box.subscribe", "undefined");

		_boxSubscribe = (_boxSubscribe == UNDEFINED) 
						? getSubscribeStatus(tmp) : _boxSubscribe;

		updateGUIState();
	}
}
