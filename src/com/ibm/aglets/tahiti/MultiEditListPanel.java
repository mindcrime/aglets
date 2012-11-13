package com.ibm.aglets.tahiti;

/*
 * @(#)MultiEditListPanel.java
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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MultiEditListPanel extends GridBagPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8831622465350074476L;
	private static final String LABEL_ADD = "add";
	private static final String LABEL_REMOVE = "remove";
	private static final String LABEL_MOVE_TO_TOP = "move to top";
	private static final String LABEL_MOVE_TO_LAST = "move to last";

	public static void main(final String arg[]) {
		final Frame frame = new Frame("test");

		frame.setSize(1000, 200);
		final int cols = 4;
		final double[] weight = new double[cols];

		weight[0] = 0.0;
		weight[1] = 0.0;
		weight[2] = 0.0;
		weight[3] = 0.0;

		final int[] width = new int[cols];

		width[0] = 5;
		width[1] = 5;
		width[2] = 10;
		width[3] = 5;

		final String[] items = new String[cols];

		items[0] = "item 1";
		items[1] = "ITEM 2";
		items[2] = "item 3";
		items[3] = "item 4";

		// MultiEditListPanel mpanel = new MultiEditListPanel(3, weight, items);
		// MultiEditListPanel mpanel = new MultiEditListPanel(3, items);
		final MultiEditListPanel mpanel = new MultiEditListPanel(3, width, items);

		frame.add(mpanel);

		// frame.pack();
		frame.show();
	}
	private MultiListEditable _mlist = null;

	GridBagLayout _layout = new GridBagLayout();

	MultiEditListPanel(final int rows, final double[] weight) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, weight);
		makePanel();
	}

	MultiEditListPanel(final int rows, final double[] weight, final String[] labels) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, weight, labels);
		makePanel();
	}

	MultiEditListPanel(final int rows, final int cols) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, cols);
		makePanel();
	}

	MultiEditListPanel(final int rows, final int[] width) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, width);
		makePanel();
	}

	MultiEditListPanel(final int rows, final int[] width, final double[] weight) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, width, weight);
		makePanel();
	}

	MultiEditListPanel(final int rows, final int[] width, final double[] weight, final String[] labels) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, width, weight, labels);
		makePanel();
	}

	MultiEditListPanel(final int rows, final int[] width, final String[] labels) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, width, labels);
		makePanel();
	}

	MultiEditListPanel(final int rows, final String[] labels) {
		setLayout(_layout);
		_mlist = new MultiListEditable(rows, labels);
		makePanel();
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		final String cmd = ev.getActionCommand();

		if (LABEL_ADD.equals(cmd)) {
			_mlist.addItemsInTextFields();
		} else if (LABEL_REMOVE.equals(cmd)) {
			_mlist.delSelectedItems();
		} else if (LABEL_MOVE_TO_TOP.equals(cmd)) {
			_mlist.moveToTop();
		} else if (LABEL_MOVE_TO_LAST.equals(cmd)) {
			_mlist.moveToLast();
		}
	}

	protected void addButton(
	                         final String label,
	                         final GridBagLayout layout,
	                         final GridBagConstraints cnt) {
		final Button button = new Button(label);

		layout.setConstraints(button, cnt);
		this.add(button);
		button.setActionCommand(label);
		button.addActionListener(this);
	}

	public void addButtons() {
		this.addButtons(true, true, true, true);
	}

	public void addButtons(
	                       final boolean add,
	                       final boolean remove,
	                       final boolean top,
	                       final boolean last) {
		final GridBagConstraints cnt = new GridBagConstraints();

		cnt.gridx = GridBagConstraints.RELATIVE;
		cnt.gridy = GridBagConstraints.RELATIVE;

		// cnt.gridy = 1;
		cnt.gridwidth = GridBagConstraints.RELATIVE;
		cnt.gridheight = 1;
		cnt.weightx = 0.0;
		cnt.weighty = 0.0;
		cnt.fill = GridBagConstraints.NONE;
		cnt.anchor = GridBagConstraints.WEST;
		cnt.ipadx = 2;
		cnt.ipady = 2;

		if (add) {
			addButton(LABEL_ADD, _layout, cnt);
		}
		if (remove) {
			addButton(LABEL_REMOVE, _layout, cnt);
		}
		if (top) {
			addButton(LABEL_MOVE_TO_TOP, _layout, cnt);
		}
		if (last) {
			addButton(LABEL_MOVE_TO_LAST, _layout, cnt);
		}
	}

	public void addEditFields() {
	}

	private void makePanel() {
		final GridBagConstraints cnt = new GridBagConstraints();

		cnt.gridwidth = GridBagConstraints.REMAINDER;
		_layout.setConstraints(_mlist, cnt);
		this.add(_mlist);
		this.addButtons(true, true, true, true);
	}
}
