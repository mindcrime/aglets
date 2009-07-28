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

class EditListPanel extends GridBagPanel implements ItemListener, 
		ActionListener {
	private static final String LABEL_ADD = "add";
	private static final String LABEL_REMOVE = "remove";

	private Button _add = new Button(LABEL_ADD);
	private Button _remove = new Button(LABEL_REMOVE);
	private List _list;
	private Editor _editor;

	EditListPanel(String title, List list, Editor editor) {
		_list = list;
		_editor = editor;

		GridBagConstraints cns = new GridBagConstraints();

		cns.weightx = 0.0;
		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.NONE;
		cns.anchor = GridBagConstraints.WEST;
		cns.ipadx = cns.ipady = 2;

		setConstraints(cns);

		if (title != null &&!title.equals("")) {
			add(new Label(title), 1, 0.0);
		} 
		add(_add, 1, 0.0);
		add(_remove, GridBagConstraints.REMAINDER, 1.0);
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;
		cns.anchor = GridBagConstraints.CENTER;
		add(_list, GridBagConstraints.REMAINDER, 1.0);

		_add.setActionCommand(LABEL_ADD);
		_add.addActionListener(this);
		_remove.setActionCommand(LABEL_REMOVE);
		_remove.addActionListener(this);
		_list.addItemListener(this);
	}
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if (LABEL_ADD.equals(cmd)) {
			addItemFromEditorIntoList();
		} else if (LABEL_REMOVE.equals(cmd) 
				   && _list.getSelectedIndex() >= 0) {
			removeItemFromList();
		} 
	}
	private void addItemFromEditorIntoList() {
		addItemIntoList(_editor.getText().trim());
	}
	protected void addItemIntoList(String item) {
		if (!hasItem(item)) {
			_list.add(item);
			selectItem(item);
		} 
	}
	void addItemIntoList(Vector args) {
		addItemIntoList(EditorPanel.toText(args));
	}
	protected int getSelectedIndex() {
		return _list.getSelectedIndex();
	}
	protected String getSelectedItem() {
		final int idx = getSelectedIndex();

		if (idx < 0) {
			return null;
		} 
		return _list.getItem(idx);
	}
	private boolean hasItem(String text) {
		String[] items = _list.getItems();
		int i = 0;

		for (i = 0; i < items.length; i++) {
			if (text.equals(items[i])) {
				return true;
			} 
		} 
		return false;
	}
	public void itemStateChanged(ItemEvent ev) {
		if (_list.getSelectedIndex() >= 0) {
			_remove.setEnabled(true);
			_editor.setText(_list.getSelectedItem());
		} else {
			_remove.setEnabled(false);
			_editor.setText("");
		} 
	}
	protected void removeItemFromList() {
		_list.remove(_list.getSelectedIndex());
	}
	public void selectItem(String text) {
		String[] items = _list.getItems();
		int i = 0;

		for (i = 0; i < items.length; i++) {
			if (text.equals(items[i])) {
				_list.select(i);
				_remove.setEnabled(true);
				_editor.setText(_list.getSelectedItem());
				return;
			} 
		} 
		return;
	}
}
