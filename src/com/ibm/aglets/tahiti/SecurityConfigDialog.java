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

import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;
import java.awt.CardLayout;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;


import java.io.File;
import java.io.IOException;


import com.ibm.aglets.security.PolicyDB;
import com.ibm.aglets.security.PolicyFileReader;
import com.ibm.aglets.security.PolicyFileWriter;
import com.ibm.aglets.security.PolicyGrant;

import com.ibm.awb.misc.URIPattern;

import com.ibm.awb.misc.Resource;

import javax.swing.event.*;

/**
 * Class SecurityConfigDialog represents the dialog for
 * 
 * @version     1.03    96/04/15
 * @author      Danny B. Lange
 */

final class SecurityConfigDialog extends TahitiDialog 
	implements ActionListener, ListSelectionListener {


	/*
	 * Singleton instance reference.
	 */
	private static SecurityConfigDialog _instance = null;

	private PolicyDB _db = null;

	private AgentListPanel _grantList = new AgentListPanel(5);
	private GrantEditor _grantEditor = new GrantEditor();
	private GrantEditPanel _grantPanel = null;
	GridBagPanel _panel = new GridBagPanel();
	JPanel setting_panel = new JPanel();
	CardLayout layout = new CardLayout();
	Hashtable privileges = new Hashtable();

	/*
	 * Constructs a security configuration dialog.
	 */
	private SecurityConfigDialog(MainWindow parent) {
		super(parent, bundle.getString("dialog.secprefs.title"), true);
		loadPolicyFile();
		makePanel();
		this.getContentPane().add("Center",this._panel);
		
		// add buttons
		this.addJButton(bundle.getString("dialog.secprefs.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
		this.addJButton(bundle.getString("dialog.secprefs.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
	
	}

	/**
	 * *****************************************************************
	 * The call back methods
	 */

	/**
	 * Manage events from buttons.
	 * @param event the event to deal with
	 */
	public void actionPerformed(ActionEvent event) {
	    if(event==null){
	        return;
	    }
	    
	    
		String command = event.getActionCommand();
		
		if(command==null || command.equals("")){ 
		    return;
		}

		if(command.equals(TahitiCommandStrings.OK_COMMAND)){
		    // save the policy
		    this.save();
		    this.setVisible(false);
		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.secprefs.reboot"),bundle.getString("dialog.secprefs.reboot.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("warning"));
		}
		else
		if(command.equals(TahitiCommandStrings.CANCEL_COMMAND)){
		    this.setVisible(false);
		}
		
		this.dispose();
	}
	
	
	
	

	private void addGrantPanel(PolicyGrant grant) {
		addGrantPanel(getGrantName(grant), grant);
	}
	
	public void addGrantPanel(String name) {
		addGrantPanel(name, null);
	}
	
	private void addGrantPanel(String name, PolicyGrant grant) {
		if (!hasGrant(name)) {
			this._grantList.addItem(name);
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
	
	
	/**
	 * Events from the list
	 * @param event the event to work with
	 */
	public void valueChanged(ListSelectionEvent event) {
		String selected = (String) this._grantList.getSelectedItem();

		if(selected!=null){
		    this.showGrantPanel(selected);
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
		BorderPanel grantPanel = new BorderPanel(bundle.getString("dialog.secprefs.grantpanel"));

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
		this._grantList.addListSelectionListener(this);

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
