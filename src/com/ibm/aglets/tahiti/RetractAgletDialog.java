package com.ibm.aglets.tahiti;

/*
 * @(#)RetractAgletDialog.java
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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import java.util.Vector;
import java.util.Enumeration;

import java.net.*;

import com.ibm.awb.misc.Resource;

/**
 * Class RetractAgletDialog represents the dialog for Retracting an Aglet.
 * 
 * @version     1.02    $Date: 2001/07/28 06:32:32 $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

final class RetractAgletDialog extends TahitiDialog implements ActionListener, 
		ItemListener, Runnable {

	/*
	 * Singleton instance reference.
	 */
	private static RetractAgletDialog _instance = null;

	/*
	 * GUI components
	 */
	private Choice _servers = new Choice();

	private List _agletsList = new List();

	private GridBagLayout grid = new GridBagLayout();

	String currentList = null;

	com.ibm.aglet.AgletProxy proxies[] = null;

	Thread handler = null;

	/*
	 * Constructs a new Aglet retract dialog.
	 */
	private RetractAgletDialog(MainWindow parent) {
		super(parent, "Retract", true);

		add("Center", makePanel());

		addButton("Retract", this);
		addCloseButton("Cancel");
	}
	/*
	 * Creates an Aglet retract dialog.
	 */
	public void actionPerformed(ActionEvent ev) {
		int i = _agletsList.getSelectedIndex();

		if (i >= 0) {
			getMainWindow().retractAglet(proxies[i]);
		} 
		dispose();

		/*
		 * String key = _agletsList.getSelectedItem();
		 * if (key != null) {
		 * URL url = (URL)Tahiti.getMigrantAglet(key);
		 * Event ev = new Event(url, MainWindow.RETRACT_AGLET, null);
		 * // to work around the bug in JDK1.1 for UNIX
		 * hide();
		 * dispose();
		 * getParent().postEvent(ev);
		 * return;
		 * }
		 * System.out.print("\007");
		 * System.out.flush();
		 */
	}
	/*
	 * Singleton method to obtain the instance
	 */
	static RetractAgletDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new RetractAgletDialog(parent);
		} 
		_instance.updateList();
		return _instance;
	}
	synchronized public void itemStateChanged(ItemEvent ev) {
		updateList();
	}
	protected GridBagPanel makePanel() {
		GridBagPanel p = new GridBagPanel();
		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagConstraints.BOTH;
		cns.anchor = GridBagConstraints.WEST;
		cns.insets = new Insets(5, 5, 5, 5);
		cns.weighty = 0.0;
		cns.gridwidth = 1;
		cns.weightx = 0.1;

		// p.addLabeled("Remote Aglet", _retractSelection);
		// _retractSelection.setEnabled(false);
		// _retractSelection.setEditable(false);
		// _retractSelection.addActionListener(this);

		p.add(new Label("Remote Aglets List"), GridBagPanel.REMAINDER);

		_servers = new Choice();
		p.addLabeled("Select Server:", _servers);

		_servers.addItemListener(this);
		_agletsList.addActionListener(this);
		_agletsList.setBackground(Color.white);

		Util.setFixedFont(_agletsList);

		// _agletsList.setFont( DefaultResource.getFixedFont() );
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weighty = 1.0;
		p.add(_agletsList, cns);
		return p;
	}
	public void run() {
		String dest = (String)_servers.getSelectedObjects()[0];

		try {
			proxies = com.ibm.aglet.system.Aglets.getAgletProxies(dest);
		} catch (java.net.MalformedURLException ex) {

			// ex.printStackTrace();
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
		} 

		_agletsList.removeAll();
		if (proxies == null) {
			return;
		} 
		for (int i = 0; i < proxies.length; i++) {
			try {
				com.ibm.aglet.AgletInfo info = proxies[i].getAgletInfo();

				_agletsList.add(info.getAgletClassName() + " : " 
								+ info.getAgletID());
			} catch (com.ibm.aglet.InvalidAgletException ex) {
				_agletsList.add("InvalidAglet");
			} 
		} 
	}
	void updateChoice() {
		Resource res = Resource.getResourceFor("aglets");
		String list = res.getString("aglets.addressbook");

		if (list != null && list.equals(currentList) == false) {
			currentList = list;
			_servers.removeAll();
			_servers.addItem("Select Server");
			String items[] = res.getStringArray("aglets.addressbook", " ");

			for (int i = 0; i < items.length; i++) {
				_servers.addItem(items[i]);
			} 
		} 
	}
	/*
	 * Update list
	 */
	private void updateList() {
		updateChoice();
		if (handler != null) {
			handler.interrupt();
			handler.stop();
		} 
		handler = new Thread(this);
		handler.start();
	}
	/*
	 * public void foo() {
	 * String item = _agletsList.getSelectedItem();
	 * if (item != null) {
	 * String defaultValue =
	 * MainWindow.dotdotdot(item,
	 * _retractSelection.getColumns());
	 * _retractSelection.setText(defaultValue);
	 * }
	 * else {
	 * _retractSelection.setText("");
	 * }
	 * }
	 */

	public boolean windowClosing(WindowEvent ev) {
		return false;
	}
}
