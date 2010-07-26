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
import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

class EditListPanel extends GridBagPanel implements ItemListener,
	ActionListener {
    private static final String LABEL_ADD = "add";
    private static final String LABEL_REMOVE = "remove";

    private Button _add = new Button(LABEL_ADD);
    private Button _remove = new Button(LABEL_REMOVE);
    private List _list;
    private Editor _editor;

    EditListPanel(String title, List list, Editor editor) {
	this._list = list;
	this._editor = editor;

	GridBagConstraints cns = new GridBagConstraints();

	cns.weightx = 0.0;
	cns.weighty = 0.0;
	cns.fill = GridBagConstraints.NONE;
	cns.anchor = GridBagConstraints.WEST;
	cns.ipadx = cns.ipady = 2;

	this.setConstraints(cns);

	if ((title != null) && !title.equals("")) {
	    this.add(new Label(title), 1, 0.0);
	}
	this.add(this._add, 1, 0.0);
	this.add(this._remove, GridBagConstraints.REMAINDER, 1.0);
	cns.weightx = 1.0;
	cns.weighty = 1.0;
	cns.fill = GridBagConstraints.BOTH;
	cns.anchor = GridBagConstraints.CENTER;
	this.add(this._list, GridBagConstraints.REMAINDER, 1.0);

	this._add.setActionCommand(LABEL_ADD);
	this._add.addActionListener(this);
	this._remove.setActionCommand(LABEL_REMOVE);
	this._remove.addActionListener(this);
	this._list.addItemListener(this);
    }

    public void actionPerformed(ActionEvent ev) {
	String cmd = ev.getActionCommand();

	if (LABEL_ADD.equals(cmd)) {
	    this.addItemFromEditorIntoList();
	} else if (LABEL_REMOVE.equals(cmd)
		&& (this._list.getSelectedIndex() >= 0)) {
	    this.removeItemFromList();
	}
    }

    private void addItemFromEditorIntoList() {
	this.addItemIntoList(this._editor.getText().trim());
    }

    protected void addItemIntoList(String item) {
	if (!this.hasItem(item)) {
	    this._list.add(item);
	    this.selectItem(item);
	}
    }

    void addItemIntoList(Vector args) {
	this.addItemIntoList(EditorPanel.toText(args));
    }

    protected int getSelectedIndex() {
	return this._list.getSelectedIndex();
    }

    protected String getSelectedItem() {
	final int idx = this.getSelectedIndex();

	if (idx < 0) {
	    return null;
	}
	return this._list.getItem(idx);
    }

    private boolean hasItem(String text) {
	String[] items = this._list.getItems();
	int i = 0;

	for (i = 0; i < items.length; i++) {
	    if (text.equals(items[i])) {
		return true;
	    }
	}
	return false;
    }

    public void itemStateChanged(ItemEvent ev) {
	if (this._list.getSelectedIndex() >= 0) {
	    this._remove.setEnabled(true);
	    this._editor.setText(this._list.getSelectedItem());
	} else {
	    this._remove.setEnabled(false);
	    this._editor.setText("");
	}
    }

    protected void removeItemFromList() {
	this._list.remove(this._list.getSelectedIndex());
    }

    public void selectItem(String text) {
	String[] items = this._list.getItems();
	int i = 0;

	for (i = 0; i < items.length; i++) {
	    if (text.equals(items[i])) {
		this._list.select(i);
		this._remove.setEnabled(true);
		this._editor.setText(this._list.getSelectedItem());
		return;
	    }
	}
	return;
    }
}
