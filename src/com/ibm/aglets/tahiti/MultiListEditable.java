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

import java.util.Vector;
import java.util.Enumeration;
import java.awt.List;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Panel;
import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.awt.Button;

class MultiListEditable extends MultiList implements ItemSelectable {
	TextField[] _textFields = null;

	MultiListEditable(int rows, double[] weight) {
		super(rows, weight);
		_textFields = new TextField[_cols];
		createTextFields(weight);
	}
	MultiListEditable(int rows, double[] weight, String[] labels) {
		super(rows, weight, labels);
		_textFields = new TextField[_cols];
		createTextFields(weight);
	}
	MultiListEditable(int rows, int[] width) {
		super(rows, width.length);
		_textFields = new TextField[_cols];
		createTextFields(width);
	}
	MultiListEditable(int rows, int[] width, double[] weight) {
		super(rows, weight);
		_textFields = new TextField[_cols];
		createTextFields(weight, width);
	}
	MultiListEditable(int rows, int[] width, double[] weight, 
					  String[] labels) {
		super(rows, weight, labels);
		_textFields = new TextField[_cols];
		createTextFields(weight, width);
	}
	MultiListEditable(int rows, int[] width, String[] labels) {
		super(rows, labels);
		_textFields = new TextField[_cols];
		createTextFields(width);
	}
	MultiListEditable(int rows, String[] labels) {
		super(rows, labels);
		_textFields = new TextField[_cols];
		createTextFields();
	}
	MultiListEditable(int rows, int cols) {
		super(rows, cols);
		_textFields = new TextField[_cols];
		createTextFields();
	}
	public synchronized void addItemsInTextFields() {
		final int num = getItemCount();
		String[] items = new String[num];
		int i = 0;

		for (i = 0; i < num; i++) {
			items[i] = _textFields[i].getText();
		} 
		addItems(items);
	}
	public void createTextFields() {
		GridBagConstraints cnt = createConstraints();

		cnt.fill = GridBagConstraints.HORIZONTAL;
		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			TextField textField = new TextField();

			_textFields[i] = textField;
			if (i == _cols - 1) {
				cnt.gridwidth = GridBagConstraints.REMAINDER;
			} else {
				cnt.gridwidth = GridBagConstraints.RELATIVE;
			} 
			_layout.setConstraints(textField, cnt);
			add(textField);
		} 
		nextComponents();
	}
	public void createTextFields(double[] weight) {
		GridBagConstraints cnt = createConstraints();

		cnt.gridy = _line;
		cnt.fill = GridBagConstraints.HORIZONTAL;
		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			TextField textField = new TextField();

			_textFields[i] = textField;
			cnt.weightx = weight[i];
			_layout.setConstraints(textField, cnt);
			add(textField);
		} 
		nextComponents();
	}
	public void createTextFields(double[] weight, int[] width) {
		GridBagConstraints cnt = createConstraints();

		cnt.gridy = _line;
		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			TextField textField = new TextField(width[i]);

			_textFields[i] = textField;
			cnt.weightx = weight[i];
			_layout.setConstraints(textField, cnt);
			add(textField);
		} 
		nextComponents();
	}
	public void createTextFields(int[] width) {
		GridBagConstraints cnt = createConstraints();

		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			TextField textField = new TextField(width[i]);

			_textFields[i] = textField;
			if (i == _cols - 1) {
				cnt.gridwidth = GridBagConstraints.REMAINDER;
			} else {
				cnt.gridwidth = GridBagConstraints.RELATIVE;
			} 
			_layout.setConstraints(textField, cnt);
			add(textField);
		} 
		nextComponents();
	}
}
