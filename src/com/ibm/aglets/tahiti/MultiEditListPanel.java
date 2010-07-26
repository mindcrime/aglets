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
    private static final String LABEL_ADD = "add";
    private static final String LABEL_REMOVE = "remove";
    private static final String LABEL_MOVE_TO_TOP = "move to top";
    private static final String LABEL_MOVE_TO_LAST = "move to last";

    private MultiListEditable _mlist = null;
    GridBagLayout _layout = new GridBagLayout();

    MultiEditListPanel(int rows, double[] weight) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, weight);
	this.makePanel();
    }

    MultiEditListPanel(int rows, double[] weight, String[] labels) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, weight, labels);
	this.makePanel();
    }

    MultiEditListPanel(int rows, int[] width) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, width);
	this.makePanel();
    }

    MultiEditListPanel(int rows, int[] width, double[] weight) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, width, weight);
	this.makePanel();
    }

    MultiEditListPanel(int rows, int[] width, double[] weight, String[] labels) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, width, weight, labels);
	this.makePanel();
    }

    MultiEditListPanel(int rows, int[] width, String[] labels) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, width, labels);
	this.makePanel();
    }

    MultiEditListPanel(int rows, String[] labels) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, labels);
	this.makePanel();
    }

    MultiEditListPanel(int rows, int cols) {
	this.setLayout(this._layout);
	this._mlist = new MultiListEditable(rows, cols);
	this.makePanel();
    }

    public void actionPerformed(ActionEvent ev) {
	String cmd = ev.getActionCommand();

	if (LABEL_ADD.equals(cmd)) {
	    this._mlist.addItemsInTextFields();
	} else if (LABEL_REMOVE.equals(cmd)) {
	    this._mlist.delSelectedItems();
	} else if (LABEL_MOVE_TO_TOP.equals(cmd)) {
	    this._mlist.moveToTop();
	} else if (LABEL_MOVE_TO_LAST.equals(cmd)) {
	    this._mlist.moveToLast();
	}
    }

    protected void addButton(String label, GridBagLayout layout,
	    GridBagConstraints cnt) {
	Button button = new Button(label);

	layout.setConstraints(button, cnt);
	this.add(button);
	button.setActionCommand(label);
	button.addActionListener(this);
    }

    public void addButtons() {
	this.addButtons(true, true, true, true);
    }

    public void addButtons(boolean add, boolean remove, boolean top,
	    boolean last) {
	GridBagConstraints cnt = new GridBagConstraints();

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
	    this.addButton(LABEL_ADD, this._layout, cnt);
	}
	if (remove) {
	    this.addButton(LABEL_REMOVE, this._layout, cnt);
	}
	if (top) {
	    this.addButton(LABEL_MOVE_TO_TOP, this._layout, cnt);
	}
	if (last) {
	    this.addButton(LABEL_MOVE_TO_LAST, this._layout, cnt);
	}
    }

    public void addEditFields() {
    }

    public static void main(String arg[]) {
	Frame frame = new Frame("test");

	frame.setSize(1000, 200);
	final int cols = 4;
	double[] weight = new double[cols];

	weight[0] = 0.0;
	weight[1] = 0.0;
	weight[2] = 0.0;
	weight[3] = 0.0;

	int[] width = new int[cols];

	width[0] = 5;
	width[1] = 5;
	width[2] = 10;
	width[3] = 5;

	String[] items = new String[cols];

	items[0] = "item 1";
	items[1] = "ITEM 2";
	items[2] = "item 3";
	items[3] = "item 4";

	// MultiEditListPanel mpanel = new MultiEditListPanel(3, weight, items);
	// MultiEditListPanel mpanel = new MultiEditListPanel(3, items);
	MultiEditListPanel mpanel = new MultiEditListPanel(3, width, items);

	frame.add(mpanel);

	// frame.pack();
	frame.show();
    }

    private void makePanel() {
	GridBagConstraints cnt = new GridBagConstraints();

	cnt.gridwidth = GridBagConstraints.REMAINDER;
	this._layout.setConstraints(this._mlist, cnt);
	this.add(this._mlist);
	this.addButtons(true, true, true, true);
    }
}
