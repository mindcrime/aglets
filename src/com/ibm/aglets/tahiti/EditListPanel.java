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
	/**
	 * 
	 */
	private static final long serialVersionUID = 6991045036719636071L;
	private static final String LABEL_ADD = "add";
	private static final String LABEL_REMOVE = "remove";

	private final Button _add = new Button(LABEL_ADD);
	private final Button _remove = new Button(LABEL_REMOVE);
	private final List _list;
	private final Editor _editor;

	EditListPanel(final String title, final List list, final Editor editor) {
		_list = list;
		_editor = editor;

		final GridBagConstraints cns = new GridBagConstraints();

		cns.weightx = 0.0;
		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.NONE;
		cns.anchor = GridBagConstraints.WEST;
		cns.ipadx = cns.ipady = 2;

		setConstraints(cns);

		if ((title != null) && !title.equals("")) {
			this.add(new Label(title), 1, 0.0);
		}
		this.add(_add, 1, 0.0);
		this.add(_remove, GridBagConstraints.REMAINDER, 1.0);
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;
		cns.anchor = GridBagConstraints.CENTER;
		this.add(_list, GridBagConstraints.REMAINDER, 1.0);

		_add.setActionCommand(LABEL_ADD);
		_add.addActionListener(this);
		_remove.setActionCommand(LABEL_REMOVE);
		_remove.addActionListener(this);
		_list.addItemListener(this);
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		final String cmd = ev.getActionCommand();

		if (LABEL_ADD.equals(cmd)) {
			addItemFromEditorIntoList();
		} else if (LABEL_REMOVE.equals(cmd)
				&& (_list.getSelectedIndex() >= 0)) {
			removeItemFromList();
		}
	}

	private void addItemFromEditorIntoList() {
		this.addItemIntoList(_editor.getText().trim());
	}

	protected void addItemIntoList(final String item) {
		if (!hasItem(item)) {
			_list.add(item);
			selectItem(item);
		}
	}

	void addItemIntoList(final Vector args) {
		this.addItemIntoList(EditorPanel.toText(args));
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

	private boolean hasItem(final String text) {
		final String[] items = _list.getItems();
		int i = 0;

		for (i = 0; i < items.length; i++) {
			if (text.equals(items[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void itemStateChanged(final ItemEvent ev) {
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

	public void selectItem(final String text) {
		final String[] items = _list.getItems();
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
