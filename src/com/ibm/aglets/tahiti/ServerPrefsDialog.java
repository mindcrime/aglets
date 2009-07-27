package com.ibm.aglets.tahiti;

/*
 * @(#)ServerPrefsDialog.java
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

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglets.*;
import com.ibm.awb.misc.Resource;

import java.io.File;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Button;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.*;


/**
 * Class ServerPrefsDialog represents the dialog for
 * server preferences dialog,
 * e.g. aglets.public.root, aglets.public.aliases.
 * 
 * @version     1.00    98/05/27
 * @author      Hideki Tai
 * @author Luca Ferrari
 */

import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;

final class ServerPrefsDialog extends TahitiDialog implements ActionListener, 
		ItemListener {

	private JTextField _pubRoot;
	private AgentListPanel _aliases;
	private JTextField _alias_1;
	private JTextField _alias_2;

	private JButton _alias_add;
	private JButton _alias_remove;
	private JButton _alias_modify;

	static private final String ALIASES_SEP = " -> ";

	/*
	 * Singleton instance reference.
	 */
	private static ServerPrefsDialog _instance = null;

	private ServerPrefsDialog(MainWindow parent) {
		super(parent, bundle.getString("dialog.serprefs.title"), true);

		makePanel();

		// add buttons
		this.addJButton(bundle.getString("dialog.serprefs.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
		this.addJButton(bundle.getString("dialog.serprefs.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
		this.addJButton(bundle.getString("dialog.serprefs.button.default"),TahitiCommandStrings.DEFAULT_COMMAND,IconRepository.getIcon("default"),this);
		
		this.pack();
	}
	
	
	/**
	 * Manage events from the buttons.
	 * @param event the event to manage
	 */
	public void actionPerformed(java.awt.event.ActionEvent event) {
		String command = event.getActionCommand();

		if (command.equals(TahitiCommandStrings.ADD_COMMAND)) {
		    // add a new alias
		    
			String ali_name = _alias_1.getText();
			String ali_path = _alias_2.getText();

			if (ali_name.startsWith("/") == false) {
				ali_name = "/" + ali_name;
			}
			
			try {
				String entry = getAliasEntry(ali_name, ali_path);
				String items[] = _aliases.getItems();
				int i = 0;

				while (i < items.length) {
					if (entry.equals(items[i])) {
						break;
					} 
					i++;
				} 
				if (i >= items.length) {
					_aliases.addItem(entry);
				} 
			} catch (NullPointerException ex) {

				// No text was set in the TextTield (_alias_1, _alias_2)
			} 
		} 
		else 
		if (command.equals(TahitiCommandStrings.REMOVE_COMMAND)) {
		    // remove an alias
		    
			int idx = _aliases.getSelectedIndex();

			if (idx >= 0) {
				_aliases.remove(idx);
			} 
		} 
		else 
		if (command.equals(TahitiCommandStrings.CREATE_COMMAND)) {
		    // modify an alias
		    
			int idx = _aliases.getSelectedIndex();
			String ali_name = _alias_1.getText();
			String ali_path = _alias_2.getText();

			if (idx >= 0) {
				try {
					String entry = getAliasEntry(ali_name, ali_path);

					_aliases.replaceItem(entry, idx);
				} catch (NullPointerException ex) {

					// No text was set in the TextTield (_alias_1, _alias_2)
				} 
			} 
		} 
		else 
		if (command.equals(TahitiCommandStrings.OK_COMMAND)) {
			commitValues();
			dispose();
		} 
		else 
		if (command.equals(TahitiCommandStrings.CANCEL_COMMAND)) {
			dispose();
		} 
		else 
		if (command.equals(TahitiCommandStrings.DEFAULT_COMMAND)) {
			updateValues();
		} 
	}
	
	
	private void commitValues() {
		Resource aglets_res = Resource.getResourceFor("aglets");
		String public_root = _pubRoot.getText();

		if (!public_root.endsWith(File.separator)) {
			public_root = public_root + File.separator;
		} 
		aglets_res.setResource("aglets.public.root", public_root);

		StringBuffer sb = new StringBuffer();
		String items[] = _aliases.getItems();

		if (items != null && items.length > 0) {
			sb.append(items[0]);
			for (int i = 1; i < items.length; i++) {
				sb.append("," + items[i]);
			} 
		} 

		aglets_res.setResource("aglets.public.aliases", sb.toString());

		String aglet_path = aglets_res.getString("aglets.class.path");

		if (aglet_path == null) {
			aglet_path = "";
		} 
		aglet_path = aglet_path.trim();
		if (aglet_path.length() > 0 
				&& aglet_path.charAt(aglet_path.length() - 1) != ',') {
			aglet_path = aglet_path + ",";
		} 
		aglet_path = aglet_path + public_root;
		aglets_res.setResource("aglets.class.path", aglet_path);
	}
	private String getAliasEntry(String ali_name, String ali_path) 
			throws NullPointerException {
		if (ali_name.length() == 0 || ali_path.length() == 0) {
			throw new NullPointerException();
		} 
		if (!ali_name.endsWith("/")) {
			ali_name = ali_name + "/";
		} 
		if (!ali_path.endsWith(File.separator)) {
			ali_path = ali_path + File.separator;
		} 
		return ali_name + ALIASES_SEP + ali_path;
	}
	/*
	 * Singletion method to get the instnace
	 */
	static ServerPrefsDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new ServerPrefsDialog(parent);
		} else {
			_instance.updateValues();
		} 
		return _instance;
	}
	public void itemStateChanged(java.awt.event.ItemEvent ev) {
		if (ev.getItemSelectable() == _aliases 
				&& ev.getStateChange() == ItemEvent.SELECTED) {
			int item = ((Integer)ev.getItem()).intValue();
			String alias = _aliases.getItem(item);
			int idx = alias.indexOf(ALIASES_SEP);
			String ali_name = alias.substring(0, idx);

			if (ali_name.startsWith("/")) {
				ali_name = ali_name.substring(1);
			} 
			String ali_path = alias.substring(idx + ALIASES_SEP.length());

			_alias_1.setText(ali_name);
			_alias_2.setText(ali_path);
		} 
	}

	
	/*
	 * Layouts all components.
	 */
	protected void makePanel() {

	    JPanel p = new JPanel();
	    JPanel pubRootPanel = new JPanel();
	    this.getContentPane().add("Center",p);
	    setupPubRootPanel(pubRootPanel);
	    p.add(pubRootPanel);
	    
		

		updateValues();
	}
	
	
	private void setupPubRootPanel(JPanel p) {
	    
	    p.setLayout(new BorderLayout());
	    _pubRoot = new JTextField(40);
	    JPanel northPanel = new JPanel();
	    northPanel.setLayout(new FlowLayout());
	    northPanel.add(new JLabel(bundle.getString("dialog.serprefs.label.root")));
	    northPanel.add(_pubRoot);
	    p.add("North",northPanel);
	    JPanel aliasesPanel = new JPanel();
		aliasesPanel.setLayout(new BorderLayout());
		_aliases = new AgentListPanel();
		
		aliasesPanel.add("Center",_aliases);
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		aliasesPanel.add("South",southPanel);
		p.add("Center",aliasesPanel);
		
	    
			_alias_1 = new JTextField(20);
			_alias_2 = new JTextField(20);
			JPanel centerPanel = new JPanel();
			centerPanel.setLayout(new FlowLayout());
			centerPanel.add(new JLabel("/"));
			centerPanel.add(_alias_1);
			centerPanel.add(new JLabel("=>"));
			centerPanel.add(_alias_2);
			southPanel.add("Center",centerPanel);

			 

			_alias_add = new JButton(bundle.getString("dialog.serprefs.button.add"));
			_alias_remove = new JButton(bundle.getString("dialog.serprefs.button.remove"));
			_alias_modify = new JButton(bundle.getString("dialog.serprefs.button.modify"));
			_alias_add.setActionCommand(TahitiCommandStrings.ADD_COMMAND);
			_alias_remove.setActionCommand(TahitiCommandStrings.REMOVE_COMMAND);
			_alias_modify.setActionCommand(TahitiCommandStrings.CREATE_COMMAND);
			_alias_add.addActionListener(this);
			_alias_remove.addActionListener(this);
			_alias_modify.addActionListener(this);

			JPanel pp = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			pp.add(_alias_add);
			pp.add(_alias_remove);
			pp.add(_alias_modify);
			southPanel.add("South",pp);
 
	} 
	
	
	private void updateValues() {
		Resource aglets_res = Resource.getResourceFor("aglets");
		String public_root = aglets_res.getString("aglets.public.root", "");
		String public_root_aliases[] = 
			aglets_res.getStringArray("aglets.public.aliases", ",");

		_pubRoot.setText(public_root);
		_aliases.removeAll();
		for (int i = 0; i < public_root_aliases.length; i++) {
			if (public_root_aliases[i] != null 
					&& public_root_aliases[i].length() > 0) {
				int idx = public_root_aliases[i].indexOf(ALIASES_SEP);

				if (idx < 0) {
				    JOptionPane.showMessageDialog(this,bundle.getString("dialog.serprefs.error.properties.message"),bundle.getString("dialog.serprefs.error.properties.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
				} else {
					_aliases.addItem(public_root_aliases[i]);
				} 
			} 
		} 
	}
}
