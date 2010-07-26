package com.ibm.aglets.tahiti;

/*
 * @(#)MultiListEditable.java
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

import java.awt.GridBagConstraints;
import java.awt.ItemSelectable;
import java.awt.TextField;

class MultiListEditable extends MultiList implements ItemSelectable {
    TextField[] _textFields = null;

    MultiListEditable(int rows, double[] weight) {
	super(rows, weight);
	this._textFields = new TextField[this._cols];
	this.createTextFields(weight);
    }

    MultiListEditable(int rows, double[] weight, String[] labels) {
	super(rows, weight, labels);
	this._textFields = new TextField[this._cols];
	this.createTextFields(weight);
    }

    MultiListEditable(int rows, int[] width) {
	super(rows, width.length);
	this._textFields = new TextField[this._cols];
	this.createTextFields(width);
    }

    MultiListEditable(int rows, int[] width, double[] weight) {
	super(rows, weight);
	this._textFields = new TextField[this._cols];
	this.createTextFields(weight, width);
    }

    MultiListEditable(int rows, int[] width, double[] weight, String[] labels) {
	super(rows, weight, labels);
	this._textFields = new TextField[this._cols];
	this.createTextFields(weight, width);
    }

    MultiListEditable(int rows, int[] width, String[] labels) {
	super(rows, labels);
	this._textFields = new TextField[this._cols];
	this.createTextFields(width);
    }

    MultiListEditable(int rows, String[] labels) {
	super(rows, labels);
	this._textFields = new TextField[this._cols];
	this.createTextFields();
    }

    MultiListEditable(int rows, int cols) {
	super(rows, cols);
	this._textFields = new TextField[this._cols];
	this.createTextFields();
    }

    public synchronized void addItemsInTextFields() {
	final int num = this.getItemCount();
	String[] items = new String[num];
	int i = 0;

	for (i = 0; i < num; i++) {
	    items[i] = this._textFields[i].getText();
	}
	this.addItems(items);
    }

    public void createTextFields() {
	GridBagConstraints cnt = this.createConstraints();

	cnt.fill = GridBagConstraints.HORIZONTAL;
	cnt.anchor = GridBagConstraints.WEST;

	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    TextField textField = new TextField();

	    this._textFields[i] = textField;
	    if (i == this._cols - 1) {
		cnt.gridwidth = GridBagConstraints.REMAINDER;
	    } else {
		cnt.gridwidth = GridBagConstraints.RELATIVE;
	    }
	    this._layout.setConstraints(textField, cnt);
	    this.add(textField);
	}
	this.nextComponents();
    }

    public void createTextFields(double[] weight) {
	GridBagConstraints cnt = this.createConstraints();

	cnt.gridy = this._line;
	cnt.fill = GridBagConstraints.HORIZONTAL;
	cnt.anchor = GridBagConstraints.WEST;

	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    TextField textField = new TextField();

	    this._textFields[i] = textField;
	    cnt.weightx = weight[i];
	    this._layout.setConstraints(textField, cnt);
	    this.add(textField);
	}
	this.nextComponents();
    }

    public void createTextFields(double[] weight, int[] width) {
	GridBagConstraints cnt = this.createConstraints();

	cnt.gridy = this._line;
	cnt.anchor = GridBagConstraints.WEST;

	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    TextField textField = new TextField(width[i]);

	    this._textFields[i] = textField;
	    cnt.weightx = weight[i];
	    this._layout.setConstraints(textField, cnt);
	    this.add(textField);
	}
	this.nextComponents();
    }

    public void createTextFields(int[] width) {
	GridBagConstraints cnt = this.createConstraints();

	cnt.anchor = GridBagConstraints.WEST;

	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    TextField textField = new TextField(width[i]);

	    this._textFields[i] = textField;
	    if (i == this._cols - 1) {
		cnt.gridwidth = GridBagConstraints.REMAINDER;
	    } else {
		cnt.gridwidth = GridBagConstraints.RELATIVE;
	    }
	    this._layout.setConstraints(textField, cnt);
	    this.add(textField);
	}
	this.nextComponents();
    }
}
