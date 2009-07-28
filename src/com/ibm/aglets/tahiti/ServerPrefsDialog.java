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

/**
 * Class ServerPrefsDialog represents the dialog for
 * server preferences dialog,
 * e.g. aglets.public.root, aglets.public.aliases.
 * 
 * @version     1.00    98/05/27
 * @author      Hideki Tai
 */

final class ServerPrefsDialog extends TahitiDialog implements ActionListener, 
		ItemListener {

	private TextField _pubRoot;
	private List _aliases;
	private TextField _alias_1;
	private TextField _alias_2;

	private Button _alias_add;
	private Button _alias_remove;
	private Button _alias_modify;

	static private final String ALIASES_SEP = " -> ";

	/*
	 * Singleton instance reference.
	 */
	private static ServerPrefsDialog _instance = null;

	private ServerPrefsDialog(MainWindow parent) {
		super(parent, "Server Preferences", true);

		makePanel();

		addButton("OK", this);
		addButton("Cancel", this);
		addButton("Restore Defaults", this);
	}
	public void actionPerformed(java.awt.event.ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if ("Add".equals(cmd)) {
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
					_aliases.add(entry);
				} 
			} catch (NullPointerException ex) {

				// No text was set in the TextTield (_alias_1, _alias_2)
			} 
		} else if ("Remove".equals(cmd)) {
			int idx = _aliases.getSelectedIndex();

			if (idx >= 0) {
				_aliases.remove(idx);
			} 
		} else if ("Modify".equals(cmd)) {
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
		} else if ("OK".equals(cmd)) {
			commitValues();
			dispose();
		} else if ("Cancel".equals(cmd)) {
			dispose();
		} else if ("Restore Defaults".equals(cmd)) {
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
		GridBagPanel p = new GridBagPanel();

		add("Center", p);

		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.ipadx = cns.ipady = 5;
		cns.insets = new Insets(5, 5, 5, 5);
		p.setConstraints(cns);

		BorderPanel pubRootPanel = new BorderPanel("Root Path");

		p.add(pubRootPanel, GridBagConstraints.REMAINDER);

		setupPubRootPanel(pubRootPanel);

		updateValues();
	}
	private void setupPubRootPanel(BorderPanel p) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.NONE;
		cns.anchor = GridBagConstraints.WEST;
		cns.insets = p.topInsets();
		p.add(new Label("Public Root:"), cns);

		_pubRoot = new TextField(40);
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.anchor = GridBagConstraints.WEST;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		p.add(_pubRoot, cns);

		BorderPanel aliasesPanel = new BorderPanel("Aliases");

		cns = new GridBagConstraints();
		cns.fill = GridBagPanel.BOTH;
		cns.gridwidth = 2;
		cns.weighty = 1.0;
		cns.insets = new Insets(5, 5, 5, 5);
		p.add(aliasesPanel, cns);

		{
			GridBagConstraints cns2 = new GridBagConstraints();

			cns2.anchor = GridBagConstraints.WEST;
			cns2.fill = GridBagConstraints.BOTH;
			cns2.weightx = 1.0;
			cns2.weighty = 1.0;
			cns2.insets = aliasesPanel.topInsets();
			cns2.insets.bottom = aliasesPanel.bottomInsets().bottom;
			aliasesPanel.setConstraints(cns2);
			GridBagPanel p1 = new GridBagPanel();

			aliasesPanel.add(p1);

			cns2 = new GridBagConstraints();
			cns2.anchor = GridBagConstraints.WEST;
			cns2.fill = GridBagConstraints.HORIZONTAL;
			cns2.weightx = 1.0;
			cns2.weighty = 1.0;
			p1.setConstraints(cns2);

			_aliases = new List(5);
			_aliases.addActionListener(this);
			_aliases.addItemListener(this);
			p1.add(_aliases, GridBagConstraints.REMAINDER, 0.1);

			_alias_1 = new TextField(20);
			_alias_2 = new TextField(20);
			{
				GridBagPanel pp = new GridBagPanel();
				GridBagConstraints cns3 = new GridBagConstraints();

				cns3.fill = GridBagConstraints.HORIZONTAL;
				cns3.weightx = 1.0;
				cns3.weighty = 0.0;
				pp.setConstraints(cns3);
				pp.add(new Label("/"));
				pp.add(_alias_1);

				pp.add(new Label("->"), new GridBagConstraints());

				pp.add(_alias_2);
				p1.add(pp);

				// p1.add(pp, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
			} 

			_alias_add = new Button("Add");
			_alias_remove = new Button("Remove");
			_alias_modify = new Button("Modify");
			_alias_add.addActionListener(this);
			_alias_remove.addActionListener(this);
			_alias_modify.addActionListener(this);
			{
				Panel pp = new Panel(new FlowLayout(FlowLayout.RIGHT));

				pp.add(_alias_add);
				pp.add(_alias_remove);
				pp.add(_alias_modify);
				p1.add(pp, GridBagConstraints.REMAINDER);
			} 
		} 
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
					System.out
						.println("Illegal resource setting in aglets.properties: " 
								 + public_root_aliases[i]);
				} else {
					_aliases.add(public_root_aliases[i]);
				} 
			} 
		} 
	}
}
