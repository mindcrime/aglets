package com.ibm.aglets.tahiti;

/*
 * @(#)SecurityConfigDialog.java
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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Font;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.io.File;
import java.io.IOException;

import java.security.Policy;
import com.ibm.aglets.security.PolicyDB;
import com.ibm.aglets.security.PolicyFileReader;
import com.ibm.aglets.security.PolicyFileWriter;
import com.ibm.aglets.security.PolicyGrant;
import com.ibm.aglets.security.PolicyPermission;
import com.ibm.aglets.security.PolicyFileParsingException;

import com.ibm.awb.misc.URIPattern;
import com.ibm.awb.misc.MalformedURIPatternException;
import com.ibm.awb.misc.Resource;

/**
 * Class SecurityConfigDialog represents the dialog for
 * 
 * @version     1.03    96/04/15
 * @author      Danny B. Lange
 */

final class SecurityConfigDialog extends TahitiDialog 
	implements ActionListener, ItemListener {

	// Labels
	private static final String LABEL_TITLE = "Security Preferences";
	private static final String LABEL_OK = "OK";
	private static final String LABEL_CLOSE = "Close";
	private static final String LABEL_RELOAD = "Reload";
	private static final String LABEL_GRANT_PANEL = "Code Base";

	/*
	 * Singleton instance reference.
	 */
	private static SecurityConfigDialog _instance = null;

	private PolicyDB _db = null;

	private List _grantList = new List(5, false);
	private GrantEditor _grantEditor = new GrantEditor();
	private GrantEditPanel _grantPanel = null;
	GridBagPanel _panel = new GridBagPanel();
	Panel setting_panel = new Panel();
	CardLayout layout = new CardLayout();
	Hashtable privileges = new Hashtable();

	/*
	 * Constructs a security configuration dialog.
	 */
	private SecurityConfigDialog(MainWindow parent) {
		super(parent, LABEL_TITLE, true);
		loadPolicyFile();
		add("Center", _panel);
		makePanel();

		addButton(LABEL_OK, this);
		addButton(LABEL_CLOSE, this);

		// -     addButton(LABEL_RELOAD, this);
	}
	// -   public void restoreDefaults() {
	// -     Enumeration e = privileges.elements();
	// -     while(e.hasMoreElements()) {
	// -       ((SecurityConfigPanel)e.nextElement()).restoreDefaults();
	// -     }
	// -   }

	/**
	 * *****************************************************************
	 * The call back methods
	 */

	// Creates an Aglet creation dialog.
	// 
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if (LABEL_OK.equals(cmd)) {
			save();

			// -       Policy policy = Policy.getPolicy();
			// -       if(policy!=null) {
			// - 	policy.refresh();
			// -       }
			// to work around the bug in JDK1.1 for UNIX
			// -       dispose();
			setVisible(false);
			ShutdownDialog sd = 
				new ShutdownDialog((MainWindow)getParent(), 
								   "To be effective, you need reboot the server.");

			sd.popupAtCenterOfParent();
		} else if (LABEL_CLOSE.equals(cmd)) {
			setVisible(false);

			// -     } else if(LABEL_RELOAD.equals(cmd)) {
			// - //System.out.println("remove all");
			// - //      _instance.removeAll();
			// -       restoreDefaults();
			// -       _grantPanel.removeAll();
			// -       _panel.removeAll();
			// - System.out.println("load policy file");
			// -       loadPolicyFile();
			// - System.out.println("make panel");
			// -       makePanel();
			// - //System.out.println("add buttons");
			// - //	addButton(LABEL_OK, this);
			// - //	addCloseButton(null);
			// - //	addButton(LABEL_RELOAD, this);
			// - System.out.println("update values");
			// -       _instance.updateValues();
			// - System.out.println("done.");
		} 
	}
	private void addGrantPanel(PolicyGrant grant) {
		addGrantPanel(getGrantName(grant), grant);
	}
	public void addGrantPanel(String name) {
		addGrantPanel(name, null);
	}
	private void addGrantPanel(String name, PolicyGrant grant) {
		if (!hasGrant(name)) {
			_grantList.add(name);
			SecurityConfigPanel panel = new SecurityConfigPanel(name, grant);

			privileges.put(name, panel);
			setting_panel.add(name, panel);
			panel.setupPanels();
		} 
	}
	private static String getGrantName(PolicyGrant grant) {
		final URIPattern codeBase = grant.getCodeBase();
		final String signers = grant.getSignerNames();
		final String owners = grant.getOwnerNames();
		final boolean cb = codeBase != null;
		final boolean s = signers != null &&!signers.equals("");
		final boolean o = owners != null &&!owners.equals("");
		Vector args = new Vector();

		if (cb || s || o) {
			args.addElement(codeBase.toString());
		} 
		if (s || o) {
			args.addElement(signers);
		} 
		if (o) {
			args.addElement(owners);
		} 
		return EditorPanel.toText(args);
	}
	/*
	 * Singletion method to get the instnace
	 */
	static SecurityConfigDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new SecurityConfigDialog(parent);
		} 
		_instance.updateValues();
		return _instance;
	}
	private boolean hasGrant(PolicyGrant grant) {
		return hasGrant(getGrantName(grant));
	}
	private boolean hasGrant(String name) {
		String[] grants = _grantList.getItems();
		int i = 0;

		for (i = 0; i < grants.length; i++) {
			if (name.equals(grants[i])) {
				return true;
			} 
		} 
		return false;
	}
	/*
	 * Handles the events
	 */
	public void itemStateChanged(ItemEvent ev) {
		String item = _grantList.getSelectedItem();

		if (item != null) {
			showGrantPanel(item);
		} 
	}
	private void loadPolicyFile() {
		final String file = PolicyFileReader.getUserPolicyFilename();
		PolicyFileReader reader = new PolicyFileReader(file);

		_db = reader.getPolicyDB();
	}
	/*
	 * Layouts all components.
	 */
	void makePanel() {
		GridBagConstraints cns = new GridBagConstraints();

		cns.weightx = 0.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.VERTICAL;
		cns.ipadx = cns.ipady = 5;
		cns.insets = new Insets(10, 5, 5, 5);

		_panel.setConstraints(cns);
		BorderPanel grantPanel = new BorderPanel(LABEL_GRANT_PANEL);

		_panel.add(grantPanel, 1, 0.0);
		setupGrantPanel(grantPanel);

		cns.fill = GridBagConstraints.BOTH;
		_panel.add(setting_panel, GridBagConstraints.REMAINDER, 1.0);

		setting_panel.setLayout(layout);

		PolicyGrant firstGrant = null;
		Enumeration grants = _db.getGrants();

		while (grants.hasMoreElements()) {
			Object obj = grants.nextElement();

			if (obj instanceof PolicyGrant) {
				PolicyGrant grant = (PolicyGrant)obj;

				if (firstGrant == null) {
					firstGrant = grant;
				} 
				addGrantPanel(grant);
			} 
		} 

		showGrantPanel(firstGrant);
	}
	public void removeGrantPanel(int idx, String name) {
		if (hasGrant(name)) {
			_grantList.remove(idx);
			Object obj = privileges.get(name);

			if (obj instanceof SecurityConfigPanel) {
				SecurityConfigPanel panel = (SecurityConfigPanel)obj;

				privileges.remove(obj);
				setting_panel.remove(panel);
			} 
		} 
	}
	public void save() {
		PolicyDB db = new PolicyDB();
		final int num = _grantList.getItemCount();
		int idx = 0;

		for (idx = 0; idx < num; idx++) {
			final String item = _grantList.getItem(idx);
			PolicyGrant grant = GrantEditor.toGrant(item);
			Object obj = privileges.get(item);

			if (obj instanceof SecurityConfigPanel) {
				SecurityConfigPanel panel = (SecurityConfigPanel)obj;

				panel.addPermissions(grant);
			} 
			db.addGrant(grant);
		} 

		try {
			final String policyFilename = 
				PolicyFileReader.getUserPolicyFilename();
			File policyFile = new File(policyFilename);

			if (policyFile.exists()) {
				final String backupFilename = 
					PolicyFileWriter.getBackupFilename(policyFilename);
				File backupFile = new File(backupFilename);

				try {
					policyFile.renameTo(backupFile);
				} catch (SecurityException excpt) {
					System.err.println(excpt.toString());
					return;
				} 
			} 
			PolicyFileWriter.writePolicyDB(policyFilename, db);
		} catch (IOException excpt) {
			System.err.println(excpt.toString());
		} 
	}
	/*
	 * setup Grant Panel
	 */
	void setupGrantPanel(BorderPanel panel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;
		cns.ipadx = cns.ipady = 3;
		cns.insets = panel.topInsets();
		cns.insets.bottom = panel.bottomInsets().bottom;

		panel.setConstraints(cns);

		cns.weighty = 0.1;
		_grantPanel = new GrantEditPanel(this, _grantList, _grantEditor);
		panel.add(_grantPanel, GridBagConstraints.REMAINDER, 1.0);
		_grantList.addItemListener(this);

		cns.insets.top = 0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.weighty = 0.0;
		panel.add(_grantEditor, GridBagConstraints.REMAINDER, 0.1);
	}
	public void showGrantPanel(PolicyGrant grant) {
		showGrantPanel(getGrantName(grant));
	}
	public void showGrantPanel(String name) {
		if (_grantList.getSelectedIndex() < 0) {
			for (int i = 0; i < _grantList.getItemCount(); i++) {
				if (name.equals(_grantList.getItem(i))) {
					_grantList.select(i);
					break;
				} 
			} 
		} 
		layout.show(setting_panel, name);
	}
	public void updateValues() {
		Resource res = Resource.getResourceFor("aglets");
		Enumeration e = privileges.elements();

		while (e.hasMoreElements()) {
			((SecurityConfigPanel)e.nextElement()).updateValues();
		} 
	}
	private void writePolicyFile() {
		final String file = PolicyFileReader.getUserPolicyFilename();

		try {
			PolicyFileWriter.writePolicyDB(file, _db);
		} catch (IOException excpt) {
			System.err.println(excpt.toString());
		} 
	}
}
