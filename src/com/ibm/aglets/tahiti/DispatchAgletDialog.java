package com.ibm.aglets.tahiti;

/*
 * @(#)DispatchAgletDialog.java
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
import com.ibm.aglet.AgletProxy;

import com.ibm.aglets.*;
import com.ibm.awb.misc.Resource;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.List;
import java.awt.Button;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.StringTokenizer;

/**
 * Class DispatchAgletDialog represents the dialog for dispatching an Aglet.
 * 
 * @version     1.02    $Date: 2001/07/28 06:32:27 $
 * @author      Danny B. Lange
 * Mitsuru Oshima
 */

final class DispatchAgletDialog extends TahitiDialog 
	implements ActionListener, ItemListener {

	/*
	 * The proxy to be dispatched.
	 */
	private AgletProxy proxy = null;

	/*
	 * GUI components
	 */
	private TextField _arlSelection = new TextField(40);
	private List _arlList = new List(10, false);
	private Button _add = new Button("Add to AddressBook");
	private Button _remove = new Button("Remove");

	/*
	 * Constructs a new Aglet dispatch dialog.
	 */
	DispatchAgletDialog(MainWindow parent, AgletProxy proxy) {
		super(parent, "Dispatch", false);
		this.proxy = proxy;

		add("Center", makePanel());

		addButton("Dispatch", this);
		addCloseButton("Cancel");

		updateList();
		disabling();
	}
	/*
	 * Creates an Aglet dispatch dialog.
	 */
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if ("add".equals(cmd)) {
			add();
			return;
		} else if ("remove".equals(cmd)) {
			remove();
			return;
		} 

		// Dispatch Button, TextField and List
		if (proxy == null) {
			return;
		} 
		if (!"".equals(_arlSelection.getText())) {
			setVisible(false);
			dispose();

			String dest = _arlSelection.getText();

			getMainWindow().dispatchAglet(proxy, dest);
			return;
		} 
		beep();
	}
	/*
	 * Adds an item to the list
	 */
	void add() {
		String name = _arlSelection.getText().trim();

		if (name.length() == 0) {
			return;
		} 
		int num = _arlList.getItemCount();

		for (int i = 0; i < num; i++) {
			if (_arlList.getItem(i).equals(name)) {
				return;
			} 
		} 
		_arlList.add(name);

		updateProperty();
	}
	private void disabling() {
		_remove.setEnabled(_arlList.getSelectedIndex() != -1);
	}
	// Handles list box selections.
	// 
	public void itemStateChanged(ItemEvent ev) {
		disabling();
		_arlSelection.setText(_arlList.getSelectedItem());
	}
	/*
	 * Layouts all Components
	 */
	protected GridBagPanel makePanel() {
		GridBagPanel p = new GridBagPanel();

		GridBagConstraints cns = new GridBagConstraints();

		cns.insets = new Insets(5, 5, 5, 5);
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;


		p.setConstraints(cns);

		/*
		 * Aglet name
		 */
		String agletname = "Invalid Aglet";

		try {
			agletname = (proxy == null ? "No Aglet" 
						 : proxy.getAgletClassName());
		} catch (InvalidAgletException ex) {}
		p.add(new Label(agletname, Label.CENTER));

		/*
		 * Destination ARL
		 */
		p.add(new Label("Destination URL"), 1, 0.0);

		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		p.add(_arlSelection);
		_arlSelection.addActionListener(this);

		/*
		 * Hot List
		 */
		cns.weightx = 1.0;
		cns.gridwidth = 1;
		cns.fill = GridBagConstraints.NONE;
		cns.anchor = GridBagConstraints.WEST;
		p.add(new Label("AddressBook"));

		cns.anchor = GridBagConstraints.EAST;
		p.add(_add);
		_add.setActionCommand("add");
		_add.addActionListener(this);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		p.add(_remove);
		_remove.setActionCommand("remove");
		_remove.addActionListener(this);

		cns.weighty = 1.0;
		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.BOTH;
		_arlList.addActionListener(this);
		_arlList.addItemListener(this);
		p.add(_arlList);

		Util.setFixedFont(_arlList);

		// _arlList.setFont(DefaultResource.getFixedFont());
		_arlList.setBackground(Color.white);

		return p;
	}
	/*
	 * Delete an item from the list
	 */
	void remove() {
		if (_arlList.getSelectedIndex() != -1) {
			_arlList.remove(_arlList.getSelectedIndex());
			updateProperty();
		} 
		disabling();
	}
	/*
	 * Updates the addressbook
	 */
	private void updateList() {
		Resource res = Resource.getResourceFor("aglets");
		String items[] = res.getStringArray("aglets.addressbook", " ");

		_arlList.removeAll();
		for (int i = 0; i < items.length; i++) {
			_arlList.add(items[i]);
		} 
	}
	private void updateProperty() {
		synchronized (_arlList) {
			int num = _arlList.getItemCount();
			String addressList = "";

			for (int i = 0; i < num; i++) {
				addressList += (_arlList.getItem(i) + " ");
			} 
			Resource res = Resource.getResourceFor("aglets");

			res.setResource("aglets.addressbook", addressList);
			res.save("Tahiti");
		} 
	}
}
