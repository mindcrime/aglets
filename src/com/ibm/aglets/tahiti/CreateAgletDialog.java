package com.ibm.aglets.tahiti;

/*
 * @(#)CreateAgletDialog.java
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
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Color;
import java.awt.List;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import java.util.StringTokenizer;

import com.ibm.awb.misc.Resource;

/**
 * Class CreateAgletDialog represents the dialog for creating a new Aglet
 * instance. The class uses a CardLayout to handle the GUI differences
 * between creating an Aglet for a system class, local class file, remote
 * URL, and the hotlist of recently used Aglet classes.
 * 
 * @version     1.04    $Date: 2001/07/28 06:32:34 $
 * @author      Danny B. Lange
 */

final class CreateAgletDialog extends TahitiDialog implements ActionListener, 
		ItemListener {

	/*
	 * Singleton instance reference.
	 */
	private static CreateAgletDialog _instance = null;

	/*
	 * GUI components
	 */
	private TextField _classField = new TextField(20);
	private TextField _urlField = new TextField(20);
	private List _selectionList = new List(10, false);
	private Button _add = new Button("Add to List");
	private Button _remove = new Button("Remove");

	/**
	 * Constructs a new Aglet creation dialog.
	 * @param parent the parent frame.
	 */
	private CreateAgletDialog(MainWindow parent) {
		super(parent, "Create Aglet", false);

		add("Center", makePanel());

		addButton("Create", this);
		addCloseButton("Cancel");
		addButton("Reload Class and Create", this);

		_selectionList.addActionListener(this);
		_selectionList.addItemListener(this);
		Util.setFixedFont(_selectionList);
		_selectionList.setBackground(Color.white);
	}
	/*
	 * Creation without reloading class.
	 */
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if ("add".equals(cmd)) {
			add();
		} else if ("remove".equals(cmd)) {
			remove();
		} else if ("Reload Class and Create".equals(cmd)) {
			createAglet(true);
			setVisible(false);
		} else {
			createAglet(false);
			setVisible(false);
		} 
	}
	/*
	 * Adds an item to the hotlist
	 */
	void add() {
		String name = _urlField.getText().trim();

		if (name.length() > 0 && name.charAt(name.length() - 1) != '/') {
			name += '/';
		} 
		name += _classField.getText().trim();

		if (name.length() == 0) {
			return;
		} 

		int num = _selectionList.getItemCount();

		for (int i = 0; i < num; i++) {
			if (_selectionList.getItem(i).equals(name)) {
				return;
			} 
		} 
		_selectionList.add(name);
		updateProperty();
	}
	public void closeButtonPressed() {
		if (_selectionList.getSelectedIndex() != -1) {
			_selectionList.deselect(_selectionList.getSelectedIndex());
		} 
	}
	/*
	 * Creates an Aglet creation dialog.
	 */
	synchronized void createAglet(boolean reload) {
		if (_selectionList.getSelectedIndex() != -1) {
			_selectionList.deselect(_selectionList.getSelectedIndex());
		} 
		disabling();
		String classname = _classField.getText().trim();
		String codebase = _urlField.getText().trim();

		// System.out.println("createAglet("+codebase+","+classname+","+reload+")");
		getMainWindow().createAglet(codebase, classname, reload);
	}
	/*
	 * 
	 */
	private void disabling() {
		_remove.setEnabled(_selectionList.getSelectedIndex() != -1);
	}
	/*
	 * Singleton method to get the instance
	 */
	static CreateAgletDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new CreateAgletDialog(parent);
		} 
		_instance.updateList();
		return _instance;
	}
	/*
	 * Handles list box selections.
	 */
	public void itemStateChanged(ItemEvent ev) {
		disabling();

		String selectedItem = _selectionList.getSelectedItem();

		if (selectedItem.toLowerCase().startsWith("http://") 
				|| selectedItem.toLowerCase().startsWith("https://") 
				|| selectedItem.toLowerCase().startsWith("atps://") 
				|| selectedItem.toLowerCase().startsWith("atp://") 
				|| selectedItem.toLowerCase().startsWith("file://")) {
			int delimiter = selectedItem.lastIndexOf('/');

			_classField.setText(selectedItem.substring(delimiter + 1));
			_urlField.setText(selectedItem.substring(0, delimiter));
		} else {
			_classField.setText(selectedItem);
			_urlField.setText("");
		} 
	}
	/*
	 * Creates the panel
	 */
	protected GridBagPanel makePanel() {
		GridBagPanel p = new GridBagPanel();
		GridBagConstraints cns = new GridBagConstraints();

		/*
		 * Initializes the constraints
		 */
		cns.ipadx = cns.ipady = 0;
		cns.insets = new Insets(5, 5, 5, 5);
		cns.anchor = GridBagConstraints.WEST;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.HORIZONTAL;

		p.setConstraints(cns);

		/*
		 * Label
		 */
		_classField.addActionListener(this);
		_urlField.addActionListener(this);

		p.addLabeled("Aglet name", _classField);
		p.addLabeled("Source URL", _urlField);

		/*
		 * HotList
		 */
		p.add(new Label("Aglets List"), GridBagPanel.WEST, GridBagPanel.NONE, 
			  1);

		p.add(_add, GridBagPanel.EAST, GridBagPanel.NONE, 1);
		p.add(_remove, GridBagPanel.REMAINDER);

		_add.setActionCommand("add");
		_add.addActionListener(this);
		_remove.setActionCommand("remove");
		_remove.addActionListener(this);

		cns.anchor = GridBagConstraints.CENTER;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;
		p.add(_selectionList, cns);

		disabling();
		return p;
	}
	/*
	 * Deletes an item from the hotlist.
	 */
	void remove() {
		if (_selectionList.getSelectedIndex() != -1) {
			_selectionList.remove(_selectionList.getSelectedIndex());
			_classField.setText("");
			_urlField.setText("");
			updateProperty();
		} 
		disabling();
	}
	/*
	 * Updates the hotlist
	 */
	private void updateList() {
		Resource res = Resource.getResourceFor("aglets");
		String lists = res.getString("aglets.agletsList");

		_selectionList.removeAll();

		StringTokenizer st = new StringTokenizer(lists, " ", false);

		while (st.hasMoreTokens()) {
			_selectionList.add(st.nextToken());
		} 
	}
	private void updateProperty() {
		synchronized (_selectionList) {
			int num = _selectionList.getItemCount();
			String agletsList = "";

			for (int i = 0; i < num; i++) {
				agletsList += (_selectionList.getItem(i) + " ");
			} 
			Resource res = Resource.getResourceFor("aglets");

			res.setResource("aglets.agletsList", agletsList);
			res.save("Tahiti");
		} 
	}
	public boolean windowClosing(WindowEvent ev) {
		closeButtonPressed();
		return false;
	}
}
